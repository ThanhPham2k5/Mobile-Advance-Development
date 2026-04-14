package com.goldtracker.model;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// ================== METAL PRICE RESPONSE ==================
public class GoldResponse {

    // ===== MetalPriceAPI =====
    public static class MetalPriceResponse {
        public boolean success;
        public String base;
        public long timestamp;
        public Map<String, Double> rates;
    }

    // ===== Exchange Rate =====
    public class ExchangeRateResponse {

        public String result;
        public String base_code;
        public Map<String, Double> rates;
    }

    // ================== UI MODEL ==================

    public enum GoldType {

        GOLD_24K("Vàng 24K (SJC)", 1.0),
        GOLD_22K("Vàng 22K (PNJ)", 22.0 / 24.0),
        GOLD_18K("Vàng 18K (Nhẫn)", 18.0 / 24.0),
        GOLD_14K("Vàng 14K", 14.0 / 24.0),
        GOLD_10K("Vàng 10K", 10.0 / 24.0);

        public final String displayName;
        public final double purity;

        GoldType(String displayName, double purity) {
            this.displayName = displayName;
            this.purity = purity;
        }
    }

    public static class GoldUnit {

        public String name;
        public double multiplierGram;

        public GoldUnit(String name, double multiplierGram) {
            this.name = name;
            this.multiplierGram = multiplierGram;
        }

        public static final GoldUnit GRAM = new GoldUnit("Gram", 1.0);
        public static final GoldUnit CHI = new GoldUnit("Chỉ", 3.75);
        public static final GoldUnit LUONG = new GoldUnit("Lượng", 37.5);
        public static final GoldUnit OUNCE = new GoldUnit("Ounce", 31.1035);

        public static final List<GoldUnit> ALL =
                Arrays.asList(GRAM, CHI, LUONG, OUNCE);
    }

    public static class GoldPrice {

        public String type;

        public double pricePerGramVnd;

        public double pricePerChiVnd;
        public double pricePerLuongVnd;

        public double pricePerOunceVnd;

        public GoldPrice(String type,
                         double pricePerGramVnd,
                         double pricePerChiVnd,
                         double pricePerLuongVnd,
                         double pricePerOunceVnd) {

            this.type = type;
            this.pricePerGramVnd = pricePerGramVnd;
            this.pricePerChiVnd = pricePerChiVnd;
            this.pricePerLuongVnd = pricePerLuongVnd;
            this.pricePerOunceVnd = pricePerOunceVnd;
        }
    }
}