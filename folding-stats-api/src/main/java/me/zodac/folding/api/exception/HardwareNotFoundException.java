package me.zodac.folding.api.exception;

public class HardwareNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 4515679320571283490L;

    public HardwareNotFoundException() {
        super();
    }

    public HardwareNotFoundException(final String message) {
        super(message);
    }

    public HardwareNotFoundException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
