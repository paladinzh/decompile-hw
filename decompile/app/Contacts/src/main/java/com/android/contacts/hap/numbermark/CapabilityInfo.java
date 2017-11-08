package com.android.contacts.hap.numbermark;

import android.content.ComponentName;
import android.content.Intent;
import android.net.ParseException;
import android.net.Uri;
import android.net.WebAddress;
import android.text.TextUtils;
import com.android.contacts.Collapser.Collapsible;
import com.android.contacts.detail.ContactDetailAdapter.DetailViewEntry;
import com.android.contacts.util.StructuredPostalUtils;
import java.util.HashMap;

public class CapabilityInfo implements Collapsible<CapabilityInfo>, Comparable<CapabilityInfo> {
    private static final HashMap<String, Integer> TYPE_SORT_ORDER_MAP = new HashMap();
    String content = "";
    String externalLink;
    String icon = "";
    String internalLink;
    String number;
    String packageName = "";
    String subTitle;
    String title;
    String type;

    static {
        TYPE_SORT_ORDER_MAP.put("sms", Integer.valueOf(5));
        TYPE_SORT_ORDER_MAP.put("address", Integer.valueOf(7));
        TYPE_SORT_ORDER_MAP.put("coupon", Integer.valueOf(5));
        TYPE_SORT_ORDER_MAP.put("deal", Integer.valueOf(5));
        TYPE_SORT_ORDER_MAP.put("website", Integer.valueOf(8));
        TYPE_SORT_ORDER_MAP.put("site", Integer.valueOf(6));
        TYPE_SORT_ORDER_MAP.put("introduction", Integer.valueOf(10));
        TYPE_SORT_ORDER_MAP.put("phone", Integer.valueOf(10));
        TYPE_SORT_ORDER_MAP.put("period", Integer.valueOf(10));
        TYPE_SORT_ORDER_MAP.put("weixin", Integer.valueOf(11));
        TYPE_SORT_ORDER_MAP.put("weibo", Integer.valueOf(12));
    }

    public CapabilityInfo(String type, String title, String subTitle, String number, String externalLink, String internalLink) {
        this.type = type;
        this.title = title;
        this.subTitle = subTitle;
        this.number = number;
        this.externalLink = externalLink;
        this.internalLink = internalLink;
    }

    public boolean collapseWith(CapabilityInfo info) {
        if (shouldCollapseWith(info)) {
            return false;
        }
        return true;
    }

    public boolean shouldCollapseWith(CapabilityInfo info) {
        boolean result = true;
        if (this.type != null) {
            result = 1 != null ? this.type.equals(info.type) : false;
        }
        if (result && this.title != null) {
            result = result ? this.title.equals(info.title) : false;
        }
        if (!result || this.subTitle == null) {
            return result;
        }
        return result ? this.subTitle.equals(info.subTitle) : false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("CapabilityInfo [type=");
        sb.append(this.type).append(", title=").append(this.title).append(", subTitle=").append(this.subTitle).append(", content=").append(this.content).append(", number=").append(this.number).append(", icon=").append(this.icon).append(", internalLink=").append(this.internalLink).append(", externalLink=").append(this.externalLink).append(", packageName=").append(this.packageName).append("]");
        return sb.toString();
    }

    public DetailViewEntry buildViewEntry(boolean isFristEntry) {
        DetailViewEntry entry = new DetailViewEntry();
        entry.isFristEntry = isFristEntry;
        entry.data = this.title;
        if (!TextUtils.isEmpty(this.subTitle)) {
            entry.typeString = this.subTitle;
        }
        entry.mimetype = "capability";
        entry.mCustom_mimetype = this.type;
        if ("sms".equals(this.type)) {
            entry.intent = new Intent();
            entry.intent.putExtra("number", this.number);
            entry.intent.putExtra("content", this.content);
        } else if ("address".equals(this.type)) {
            entry.intent = StructuredPostalUtils.getViewPostalAddressIntent(this.internalLink);
        } else if ("website".equals(this.type) || "site".equals(this.type) || "coupon".equals(this.type) || "deal".equals(this.type)) {
            Intent intent = buildLinkIntent(this.externalLink);
            if (intent != null) {
                entry.intent = intent;
            }
        } else if ("weixin".equals(this.type)) {
            entry.intent = buildComponentIntent("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
        } else if ("weibo".equals(this.type)) {
            entry.intent = buildLinkIntent(this.subTitle);
        }
        return entry;
    }

    private static Intent buildLinkIntent(String link) {
        if (!TextUtils.isEmpty(link)) {
            try {
                return new Intent("android.intent.action.VIEW", Uri.parse(new WebAddress(link).toString()));
            } catch (ParseException e) {
            }
        }
        return null;
    }

    private Intent buildComponentIntent(String packageName, String mainUI) {
        Intent intent = new Intent();
        ComponentName cmp = new ComponentName(packageName, mainUI);
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.addFlags(268435456);
        intent.setComponent(cmp);
        return intent;
    }

    public int compareTo(CapabilityInfo cinfo) {
        Integer innerSortOrderIdx = (Integer) TYPE_SORT_ORDER_MAP.get(this.type);
        Integer outterSortOrderIdx = (Integer) TYPE_SORT_ORDER_MAP.get(cinfo.type);
        if (innerSortOrderIdx == null && outterSortOrderIdx == null) {
            return 0;
        }
        if (innerSortOrderIdx == null) {
            return 1;
        }
        if (outterSortOrderIdx == null) {
            return -1;
        }
        return innerSortOrderIdx.intValue() - outterSortOrderIdx.intValue();
    }

    public int hashCode() {
        int i = 0;
        int hashCode = ((((this.subTitle == null ? 0 : this.subTitle.hashCode()) + 31) * 31) + (this.title == null ? 0 : this.title.hashCode())) * 31;
        if (this.type != null) {
            i = this.type.hashCode();
        }
        return hashCode + i;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CapabilityInfo other = (CapabilityInfo) obj;
        if (this.type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!this.type.equals(other.type)) {
            return false;
        }
        if (this.title == null) {
            if (other.title != null) {
                return false;
            }
        } else if (!this.title.equals(other.title)) {
            return false;
        }
        if (this.subTitle == null) {
            if (other.subTitle != null) {
                return false;
            }
        } else if (!this.subTitle.equals(other.subTitle)) {
            return false;
        }
        return true;
    }

    public String getTitle() {
        return this.title;
    }

    public String getSubTitle() {
        return this.subTitle;
    }

    public String getInternalLink() {
        return this.internalLink;
    }

    public String getExternalLink() {
        return this.externalLink;
    }

    public String getType() {
        return this.type;
    }
}
