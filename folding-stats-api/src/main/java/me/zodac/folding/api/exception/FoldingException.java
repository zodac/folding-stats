package me.zodac.folding.api.exception;

// TODO: [zodac] Simple application exception between BL and REST layer
//   Split this into more explicit exceptions
public class FoldingException extends Exception {

    private static final long serialVersionUID = 2084075118178238910L;

    public FoldingException(final String message) {
        super(message);
    }

    public FoldingException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
