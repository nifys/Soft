package com.nify.menu;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setBackgroundColor(Color.rgb(10, 14, 21));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(32), dp(24), dp(24));
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setBackgroundColor(Color.rgb(10, 14, 21));

        TextView title = new TextView(this);
        title.setText("NIFY MENU");
        title.setTextColor(Color.rgb(87, 210, 255));
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, 1);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(56)));

        TextView desc = new TextView(this);
        desc.setText("Плавающая тестовая панель\\nРаботает отдельно от Standoff 2. Не внедряется в игру.");
        desc.setTextColor(Color.LTGRAY);
        desc.setTextSize(15);
        desc.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(-1, dp(84));
        dlp.bottomMargin = dp(20);
        root.addView(desc, dlp);

        Button permission = makeButton("1. Разрешить отображение поверх окон");
        permission.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(i);
            } else {
                startOverlay();
            }
        });
        root.addView(permission, buttonParams());

        Button launch = makeButton("2. Запустить плавающее меню");
        launch.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())));
            } else startOverlay();
        });
        LinearLayout.LayoutParams lp = buttonParams();
        lp.topMargin = dp(12);
        root.addView(launch, lp);

        TextView note = new TextView(this);
        note.setText("Aim UI / Crosshair / Theme / Demo FPS\\nAimbot и Wallhack — только локальные симуляции.");
        note.setTextColor(Color.rgb(140, 155, 175));
        note.setTextSize(12);
        note.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(-1, dp(72));
        nlp.topMargin = dp(20);
        root.addView(note, nlp);

        setContentView(root);
    }

    private Button makeButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(30, 105, 145)));
        return b;
    }

    private LinearLayout.LayoutParams buttonParams() {
        return new LinearLayout.LayoutParams(-1, dp(54));
    }

    private void startOverlay() {
        Intent i = new Intent(this, OverlayService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i); else startService(i);
        finish();
    }
}
