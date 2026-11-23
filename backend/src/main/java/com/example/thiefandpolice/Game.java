package com.example.thiefandpolice;

import com.example.thiefandpolice.GameDtos.GameStateView;
import com.example.thiefandpolice.GameDtos.PlayerView;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


public class Game {

    private final String code;
    private final String hostToken;
    private final int totalRounds;
    private int currentRound = 0;
    private Phase phase = Phase.LOBBY;
    private Role targetRole; // the outlaw role the King picked this round (null until chosen)
    private String guessedPlayerId; // এই রাউন্ডে police কাকে thief বলে ধরেছে (guess-এর আগে null)
    private final List<Player> players = new ArrayList<>();
    // শেষ কাজ (join/start/reveal/guess/next-round) কখন হয়েছে। /state polling এটা বদলায় না
    private Instant lastActivity = Instant.now();

    public Game(String code, int totalRounds) {
        this.code = code;
        this.totalRounds = totalRounds;
        this.hostToken = UUID.randomUUID().toString();
    }

    public synchronized Player addPlayer(String name) {
        return addPlayerWithToken(name, UUID.randomUUID().toString());
    }

    // host নিজেও খেলতে চাইলে: তার Player-এর token = hostToken। বাকি সব নিয়ম (সীমা, নামের মিল) সাধারণ player-এর মতোই
    public synchronized Player addHostPlayer(String name) {
        return addPlayerWithToken(name, hostToken);
    }

    private Player addPlayerWithToken(String name, String token) {
        if (phase != Phase.LOBBY) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game already started");
        }
        if (players.size() >= GameConfig.MAX_PLAYERS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game is full");
        }
        boolean nameTaken = players.stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(name));
        if (nameTaken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Name already taken");
        }
        Player player = new Player(name, token);
        players.add(player);
        touch();
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
        String guessedId = visibleGuessedId();
        Role target = visibleTargetRole();
        List<Role> outlawRoles = outlawRolesInPlay();

        boolean host = isHost(token);
        Optional<Player> meOpt = findPlayerByToken(token);

        // host খেলছে না (শুধু স্ক্রিন): player নেই, তাই HOST view
        if (meOpt.isEmpty() && host) {
            return new GameStateView(code, phase, currentRound, totalRounds,
                    GameConfig.MIN_PLAYERS, GameConfig.MAX_PLAYERS, playerViews, "HOST", true, null, null, kingId, policeId, guessedId,
                    outlawRoles, target);
        }

        Player me = meOpt
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid token"));

        // host নিজেও খেললে সে PLAYER view পায় (শুধু নিজের role), সাথে host = true
        return new GameStateView(code, phase, currentRound, totalRounds,
                GameConfig.MIN_PLAYERS, GameConfig.MAX_PLAYERS, playerViews, "PLAYER", host,
                toPublicView(me),
                me.getRole(), kingId, policeId, guessedId, outlawRoles, target);
    }

    // PlayerView বানানোর একমাত্র জায়গা, তাই role কখন যাবে সেই নিয়ম এখানেই আছে
    private PlayerView toPublicView(Player p) {
        boolean showRoles = phase == Phase.RESULT || phase == Phase.GAME_OVER;
        return new PlayerView(p.getId(), p.getName(), p.getMoney(),
                showRoles ? p.getRole() : null);
    }

    // king কে, সেটা শুধু reveal-এর পরের phase-গুলোতে সবাইকে জানানো হয়
    private String visibleKingId() {
        boolean revealed = phase == Phase.POLICE_REVEAL
                || phase == Phase.TARGET
                || phase == Phase.GUESSING
                || phase == Phase.RESULT
                || phase == Phase.GAME_OVER;
        return revealed ? idOfRole(Role.KING) : null;
    }

    // police কে, সেটা শুধু police reveal-এর পরের phase-গুলোতে সবাইকে জানানো হয়
    private String visiblePoliceId() {
        boolean revealed = phase == Phase.TARGET
                || phase == Phase.GUESSING
                || phase == Phase.RESULT
                || phase == Phase.GAME_OVER;
        return revealed ? idOfRole(Role.POLICE) : null;
    }

    // The target is public as soon as the King has chosen it: the Police needs it to guess
    private Role visibleTargetRole() {
        boolean revealed = phase == Phase.GUESSING
                || phase == Phase.RESULT
                || phase == Phase.GAME_OVER;
        return revealed ? targetRole : null;
    }

    // The list of outlaw roles in play is public; who holds which role stays secret.
    // Empty in the lobby, because the roles depend on how many players there will be.
    private List<Role> outlawRolesInPlay() {
        if (phase == Phase.LOBBY) {
            return List.of();
        }
        return Role.rolesFor(players.size()).stream().filter(Role::isOutlaw).toList();
    }

    // police কাকে বেছেছে, সেটা শুধু RESULT আর GAME_OVER-এ সবাইকে জানানো হয়
    private String visibleGuessedId() {
        boolean revealed = phase == Phase.RESULT || phase == Phase.GAME_OVER;
        return revealed ? guessedPlayerId : null;
    }

    private String idOfRole(Role role) {
        return players.stream()
                .filter(p -> p.getRole() == role)
                .map(Player::getId)
                .findFirst()
                .orElse(null);
    }

    // সফল কাজের শেষে ডাকা হয়: "গেমটা এখনো ব্যবহার হচ্ছে"
    private void touch() {
        lastActivity = Instant.now();
    }

    // শেষ কাজের পর limit-এর সমান বা বেশি সময় কেটে গেছে কি? (GameService পুরনো গেম মুছতে এটা ব্যবহার করে)
    public synchronized boolean isIdleFor(Duration limit) {
        return Duration.between(lastActivity, Instant.now()).compareTo(limit) >= 0;
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
        if (players.size() < GameConfig.MIN_PLAYERS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "At least " + GameConfig.MIN_PLAYERS + " players are needed to start the game");
        }
        
        dealRoles();

        phase = Phase.KING_REVEAL;
        currentRound = 1;
        touch();
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
        touch();
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
        phase = Phase.TARGET;
        touch();
    }

    // The King (who knows nobody's role) picks which outlaw role the Police must find
    public synchronized void chooseTarget(String token, Role role) {
        if (phase != Phase.TARGET) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not in the target phase");
        }
        Player me = findPlayerByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid token"));
        if (me.getRole() != Role.KING) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the king can choose the target");
        }
        if (!outlawRolesInPlay().contains(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That is not an outlaw role in this game");
        }
        targetRole = role;
        phase = Phase.GUESSING;
        touch();
    }

    // police একজনকে thief বলে ধরে; এখানেই টাকা (money) যোগ হয় এবং RESULT শুরু হয়
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

        Role guessedRole = guessed.getRole();
        // Right only if the Police found the player who holds the King's target role
        boolean correct = guessedRole == targetRole;
        for (Player p : players) {
            Role role = p.getRole();
            int amount = switch (role.getKind()) {
                // King আর Villager সবসময় নিজের নির্দিষ্ট বেতন পায়
                case KING, VILLAGER -> role.getPayout();
                // The Police gets the loot of the target outlaw he found; a wrong guess gives nothing
                case POLICE -> correct ? guessedRole.getPayout() : 0;
                // An outlaw keeps his loot unless he is the caught target.
                // A wrongly picked outlaw is not punished: he keeps his loot too.
                case OUTLAW -> correct && p == guessed ? 0 : role.getPayout();
            };
            p.addMoney(amount);
        }

        // নির্দোষ Villager-কে ধরলে Police তাকে জরিমানা দেয়: যা আছে তার বেশি দিতে পারে না
        if (guessedRole == Role.VILLAGER) {
            guessed.addMoney(me.takeMoney(GameConfig.VILLAGER_FINE));
        }

        this.guessedPlayerId = guessed.getId();
        phase = Phase.RESULT;
        touch();
    }

    // host RESULT দেখার পর চাপে: শেষ রাউন্ড হলে GAME_OVER, নইলে নতুন রাউন্ড শুরু
    public synchronized void nextRound(String token) {
        if (phase != Phase.RESULT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Round is not finished yet");
        }
        if (!isHost(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the host can start the next round");
        }

        touch();

        if (currentRound >= totalRounds) {
            phase = Phase.GAME_OVER;
            return;
        }

        currentRound++;
        guessedPlayerId = null;
        targetRole = null;
        dealRoles();
        phase = Phase.KING_REVEAL;
    }

    // প্রতিবার নতুন করে shuffle করে প্রত্যেককে একটা role দেয় (টাকা অপরিবর্তিত থাকে)
    private void dealRoles() {
        // খেলোয়াড় সংখ্যা অনুযায়ী যে চরিত্রগুলো খেলায় আছে, শুধু সেগুলোই shuffle হয়
        List<Role> roles = new ArrayList<>(Role.rolesFor(players.size()));
        Collections.shuffle(roles);
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setRole(roles.get(i));
        }
    }
}