package com.huawei.openalliance.ad.utils.db.bean;

/* compiled from: Unknown */
public class MaterialRecord extends a {
    private int actionType_;
    private int adType_;
    private String clickMonitorUrl_;
    private int creativeType_ = 2;
    private int displayCount_;
    private String displayDate_;
    private String htmlStr_;
    private String impMonitorUrl_;
    private String intentUri_;
    private int isPriority_;
    private int isSplashPreContent_;
    private int isTest_;
    private String materialId_;
    private int maxDiaplayCount_;
    private String md5_;
    private String metaData_;
    private String paramFromServer_;
    private String sha256_;
    private String skipText_;
    private String slotId_;
    private long updateTime_;
    private long validTime_;

    public String a() {
        return this.md5_;
    }

    public void a(int i) {
        this.adType_ = i;
    }

    public void a(long j) {
        this.validTime_ = j;
    }

    public void a(String str) {
        this.md5_ = str;
    }

    public String b() {
        return this.sha256_;
    }

    public void b(int i) {
        this.isTest_ = i;
    }

    public void b(long j) {
        this.updateTime_ = j;
    }

    public void b(String str) {
        this.sha256_ = str;
    }

    public String c() {
        return this.skipText_;
    }

    public void c(int i) {
        this.isSplashPreContent_ = i;
    }

    public void c(String str) {
        this.skipText_ = str;
    }

    public String d() {
        return this.materialId_;
    }

    public void d(int i) {
        this.displayCount_ = i;
    }

    public void d(String str) {
        this.metaData_ = str;
    }

    public long e() {
        return this.validTime_;
    }

    public void e(int i) {
        this.maxDiaplayCount_ = i;
    }

    public void e(String str) {
        this.slotId_ = str;
    }

    public long f() {
        return this.updateTime_;
    }

    public void f(int i) {
        this.actionType_ = i;
    }

    public void f(String str) {
        this.materialId_ = str;
    }

    public void g(int i) {
        this.isPriority_ = i;
    }

    public void g(String str) {
        this.displayDate_ = str;
    }

    public int h() {
        return this.maxDiaplayCount_;
    }

    public void h(int i) {
        this.creativeType_ = i;
    }

    public void h(String str) {
        this.htmlStr_ = str;
    }

    public String i() {
        return this.htmlStr_;
    }

    public void i(String str) {
        this.clickMonitorUrl_ = str;
    }

    public int j() {
        return this.actionType_;
    }

    public void j(String str) {
        this.impMonitorUrl_ = str;
    }

    public int k() {
        return this.creativeType_;
    }

    public void k(String str) {
        this.paramFromServer_ = str;
    }

    public String l() {
        return this.clickMonitorUrl_;
    }

    public void l(String str) {
        this.intentUri_ = str;
    }

    public String m() {
        return this.impMonitorUrl_;
    }

    public String n() {
        return "materialId";
    }

    public String o() {
        return this.paramFromServer_;
    }

    public String p() {
        return this.intentUri_;
    }
}
