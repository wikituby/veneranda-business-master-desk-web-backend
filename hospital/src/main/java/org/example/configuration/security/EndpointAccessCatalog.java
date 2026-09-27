package org.example.configuration.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry of protectable API endpoints and their linked overview page keys.
 * Keys are stored in {@link org.example.user.domains.RoleEndpointAccess}.
 */
public final class EndpointAccessCatalog {

    public record EndpointDefinition(
            String key,
            String label,
            String group,
            String httpMethod,
            String pathContains,
            String pageKey
    ) {}

    private static final List<EndpointDefinition> DEFINITIONS = List.of(
            new EndpointDefinition(
                    "stock.transfer.create",
                    "Transfer stock (POST)",
                    "Inventory — Stock Management",
                    "POST",
                    "/health_care/Business-item/transfer-stock",
                    "stock-transfer"
            ),
            new EndpointDefinition(
                    "stock.transfer.list",
                    "List stock transfers (GET)",
                    "Inventory — Stock Management",
                    "GET",
                    "/health_care/Business-item/stock-transfers",
                    "stock-transfer"
            )
    );

    private static final Map<String, EndpointDefinition> BY_KEY = buildKeyIndex();
    private static final Map<String, Set<String>> PAGE_TO_ENDPOINTS = buildPageIndex();

    private EndpointAccessCatalog() {
    }

    public static List<EndpointDefinition> allDefinitions() {
        return Collections.unmodifiableList(DEFINITIONS);
    }

    public static Optional<String> resolveEndpointKey(String httpMethod, String path) {
        if (httpMethod == null || path == null) {
            return Optional.empty();
        }
        String method = httpMethod.trim().toUpperCase(Locale.ROOT);
        String normalizedPath = path.trim();
        for (EndpointDefinition def : DEFINITIONS) {
            if (!def.httpMethod().equals(method)) {
                continue;
            }
            String needle = stripPrefix(def.pathContains());
            if (normalizedPath.contains(needle) || normalizedPath.contains(def.pathContains())) {
                return Optional.of(def.key());
            }
        }
        return Optional.empty();
    }

    public static Set<String> endpointKeysForPage(String pageKey) {
        if (pageKey == null || pageKey.isBlank()) {
            return Set.of();
        }
        return PAGE_TO_ENDPOINTS.getOrDefault(pageKey.trim(), Set.of());
    }

    public static Set<String> endpointKeysForPages(Iterable<String> pageKeys) {
        Set<String> keys = new LinkedHashSet<>();
        if (pageKeys == null) {
            return keys;
        }
        for (String page : pageKeys) {
            keys.addAll(endpointKeysForPage(page));
        }
        return keys;
    }

    public static Optional<EndpointDefinition> findByKey(String key) {
        return Optional.ofNullable(BY_KEY.get(key));
    }

    private static String stripPrefix(String pathContains) {
        return pathContains.replace("/health_care", "");
    }

    private static Map<String, EndpointDefinition> buildKeyIndex() {
        Map<String, EndpointDefinition> map = new LinkedHashMap<>();
        for (EndpointDefinition def : DEFINITIONS) {
            map.put(def.key(), def);
        }
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, Set<String>> buildPageIndex() {
        Map<String, Set<String>> map = new LinkedHashMap<>();
        for (EndpointDefinition def : DEFINITIONS) {
            if (def.pageKey() == null || def.pageKey().isBlank()) {
                continue;
            }
            map.computeIfAbsent(def.pageKey(), k -> new LinkedHashSet<>()).add(def.key());
        }
        Map<String, Set<String>> immutable = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> e : map.entrySet()) {
            immutable.put(e.getKey(), Set.copyOf(e.getValue()));
        }
        return Collections.unmodifiableMap(immutable);
    }

    public static List<String> parseEndpointKeysCsv(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        Set<String> valid = BY_KEY.keySet();
        List<String> out = new ArrayList<>();
        for (String part : raw.split(",")) {
            String key = part == null ? "" : part.trim();
            if (!key.isEmpty() && valid.contains(key)) {
                out.add(key);
            }
        }
        return out;
    }

    public static String endpointKeysToCsv(Iterable<String> keys) {
        if (keys == null) {
            return "";
        }
        Set<String> valid = BY_KEY.keySet();
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String key : keys) {
            if (key != null) {
                String k = key.trim();
                if (!k.isEmpty() && valid.contains(k)) {
                    normalized.add(k);
                }
            }
        }
        return String.join(",", normalized);
    }
}
