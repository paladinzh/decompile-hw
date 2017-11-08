package cn.com.xy.sms.sdk.db.entity;

/* compiled from: Unknown */
public class MatchCache {
    public String bubble_result;
    public String card_result;
    public String extend;
    public long id;
    public String msg_id;
    public String msg_num_md5;
    public String phonenum;
    public String popup_window_result;
    public long save_time;
    public String scene_id;
    public String session_reuslt;

    public MatchCache(String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9) {
        this.msg_num_md5 = str;
        this.phonenum = str2;
        this.msg_id = str3;
        this.scene_id = str4;
        this.popup_window_result = str5;
        this.bubble_result = str6;
        this.session_reuslt = str7;
        this.card_result = str8;
        this.extend = str9;
    }
}
