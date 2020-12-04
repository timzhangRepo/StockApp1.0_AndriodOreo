package com.timzhang.stockapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private ArrayList<NewsItem> newsItemArrayList;
    public static class NewsViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public TextView textView1;
        public TextView textView2;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.newsImage);
            textView1 = itemView.findViewById(R.id.newsSource);
            textView2 = itemView.findViewById(R.id.newsTitle);
        }
    }
    public NewsAdapter(ArrayList<NewsItem> newsItemArrayList){
        this.newsItemArrayList = newsItemArrayList;
    }
    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);
        NewsViewHolder nvh = new NewsViewHolder(v);
        return nvh;
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem currentItem = newsItemArrayList.get(position);
        Picasso.get().load(currentItem.getmURLtoImage()).into(holder.imageView);
        holder.textView1.setText(currentItem.getmText1());
        holder.textView2.setText(currentItem.getmText2());
    }

    @Override
    public int getItemCount() {
        return newsItemArrayList.size();
    }
}
