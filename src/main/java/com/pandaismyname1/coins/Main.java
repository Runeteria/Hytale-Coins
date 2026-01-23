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
import com.pandaismyname1.coins.plugins.VaultUnlockedPlugin;
import com.pandaismyname1.coins.ui.WalletPage;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
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
        String dbHost = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "172.17.0.1";
        int dbPort = System.getenv("DB_PORT") != null ? Integer.parseInt(System.getenv("DB_PORT")) : 3306;
        String dbName = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "runeteria";
        String dbUser = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "runeteria";
        String dbPassword = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "wbn3wzk6HRQ3efv-yfr";
        String serverId = System.getenv("SERVER_ID") != null ? System.getenv("SERVER_ID") : "server-1";

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
        ((AssetCodecMapCodec) Interaction.CODEC).register("DepositCoin", DepositCoinInteraction.class, DepositCoinInteraction.CODEC);

        // Register mob death listener
        this.getEntityStoreRegistry().registerSystem(new MobDeathListener());

        // Register crop harvest listener
        this.getEntityStoreRegistry().registerSystem(new CropHarvestListener());

        try {
            Class.forName("net.milkbowl.vault2.economy.Economy");
            VaultUnlockedPlugin.setup(this.getLogger());
        } catch (ClassNotFoundException e) {
            this.getLogger().atInfo().log("Vault2 not found. Skipping Vault integration.");
        }

        // Connect to database
        this.databaseManager.connect(dbHost, dbPort, dbName, dbUser, dbPassword, serverId);

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

