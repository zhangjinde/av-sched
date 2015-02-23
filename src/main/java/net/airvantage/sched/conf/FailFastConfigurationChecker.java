package net.airvantage.sched.conf;

import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * Checks the content of the application configuration and exists if some mandatory props are missing.
 */
public class FailFastConfigurationChecker {

    private static final Logger LOG = LoggerFactory.getLogger(TrackingConfiguration.class);

    private static final String[] MANDATORY_KEYS = new String[] { 
        Keys.SECRET,
        Keys.Db.SERVER,
        Keys.Db.DB_NAME,
        Keys.Db.PORT,
        Keys.Db.USER,
    };

    public synchronized void failIfNotCorrect(Configuration configuration) {

        Set<String> missing = Sets.newHashSet();
        for (String mandatoryKey : MANDATORY_KEYS) {
            if (!configuration.containsKey(mandatoryKey)) {
                missing.add(mandatoryKey);
            }
        }

        if (missing.size() > 0) {
            LOG.error("FATAL: Missing mandatory configuration keys '{}'", missing);
            LOG.error("FATAL: Exiting");
            System.exit(1);
        }

    }
}
