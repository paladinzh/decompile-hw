package cn.com.xy.sms.sdk.Iservice;

import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class ResolveData {
    private int a;
    private String b;
    private String c;
    private List<a> d = new ArrayList();

    public int getActionType() {
        return this.a;
    }

    public List<a> getAttachItems() {
        return this.d;
    }

    public String getDirective() {
        return this.c;
    }

    public String getTargetChannel() {
        return this.b;
    }

    public void setActionType(int i) {
        this.a = i;
    }

    public void setAttachItems(List<a> list) {
        this.d = list;
    }

    public void setDirective(String str) {
        this.c = str;
    }

    public void setTargetChannel(String str) {
        this.b = str;
    }
}
