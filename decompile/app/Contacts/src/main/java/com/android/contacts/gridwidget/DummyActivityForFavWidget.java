package com.android.contacts.gridwidget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.hap.delete.ExtendedContactSaveService;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class DummyActivityForFavWidget extends Activity {
    private String TAG = DummyActivityForFavWidget.class.getSimpleName();
    private int mIconSize = 0;
    private boolean mLocaleChanged = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            finish();
            String lAction = intent.getAction();
            WidgetStatus lWidgetStatus = WidgetStatus.getWidgetStatus();
            this.mIconSize = getResources().getDimensionPixelSize(R.dimen.shortcut_icon_size);
            if (HwLog.HWDBG) {
                HwLog.d(this.TAG, "onReceive() is called!, Action: " + lAction);
            }
            this.mLocaleChanged = intent.getBooleanExtra("localeChanged", false);
            if ("com.huawei.android.ADD_FAVOURITE".equals(lAction)) {
                lWidgetStatus.setEditMode(true);
                startUpdateService(lAction, this, this.mLocaleChanged);
            } else if ("com.huawei.android.CLICK_DONE".equals(lAction)) {
                lWidgetStatus.setEditMode(false);
                startUpdateService(lAction, this, this.mLocaleChanged);
            } else if ("com.huawei.android.ADDFROM_GROUP".equals(lAction)) {
                lIntent = new Intent();
                lIntent.setAction("com.huawei.android.ADDFROM_GROUP");
                lIntent.addFlags(268435456);
                lIntent.addFlags(32768);
                startActivity(lIntent);
            } else if ("com.android.huawei.multiselect".equals(lAction)) {
                Intent multiselectIntent = new Intent();
                multiselectIntent.setAction("android.intent.action.HAP_ADD_FAVORITES");
                multiselectIntent.putExtra("favorite_from_widget", true);
                multiselectIntent.setFlags(268435456);
                multiselectIntent.addFlags(32768);
                startActivity(multiselectIntent);
            } else if ("com.android.huawei.DELETE".equals(lAction)) {
                deleteFavorite(intent.getLongExtra("contact_id", -1), this);
            } else if ("com.android.contacts.quickcontact.QuickContactActivity".equals(lAction)) {
                Uri lookupUri = Contacts.getLookupUri(intent.getLongExtra("contact_id", -1), intent.getStringExtra("lookupKey"));
                lIntent = new Intent(this, ContactDetailActivity.class);
                lIntent.setData(lookupUri);
                lIntent.setSourceBounds(new Rect(0, 0, this.mIconSize, this.mIconSize));
                lIntent.setFlags(268435456);
                lIntent.addFlags(32768);
                startActivity(lIntent);
            } else if ("com.android.contacts.favorites.updated".equals(lAction)) {
                startUpdateService(lAction, this, this.mLocaleChanged);
            }
        }
    }

    void startUpdateService(String aAction, Context aContext, boolean aLocaleChanged) {
        Intent intent = new Intent(aContext, UpdateContactGridWidgetService.class);
        intent.setAction(aAction);
        intent.putExtra("localeChanged", aLocaleChanged);
        aContext.startService(intent);
    }

    private void deleteFavorite(long contactid, Context aContext) {
        aContext.startService(ExtendedContactSaveService.createMarkUnmarkFavoriteSelectedContactsIntent(aContext, new long[]{contactid}, false));
    }
}
