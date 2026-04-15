package com.goldtracker.ui.converter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.goldtracker.Data.AppDatabase;
import com.goldtracker.Data.History;
import com.goldtracker.MainActivity;
import com.goldtracker.R;
import com.goldtracker.model.GoldResponse;
import com.goldtracker.repository.GoldRepository;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ConverterFragment extends Fragment {

    private EditText edtAmount;
    private TextView tvResult;
    private Button btnConvert;
    private Spinner spGoldType, spUnit;

    private GoldRepository repo;
    private List<GoldResponse.GoldPrice> goldPrices;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_converter, container, false);

        edtAmount = view.findViewById(R.id.edtGold);
        tvResult = view.findViewById(R.id.tvResult);
        btnConvert = view.findViewById(R.id.btnConvert);
        spGoldType = view.findViewById(R.id.spGoldType);
        spUnit = view.findViewById(R.id.spUnit);

        repo = new GoldRepository(requireContext());

        setupSpinners();
        loadPrices();
        setupConvertButton();

        return view;
    }

    // =========================
    // Load bảng giá vàng
    // =========================
    private void loadPrices() {
        // Nên hiện một ProgressBar ở đây để người dùng biết là đang load giá
        repo.getCurrentGoldPrices(new GoldRepository.GoldCallback() {
            @Override
            public void onSuccess(List<GoldResponse.GoldPrice> prices, double worldPrice, double rate) {
                // Rất quan trọng: Phải gán giá trị này để setupConvertButton có dữ liệu dùng
                goldPrices = prices;

                // Nếu bạn muốn hiện thông báo "Đã cập nhật giá mới nhất"
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        // Có thể thông báo cho người dùng hoặc cho phép bấm nút convert
                        btnConvert.setEnabled(true);
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        tvResult.setText("Lỗi: Không thể lấy giá vàng.");
                    });
                }
            }
        });
    }

    // =========================
    // Spinner data
    // =========================
    private void setupSpinners() {

        if (getContext() == null) return;

        String[] types = {"24K", "22K", "18K", "14K", "10K"};
        String[] units = {"Gram", "Chỉ", "Lượng", "Ounce"};

        spGoldType.setAdapter(new ArrayAdapter<>(
                getContext(),
                R.layout.item_spinner_selected,
                types
        ));

        spUnit.setAdapter(new ArrayAdapter<>(
                getContext(),
                R.layout.item_spinner_selected,
                units
        ));
    }

    // Convert
    private void setupConvertButton() {

        btnConvert.setOnClickListener(v -> {

            if (goldPrices == null || goldPrices.isEmpty()) {
                tvResult.setText("Chưa có dữ liệu giá vàng");
                return;
            }

            String inputStr = edtAmount.getText().toString().trim();

            if (inputStr.isEmpty()) {
                tvResult.setText("Nhập số lượng!");
                return;
            }

            double amount = Double.parseDouble(inputStr);

            String selectedType = spGoldType.getSelectedItem().toString();
            GoldResponse.GoldPrice selected = null;

            for (GoldResponse.GoldPrice p : goldPrices) {
                if (p.type.contains(selectedType)) {
                    selected = p;
                    break;
                }
            }

            if (selected == null) {
                tvResult.setText("Không tìm thấy loại vàng");
                return;
            }

            double vndPerGram = selected.pricePerGramVnd;

            // đổi đơn vị về gram
            double gram;

            switch (spUnit.getSelectedItemPosition()) {
                case 0: // gram
                    gram = amount;
                    break;
                case 1: // chỉ
                    gram = amount * 3.75;
                    break;
                case 2: // lượng
                    gram = amount * 37.5;
                    break;
                case 3: // ounce
                    gram = amount * 31.1035;
                    break;
                default:
                    gram = amount * 31.1035;
            }

            double result = gram * vndPerGram;

            tvResult.setText(format(result) + " VND");

            // lưu history

            History history = new History();
            history.goldType = selected.type;
            history.goldPrice = vndPerGram;
            history.unit = spUnit.getSelectedItem().toString();
            history.quantity = amount;
            history.result = result;
            history.time = System.currentTimeMillis();

            new Thread(() -> {
                try {
                    AppDatabase db = AppDatabase.getInstance(requireContext());
                    db.historyDao().insert(history);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private String format(double number) {
        NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));
        return format.format(number);
    }
}