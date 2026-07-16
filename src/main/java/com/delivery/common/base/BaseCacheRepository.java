package com.delivery.common.base;

/**
 * 캐시 인터페이스 구현체 생성하여 사용
 *
 * @param <K>
 * @param <V>
 */
public interface BaseCacheRepository<K, V> {
    void save(K key, V value);

    V findByKey(K key);

    void delete(K key);
}
