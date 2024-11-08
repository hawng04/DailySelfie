package com.example.dailyselfie;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TimePicker;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "SettingsPrefs";
    private static final String KEY_HOUR = "hour";
    private static final String KEY_MINUTE = "minute";

    private TimePicker timePicker;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupToolbar();
        initializeTimePicker();

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveAlarmTime());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
    }

    private void initializeTimePicker() {
        timePicker = findViewById(R.id.simpleTimePicker);
//        timePicker.setIs24HourView(true);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedHour = sharedPreferences.getInt(KEY_HOUR, 8);
        int savedMinute = sharedPreferences.getInt(KEY_MINUTE, 0);

        timePicker.setHour(savedHour);
        timePicker.setMinute(savedMinute);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    @SuppressLint("DefaultLocale")
    private void saveAlarmTime() {
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        // Save the selected time
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(KEY_HOUR, hour);
        editor.putInt(KEY_MINUTE, minute);
        editor.apply();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        setAlarm(calendar);

        Toast.makeText(this, "Time saved: " + hour + ":" + String.format("%02d", minute), Toast.LENGTH_SHORT).show();
        finish();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(Calendar calendar) {
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}