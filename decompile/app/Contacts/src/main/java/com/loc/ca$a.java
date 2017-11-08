package com.loc;

import java.util.Hashtable;

/* compiled from: Adjacent */
public class ca$a {
    protected static final Hashtable<String, Hashtable<String, String>> a = new Hashtable();

    static {
        Hashtable hashtable = new Hashtable();
        hashtable.put("even", "bcfguvyz");
        hashtable.put("odd", "prxz");
        Hashtable hashtable2 = new Hashtable();
        hashtable2.put("even", "0145hjnp");
        hashtable2.put("odd", "028b");
        Hashtable hashtable3 = new Hashtable();
        hashtable3.put("even", "prxz");
        hashtable3.put("odd", "bcfguvyz");
        Hashtable hashtable4 = new Hashtable();
        hashtable4.put("even", "028b");
        hashtable4.put("odd", "0145hjnp");
        a.put("top", hashtable);
        a.put("btm", hashtable2);
        a.put("right", hashtable3);
        a.put("left", hashtable4);
    }
}
