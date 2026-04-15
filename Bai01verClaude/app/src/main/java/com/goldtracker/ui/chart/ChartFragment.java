package com.goldtracker.ui.chart;


import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.goldtracker.R;
import com.goldtracker.repository.GoldRepository;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChartFragment extends Fragment {

    private LineChart lineChart;
    private GoldRepository repo;
    private TextView tvMinPrice, tvMaxPrice;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        lineChart = view.findViewById(R.id.lineChart);
        tvMinPrice = view.findViewById(R.id.tvMinPrice);
        tvMaxPrice = view.findViewById(R.id.tvMaxPrice);
        repo = new GoldRepository(requireContext());
        loadChartData();
        return view;
    }

    // =========================
    // Load data 7 ngày
    // =========================
    private void loadChartData() {

        repo.getLast7DaysOuncePrices(new GoldRepository.ChartCallback() {

            @Override
            public void onSuccess(List<AbstractMap.SimpleEntry<String, Double>> data) {

                requireActivity().runOnUiThread(() -> showChart(data));
            }

            @Override
            public void onError(Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(),
                                "Lỗi load chart",
                                Toast.LENGTH_SHORT).show());
            }
        });
    }

    // =========================
    // Render chart
    // =========================
    private void showChart(List<AbstractMap.SimpleEntry<String, Double>> data) {
        if (data == null || data.isEmpty()) return;

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        double min = data.get(0).getValue();
        double max = data.get(0).getValue();

        for (int i = 0; i < data.size(); i++) {
            double val = data.get(i).getValue();
            labels.add(data.get(i).getKey());
            entries.add(new Entry((float) i, (float) val));

            if (val < min) min = val;
            if (val > max) max = val;
        }

        // Cập nhật 2 Card Thấp nhất/Cao nhất
        tvMinPrice.setText(formatVND(min));
        tvMaxPrice.setText(formatVND(max));

        LineDataSet dataSet = new LineDataSet(entries, "Giá Vàng");

        // ─── TÙY CHỈNH ĐỂ ĐẸP HƠN ───
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Uốn lượn mềm mại
        dataSet.setColor(Color.parseColor("#D4A843")); // Màu vàng chủ đạo
        dataSet.setLineWidth(3f); // Đường kẻ đậm hơn
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(Color.parseColor("#D4A843"));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.parseColor("#1C1C26")); // Màu nền của card

        // Vẽ vùng đổ bóng phía dưới
        dataSet.setDrawFilled(true);
        dataSet.setFillDrawable(getResources().getDrawable(R.drawable.chart_gradient)); // Tạo gradient ở bước 3

        dataSet.setDrawValues(false); // Tắt số hiển thị trên từng điểm cho sạch

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // ─── TÙY CHỈNH TRỤC (AXIS) ───
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#9898A8"));
        xAxis.setDrawGridLines(false); // Tắt lưới dọc
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        lineChart.getAxisRight().setEnabled(false); // Tắt trục bên phải
        lineChart.getAxisLeft().setTextColor(Color.parseColor("#9898A8"));
        lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getAxisLeft().setGridColor(Color.parseColor("#1E1E2A")); // Màu lưới mờ

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false); // Tắt chú thích vì mình đã có header rồi
        lineChart.animateY(1000); // Hiệu ứng mọc từ dưới lên
        lineChart.invalidate();
    }

    private String formatVND(double price) {
        return java.text.NumberFormat.getInstance(new Locale("vi", "VN")).format(price);
    }
}