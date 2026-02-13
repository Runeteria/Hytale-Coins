package com.pandaismyname1.coins.interaction;

import com.pandaismyname1.coins.economy.Coin;
import com.pandaismyname1.coins.economy.Wallet;
import com.pandaismyname1.coins.economy.WalletManager;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.logging.Logger;

public class DepositCoinInteraction extends SimpleInstantInteraction {
    private static final Logger LOGGER = Logger.getLogger(DepositCoinInteraction.class.getName());
    public static final BuilderCodec CODEC = BuilderCodec.builder(DepositCoinInteraction.class, DepositCoinInteraction::new, SimpleInstantInteraction.CODEC).build();

    public DepositCoinInteraction() {
        super("DepositCoin");
    }

    @Override
    protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        ItemStack heldItem = context.getHeldItem();
        if (heldItem == null || heldItem.isEmpty()) return;

        Coin coin = Coin.fromItemId(heldItem.getItemId());
        if (coin != null) {
            Ref<EntityStore> ref = context.getEntity();
            UUIDComponent uuidComponent = (UUIDComponent) context.getCommandBuffer().getComponent(ref, UUIDComponent.getComponentType());
            if (uuidComponent == null) return;
            UUID uuid = uuidComponent.getUuid();

            long quantity = heldItem.getQuantity();
            long value = coin.getValue() * quantity;

            Wallet wallet = WalletManager.getWallet(uuid);
            if (wallet == null) {
                // Economy unavailable - notify player but don't consume items
                LOGGER.warning("[ECONOMY_UNAVAILABLE] DEPOSIT FAILED: player=" + uuid + ", coin=" + coin.name() + ", quantity=" + quantity + ", value=" + value + " - database not connected");
                Player player = (Player) context.getCommandBuffer().getComponent(ref, Player.getComponentType());
                if (player != null) {
                    player.sendMessage(Message.raw("§cEconomy system is currently unavailable."));
                }
                return;
            }
            // TEMP fix Feb 13, 26
            //wallet.add(value);
            //LOGGER.info("[TRANSACTION] DEPOSIT: player=" + uuid + ", coin=" + coin.name() + ", quantity=" + quantity + ", value=" + value + ", newBalance=" + wallet.getBalance());

            // Inform the user
            Player player = (Player) context.getCommandBuffer().getComponent(ref, Player.getComponentType());
            /*
            if (player != null) {
                player.sendMessage(Message.raw("Deposited " + value + " coins. New balance: " + wallet.getBalance()));
            }

            // Remove items from inventory
            ItemContainer container = context.getHeldItemContainer();
            if (container != null) {
                container.removeItemStackFromSlot(context.getHeldItemSlot(), (int) quantity);
            }
            */
            player.sendMessage(Message.raw("Deposits temporarily disabled, sorry!"));
        }
    }
}
