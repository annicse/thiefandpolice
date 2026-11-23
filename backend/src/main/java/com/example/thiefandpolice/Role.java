package com.example.thiefandpolice;

import java.util.ArrayList;
import java.util.List;

// সব চরিত্রের তালিকা ও টাকার হিসাব এক জায়গায় (config-এর মতো)।
// নতুন outlaw যোগ করতে শুধু নিচে এক লাইন বাড়ালেই হয়।
public enum Role {
    KING(Kind.KING, 1000),         // প্রতি রাউন্ডে নির্দিষ্ট বেতন
    POLICE(Kind.POLICE, 0),        // নিজের কোনো বেতন নেই: শুধু ধরা outlaw-এর loot পায়
    VILLAGER(Kind.VILLAGER, 200),  // নির্দোষ, প্রতি রাউন্ডে নির্দিষ্ট বেতন

    // outlaw-রা। এই ক্রমেই খেলোয়াড় বাড়লে একে একে খেলায় ঢোকে (৪ জনে শুধু THIEF)
    THIEF(Kind.OUTLAW, 500),
    BURGLAR(Kind.OUTLAW, 700),
    SMUGGLER(Kind.OUTLAW, 800),
    PICKPOCKET(Kind.OUTLAW, 400),
    GANGSTER(Kind.OUTLAW, 900),
    DAKAT(Kind.OUTLAW, 1000),
    KIDNAPPER(Kind.OUTLAW, 1100),
    // ১১ জনের বেশি খেলোয়াড় হলে এগুলো ঢোকে (১২ জন থেকে)
    TOKAI(Kind.OUTLAW, 300),
    DALAL(Kind.OUTLAW, 600),
    JAAL_NOTE(Kind.OUTLAW, 650),
    CHANDABAZ(Kind.OUTLAW, 750),
    MASTAN(Kind.OUTLAW, 850),
    HIJACKER(Kind.OUTLAW, 950),
    HITMAN(Kind.OUTLAW, 1200),
    DON(Kind.OUTLAW, 1500);

    // চরিত্রের ধরন: নিয়ম লেখার সময় "outlaw কি না" এভাবে জিজ্ঞেস করা যায়
    public enum Kind { KING, POLICE, VILLAGER, OUTLAW }

    public static final int MIN_PLAYERS = 4;
    public static final int MAX_PLAYERS = 20;
    // King আর Police সবসময় একজন করে; Villager কত জন, সেটা villagersFor(n) ঠিক করে
    private static final int KING_AND_POLICE = 2;

    private final Kind kind;
    private final int payout; // outlaw হলে এটাই তার loot

    Role(Kind kind, int payout) {
        this.kind = kind;
        this.payout = payout;
    }

    public Kind getKind() {
        return kind;
    }

    public int getPayout() {
        return payout;
    }

    public boolean isOutlaw() {
        return kind == Kind.OUTLAW;
    }

    // খেলোয়াড় বেশি হলে Villager বাড়ে: ৪-১০ জনে ১, ১১-১৫ জনে ২, ১৬-২০ জনে ৩
    public static int villagersFor(int playerCount) {
        if (playerCount <= 10) {
            return 1;
        }
        return playerCount <= 15 ? 2 : 3;
    }

    // এই সংখ্যক খেলোয়াড়ের গেমে কোন কোন চরিত্র থাকবে (প্রতি খেলোয়াড়ের জন্য একটা)।
    // King, Police আর প্রতিটা outlaw আলাদা; শুধু Villager একাধিক হতে পারে (কেউ নাম ধরে তাদের খোঁজে না)
    public static List<Role> rolesFor(int playerCount) {
        if (playerCount < MIN_PLAYERS || playerCount > MAX_PLAYERS) {
            throw new IllegalArgumentException(
                    "Players must be between " + MIN_PLAYERS + " and " + MAX_PLAYERS);
        }
        int villagers = villagersFor(playerCount);
        int outlawCount = playerCount - KING_AND_POLICE - villagers;

        List<Role> roles = new ArrayList<>(List.of(KING, POLICE));
        for (int i = 0; i < villagers; i++) {
            roles.add(VILLAGER);
        }
        List<Role> outlaws = new ArrayList<>();
        for (Role role : values()) { // values() declaration-এর ক্রমে দেয়
            if (role.isOutlaw()) {
                outlaws.add(role);
            }
        }
        roles.addAll(outlaws.subList(0, outlawCount));
        return roles;
    }
}
