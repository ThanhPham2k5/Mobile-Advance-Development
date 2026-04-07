package com.goldtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "GoldTracker.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE History (id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT, unit TEXT, quantity REAL, total_vnd REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS History");
        onCreate(db);
    }

    public void addHistory(String date, String unit, double qty, double total) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("unit", unit);
        values.put("quantity", qty);
        values.put("total_vnd", total);
        db.insert("History", null, values);
        db.close();
    }

    public List<String> getAllHistory(double currentRateVnd) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM History ORDER BY id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(1);
                String unit = cursor.getString(2);
                double qty = cursor.getDouble(3);
                double oldTotal = cursor.getDouble(4);

                // Tính năng sáng tạo: Tính ROI (Lời/Lỗ) so với giá hiện tại
                double currentTotal = calculateCurrentValue(qty, unit, currentRateVnd);
                double roi = currentTotal - oldTotal;
                String status = roi >= 0 ? " Lời: +" + String.format("%.0f", roi) + "đ" : " Lỗ: " + String.format("%.0f", roi) + "đ";

                list.add(date + " | " + qty + " " + unit + "\nĐã lưu: " + String.format("%.0f", oldTotal) + "đ |" + status);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    private double calculateCurrentValue(double qty, String unit, double currentRate) {
        double multiplier = unit.equals("Ounce") ? 1.0 : (unit.equals("Lượng") ? 1.2 : 0.12);
        return qty * multiplier * currentRate;
    }
}