package com.theboys.aoe.opp.info.repository.http;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

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

    public void scrape() {
        String profileUrl = "https://aoe4world.com/players/3861757-GSUGambit/games";
        String userAgent = "user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36^";

        try {
            LOGGER.info("Fetching HTML from " + profileUrl + "...");

            // 1. Fetch and Parse the HTML using Jsoup
            Document doc = Jsoup.connect(profileUrl)
                    .userAgent(userAgent)
                    .header("Referer", "https://aoe4world.com/players/3861757")
                    .header("sec-fetch-dest", "document")
                    .get();

            LOGGER.info(doc.html());

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

            LOGGER.info("Found " + signedUrls.size() + " signed game URLs! Starting JSON download...\n");

            signedUrls.forEach(url -> LOGGER.info("url: {}", url));


            //3. Setup Java's built-in HttpClient (Available in Java 11+)
            try (HttpClient client = HttpClient.newHttpClient()) {

                final AtomicInteger counter = new AtomicInteger(1);
                for (String url : signedUrls) {

                    if (counter.getAndIncrement() > 1) {
                        continue;
                    }

                    String actualUrl = url.replace("?", "/summary?camelize=true&");

                    System.out.println("Fetching JSON for: " + actualUrl);

                    // Build the HTTP request
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(actualUrl))
                            .header("User-Agent", userAgent)
                            .header("sec-fetch-dest", "empty")
                            //.header("Accept", "text/html, text/vnd.turbo-stream.html")
                            //.header("Accept", "application/json") // Tell the server we want JSON
                            .GET()
                            .build();

                    // Send the request and get the response body as a String
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {

                        String responseBody = response.body();
                        LOGGER.info("{}", responseBody);

                        // Extract the game ID from the URL for the filename
                        // NOTE: In Java, split() uses regex, so we must escape the question mark: "\\?"
                        String[] parts = url.split("/games/")[1].split("\\?");
                        String gameId = parts[0];

                        // Save the JSON string directly to a file
                        Path filePath = Paths.get("match_" + gameId + ".json");
                        Files.writeString(filePath, response.body());

                        System.out.println("  -> Saved match_" + gameId + ".json successfully.");
                    } else {
                        System.out.println("  -> Failed to fetch. Status code: " + response.statusCode());
                    }

                    // -------------------------------------------------------------------
                    // 4. MANDATORY POLITE PAUSE - Keeps their servers happy!
                    // -------------------------------------------------------------------
                    Thread.sleep(3000); // Sleep for 3,000 milliseconds (3 seconds)
                }
            }

            LOGGER.info("Finished scraping the page!");

        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
}