package org.cryse.novelreader.util.store;

import org.cryse.novelreader.util.RunTimeStore;

import java.util.Hashtable;

public enum HashTableRunTimeStore implements RunTimeStore {
    INSTANCE;

    private Hashtable<String,Object> hashTable = new Hashtable<String,Object>();

    public static HashTableRunTimeStore getInstance() {
        return INSTANCE;
    }

    @Override
    public void put(String key, Object obj) {
        hashTable.put(key,obj);
    }

    @Override
    public Object get(String key) {
        return hashTable.get(key);
    }

    @Override
    public void remove(String key) {
        hashTable.remove(key);
    }

    @Override
    public void clear() {
        hashTable.clear();
    }

    @Override
    public boolean containsKey(String key) {
        return hashTable.containsKey(key);
    }

    @Override
    public int size() {
        return hashTable.size();
    }
}
