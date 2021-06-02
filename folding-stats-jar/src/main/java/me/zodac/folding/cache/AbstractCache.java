package me.zodac.folding.cache;

import me.zodac.folding.api.ResponsePojo;
import me.zodac.folding.api.tc.exception.NotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract class AbstractCache<V extends ResponsePojo> {

    private transient final Map<Integer, V> elementsById;

    protected AbstractCache() {
        elementsById = new ConcurrentHashMap<>();
    }

    protected abstract String elementType();

    /**
     * Add an element of type {@link V} to the cache.
     *
     * @param element the {@link V} element to add to the cache
     */
    public void add(final V element) {
        final int elementId = element.getId();

        if (elementId == 0) {
            throw new IllegalArgumentException(String.format("ID cannot be 0: %s", element));
        }
        elementsById.put(elementId, element);
    }

    public void addAll(final Collection<V> elements) {
        for (final V element : elements) {
            add(element);
        }
    }

    public void remove(final int elementId) {
        elementsById.remove(elementId);
    }

    public V get(final int id) throws NotFoundException {
        final V element = elementsById.get(id);
        if (element == null) {
            throw new NotFoundException(elementType(), id);
        }
        return element;
    }

    public V get(final String id) throws NotFoundException {
        return get(Integer.parseInt(id));
    }

    public V getOrNull(final int id) {
        try {
            return get(id);
        } catch (final NotFoundException e) {
            return null;
        }
    }

    public Collection<V> getAll() {
        return Collections.unmodifiableCollection(elementsById.values());
    }
}
