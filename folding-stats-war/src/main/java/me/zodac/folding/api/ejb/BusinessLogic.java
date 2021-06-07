package me.zodac.folding.api.ejb;


/**
 * In order to decouple the REST layer from any business requirements, we move that logic into this interface, to be
 * implemented as an EJB.
 * <p>
 * This should simplify the REST layer to simply validate incoming requests and forward to here.
 */
public interface BusinessLogic {

}
