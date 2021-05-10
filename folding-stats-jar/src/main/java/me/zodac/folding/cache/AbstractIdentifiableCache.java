package me.zodac.folding.cache;

import me.zodac.folding.api.Identifiable;
import me.zodac.folding.api.tc.exception.NotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractIdentifiableCache<V extends Identifiable> {

    private final Map<Integer, V> elementsById;

    protected AbstractIdentifiableCache() {
        elementsById = new HashMap<>();
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

    public void addAll(final List<V> elements) {
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

    // This is used in the validators to ensure a user's hardware or a team's user exists.
    // However, if no #getAll() request has been made for hardware/users, the cache is not populated.
    // We get around this by hitting the cache in the Initialiser, so that should not be removed.
    public boolean doesNotContain(final int id) {
        return !elementsById.containsKey(id);
    }

    public List<V> getAll() {
        return List.copyOf(elementsById.values());
    }
}
