package org.example.messages.services;



import io.quarkus.websockets.next.OpenConnections;

import io.quarkus.websockets.next.WebSocketConnection;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;

import jakarta.json.bind.Jsonb;

import org.example.messages.services.payloads.responses.MessagePushEvent;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;



import java.util.ArrayList;

import java.util.Map;

import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;



@ApplicationScoped

public class MessagingWebSocketRegistry {



    private static final Logger LOG = LoggerFactory.getLogger(MessagingWebSocketRegistry.class);



    @Inject

    Jsonb jsonb;



    @Inject

    OpenConnections openConnections;



    /** userId -> open connection ids (multiple tabs/devices). */

    private final Map<Long, Set<String>> connectionIdsByUser = new ConcurrentHashMap<>();

    private final Map<String, Long> userByConnectionId = new ConcurrentHashMap<>();



    public void register(Long userId, WebSocketConnection connection) {

        if (userId == null || connection == null) {

            return;

        }

        String connectionId = connection.id();

        userByConnectionId.put(connectionId, userId);

        connectionIdsByUser.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet()).add(connectionId);

        LOG.debug("Messaging WS registered user {} connection {}", userId, connectionId);

    }



    public void unregister(WebSocketConnection connection) {

        if (connection == null) {

            return;

        }

        unregister(connection.id());

    }



    public void unregister(String connectionId) {

        if (connectionId == null) {

            return;

        }

        Long userId = userByConnectionId.remove(connectionId);

        if (userId == null) {

            return;

        }

        Set<String> set = connectionIdsByUser.get(userId);

        if (set != null) {

            set.remove(connectionId);

            if (set.isEmpty()) {

                connectionIdsByUser.remove(userId);

            }

        }

        LOG.debug("Messaging WS unregistered user {} connection {}", userId, connectionId);

    }



    public void pushToUser(Long userId, MessagePushEvent event) {

        if (userId == null || event == null) {

            return;

        }

        Set<String> connectionIds = connectionIdsByUser.get(userId);

        if (connectionIds == null || connectionIds.isEmpty()) {

            return;

        }

        String payload = jsonb.toJson(event);

        for (String connectionId : new ArrayList<>(connectionIds)) {

            try {

                openConnections.findByConnectionId(connectionId).ifPresent(connection -> {

                    if (connection.isOpen()) {

                        connection.sendTextAndAwait(payload);

                    } else {

                        unregister(connectionId);

                    }

                });

            } catch (Exception e) {

                LOG.warn("Failed to push message event to connection {}", connectionId, e);

            }

        }

    }

    /** Push an event to every currently connected messaging WebSocket client. */
    public void broadcast(MessagePushEvent event) {
        if (event == null || connectionIdsByUser.isEmpty()) {
            return;
        }
        for (Long userId : new ArrayList<>(connectionIdsByUser.keySet())) {
            pushToUser(userId, event);
        }
    }

}

