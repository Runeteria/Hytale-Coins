package com.pandaismyname1.coins.config;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ModConfig {

    // SQL Support
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    private boolean enableMobDeathDrops = true;
    private float mobDeathDropRate = 0.1f;
    private boolean enableCropHarvestDrops = true;
    private float cropHarvestDropRate = 0.2f;
    private Map<String, Long> coinValues = new HashMap<>();
    private Map<String, Long> mobDeathDrops = new HashMap<>();

    public ModConfig() {
        // SQL Support
        host = "";
        port = 0;
        database = "";
        username = "";
        password = "";

        // Default values
        coinValues.put("Coin_Copper", 1L);
        coinValues.put("Coin_Silver", 100L);
        coinValues.put("Coin_Gold", 1000L);

        // Example custom mob drops
        mobDeathDrops.put("Fox", 3L);
        mobDeathDrops.put("Cow", 3L);
    }
}
