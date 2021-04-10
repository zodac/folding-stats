package me.zodac.folding.cache;

import me.zodac.folding.api.Identifiable;
import me.zodac.folding.api.exception.NotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: [zodac] Caches are all singleton instances. Would be simpler to make them Singleton EJBs instead.
//   No need for Tx support, but looks a bit cleaner.
//   Check for any non-CDI use of cache, if none, make into EJBs.
abstract class AbstractPojoCache<V extends Identifiable> {

    private final Map<String, V> elementsById;

    protected AbstractPojoCache() {
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

    public V getOrNull(final int id) {
        try {
            return get(String.valueOf(id));
        } catch (final NotFoundException e) {
            return null;
        }
    }

    public boolean contains(final String id) {
        return elementsById.containsKey(id);
    }

    public boolean contains(final int id) {
        return contains(String.valueOf(id));
    }

    public List<V> getAll() {
        return List.copyOf(elementsById.values());
    }
}
