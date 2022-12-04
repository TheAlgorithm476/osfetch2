package me.thealgorithm476.osfetch;

/**
 * List of all CPU Architectures OsFetch might return.
 * <p>
 * If an unknown architecture is detected, it will be returned as {@link #OTHER}.
 * @since 2.0.0
 */
public enum Architecture {
    /**
     * 64-bit x86 architecture, officially knows as 'x86_64', but commonly referred to as 'x64' or 'amd64'.
     * @since 2.0.0
     */
    X86_64("x86_64", "x64", "amd64"),
    /**
     * 64-bit ARM architecture, officially known as 'aarch64', but commonly referred to as 'arm64'.
     * @since 2.0.0
     */
    AARCH64("aarch64", "arm64"),
    /**
     * Any other architecture that is not listed here.
     * @since 2.0.0
     */
    OTHER("other");

    private final String[] NAMES;

    Architecture(String... names) { this.NAMES = names; }

    /**
     * Returns the list with commonly used names for this architecture.
     * <p>
     * It is guaranteed that this method will return at least one name, and that index 0 will always be the official name.
     * @return The list with commonly used names for this architecture.
     * @since 2.0.0
     */
    public String[] getNames() { return this.NAMES; }
}