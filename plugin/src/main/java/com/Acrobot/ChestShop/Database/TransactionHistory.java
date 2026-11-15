package com.Acrobot.ChestShop.Database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 取引履歴の保存と検索。メインスレッドでは呼ばないこと(ChestShop.runInAsyncThread を使う)
 */
public class TransactionHistory {
    public static final int PAGE_SIZE = 10;

    private static final Object LOCK = new Object();
    private static Dao<TransactionRecord, Integer> dao;

    /**
     * 表が無ければ作る。起動時に一度だけ呼ぶ
     */
    public static void load() throws SQLException {
        dao = DaoCreator.getDaoAndCreateTable(TransactionRecord.class);
    }

    /**
     * テスト用。DAO を直接渡す
     */
    public static void load(Dao<TransactionRecord, Integer> testDao) {
        dao = testDao;
    }

    public static boolean isLoaded() {
        return dao != null;
    }

    public static void add(TransactionRecord record) throws SQLException {
        synchronized (LOCK) {
            dao.create(record);
        }
    }

    /**
     * そのプレイヤーが客または店主だった取引の件数
     */
    public static long count(UUID uuid) throws SQLException {
        synchronized (LOCK) {
            return participantQuery(uuid).countOf();
        }
    }

    /**
     * そのプレイヤーが客または店主だった取引を、新しい順に 1 ページ分返す
     *
     * @param page ページ番号(1 から)
     */
    public static List<TransactionRecord> find(UUID uuid, int page) throws SQLException {
        synchronized (LOCK) {
            QueryBuilder<TransactionRecord, Integer> query = participantQuery(uuid);
            query.orderBy("time", false).orderBy("id", false);
            query.offset((long) (page - 1) * PAGE_SIZE).limit((long) PAGE_SIZE);
            return query.query();
        }
    }

    /**
     * 指定した日数より古い記録を消す
     *
     * @return 消した件数
     */
    public static int deleteOlderThan(int days) throws SQLException {
        synchronized (LOCK) {
            DeleteBuilder<TransactionRecord, Integer> delete = dao.deleteBuilder();
            delete.where().lt("time", new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days)));
            return delete.delete();
        }
    }

    private static QueryBuilder<TransactionRecord, Integer> participantQuery(UUID uuid) throws SQLException {
        QueryBuilder<TransactionRecord, Integer> query = dao.queryBuilder();
        query.where().eq("clientUuid", new SelectArg(uuid)).or().eq("ownerUuid", new SelectArg(uuid));
        return query;
    }
}
