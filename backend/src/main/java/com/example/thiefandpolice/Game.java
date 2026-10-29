package com.example.thiefandpolice;

import com.example.thiefandpolice.GameDtos.GameStateView;
import com.example.thiefandpolice.GameDtos.PlayerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class Game {

    public static final int PLAYERS_NEEDED = 4;

    private final String code;
    private final String hostToken;
    private final int totalRounds;
    private int currentRound = 0;
    private Phase phase = Phase.LOBBY;
    private final List<Player> players = new ArrayList<>();

    public Game(String code, int totalRounds) {
        this.code = code;
        this.totalRounds = totalRounds;
        this.hostToken = UUID.randomUUID().toString();
    }

    public synchronized Player addPlayer(String name) {
        if (phase != Phase.LOBBY) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game already started");
        }
        if (players.size() >= PLAYERS_NEEDED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game is full");
        }
        boolean nameTaken = players.stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(name));
        if (nameTaken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Name already taken");
        }
        Player player = new Player(name);
        players.add(player);
        return player;
    }

    public synchronized List<Player> getPlayers() {
        return List.copyOf(players);
    }

    public synchronized Optional<Player> findPlayerByToken(String token) {
        return players.stream()
                .filter(p -> p.getToken().equals(token))
                .findFirst();
    }

    // একই lock-এর ভেতরে পুরো snapshot তৈরি হয়, তাই মাঝপথে state বদলাতে পারে না
    public synchronized GameStateView viewFor(String token) {
        List<PlayerView> playerViews = players.stream()
                .map(p -> new PlayerView(p.getId(), p.getName(), p.getScore()))
                .toList();

        if (isHost(token)) {
            return new GameStateView(code, phase, currentRound, totalRounds,
                    PLAYERS_NEEDED, playerViews, "HOST", null);
        }

        Player me = findPlayerByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid token"));

        return new GameStateView(code, phase, currentRound, totalRounds,
                PLAYERS_NEEDED, playerViews, "PLAYER",
                new PlayerView(me.getId(), me.getName(), me.getScore()));
    }

    public boolean isHost(String token) {
        return hostToken.equals(token);
    }

    public String getCode() { return code; }
    public String getHostToken() { return hostToken; }
    public int getTotalRounds() { return totalRounds; }
    public synchronized int getCurrentRound() { return currentRound; }
    public synchronized Phase getPhase() { return phase; }
}