package me.zodac.folding.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import me.zodac.folding.api.ResponsePojo;
import me.zodac.folding.api.exception.NotFoundException;

abstract class AbstractCache<V extends ResponsePojo> {

    private final transient Map<Integer, V> elementsById;

    protected AbstractCache() {
        elementsById = new ConcurrentHashMap<>();
    }

    protected abstract String elementType();

    /**
     * Add an element of type {@link V} to the cache.
     *
     * @param element the {@link V} element to add to the cache
     * @throws IllegalArgumentException thrown if the input element is <code>null</code> or has an ID of <b>0</b> or
     *                                  lower
     */
    public void add(final V element) {
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }

        final int elementId = element.getId();

        if (elementId <= 0) {
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

    public Optional<V> get(final int id) {
        if (elementsById.containsKey(id)) {
            return Optional.of(elementsById.get(id));
        }

        return Optional.empty();
    }

    @Deprecated
    public V getOrError(final int id) throws NotFoundException {
        final V element = elementsById.get(id);
        if (element == null) {
            throw new NotFoundException(elementType(), id);
        }
        return element;
    }

    public Collection<V> getAll() {
        return Collections.unmodifiableCollection(elementsById.values());
    }
}
