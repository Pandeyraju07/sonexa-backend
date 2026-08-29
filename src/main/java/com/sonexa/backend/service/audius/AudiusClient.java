package com.sonexa.backend.service.audius;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class AudiusClient {

    private static final Logger log = LoggerFactory.getLogger(AudiusClient.class);
    private static final String DEFAULT_APP_NAME = "TuneFlow";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("")
    private String primaryHost;

    @Value("")
    private String appName;

    private final List<String> fallbackHosts = List.of(
            "https://discoveryprovider.audius.co/v1",
            "https://audius-discovery-1.altego.net/v1",
            "https://audius-discovery-2.altego.net/v1",
            "https://audius-discovery-3.altego.net/v1",
            "https://discovery-us-01.audius.openplayer.org/v1"
    );

    public AudiusClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(6))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public JsonNode get(String endpointPath, String queryParams) {
        String baseParams = "app_name=" + URLEncoder.encode(appName != null ? appName : DEFAULT_APP_NAME, StandardCharsets.UTF_8);
        String fullQuery = (queryParams != null && !queryParams.isBlank())
                ? baseParams + "&" + queryParams
                : baseParams;

        List<String> hostsToTry = new ArrayList<>();
        if (primaryHost != null && !primaryHost.isBlank()) {
            hostsToTry.add(primaryHost.endsWith("/") ? primaryHost.substring(0, primaryHost.length() - 1) : primaryHost);
        }
        for (String fb : fallbackHosts) {
            if (!hostsToTry.contains(fb)) {
                hostsToTry.add(fb);
            }
        }

        for (String host : hostsToTry) {
            String path = endpointPath.startsWith("/") ? endpointPath : "/" + endpointPath;
            String url = host + path + "?" + fullQuery;

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Accept", "application/json")
                        .header("User-Agent", "TuneFlow/1.0 (https://tuneflow.app)")
                        .timeout(Duration.ofSeconds(7))
                        .GET()
                        .build();

                HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    try (InputStream is = response.body()) {
                        return objectMapper.readTree(is);
                    }
                }
            } catch (Exception e) {
                log.debug("Failed requesting Audius host [{}]: {}", host, e.getMessage());
            }
        }
        return null;
    }

    public String resolveStreamUrl(String trackId) {
        String baseHost = (primaryHost != null && !primaryHost.isBlank()) ? primaryHost : fallbackHosts.get(0);
        String cleanHost = baseHost.endsWith("/") ? baseHost.substring(0, baseHost.length() - 1) : baseHost;
        return cleanHost + "/tracks/" + trackId + "/stream?app_name=" + URLEncoder.encode(appName != null ? appName : DEFAULT_APP_NAME, StandardCharsets.UTF_8);
    }
}