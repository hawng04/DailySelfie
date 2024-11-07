package com.example.dailyselfie;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
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

        checkBox.setChecked(selfie.isChecked());

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selfies.get(position).setChecked(isChecked);
        });

        return convertView;
    }

    public void toggleSelectAllItems() {
        boolean allSelected = true;
        for (SelfieItem selfie : selfies) {
            if (!selfie.isChecked()) {
                allSelected = false;
                break;
            }
        }

        for (SelfieItem selfie : selfies) {
            selfie.setChecked(!allSelected);
        }

        notifyDataSetChanged();
    }

    public ArrayList<SelfieItem> getCheckedItems() {
        ArrayList<SelfieItem> selectedItems = new ArrayList<>();
        for (SelfieItem selfie : selfies) {
            if (selfie.isChecked()) {
                selectedItems.add(selfie);
            }
        }
        return selectedItems;
    }


    public void removeCheckedItems() {
        ArrayList<SelfieItem> checkedItems = getCheckedItems();

        if (!checkedItems.isEmpty()) {
            new AlertDialog.Builder(context)
                    .setMessage("Are you sure you want to delete these files?")
                    .setTitle("Confirm Deletion")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean isDeleted;
                            for (SelfieItem item : checkedItems) {
                                String filePath = item.getFilePath();

                                if (!filePath.isEmpty()) {
                                    isDeleted = deleteFile(filePath);

                                    if (isDeleted) {
                                        Log.d("FileUtils", "File deleted: " + filePath);
                                    } else {
                                        Log.d("FileUtils", "Failed to delete file: " + filePath);
                                    }
                                } else Toast.makeText(context, "File path is empty!", Toast.LENGTH_SHORT).show();
                            }

                            selfies.removeAll(checkedItems);
                            notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();

            notifyDataSetChanged();
        }
    }

    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);

        if (file.exists()) {
            return file.delete();
        } else {
            return false;
        }
    }
}
