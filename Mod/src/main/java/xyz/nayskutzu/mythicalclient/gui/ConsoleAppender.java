package xyz.nayskutzu.mythicalclient.gui;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.reflect.Method;

public class ConsoleAppender extends AbstractAppender {

    private static final SimpleDateFormat TIME = new SimpleDateFormat("HH:mm:ss");

    protected ConsoleAppender(String name) {
        super(name, null, null, true);
    }

    public static ConsoleAppender create(String name) {
        return new ConsoleAppender(name);
    }

    @Override
    public void append(LogEvent event) {
        try {
            String ts = TIME.format(new Date(getEventTime(event)));
            String line = String.format("%s [%s/%s] %s - %s%n",
                    ts,
                    event.getThreadName(),
                    event.getLevel().name(),
                    event.getLoggerName(),
                    event.getMessage() != null ? event.getMessage().getFormattedMessage() : "");
            xyz.nayskutzu.mythicalclient.gui.ConsoleLogBuffer.getInstance().append(line);
        } catch (Exception ignored) {
        }
    }

    private long getEventTime(LogEvent event) {
        try {
            // Try getTimeMillis (older/newer variants)
            Method m = event.getClass().getMethod("getTimeMillis");
            Object v = m.invoke(event);
            if (v instanceof Long) return (Long) v;
        } catch (Throwable ignored) {}
        try {
            // Try getMillis (some versions expose this)
            Method m = event.getClass().getMethod("getMillis");
            Object v = m.invoke(event);
            if (v instanceof Long) return (Long) v;
        } catch (Throwable ignored) {}
        try {
            // Try getInstant (Log4j 2.11+)
            Method m = event.getClass().getMethod("getInstant");
            Object instant = m.invoke(event);
            if (instant != null) {
                Method epochMilli = instant.getClass().getMethod("getEpochMillisecond");
                Object v = epochMilli.invoke(instant);
                if (v instanceof Long) return (Long) v;
            }
        } catch (Throwable ignored) {}
        return System.currentTimeMillis();
    }
} 