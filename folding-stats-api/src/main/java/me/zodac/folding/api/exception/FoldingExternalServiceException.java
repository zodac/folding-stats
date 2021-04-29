package me.zodac.folding.api.exception;

// TODO: [zodac] Anything throwing a conflict should also contain links to the things using them, ideally
public class FoldingExternalServiceException extends Exception {

    private static final long serialVersionUID = 2084075114898438910L;

    public FoldingExternalServiceException(final String message) {
        super(message);
    }
}
