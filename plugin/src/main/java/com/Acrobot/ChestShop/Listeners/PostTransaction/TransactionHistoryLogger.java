package com.Acrobot.ChestShop.Listeners.PostTransaction;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Database.TransactionHistory;
import com.Acrobot.ChestShop.Database.TransactionRecord;
import com.Acrobot.ChestShop.Events.TransactionEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;

import static com.Acrobot.Breeze.Utils.InventoryUtil.getItemCounts;
import static com.Acrobot.ChestShop.Utils.ItemUtil.getName;

/**
 * 取引が成立したあとに、取引履歴(transactions.db)へ 1 件記録する。
 * アイテム名の組み立てだけメインスレッドで行い、DB への書き込みは非同期で行う。
 */
public class TransactionHistoryLogger implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void onTransaction(final TransactionEvent event) {
        if (!TransactionHistory.isLoaded()) {
            return;
        }

        StringJoiner items = new StringJoiner(", ");
        for (Map.Entry<ItemStack, Integer> entry : getItemCounts(event.getStock()).entrySet()) {
            items.add(entry.getValue() + " " + getName(entry.getKey()));
        }

        Location location = event.getSign().getLocation();
        final TransactionRecord record = new TransactionRecord(
                new Date(),
                event.getClient().getUniqueId(),
                event.getClient().getName(),
                event.getOwnerAccount().getUuid(),
                event.getOwnerAccount().getName(),
                event.getTransactionType().name(),
                items.toString(),
                event.getExactPrice().doubleValue(),
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );

        ChestShop.runInAsyncThread(() -> {
            try {
                TransactionHistory.add(record);
            } catch (Exception e) {
                ChestShop.getBukkitLogger().log(Level.WARNING, "Error while saving the transaction history:", e);
            }
        });
    }
}
