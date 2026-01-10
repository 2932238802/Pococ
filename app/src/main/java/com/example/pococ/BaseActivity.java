package com.example.pococ;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyNightMode();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("AppSettings", MODE_PRIVATE);
        float fontScale = prefs.getFloat("font_scale", 1.0f);

        Context context = applyFontScale(newBase, fontScale);
        super.attachBaseContext(context);
    }

    private void applyNightMode() {
        SharedPreferences appPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        boolean isNightMode = appPrefs.getBoolean("night_mode", false);
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private Context applyFontScale(Context context, float scale) {
        Configuration newConfig = new Configuration(context.getResources().getConfiguration());
        newConfig.fontScale = scale;
        return context.createConfigurationContext(newConfig);
    }
}
