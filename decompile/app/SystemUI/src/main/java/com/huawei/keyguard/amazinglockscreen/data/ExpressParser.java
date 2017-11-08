package com.huawei.keyguard.amazinglockscreen.data;

import fyusion.vislib.BuildConfig;
import java.util.HashMap;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

public class ExpressParser {
    private static ExpressParser mInstance = null;
    private JexlContext mContext;
    private JexlEngine mEngine = new JexlEngine();
    private HashMap<String, Expression> mExpressions = new HashMap();
    private HashMap<String, Object> mSystemValues = new HashMap();

    public static void setmInstance(ExpressParser mInstance) {
        synchronized (ExpressParser.class) {
            mInstance = mInstance;
        }
    }

    public static ExpressParser getInstance() {
        ExpressParser expressParser;
        synchronized (ExpressParser.class) {
            if (mInstance == null) {
                mInstance = new ExpressParser();
            }
            expressParser = mInstance;
        }
        return expressParser;
    }

    public void clean() {
        cleanCache();
        setmInstance(null);
    }

    private void cleanCache() {
        this.mSystemValues.clear();
        this.mExpressions.clear();
    }

    private ExpressParser() {
        this.mSystemValues.put("carrier", BuildConfig.FLAVOR);
        this.mSystemValues.put("carrier2", BuildConfig.FLAVOR);
        this.mSystemValues.put("charge", new Charge(BuildConfig.FLAVOR, false, 0, BuildConfig.FLAVOR));
        this.mSystemValues.put("date", "00-00-00");
        this.mSystemValues.put("time", new Time("0", "0", "0", "0", BuildConfig.FLAVOR, false));
        this.mSystemValues.put("clockdesc_default", BuildConfig.FLAVOR);
        this.mSystemValues.put("clockdesc_roaming", BuildConfig.FLAVOR);
        this.mSystemValues.put("time_default", new Time("0", "0", "0", "0", BuildConfig.FLAVOR, false));
        this.mSystemValues.put("time_roaming", new Time("0", "0", "0", "0", BuildConfig.FLAVOR, false));
        this.mSystemValues.put("date_default", "00-00-00");
        this.mSystemValues.put("date_roaming", "00-00-00");
        this.mSystemValues.put("dualclock", Boolean.valueOf(false));
        this.mSystemValues.put("version", "current version 1.0");
        this.mSystemValues.put("point", new Position(0, 0));
        this.mSystemValues.put("press", Boolean.valueOf(false));
        this.mSystemValues.put("call", new Missed(BuildConfig.FLAVOR, 0));
        this.mSystemValues.put("email", new Missed(BuildConfig.FLAVOR, 0));
        this.mSystemValues.put("message", new Missed(BuildConfig.FLAVOR, 0));
        this.mSystemValues.put("ownerinfo", new OwnerInfo(BuildConfig.FLAVOR, false));
        this.mSystemValues.put("unlocktip", BuildConfig.FLAVOR);
        this.mSystemValues.put("move", new Move(0, 0));
        this.mSystemValues.put("start", Boolean.valueOf(false));
        this.mSystemValues.put("_state", Integer.valueOf(0));
        this.mSystemValues.put("music_visible", Integer.valueOf(0));
        this.mSystemValues.put("music_state", Integer.valueOf(0));
        this.mSystemValues.put("music_text", BuildConfig.FLAVOR);
        this.mSystemValues.put("music_prev", Integer.valueOf(0));
        this.mSystemValues.put("music_next", Integer.valueOf(0));
        this.mSystemValues.put("music_pause", Integer.valueOf(0));
        this.mSystemValues.put("music_play", Integer.valueOf(0));
        this.mSystemValues.put("time_value", Integer.valueOf(0));
        this.mSystemValues.put("week", Integer.valueOf(1));
        this.mContext = new MapContext();
        this.mContext.set("system", this.mSystemValues);
    }

    public void setSystemValue(String key, Object value) {
        this.mSystemValues.put(key, value);
    }

    public Object getSystemValue(String key) {
        return this.mSystemValues.get(key);
    }

    public Object parse(String exp) {
        Expression e = (Expression) this.mExpressions.get(exp);
        if (e == null) {
            e = this.mEngine.createExpression(exp);
            this.mExpressions.put(exp, e);
        }
        return e.evaluate(this.mContext);
    }
}
