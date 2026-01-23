package com.pandaismyname1.coins.economy;

import com.pandaismyname1.coins.config.ConfigManager;

public enum Coin {
    COPPER("Coin_Copper", 1),
    SILVER("Coin_Silver", 100),
    GOLD("Coin_Gold", 1000);

    private final String itemId;
    private final long value;

    Coin(String itemId, long value) {
        this.itemId = itemId;
        this.value = value;
    }

    public String getItemId() {
        return itemId;
    }

    public long getValue() {
        return ConfigManager.getConfig().getCoinValues().getOrDefault(itemId, value);
    }

    public static Coin fromItemId(String itemId) {
        for (Coin coin : values()) {
            if (coin.itemId.equals(itemId)) {
                return coin;
            }
        }
        return null;
    }
}
