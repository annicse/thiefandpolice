package com.example.thiefandpolice;

import java.util.ArrayList;
import java.util.List;

// সব চরিত্রের তালিকা ও টাকার হিসাব এক জায়গায় (config-এর মতো)।
// Money amounts come from GameConfig. To add an outlaw: add a loot constant there and one line here.
public enum Role {
    KING(Kind.KING, GameConfig.KING_SALARY),         // প্রতি রাউন্ডে নির্দিষ্ট বেতন
    POLICE(Kind.POLICE, GameConfig.POLICE_SALARY),        // নিজের কোনো বেতন নেই: শুধু ধরা outlaw-এর loot পায়
    VILLAGER(Kind.VILLAGER, GameConfig.VILLAGER_SALARY),  // নির্দোষ, প্রতি রাউন্ডে নির্দিষ্ট বেতন

    // outlaw-রা। এই ক্রমেই খেলোয়াড় বাড়লে একে একে খেলায় ঢোকে (৪ জনে শুধু THIEF)
    THIEF(Kind.OUTLAW, GameConfig.THIEF_LOOT),
    BURGLAR(Kind.OUTLAW, GameConfig.BURGLAR_LOOT),
    SMUGGLER(Kind.OUTLAW, GameConfig.SMUGGLER_LOOT),
    PICKPOCKET(Kind.OUTLAW, GameConfig.PICKPOCKET_LOOT),
    GANGSTER(Kind.OUTLAW, GameConfig.GANGSTER_LOOT),
    DAKAT(Kind.OUTLAW, GameConfig.DAKAT_LOOT),
    KIDNAPPER(Kind.OUTLAW, GameConfig.KIDNAPPER_LOOT),
    // ১১ জনের বেশি খেলোয়াড় হলে এগুলো ঢোকে (১২ জন থেকে)
    TOKAI(Kind.OUTLAW, GameConfig.TOKAI_LOOT),
    DALAL(Kind.OUTLAW, GameConfig.DALAL_LOOT),
    JAAL_NOTE(Kind.OUTLAW, GameConfig.JAAL_NOTE_LOOT),
    CHANDABAZ(Kind.OUTLAW, GameConfig.CHANDABAZ_LOOT),
    MASTAN(Kind.OUTLAW, GameConfig.MASTAN_LOOT),
    HIJACKER(Kind.OUTLAW, GameConfig.HIJACKER_LOOT),
    HITMAN(Kind.OUTLAW, GameConfig.HITMAN_LOOT),
    DON(Kind.OUTLAW, GameConfig.DON_LOOT);

    // চরিত্রের ধরন: নিয়ম লেখার সময় "outlaw কি না" এভাবে জিজ্ঞেস করা যায়
    public enum Kind { KING, POLICE, VILLAGER, OUTLAW }

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
        if (playerCount < GameConfig.MIN_PLAYERS || playerCount > GameConfig.MAX_PLAYERS) {
            throw new IllegalArgumentException(
                    "Players must be between " + GameConfig.MIN_PLAYERS + " and " + GameConfig.MAX_PLAYERS);
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
