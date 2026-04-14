package com.goldtracker.ui.chart;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class ChartFragment extends Fragment {

    private LineChart lineChart;
    private GoldRepository repo;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        lineChart = view.findViewById(R.id.lineChart);
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

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {

            AbstractMap.SimpleEntry<String, Double> item = data.get(i);

            labels.add(item.getKey());

            entries.add(new Entry(
                    (float) i,
                    item.getValue().floatValue()
            ));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Giá 1 Ounce (VND)");

        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);

        lineChart.setData(lineData);

        // =========================
        // X-axis label (dd/MM)
        // =========================
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        lineChart.getDescription().setEnabled(false);
        lineChart.animateX(1000);
        lineChart.invalidate();
    }
}