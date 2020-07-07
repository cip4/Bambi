package org.cip4.bambi.server;

/**
 * Applications runtime properties.
 */
public class RuntimeProperties {
    static String productVersion = "no-version";
    static String productBuildTimestamp = "no-build-timestamp";
    static String productBuildNumber = "no-build-number";

    /**
     * Returns the application's version number.
     *
     * @return The application's version number.
     */
    public static String getProductVersion() {
        return productVersion;
    }

    /**
     * Set the application's version number.
     *
     * @param productVersion The applications version number.
     */
    public static void setProductVersion(final String productVersion) {
        RuntimeProperties.productVersion = productVersion;
    }

    /**
     * Returns the application's build timestamp.
     *
     * @return The build timestamp as formatted String.
     */
    public static String getProductBuildTimestamp() {
        return productBuildTimestamp;
    }

    /**
     * Set the formatted application's build timestamp.
     *
     * @param productBuildTimestamp The formatted application's build timestamp.
     */
    public static void setProductBuildTimestamp(final String productBuildTimestamp) {
        RuntimeProperties.productBuildTimestamp = productBuildTimestamp;
    }

    /**
     * Returns the application's build number.
     *
     * @return The build number as String.
     */
    public static String getProductBuildNumber() {
        return productBuildNumber;
    }

    /**
     * Set the application's build number.
     *
     * @param productBuildNumber The application's build number.
     */
    public static void setProductBuildNumber(final String productBuildNumber) {
        RuntimeProperties.productBuildNumber = productBuildNumber;
    }
}
