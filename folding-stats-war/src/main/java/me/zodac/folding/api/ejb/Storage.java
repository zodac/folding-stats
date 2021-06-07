package me.zodac.folding.api.ejb;


/**
 * In order to decouple both the REST layer and the {@link BusinessLogic} from the persistence solution we use this
 * interface - to be implemented as an EJB - as a single point of entry for CRUD operations.
 * Since some of the persisted data can be cached, we don't want any other modules of the
 * codebase to need to worry about DB vs cache access, and instead encapsulate all of that logic here.
 */
public interface Storage {

}
