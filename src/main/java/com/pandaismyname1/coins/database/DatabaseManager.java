package com.pandaismyname1.coins.database;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.pandaismyname1.coins.Main;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class DatabaseManager {
    @Getter private final List<DataHandler> dataHandlers = new ArrayList<>();
    @Getter private JavaPlugin plugin;

    @Getter private HikariDataSource dataSource;
    private ExecutorService executorService;
    private String serverId;

    private volatile boolean connected = false;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers a DataHandler with this manager.
     * Called automatically by DataHandler constructor.
     */
    public void registerHandler(DataHandler handler) {
        dataHandlers.add(handler);
        Main.getInstance().getLogger().atInfo()
                .log("Registered DataHandler: " + handler.getHandlerName());

        // If already connected, initialize the handler immediately
        if (isConnected()) {
            try {
                handler.initializeTable();
                Main.getInstance().getLogger().atInfo()
                        .log("Initialized table for: " + handler.getHandlerName());
            } catch (SQLException e) {
                Main.getInstance().getLogger().atSevere()
                        .withCause(e)
                        .log("Failed to initialize table for: " + handler.getHandlerName());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CONNECTION MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Connects to MariaDB and initializes the connection pool.
     * Call this during plugin setup.
     *
     * @param host     Database host
     * @param port     Database port (default 3306)
     * @param database Database name
     * @param username Database username
     * @param password Database password
     * @param serverId Unique identifier for this server instance
     */
    public void connect(String host, int port, String database, String username, String password, String serverId) {
        try {
            this.serverId = serverId;

            // Explicitly load the MariaDB driver (required in plugin classloader environments)
            Class.forName("org.mariadb.jdbc.Driver");

            HikariConfig config = new HikariConfig();
            config.setDriverClassName("org.mariadb.jdbc.Driver");
            config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database);
            config.setUsername(username);
            config.setPassword(password);

            // Connection pool settings
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(300000);       // 5 minutes
            config.setConnectionTimeout(10000);  // 10 seconds
            config.setMaxLifetime(1800000);      // 30 minutes

            // Performance settings
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            this.dataSource = new HikariDataSource(config);

            this.connected = true;

            initHandlers();

            Main.getInstance().getLogger().atInfo()
                    .log("DatabaseManager connected to MariaDB at " + host + ":" + port + "/" + database);

        } catch (Exception e) {
            Main.getInstance().getLogger().atSevere()
                    .withCause(e)
                    .log("Failed to connect DatabaseManager to MariaDB");
        }
    }

    /**
     * Initializes all registered data handlers.
     * Called automatically after successful database connection.
     */
    private void initHandlers() {
        for (DataHandler handler : dataHandlers) {
            try {
                handler.initializeTable();
                Main.getInstance().getLogger().atInfo()
                        .log("Initialized table for: " + handler.getHandlerName());
            } catch (SQLException e) {
                Main.getInstance().getLogger().atSevere()
                        .withCause(e)
                        .log("Failed to initialize table for: " + handler.getHandlerName());
            }
        }
    }

    /**
     * Gets a connection from the connection pool.
     * Used by DataHandlers to execute queries.
     */
    public Connection getConnection() throws SQLException {
        if (!isConnected()) {
            throw new SQLException("Database is not connected");
        }
        return dataSource.getConnection();
    }

    /**
     * Gracefully shuts down database connections.
     * Call this during plugin shutdown.
     */
    public void shutdown() {
        connected = false;

        try {
            // Wait for pending operations if executor exists
            if (executorService != null) {
                executorService.shutdown();
            }

            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }

            Main.getInstance().getLogger().atInfo()
                    .log("DatabaseManager disconnected from MariaDB");

        } catch (Exception e) {
            Main.getInstance().getLogger().atWarning()
                    .withCause(e)
                    .log("Error during DatabaseManager shutdown");
        }
    }

    public boolean isConnected() {
        return connected && dataSource != null && !dataSource.isClosed();
    }
}



