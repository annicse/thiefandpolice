package com.example.thiefandpolice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class GameServiceTest {

    // সময়-নির্ভর টেস্টে বড় ফাঁক: ধীর মেশিনেও হঠাৎ ফেল না করার জন্য
    private static final long WAIT_MS = 150;
    private static final Duration LIMIT = Duration.ofMillis(100);

    private GameService service;

    @BeforeEach
    void setUp() {
        service = new GameService();
    }

    @Test
    void removeIdleGames_keepsRecentlyActiveGames() {
        Game game = service.createGame(10);

        service.removeIdleGames(Duration.ofHours(3));

        assertSame(game, service.getGame(game.getCode()));
    }

    @Test
    void removeIdleGames_removesGamesIdleForAtLeastTheLimit() {
        Game game = service.createGame(10);

        // Duration.ZERO: "কমপক্ষে ০ সময় নিষ্ক্রিয়" মানে সব গেমই মুছবে
        service.removeIdleGames(Duration.ZERO);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getGame(game.getCode()));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void removeIdleGames_removesOnlyTheIdleOnes() throws InterruptedException {
        Game old = service.createGame(10);
        Thread.sleep(WAIT_MS);
        Game fresh = service.createGame(10);

        // old প্রায় 150ms পুরনো (>= 100ms) তাই মুছবে; fresh এইমাত্র বানানো তাই থাকবে
        service.removeIdleGames(LIMIT);

        assertSame(fresh, service.getGame(fresh.getCode()));
        assertThrows(ResponseStatusException.class, () -> service.getGame(old.getCode()));
    }

    @Test
    void actionsResetTheIdleClock() throws InterruptedException {
        Game game = service.createGame(10);
        Thread.sleep(WAIT_MS);
        assertTrue(game.isIdleFor(LIMIT));

        game.addPlayer("Rahim"); // কাজ হলো, ঘড়ি রিসেট

        assertFalse(game.isIdleFor(LIMIT));
    }

    @Test
    void pollingState_doesNotResetTheIdleClock() throws InterruptedException {
        Game game = service.createGame(10);
        Player p = game.addPlayer("Rahim");
        Thread.sleep(WAIT_MS);

        game.viewFor(p.getToken()); // শুধু state পড়া, কাজ না

        assertTrue(game.isIdleFor(LIMIT));
    }
}
