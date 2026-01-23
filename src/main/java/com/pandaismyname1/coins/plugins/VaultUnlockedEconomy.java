package com.pandaismyname1.coins.plugins;

import com.pandaismyname1.coins.economy.Wallet;
import com.pandaismyname1.coins.economy.WalletManager;
import net.milkbowl.vault2.economy.AccountPermission;
import net.milkbowl.vault2.economy.Economy;
import net.milkbowl.vault2.economy.EconomyResponse;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

public class VaultUnlockedEconomy implements Economy {
    private static final Logger LOGGER = Logger.getLogger(VaultUnlockedEconomy.class.getName());

    @Override
    public boolean isEnabled() {
        return WalletManager.isAvailable();
    }
    
    @Override
    public String getName() {
        return "Coins";
    }

    @Override
    public boolean hasSharedAccountSupport() {
        return false;
    }

    @Override
    public boolean hasMultiCurrencySupport() {
        return false;
    }

    @Override
    public int fractionalDigits( String pluginName) {
        return 0;
    }

    
    @Override
    public String format( BigDecimal amount) {
        return amount.toPlainString() + " Copper";
    }

    
    @Override
    public String format( String pluginName,  BigDecimal amount) {
        return format(amount);
    }

    
    @Override
    public String format( BigDecimal amount,  String currency) {
        if ("Copper".equalsIgnoreCase(currency)) {
            return format(amount);
        }
        return amount.toPlainString() + " " + currency;
    }

    
    @Override
    public String format( String pluginName,  BigDecimal amount,  String currency) {
        return format(amount, currency);
    }

    @Override
    public boolean hasCurrency( String currency) {
        return "Copper".equalsIgnoreCase(currency);
    }

    
    @Override
    public String getDefaultCurrency( String pluginName) {
        return "Copper";
    }

    
    @Override
    public String defaultCurrencyNamePlural( String pluginName) {
        return "Copper";
    }

    
    @Override
    public String defaultCurrencyNameSingular( String pluginName) {
        return "Copper";
    }

    @Override
    public  Collection<String> currencies() {
        return List.of("Copper");
    }

    @Override
    public boolean createAccount( UUID accountID,  String name) {
        Wallet wallet = WalletManager.getWallet(accountID);
        if (wallet == null) {
            LOGGER.warning("[ECONOMY_UNAVAILABLE] createAccount FAILED: account=" + accountID + ", name=" + name + " - database not connected");
            return false;
        }
        return true;
    }

    @Override
    public boolean createAccount( UUID accountID,  String name, boolean player) {
        return createAccount(accountID, name);
    }

    @Override
    public boolean createAccount( UUID accountID,  String name,  String worldName) {
        return createAccount(accountID, name);
    }

    @Override
    public boolean createAccount( UUID accountID,  String name,  String worldName, boolean player) {
        return createAccount(accountID, name);
    }

    @Override
    public  Map<UUID, String> getUUIDNameMap() {
        return Map.of();
    }

    @Override
    public Optional<String> getAccountName( UUID accountID) {
        return Optional.empty();
    }

    @Override
    public boolean hasAccount( UUID accountID) {
        return WalletManager.isAvailable();
    }

    @Override
    public boolean hasAccount( UUID accountID,  String worldName) {
        return hasAccount(accountID);
    }

    @Override
    public boolean renameAccount( UUID accountID,  String name) {
        return false;
    }

    @Override
    public boolean renameAccount( String plugin,  UUID accountID,  String name) {
        return false;
    }

    @Override
    public boolean deleteAccount( String plugin,  UUID accountID) {
        return false;
    }

    @Override
    public boolean accountSupportsCurrency( String plugin,  UUID accountID,  String currency) {
        return hasCurrency(currency);
    }

    @Override
    public boolean accountSupportsCurrency( String plugin,  UUID accountID,  String currency,  String world) {
        return hasCurrency(currency);
    }


    @Override
    public BigDecimal getBalance( String pluginName,  UUID accountID) {
        Wallet wallet = WalletManager.getWallet(accountID);
        if (wallet == null) {
            LOGGER.warning("[ECONOMY_UNAVAILABLE] getBalance FAILED: plugin=" + pluginName + ", account=" + accountID + " - database not connected");
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(wallet.getBalance());
    }

    
    @Override
    public BigDecimal getBalance( String pluginName,  UUID accountID,  String world) {
        return getBalance(pluginName, accountID);
    }

    
    @Override
    public BigDecimal getBalance( String pluginName,  UUID accountID,  String world,  String currency) {
        return getBalance(pluginName, accountID);
    }

    @Override
    public boolean has( String pluginName,  UUID accountID,  BigDecimal amount) {
        Wallet wallet = WalletManager.getWallet(accountID);
        if (wallet == null) {
            LOGGER.warning("[ECONOMY_UNAVAILABLE] has FAILED: plugin=" + pluginName + ", account=" + accountID + ", amount=" + amount + " - database not connected");
            return false;
        }
        return wallet.getBalance() >= amount.longValue();
    }

    @Override
    public boolean has( String pluginName,  UUID accountID,  String worldName,  BigDecimal amount) {
        return has(pluginName, accountID, amount);
    }

    @Override
    public boolean has( String pluginName,  UUID accountID,  String worldName,  String currency,  BigDecimal amount) {
        return has(pluginName, accountID, amount);
    }

    @Override
    public  EconomyResponse withdraw( String pluginName,  UUID accountID,  BigDecimal amount) {
        Wallet wallet = WalletManager.getWallet(accountID);
        if (wallet == null) {
            LOGGER.warning("[ECONOMY_UNAVAILABLE] WITHDRAW FAILED: plugin=" + pluginName + ", account=" + accountID + ", amount=" + amount + " - database not connected");
            return new EconomyResponse(amount, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, "Economy system unavailable");
        }
        long amountLong = amount.longValue();
        if (wallet.remove(amountLong)) {
            LOGGER.info("[TRANSACTION] VAULT_WITHDRAW: plugin=" + pluginName + ", account=" + accountID + ", amount=" + amountLong + ", newBalance=" + wallet.getBalance());
            return new EconomyResponse(amount, BigDecimal.valueOf(wallet.getBalance()), EconomyResponse.ResponseType.SUCCESS, "");
        } else {
            return new EconomyResponse(amount, BigDecimal.valueOf(wallet.getBalance()), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }
    }

    
    @Override
    public EconomyResponse withdraw( String pluginName,  UUID accountID,  String worldName,  BigDecimal amount) {
        return withdraw(pluginName, accountID, amount);
    }

    
    @Override
    public EconomyResponse withdraw( String pluginName,  UUID accountID,  String worldName,  String currency,  BigDecimal amount) {
        return withdraw(pluginName, accountID, amount);
    }


    @Override
    public EconomyResponse deposit( String pluginName,  UUID accountID,  BigDecimal amount) {
        Wallet wallet = WalletManager.getWallet(accountID);
        if (wallet == null) {
            LOGGER.warning("[ECONOMY_UNAVAILABLE] DEPOSIT FAILED: plugin=" + pluginName + ", account=" + accountID + ", amount=" + amount + " - database not connected");
            return new EconomyResponse(amount, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, "Economy system unavailable");
        }
        long amountLong = amount.longValue();
        wallet.add(amountLong);
        LOGGER.info("[TRANSACTION] VAULT_DEPOSIT: plugin=" + pluginName + ", account=" + accountID + ", amount=" + amountLong + ", newBalance=" + wallet.getBalance());
        return new EconomyResponse(amount, BigDecimal.valueOf(wallet.getBalance()), EconomyResponse.ResponseType.SUCCESS, "");
    }

    
    @Override
    public EconomyResponse deposit( String pluginName,  UUID accountID,  String worldName,  BigDecimal amount) {
        return deposit(pluginName, accountID, amount);
    }

    
    @Override
    public EconomyResponse deposit( String pluginName,  UUID accountID,  String worldName,  String currency,  BigDecimal amount) {
        return deposit(pluginName, accountID, amount);
    }

    @Override
    public boolean createSharedAccount( String pluginName,  UUID accountID,  String name,  UUID owner) {
        return false;
    }

    @Override
    public boolean isAccountOwner( String pluginName,  UUID accountID,  UUID uuid) {
        return accountID.equals(uuid);
    }

    @Override
    public boolean setOwner( String pluginName,  UUID accountID,  UUID uuid) {
        return false;
    }

    @Override
    public boolean isAccountMember( String pluginName,  UUID accountID,  UUID uuid) {
        return accountID.equals(uuid);
    }

    @Override
    public boolean addAccountMember( String pluginName,  UUID accountID,  UUID uuid) {
        return false;
    }

    @Override
    public boolean addAccountMember( String pluginName,  UUID accountID,  UUID uuid,  AccountPermission... initialPermissions) {
        return false;
    }

    @Override
    public boolean removeAccountMember( String pluginName,  UUID accountID,  UUID uuid) {
        return false;
    }

    @Override
    public boolean hasAccountPermission( String pluginName,  UUID accountID,  UUID uuid,  AccountPermission permission) {
        return accountID.equals(uuid);
    }

    @Override
    public boolean updateAccountPermission( String pluginName,  UUID accountID,  UUID uuid,  AccountPermission permission, boolean value) {
        return false;
    }
}