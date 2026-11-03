package com.pubgflash.gamebooster.monitor;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PingMonitor {

    public interface Callback {
        void onResult(String result);
    }

    private final ExecutorService executor =
        Executors.newSingleThreadExecutor();

    public void measure(Callback callback) {
        executor.execute(() -> {
            Socket socket = new Socket();

            try {
                long start = System.nanoTime();

                socket.connect(
                    new java.net.InetSocketAddress(
                        "1.1.1.1", 443
                    ),
                    3000
                );

                long elapsed =
                    (System.nanoTime() - start) / 1000000L;

                callback.onResult(elapsed + " ms");

            } catch (Exception e) {
                callback.onResult("Bağlantı başarısız");

            } finally {
                try {
                    socket.close();
                } catch (Exception ignored) {
                }
            }
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
