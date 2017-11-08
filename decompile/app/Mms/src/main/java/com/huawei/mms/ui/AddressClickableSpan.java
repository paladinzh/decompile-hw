package com.huawei.mms.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import com.google.android.gms.R;
import com.huawei.mms.util.HwMessageUtils;
import java.util.ArrayList;

public class AddressClickableSpan extends HwClickableSpan {
    private static final int[] OPS_ALL = new int[]{R.string.clickspan_view_in_maps, R.string.clickspan_navigate, R.string.clickspan_copy, R.string.clickspan_new_contact, R.string.clickspan_save_contact};
    private String mUrl;

    public AddressClickableSpan(Context context, String url) {
        super(context, url, 2);
        this.mUrl = url;
    }

    protected ArrayList<Integer> getOperations() {
        ArrayList<Integer> menuItems = new ArrayList();
        for (int resId : OPS_ALL) {
            menuItems.add(Integer.valueOf(resId));
        }
        return menuItems;
    }

    protected String getShowingtitle() {
        return this.mUrl.substring("geo:0,0?q=".length(), this.mUrl.length());
    }

    protected String getCopiedString() {
        return this.mUrl.substring("geo:0,0?q=".length(), this.mUrl.length());
    }

    protected long getContactId() {
        return 0;
    }

    public void onPress(View view) {
        if (HwMessageUtils.isPkgInstalled(this.mContext, "com.autonavi.minimap")) {
            gotoGaode(Uri.parse(this.mUrl));
        }
    }

    private void gotoGaode(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW", uri);
        intent.putExtra("android.intent.action.START_PEEK_ACTIVITY", "startPeekActivity");
        intent.setComponent(new ComponentName("com.autonavi.minimap", "com.autonavi.map.activity.PeekActivity"));
        try {
            this.mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
