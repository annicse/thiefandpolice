package com.example.thiefandpolice;

public enum Role {
    KING(1000),
    QUEEN(800),
    POLICE(500),
    THIEF(0);

    private final int payout;

    Role(int payout) {
        this.payout = payout;
    }

    public int getPayout() {
        return payout;
    }
}
