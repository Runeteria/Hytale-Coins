package com.pandaismyname1.coins.economy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Wallet {
    private final UUID owner;
    private long balance; // Total value in copper coins

    public Wallet(UUID owner, long balance) {
        this.owner = owner;
        this.balance = balance;
    }

    public long getBalance() {
        return balance;
    }

    public void add(long amount) {
        if (!(this.balance + amount < 0)) this.balance += amount;
        save();
    }

    public boolean remove(long amount) {
        if (this.balance >= amount) {
            this.balance -= amount;
            save();
            return true;
        }
        return false;
    }

    private void save() {
        WalletManager.saveWallet(owner);
    }

    public Map<Coin, Long> getBreakdown() {
        Map<Coin, Long> breakdown = new HashMap<>();
        long remaining = balance;
        Coin[] coins = Coin.values();
        for (int i = coins.length - 1; i >= 0; i--) {
            Coin coin = coins[i];
            long count = remaining / coin.getValue();
            if (count > 0) {
                breakdown.put(coin, count);
                remaining %= coin.getValue();
            }
        }
        return breakdown;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Map<Coin, Long> breakdown = getBreakdown();
        Coin[] coins = Coin.values();
        boolean first = true;
        for (int i = coins.length - 1; i >= 0; i--) {
            Coin coin = coins[i];
            if (breakdown.containsKey(coin)) {
                if (!first) sb.append(", ");
                sb.append(breakdown.get(coin)).append(" ").append(coin.name());
                first = false;
            }
        }
        if (sb.length() == 0) return "0 Copper";
        return sb.toString();
    }
}
