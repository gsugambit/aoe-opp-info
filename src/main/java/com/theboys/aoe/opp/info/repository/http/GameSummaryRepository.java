package com.theboys.aoe.opp.info.repository.http;

import com.theboys.aoe.opp.info.dto.internal.ScrapedGameSummary;
import com.theboys.aoe.opp.info.exception.BusinessException;
import com.theboys.aoe.opp.info.exception.NotFoundException;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class GameSummaryRepository {

    // Regex pattern to extract game ID from URL
    private static final Pattern GAME_ID_PATTERN = Pattern.compile("/games/(\\d+)");
    private static final String USER_AGENT = "GSUGambit-aoe-opp-info";
    private static final int DEFAULT_LIMIT = 10;

    private final String apiKey;

    public GameSummaryRepository(@Value("${aoe4world.apiKey:}") final String apiKey) {
        this.apiKey = apiKey;
    }

    public String buildProfileUrl(
            final String profileId,
            final String leaderboard,
            final Integer page,
            final String civilization
    ) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(String.format("https://aoe4world.com/players/%s/games", profileId))
                .queryParam("mode", leaderboard)
                .queryParam("page", page);

        if (StringUtils.isNotBlank(civilization)) {
            uriBuilder.queryParam("civilization", civilization);
        }

        if (StringUtils.isNotBlank(apiKey)) {
            uriBuilder.queryParam("api_key", apiKey);
        }

        return uriBuilder.toUriString();
    }

    public String buildProfileUrl(final String profileId, final String leaderboard, final Integer page) {
        return buildProfileUrl(profileId, leaderboard, page, null);
    }

    public String scrape(
            final String profileId,
            final String gamerName,
            final String gameId,
            final String leaderboard,
            final Integer page
    ) {
        final String profileUrl = buildProfileUrl(profileId, leaderboard, page);

        try {
            LOGGER.info("Fetching HTML from {}...", profileUrl);

            final Document doc = Jsoup.connect(profileUrl)
                    .userAgent(USER_AGENT)
                    .header("sec-fetch-dest", "document")
                    .get();

            LOGGER.trace("gamerProfile: {}. documentHtml: {}", profileId, doc.html());

            final Set<String> signedUrls = extractSignedUrls(doc);

            LOGGER.info("For gamer profile: {} found these signed URLs: {}", gamerName, signedUrls.size());

            final String gamerIdentifier = String.format("%s-%s", profileId, gamerName);

            final Optional<String> gameUrl = signedUrls.stream()
                    .filter(url -> url.contains(gamerIdentifier) && url.contains(gameId))
                    .findFirst();

            if (gameUrl.isEmpty()) {
                throw new NotFoundException(
                        "No game URL found for gamerIdentifier: " + gamerIdentifier + " and gameId: " + gameId
                );
            }

            final String signedGameUrl = gameUrl.get();
            LOGGER.info("Found game URL for gameId: {}: {}", gameId, signedGameUrl);

            try (final HttpClient client = HttpClient.newHttpClient()) {
                final Optional<String> summaryJson = fetchGameSummaryJson(client, signedGameUrl);
                if (summaryJson.isPresent()) {
                    LOGGER.info("Successfully fetched game summary for gameId: {}", gameId);
                    return summaryJson.get();
                } else {
                    throw new BusinessException("Failed to fetch game summary from AOE4World for gameId: " + gameId);
                }
            }
        } catch (final NotFoundException e) {
            throw e;
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
            throw new BusinessException("Error scraping game summary", e);
        }
    }

    public Map<String, String> scrapeRecentGameUrls(
            final String profileId,
            final String gamerName,
            final String civilization,
            final String leaderboard,
            final Integer limit
    ) {
        final int maxGames = (limit != null && limit > 0) ? limit : DEFAULT_LIMIT;
        final String profileUrl = buildProfileUrl(profileId, leaderboard, 1, civilization);

        try {
            LOGGER.info("Fetching HTML from {} for player game URLs...", profileUrl);

            final Document doc = Jsoup.connect(profileUrl)
                    .userAgent(USER_AGENT)
                    .header("sec-fetch-dest", "document")
                    .get();

            final Set<String> signedUrls = extractSignedUrls(doc);
            LOGGER.info("Found {} signed URLs for gamer: {} ({})", signedUrls.size(), gamerName, profileId);

            final Map<String, String> gameIdToUrlMap = new LinkedHashMap<>();
            for (final String signedUrl : signedUrls) {
                if (gameIdToUrlMap.size() >= maxGames) {
                    break;
                }
                final Optional<String> extractedGameId = extractGameId(signedUrl);
                if (extractedGameId.isPresent() && !gameIdToUrlMap.containsKey(extractedGameId.get())) {
                    gameIdToUrlMap.put(extractedGameId.get(), signedUrl);
                }
            }

            return gameIdToUrlMap;
        } catch (final Exception e) {
            LOGGER.warn("Error scraping recent game URLs for profileId: {} - {}", profileId, e.getMessage(), e);
            throw new BusinessException("Error scraping player games for profileId: " + profileId, e);
        }
    }

    public List<ScrapedGameSummary> scrapePlayerGames(
            final String profileId,
            final String gamerName,
            final String civilization,
            final String leaderboard,
            final Integer limit
    ) {
        final Map<String, String> gameIdToUrlMap = scrapeRecentGameUrls(
                profileId,
                gamerName,
                civilization,
                leaderboard,
                limit
        );

        final List<ScrapedGameSummary> results = new ArrayList<>();
        try (final HttpClient client = HttpClient.newHttpClient()) {
            for (final Map.Entry<String, String> entry : gameIdToUrlMap.entrySet()) {
                final String gameId = entry.getKey();
                final String signedUrl = entry.getValue();

                try {
                    final Optional<String> summaryJson = fetchGameSummaryJson(client, signedUrl);
                    if (summaryJson.isPresent()) {
                        results.add(new ScrapedGameSummary(gameId, summaryJson.get()));
                        LOGGER.info("Successfully scraped game summary for gameId: {}", gameId);
                    } else {
                        LOGGER.warn("Skipping gameId: {} due to unsuccessful fetch", gameId);
                    }
                } catch (final Exception ex) {
                    LOGGER.warn("Failed to fetch summary for gameId: {} - {}", gameId, ex.getMessage());
                }
            }
        }

        return results;
    }

    public Set<String> extractSignedUrls(final Document doc) {
        final Set<String> signedUrls = new LinkedHashSet<>();
        if (doc == null) {
            return signedUrls;
        }

        final Elements links = doc.select("a[data-href*=/games/][data-href*=sig=]");
        for (final Element link : links) {
            final String href = link.attr("data-href");
            final String fullUrl = href.startsWith("/") ? "https://aoe4world.com" + href : href;
            signedUrls.add(fullUrl);
        }
        return signedUrls;
    }

    public Optional<String> extractGameId(final String url) {
        if (StringUtils.isBlank(url)) {
            return Optional.empty();
        }
        final Matcher matcher = GAME_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }

    public Optional<String> fetchGameSummaryJson(final String signedGameUrl) {
        try (final HttpClient client = HttpClient.newHttpClient()) {
            return fetchGameSummaryJson(client, signedGameUrl);
        }
    }

    public Optional<String> fetchGameSummaryJson(final HttpClient client, final String signedGameUrl) {
        final String actualUrl = signedGameUrl.replace("?", "/summary?camelize=true&");
        LOGGER.trace("Fetching JSON for: {}", actualUrl);

        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(actualUrl))
                    .header("User-Agent", USER_AGENT)
                    .header("sec-fetch-dest", "empty")
                    .GET()
                    .build();

            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                final String responseBody = response.body();
                LOGGER.trace("{} response: {}", actualUrl, responseBody);
                return Optional.of(responseBody);
            } else {
                LOGGER.warn("Failed to fetch from: {} Status code: {}", actualUrl, response.statusCode());
                return Optional.empty();
            }
        } catch (final Exception e) {
            LOGGER.warn("Error fetching game summary from {}: {}", actualUrl, e.getMessage());
            return Optional.empty();
        }
    }
}