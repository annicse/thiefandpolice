package com.example.thiefandpolice;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GameService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ";

    private static final Set<Integer> ALLOWED_ROUNDS = Set.of(10, 20);

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

    private String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }
}