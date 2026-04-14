package com.goldtracker.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goldtracker.Data.AppDatabase;
import com.goldtracker.Data.History;
import com.goldtracker.MainActivity;
import com.goldtracker.R;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView rcHistory;
    private HistoryAdapter adapter;
    private List<History> list = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        rcHistory = view.findViewById(R.id.rcHistory);

        rcHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HistoryAdapter(list);
        rcHistory.setAdapter(adapter);

        loadHistory();

        return view;
    }

    // =========================
    // Load data từ Room
    // =========================
    private void loadHistory() {

        new Thread(() -> {

            try {
                AppDatabase db = AppDatabase.getInstance(requireContext());

                List<History> data = db.historyDao().getAll();

                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    list.clear();
                    if (data != null) {
                        list.addAll(data);
                    }
                    adapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }
}