package com.pandaismyname1.coins.command;

import com.pandaismyname1.coins.economy.Wallet;
import com.pandaismyname1.coins.economy.WalletManager;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EnumArgumentType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("deprecation")
public class EconomyCommand extends AbstractCommand {
    public EconomyCommand() {
        super("economy", "Manage player economies");
        this.addAliases("eco");
        this.requirePermission("coins.admin.economy");
        
        this.addSubCommand(new EconomySetCommand());
        this.addSubCommand(new EconomyAddCommand());
    }

    @Override
    protected CompletableFuture<Void> execute(@NonNullDecl CommandContext commandContext) {
        commandContext.sendMessage(this.getUsageString(commandContext.sender()));
        return CompletableFuture.completedFuture(null);
    }

    private static class EconomySetCommand extends AbstractCommand {
        private final RequiredArg playerArg;
        private final RequiredArg amountArg;

        public EconomySetCommand() {
            super("set", "Set a player's balance");
            this.playerArg = this.withRequiredArg("player", "The player", ArgTypes.PLAYER_REF);
            this.amountArg = this.withRequiredArg("amount", "The amount", ArgTypes.INTEGER);
        }

        @Override
        protected CompletableFuture<Void> execute(@NonNullDecl CommandContext commandContext) {
            PlayerRef targetRef = (PlayerRef) commandContext.get(this.playerArg);
            Integer amount = (Integer) commandContext.get(this.amountArg);

            if (targetRef == null || amount == null) return CompletableFuture.completedFuture(null);

            Wallet wallet = WalletManager.getWallet(targetRef.getUuid());
            if (wallet == null) {
                commandContext.sendMessage(Message.raw("Economy system is currently unavailable."));
                return CompletableFuture.completedFuture(null);
            }

            long current = wallet.getBalance();
            wallet.remove(current);
            wallet.add(amount);

            commandContext.sendMessage(Message.raw("[Economy] Set " + targetRef.getUsername() + "'s balance to " + amount + " Copper."));
            return CompletableFuture.completedFuture(null);
        }
    }

    private static class EconomyAddCommand extends AbstractCommand {
        private final RequiredArg playerArg;
        private final RequiredArg amountArg;

        public EconomyAddCommand() {
            super("add", "Add coins to a player's balance");
            this.playerArg = this.withRequiredArg("player", "The player", ArgTypes.PLAYER_REF);
            this.amountArg = this.withRequiredArg("amount", "The amount", ArgTypes.INTEGER);
        }

        @Override
        protected CompletableFuture<Void> execute(@NonNullDecl CommandContext commandContext) {
            PlayerRef targetRef = (PlayerRef) commandContext.get(this.playerArg);
            Integer amount = (Integer) commandContext.get(this.amountArg);

            if (targetRef == null || amount == null) return CompletableFuture.completedFuture(null);

            Wallet wallet = WalletManager.getWallet(targetRef.getUuid());
            if (wallet == null) {
                commandContext.sendMessage(Message.raw("Economy system is currently unavailable."));
                return CompletableFuture.completedFuture(null);
            }

            wallet.add(amount);

            commandContext.sendMessage(Message.raw("[Economy] Added " + amount + " Copper to " + targetRef.getUsername() + "'s balance."));
            return CompletableFuture.completedFuture(null);
        }
    }
}
