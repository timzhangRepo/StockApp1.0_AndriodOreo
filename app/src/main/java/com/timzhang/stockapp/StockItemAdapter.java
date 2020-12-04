package com.timzhang.stockapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StockItemAdapter extends RecyclerView.Adapter<StockItemAdapter.StockViewHolder>{
    private ArrayList<StockItem> favorites;
    // 这里写购买的arraylist

    public static class StockViewHolder extends RecyclerView.ViewHolder{
        public TextView ticker;
        public TextView name;
        public TextView price;
        public TextView change;
        public ImageView trend;
        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            ticker = itemView.findViewById(R.id.stockItem_textView1);
            name = itemView.findViewById(R.id.stockItem_textView3);
            price = itemView.findViewById(R.id.stockItem_textView2);
            change = itemView.findViewById(R.id.stockItem_textView4);
            trend = itemView.findViewById(R.id.stock_trend);
        }
    }

    /**
     * 在parameter 添加stockPurchased Arraylist
     * @param favorites
     */
    public StockItemAdapter(ArrayList<StockItem> favorites){
        this.favorites = favorites;
    }
    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_item, parent, false);
        StockViewHolder svh = new StockViewHolder(v);
        return svh;
    }

    /**
     * 如果购买过，则修改name为购买后的数量
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        StockItem currentItem = favorites.get(position);
        //如果购买过，则覆盖text为购买的数量，逻辑写在这里
        holder.name.setText(String.valueOf(currentItem.getInfo()));
        holder.ticker.setText(currentItem.getTicker());
        holder.change.setText(String.valueOf(currentItem.getChange()));
        holder.price.setText(String.valueOf((currentItem.getCurrent_price())));
        if(currentItem.getChange()>=0){
            holder.trend.setImageResource(R.drawable.ic_twotone_trending_up_24);
        }else{
            holder.trend.setImageResource(R.drawable.ic_baseline_trending_down_24);
        }
    }
    @Override
    public int getItemCount() {
        return favorites.size();
    }
}
