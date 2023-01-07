package me.thealgorithm476.osfetch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Properties;

public final class OsFetch {
    private static boolean populated;

    private static OperatingSystem operatingSystem;
    private static Architecture architecture;
    private static String kernelVersion;
    private static String osVersionNumber;
    private static String releaseName;
    private static String fullyConstructedReleaseName;

    private OsFetch() { throw new IllegalStateException("OsFetch should not be instantiated!"); }

    /**
     * Populates OsFetch with the OS Data is needs to parse the Operating System and Architecture this JVM is running on
     * @implNote This method may be called at any moment, but it is not mandatory. When requesting any data from OsFetch while not populated will auto-populate it.
     * <p>
     * Calling this method multiple times will make it return on any additional call.
     * @since 2.0.0
     */
    public static void populate() {
        if (populated) return;

        // Needed for fetching Linux Distro version later down the line
        Properties osRelease = new Properties();
        Properties lsbRelease = new Properties();

        // -------------------------------------------------
        // Step 0: Fetch initial data from System Properties
        // -------------------------------------------------
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");

        operatingSystem = OperatingSystem.forName(osName);
        architecture = Architecture.forName(osArch);

        if (operatingSystem == OperatingSystem.OTHER && osName.startsWith("Windows")) operatingSystem = OperatingSystem.WINDOWS; // Windows will return OTHER by default

        // ---------------------------------------------------------------
        // Step 1: Fetch /etc/os-release and/or /etc/lsb-release for Linux
        // ---------------------------------------------------------------
        if (operatingSystem == OperatingSystem.LINUX) {
            try {
                File osReleaseFile = new File("/etc/os-release");
                File lsbReleaseFile = new File("/etc/lsb-release");

                if (osReleaseFile.exists()) {
                    String[] content = Files.readAllLines(osReleaseFile.toPath()).toArray(new String[0]);

                    for (String line : content) {
                        String[] split = line.split("=");

                        if (split.length != 2) continue;

                        osRelease.put(split[0].trim(), split[1].trim().replace("\"", ""));
                    }
                }

                if (lsbReleaseFile.exists()) {
                    String[] content = Files.readAllLines(lsbReleaseFile.toPath()).toArray(new String[0]);

                    for (String line : content) {
                        String[] split = line.split("=");

                        if (split.length != 2) continue;

                        lsbRelease.put(split[0].trim(), split[1].trim().replace("\"", ""));
                    }
                }
            } catch (IOException ignored) {}
        }

        // ------------------------------------------------------------
        // Step 2: Assign the Kernel Versions for each Operating System
        // ------------------------------------------------------------
        switch (operatingSystem) {
            case WINDOWS -> kernelVersion = "NT " + osVersion;
            case LINUX -> kernelVersion = osVersion;
            case MAC_OS -> { // macOS requires a bit more work to determine the Darwin version
                try {
                    Process unameProcess = Runtime.getRuntime().exec("uname -r");
                    BufferedReader stdout = new BufferedReader(new InputStreamReader(unameProcess.getInputStream()));

                    String line;

                    while ((line = stdout.readLine()) != null) {
                        if (kernelVersion != null) continue;

                        kernelVersion = "Darwin " + line;
                    }

                    stdout.close();
                    unameProcess.waitFor();
                } catch (IOException | InterruptedException exception) {
                    kernelVersion = "Failed to determine Darwin version";
                }
            }
        }

        // ----------------------------------------------
        // Step 3: Assign Operating System Version number
        // ----------------------------------------------
        osVersionNumber = switch (operatingSystem) {
            case WINDOWS -> osName.replace("Windows ", "");
            case MAC_OS -> osVersion;
            case LINUX -> osRelease.getProperty("VERSION_ID", lsbRelease.getProperty("DISTRIB_RELEASE", "Failed to determine Release version"));
            case OTHER -> "Unknown";
        };

        // ------------------------------
        // Step 4: Determine Release name
        // ------------------------------
        switch (operatingSystem) {
            case WINDOWS -> releaseName = "Windows";
            case LINUX -> {
                String versionProperty = osRelease.getProperty("VERSION");

                if (versionProperty != null) {
                    releaseName = versionProperty.replace(versionProperty.substring(0, versionProperty.indexOf('(') + 1), "").replace(")", "");
                    break;
                }

                String prettyNameProperty = osRelease.getProperty("PRETTY_NAME");

                if (prettyNameProperty != null) {
                    releaseName = prettyNameProperty;
                    break;
                }

                String distribDescriptionProperty = lsbRelease  .getProperty("DISTRIB_DESCRIPTION");
                releaseName = distribDescriptionProperty == null ? "Failed to determine Release Name" : distribDescriptionProperty;
            }
            case MAC_OS -> { // Oh boy
                if (osVersionNumber.startsWith("10.")) {
                    releaseName = switch (osVersionNumber.substring(3, 5)) {
                        case "14" -> "Mojave";
                        case "15" -> "Catalina";
                        case "16" -> "Big Sur"; // Early versions of macOS Big Sur had 10.16 as their version number.
                        default -> "Pre-Mojave macOS";
                    };

                    break;
                }

                releaseName = switch (osVersionNumber.substring(0, 2)) {
                    case "11" -> "Big Sur";
                    case "12" -> "Monterey";
                    case "13" -> "Ventura";
                    default -> "Post-Ventura macOS";
                };
            }
        }

        // ----------------------------------------
        // Step 5: Construct the final release name
        // ----------------------------------------
        fullyConstructedReleaseName = switch (operatingSystem) {
            case WINDOWS -> "Microsoft " + osName;
            case MAC_OS -> "macOS " + osVersion + " \"" + releaseName + "\"";
            case LINUX -> osVersionNumber + " (" + releaseName + ")";
            case OTHER -> "Other OS";
        };

        populated = true;
    }

    /**
     * @return Whether or not the OsFetch-data has been populated, and the OS, Architecture, ... has been decided already.
     * @since 2.0.0
     */
    public static boolean isPopulated() { return populated; }

    /**
     * @return The Operating System this JVM runs on
     * @since 2.0.0
     */
    public static OperatingSystem getOperatingSystem() {
        populate();
        return operatingSystem;
    }

    /**
     * @return The CPU Architecture this JVM runs on
     * @since 2.0.0
     */
    public static Architecture getArchitecture() {
        populate();
        return architecture;
    }

    /**
     * @return The Operating System's Kernel Version. Will be the Linux kernel version on Linux, 'NT [version]' on Windows, and 'Darwin [version]' on macOS
     * @since 2.0.0
     */
    public static String getKernelVersion() {
        populate();
        return kernelVersion;
    }

    /**
     * @return The Operating System's Version Number. Will be '11' on Windows 11, '22.04' for Ubuntu 22.04, and '13.1' for macOS Ventura
     * @since 2.0.0
     */
    public static String getOsVersionNumber() {
        populate();
        return osVersionNumber;
    }

    /**
     * @return A release name. Many Linux distros have special release names, and macOS does too. Windows will return 'Windows'
     * @since 2.0.0
     */
    public static String getReleaseName() {
        populate();
        return releaseName;
    }

    /**
     * @return The full release name, including OS, OS Release, release version, .... Does not include kernel version, however
     * @since 2.0.0
     */
    public static String getFullyConstructedReleaseName() {
        populate();
        return fullyConstructedReleaseName;
    }
}