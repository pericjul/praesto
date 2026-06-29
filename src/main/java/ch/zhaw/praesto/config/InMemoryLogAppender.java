package ch.zhaw.praesto.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.AppenderBase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Hält die letzten Log-Zeilen im Speicher (Ring-Puffer), damit der Super-Admin
 * sie direkt in der App ansehen kann (ohne Azure Log stream). Registriert in
 * logback-spring.xml.
 */
public class InMemoryLogAppender extends AppenderBase<ILoggingEvent> {

    private static final ConcurrentLinkedDeque<String> BUFFER = new ConcurrentLinkedDeque<>();
    private static final int MAX = 1000;

    @Override
    protected void append(ILoggingEvent e) {
        StringBuilder sb = new StringBuilder()
                .append(Instant.ofEpochMilli(e.getTimeStamp()))
                .append(' ').append(e.getLevel())
                .append(' ').append(shortLogger(e.getLoggerName()))
                .append(" - ").append(e.getFormattedMessage());
        IThrowableProxy t = e.getThrowableProxy();
        if (t != null) {
            sb.append(" | ").append(t.getClassName());
            if (t.getMessage() != null) sb.append(": ").append(t.getMessage());
        }
        BUFFER.addLast(sb.toString());
        while (BUFFER.size() > MAX) BUFFER.pollFirst();
    }

    private static String shortLogger(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return i >= 0 ? name.substring(i + 1) : name;
    }

    /** Neueste zuerst. */
    public static List<String> recent() {
        List<String> list = new ArrayList<>(BUFFER);
        java.util.Collections.reverse(list);
        return list;
    }
}
