package com.example.gallery;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import

import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {

    private ArrayList<DataClass> dataList;
    private Context context;

    public MyAdapter(ArrayList<DataClass> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.imageView);
        String imageUrl = dataList.get(position).getImageUrl();

        // Load the image using Glide
        Glide.with(context)
                .load(imageUrl)
                .into(imageView);

        return convertView;
    }
}
