package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class bt extends fs {
    static ArrayList<bu> dZ = new ArrayList();
    public float fScore = 0.0f;
    public String sRiskClassify = "";
    public String sRiskName = "";
    public String sRiskReach = "";
    public String sRiskUrl = "";
    public String sRule = "";
    public ArrayList<bu> stRuleTypeID = null;
    public int uiActionReason = 0;
    public int uiContentType = 0;
    public int uiFinalAction = 0;
    public int uiMatchCnt = 0;
    public int uiShowRiskName = 0;

    static {
        dZ.add(new bu());
    }

    public fs newInit() {
        return new bt();
    }

    public void readFrom(fq fqVar) {
        this.uiFinalAction = fqVar.a(this.uiFinalAction, 0, true);
        this.uiContentType = fqVar.a(this.uiContentType, 1, true);
        this.uiMatchCnt = fqVar.a(this.uiMatchCnt, 2, true);
        this.fScore = fqVar.a(this.fScore, 3, true);
        this.uiActionReason = fqVar.a(this.uiActionReason, 4, true);
        this.stRuleTypeID = (ArrayList) fqVar.b(dZ, 5, false);
        this.sRule = fqVar.a(6, false);
        this.uiShowRiskName = fqVar.a(this.uiShowRiskName, 7, false);
        this.sRiskClassify = fqVar.a(8, false);
        this.sRiskUrl = fqVar.a(9, false);
        this.sRiskName = fqVar.a(10, false);
        this.sRiskReach = fqVar.a(11, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.uiFinalAction, 0);
        frVar.write(this.uiContentType, 1);
        frVar.write(this.uiMatchCnt, 2);
        frVar.a(this.fScore, 3);
        frVar.write(this.uiActionReason, 4);
        if (this.stRuleTypeID != null) {
            frVar.a(this.stRuleTypeID, 5);
        }
        if (this.sRule != null) {
            frVar.a(this.sRule, 6);
        }
        if (this.uiShowRiskName != 0) {
            frVar.write(this.uiShowRiskName, 7);
        }
        if (this.sRiskClassify != null) {
            frVar.a(this.sRiskClassify, 8);
        }
        if (this.sRiskUrl != null) {
            frVar.a(this.sRiskUrl, 9);
        }
        if (this.sRiskName != null) {
            frVar.a(this.sRiskName, 10);
        }
        if (this.sRiskReach != null) {
            frVar.a(this.sRiskReach, 11);
        }
    }
}
