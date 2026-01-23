package com.pandaismyname1.coins.database;

import lombok.Getter;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.pandaismyname1.coins.database.MySQLUtil.*;

/**
 * Handles economy-related database operations.
 * Syncs player wallet balances to/from the database on join/leave/transaction.
 */
public class EconomyDataHandler extends DataHandler {

    private static final Logger LOGGER = Logger.getLogger(EconomyDataHandler.class.getName());

    @Getter
    private static EconomyDataHandler instance;

    /**
     * Creates a new EconomyDataHandler and registers it with the DatabaseManager.
     *
     * @param databaseManager The DatabaseManager to register with
     */
    public EconomyDataHandler(DatabaseManager databaseManager) {
        super(databaseManager);
        instance = this;
    }

    @Override
    public void initializeTable() throws SQLException {
        // Create wallets table for persistent storage
        executeUpdate("""
            CREATE TABLE IF NOT EXISTS economy_wallets (
                uuid VARCHAR(36) PRIMARY KEY,
                balance BIGINT NOT NULL DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_balance (balance DESC)
            )
        """);

        LOGGER.info("Economy wallets table initialized");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DATABASE SYNC OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    private static final String TABLE = "economy_wallets";
    private static final String COL_UUID = "uuid";
    private static final String COL_BALANCE = "balance";

    /**
     * Loads a player's balance from the database.
     * Call this on player join to sync from DB.
     *
     * @param playerUuid The player's UUID
     * @return Optional containing the balance if found, empty if player not in database
     */
    public Optional<Long> loadBalance(UUID playerUuid) {
        if (!databaseManager.isConnected()) {
            LOGGER.warning("Database not connected, cannot load balance for " + playerUuid);
            return Optional.empty();
        }

        String query = selectFrom(TABLE) + where(COL_UUID, playerUuid.toString()) + end();

        try {
            return executeQuery(
                query,
                rs -> {
                    try {
                        if (rs.next()) {
                            return Optional.of(rs.getLong(COL_BALANCE));
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return Optional.<Long>empty();
                }
            );
        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.WARNING, "Failed to load balance for " + playerUuid, e);
            return Optional.empty();
        }
    }

    /**
     * Saves a player's balance to the database.
     * Call this on player leave and after transactions.
     *
     * @param playerUuid The player's UUID
     * @param balance    The balance to save
     */
    public void saveBalance(UUID playerUuid, long balance) {
        if (!databaseManager.isConnected()) {
            LOGGER.warning("Database not connected, cannot save balance for " + playerUuid);
            return;
        }

        String query = insertInto(TABLE)
                + withFields(List.of(COL_UUID, COL_BALANCE))
                + withValues(List.of(playerUuid.toString(), balance))
                + onDuplicateKeyUpdate(COL_BALANCE, balance)
                + end();

        try {
            executeUpdate(query);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to save balance for " + playerUuid, e);
        }
    }

    /**
     * Checks if the database is available for operations.
     *
     * @return true if connected
     */
    public boolean isAvailable() {
        return databaseManager.isConnected();
    }
}
