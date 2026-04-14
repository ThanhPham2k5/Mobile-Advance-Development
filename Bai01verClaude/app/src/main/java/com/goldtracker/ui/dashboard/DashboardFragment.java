package com.goldtracker.ui.dashboard;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.goldtracker.R;
import com.goldtracker.model.GoldResponse;
import com.goldtracker.repository.GoldRepository;

import java.util.*;

public class DashboardFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private GoldAdapter adapter;
    private GoldRepository repository;

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

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GoldAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        repository = new GoldRepository(requireContext());

        loadData();
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);

        repository.getCurrentGoldPrices(new GoldRepository.GoldCallback() {
            @Override
            public void onSuccess(List<GoldResponse.GoldPrice> prices) {

                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    adapter.setData(prices);
                });
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
                });

                e.printStackTrace();
            }
        });
    }
}