package com.pubgflash.gamebooster.monitor;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

public class CpuThermalMonitor {

    private final Context context;

    public CpuThermalMonitor(Context context) {
        this.context = context.getApplicationContext();
    }

    public String getCpuTemperature() {
        File directory = new File("/sys/class/thermal");

        File[] zones = directory.listFiles();

        if (zones != null) {
            for (File zone : zones) {
                if (!zone.getName().startsWith("thermal_zone")) {
                    continue;
                }

                try {
                    String type = readFile(
                        new File(zone, "type")
                    ).toLowerCase(Locale.US);

                    if (!(type.contains("cpu") ||
                          type.contains("soc") ||
                          type.contains("ap"))) {
                        continue;
                    }

                    String value = readFile(
                        new File(zone, "temp")
                    );

                    double temperature =
                        Double.parseDouble(value.trim());

                    if (temperature > 1000) {
                        temperature /= 1000.0;
                    }

                    if (temperature >= 0 &&
                        temperature <= 150) {
                        return String.format(
                            Locale.US,
                            "%.1f °C",
                            temperature
                        );
                    }

                } catch (Exception ignored) {
                }
            }
        }

        return "N/A - Sensör erişilemiyor";
    }

    private String readFile(File file) throws Exception {
        BufferedReader reader =
            new BufferedReader(new FileReader(file));

        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    public String getThermalStatus() {
        if (Build.VERSION.SDK_INT < 29) {
            return "Android termal API desteklenmiyor";
        }

        try {
            PowerManager manager =
                (PowerManager) context.getSystemService(
                    Context.POWER_SERVICE
                );

            int status = manager.getCurrentThermalStatus();

            switch (status) {
                case PowerManager.THERMAL_STATUS_NONE:
                    return "Normal";

                case PowerManager.THERMAL_STATUS_LIGHT:
                    return "Hafif ısınma";

                case PowerManager.THERMAL_STATUS_MODERATE:
                    return "Orta seviye ısınma";

                case PowerManager.THERMAL_STATUS_SEVERE:
                    return "Yüksek ısınma";

                case PowerManager.THERMAL_STATUS_CRITICAL:
                    return "Kritik sıcaklık";

                case PowerManager.THERMAL_STATUS_EMERGENCY:
                    return "Acil termal durum";

                case PowerManager.THERMAL_STATUS_SHUTDOWN:
                    return "Kapanma seviyesi";

                default:
                    return "Bilinmiyor";
            }

        } catch (Exception e) {
            return "Termal bilgi alınamadı";
        }
    }
}
