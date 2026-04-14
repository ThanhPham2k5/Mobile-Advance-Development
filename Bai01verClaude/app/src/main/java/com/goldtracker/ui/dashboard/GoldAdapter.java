package com.goldtracker.ui.dashboard;

import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goldtracker.R;
import com.goldtracker.model.GoldResponse;

import java.util.List;

public class GoldAdapter extends RecyclerView.Adapter<GoldAdapter.ViewHolder> {

    private List<GoldResponse.GoldPrice> list;

    public GoldAdapter(List<GoldResponse.GoldPrice> list) {
        this.list = list;
    }

    public void setData(List<GoldResponse.GoldPrice> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gold, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        GoldResponse.GoldPrice item = list.get(position);

        holder.txtType.setText(item.type);
        holder.txtGram.setText("Gram: " + format(item.pricePerGramVnd));
        holder.txtChi.setText("Chỉ: " + format(item.pricePerChiVnd));
        holder.txtLuong.setText("Lượng: " + format(item.pricePerLuongVnd));
        holder.txtOunce.setText("Ounce: " + format(item.pricePerOunceVnd));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtType, txtGram, txtChi, txtLuong, txtOunce;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtType = itemView.findViewById(R.id.txtType);
            txtGram = itemView.findViewById(R.id.txtGram);
            txtChi = itemView.findViewById(R.id.txtChi);
            txtLuong = itemView.findViewById(R.id.txtLuong);
            txtOunce = itemView.findViewById(R.id.txtOunce);
        }
    }

    private String format(double value) {
        return String.format("%,.0f VND", value);
    }
}