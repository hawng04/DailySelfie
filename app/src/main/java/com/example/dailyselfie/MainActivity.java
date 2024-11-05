package com.example.dailyselfie;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;

    private ArrayList<SelfieItem> selfieList;
    private SelfieAdapter adapter;
    private ListView selfieListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        selfieListView = findViewById(R.id.selfieListView);
        selfieList = new ArrayList<>();
        adapter = new SelfieAdapter(this, selfieList);
        selfieListView.setAdapter(adapter);

        FloatingActionButton btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(view -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                openCamera();
            }
        });
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            if (photo != null) {
                String dateTime = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
                selfieList.add(new SelfieItem(photo, dateTime));
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Error capturing photo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId(); // Lấy ID của item

        if (id == R.id.delete_selected) {
            // Xóa ảnh được chọn
            return true;
        } else if (id == R.id.delete_all) {
            // Xóa tất cả ảnhD
            selfieList.clear();
            adapter.notifyDataSetChanged();
            return true;
        } else if (id == R.id.camera) {
            openCamera();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
