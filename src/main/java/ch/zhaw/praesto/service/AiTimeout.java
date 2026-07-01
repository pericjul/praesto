package ch.zhaw.praesto.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Führt einen KI-Aufruf mit hartem Timeout aus, damit ein hängender Azure-OpenAI-
 * Request nicht den Nutzer-Request blockiert. Läuft das Timeout ab, wirft {@code get}
 * eine {@link java.util.concurrent.TimeoutException} – die aufrufende Stelle fängt sie
 * und liefert ihren Fallback-Text.
 */
public final class AiTimeout {

    private static final ExecutorService POOL = Executors.newFixedThreadPool(20, r -> {
        Thread t = new Thread(r, "ai-call");
        t.setDaemon(true);
        return t;
    });

    private AiTimeout() {
    }

    public static String call(Supplier<String> call, int seconds) throws Exception {
        return POOL.submit(call::get).get(seconds, TimeUnit.SECONDS);
    }
}
