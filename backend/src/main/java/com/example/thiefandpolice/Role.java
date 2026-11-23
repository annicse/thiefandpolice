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
    KIDNAPPER(Kind.OUTLAW, 1100);

    // চরিত্রের ধরন: নিয়ম লেখার সময় "outlaw কি না" এভাবে জিজ্ঞেস করা যায়
    public enum Kind { KING, POLICE, VILLAGER, OUTLAW }

    public static final int MIN_PLAYERS = 4;
    public static final int MAX_PLAYERS = 10;
    // King, Police, Villager ছাড়া বাকি সবাই outlaw
    private static final int NON_OUTLAW_COUNT = 3;

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

    // এই সংখ্যক খেলোয়াড়ের গেমে কোন কোন চরিত্র থাকবে (প্রতি খেলোয়াড়ের জন্য একটা, সব আলাদা)
    public static List<Role> rolesFor(int playerCount) {
        if (playerCount < MIN_PLAYERS || playerCount > MAX_PLAYERS) {
            throw new IllegalArgumentException(
                    "Players must be between " + MIN_PLAYERS + " and " + MAX_PLAYERS);
        }
        int outlawCount = playerCount - NON_OUTLAW_COUNT;

        List<Role> roles = new ArrayList<>();
        List<Role> outlaws = new ArrayList<>();
        for (Role role : values()) { // values() declaration-এর ক্রমে দেয়
            if (role.isOutlaw()) {
                outlaws.add(role);
            } else {
                roles.add(role);
            }
        }
        roles.addAll(outlaws.subList(0, outlawCount));
        return roles;
    }
}
