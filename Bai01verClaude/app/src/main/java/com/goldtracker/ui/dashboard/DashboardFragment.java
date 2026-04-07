package com.goldtracker.ui.dashboard;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;

import com.goldtracker.R;
import com.goldtracker.model.GoldResponse;
import com.goldtracker.repository.GoldRepository;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.*;

public class DashboardFragment extends Fragment {

    private TextView tvPrice;
    private GoldRepository repo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tvPrice = view.findViewById(R.id.tvPrice);
        repo = new GoldRepository();

        loadPrice();

        return view;
    }

    private void loadPrice() {
        repo.getGoldPrice().enqueue(new Callback<GoldResponse>() {
            @Override
            public void onResponse(Call<GoldResponse> call, Response<GoldResponse> response) {
                if (response.body() != null) {
                    double price = response.body().getRates().get("VND");
                    tvPrice.setText(format(Math.round(price)) + " VND/Ounce");
                }
            }

            @Override
            public void onFailure(Call<GoldResponse> call, Throwable t) {
                tvPrice.setText("Fail: " + t.getMessage());
            }
        });
    }

    private String format(long number) {
        NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));
        return format.format(number);
    }
}