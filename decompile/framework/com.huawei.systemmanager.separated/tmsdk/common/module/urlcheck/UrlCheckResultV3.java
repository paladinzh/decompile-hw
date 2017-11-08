package tmsdk.common.module.urlcheck;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tmsdkobf.cf;
import tmsdkobf.cj;
import tmsdkobf.ck;

/* compiled from: Unknown */
public class UrlCheckResultV3 implements Parcelable {
    public static Creator<UrlCheckResultV3> CREATOR = new Creator<UrlCheckResultV3>() {
        public /* synthetic */ Object createFromParcel(Parcel parcel) {
            return t(parcel);
        }

        public UrlCheckResultV3[] cv(int i) {
            return new UrlCheckResultV3[i];
        }

        public /* synthetic */ Object[] newArray(int i) {
            return cv(i);
        }

        public UrlCheckResultV3 t(Parcel parcel) {
            UrlCheckResultV3 urlCheckResultV3 = new UrlCheckResultV3();
            urlCheckResultV3.url = parcel.readString();
            urlCheckResultV3.level = parcel.readInt();
            urlCheckResultV3.linkType = parcel.readInt();
            urlCheckResultV3.riskType = parcel.readInt();
            if (urlCheckResultV3.linkType != 0) {
                urlCheckResultV3.apkDetail = (ApkDetail) parcel.readParcelable(ApkDetail.class.getClassLoader());
            } else {
                urlCheckResultV3.webPageDetail = (WebPageDetail) parcel.readParcelable(WebPageDetail.class.getClassLoader());
            }
            urlCheckResultV3.mErrCode = parcel.readInt();
            return urlCheckResultV3;
        }
    };
    public ApkDetail apkDetail;
    public int level;
    public int linkType;
    public int mErrCode;
    public int riskType;
    public String url;
    public WebPageDetail webPageDetail;

    private UrlCheckResultV3() {
        this.level = -1;
        this.linkType = -1;
        this.riskType = -1;
        this.webPageDetail = null;
        this.apkDetail = null;
        this.mErrCode = 0;
    }

    public UrlCheckResultV3(int i) {
        this.level = -1;
        this.linkType = -1;
        this.riskType = -1;
        this.webPageDetail = null;
        this.apkDetail = null;
        this.mErrCode = 0;
        this.mErrCode = i;
    }

    public UrlCheckResultV3(String str, cj cjVar) {
        this.level = -1;
        this.linkType = -1;
        this.riskType = -1;
        this.webPageDetail = null;
        this.apkDetail = null;
        this.mErrCode = 0;
        this.url = str;
        this.level = cjVar.level;
        this.linkType = cjVar.linkType;
        this.riskType = cjVar.riskType;
        if (cjVar.fj != null) {
            this.webPageDetail = a(cjVar.fj);
        }
        if (cjVar.fk != null) {
            this.apkDetail = a(cjVar.fk);
        }
    }

    private ApkDetail a(cf cfVar) {
        ApkDetail apkDetail = new ApkDetail();
        apkDetail.apkName = cfVar.apkName;
        apkDetail.apkPackage = cfVar.apkPackage;
        apkDetail.iconUrl = cfVar.iconUrl;
        apkDetail.versionCode = cfVar.versionCode;
        apkDetail.versionName = cfVar.versionName;
        apkDetail.size = cfVar.size;
        apkDetail.official = cfVar.official;
        apkDetail.developer = cfVar.developer;
        apkDetail.certMD5 = cfVar.certMD5;
        apkDetail.isInSoftwareDB = cfVar.isInSoftwareDB;
        apkDetail.description = cfVar.description;
        apkDetail.imageUrls = cfVar.imageUrls;
        apkDetail.downloadCount = cfVar.downloadCount;
        apkDetail.source = cfVar.source;
        apkDetail.sensitivePermissions = cfVar.sensitivePermissions;
        apkDetail.virsusName = cfVar.virsusName;
        apkDetail.virsusDescription = cfVar.virsusDescription;
        return apkDetail;
    }

    private WebPageDetail a(ck ckVar) {
        WebPageDetail webPageDetail = new WebPageDetail();
        webPageDetail.title = ckVar.title;
        webPageDetail.description = ckVar.description;
        webPageDetail.webIconUrl = ckVar.webIconUrl;
        webPageDetail.screenshotUrl = ckVar.screenshotUrl;
        webPageDetail.maliceType = ckVar.maliceType;
        webPageDetail.maliceTitle = ckVar.maliceTitle;
        webPageDetail.maliceBody = ckVar.maliceBody;
        webPageDetail.flawName = ckVar.flawName;
        return webPageDetail;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.url);
        parcel.writeInt(this.level);
        parcel.writeInt(this.linkType);
        parcel.writeInt(this.riskType);
        if (this.linkType != 0) {
            parcel.writeParcelable(this.apkDetail, 0);
        } else {
            parcel.writeParcelable(this.webPageDetail, 0);
        }
        parcel.writeInt(this.mErrCode);
    }
}
