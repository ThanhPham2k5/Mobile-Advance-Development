package com.goldtracker.Data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history")
public class History {

    @PrimaryKey(autoGenerate = true)
    public int id;

    // Thời gian (timestamp)
    public long time;

    // Loại vàng (24K, 18K...)
    public String goldType;

    // Giá vàng (VND hoặc USD)
    public double goldPrice;

    // Đơn vị (gram, chỉ, lượng...)
    public String unit;

    // Số lượng
    public double quantity;

    // Kết quả (tổng tiền)
    public double result;
}