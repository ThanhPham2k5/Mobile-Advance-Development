package com.goldtracker.ui.history;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;

import com.goldtracker.Data.History;
import com.goldtracker.MainActivity;
import com.goldtracker.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        listView = view.findViewById(R.id.listView);

        loadData();

        return view;
    }

    private void loadData() {
        List<History> list = MainActivity.db.historyDao().getAll();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(),
                android.R.layout.simple_list_item_1,
                convertToString(list)
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(Color.WHITE);

                return view;
            }
        };

        listView.setAdapter(adapter);
    }

    private List<String> convertToString(List<History> list) {
        List<String> data = new java.util.ArrayList<>();

        for (History h : list) {
            data.add(
                    "Vàng: " + h.goldAmount + " lượng\n" +
                            "GQD: " + format(Math.round(h.price)) + " VND\n" +
                            "KQ: " + format(Math.round(h.result)) + " VND\n" +
                            "Time: " + h.time
            );
        }

        return data;
    }

    private String format(long number) {
        NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));
        return format.format(number);
    }
}