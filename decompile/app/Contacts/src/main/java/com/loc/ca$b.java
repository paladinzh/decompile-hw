package com.loc;

import java.util.Hashtable;

/* compiled from: Adjacent */
public class ca$b {
    protected static final Hashtable<String, Hashtable<String, String>> a = new Hashtable();

    static {
        Hashtable hashtable = new Hashtable();
        hashtable.put("even", "bc01fg45238967deuvhjyznpkmstqrwx");
        hashtable.put("odd", "p0r21436x8zb9dcf5h7kjnmqesgutwvy");
        Hashtable hashtable2 = new Hashtable();
        hashtable2.put("even", "238967debc01fg45kmstqrwxuvhjyznp");
        hashtable2.put("odd", "14365h7k9dcfesgujnmqp0r2twvyx8zb");
        Hashtable hashtable3 = new Hashtable();
        hashtable3.put("even", "p0r21436x8zb9dcf5h7kjnmqesgutwvy");
        hashtable3.put("odd", "bc01fg45238967deuvhjyznpkmstqrwx");
        Hashtable hashtable4 = new Hashtable();
        hashtable4.put("even", "14365h7k9dcfesgujnmqp0r2twvyx8zb");
        hashtable4.put("odd", "238967debc01fg45kmstqrwxuvhjyznp");
        a.put("top", hashtable);
        a.put("btm", hashtable2);
        a.put("right", hashtable3);
        a.put("left", hashtable4);
    }
}
