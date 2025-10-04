package xyz.nayskutzu.mythicalclient.data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class MemoryStorageDriveData {
    private final Map<String, Object> dataMap = new ConcurrentHashMap<>();
    
    public void put(String key, Object value) {
        if (key == null) return;
        dataMap.put(key, value);
    }
    
    public Object get(String key) {
        return dataMap.get(key);
    }
    
    public boolean contains(String key) {
        return dataMap.containsKey(key);
    }
    
    public void remove(String key) {
        dataMap.remove(key);
    }
    
    public void clear() {
        dataMap.clear();
    }
    
    public int size() {
        return dataMap.size();
    }
}