package com.pandaismyname1.coins.economy;

import com.pandaismyname1.coins.database.EconomyDataHandler;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class WalletManager {
    private static final Logger LOGGER = Logger.getLogger(WalletManager.class.getName());
    private static final ConcurrentHashMap<UUID, Wallet> wallets = new ConcurrentHashMap<>();

    /**
     * Gets a wallet for the given player. Requires database connection.
     *
     * @param playerUuid The player's UUID
     * @return The wallet, or null if database is unavailable
     */
    public static Wallet getWallet(UUID playerUuid) {
        if (!isAvailable()) {
            LOGGER.warning("Economy unavailable - database not connected");
            return null;
        }
        return wallets.computeIfAbsent(playerUuid, WalletManager::loadWallet);
    }

    /**
     * Checks if the economy system is available (database connected).
     *
     * @return true if database is connected and economy is operational
     */
    public static boolean isAvailable() {
        EconomyDataHandler dbHandler = EconomyDataHandler.getInstance();
        return dbHandler != null && dbHandler.isAvailable();
    }

    private static Wallet loadWallet(UUID playerUuid) {
        EconomyDataHandler dbHandler = EconomyDataHandler.getInstance();
        Optional<Long> dbBalance = dbHandler.loadBalance(playerUuid);

        if (dbBalance.isPresent()) {
            LOGGER.fine("Loaded wallet for " + playerUuid + " from database: " + dbBalance.get());
            return new Wallet(playerUuid, dbBalance.get());
        }

        // New player - create with 0 balance and save to DB
        LOGGER.fine("Creating new wallet for " + playerUuid);
        dbHandler.saveBalance(playerUuid, 0);
        return new Wallet(playerUuid, 0);
    }

    public static void saveWallet(UUID playerUuid) {
        if (!isAvailable()) {
            LOGGER.warning("Cannot save wallet - database not connected");
            return;
        }

        Wallet wallet = wallets.get(playerUuid);
        if (wallet == null) return;

        EconomyDataHandler.getInstance().saveBalance(playerUuid, wallet.getBalance());
    }

    public static void saveAll() {
        if (!isAvailable()) {
            LOGGER.warning("Cannot save wallets - database not connected");
            return;
        }
        wallets.keySet().forEach(WalletManager::saveWallet);
    }

    /**
     * Removes a wallet from the cache (e.g., on player disconnect).
     * Saves to database before removing.
     *
     * @param playerUuid The player's UUID
     */
    public static void unloadWallet(UUID playerUuid) {
        saveWallet(playerUuid);
        wallets.remove(playerUuid);
    }
}
