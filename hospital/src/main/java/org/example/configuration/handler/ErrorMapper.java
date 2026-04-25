package org.example.configuration.handler;

import jakarta.json.Json;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ErrorMapper {

    static final String STATIC_FILES = "./static/error-logs/";

    private static final Logger LOG = LoggerFactory.getLogger(ErrorMapper.class.getName());

    ErrorMapper() {
    }

    @Provider
    public static class CustomErrorMapper implements ExceptionMapper<Exception> {

        public static void appendToFile(Exception systemException) {

            String fileName = LocalDate.now() + "-log.txt";

            String message = "\n" + LocalDateTime.now() + " " + systemException.getMessage() + "\n";

            Path targetLocation = Paths.get(STATIC_FILES);

            if (!Files.exists(targetLocation)) {
                try {
                    Files.createDirectories(targetLocation);
                } catch (IOException ex) {
                    LOG.error("Exception {}!", ex.getMessage());
                }
            }

            File logFile = new File(STATIC_FILES + fileName);

            try (FileWriter newFile = new FileWriter(logFile, true)) {

                newFile.write(message);
                BufferedWriter buffFile = new BufferedWriter(newFile);
                PrintWriter printFile = new PrintWriter(buffFile, true);
                systemException.printStackTrace(printFile);

            } catch (IOException e) {
                LOG.error("Exception {}!", e.getMessage());
            }

        }

        @Override
        public Response toResponse(Exception exception) {

            LOG.error("Exception encountered: {}", exception.getMessage());

            int code = 500;
            if (exception instanceof WebApplicationException webApplicationException) {
                code = webApplicationException.getResponse().getStatus();
            }

            appendToFile(exception);

            // Safely handle null values by providing a default message.
            String errorMessage = exception.getMessage() != null ? exception.getMessage() : "Unknown error occurred.";
            String stackTrace = exception.getStackTrace() != null ? exception.getStackTrace().toString() : "No stack trace available.";

            return Response.status(code)
                    .entity(Json.createObjectBuilder()
                            .add("error", errorMessage)
                            .add("stack", stackTrace)
                            .add("code", code)
                            .build())
                    .build();
        }
    }
}






