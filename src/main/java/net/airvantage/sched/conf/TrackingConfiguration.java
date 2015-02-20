package net.airvantage.sched.conf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration.Configuration;

/**
 * Tracks calls to the configuration object in order to have some insight which configuration keys have been requested.
 */
public class TrackingConfiguration implements Configuration {

    private final Configuration conf;
    private final Map<String, AtomicInteger> requestedKeys = new HashMap<>(100);

    public TrackingConfiguration(Configuration conf) {
        this.conf = conf;
    }

    protected Map<String, AtomicInteger> getRequestedKeys() {
        return requestedKeys;
    }

    @Override
    public Configuration subset(String prefix) {
        return conf.subset(prefix);
    }

    @Override
    public boolean isEmpty() {
        return conf.isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return conf.containsKey(key);
    }

    @Override
    public void addProperty(String key, Object value) {
        conf.addProperty(key, value);
    }

    @Override
    public void setProperty(String key, Object value) {
        conf.setProperty(key, value);
    }

    @Override
    public void clearProperty(String key) {
        conf.clearProperty(key);
    }

    @Override
    public void clear() {
        conf.clear();
    }

    @Override
    public Object getProperty(String key) {
        track(key);
        return conf.getProperty(key);
    }

    @Override
    public Iterator<String> getKeys(String prefix) {
        return conf.getKeys(prefix);
    }

    @Override
    public Iterator<String> getKeys() {
        return conf.getKeys();
    }

    @Override
    public Properties getProperties(String key) {
        track(key);
        return conf.getProperties(key);
    }

    @Override
    public boolean getBoolean(String key) {
        track(key);
        return conf.getBoolean(key);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        track(key);
        return conf.getBoolean(key, defaultValue);
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        track(key);
        return conf.getBoolean(key, defaultValue);
    }

    @Override
    public byte getByte(String key) {
        track(key);
        return conf.getByte(key);
    }

    @Override
    public byte getByte(String key, byte defaultValue) {
        track(key);
        return conf.getByte(key, defaultValue);
    }

    @Override
    public Byte getByte(String key, Byte defaultValue) {
        track(key);
        return conf.getByte(key, defaultValue);
    }

    @Override
    public double getDouble(String key) {
        track(key);
        return conf.getDouble(key);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        track(key);
        return conf.getDouble(key, defaultValue);
    }

    @Override
    public Double getDouble(String key, Double defaultValue) {
        track(key);
        return conf.getDouble(key, defaultValue);
    }

    @Override
    public float getFloat(String key) {
        track(key);
        return conf.getFloat(key);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        track(key);
        return conf.getFloat(key, defaultValue);
    }

    @Override
    public Float getFloat(String key, Float defaultValue) {
        track(key);
        return conf.getFloat(key, defaultValue);
    }

    @Override
    public int getInt(String key) {
        track(key);
        return conf.getInt(key);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        track(key);
        return conf.getInt(key, defaultValue);
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue) {
        track(key);
        return conf.getInteger(key, defaultValue);
    }

    @Override
    public long getLong(String key) {
        track(key);
        return conf.getLong(key);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        track(key);
        return conf.getLong(key, defaultValue);
    }

    @Override
    public Long getLong(String key, Long defaultValue) {
        track(key);
        return conf.getLong(key, defaultValue);
    }

    @Override
    public short getShort(String key) {
        track(key);
        return conf.getShort(key);
    }

    @Override
    public short getShort(String key, short defaultValue) {
        track(key);
        return conf.getShort(key, defaultValue);
    }

    @Override
    public Short getShort(String key, Short defaultValue) {
        track(key);
        return conf.getShort(key, defaultValue);
    }

    @Override
    public BigDecimal getBigDecimal(String key) {
        track(key);
        return conf.getBigDecimal(key);
    }

    @Override
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        track(key);
        return conf.getBigDecimal(key, defaultValue);
    }

    @Override
    public BigInteger getBigInteger(String key) {
        track(key);
        return conf.getBigInteger(key);
    }

    @Override
    public BigInteger getBigInteger(String key, BigInteger defaultValue) {
        track(key);
        return conf.getBigInteger(key, defaultValue);
    }

    @Override
    public String getString(String key) {
        track(key);
        return conf.getString(key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        track(key);
        return conf.getString(key, defaultValue);
    }

    @Override
    public String[] getStringArray(String key) {
        track(key);
        return conf.getStringArray(key);
    }

    @Override
    public List<Object> getList(String key) {
        track(key);
        return conf.getList(key);
    }

    @Override
    public List<Object> getList(String key, List<?> defaultValue) {
        track(key);
        return conf.getList(key, defaultValue);
    }

    private synchronized void track(String key) {
        AtomicInteger counter = requestedKeys.get(key);
        if (counter == null) {
            counter = new AtomicInteger();
            requestedKeys.put(key, counter);
        }
        counter.incrementAndGet();
    }

}
