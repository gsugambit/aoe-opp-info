package com.theboys.aoe.opp.info.repository.http;

import com.theboys.aoe.opp.info.exception.BusinessException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class GameSummaryRepositoryTest {

    @Test
    public void testBuildProfileUrl_WithApiKey() {
        // given
        final String apiKey = "dc04b785cb8942fa82d0e2b61b45dc31";
        final GameSummaryRepository repository = new GameSummaryRepository(apiKey);
        final String profileId = "3861757";
        final String leaderboard = "rm_1v1";
        final Integer page = 1;

        // when
        final String url = repository.buildProfileUrl(profileId, leaderboard, page);

        // then
        final String expectedUrl = "https://aoe4world.com/players/3861757/games?mode=rm_1v1&page=1&api_key=dc04b785cb8942fa82d0e2b61b45dc31";
        assertEquals(expectedUrl, url);
    }

    @Test
    public void testBuildProfileUrl_WithoutApiKey_Null() {
        // given
        final GameSummaryRepository repository = new GameSummaryRepository(null);
        final String profileId = "3861757";
        final String leaderboard = "rm_2v2";
        final Integer page = 2;

        // when
        final String url = repository.buildProfileUrl(profileId, leaderboard, page);

        // then
        final String expectedUrl = "https://aoe4world.com/players/3861757/games?mode=rm_2v2&page=2";
        assertEquals(expectedUrl, url);
    }

    @Test
    public void testBuildProfileUrl_WithoutApiKey_EmptyOrBlank() {
        // given
        final GameSummaryRepository repository = new GameSummaryRepository("   ");
        final String profileId = "123456";
        final String leaderboard = "qm_1v1";
        final Integer page = 1;

        // when
        final String url = repository.buildProfileUrl(profileId, leaderboard, page);

        // then
        final String expectedUrl = "https://aoe4world.com/players/123456/games?mode=qm_1v1&page=1";
        assertEquals(expectedUrl, url);
    }

    @Test
    public void testBuildProfileUrl_WithCivilizationAndApiKey() {
        // given
        final String apiKey = "dc04b785cb8942fa82d0e2b61b45dc31";
        final GameSummaryRepository repository = new GameSummaryRepository(apiKey);
        final String profileId = "3861757";
        final String leaderboard = "rm_solo";
        final Integer page = 1;
        final String civilization = "english";

        // when
        final String url = repository.buildProfileUrl(profileId, leaderboard, page, civilization);

        // then
        final String expectedUrl = "https://aoe4world.com/players/3861757/games?mode=rm_solo&page=1&civilization=english&api_key=dc04b785cb8942fa82d0e2b61b45dc31";
        assertEquals(expectedUrl, url);
    }

    @Test
    public void testBuildProfileUrl_WithCivilization_NoApiKey() {
        // given
        final GameSummaryRepository repository = new GameSummaryRepository(null);
        final String profileId = "3861757";
        final String leaderboard = "rm_1v1_elo";
        final Integer page = 1;
        final String civilization = "french";

        // when
        final String url = repository.buildProfileUrl(profileId, leaderboard, page, civilization);

        // then
        final String expectedUrl = "https://aoe4world.com/players/3861757/games?mode=rm_1v1_elo&page=1&civilization=french";
        assertEquals(expectedUrl, url);
    }

    @Test
    public void testExtractGameId_Success() {
        // given
        final GameSummaryRepository repository = new GameSummaryRepository(null);
        final String signedUrl = "https://aoe4world.com/players/3861757-GSUGambit/games/145892301?sig=abcdef123456";

        // when
        final Optional<String> gameId = repository.extractGameId(signedUrl);

        // then
        assertTrue(gameId.isPresent());
        assertEquals("145892301", gameId.get());
    }

    @Test
    public void testExtractGameId_MissingOrBlank() {
        // given
        final GameSummaryRepository repository = new GameSummaryRepository(null);

        // when / then
        assertFalse(repository.extractGameId(null).isPresent());
        assertFalse(repository.extractGameId("").isPresent());
        assertFalse(repository.extractGameId("https://aoe4world.com/players/3861757").isPresent());
    }

    @Test
    public void testExtractSignedUrls_Success() {
        // given
        final GameSummaryRepository repository = new GameSummaryRepository(null);
        final String html = "<div>"
                + "<a data-href=\"/players/3861757-GSUGambit/games/101?sig=aaa\">Game 1</a>"
                + "<a data-href=\"https://aoe4world.com/players/3861757-GSUGambit/games/102?sig=bbb\">Game 2</a>"
                + "<a data-href=\"/players/3861757-GSUGambit/games/101?sig=aaa\">Duplicate Game 1</a>"
                + "<a href=\"/players/3861757\">Profile</a>"
                + "</div>";
        final Document doc = Jsoup.parse(html);

        // when
        final Set<String> signedUrls = repository.extractSignedUrls(doc);

        // then
        assertEquals(2, signedUrls.size());
        assertTrue(signedUrls.contains("https://aoe4world.com/players/3861757-GSUGambit/games/101?sig=aaa"));
        assertTrue(signedUrls.contains("https://aoe4world.com/players/3861757-GSUGambit/games/102?sig=bbb"));
    }

    @Test
    public void testScrapeRecentGameUrls_Success() throws IOException {
        // given
        final GameSummaryRepository repository = new GameSummaryRepository(null);
        final String html = "<div>"
                + "<a data-href=\"/players/3861757-GSUGambit/games/101?sig=aaa\">Game 1</a>"
                + "<a data-href=\"/players/3861757-GSUGambit/games/102?sig=bbb\">Game 2</a>"
                + "<a data-href=\"/players/3861757-GSUGambit/games/103?sig=ccc\">Game 3</a>"
                + "</div>";
        final Document doc = Jsoup.parse(html);
        final Connection connection = mock(Connection.class);
        when(connection.userAgent(anyString())).thenReturn(connection);
        when(connection.header(anyString(), anyString())).thenReturn(connection);
        when(connection.get()).thenReturn(doc);

        try (final MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class, Mockito.CALLS_REAL_METHODS)) {
            mockedJsoup.when(() -> Jsoup.connect(anyString())).thenReturn(connection);

            // when
            final Map<String, String> result = repository.scrapeRecentGameUrls(
                    "3861757",
                    "GSUGambit",
                    "english",
                    "rm_solo",
                    2
            );

            // then
            assertEquals(2, result.size());
            assertTrue(result.containsKey("101"));
            assertTrue(result.containsKey("102"));
            assertFalse(result.containsKey("103"));
            assertEquals("https://aoe4world.com/players/3861757-GSUGambit/games/101?sig=aaa", result.get("101"));
            assertEquals("https://aoe4world.com/players/3861757-GSUGambit/games/102?sig=bbb", result.get("102"));
        }
    }

    @Test
    public void testScrapeRecentGameUrls_NetworkError() throws IOException {
        // given
        final GameSummaryRepository repository = new GameSummaryRepository(null);
        final Connection connection = mock(Connection.class);
        when(connection.userAgent(anyString())).thenReturn(connection);
        when(connection.header(anyString(), anyString())).thenReturn(connection);
        when(connection.get()).thenThrow(new IOException("Connection timed out"));

        try (final MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class, Mockito.CALLS_REAL_METHODS)) {
            mockedJsoup.when(() -> Jsoup.connect(anyString())).thenReturn(connection);

            // when / then
            assertThrows(BusinessException.class, () -> repository.scrapeRecentGameUrls(
                    "3861757",
                    "GSUGambit",
                    "english",
                    "rm_solo",
                    10
            ));
        }
    }
}
