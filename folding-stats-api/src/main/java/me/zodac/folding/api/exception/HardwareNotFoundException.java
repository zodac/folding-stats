package me.zodac.folding.api.exception;

/**
 * Implementation of {@link NotFoundException} for {@link me.zodac.folding.api.tc.Hardware}.
 */
public class HardwareNotFoundException extends NotFoundException {

    private static final long serialVersionUID = -4455014154387328040L;

    /**
     * Create with a {@link me.zodac.folding.api.tc.Hardware} ID.
     *
     * @param id the {@link me.zodac.folding.api.tc.Hardware} ID
     */
    public HardwareNotFoundException(final int id) {
        super("hardware", id);
    }
}
