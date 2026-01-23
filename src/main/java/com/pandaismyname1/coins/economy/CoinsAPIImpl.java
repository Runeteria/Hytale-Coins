package com.pandaismyname1.coins.economy;

import com.pandaismyname1.coins.api.CoinsAPI;

import java.util.UUID;
import java.util.logging.Logger;

public class CoinsAPIImpl implements CoinsAPI {
    private static final Logger LOGGER = Logger.getLogger(CoinsAPIImpl.class.getName());

    @Override
    public long getBalance(UUID playerUuid) {
        Wallet wallet = WalletManager.getWallet(playerUuid);
        if (wallet == null) {
            LOGGER.warning("[ECONOMY_UNAVAILABLE] getBalance failed for " + playerUuid + " - database not connected");
            return 0;
        }
        return wallet.getBalance();
    }

    @Override
    public void addCoins(UUID playerUuid, long amount) {
        Wallet wallet = WalletManager.getWallet(playerUuid);
        if (wallet == null) {
            LOGGER.warning("[ECONOMY_UNAVAILABLE] addCoins FAILED: player=" + playerUuid + ", amount=" + amount + " - database not connected");
            return;
        }
        wallet.add(amount);
        LOGGER.info("[TRANSACTION] ADD: player=" + playerUuid + ", amount=" + amount + ", newBalance=" + wallet.getBalance());
    }

    @Override
    public boolean removeCoins(UUID playerUuid, long amount) {
        Wallet wallet = WalletManager.getWallet(playerUuid);
        if (wallet == null) {
            LOGGER.warning("[ECONOMY_UNAVAILABLE] removeCoins FAILED: player=" + playerUuid + ", amount=" + amount + " - database not connected");
            return false;
        }
        boolean success = wallet.remove(amount);
        if (success) {
            LOGGER.info("[TRANSACTION] REMOVE: player=" + playerUuid + ", amount=" + amount + ", newBalance=" + wallet.getBalance());
        }
        return success;
    }

    @Override
    public void setBalance(UUID playerUuid, long amount) {
        Wallet wallet = WalletManager.getWallet(playerUuid);
        if (wallet == null) {
            LOGGER.warning("[ECONOMY_UNAVAILABLE] setBalance FAILED: player=" + playerUuid + ", amount=" + amount + " - database not connected");
            return;
        }
        long oldBalance = wallet.getBalance();
        wallet.remove(oldBalance);
        wallet.add(amount);
        LOGGER.info("[TRANSACTION] SET: player=" + playerUuid + ", oldBalance=" + oldBalance + ", newBalance=" + amount);
    }

    @Override
    public boolean transferCoins(UUID senderUuid, UUID recipientUuid, long amount) {
        if (amount <= 0) return false;
        if (senderUuid.equals(recipientUuid)) return false;

        Wallet senderWallet = WalletManager.getWallet(senderUuid);
        if (senderWallet == null) {
            LOGGER.warning("[ECONOMY_UNAVAILABLE] transferCoins FAILED: sender=" + senderUuid + ", recipient=" + recipientUuid + ", amount=" + amount + " - database not connected");
            return false;
        }

        if (senderWallet.remove(amount)) {
            Wallet recipientWallet = WalletManager.getWallet(recipientUuid);
            if (recipientWallet != null) {
                recipientWallet.add(amount);
                LOGGER.info("[TRANSACTION] TRANSFER: sender=" + senderUuid + ", recipient=" + recipientUuid + ", amount=" + amount + ", senderNewBalance=" + senderWallet.getBalance() + ", recipientNewBalance=" + recipientWallet.getBalance());
                return true;
            }
            // Refund if recipient wallet unavailable
            senderWallet.add(amount);
            LOGGER.warning("[ECONOMY_UNAVAILABLE] transferCoins REFUNDED: sender=" + senderUuid + ", recipient=" + recipientUuid + ", amount=" + amount + " - recipient wallet unavailable");
        }
        return false;
    }
}
