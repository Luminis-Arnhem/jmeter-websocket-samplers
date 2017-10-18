/**
 * Taken from blog by Thomas Klambauer:
 * https://www.dynatrace.com/blog/how-stable-are-your-unit-tests-best-practices-to-raise-test-automation-quality/
 */
package eu.luminis.jmeter.wssampler;

/**
 * A helper to switch a system property value and restore the previous one.
 * When used in try-with-resources, restores the values automatically.
 */
class ScopedProperty implements AutoCloseable {

    private final String key;
    private final String oldValue;

    /**
     *
     * @param key The System.setProperty key
     * @param value The System.setProperty value to switch to.
     */
    public ScopedProperty(final String key, final String value) {
        this.key = key;
        oldValue = System.setProperty(key, value);
    }

    @Override
    public void close() {
        // Can't use setProperty(key, null) -> Throws NullPointerException.
        if( oldValue == null ) {
            // Previously there was no entry.
            System.clearProperty(key);
        } else {
            System.setProperty(key, oldValue);
        }
    }
}
