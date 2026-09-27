package org.example.messages.endpoints;



import io.quarkus.websockets.next.OnClose;

import io.quarkus.websockets.next.OnOpen;

import io.quarkus.websockets.next.WebSocket;

import io.quarkus.websockets.next.WebSocketConnection;

import jakarta.inject.Inject;

import org.example.configuration.security.JwtUtils;

import org.example.messages.services.MessagingWebSocketRegistry;

import org.example.user.domains.User;

import org.example.user.domains.repositories.UserRepository;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;



import java.net.URLDecoder;

import java.nio.charset.StandardCharsets;



@WebSocket(path = "/messages/ws")

public class MessageWebSocketEndpoint {



    private static final Logger LOG = LoggerFactory.getLogger(MessageWebSocketEndpoint.class);



    @Inject

    MessagingWebSocketRegistry registry;



    @Inject

    JwtUtils jwtUtils;



    @Inject

    UserRepository userRepository;



    @OnOpen

    public void onOpen(WebSocketConnection connection) {

        String token = extractToken(connection.handshakeRequest().query());

        if (token == null || token.isBlank() || !jwtUtils.validateJwtToken(token)) {

            LOG.debug("Messaging WS rejected: invalid or missing token");

            connection.closeAndAwait();

            return;

        }



        try {

            String login = jwtUtils.getLoginFromJwtToken(token);

            User user = userRepository.findByUsernameOrEmailOptional(login).orElse(null);

            if (user == null) {

                LOG.debug("Messaging WS rejected: user not found for login {}", login);

                connection.closeAndAwait();

                return;

            }

            registry.register(user.id, connection);

            LOG.info("Messaging WS connected for user {} (connection {})", user.id, connection.id());

        } catch (Exception e) {

            LOG.debug("Messaging WS rejected: auth failed", e);

            connection.closeAndAwait();

        }

    }



    @OnClose

    public void onClose(WebSocketConnection connection) {

        registry.unregister(connection);

    }



    private static String extractToken(String query) {

        if (query == null || query.isBlank()) {

            return null;

        }

        for (String part : query.split("&")) {

            int eq = part.indexOf('=');

            if (eq <= 0) {

                continue;

            }

            String key = part.substring(0, eq);

            if ("token".equals(key)) {

                return URLDecoder.decode(part.substring(eq + 1), StandardCharsets.UTF_8);

            }

        }

        return null;

    }

}

