package com.goldtracker.ui.portfolio;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;

import com.goldtracker.R;
import com.goldtracker.model.GoldResponse;
import com.goldtracker.repository.GoldRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfitFragment extends Fragment {

    private EditText edtQuantity, edtBuyPrice;
    private Spinner spinnerType, spinnerUnit;
    private Button btnCalc;
    private TextView tvResult;

    private GoldRepository repo;

    private Map<String, GoldResponse.GoldPrice> priceMap = new HashMap<>();

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

    private void loadPrice() {
        // Thêm 2 tham số worldPrice và rate vào onSuccess
        repo.getCurrentGoldPrices(new GoldRepository.GoldCallback() {
            @Override
            public void onSuccess(java.util.List<GoldResponse.GoldPrice> data, double worldPrice, double rate) {
                for (GoldResponse.GoldPrice item : data) {

                    String key = item.type;

                    if (key.contains("24K")) key = "24K";
                    else if (key.contains("22K")) key = "22K";
                    else if (key.contains("18K")) key = "18K";
                    else if (key.contains("14K")) key = "14K";
                    else if (key.contains("10K")) key = "10K";

                    priceMap.put(key, item);
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

    private void setupSpinners() {

        String[] goldTypes = {"24K", "22K", "18K", "14K", "10K"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.item_spinner_selected,
                goldTypes
        );
        spinnerType.setAdapter(typeAdapter);

        String[] units = {"Gram", "Chỉ", "Lượng", "Ounce"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.item_spinner_selected,
                units
        );
        spinnerUnit.setAdapter(unitAdapter);
    }

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

                GoldResponse.GoldPrice price = priceMap.get(goldType);

                if (price == null) {
                    tvResult.setText("Không có dữ liệu giá");
                    return;
                }

                BigDecimal unitPrice;

                switch (unit) {
                    case "Gram":
                        unitPrice = BigDecimal.valueOf(price.pricePerGramVnd);
                        break;

                    case "Chỉ":
                        unitPrice = BigDecimal.valueOf(price.pricePerChiVnd);
                        break;

                    case "Lượng":
                        unitPrice = BigDecimal.valueOf(price.pricePerLuongVnd);
                        break;

                    case "Ounce":
                        unitPrice = BigDecimal.valueOf(price.pricePerOunceVnd);
                        break;

                    default:
                        unitPrice = BigDecimal.valueOf(price.pricePerGramVnd);
                }

                BigDecimal qtyBD = BigDecimal.valueOf(quantity);

                BigDecimal marketTotal = unitPrice.multiply(qtyBD);

                BigDecimal buyTotal = BigDecimal.valueOf(buyPrice);

                BigDecimal profit = marketTotal.subtract(buyTotal)
                        .setScale(0, RoundingMode.HALF_UP);

                int cmp = profit.compareTo(BigDecimal.ZERO);

                if (cmp > 0) {
                    tvResult.setText("Lãi: " + format(profit) + " VND");
                    tvResult.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

                } else if (cmp < 0) {
                    tvResult.setText("Lỗ: " + format(profit.abs()) + " VND");
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

    // =========================
    // FORMAT MONEY
    // =========================
    private String format(BigDecimal number) {
        NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));
        return format.format(number);
    }
}