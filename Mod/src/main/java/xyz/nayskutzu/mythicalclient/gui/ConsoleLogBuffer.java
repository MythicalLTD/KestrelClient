package xyz.nayskutzu.mythicalclient.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConsoleLogBuffer {
    private static final int DEFAULT_CAPACITY = 5000;
    private static final ConsoleLogBuffer INSTANCE = new ConsoleLogBuffer(DEFAULT_CAPACITY);

    private final List<String> buffer;
    private final int capacity;

    private ConsoleLogBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new ArrayList<>(capacity);
    }

    public static ConsoleLogBuffer getInstance() {
        return INSTANCE;
    }

    public synchronized void append(String line) {
        if (line == null) return;
        if (buffer.size() >= capacity) {
            buffer.remove(0);
        }
        buffer.add(line);
    }

    public synchronized List<String> snapshot() {
        return new ArrayList<>(buffer);
    }

    public synchronized int size() {
        return buffer.size();
    }

    public synchronized List<String> tail(int maxLines) {
        if (maxLines <= 0) return Collections.emptyList();
        int start = Math.max(0, buffer.size() - maxLines);
        return new ArrayList<>(buffer.subList(start, buffer.size()));
    }

    public synchronized void clear() {
        buffer.clear();
    }
} 