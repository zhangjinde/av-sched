package net.airvantage.sched.conf;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import net.airvantage.sched.app.ServiceLocator;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration works with properties files. One is embedded with the runtime and contains the defaults. And one other
 * may be defined to override those defaults. It is reloadable (scanning content every 10 seconds) but all the
 * properties are not reload compliant.
 */
public class ConfigurationManager implements ConfigurationListener {

    private static final String CONF_DIR_PROPERTY = "AVSCHED_CONF_DIR";

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationManager.class);

    private TrackingConfiguration trackConf;
    private Configuration noTrackConf;

    private PropertiesConfiguration configLocal;

    public ConfigurationManager() {
        try {
            CompositeConfiguration cConf = new CompositeConfiguration();

            try {
                File localFile = new File(getConfDirLocation() + "/deploy-sched-local.properties");
                if (localFile.exists()) {
                    configLocal = new PropertiesConfiguration(localFile);
                    configLocal.addConfigurationListener(this);
                    FileChangedReloadingStrategy reload = new FileChangedReloadingStrategy();
                    reload.setConfiguration(configLocal);
                    reload.setRefreshDelay(1_000);
                    reload.init();
                    configLocal.setReloadingStrategy(reload);
                    cConf.addConfiguration(configLocal);
                } else {
                    LOG.warn("File 'deploy-sched-local.properties' not found in AVSCHED_CONF_DIR");
                }
            } catch (IllegalStateException e) {
                LOG.warn("No AVSCHED_CONF_DIR found, only taking default configuration");
            }

            PropertiesConfiguration configDefault = new PropertiesConfiguration(getClass().getResource(
                    "/deploy-sched.properties"));
            cConf.addConfiguration(configDefault);

            noTrackConf = cConf;
            trackConf = new TrackingConfiguration(cConf);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

        dumpConfig();

        new FailFastConfigurationChecker().failIfNotCorrect(this.get());
    }

    public void reload() {
        trackConf.getString("fake-key-used-to-trigger-reloading"); // triggers an internal reload
        new FailFastConfigurationChecker().failIfNotCorrect(this.get());
    }

    public Configuration get() {
        return get(true);
    }

    public String getAvSchedVersion() {
        return get().getString("av-sched.version");
    }

    // We can get a configuration without tracking just because when we want to display / dump content we should not
    // increment counters...
    public Configuration get(boolean withTracking) {
        if (withTracking) {
            return trackConf;
        } else {
            return noTrackConf;
        }
    }

    public Map<String, Integer> getRequestedKeys() {
        Map<String, Integer> ret = new HashMap<>();
        for (Entry<String, AtomicInteger> e : trackConf.getRequestedKeys().entrySet()) {
            ret.put(e.getKey(), e.getValue().get());
        }
        return ret;
    }

    // ---------- Reload mechanism ----------
    private boolean needsReload = false;
    private final List<ConfigurationUpdateListener> changeListeners = new ArrayList<>();

    public void registerReloadListener(ConfigurationUpdateListener listener) {
        changeListeners.add(listener);
        LOG.debug("Registered a ConfigReloadListener {}", listener);
    }

    @Override
    public void configurationChanged(ConfigurationEvent event) {
        needsReload = true;
        LOG.debug("Configuration reloaded, notification should happen soon.");
    }

    private synchronized void notifyReload() {
        if (needsReload) {
            LOG.info("Notifying all ConfigReloadListener of a reload");
            dumpConfig();

            for (ConfigurationUpdateListener listener : changeListeners) {
                listener.configurationUpdated();
            }
            needsReload = false;
        }
    }

    @DisallowConcurrentExecution
    public static class ConfigReloadChecker implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            ServiceLocator.getInstance().getConfigManager().reload();
        }
    }

    @DisallowConcurrentExecution
    public static class ConfigReloadNotifier implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            ServiceLocator.getInstance().getConfigManager().notifyReload();
        }
    }

    // ------------------------------------------------------
    /**
     * Return the application configuration location according to value of
     * {@link PropertiesProviderServiceImpl#CONF_DIR_PROPERTY}
     */
    private String getConfDirLocation() {

        String confDir = System.getProperty(CONF_DIR_PROPERTY);
        if (confDir == null) {
            confDir = System.getenv(CONF_DIR_PROPERTY);
        }

        // The configuration dir location is mandatory
        if (confDir == null) {
            throw new IllegalStateException("No configuration location property found, set '" + CONF_DIR_PROPERTY
                    + "' system property.");
        }

        // The configuration dir location has to exist
        File dirFile = new File(confDir);
        if (!dirFile.isDirectory()) {
            throw new IllegalStateException("Configuration location doesn't exist : " + confDir);
        }

        LOG.info("Load configuration from location : {}", confDir);

        return confDir;
    }

    private void dumpConfig() {
        Iterator<String> keys = noTrackConf.getKeys();
        while (keys.hasNext()) {
            String k = keys.next();
            LOG.info("Config dump > " + k + "=" + noTrackConf.getProperty(k));
        }

    }
}
