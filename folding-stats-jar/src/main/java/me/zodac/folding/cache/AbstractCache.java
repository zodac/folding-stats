package me.zodac.folding.cache;

import me.zodac.folding.api.ObjectWithId;
import me.zodac.folding.api.exception.NotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: [zodac] Caches are all singleton instances. Would be simpler to make them Singleton EJBs instead.
//   No need for Tx support, but looks a bit cleaner.
//   Check for any non-CDI use of cache, if none, make into EJBs.
abstract class AbstractCache<V extends ObjectWithId> {

    private final Map<String, V> elementsById;

    protected AbstractCache() {
        this.elementsById = new HashMap<>();
    }

    /**
     * Add an element of type {@link V} to the cache.
     *
     * @param element the {@link V} element to add to the cache
     */
    public void add(final V element) {
        final int elementId = element.getId();

        if (elementId == 0) {
            throw new IllegalArgumentException(String.format("ID cannot be 0: %s", element.toString()));
        }

        elementsById.put(String.valueOf(elementId), element);
    }

    public void addAll(final List<V> elements) {
        for (final V element : elements) {
            add(element);
        }
    }

    public V get(final String id) throws NotFoundException {
        final V element = elementsById.get(id);
        if (element == null) {
            throw new NotFoundException();
        }
        return element;
    }

    public V get(final int id) throws NotFoundException {
        return get(String.valueOf(id));
    }

    public List<V> getAll() {
        return List.copyOf(elementsById.values());
    }
}
