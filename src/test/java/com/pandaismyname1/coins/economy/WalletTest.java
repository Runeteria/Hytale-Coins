package com.pandaismyname1.coins.economy;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class WalletTest {
    @Test
    public void testWithdrawalLogic() {
        UUID uuid = UUID.randomUUID();
        Wallet wallet = new Wallet(uuid, 500); // 500 copper
        
        // Initial balance check
        assertEquals(500, wallet.getBalance());
        
        // Test withdrawing 50 copper coins (50 * 10 = 500)
        // Note: Wallet.java doesn't handle the coin type in its remove method, 
        // it only removes copper. The logic is in WalletPage.
        
        assertTrue(wallet.remove(50 * Coin.COPPER.getValue()));
        assertEquals(450, wallet.getBalance());
    }

    @Test
    public void testInsufficientFunds() {
        UUID uuid = UUID.randomUUID();
        Wallet wallet = new Wallet(uuid, 50);
        
        assertFalse(wallet.remove(100));
        assertEquals(50, wallet.getBalance());
    }
}
