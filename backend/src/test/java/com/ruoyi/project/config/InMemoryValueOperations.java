package com.ruoyi.project.config;

import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 测试用内存版 {@link ValueOperations} 实现。
 *
 * <p>用 {@link ConcurrentHashMap} 模拟 Redis 字符串读写的真实状态，使积分（LuaScriptExecutor
 * 以 Redis 字符串为唯一真实来源）在测试中可正确累加：addPoints 写入后 deductPoints 能读到。</p>
 *
 * <p><b>为什么不用 Mockito mock 替代：</b> raw-type 的 {@code ValueOperations} mock 在 stub
 * {@code get} 时会触发 Mockito 泛型桥方法的错误强转
 * （{@code java.lang.String cannot be cast to [C}），导致运行期 {@link ClassCastException}。
 * 直接提供一个有状态的具体实现可彻底规避该问题。</p>
 */
@SuppressWarnings("unchecked")
class InMemoryValueOperations<K, V> implements ValueOperations<K, V> {

    private final Map<String, Object> store = new ConcurrentHashMap<>();

    private String key(Object k) {
        return String.valueOf(k);
    }

    @Override
    public V get(Object key) {
        return (V) store.get(key(key));
    }

    @Override
    public List<V> multiGet(Collection<K> keys) {
        if (keys == null) {
            return null;
        }
        List<V> result = new ArrayList<>(keys.size());
        for (K k : keys) {
            result.add((V) store.get(key(k)));
        }
        return result;
    }

    @Override
    public void set(K key, V value) {
        store.put(key(key), value);
    }

    @Override
    public void set(K key, V value, long timeout, TimeUnit unit) {
        // 测试不关心过期时间，仅保留值
        store.put(key(key), value);
    }

    @Override
    public void set(K key, V value, long timeout) {
        store.put(key(key), value);
    }

    @Override
    public void set(K key, V value, Duration timeout) {
        store.put(key(key), value);
    }

    @Override
    public Boolean setIfAbsent(K key, V value) {
        return store.putIfAbsent(key(key), value) == null;
    }

    @Override
    public Boolean setIfAbsent(K key, V value, long timeout, TimeUnit unit) {
        return store.putIfAbsent(key(key), value) == null;
    }

    @Override
    public Boolean setIfAbsent(K key, V value, Duration timeout) {
        return store.putIfAbsent(key(key), value) == null;
    }

    @Override
    public Boolean setIfPresent(K key, V value) {
        if (store.containsKey(key(key))) {
            store.put(key(key), value);
            return true;
        }
        return false;
    }

    @Override
    public Boolean setIfPresent(K key, V value, long timeout, TimeUnit unit) {
        return setIfPresent(key, value);
    }

    @Override
    public Boolean setIfPresent(K key, V value, Duration timeout) {
        return setIfPresent(key, value);
    }

    @Override
    public void multiSet(Map<? extends K, ? extends V> map) {
        if (map == null) {
            return;
        }
        map.forEach((kk, vv) -> store.put(key(kk), vv));
    }

    @Override
    public Boolean multiSetIfAbsent(Map<? extends K, ? extends V> map) {
        if (map == null) {
            return false;
        }
        boolean allAbsent = true;
        for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
            if (store.putIfAbsent(key(e.getKey()), e.getValue()) != null) {
                allAbsent = false;
            }
        }
        return allAbsent;
    }

    @Override
    public V getAndSet(K key, V value) {
        return (V) store.put(key(key), value);
    }

    @Override
    public V getAndDelete(K key) {
        return (V) store.remove(key(key));
    }

    @Override
    public V getAndExpire(K key, long timeout, TimeUnit unit) {
        return (V) store.get(key(key));
    }

    @Override
    public V getAndExpire(K key, Duration timeout) {
        return (V) store.get(key(key));
    }

    @Override
    public V getAndPersist(K key) {
        return (V) store.get(key(key));
    }

    @Override
    public Long increment(K key) {
        return increment(key, 1L);
    }

    @Override
    public Long increment(K key, long delta) {
        Object cur = store.get(key(key));
        long base = (cur == null) ? 0L : Long.parseLong(String.valueOf(cur));
        long next = base + delta;
        store.put(key(key), next);
        return next;
    }

    @Override
    public Double increment(K key, double delta) {
        Object cur = store.get(key(key));
        double base = (cur == null) ? 0d : Double.parseDouble(String.valueOf(cur));
        double next = base + delta;
        store.put(key(key), next);
        return next;
    }

    @Override
    public Long decrement(K key) {
        return decrement(key, 1L);
    }

    @Override
    public Long decrement(K key, long delta) {
        Object cur = store.get(key(key));
        long base = (cur == null) ? 0L : Long.parseLong(String.valueOf(cur));
        long next = base - delta;
        store.put(key(key), next);
        return next;
    }

    @Override
    public Integer append(K key, String value) {
        Object cur = store.get(key(key));
        String base = (cur == null) ? "" : String.valueOf(cur);
        String next = base + value;
        store.put(key(key), next);
        return next.length();
    }

    @Override
    public String get(K key, long start, long end) {
        Object cur = store.get(key(key));
        if (cur == null) {
            return null;
        }
        return String.valueOf(cur);
    }

    @Override
    public Long size(K key) {
        Object cur = store.get(key(key));
        return (cur == null) ? 0L : (long) String.valueOf(cur).length();
    }

    @Override
    public Boolean setBit(K key, long offset, boolean value) {
        return false;
    }

    @Override
    public Boolean getBit(K key, long offset) {
        return false;
    }

    @Override
    public List<Long> bitField(K key, BitFieldSubCommands subCommands) {
        return null;
    }

    @Override
    public RedisOperations<K, V> getOperations() {
        return null;
    }
}
