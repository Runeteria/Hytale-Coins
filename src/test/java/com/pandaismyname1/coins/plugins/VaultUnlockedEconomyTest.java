package com.pandaismyname1.coins.plugins;

import com.pandaismyname1.coins.economy.Wallet;
import com.pandaismyname1.coins.economy.WalletManager;
import net.milkbowl.vault2.economy.EconomyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

public class VaultUnlockedEconomyTest {
    private VaultUnlockedEconomy economy;
    private UUID playerUuid;

    @BeforeEach
    public void setup() {
        economy = new VaultUnlockedEconomy();
        playerUuid = UUID.randomUUID();
    }

    private Wallet getWalletOrSkip() {
        Wallet wallet = WalletManager.getWallet(playerUuid);
        assumeTrue(wallet != null, "Skipping test - database not available");
        return wallet;
    }

    @Test
    public void testIsEnabledWithoutDatabase() {
        // When database is not connected, isEnabled should return false
        if (!WalletManager.isAvailable()) {
            assertFalse(economy.isEnabled());
        }
    }

    @Test
    public void testGetBalance() {
        Wallet wallet = getWalletOrSkip();
        wallet.remove(wallet.getBalance());

        BigDecimal balance = economy.getBalance("test", playerUuid);
        assertEquals(0, balance.compareTo(BigDecimal.ZERO));

        wallet.add(100);
        balance = economy.getBalance("test", playerUuid);
        assertEquals(0, balance.compareTo(BigDecimal.valueOf(100)));
    }

    @Test
    public void testDeposit() {
        Wallet wallet = getWalletOrSkip();
        wallet.remove(wallet.getBalance());

        EconomyResponse response = economy.deposit("test", playerUuid, BigDecimal.valueOf(50));
        assertTrue(response.transactionSuccess());
        assertEquals(0, response.amount.compareTo(BigDecimal.valueOf(50)));
        assertEquals(0, response.balance.compareTo(BigDecimal.valueOf(50)));
        assertEquals(50, wallet.getBalance());
    }

    @Test
    public void testWithdraw() {
        Wallet wallet = getWalletOrSkip();
        wallet.remove(wallet.getBalance());
        wallet.add(100);

        EconomyResponse response = economy.withdraw("test", playerUuid, BigDecimal.valueOf(40));
        assertTrue(response.transactionSuccess());
        assertEquals(0, response.amount.compareTo(BigDecimal.valueOf(40)));
        assertEquals(0, response.balance.compareTo(BigDecimal.valueOf(60)));
        assertEquals(60, wallet.getBalance());
    }

    @Test
    public void testWithdrawInsufficientFunds() {
        Wallet wallet = getWalletOrSkip();
        wallet.remove(wallet.getBalance());
        wallet.add(30);

        EconomyResponse response = economy.withdraw("test", playerUuid, BigDecimal.valueOf(40));
        assertFalse(response.transactionSuccess());
        assertEquals("Insufficient funds", response.errorMessage);
        assertEquals(30, wallet.getBalance());
    }

    @Test
    public void testHas() {
        Wallet wallet = getWalletOrSkip();
        wallet.remove(wallet.getBalance());
        wallet.add(100);

        assertTrue(economy.has("test", playerUuid, BigDecimal.valueOf(50)));
        assertTrue(economy.has("test", playerUuid, BigDecimal.valueOf(100)));
        assertFalse(economy.has("test", playerUuid, BigDecimal.valueOf(101)));
    }

    @Test
    public void testFormat() {
        assertEquals("100 Copper", economy.format(BigDecimal.valueOf(100)));
        assertEquals("100 Copper", economy.format(BigDecimal.valueOf(100), "Copper"));
        assertEquals("100 Gold", economy.format(BigDecimal.valueOf(100), "Gold"));
    }

    @Test
    public void testUnavailableResponses() {
        // Test behavior when database is unavailable
        if (!WalletManager.isAvailable()) {
            assertEquals(BigDecimal.ZERO, economy.getBalance("test", playerUuid));
            assertFalse(economy.has("test", playerUuid, BigDecimal.valueOf(1)));

            EconomyResponse withdrawResponse = economy.withdraw("test", playerUuid, BigDecimal.valueOf(10));
            assertFalse(withdrawResponse.transactionSuccess());
            assertEquals("Economy system unavailable", withdrawResponse.errorMessage);

            EconomyResponse depositResponse = economy.deposit("test", playerUuid, BigDecimal.valueOf(10));
            assertFalse(depositResponse.transactionSuccess());
            assertEquals("Economy system unavailable", depositResponse.errorMessage);
        }
    }
}
