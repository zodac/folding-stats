package me.zodac.folding.api.exception;

public class HardwareNotFoundException extends NotFoundException {

    private static final long serialVersionUID = -4455014154387328040L;

    public HardwareNotFoundException(final int id) {
        super("hardware", id);
    }
}
