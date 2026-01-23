package com.pandaismyname1.coins.database;

import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Base class for all data handlers in the system.
 * Automatically registers with the DatabaseManager and provides helper methods for database operations.
 *
 * To create a new data handler:
 * 1. Extend this class
 * 2. Implement initializeTable() to create your tables
 * 3. Add your business logic methods
 * 4. Instantiate it - it will auto-register with the DatabaseManager
 */
public abstract class DataHandler {

    @Getter
    protected final DatabaseManager databaseManager;

    /**
     * Creates a new data handler and automatically registers it with the DatabaseManager.
     *
     * @param databaseManager The DatabaseManager to register with
     */
    protected DataHandler(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        databaseManager.registerHandler(this);
    }

    /**
     * Initialize tables and schema for this handler.
     * Called automatically when the DatabaseManager connects.
     */
    public abstract void initializeTable() throws SQLException;

    /**
     * Gets the name of this handler for logging purposes.
     * Override to provide a custom name.
     */
    public String getHandlerName() {
        return this.getClass().getSimpleName();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Gets a connection from the pool.
     */
    protected Connection getConnection() throws SQLException {
        return databaseManager.getConnection();
    }

    /**
     * Executes an update (INSERT, UPDATE, DELETE) and returns affected rows.
     * Uses varargs for parameters.
     *
     * @param sql SQL query string
     * @param params Parameters to bind to the query (in order)
     * @return Number of affected rows
     */
    protected int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            return stmt.executeUpdate();
        }
    }

    /**
     * Executes an update (INSERT, UPDATE, DELETE) and returns affected rows.
     * Uses List for parameters - works well with MySQLUtil.addTokens().
     *
     * @param sql SQL query string
     * @param params List of parameters to bind to the query
     * @return Number of affected rows
     */
    protected int executeUpdate(String sql, List<?> params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            return stmt.executeUpdate();
        }
    }

    /**
     * Executes a query and processes the ResultSet with the provided function.
     * Uses varargs for parameters.
     *
     * @param sql SQL query string
     * @param handler Function to process the ResultSet and return a result
     * @param params Parameters to bind to the query
     * @return The result from the handler function
     */
    protected <T> T executeQuery(String sql, Function<ResultSet, T> handler, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                return handler.apply(rs);
            }
        }
    }

    /**
     * Executes a query and processes the ResultSet with the provided function.
     * Uses List for parameters - works well with MySQLUtil.addTokens().
     *
     * @param sql SQL query string
     * @param handler Function to process the ResultSet and return a result
     * @param params List of parameters to bind to the query
     * @return The result from the handler function
     */
    protected <T> T executeQuery(String sql, List<?> params, Function<ResultSet, T> handler) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                return handler.apply(rs);
            }
        }
    }

    /**
     * Executes a query and processes each row with the provided consumer.
     * Uses varargs for parameters.
     *
     * @param sql SQL query string
     * @param rowHandler Consumer to process each row in the ResultSet
     * @param params Parameters to bind to the query
     */
    protected void executeQuery(String sql, Consumer<ResultSet> rowHandler, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rowHandler.accept(rs);
                }
            }
        }
    }

    /**
     * Executes a query and processes each row with the provided consumer.
     * Uses List for parameters - works well with MySQLUtil.addTokens().
     *
     * @param sql SQL query string
     * @param params List of parameters to bind to the query
     * @param rowHandler Consumer to process each row in the ResultSet
     */
    protected void executeQuery(String sql, List<?> params, Consumer<ResultSet> rowHandler) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rowHandler.accept(rs);
                }
            }
        }
    }

    /**
     * Executes a batch update operation.
     *
     * @param sql SQL query string with ? placeholders
     * @param batchBuilder Consumer that adds batches to the PreparedStatement
     * @return Array of update counts for each batch
     */
    protected int[] executeBatch(String sql, Consumer<PreparedStatement> batchBuilder) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            batchBuilder.accept(stmt);
            return stmt.executeBatch();
        }
    }

    /**
     * Helper to set parameters on a PreparedStatement from varargs.
     */
    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    /**
     * Helper to set parameters on a PreparedStatement from a List.
     */
    private void setParameters(PreparedStatement stmt, List<?> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }
    }
}
