package com.android.mms;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.HwQuickActionActivity;
import com.android.mms.util.ContactUtil;
import com.android.mms.util.HwQuickActionContact;
import com.google.android.gms.R;
import com.huawei.android.quickaction.ActionIcon;
import com.huawei.android.quickaction.QuickAction;
import com.huawei.android.quickaction.QuickActionService;
import java.util.ArrayList;
import java.util.List;

public class HwQuickActionService extends QuickActionService {
    private ComponentName mComposeMessageComponent;
    private ComponentName mContactFavoriteComponent;
    private List<HwQuickActionContact> mContactsList = new ArrayList();
    private ComponentName mFilterComponent;

    public void onCreate() {
        Log.i("HwQuickActionService", "HwQuickActionService on create");
        super.onCreate();
        this.mComposeMessageComponent = new ComponentName(this, ComposeMessageActivity.class);
        this.mFilterComponent = new ComponentName(this, HwQuickActionActivity.class);
        this.mContactFavoriteComponent = new ComponentName(this, "com.android.contacts.hap.activities.FavoriteContactsActivity");
    }

    public List<QuickAction> onGetQuickActions(ComponentName componentName) {
        List<QuickAction> quickActions = new ArrayList();
        Log.i("HwQuickActionService", "HwQuickActionService onGetQuickActions");
        Intent smsIntent = new Intent("android.intent.action.SENDTO", Uri.fromParts("smsto", "", null));
        smsIntent.addFlags(32768);
        smsIntent.addFlags(268435456);
        smsIntent.putExtra("QUICKACTION_QUICK_NEW_MESSAGE_KEY", "QUICKACTION_QUICK_NEW_MESSAGE_VALUE");
        quickActions.add(new QuickAction(getString(R.string.new_message), ActionIcon.createWithResource(getApplicationContext(), (int) R.drawable.quick_menu_plus_normal), this.mComposeMessageComponent, PendingIntent.getActivity(this, 0, smsIntent, 134217728).getIntentSender()));
        this.mContactsList = ContactUtil.getFavoriteContact(this);
        if (this.mContactsList.size() == 0) {
            Intent contactFavorivateIntent = new Intent("com.huawei.contacts.action.FAVORITE_CONTACTS");
            contactFavorivateIntent.addFlags(32768);
            contactFavorivateIntent.addFlags(268435456);
            quickActions.add(new QuickAction(getString(R.string.quickaction_add_favorites), ActionIcon.createWithResource(getApplicationContext(), (int) R.drawable.btn_quick_action_starred), this.mContactFavoriteComponent, PendingIntent.getActivity(this, 0, contactFavorivateIntent, 0).getIntentSender()));
        } else {
            for (HwQuickActionContact mmsContact : this.mContactsList) {
                ActionIcon actionIcon;
                Intent filterIntent = new Intent("com.android.mms.action.QUICK_ACTION_ACTIVITY");
                filterIntent.addFlags(32768);
                filterIntent.addFlags(268435456);
                filterIntent.setComponent(this.mFilterComponent);
                filterIntent.putExtra("QUICKACTION_CONTACT_KEY", mmsContact.getId());
                PendingIntent filterpendingIntent = PendingIntent.getActivity(this, (int) mmsContact.getId(), filterIntent, 134217728);
                if (mmsContact.getData().length == 0) {
                    Log.d("HwQuickActionService", "contact has no head image");
                    actionIcon = ActionIcon.createWithResource((Context) this, (int) R.drawable.csp_default_avatar);
                } else {
                    Log.d("HwQuickActionService", "contact has head image");
                    actionIcon = ActionIcon.createWithBitmap(ContactUtil.createRoundPhoto(BitmapFactory.decodeByteArray(mmsContact.getData(), 0, mmsContact.getData().length)));
                }
                quickActions.add(new QuickAction(mmsContact.getContactName(), actionIcon, this.mFilterComponent, filterpendingIntent.getIntentSender()));
            }
        }
        return quickActions;
    }

    public void onDestroy() {
        Log.i("HwQuickActionService", "HwQuickActionService onDestroy");
        super.onDestroy();
    }
}
