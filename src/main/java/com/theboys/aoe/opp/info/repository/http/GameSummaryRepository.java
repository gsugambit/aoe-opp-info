package com.theboys.aoe.opp.info.repository.http;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class GameSummaryRepository {

    public void scrape(String gamerProfile, String userAgent) {
        String profileUrl = String.format("https://aoe4world.com/players/%s/games", gamerProfile);

        try {
            LOGGER.info("Fetching HTML from {}...", profileUrl);

            Document doc = Jsoup.connect(profileUrl)
                    .userAgent(userAgent)
                    .header("sec-fetch-dest", "document")
                    .get();

            LOGGER.trace(doc.html());

            // 2. Extract signed URLs
            // We use a LinkedHashSet to automatically prevent duplicate URLs
            Set<String> signedUrls = new LinkedHashSet<>();

            // Jsoup magic: Select <a> tags where the href contains "/games/" AND contains "sig="
            Elements links = doc.select("a[data-href*=/games/][data-href*=sig=]");

            for (Element link : links) {
                String href = link.attr("data-href");
                // Convert relative paths to absolute URLs
                String fullUrl = href.startsWith("/") ? "https://aoe4world.com" + href : href;
                signedUrls.add(fullUrl);
            }

            LOGGER.info("Found {} signed game URLs! Starting JSON download...", signedUrls.size());

            signedUrls.forEach(url -> LOGGER.info("url: {}", url));

            try (HttpClient client = HttpClient.newHttpClient()) {

                final AtomicInteger counter = new AtomicInteger(1);
                for (String url : signedUrls) {


                    // TODO: for right now we're only receiving first until this is fully supported
                    if (counter.getAndIncrement() > 1) {
                        //not spamming the server
                        //Thread.sleep(3000);
                        continue;
                    }

                    String actualUrl = url.replace("?", "/summary?camelize=true&");

                    LOGGER.trace("Fetching JSON for: {}", actualUrl);

                    // Build the HTTP request
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(actualUrl))
                            .header("User-Agent", userAgent)
                            .header("sec-fetch-dest", "empty")
                            .GET()
                            .build();

                    // Send the request and get the response body as a String
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {

                        String responseBody = response.body();
                        LOGGER.trace("{} response: {}", actualUrl, responseBody);

                        // Extract the game ID from the URL for the filename
                        // NOTE: In Java, split() uses regex, so we must escape the question mark: "\\?"
                        String[] parts = url.split("/games/")[1].split("\\?");
                        String gameId = parts[0];

                        // Save the JSON string directly to a file
                        String fileName = "matches" + File.separator + "match_" + gameId + ".json";
                        Path filePath = Paths.get(fileName);
                        Files.writeString(filePath, response.body());

                        LOGGER.info("Saved {} successfully.", fileName);
                    } else {
                        LOGGER.warn("Failed to fetch from: {} Status code: {}", actualUrl, response.statusCode());
                    }
                }
            }

            LOGGER.info("Finished scraping the page!");

        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
}