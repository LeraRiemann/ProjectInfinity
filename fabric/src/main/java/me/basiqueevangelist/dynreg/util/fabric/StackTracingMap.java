package me.basiqueevangelist.dynreg.util.fabric;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class StackTracingMap<K, V> implements Map<K, V> {
    private final Map<K, V> inner;

    public StackTracingMap(Map<K, V> inner) {
        this.inner = inner;
    }


    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return inner.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return inner.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return inner.get(key);
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        System.out.println("put called!");
        new Exception().printStackTrace();

        return inner.put(key, value);
    }

    @Override
    public V remove(Object key) {
        System.out.println("remove called!");
        new Exception().printStackTrace();

        return inner.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        System.out.println("putAll called!");
        new Exception().printStackTrace();

        inner.putAll(m);
    }

    @Override
    public void clear() {
        System.out.println("clear called!");
        new Exception().printStackTrace();

        inner.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return inner.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return inner.values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return inner.entrySet();
    }
}
