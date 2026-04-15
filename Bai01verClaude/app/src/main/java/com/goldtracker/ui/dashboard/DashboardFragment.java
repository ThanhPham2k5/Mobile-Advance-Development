package com.goldtracker.ui.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.goldtracker.R;
import com.goldtracker.model.GoldResponse;
import com.goldtracker.repository.GoldRepository;

import java.util.*;

public class DashboardFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private GoldAdapter adapter;
    private GoldRepository repository;
    private TextView tvWorldPrice;
    private TextView tvUsdVnd;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              Bundle savedInstanceState) {

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        tvWorldPrice = view.findViewById(R.id.tvWorldPrice);
        tvUsdVnd = view.findViewById(R.id.tvUsdVnd);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GoldAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        repository = new GoldRepository(requireContext());

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeColors(Color.parseColor("#D4A843"));

        swipeRefresh.setOnRefreshListener(() -> {
            loadData();
        });

        loadData();
    }

    private void loadData() {
        if (!swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        repository.getCurrentGoldPrices(new GoldRepository.GoldCallback() {
            @Override
            public void onSuccess(List<GoldResponse.GoldPrice> prices, double worldPrice, double rate) {
                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);

                    adapter.setData(prices);
                    tvWorldPrice.setText(String.format("%,.2f USD/oz", worldPrice));
                    tvUsdVnd.setText(String.format("%,.0f", rate));
                });
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
                });

                e.printStackTrace();
            }
        });
    }
}