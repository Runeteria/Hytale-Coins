package com.pandaismyname1.coins;

import com.pandaismyname1.coins.api.CoinsAPIProvider;
import com.pandaismyname1.coins.command.EconomyCommand;
import com.pandaismyname1.coins.command.PayCommand;
import com.pandaismyname1.coins.command.WalletCommand;
import com.pandaismyname1.coins.config.ConfigManager;
import com.pandaismyname1.coins.database.DatabaseManager;
import com.pandaismyname1.coins.database.EconomyDataHandler;
import com.pandaismyname1.coins.economy.CoinsAPIImpl;
import com.pandaismyname1.coins.economy.WalletManager;
import com.pandaismyname1.coins.interaction.DepositCoinInteraction;
import com.pandaismyname1.coins.listener.CropHarvestListener;
import com.pandaismyname1.coins.listener.MobDeathListener;
import com.pandaismyname1.coins.listener.PlayerListener;
import com.pandaismyname1.coins.plugins.VaultUnlockedPlugin;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.pandaismyname1.coins.ui.WalletPage;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import lombok.Getter;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class Main extends JavaPlugin {
    @Getter private static Main instance;
    @Getter private DatabaseManager databaseManager;
    @Getter private EconomyDataHandler economyDataHandler;

    public Main(@NonNullDecl JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        super.setup();

        // Initialize database
        this.databaseManager = new DatabaseManager(this);

        // Load config
        ConfigManager.load();

        // Register API
        CoinsAPIProvider.register(new CoinsAPIImpl());

        // Register commands
        this.getCommandRegistry().registerCommand(new WalletCommand());
        this.getCommandRegistry().registerCommand(new PayCommand());
        this.getCommandRegistry().registerCommand(new EconomyCommand());

        // Register Custom UI
        OpenCustomUIInteraction.registerSimple(this, WalletPage.class, "Coins_Wallet", WalletPage::new);

        // Register custom interaction
        Interaction.CODEC.register("DepositCoin", DepositCoinInteraction.class, DepositCoinInteraction.CODEC);

        // TEMP fix Feb 13, 26
        // Register mob death listener
        //this.getEntityStoreRegistry().registerSystem(new MobDeathListener());

        // TEMP fix Feb 13, 26
        // Register crop harvest listener
        //this.getEntityStoreRegistry().registerSystem(new CropHarvestListener());

        // Register player join/quit listeners for cross-server wallet sync
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerListener::onPlayerReady);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, PlayerListener::onPlayerDisconnect);

        try {
            Class.forName("net.milkbowl.vault2.economy.Economy");
            VaultUnlockedPlugin.setup(this.getLogger());
        } catch (ClassNotFoundException e) {
            this.getLogger().atInfo().log("Vault2 not found. Skipping Vault integration.");
        }

        // Load DB values from the config
        String dbHost = ConfigManager.getConfig().getHost();
        int dbPort = ConfigManager.getConfig().getPort();
        String dbName = ConfigManager.getConfig().getDatabase();
        String dbUser = ConfigManager.getConfig().getUsername();
        String dbPassword = ConfigManager.getConfig().getPassword();

        // Connect to database
        this.databaseManager.connect(dbHost, dbPort, dbName, dbUser, dbPassword);

        // Initialize economy data handler (auto-registers with DatabaseManager)
        this.economyDataHandler = new EconomyDataHandler(this.databaseManager);
    }

    @Override
    public void shutdown() {
        WalletManager.saveAll();
        CoinsAPIProvider.unregister();
        if (this.databaseManager != null) {
            this.databaseManager.shutdown();
        }
        super.shutdown();
    }
}

