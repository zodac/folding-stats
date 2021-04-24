package me.zodac.folding.api.exception;

public class HardwareNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 4515679320571283490L;

    public HardwareNotFoundException(final int id) {
        super("hardware", id);
    }
}
