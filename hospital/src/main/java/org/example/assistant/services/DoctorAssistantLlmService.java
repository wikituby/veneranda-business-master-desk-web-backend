package org.example.assistant.services;



import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;

import jakarta.json.Json;

import jakarta.json.JsonObject;

import jakarta.json.JsonObjectBuilder;

import jakarta.json.JsonReader;

import org.eclipse.microprofile.config.Config;

import org.eclipse.microprofile.config.inject.ConfigProperty;



import java.io.StringReader;

import java.net.URI;

import java.net.http.HttpClient;

import java.net.http.HttpRequest;

import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;

import java.time.Duration;

import java.time.Instant;

import java.util.Optional;



/**

 * Server-side proxy to OpenAI-compatible chat/completions APIs.

 * Supports free tiers: Groq, OpenRouter (:free models), and local Ollama (no key).

 */

@ApplicationScoped

public class DoctorAssistantLlmService {



    @ConfigProperty(name = "doctor-assistant.llm.provider", defaultValue = "auto")

    String provider;



    @ConfigProperty(name = "doctor-assistant.llm.base-url", defaultValue = "https://api.groq.com/openai/v1")

    String legacyBaseUrl;



    @ConfigProperty(name = "doctor-assistant.llm.default-model", defaultValue = "llama-3.3-70b-versatile")

    String legacyDefaultModel;



    @ConfigProperty(name = "doctor-assistant.llm.groq.base-url", defaultValue = "https://api.groq.com/openai/v1")

    String groqBaseUrl;



    @ConfigProperty(name = "doctor-assistant.llm.groq.model", defaultValue = "llama-3.3-70b-versatile")

    String groqModel;



    @ConfigProperty(name = "doctor-assistant.llm.ollama.base-url", defaultValue = "http://127.0.0.1:11434/v1")

    String ollamaBaseUrl;



    @ConfigProperty(name = "doctor-assistant.llm.ollama.model", defaultValue = "llama3.2")

    String ollamaModel;



    @ConfigProperty(name = "doctor-assistant.llm.openrouter.base-url", defaultValue = "https://openrouter.ai/api/v1")

    String openRouterBaseUrl;



    @ConfigProperty(name = "doctor-assistant.llm.openrouter.model", defaultValue = "meta-llama/llama-3.3-70b-instruct:free")

    String openRouterModel;



    @Inject

    Config config;



    private final HttpClient httpClient = HttpClient.newBuilder()

            .connectTimeout(Duration.ofSeconds(15))

            .build();



    private volatile Boolean ollamaAvailableCache;

    private volatile Instant ollamaCheckedAt;



    /** Public config for the frontend (never exposes API keys). */

    public JsonObject getPublicConfig() {

        Optional<LlmTarget> active = resolveTarget(null, null, false);

        JsonObjectBuilder providers = Json.createObjectBuilder();



        boolean groqReady = hasKey("doctor-assistant.llm.groq.api-key");

        providers.add("groq", Json.createObjectBuilder()

                .add("label", "Groq (free tier)")

                .add("configured", groqReady)

                .add("model", groqModel)

                .add("signupUrl", "https://console.groq.com/keys")

                .build());



        boolean openRouterReady = hasKey("doctor-assistant.llm.openrouter.api-key");

        providers.add("openrouter", Json.createObjectBuilder()

                .add("label", "OpenRouter (free models)")

                .add("configured", openRouterReady)

                .add("model", openRouterModel)

                .add("signupUrl", "https://openrouter.ai/settings/keys")

                .build());



        boolean ollamaReady = isOllamaAvailable();

        providers.add("ollama", Json.createObjectBuilder()

                .add("label", "Ollama (local, free)")

                .add("configured", ollamaReady)

                .add("model", ollamaModel)

                .add("signupUrl", "https://ollama.com/download")

                .build());



        JsonObjectBuilder out = Json.createObjectBuilder()

                .add("provider", provider)

                .add("available", active.isPresent())

                .add("providers", providers);



        if (active.isPresent()) {

            LlmTarget t = active.get();

            out.add("activeProvider", t.providerName());

            out.add("model", t.defaultModel());

            out.add("keySource", t.keySource());

        } else {

            out.add("activeProvider", "");

            out.add("model", legacyDefaultModel);

            out.add("keySource", "none");

        }



        return out.build();

    }



    public JsonObject chat(JsonObject body, String requestApiKey) throws Exception {

        LlmTarget target = resolveTarget(requestApiKey, body, true)

                .orElseThrow(() -> new IllegalArgumentException(buildSetupHint()));



        String model = body.containsKey("model") && !body.isNull("model")

                ? body.getString("model")

                : target.defaultModel();



        var payloadBuilder = Json.createObjectBuilder()

                .add("model", model)

                .add("messages", body.getJsonArray("messages"));



        if (body.containsKey("tools") && !body.isNull("tools")) {

            payloadBuilder.add("tools", body.getJsonArray("tools"));

            payloadBuilder.add("tool_choice", "auto");

        }

        if (body.containsKey("temperature") && !body.isNull("temperature")) {

            payloadBuilder.add("temperature", body.getJsonNumber("temperature").doubleValue());

        } else {

            payloadBuilder.add("temperature", 0.2);

        }



        String url = target.baseUrl().endsWith("/")

                ? target.baseUrl() + "chat/completions"

                : target.baseUrl() + "/chat/completions";



        var requestBuilder = HttpRequest.newBuilder()

                .uri(URI.create(url))

                .timeout(Duration.ofSeconds(90))

                .header("Content-Type", "application/json");



        if (target.apiKey() != null && !target.apiKey().isBlank()) {

            requestBuilder.header("Authorization", "Bearer " + target.apiKey().trim());

        }



        HttpRequest request = requestBuilder

                .POST(HttpRequest.BodyPublishers.ofString(payloadBuilder.build().toString(), StandardCharsets.UTF_8))

                .build();



        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() < 200 || response.statusCode() >= 300) {

            throw new IllegalStateException("LLM API error HTTP " + response.statusCode() + ": " + response.body());

        }



        try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {

            return reader.readObject();

        }

    }



    private Optional<LlmTarget> resolveTarget(String requestApiKey, JsonObject body, boolean strict) {

        String clientKey = blankToNull(requestApiKey);

        if (clientKey != null) {

            return Optional.of(new LlmTarget(

                    legacyBaseUrl,

                    clientKey,

                    modelFromBody(body, legacyDefaultModel),

                    "client",

                    "browser"));

        }



        String forced = provider == null ? "auto" : provider.trim().toLowerCase();

        if ("groq".equals(forced)) {

            return groqTarget(body);

        }

        if ("openrouter".equals(forced)) {

            return openRouterTarget(body);

        }

        if ("ollama".equals(forced)) {

            return ollamaTarget(body, strict);

        }

        if ("openai".equals(forced) || "custom".equals(forced)) {

            return legacyTarget(body);

        }



        // auto: Groq → OpenRouter → legacy key → Ollama

        Optional<LlmTarget> groq = groqTarget(body);

        if (groq.isPresent()) {

            return groq;

        }

        Optional<LlmTarget> openRouter = openRouterTarget(body);

        if (openRouter.isPresent()) {

            return openRouter;

        }

        Optional<LlmTarget> legacy = legacyTarget(body);

        if (legacy.isPresent()) {

            return legacy;

        }

        return ollamaTarget(body, strict);

    }



    private Optional<LlmTarget> groqTarget(JsonObject body) {

        String key = configValue("doctor-assistant.llm.groq.api-key");

        if (key == null) {

            return Optional.empty();

        }

        return Optional.of(new LlmTarget(

                groqBaseUrl,

                key,

                modelFromBody(body, groqModel),

                "groq",

                "server"));

    }



    private Optional<LlmTarget> openRouterTarget(JsonObject body) {

        String key = configValue("doctor-assistant.llm.openrouter.api-key");

        if (key == null) {

            return Optional.empty();

        }

        return Optional.of(new LlmTarget(

                openRouterBaseUrl,

                key,

                modelFromBody(body, openRouterModel),

                "openrouter",

                "server"));

    }



    private Optional<LlmTarget> legacyTarget(JsonObject body) {

        String key = configValue("doctor-assistant.llm.api-key");

        if (key == null) {

            return Optional.empty();

        }

        return Optional.of(new LlmTarget(

                legacyBaseUrl,

                key,

                modelFromBody(body, legacyDefaultModel),

                "custom",

                "server"));

    }



    private Optional<LlmTarget> ollamaTarget(JsonObject body, boolean strict) {

        if (strict && !isOllamaAvailable()) {

            return Optional.empty();

        }

        if (!strict && !isOllamaAvailable()) {

            return Optional.empty();

        }

        return Optional.of(new LlmTarget(

                ollamaBaseUrl,

                null,

                modelFromBody(body, ollamaModel),

                "ollama",

                "local"));

    }



    private String modelFromBody(JsonObject body, String fallback) {

        if (body != null && body.containsKey("model") && !body.isNull("model")) {

            return body.getString("model");

        }

        return fallback;

    }



    private boolean hasKey(String property) {

        return configValue(property) != null;

    }



    private String configValue(String property) {

        return config.getOptionalValue(property, String.class)

                .map(String::trim)

                .filter(s -> !s.isEmpty())

                .orElse(null);

    }



    private String blankToNull(String value) {

        if (value == null || value.isBlank()) {

            return null;

        }

        return value.trim();

    }



    private boolean isOllamaAvailable() {

        Instant now = Instant.now();

        if (ollamaCheckedAt != null && ollamaAvailableCache != null

                && ollamaCheckedAt.plus(Duration.ofSeconds(45)).isAfter(now)) {

            return ollamaAvailableCache;

        }



        String probeUrl = ollamaBaseUrl.replace("/v1", "").replaceAll("/$", "") + "/api/tags";

        try {

            HttpRequest request = HttpRequest.newBuilder()

                    .uri(URI.create(probeUrl))

                    .timeout(Duration.ofSeconds(2))

                    .GET()

                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            ollamaAvailableCache = response.statusCode() >= 200 && response.statusCode() < 300;

        } catch (Exception e) {

            ollamaAvailableCache = false;

        }

        ollamaCheckedAt = now;

        return ollamaAvailableCache;

    }



    private String buildSetupHint() {

        return "No LLM configured. Free options: "

                + "(1) Groq — sign up at console.groq.com, set GROQ_API_KEY env var or doctor-assistant.llm.groq.api-key; "

                + "(2) OpenRouter — openrouter.ai free models, set OPENROUTER_API_KEY; "

                + "(3) Ollama — install locally (ollama.com), run `ollama pull llama3.2`. "

                + "Or pass apiKey from Assistant settings.";

    }



    private record LlmTarget(String baseUrl, String apiKey, String defaultModel, String providerName, String keySource) {}

}

