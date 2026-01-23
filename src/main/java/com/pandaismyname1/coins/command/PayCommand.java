package com.pandaismyname1.coins.command;

import com.pandaismyname1.coins.economy.Wallet;
import com.pandaismyname1.coins.economy.WalletManager;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("deprecation")
public class PayCommand extends AbstractCommand {
    private final RequiredArg playerArg;
    private final RequiredArg amountArg;

    public PayCommand() {
        super("pay", "Pay coins to another player");
        this.setPermissionGroup(GameMode.Adventure);
        this.playerArg = this.withRequiredArg("player", "The player to pay", ArgTypes.PLAYER_REF);
        this.amountArg = this.withRequiredArg("amount", "The amount of copper to pay", ArgTypes.INTEGER);
    }

    @Override
    protected CompletableFuture<Void> execute(@NonNullDecl CommandContext commandContext) {
        if (!commandContext.isPlayer()) {
            commandContext.sendMessage(Message.raw("This command can only be used by players."));
            return CompletableFuture.completedFuture(null);
        }

        Player sender = (Player) commandContext.sender();
        PlayerRef recipientRef = (PlayerRef) commandContext.get(this.playerArg);
        Integer amount = (Integer) commandContext.get(this.amountArg);

        if (recipientRef == null || amount == null) {
            return CompletableFuture.completedFuture(null);
        }

        if (amount <= 0) {
            commandContext.sendMessage(Message.raw("§cAmount must be greater than zero."));
            return CompletableFuture.completedFuture(null);
        }

        if (sender.getUuid().equals(recipientRef.getUuid())) {
            commandContext.sendMessage(Message.raw("§cYou cannot pay yourself."));
            return CompletableFuture.completedFuture(null);
        }

        Wallet senderWallet = WalletManager.getWallet(sender.getUuid());
        if (senderWallet == null) {
            commandContext.sendMessage(Message.raw("§cEconomy system is currently unavailable."));
            return CompletableFuture.completedFuture(null);
        }

        if (senderWallet.remove(amount)) {
            Wallet recipientWallet = WalletManager.getWallet(recipientRef.getUuid());
            if (recipientWallet == null) {
                // Refund sender if recipient wallet unavailable
                senderWallet.add(amount);
                commandContext.sendMessage(Message.raw("§cEconomy system is currently unavailable."));
                return CompletableFuture.completedFuture(null);
            }
            recipientWallet.add(amount);

            sender.sendMessage(Message.raw("§6[Wallet] §fYou paid §e" + amount + " Copper §fto §b" + recipientRef.getUsername() + "§f."));

            // Try to notify recipient if they are online
            Player recipientPlayer = recipientRef.getHolder().getComponent(com.hypixel.hytale.server.core.modules.entity.EntityModule.get().getPlayerComponentType());
            if (recipientPlayer != null) {
                recipientPlayer.sendMessage(Message.raw("§6[Wallet] §fYou received §e" + amount + " Copper §ffrom §b" + sender.getDisplayName() + "§f."));
            }
        } else {
            commandContext.sendMessage(Message.raw("§cYou do not have enough coins."));
        }

        return CompletableFuture.completedFuture(null);
    }
}
