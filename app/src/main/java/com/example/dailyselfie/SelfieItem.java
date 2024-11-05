package com.example.dailyselfie;

import android.graphics.Bitmap;

public class SelfieItem {
    private Bitmap image;
    private String dateTime;

    public SelfieItem(Bitmap image, String dateTime) {
        this.image = image;
        this.dateTime = dateTime;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getDateTime() {
        return dateTime;
    }
}
