package tmsdk.common.module.numbermarker;

import tmsdkobf.cd;
import tmsdkobf.de;

/* compiled from: Unknown */
public class NumberMarkEntity {
    public static int CLIENT_LOGIC_BLACK_LIST = 1;
    public static int CLIENT_LOGIC_MAX = 103;
    public static int CLIENT_LOGIC_MIN = 0;
    public static int TAG_TYPE_CHEAT = 54;
    public static int TAG_TYPE_CORRECT_YELLOW = 10056;
    public static int TAG_TYPE_HOUSE_AGT = 51;
    public static int TAG_TYPE_INSURANCE = 52;
    public static int TAG_TYPE_MAX = 30056;
    public static int TAG_TYPE_NONE = 0;
    public static int TAG_TYPE_OTHER = 50;
    public static int TAG_TYPE_SALES = 53;
    public static int TAG_TYPE_SELF_TAG = 10055;
    public static int TEL_TYPE_MISS_CALL = 3;
    public static int TEL_TYPE_RING_ONE_SOUND = 1;
    public static int TEL_TYPE_USER_CANCEL = 2;
    public static int TEL_TYPE_USER_HANG_UP = 4;
    public static int USER_ACTION_IMPEACH = 11;
    public int calltime = 0;
    public int clientlogic = CLIENT_LOGIC_MIN;
    public int localTagType = 0;
    public String originName;
    public String phonenum = "";
    public int scene = 0;
    public int tagtype = 0;
    public int talktime = 0;
    public int teltype = de.ih.value();
    public String userDefineName;
    public int useraction = USER_ACTION_IMPEACH;

    public cd toTelReport() {
        cd cdVar = new cd();
        cdVar.ej = this.phonenum;
        cdVar.eV = this.useraction;
        cdVar.eW = this.teltype;
        cdVar.eX = this.talktime;
        cdVar.eY = this.calltime;
        cdVar.eZ = this.clientlogic;
        cdVar.tagType = this.tagtype;
        cdVar.userDefineName = this.userDefineName;
        cdVar.localTagType = this.localTagType;
        cdVar.originName = this.originName;
        cdVar.scene = this.scene;
        return cdVar;
    }
}
