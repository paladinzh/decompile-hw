package cn.com.xy.sms.sdk.db.entity;

/* compiled from: Unknown */
public class IccidInfo {
    public String areaCode;
    public String city = "";
    public String cnum;
    public int deft;
    public String iccid;
    public int id;
    public int isPost;
    public long netUpdateTime;
    public String num;
    public String operator = "";
    public String provinces;
    public int simIndex;
    public long updateTime;
    public String userAreacode;
    public String userOperator;
    public String userProvinces;

    public String toString() {
        return "IccidInfo [id=" + this.id + ", iccid=" + this.iccid + ", city=" + this.city + ", provinces=" + this.provinces + ", cnum=" + this.cnum + ", num=" + this.num + ", isPost=" + this.isPost + ", netUpdateTime=" + this.netUpdateTime + ", areaCode=" + this.areaCode + ", operator=" + this.operator + ", updateTime=" + this.updateTime + ", deft=" + this.deft + ", userProvinces=" + this.userProvinces + ", userAreacode=" + this.userAreacode + ", userOperator=" + this.userOperator + ", simIndex=" + this.simIndex + "]";
    }
}
