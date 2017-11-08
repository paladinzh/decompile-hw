package org.apache.commons.jexl2;

import java.util.HashMap;
import java.util.Map;

public class MapContext implements JexlContext {
    protected final Map<String, Object> map;

    public MapContext() {
        this(null);
    }

    public MapContext(Map<String, Object> vars) {
        if (vars == null) {
            vars = new HashMap();
        }
        this.map = vars;
    }

    public boolean has(String name) {
        return this.map.containsKey(name);
    }

    public Object get(String name) {
        return this.map.get(name);
    }

    public void set(String name, Object value) {
        this.map.put(name, value);
    }
}
