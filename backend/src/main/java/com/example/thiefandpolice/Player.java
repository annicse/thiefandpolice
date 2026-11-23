package com.example.thiefandpolice;

import java.util.UUID;

public class Player {

    private final String id;
    private final String name;
    private final String token;
    private Role role;   // খেলা শুরুর পর বসবে
    private int money;

    public Player(String name) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.token = UUID.randomUUID().toString();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getToken() { return token; }
    public Role getRole() { return role; }
    public int getMoney() { return money; }

    public void setRole(Role role) { this.role = role; }
    public void addMoney(int amount) { this.money += amount; }

    // সর্বোচ্চ maxAmount টাকা কেটে নেয়; যা আছে তার বেশি কাটে না (টাকা ০-এর নিচে নামে না)। আসলে কত কাটল সেটা ফেরত দেয়
    public int takeMoney(int maxAmount) {
        int taken = Math.min(maxAmount, money);
        this.money -= taken;
        return taken;
    }
}