package com.Acrobot.ChestShop.Database;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.UUID;

/**
 * 取引 1 件の記録(取引履歴用)
 * 既存の accounts / items とは別のファイル(transactions.db)に保存する
 */
@DatabaseTable(tableName = "transactions")
@DatabaseFileName("transactions.db")
public class TransactionRecord {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(index = true, canBeNull = false, dataType = DataType.DATE_LONG)
    private Date time;

    @DatabaseField(index = true, canBeNull = false)
    private UUID clientUuid;

    @DatabaseField(canBeNull = false)
    private String clientName;

    @DatabaseField(index = true, canBeNull = false)
    private UUID ownerUuid;

    @DatabaseField(canBeNull = false)
    private String ownerName;

    /** "BUY"(客が店から買った)または "SELL"(客が店へ売った) */
    @DatabaseField(canBeNull = false)
    private String type;

    @DatabaseField(canBeNull = false)
    private String items;

    @DatabaseField(canBeNull = false)
    private double price;

    @DatabaseField(canBeNull = false)
    private String world;

    @DatabaseField
    private int x;

    @DatabaseField
    private int y;

    @DatabaseField
    private int z;

    public TransactionRecord() {
        //empty constructor, needed for ORMLite
    }

    public TransactionRecord(Date time, UUID clientUuid, String clientName, UUID ownerUuid, String ownerName,
                             String type, String items, double price, String world, int x, int y, int z) {
        this.time = time;
        this.clientUuid = clientUuid;
        this.clientName = clientName;
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.type = type;
        this.items = items;
        this.price = price;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Date getTime() {
        return time;
    }

    public UUID getClientUuid() {
        return clientUuid;
    }

    public String getClientName() {
        return clientName;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getType() {
        return type;
    }

    public String getItems() {
        return items;
    }

    public double getPrice() {
        return price;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}
