package com.example.dailyselfie;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private static final String KEY_LAST_PHOTO_DATE = "last_photo_date";


    private ArrayList<SelfieItem> selfieList;
    private SelfieAdapter adapter;

    private ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm dd-MM-yyyy", java.util.Locale.getDefault());
                    java.util.Date resultDate = new java.util.Date(System.currentTimeMillis());
                    String dateTime = sdf.format(resultDate);
                    String filePath;

                    if (photo != null) {
                        savePhotoDateToday();
                        filePath = saveImageToStorage(photo, dateTime);
                        showToast("File path:" + filePath);
                        SelfieItem selfieItem = new SelfieItem(photo, dateTime, filePath);
                        selfieList.add(0, selfieItem);
                    } else {
                        showToast("Error capturing photo");
                    }

                    adapter.notifyDataSetChanged();
                }
            });

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();
        setupListView();
        setupCameraButton();

        requestPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_REQUEST_CODE);

        loadImages();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupListView() {
        ListView selfieListView = findViewById(R.id.selfieListView);
        selfieList = new ArrayList<>();
        adapter = new SelfieAdapter(this, selfieList);
        selfieListView.setAdapter(adapter);
    }

    private void setupCameraButton() {
        FloatingActionButton btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(view -> {
            if (hasPermission(Manifest.permission.CAMERA)) {
                openCamera();
            } else {
                requestPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_REQUEST_CODE);
            }
        });
    }

    private void loadImages() {
        selfieList.clear();
        adapter.notifyDataSetChanged();
        Uri directoryUri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/DailySelfie");

        String folderPath = directoryUri.getPath();

        List<Uri> images = getImagesFromFolder(folderPath);

        if (images != null) {
            for (Uri imageUri : images) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    String filePath = getRealPathFromURI(imageUri);

                    String dateTime = getDateTaken(filePath);

                    SelfieItem selfieItem = new SelfieItem(bitmap, dateTime, filePath);
                    selfieList.add(0, selfieItem);

                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        String path = null;
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            path = cursor.getString(columnIndex);
            cursor.close();
        }
        return path;
    }


    private String getDateTaken(String filePath) {
        File file = new File(filePath);

        if (file.exists()) {
            long lastModified = file.lastModified();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm dd-MM-yyyy", java.util.Locale.getDefault());
            java.util.Date resultDate = new java.util.Date(lastModified);
            return sdf.format(resultDate);
        } else {
            showToast("File does not exist!");
            return null;
        }
    }


    public List<Uri> getImagesFromFolder(String folderPath) {
        List<Uri> images = new ArrayList<>();

        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.DATA + " like ? ";
        String[] selectionArgs = new String[]{"%" + folderPath + "%"};

        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );

        if (cursor != null) {
            int columnIndexId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(columnIndexId);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                images.add(imageUri);
            }
            cursor.close();
        }

        return images;
    }


    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(String permission, int requestCode) {
        if (!hasPermission(permission)) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            cameraLauncher.launch(cameraIntent);
        } catch (ActivityNotFoundException e) {
            showToast("No camera app found");
        }
    }

    private void savePhotoDateToday() {
        SharedPreferences.Editor editor = getSharedPreferences("selfie_prefs", MODE_PRIVATE).edit();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        editor.putString(KEY_LAST_PHOTO_DATE, today);
        editor.apply();
    }

    private String saveImageToStorage(Bitmap imageBitmap, String dateTime) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpg";

        File picturesDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "dailyselfie");
        if (!picturesDirectory.exists()) {
            picturesDirectory.mkdirs();
        }

        File imageFile = new File(picturesDirectory, fileName);
        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            ExifInterface exif = new ExifInterface(imageFile);
            exif.setAttribute(ExifInterface.TAG_DATETIME, dateTime);
            exif.saveAttributes();

            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            showToast("Camera permission denied");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.delete_selected) {
            adapter.removeCheckedItems();
            return true;
        } else if (id == R.id.select_all) {
            adapter.toggleSelectAllItems();
            boolean allSelected = true;
            for (SelfieItem selfie : selfieList) {
                if (!selfie.isChecked()) {
                    allSelected = false;
                    break;
                }
            }

            if (allSelected) {
                item.setTitle("Unselect all");
            } else {
                item.setTitle("Select all");
            }

            adapter.notifyDataSetChanged();
            return true;
        } else if (id == R.id.camera) {
            openCamera();
            return true;
        } else if (id == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
