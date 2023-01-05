package me.thealgorithm476.osfetch;

import java.util.Arrays;

/**
 * List of all Operating Systems OsFetch might return
 * @since 2.0.0
 */
public enum OperatingSystem {
    WINDOWS("Windows", "Windows NT", "WinNT", "Win32"),
    MAC_OS("macOS", "OS X", "Mac OS X", "Mac OS"),
    LINUX("Linux", "GNU/Linux"),
    OTHER("other");

    private final String[] NAMES;

    OperatingSystem(String... names) { this.NAMES = names; }

    /**
     * Returns a list with commonly used names for this Operating System.
     * <p>
     * This method will return AT LEAST 1 name, and index 0 will be the most-used name.
     * @return A list with commonly used names for this OS.
     * @since 2.0.0
     */
    public String[] getNames() { return this.NAMES; }

    /**
     * Returns the Operating System with given name.
     * @param name The name of the OS; Can be the official name, or a well-known one
     * @return The found OS; {@link #OTHER} when none was found
     * @since 2.0.0
     */
    public static OperatingSystem forName(String name) {
        for (OperatingSystem os : OperatingSystem.values()) if (Arrays.asList(os.getNames()).contains(name)) return os;
        return OTHER;
    }
}