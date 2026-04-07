package com.goldtracker.Data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history")
public class History {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public double goldAmount;   // số lượng vàng
    public double price;        // giá vàng
    public double result;       // tiền VND
    public String time;         // thời gian
}