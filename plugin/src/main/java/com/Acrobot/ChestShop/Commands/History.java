package com.Acrobot.ChestShop.Commands;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Configuration.Messages;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Database.Account;
import com.Acrobot.ChestShop.Database.TransactionHistory;
import com.Acrobot.ChestShop.Database.TransactionRecord;
import com.Acrobot.ChestShop.Events.Economy.CurrencyFormatEvent;
import com.Acrobot.ChestShop.Permission;
import com.Acrobot.ChestShop.UUIDs.NameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * /cshistory [ページ] … 自分の取引履歴を表示する
 * /cshistory <プレイヤー名> [ページ] … 他の人の取引履歴を表示する(ChestShop.admin.history が必要)
 */
public class History implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!Properties.LOG_TRANSACTIONS_TO_DB) {
            Messages.HISTORY_DISABLED.sendWithPrefix(sender);
            return true;
        }
        if (!TransactionHistory.isLoaded()) {
            Messages.ERROR_OCCURRED.sendWithPrefix(sender, "error", "Transaction history is not available.");
            return true;
        }
        if (args.length > 2) {
            return false;
        }

        int page = 1;
        String targetName = null;
        if (args.length == 2) {
            targetName = args[0];
            page = parsePage(args[1]);
        } else if (args.length == 1) {
            if (isNumber(args[0])) {
                page = parsePage(args[0]);
            } else {
                targetName = args[0];
            }
        }
        if (page < 1) {
            return false;
        }

        final Account target;
        if (targetName != null) {
            // 他人の履歴は、管理者だけが見られる
            if (!Permission.has(sender, Permission.HISTORY_ADMIN)) {
                Messages.ACCESS_DENIED.sendWithPrefix(sender);
                return true;
            }
            target = NameManager.getAccount(targetName);
            if (target == null) {
                Messages.PLAYER_NOT_FOUND.sendWithPrefix(sender);
                return true;
            }
        } else if (sender instanceof Player) {
            target = NameManager.getOrCreateAccount((Player) sender);
        } else {
            sender.sendMessage("Please specify a player name.");
            return true;
        }

        final int requestedPage = page;
        // DB の読み込みは非同期で行い、表示はメインスレッドに戻して行う
        ChestShop.runInAsyncThread(() -> {
            try {
                long total = TransactionHistory.count(target.getUuid());
                int pages = (int) Math.max(1, (total + TransactionHistory.PAGE_SIZE - 1) / TransactionHistory.PAGE_SIZE);
                int shownPage = Math.min(requestedPage, pages);
                List<TransactionRecord> records = total == 0
                        ? Collections.<TransactionRecord>emptyList()
                        : TransactionHistory.find(target.getUuid(), shownPage);
                Bukkit.getScheduler().runTask(ChestShop.getPlugin(), () -> show(sender, target, records, shownPage, pages, total));
            } catch (Exception e) {
                ChestShop.getBukkitLogger().log(Level.WARNING, "Error while reading the transaction history:", e);
                Bukkit.getScheduler().runTask(ChestShop.getPlugin(), () ->
                        Messages.ERROR_OCCURRED.sendWithPrefix(sender, "error", "Unable to read the transaction history."));
            }
        });
        return true;
    }

    private static void show(CommandSender sender, Account target, List<TransactionRecord> records, int page, int pages, long total) {
        if (records.isEmpty()) {
            Messages.HISTORY_EMPTY.sendWithPrefix(sender, "player", target.getName());
            return;
        }

        Messages.HISTORY_HEADER.sendWithPrefix(sender,
                "player", target.getName(),
                "page", String.valueOf(page),
                "pages", String.valueOf(pages),
                "total", String.valueOf(total));

        SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:mm");
        for (TransactionRecord record : records) {
            CurrencyFormatEvent price = ChestShop.callEvent(new CurrencyFormatEvent(BigDecimal.valueOf(record.getPrice())));
            Messages.Message message = "BUY".equals(record.getType()) ? Messages.HISTORY_BOUGHT : Messages.HISTORY_SOLD;
            message.send(sender,
                    "time", format.format(record.getTime()),
                    "client", record.getClientName(),
                    "owner", record.getOwnerName(),
                    "item", record.getItems(),
                    "price", price.getFormattedAmount(),
                    "world", record.getWorld(),
                    "x", String.valueOf(record.getX()),
                    "y", String.valueOf(record.getY()),
                    "z", String.valueOf(record.getZ()));
        }
    }

    private static boolean isNumber(String text) {
        return !text.isEmpty() && text.chars().allMatch(Character::isDigit);
    }

    private static int parsePage(String text) {
        if (!isNumber(text) || text.length() > 6) {
            return -1;
        }
        return Integer.parseInt(text);
    }
}
