package com.example.thiefandpolice;

// All tunable game numbers in one place. Change a value here and the whole game follows:
// the rules use it, and the frontend reads the public ones from GET /api/config.
// Role.java reads the money amounts below, one per role.
public final class GameConfig {

    private GameConfig() {}

    // Rounds the host can choose when creating a game
    public static final int MIN_ROUNDS = 3;
    public static final int MAX_ROUNDS = 50;
    public static final int DEFAULT_ROUNDS = 10;

    // Players allowed in one game (the roles in play are decided in Role.rolesFor)
    public static final int MIN_PLAYERS = 4;
    public static final int MAX_PLAYERS = 20;

    // Fine the Police pays to the Villager for wrongly picking him
    public static final int VILLAGER_FINE = 150;

    // Money per round for the non-outlaw roles
    public static final int KING_SALARY = 1000;
    public static final int VILLAGER_SALARY = 200;
    // The Police has no pay of his own: he only gets the loot of the outlaw he catches
    public static final int POLICE_SALARY = 0;

    // Loot of each outlaw. An outlaw keeps it unless the Police catches him.
    public static final int THIEF_LOOT = 500;
    public static final int BURGLAR_LOOT = 700;
    public static final int SMUGGLER_LOOT = 800;
    public static final int PICKPOCKET_LOOT = 400;
    public static final int GANGSTER_LOOT = 900;
    public static final int DAKAT_LOOT = 1000;
    public static final int KIDNAPPER_LOOT = 1100;
    public static final int TOKAI_LOOT = 300;
    public static final int DALAL_LOOT = 600;
    public static final int JAAL_NOTE_LOOT = 650;
    public static final int CHANDABAZ_LOOT = 750;
    public static final int MASTAN_LOOT = 850;
    public static final int HIJACKER_LOOT = 950;
    public static final int HITMAN_LOOT = 1200;
    public static final int DON_LOOT = 1500;
}
