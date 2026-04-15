package com.goldtracker.ui.portfolio;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;

import com.goldtracker.R;
import com.goldtracker.model.GoldResponse;
import com.goldtracker.repository.GoldRepository;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.*;

public class ProfitFragment extends Fragment {

    private EditText edtQuantity, edtBuyPrice;
    private Spinner spinnerType, spinnerUnit;
    private Button btnCalc;
    private TextView tvResult;

    private GoldRepository repo;

    private double currentPricePerGram24k = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profit, container, false);

        edtQuantity = view.findViewById(R.id.edtQuantity);
        edtBuyPrice = view.findViewById(R.id.edtBuyPrice);
        spinnerType = view.findViewById(R.id.spinnerType);
        spinnerUnit = view.findViewById(R.id.spinnerUnit);

        btnCalc = view.findViewById(R.id.btnCalc);
        tvResult = view.findViewById(R.id.tvResult);

        repo = new GoldRepository(requireContext());

        setupSpinners();
        loadPrice();
        setupButton();

        return view;
    }

    // =========================
    // LOAD CURRENT GOLD PRICE
    // =========================
    private void loadPrice() {
        // Thêm 2 tham số worldPrice và rate vào onSuccess
        repo.getCurrentGoldPrices(new GoldRepository.GoldCallback() {
            @Override
            public void onSuccess(java.util.List<GoldResponse.GoldPrice> data, double worldPrice, double rate) {
                for (GoldResponse.GoldPrice item : data) {
                    if (item.type.contains("24K")) {
                        currentPricePerGram24k = item.pricePerGramVnd;
                        break;
                    }
                }

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (tvResult.getText().toString().contains("Đang tải")) {
                            tvResult.setText("Giá vàng đã được cập nhật.");
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                tvResult.setText("Lỗi tải giá");
            }
        });
    }

    // =========================
    // SPINNER DATA
    // =========================
    private void setupSpinners() {

        // Gold types
        String[] goldTypes = {"24K", "22K", "18K", "14K", "10K"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.item_spinner_selected,
                goldTypes
        );
        spinnerType.setAdapter(typeAdapter);

        // Units
        String[] units = {"Gram", "Chỉ", "Lượng", "Ounce"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.item_spinner_selected,
                units
        );
        spinnerUnit.setAdapter(unitAdapter);
    }

    // CALCULATE PROFIT / LOSS
    private void setupButton() {

        btnCalc.setOnClickListener(v -> {

            String qtyStr = edtQuantity.getText().toString().trim();
            String buyStr = edtBuyPrice.getText().toString().trim();

            if (qtyStr.isEmpty() || buyStr.isEmpty()) {
                tvResult.setText("Nhập đầy đủ!");
                return;
            }

            try {

                double quantity = Double.parseDouble(qtyStr);
                double buyPrice = Double.parseDouble(buyStr);

                String goldType = spinnerType.getSelectedItem().toString();
                String unit = spinnerUnit.getSelectedItem().toString();

                double purity = getPurity(goldType);
                double gram = convertToGram(quantity, unit);

                double currentPricePerGram = currentPricePerGram24k * purity;

                // =========================
                // DÙNG BIGDECIMAL CHO CHÍNH XÁC
                // =========================
                java.math.BigDecimal currentTotal =
                        java.math.BigDecimal.valueOf(currentPricePerGram)
                                .multiply(java.math.BigDecimal.valueOf(gram));

                java.math.BigDecimal buyTotal =
                        java.math.BigDecimal.valueOf(buyPrice)
                                .multiply(java.math.BigDecimal.valueOf(gram));

                java.math.BigDecimal profit = currentTotal.subtract(buyTotal);

                // làm tròn 2 số thập phân VND (an toàn)
                profit = profit.setScale(0, java.math.RoundingMode.HALF_UP);

                if (profit.compareTo(java.math.BigDecimal.ZERO) > 0) {

                    tvResult.setText("Lãi: " + format(profit.longValue()) + " VND");
                    tvResult.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

                } else if (profit.compareTo(java.math.BigDecimal.ZERO) < 0) {

                    tvResult.setText("Lỗ: " + format(profit.abs().longValue()) + " VND");
                    tvResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

                } else {

                    tvResult.setText("Hòa vốn");
                    tvResult.setTextColor(getResources().getColor(android.R.color.darker_gray));
                }

            } catch (Exception e) {
                tvResult.setText("Lỗi dữ liệu");
            }
        });
    }

    // PURITY MAP
    private double getPurity(String type) {

        switch (type) {
            case "24K": return 1.0;
            case "22K": return 22.0 / 24.0;
            case "18K": return 18.0 / 24.0;
            case "14K": return 14.0 / 24.0;
            case "10K": return 10.0 / 24.0;
            default: return 1.0;
        }
    }

    // UNIT CONVERT
    private double convertToGram(double value, String unit) {

        switch (unit) {
            case "Chỉ":
                return value * 3.75;
            case "Lượng":
                return value * 37.5;
            case "Ounce":
                return value * 31.1035;
            default:
                return value;
        }
    }

    // FORMAT MONEY
    private String format(double number) {

        NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));
        return format.format(Math.round(number));
    }
}