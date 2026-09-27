package org.example.assistant.services.payloads;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

/** Proxied chat/completions request (OpenAI-compatible). */
public class DoctorAssistantLlmChatRequest {

    public JsonArray messages;
    public JsonArray tools;
    public String model;
    /** Optional per-request key; falls back to {@code doctor-assistant.llm.api-key} in config. */
    public String apiKey;
    public Double temperature;
}
