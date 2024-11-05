package com.example.dailyselfie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class SelfieAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<SelfieItem> selfies;

    public SelfieAdapter(Context context, ArrayList<SelfieItem> selfies) {
        this.context = context;
        this.selfies = selfies;
    }

    @Override
    public int getCount() {
        return selfies.size();
    }

    @Override
    public Object getItem(int position) {
        return selfies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }

        ImageView selfieImage = convertView.findViewById(R.id.imageView);
        TextView selfieDateTime = convertView.findViewById(R.id.dateTimeView);
        CheckBox checkBox = convertView.findViewById(R.id.checkBox);

        SelfieItem selfie = selfies.get(position);
        selfieImage.setImageBitmap(selfie.getImage());
        selfieDateTime.setText(selfie.getDateTime());

        return convertView;
    }
}
