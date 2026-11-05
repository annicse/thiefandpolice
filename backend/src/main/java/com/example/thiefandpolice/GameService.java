package com.example.thiefandpolice;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GameService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ";

    private static final Set<Integer> ALLOWED_ROUNDS = Set.of(10, 20);

    // এতক্ষণ কোনো কাজ না হলে গেমটা মুছে ফেলা হয়
    private static final Duration MAX_IDLE = Duration.ofHours(3);

    private final Map<String, Game> games = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public Game createGame(int totalRounds) {
        if (!ALLOWED_ROUNDS.contains(totalRounds)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "totalRounds must be 10 or 20");
        }
        while (true) {
            String code = generateCode();
            Game game = new Game(code, totalRounds);
            if (games.putIfAbsent(code, game) == null) {
                return game;
            }
        }
    }

    public Game getGame(String code) {
        Game game = games.get(code.toUpperCase());
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        return game;
    }

    // প্রতি ১০ মিনিটে চলে। সার্ভার চালু থাকা অবস্থায় গেম জমে মেমোরি ভরে যাওয়া ঠেকায়
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void removeIdleGames() {
        removeIdleGames(MAX_IDLE);
    }

    // আলাদা মেথড রাখা হয়েছে যাতে টেস্টে ইচ্ছামতো সীমা দিয়ে পরীক্ষা করা যায়
    void removeIdleGames(Duration maxIdle) {
        games.values().removeIf(game -> game.isIdleFor(maxIdle));
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }
}