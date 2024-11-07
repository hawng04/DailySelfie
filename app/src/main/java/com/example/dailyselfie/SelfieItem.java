package com.example.dailyselfie;

import android.graphics.Bitmap;

public class SelfieItem {
    private Bitmap image;
    private String dateTime;
    private boolean isChecked;
    private String filePath;

    public SelfieItem(Bitmap image, String dateTime, String filePath) {
        this.image = image;
        this.dateTime = dateTime;
        this.isChecked = false;
        this.filePath = filePath;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getDateTime() {
        return dateTime;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
