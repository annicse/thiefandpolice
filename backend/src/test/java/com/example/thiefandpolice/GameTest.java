package com.example.thiefandpolice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game("ABCD", 10);
    }

    @Test
    void addPlayer_addsPlayerToLobby() {
        Player p = game.addPlayer("Rahim");

        assertEquals("Rahim", p.getName());
        assertEquals(1, game.getPlayers().size());
    }

    @Test
    void addPlayer_rejectsDuplicateNameIgnoringCase() {
        game.addPlayer("Rahim");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> game.addPlayer("rAHIM"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals(1, game.getPlayers().size());
    }

    @Test
    void addPlayer_rejectsPlayerOverTheMaximum() {
        for (int i = 1; i <= Game.MAX_PLAYERS; i++) {
            game.addPlayer("P" + i);
        }

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> game.addPlayer("Extra"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals(Game.MAX_PLAYERS, game.getPlayers().size());
    }

    @Test
    void addPlayer_neverExceedsLimitUnderConcurrentJoins() throws Exception {
        int attempts = 40; // সীমার (২০) চেয়ে বেশি চেষ্টা
        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        CountDownLatch startSignal = new CountDownLatch(1);
        List<java.util.concurrent.Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < attempts; i++) {
            String name = "P" + i;
            futures.add(pool.submit(() -> {
                startSignal.await();
                try {
                    game.addPlayer(name);
                } catch (ResponseStatusException ignored) {
                    // full হলে reject হওয়াই প্রত্যাশিত
                }
                return null;
            }));
        }
        startSignal.countDown();
        for (var f : futures) {
            f.get();
        }
        pool.shutdown();

        assertEquals(Game.MAX_PLAYERS, game.getPlayers().size());
    }
}
