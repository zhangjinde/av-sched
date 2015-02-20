package net.airvantage.sched.conf;

/**
 * Listener for configuration changes.
 */
public interface ConfigurationUpdateListener {

    /**
     * Called when the configuration has changed. May be called even if no configuration change but should be called
     * every time there is a change.
     */
    void configurationUpdated();

}
