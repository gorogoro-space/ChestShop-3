package com.Acrobot.ChestShop.Tests;

import com.Acrobot.ChestShop.Database.TransactionHistory;
import com.Acrobot.ChestShop.Database.TransactionRecord;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.db.SqliteDatabaseType;
import com.j256.ormlite.table.TableUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 取引履歴の保存・検索・削除を、実際の SQLite で確かめる
 */
class TransactionHistoryTest {
    private static final UUID ALICE = UUID.randomUUID();
    private static final UUID BOB = UUID.randomUUID();
    private static final UUID CAROL = UUID.randomUUID();

    private JdbcConnectionSource source;

    @BeforeEach
    void setUp(@TempDir Path dir) throws Exception {
        source = new JdbcConnectionSource("jdbc:sqlite:" + dir.resolve("transactions.db"), new SqliteDatabaseType());
        Dao<TransactionRecord, Integer> dao = DaoManager.createDao(source, TransactionRecord.class);
        TableUtils.createTable(source, TransactionRecord.class);
        TransactionHistory.load(dao);
    }

    @AfterEach
    void tearDown() throws Exception {
        source.close();
    }

    private static TransactionRecord record(long time, UUID client, UUID owner, String items) {
        return new TransactionRecord(new Date(time), client, "client", owner, "owner", "BUY", items, 10.5, "world", 1, 2, 3);
    }

    @Test
    void onlyOwnTransactionsAreFound() throws Exception {
        // Alice が Bob の店で買った / Bob が Carol の店で買った
        TransactionHistory.add(record(1000, ALICE, BOB, "1 Apple"));
        TransactionHistory.add(record(2000, BOB, CAROL, "2 Stone"));

        assertEquals(1, TransactionHistory.count(ALICE));   // 客として
        assertEquals(2, TransactionHistory.count(BOB));     // 店主としても客としても
        assertEquals(1, TransactionHistory.count(CAROL));   // 店主として
        assertEquals(0, TransactionHistory.count(UUID.randomUUID()));

        List<TransactionRecord> alice = TransactionHistory.find(ALICE, 1);
        assertEquals(1, alice.size());
        assertEquals("1 Apple", alice.get(0).getItems());
    }

    @Test
    void newestFirstAndPaged() throws Exception {
        int total = TransactionHistory.PAGE_SIZE + 3;
        for (int i = 0; i < total; i++) {
            TransactionHistory.add(record(1000L + i, ALICE, BOB, "item" + i));
        }

        List<TransactionRecord> first = TransactionHistory.find(ALICE, 1);
        List<TransactionRecord> second = TransactionHistory.find(ALICE, 2);
        assertEquals(TransactionHistory.PAGE_SIZE, first.size());
        assertEquals(3, second.size());
        assertEquals("item" + (total - 1), first.get(0).getItems());   // 新しい順
        assertEquals("item2", second.get(0).getItems());
        assertEquals("item0", second.get(2).getItems());
        assertTrue(TransactionHistory.find(ALICE, 3).isEmpty());
    }

    @Test
    void oldEntriesAreDeleted() throws Exception {
        long now = System.currentTimeMillis();
        TransactionHistory.add(record(now - TimeUnit.DAYS.toMillis(100), ALICE, BOB, "old"));
        TransactionHistory.add(record(now - TimeUnit.DAYS.toMillis(10), ALICE, BOB, "recent"));

        assertEquals(1, TransactionHistory.deleteOlderThan(90));

        List<TransactionRecord> left = TransactionHistory.find(ALICE, 1);
        assertEquals(1, left.size());
        assertEquals("recent", left.get(0).getItems());
    }
}
