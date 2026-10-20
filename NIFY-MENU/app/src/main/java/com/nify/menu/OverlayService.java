package com.nify.menu;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.FrameLayout;

public class OverlayService extends Service {
    private WindowManager wm;
    private View panel;
    private WindowManager.LayoutParams params;
    private int accent = Color.rgb(78, 205, 255);
    private boolean compact = false;
    private TextView status;
    private TextView crosshair;
    private boolean crosshairOn = true;
    private boolean demoAimbot = false;
    private boolean demoWallhack = false;

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }

    @Override public void onCreate() {
        super.onCreate();
        createNotification();
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) { stopSelf(); return; }
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        buildPanel();
        try {
            wm.addView(panel, params);
            addCrosshair();
        } catch (Exception e) { stopSelf(); }
    }

    private void createNotification() {
        String channel = "nify_overlay";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(channel, "NIFY floating menu", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
        }
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, channel) : new Notification.Builder(this);
        b.setContentTitle("NIFY MENU активен").setContentText("Тестовая плавающая панель")
                .setSmallIcon(android.R.drawable.ic_menu_manage).setOngoing(true);
        startForeground(7, b.build());
    }

    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), Color.rgb(58, 75, 93));
        return d;
    }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        return t;
    }

    private void buildPanel() {
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setPadding(dp(12), dp(10), dp(12), dp(10));
        shell.setBackground(bg(Color.argb(245, 15, 21, 30), 18));
        shell.setElevation(dp(10));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text("NIFY MENU", 18, accent); title.setTypeface(null, Typeface.BOLD);
        header.addView(title, new LinearLayout.LayoutParams(0, dp(36), 1));
        Button close = smallButton("×");
        close.setOnClickListener(v -> stopSelf());
        header.addView(close, new LinearLayout.LayoutParams(dp(42), dp(36)));
        shell.addView(header);

        TextView subtitle = text("LOCAL DEMO • НЕ ВСТРАИВАЕТСЯ В ИГРУ", 9, Color.LTGRAY);
        shell.addView(subtitle);

        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, dp(5), 0, 0);

        content.addView(section("AIM UI"));
        addSwitch(content, "Настройки прицела", "Показывать прицел в этом демо", true, checked -> {
            crosshairOn = checked; if (crosshair != null) crosshair.setVisibility(checked ? View.VISIBLE : View.GONE);
        });
        content.addView(text("Размер прицела", 12, Color.LTGRAY));
        SeekBar size = new SeekBar(this); size.setMax(18); size.setProgress(6);
        size.setProgressTintList(android.content.res.ColorStateList.valueOf(accent));
        size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean user) { if (crosshair != null) crosshair.setTextSize(12 + p); }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });
        content.addView(size);

        content.addView(section("VISUAL"));
        addSwitch(content, "Тема: Cyan", "Переключить акцентный цвет", true, checked -> {
            accent = checked ? Color.rgb(78,205,255) : Color.rgb(188,130,255);
            title.setTextColor(accent);
            status.setTextColor(accent);
            size.setProgressTintList(android.content.res.ColorStateList.valueOf(accent));
        });
        addSwitch(content, "Компактный режим", "Уменьшить отступы панели", false, checked -> {
            shell.setPadding(dp(checked ? 7 : 12), dp(checked ? 6 : 10), dp(checked ? 7 : 12), dp(checked ? 6 : 10));
        });

        content.addView(section("PERFORMANCE"));
        addSwitch(content, "FPS-индикатор DEMO", "Показывает метку, не FPS игры", false, checked -> {
            status.setText(checked ? "● DEMO FPS: —" : "● READY");
        });

        content.addView(section("SANDBOX"));
        addSwitch(content, "Aimbot simulation", "Только двигает маркер в демо", false, checked -> {
            demoAimbot = checked;
            status.setText(checked ? "● AIM SIMULATION ON" : "● READY");
        });
        addSwitch(content, "Wallhack simulation", "Показывает тестовую рамку здесь", false, checked -> {
            demoWallhack = checked;
            status.setText(checked ? "● WALLHACK MOCK ON" : "● READY");
        });
        TextView warning = text("Эти переключатели не читают память игры, не наводят оружие и не раскрывают игроков сквозь стены.", 10, Color.rgb(190, 198, 210));
        warning.setPadding(0, dp(7), 0, dp(5));
        content.addView(warning);

        status = text("● READY", 11, accent);
        status.setPadding(0, dp(9), 0, 0);
        content.addView(status);

        scroll.addView(content);
        shell.addView(scroll, new LinearLayout.LayoutParams(-1, dp(365)));
        panel = shell;

        int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        params = new WindowManager.LayoutParams(dp(285), dp(455), type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = dp(16); params.y = dp(90);

        header.setOnTouchListener(new View.OnTouchListener() {
            int startX, startY; float downX, downY;
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    startX = params.x; startY = params.y; downX = e.getRawX(); downY = e.getRawY(); return true;
                }
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    params.x = startX + (int)(e.getRawX() - downX);
                    params.y = startY + (int)(e.getRawY() - downY);
                    try { wm.updateViewLayout(panel, params); } catch (Exception ignored) {}
                    return true;
                }
                return e.getAction() == MotionEvent.ACTION_UP;
            }
        });
    }

    private TextView section(String name) {
        TextView t = text(name, 11, accent); t.setTypeface(null, Typeface.BOLD);
        t.setPadding(0, dp(10), 0, dp(3)); return t;
    }

    private Button smallButton(String label) {
        Button b = new Button(this); b.setText(label); b.setTextSize(18); b.setAllCaps(false);
        b.setTextColor(Color.WHITE); b.setBackground(bg(Color.rgb(35, 49, 65), 9)); return b;
    }

    private void addSwitch(LinearLayout parent, String label, String detail, boolean initial, CompoundButton.OnCheckedChangeListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(0, dp(5), 0, dp(5));
        LinearLayout labels = new LinearLayout(this); labels.setOrientation(LinearLayout.VERTICAL);
        labels.addView(text(label, 13, Color.WHITE));
        labels.addView(text(detail, 9, Color.rgb(150, 164, 181)));
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        Switch sw = new Switch(this); sw.setChecked(initial); sw.setOnCheckedChangeListener(listener);
        row.addView(sw);
        parent.addView(row);
    }

    private void addCrosshair() {
        crosshair = text("+", 22, accent);
        crosshair.setGravity(Gravity.CENTER);
        crosshair.setShadowLayer(dp(4), 0, 0, Color.BLACK);
        int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams cp = new WindowManager.LayoutParams(dp(48), dp(48), type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        cp.gravity = Gravity.CENTER;
        try { wm.addView(crosshair, cp); } catch (Exception ignored) {}
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }
    @Override public void onDestroy() {
        if (wm != null) {
            try { if (panel != null) wm.removeView(panel); } catch (Exception ignored) {}
            try { if (crosshair != null) wm.removeView(crosshair); } catch (Exception ignored) {}
        }
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
