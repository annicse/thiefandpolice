package com.example.thiefandpolice;

public enum Role {
    KING(1000),
    QUEEN(800),
    POLICE(500),
    THIEF(0);

    private final int points;

    Role(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }
}
