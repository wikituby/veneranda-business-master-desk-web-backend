package org.example.messages;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.configuration.security.JwtUtils;
import org.example.user.domains.User;
import org.example.statics.AppConstants;

@ApplicationScoped
public class EmailService {

    @Inject
    ReactiveMailer reactiveMailer;

    @Inject
    Template reset;

    @Inject
    Template signup;

    @Inject
    Template notify;

    @Inject
    JwtUtils jwtUtils;

    public Uni<Void> sendCreationCredential(User user, String plainPassword){

        String mailData = signup.data("email", user.email, "username", user.username, "password", plainPassword,"link", AppConstants.LINK).render();
        return reactiveMailer.send(Mail.withHtml(user.email, "Sign Credentials",mailData));
    }

    public Uni<Void> sendPasswordResetLink(User user, String generatedPassword){
        String resetToken = jwtUtils.generateResetToken(user.email);
        String mailData = reset
                .data("username", user.username)
                .data("token", resetToken)
                .data("loginUrl", AppConstants.LOGIN_URL)
                .data("logo", AppConstants.LOGO)
                .data("link", AppConstants.LINK)
                .data("password", generatedPassword)
                .render();
        return reactiveMailer.send(Mail.withHtml(user.email, "Password Reset",mailData));
    }

    public Uni<Void> notifyAgentOfTicket(User user, String ticketNo){
        String mailData = notify.data("username", user.username, "ticketNo", ticketNo, "loginUrl", AppConstants.LOGIN_URL, "link", "#","logo","#")
                .render();

        return reactiveMailer.send(Mail.withHtml(user.email, "New Ticket",mailData));
    }
}






