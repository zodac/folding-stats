package me.zodac.folding.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A base {@link Class} to be extended and used as a cache for any element with an {@link Integer} available to key it. Defines useful functions and
 * instantiates a {@link ConcurrentHashMap} to be used by all implementations.
 *
 * @param <V> the type of the element to be cached
 */
public class BaseCache<V> {

    private final Map<Integer, V> elementsById;

    /**
     * Instantiates the {@link BaseCache} with a {@link ConcurrentHashMap}.
     */
    protected BaseCache() {
        elementsById = new ConcurrentHashMap<>();
    }

    /**
     * Add an element to the cache.
     *
     * @param elementId the ID to key the element in the cache
     * @param element   the element to add to the cache
     * @throws IllegalArgumentException thrown if the input element is <code>null</code> or has an ID of <b>0</b> or
     *                                  lower
     */
    public void add(final int elementId, final V element) {
        if (element == null) {
            throw new IllegalArgumentException("Input element cannot be null");
        }

        if (elementId <= 0) {
            throw new IllegalArgumentException(String.format("ID must be greater than 0: %s", elementId));
        }
        elementsById.put(elementId, element);
    }

    /**
     * Retrieves an element from the cache.
     *
     * @param elementId the ID of the element to retrieve
     * @return an {@link Optional} of the element
     */
    public Optional<V> get(final int elementId) {
        if (elementsById.containsKey(elementId)) {
            return Optional.of(elementsById.get(elementId));
        }

        return Optional.empty();
    }

    /**
     * Retrieves all elements from the cache.
     *
     * @return an unmodifiable {@link Collection} of all elements
     */
    public Collection<V> getAll() {
        return Collections.unmodifiableCollection(elementsById.values());
    }

    /**
     * Retrieves all elements from the cache as a {@link Map}.
     *
     * @return an unmodifiable {@link Map} of all elements
     */
    public Map<Integer, V> getCacheContents() {
        return Collections.unmodifiableMap(elementsById);
    }

    /**
     * Removes an element from the cache.
     *
     * @param elementId the ID of the element to remove
     */
    public void remove(final int elementId) {
        elementsById.remove(elementId);
    }

    /**
     * Removes all elements from the cache.
     */
    public void removeAll() {
        elementsById.clear();
    }
}
