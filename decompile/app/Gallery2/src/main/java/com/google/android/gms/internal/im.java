package com.google.android.gms.internal;

import android.os.Parcel;
import com.amap.api.services.core.AMapException;
import com.google.android.gms.common.data.Freezable;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.fb.a;
import com.huawei.watermark.ui.WMEditor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/* compiled from: Unknown */
public final class im extends fb implements SafeParcelable, Freezable {
    public static final in CREATOR = new in();
    private static final HashMap<String, a<?, ?>> RL = new HashMap();
    private String FH;
    private double KX;
    private double KY;
    private String Oc;
    private final Set<Integer> RM;
    private im RN;
    private List<String> RO;
    private im RP;
    private String RQ;
    private String RR;
    private String RS;
    private List<im> RT;
    private int RU;
    private List<im> RV;
    private im RW;
    private List<im> RX;
    private String RY;
    private String RZ;
    private String SA;
    private String SB;
    private im SC;
    private String SD;
    private String SE;
    private String SF;
    private String SG;
    private im Sa;
    private String Sb;
    private String Sc;
    private List<im> Sd;
    private String Se;
    private String Sf;
    private String Sg;
    private String Sh;
    private String Si;
    private String Sj;
    private String Sk;
    private String Sl;
    private im Sm;
    private String Sn;
    private String So;
    private String Sp;
    private im Sq;
    private im Sr;
    private im Ss;
    private List<im> St;
    private String Su;
    private String Sv;
    private String Sw;
    private String Sx;
    private im Sy;
    private String Sz;
    private String lt;
    private String mName;
    private String pS;
    private String uS;
    private final int wj;

    static {
        RL.put("about", a.a("about", 2, im.class));
        RL.put("additionalName", a.k("additionalName", 3));
        RL.put("address", a.a("address", 4, im.class));
        RL.put("addressCountry", a.j("addressCountry", 5));
        RL.put("addressLocality", a.j("addressLocality", 6));
        RL.put("addressRegion", a.j("addressRegion", 7));
        RL.put("associated_media", a.b("associated_media", 8, im.class));
        RL.put("attendeeCount", a.g("attendeeCount", 9));
        RL.put("attendees", a.b("attendees", 10, im.class));
        RL.put("audio", a.a("audio", 11, im.class));
        RL.put("author", a.b("author", 12, im.class));
        RL.put("bestRating", a.j("bestRating", 13));
        RL.put("birthDate", a.j("birthDate", 14));
        RL.put("byArtist", a.a("byArtist", 15, im.class));
        RL.put("caption", a.j("caption", 16));
        RL.put("contentSize", a.j("contentSize", 17));
        RL.put("contentUrl", a.j("contentUrl", 18));
        RL.put("contributor", a.b("contributor", 19, im.class));
        RL.put("dateCreated", a.j("dateCreated", 20));
        RL.put("dateModified", a.j("dateModified", 21));
        RL.put("datePublished", a.j("datePublished", 22));
        RL.put("description", a.j("description", 23));
        RL.put("duration", a.j("duration", 24));
        RL.put("embedUrl", a.j("embedUrl", 25));
        RL.put("endDate", a.j("endDate", 26));
        RL.put("familyName", a.j("familyName", 27));
        RL.put("gender", a.j("gender", 28));
        RL.put("geo", a.a("geo", 29, im.class));
        RL.put("givenName", a.j("givenName", 30));
        RL.put("height", a.j("height", 31));
        RL.put("id", a.j("id", 32));
        RL.put("image", a.j("image", 33));
        RL.put("inAlbum", a.a("inAlbum", 34, im.class));
        RL.put("latitude", a.h("latitude", 36));
        RL.put("location", a.a("location", 37, im.class));
        RL.put("longitude", a.h("longitude", 38));
        RL.put("name", a.j("name", 39));
        RL.put("partOfTVSeries", a.a("partOfTVSeries", 40, im.class));
        RL.put("performers", a.b("performers", 41, im.class));
        RL.put("playerType", a.j("playerType", 42));
        RL.put("postOfficeBoxNumber", a.j("postOfficeBoxNumber", 43));
        RL.put("postalCode", a.j("postalCode", 44));
        RL.put("ratingValue", a.j("ratingValue", 45));
        RL.put("reviewRating", a.a("reviewRating", 46, im.class));
        RL.put("startDate", a.j("startDate", 47));
        RL.put("streetAddress", a.j("streetAddress", 48));
        RL.put(WMEditor.TYPETEXT, a.j(WMEditor.TYPETEXT, 49));
        RL.put("thumbnail", a.a("thumbnail", 50, im.class));
        RL.put("thumbnailUrl", a.j("thumbnailUrl", 51));
        RL.put("tickerSymbol", a.j("tickerSymbol", 52));
        RL.put("type", a.j("type", 53));
        RL.put("url", a.j("url", 54));
        RL.put("width", a.j("width", 55));
        RL.put("worstRating", a.j("worstRating", 56));
    }

    public im() {
        this.wj = 1;
        this.RM = new HashSet();
    }

    im(Set<Integer> set, int i, im imVar, List<String> list, im imVar2, String str, String str2, String str3, List<im> list2, int i2, List<im> list3, im imVar3, List<im> list4, String str4, String str5, im imVar4, String str6, String str7, String str8, List<im> list5, String str9, String str10, String str11, String str12, String str13, String str14, String str15, String str16, String str17, im imVar5, String str18, String str19, String str20, String str21, im imVar6, double d, im imVar7, double d2, String str22, im imVar8, List<im> list6, String str23, String str24, String str25, String str26, im imVar9, String str27, String str28, String str29, im imVar10, String str30, String str31, String str32, String str33, String str34, String str35) {
        this.RM = set;
        this.wj = i;
        this.RN = imVar;
        this.RO = list;
        this.RP = imVar2;
        this.RQ = str;
        this.RR = str2;
        this.RS = str3;
        this.RT = list2;
        this.RU = i2;
        this.RV = list3;
        this.RW = imVar3;
        this.RX = list4;
        this.RY = str4;
        this.RZ = str5;
        this.Sa = imVar4;
        this.Sb = str6;
        this.Sc = str7;
        this.lt = str8;
        this.Sd = list5;
        this.Se = str9;
        this.Sf = str10;
        this.Sg = str11;
        this.FH = str12;
        this.Sh = str13;
        this.Si = str14;
        this.Sj = str15;
        this.Sk = str16;
        this.Sl = str17;
        this.Sm = imVar5;
        this.Sn = str18;
        this.So = str19;
        this.uS = str20;
        this.Sp = str21;
        this.Sq = imVar6;
        this.KX = d;
        this.Sr = imVar7;
        this.KY = d2;
        this.mName = str22;
        this.Ss = imVar8;
        this.St = list6;
        this.Su = str23;
        this.Sv = str24;
        this.Sw = str25;
        this.Sx = str26;
        this.Sy = imVar9;
        this.Sz = str27;
        this.SA = str28;
        this.SB = str29;
        this.SC = imVar10;
        this.SD = str30;
        this.SE = str31;
        this.Oc = str32;
        this.pS = str33;
        this.SF = str34;
        this.SG = str35;
    }

    protected boolean a(a aVar) {
        return this.RM.contains(Integer.valueOf(aVar.eu()));
    }

    protected Object ak(String str) {
        return null;
    }

    protected boolean al(String str) {
        return false;
    }

    protected Object b(a aVar) {
        switch (aVar.eu()) {
            case 2:
                return this.RN;
            case 3:
                return this.RO;
            case 4:
                return this.RP;
            case 5:
                return this.RQ;
            case 6:
                return this.RR;
            case 7:
                return this.RS;
            case 8:
                return this.RT;
            case 9:
                return Integer.valueOf(this.RU);
            case 10:
                return this.RV;
            case 11:
                return this.RW;
            case 12:
                return this.RX;
            case 13:
                return this.RY;
            case 14:
                return this.RZ;
            case 15:
                return this.Sa;
            case 16:
                return this.Sb;
            case 17:
                return this.Sc;
            case 18:
                return this.lt;
            case 19:
                return this.Sd;
            case 20:
                return this.Se;
            case 21:
                return this.Sf;
            case 22:
                return this.Sg;
            case 23:
                return this.FH;
            case 24:
                return this.Sh;
            case 25:
                return this.Si;
            case AMapException.ERROR_CODE_URL /*26*/:
                return this.Sj;
            case AMapException.ERROR_CODE_UNKNOW_HOST /*27*/:
                return this.Sk;
            case AMapException.ERROR_CODE_UNKNOW_SERVICE /*28*/:
                return this.Sl;
            case AMapException.ERROR_CODE_PROTOCOL /*29*/:
                return this.Sm;
            case 30:
                return this.Sn;
            case 31:
                return this.So;
            case 32:
                return this.uS;
            case 33:
                return this.Sp;
            case AMapException.ERROR_CODE_SERVER /*34*/:
                return this.Sq;
            case AMapException.ERROR_CODE_REQUEST /*36*/:
                return Double.valueOf(this.KX);
            case 37:
                return this.Sr;
            case 38:
                return Double.valueOf(this.KY);
            case 39:
                return this.mName;
            case 40:
                return this.Ss;
            case 41:
                return this.St;
            case 42:
                return this.Su;
            case 43:
                return this.Sv;
            case 44:
                return this.Sw;
            case 45:
                return this.Sx;
            case 46:
                return this.Sy;
            case 47:
                return this.Sz;
            case 48:
                return this.SA;
            case 49:
                return this.SB;
            case 50:
                return this.SC;
            case 51:
                return this.SD;
            case 52:
                return this.SE;
            case 53:
                return this.Oc;
            case 54:
                return this.pS;
            case 55:
                return this.SF;
            case 56:
                return this.SG;
            default:
                throw new IllegalStateException("Unknown safe parcelable id=" + aVar.eu());
        }
    }

    public int describeContents() {
        in inVar = CREATOR;
        return 0;
    }

    public HashMap<String, a<?, ?>> en() {
        return RL;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof im)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        im imVar = (im) obj;
        for (a aVar : RL.values()) {
            if (a(aVar)) {
                if (!imVar.a(aVar) || !b(aVar).equals(imVar.b(aVar))) {
                    return false;
                }
            } else if (imVar.a(aVar)) {
                return false;
            }
        }
        return true;
    }

    public /* synthetic */ Object freeze() {
        return hR();
    }

    public List<String> getAdditionalName() {
        return this.RO;
    }

    public String getAddressCountry() {
        return this.RQ;
    }

    public String getAddressLocality() {
        return this.RR;
    }

    public String getAddressRegion() {
        return this.RS;
    }

    public int getAttendeeCount() {
        return this.RU;
    }

    public String getBestRating() {
        return this.RY;
    }

    public String getBirthDate() {
        return this.RZ;
    }

    public String getCaption() {
        return this.Sb;
    }

    public String getContentSize() {
        return this.Sc;
    }

    public String getContentUrl() {
        return this.lt;
    }

    public String getDateCreated() {
        return this.Se;
    }

    public String getDateModified() {
        return this.Sf;
    }

    public String getDatePublished() {
        return this.Sg;
    }

    public String getDescription() {
        return this.FH;
    }

    public String getDuration() {
        return this.Sh;
    }

    public String getEmbedUrl() {
        return this.Si;
    }

    public String getEndDate() {
        return this.Sj;
    }

    public String getFamilyName() {
        return this.Sk;
    }

    public String getGender() {
        return this.Sl;
    }

    public String getGivenName() {
        return this.Sn;
    }

    public String getHeight() {
        return this.So;
    }

    public String getId() {
        return this.uS;
    }

    public String getImage() {
        return this.Sp;
    }

    public double getLatitude() {
        return this.KX;
    }

    public double getLongitude() {
        return this.KY;
    }

    public String getName() {
        return this.mName;
    }

    public String getPlayerType() {
        return this.Su;
    }

    public String getPostOfficeBoxNumber() {
        return this.Sv;
    }

    public String getPostalCode() {
        return this.Sw;
    }

    public String getRatingValue() {
        return this.Sx;
    }

    public String getStartDate() {
        return this.Sz;
    }

    public String getStreetAddress() {
        return this.SA;
    }

    public String getText() {
        return this.SB;
    }

    public String getThumbnailUrl() {
        return this.SD;
    }

    public String getTickerSymbol() {
        return this.SE;
    }

    public String getType() {
        return this.Oc;
    }

    public String getUrl() {
        return this.pS;
    }

    int getVersionCode() {
        return this.wj;
    }

    public String getWidth() {
        return this.SF;
    }

    public String getWorstRating() {
        return this.SG;
    }

    Set<Integer> hB() {
        return this.RM;
    }

    im hC() {
        return this.RN;
    }

    im hD() {
        return this.RP;
    }

    List<im> hE() {
        return this.RT;
    }

    List<im> hF() {
        return this.RV;
    }

    im hG() {
        return this.RW;
    }

    List<im> hH() {
        return this.RX;
    }

    im hI() {
        return this.Sa;
    }

    List<im> hJ() {
        return this.Sd;
    }

    im hK() {
        return this.Sm;
    }

    im hL() {
        return this.Sq;
    }

    im hM() {
        return this.Sr;
    }

    im hN() {
        return this.Ss;
    }

    List<im> hO() {
        return this.St;
    }

    im hP() {
        return this.Sy;
    }

    im hQ() {
        return this.SC;
    }

    public im hR() {
        return this;
    }

    public int hashCode() {
        int i = 0;
        Iterator it = RL.values().iterator();
        while (true) {
            int i2 = i;
            if (!it.hasNext()) {
                return i2;
            }
            a aVar = (a) it.next();
            if (a(aVar)) {
                i = b(aVar).hashCode() + (i2 + aVar.eu());
            } else {
                i = i2;
            }
        }
    }

    public void writeToParcel(Parcel out, int flags) {
        in inVar = CREATOR;
        in.a(this, out, flags);
    }
}
