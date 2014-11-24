package com.lakeside.config;


import com.lakeside.core.utils.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * configuration implementation
 *
 * @author dejun
 */
public class Configuration {

    protected Map<String, String> _map = new HashMap<>();

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public Configuration() {
    }

    public Configuration(Configuration conf) {
        this.putAll(conf);
    }

    /**
     * get all the config with a given prefix.
     * For example, if prefix ="logger.level", return a _map like this:
     * <p/>
     *
     * @param prefix
     * @return
     */
    public Map<String, String> getConfigMapByPrefix(String prefix) {
        HashMap<String, String> result = new HashMap<>();
        for (String key : _map.keySet()) {
            if (key.startsWith(prefix)) {
                result.put(key, _map.get(key));
            }
        }
        return result;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = this.get(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public Long getLong(String key) {
        String value = this.get(key);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return Long.valueOf(value);
    }

    public Integer getInt(String key, int defaultValue) {
        String value = this.get(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        return Integer.valueOf(value);
    }

    public double getDouble(String name, double defaultValue) {
        String valueString = get(name);
        if (StringUtils.isEmpty(valueString)) {
            return defaultValue;
        }
        return Double.parseDouble(valueString);
    }

    public float getFloat(String key) {
        return getFloat(key, 0);
    }

    public float getFloat(String key, float defaultValue) {
        String valueString = get(key);
        if (StringUtils.isEmpty(valueString)) {
            return defaultValue;
        }
        return Float.parseFloat(valueString);
    }

    public BigDecimal getBigDecimal(String key) {
        String value = this.get(key);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return new BigDecimal(value);
    }

    public Date getDate(String key) {
        Date date = getDate(key, "yyyy-MM-dd HH:mm:ss", null);
        if (date == null) {
            String valueString = get(key);
            if (StringUtils.isEmpty(valueString)) {
                return null;
            }
            date = new Date(Date.parse(valueString));
        }
        return date;
    }

    public Date getDate(String key, String dateFormat) throws ParseException {
        return getDate(key, dateFormat, new Date());
    }

    public Date getDate(String key, String dateFormat, Date defaultValue) {
        String valueString = get(key);
        if (StringUtils.isEmpty(valueString)) {
            return defaultValue;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        try {
            return sdf.parse(valueString);
        } catch (ParseException e) {
            return defaultValue;
        }
    }

    public String get(String key, String defaultVal) {
        String val = this.get(key);
        if (StringUtils.isEmpty(val)) {
            return defaultVal;
        }
        return val;
    }

    public String get(String key) {
        if (key == null) {
            return null;
        } else {
            key = key.trim();
        }
        String value = _map.get(key);
        if (value != null) {
            return value.trim();
        } else {
            return null;
        }
    }

    public void put(String key, String value) {
        this._map.put(key, value);
    }

    private void putAll(Configuration conf) {
        this._map.putAll(conf._map);
    }

    public int size() {
        return this._map.size();
    }

    public void clear() {
        _map.clear();
    }

    /**
     * combine with anther configuration( exclude the keys which have been exists in current configuration)
     * <p/>
     * this will combine the key that doesn't exists in current configuration object
     *
     * @param conf
     */
    public Configuration combineWith(Configuration conf) {
        Iterator<java.util.Map.Entry<String, String>> iterator = conf._map.entrySet().iterator();
        while (iterator.hasNext()) {
            java.util.Map.Entry<String, String> next = iterator.next();
            String key = next.getKey();
            String value = next.getValue();
            if (!_map.containsKey(key)) {
                _map.put(key, value);
            }
        }
        return this;
    }
}