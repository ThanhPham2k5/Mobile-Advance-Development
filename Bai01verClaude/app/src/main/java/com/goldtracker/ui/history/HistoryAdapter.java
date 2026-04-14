package com.goldtracker.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goldtracker.Data.History;
import com.goldtracker.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.Holder> {

    private List<History> list;

    public HistoryAdapter(List<History> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        History h = list.get(position);

        holder.tvType.setText(h.goldType);

        holder.tvDetail.setText(
                h.quantity + " " + h.unit + " - " +
                        format(h.result) + " VND"
        );

        holder.tvTime.setText(formatTime(h.time));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {

        TextView tvType, tvDetail, tvTime;

        public Holder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvDetail = itemView.findViewById(R.id.tvDetail);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    private String format(double number) {
        return NumberFormat.getInstance(new Locale("vi", "VN"))
                .format(number);
    }

    private String formatTime(long timeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }
}