
package com.flash.booster;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int BG = Color.rgb(12, 12, 12);
    private static final int CARD = Color.rgb(29, 27, 22);
    private static final int GOLD = Color.rgb(245, 195, 55);
    private static final int WHITE = Color.WHITE;
    private static final int GRAY = Color.LTGRAY;

    private LinearLayout root;
    private TextView ramValue;
    private TextView thermalValue;
    private TextView refreshValue;
    private TextView feedbackValue;

    private final String GAME_PACKAGE = "com.tencent.ig";

    private final String sensitivityProfile =
        "FLASH GAME BOOSTER - PUBG GLOBAL\n" +
        "CONTROL: 4 FINGER\n" +
        "GYROSCOPE: OFF\n\n" +
        "CAMERA SENSITIVITY\n" +
        "TPP No Scope: 120%\n" +
        "FPP No Scope: 110%\n" +
        "Red Dot: 50%\n" +
        "2x Scope: 35%\n" +
        "3x Scope: 25%\n" +
        "4x Scope: 20%\n" +
        "6x Scope: 12%\n" +
        "8x Scope: 10%\n\n" +
        "ADS SENSITIVITY\n" +
        "TPP No Scope: 110%\n" +
        "FPP No Scope: 100%\n" +
        "Red Dot: 45%\n" +
        "2x Scope: 32%\n" +
        "3x Scope: 24%\n" +
        "4x Scope: 18%\n" +
        "6x Scope: 10%\n" +
        "8x Scope: 8%\n\n" +
        "GYROSCOPE: OFF";

    private GradientDrawable shape(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(24);
        d.setStroke(1, GOLD);
        return d;
    }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private void heading(String value) {
        TextView t = text(value, 15, GOLD);
        t.setPadding(4, 22, 4, 10);
        root.addView(t);
    }

    private TextView card(String title, String value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(18, 16, 18, 16);
        box.setBackground(shape(CARD));

        TextView label = text(title, 12, GRAY);
        TextView result = text(value, 19, GOLD);
        result.setPadding(0, 10, 0, 0);

        box.addView(label);
        box.addView(result);

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 6, 0, 6);

        root.addView(box, p);
        return result;
    }

    private void button(String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.BLACK);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(shape(GOLD));

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 7, 0, 7);

        root.addView(b, p);
        b.setOnClickListener(v -> action.run());
    }

    private void updateDeviceInfo() {
        ActivityManager manager =
            (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        ActivityManager.MemoryInfo memory =
            new ActivityManager.MemoryInfo();

        manager.getMemoryInfo(memory);

        long total = memory.totalMem / (1024 * 1024);
        long available = memory.availMem / (1024 * 1024);
        long used = total - available;

        ramValue.setText(
            String.format(Locale.US,
                "%d MB / %d MB", used, total)
        );

        refreshValue.setText(
            getWindowManager().getDefaultDisplay()
                .getRefreshRate() + " Hz"
        );

        if (Build.VERSION.SDK_INT >= 29) {
            PowerManager pm =
                (PowerManager) getSystemService(POWER_SERVICE);

            int status = pm.getCurrentThermalStatus();

            String[] states = {
                "NORMAL",
                "LIGHT",
                "MODERATE",
                "SEVERE",
                "CRITICAL",
                "EMERGENCY",
                "SHUTDOWN"
            };

            thermalValue.setText(states[
                Math.max(0, Math.min(status, states.length - 1))
            ]);
        } else {
            thermalValue.setText(
                "Android sürümü termal durum API'sini desteklemiyor"
            );
        }
    }

    private void launchGame() {
        Intent intent = getPackageManager()
            .getLaunchIntentForPackage(GAME_PACKAGE);

        if (intent == null) {
            Toast.makeText(
                this,
                "PUBG Mobile Global bulunamadı",
                Toast.LENGTH_LONG
            ).show();
            return;
        }

        updateDeviceInfo();
        startActivity(intent);
    }

    private long deleteOwnCache(File directory) {
        long deleted = 0;

        File[] files = directory.listFiles();
        if (files == null) return 0;

        for (File file : files) {
            if (file.isDirectory()) {
                deleted += deleteOwnCache(file);
            }

            if (file.isFile()) {
                deleted += file.length();
            }

            if (file.delete()) {
                // Only this app's cache is deleted.
            }
        }

        return deleted;
    }

    private void cleanOwnCache() {
        long bytes = deleteOwnCache(getCacheDir());

        Toast.makeText(
            this,
            "Uygulama önbelleği temizlendi: " +
            (bytes / 1024) + " KB",
            Toast.LENGTH_LONG
        ).show();
    }

    private void copyProfile() {
        ClipboardManager clipboard =
            (ClipboardManager) getSystemService(
                CLIPBOARD_SERVICE
            );

        clipboard.setPrimaryClip(
            ClipData.newPlainText(
                "PUBG Sensitivity",
                sensitivityProfile
            )
        );

        Toast.makeText(
            this,
            "Hassasiyet profili kopyalandı",
            Toast.LENGTH_SHORT
        ).show();
    }

    private void feedback(String result) {
        feedbackValue.setText(result);

        Toast.makeText(
            this,
            "Değerlendirmen kaydedildi",
            Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        ScrollView scroll = new ScrollView(this);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18, 12, 18, 25);
        root.setBackgroundColor(BG);

        scroll.addView(root);
        setContentView(scroll);

        TextView title = text(
            "FLASH GAME BOOSTER 🇹🇷",
            25, GOLD
        );
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 25, 0, 5);
        root.addView(title);

        TextView subtitle = text(
            "ULTIMATE GAMING EDITION",
            11, GRAY
        );
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);

        heading("OYUN PROFİLİ");

        card(
            "SELECTED GAME",
            "PUBG MOBILE GLOBAL"
        );

        card(
            "CONTROL PROFILE",
            "4 FINGER • NO GYRO"
        );

        heading("PERFORMANS MERKEZİ");

        ramValue = card("RAM USAGE", "Ölçülüyor...");
        refreshValue = card("DISPLAY REFRESH RATE", "Ölçülüyor...");
        thermalValue = card("ANDROID THERMAL STATUS", "Kontrol ediliyor...");

        button("🔄 PERFORMANS BİLGİLERİNİ YENİLE",
            this::updateDeviceInfo);

        button("🧹 UYGULAMA ÖNBELLEĞİNİ TEMİZLE",
            this::cleanOwnCache);

        button("🎮 PUBG MOBILE BAŞLAT",
            this::launchGame);

        heading("4 PARMAK • NO GYRO HASSASİYET");

        card(
            "CAMERA",
            "TPP 120% | FPP 110%\n" +
            "Red Dot 50% | 2x 35%\n" +
            "3x 25% | 4x 20%\n" +
            "6x 12% | 8x 10%"
        );

        card(
            "ADS",
            "TPP 110% | FPP 100%\n" +
            "Red Dot 45% | 2x 32%\n" +
            "3x 24% | 4x 18%\n" +
            "6x 10% | 8x 8%"
        );

        card(
            "GYROSCOPE",
            "OFF • NO GYRO"
        );

        button("📋 HASSASİYET PROFİLİNİ KOPYALA",
            this::copyProfile);

        heading("KONTROL TESTİ");

        card(
            "TEST AÇIKLAMASI",
            "Antrenman alanında hedef takibi ve geri tepme kontrolünü dene."
        );

        feedbackValue = card(
            "OYUNCU DEĞERLENDİRMESİ",
            "Henüz değerlendirilmedi"
        );

        button("✅ HASSASİYET İYİ",
            () -> feedback("İYİ"));

        button("➖ HASSASİYET ORTA",
            () -> feedback("ORTA - Küçük ayar değişikliği dene"));

        button("⚠️ HASSASİYET KÖTÜ",
            () -> feedback("KÖTÜ - Antrenmanda yeniden ayarla"));

        heading("BİLGİ");

        card(
            "PERFORMANS SINIRLARI",
            "Uygulama diğer uygulamaları zorla durdurmaz. " +
            "Temizlik yalnızca kendi önbelleğini kapsar. " +
            "PUBG'nin gerçek oyun içi FPS ve sunucu ping " +
            "değerleri doğrudan okunmaz. Hassasiyet değerleri " +
            "başlangıç önerisidir; oyun içinden elle uygulanır."
        );

        updateDeviceInfo();
    }
}
