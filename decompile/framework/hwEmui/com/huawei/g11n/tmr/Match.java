package com.huawei.g11n.tmr;

import android.util.HwSecureWaterMark;
import com.huawei.g11n.tmr.datetime.utils.DatePeriod;

public class Match implements Comparable<Object> {
    static final /* synthetic */ boolean $assertionsDisabled;
    int begin;
    DatePeriod dp;
    int end;
    boolean isTimePeriod = false;
    String regex;
    int type;

    static {
        boolean z = false;
        if (!Match.class.desiredAssertionStatus()) {
            z = true;
        }
        $assertionsDisabled = z;
    }

    public void setIsTimePeriod(boolean z) {
        this.isTimePeriod = z;
    }

    public boolean isTimePeriod() {
        if (this.isTimePeriod) {
            return this.isTimePeriod;
        }
        if (this.regex == null || this.regex.trim().isEmpty()) {
            return false;
        }
        int parseInt = Integer.parseInt(this.regex);
        if (parseInt > 49999 && parseInt < HwSecureWaterMark.MAX_NUMER) {
            return true;
        }
        return false;
    }

    public Match(int i, int i2, String str) {
        this.begin = i;
        this.end = i2;
        this.regex = str;
    }

    public String getRegex() {
        return this.regex;
    }

    public void setRegex(String str) {
        this.regex = str;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int i) {
        this.type = i;
    }

    public DatePeriod getDp() {
        return this.dp;
    }

    public void setDp(DatePeriod datePeriod) {
        this.dp = datePeriod;
    }

    public int getBegin() {
        return this.begin;
    }

    public void setBegin(int i) {
        this.begin = i;
    }

    public int getEnd() {
        return this.end;
    }

    public void setEnd(int i) {
        this.end = i;
    }

    public String toString() {
        return "[" + this.regex + "][" + this.begin + "-" + this.end + "]";
    }

    public int compareTo(Object obj) {
        int i = 0;
        if (!(obj instanceof Match)) {
            return 0;
        }
        Match match = (Match) obj;
        if (this.begin < match.begin) {
            i = -1;
        }
        if (this.begin > match.begin) {
            return 1;
        }
        return i;
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public int hashCode() {
        if ($assertionsDisabled) {
            return 42;
        }
        throw new AssertionError("hashCode not designed");
    }
}
