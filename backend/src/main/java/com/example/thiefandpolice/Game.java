package com.example.thiefandpolice;

import com.example.thiefandpolice.GameDtos.GameStateView;
import com.example.thiefandpolice.GameDtos.PlayerView;
import java.util.ArrayList;
import java.util.Collections;
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
                .map(this::toPublicView)
                .toList();

        String kingId = visibleKingId();
        String policeId = visiblePoliceId();

        if (isHost(token)) {
            return new GameStateView(code, phase, currentRound, totalRounds,
                    PLAYERS_NEEDED, playerViews, "HOST", null, null, kingId, policeId);
        }

        Player me = findPlayerByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid token"));

        return new GameStateView(code, phase, currentRound, totalRounds,
                PLAYERS_NEEDED, playerViews, "PLAYER",
                toPublicView(me),
                me.getRole(), kingId, policeId);
    }

    // PlayerView বানানোর একমাত্র জায়গা, তাই role কখন যাবে সেই নিয়ম এখানেই আছে
    private PlayerView toPublicView(Player p) {
        boolean showRoles = phase == Phase.RESULT || phase == Phase.GAME_OVER;
        return new PlayerView(p.getId(), p.getName(), p.getScore(),
                showRoles ? p.getRole() : null);
    }

    // king কে, সেটা শুধু reveal-এর পরের phase-গুলোতে সবাইকে জানানো হয়
    private String visibleKingId() {
        boolean revealed = phase == Phase.POLICE_REVEAL
                || phase == Phase.GUESSING
                || phase == Phase.RESULT
                || phase == Phase.GAME_OVER;
        return revealed ? idOfRole(Role.KING) : null;
    }

    // police কে, সেটা শুধু police reveal-এর পরের phase-গুলোতে সবাইকে জানানো হয়
    private String visiblePoliceId() {
        boolean revealed = phase == Phase.GUESSING
                || phase == Phase.RESULT
                || phase == Phase.GAME_OVER;
        return revealed ? idOfRole(Role.POLICE) : null;
    }

    private String idOfRole(Role role) {
        return players.stream()
                .filter(p -> p.getRole() == role)
                .map(Player::getId)
                .findFirst()
                .orElse(null);
    }

    public boolean isHost(String token) {
        return hostToken.equals(token);
    }

    public String getCode() { return code; }
    public String getHostToken() { return hostToken; }
    public int getTotalRounds() { return totalRounds; }
    public synchronized int getCurrentRound() { return currentRound; }
    public synchronized Phase getPhase() { return phase; }

    public synchronized void start(String token) {
        if (phase != Phase.LOBBY) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game already started");
        }
        if (!isHost(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid token");
        }
        if (players.size() != PLAYERS_NEEDED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exactly " + PLAYERS_NEEDED + " players are needed to start the game");
        }
        
        List<Role> roles = new ArrayList<>(List.of(Role.values()));
        Collections.shuffle(roles);
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setRole(roles.get(i));
        }

        phase = Phase.KING_REVEAL;
        currentRound = 1;
    }

    // king নিজে বাটন চাপলে সবার স্ক্রিনে king-এর পরিচয় দেখানো শুরু হয়
    public synchronized void revealKing(String token) {
        if (phase != Phase.KING_REVEAL) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not in the king reveal phase");
        }
        Player me = findPlayerByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid token"));
        if (me.getRole() != Role.KING) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the king can reveal");
        }
        phase = Phase.POLICE_REVEAL;
    }

    // king "আমার police কে?" জিজ্ঞেস করার পর police নিজে বাটন চাপলে সবাই জানতে পারে
    public synchronized void revealPolice(String token) {
        if (phase != Phase.POLICE_REVEAL) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not in the police reveal phase");
        }
        Player me = findPlayerByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid token"));
        if (me.getRole() != Role.POLICE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the police can reveal");
        }
        phase = Phase.GUESSING;
    }

    // police একজনকে thief বলে ধরে; এখানেই score যোগ হয় এবং RESULT শুরু হয়
    public synchronized void guess(String token, String guessedPlayerId) {
        if (phase != Phase.GUESSING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not in the guessing phase");
        }
        Player me = findPlayerByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid token"));
        if (me.getRole() != Role.POLICE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the police can guess");
        }
        Player guessed = players.stream()
                .filter(p -> p.getId().equals(guessedPlayerId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown player"));
        if (guessed == me) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Police cannot guess themselves");
        }

        boolean correct = guessed.getRole() == Role.THIEF;
        for (Player p : players) {
            int points = switch (p.getRole()) {
                case KING, QUEEN -> p.getRole().getPoints();
                case POLICE -> correct ? Role.POLICE.getPoints() : 0;
                case THIEF -> correct ? 0 : Role.POLICE.getPoints();
            };
            p.setScore(p.getScore() + points);
        }

        phase = Phase.RESULT;
    }
}