package com.pandaismyname1.coins.listener;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.pandaismyname1.coins.economy.Wallet;
import com.pandaismyname1.coins.economy.WalletManager;
import com.pandaismyname1.coins.database.EconomyDataHandler;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Handles player join/quit events for proper wallet caching and cross-server sync.
 *
 * On join: Always reloads wallet from database to get the latest balance from other servers
 * On quit: Saves wallet to database and removes from cache to free memory
 */
public class PlayerListener {
    private static final Logger LOGGER = Logger.getLogger(PlayerListener.class.getName());

    /**
     * Called when a player is ready (joined the server and loaded).
     * Forces a wallet reload from the database to ensure cross-server sync.
     */
    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUuid();

        LOGGER.fine("Player ready: " + player.getDisplayName() + " (" + playerUuid + ") - reloading wallet from database");

        // Force reload from database to get latest balance from other servers
        EconomyDataHandler dbHandler = EconomyDataHandler.getInstance();
        if (dbHandler != null && dbHandler.isAvailable()) {
            dbHandler.loadBalance(playerUuid).ifPresent(balance -> {
                // Update or create the wallet with fresh DB data
                Wallet wallet = new Wallet(playerUuid, balance);
                WalletManager.loadWalletDirect(playerUuid, wallet);
                LOGGER.fine("Loaded wallet for " + player.getDisplayName() + " from database: " + balance + " copper");
            });
        } else {
            LOGGER.warning("Database not available - cannot sync wallet for " + player.getDisplayName());
        }
    }

    /**
     * Called when a player disconnects from the server.
     * Saves the wallet to database and removes it from memory cache.
     */
    public static void onPlayerDisconnect(PlayerDisconnectEvent event) {
        UUID playerUuid = event.getPlayerRef().getUuid();

        LOGGER.fine("Player disconnect: " + playerUuid + " - saving wallet to database and unloading");

        // Save to database and remove from cache
        WalletManager.unloadWallet(playerUuid);
    }
}
