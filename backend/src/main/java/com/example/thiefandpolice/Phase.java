package com.example.thiefandpolice;

public enum Phase {
    LOBBY,
    KING_REVEAL,
    POLICE_REVEAL,
    TARGET,       // the King picks which outlaw role the Police must find
    GUESSING,
    RESULT,
    GAME_OVER
}
