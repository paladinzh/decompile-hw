package com.android.contacts;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.util.ContactDisplayUtils;
import com.google.android.gms.R;
import com.huawei.android.quickaction.ActionIcon;
import com.huawei.android.quickaction.QuickAction;
import com.huawei.android.quickaction.QuickActionService;
import com.huawei.cspcommon.util.BitmapUtil;
import com.huawei.cspcommon.util.CommonConstants;
import java.util.ArrayList;
import java.util.List;

public class QuickPressService extends QuickActionService {
    private static final String[] COLUMNS = new String[]{"_id", "display_name", "photo_uri", "lookup", "photo_id"};
    private ArrayList<QuickAction> mActions = new ArrayList();
    private int mDefaultHeigth = 0;
    private int mDefaultWidth = 0;

    public List<QuickAction> onGetQuickActions(ComponentName targetActivityName) {
        if (targetActivityName == null) {
            return null;
        }
        this.mActions.clear();
        CommonConstants.setSimplifiedModeEnabled(CommonUtilMethods.isSimpleModeOn());
        this.mActions.add(getQuickAction("android.intent.action.INSERT", getResources().getString(R.string.menu_newContact), ActionIcon.createWithResource("com.android.contacts", (int) R.drawable.btn_quick_action_newcontact_normal), Contacts.CONTENT_URI, "com.android.contacts.activities.ContactEditorActivity", "ViewDelayedLoadingSwitch", true));
        if ("com.android.contacts.activities.DialtactsActivity".equals(targetActivityName.getClassName())) {
            getDisplayContactsQuickAction();
        } else {
            this.mActions.add(getQuickAction("com.android.contacts.action.QUICK_ACTION_ACTIVITY", getResources().getString(R.string.camcard_scan_card), ActionIcon.createWithResource("com.android.contacts", (int) R.drawable.btn_quick_action_cardscan), null, "com.android.contacts.QuickPressPickNumberActivity", "callcamcard", true));
        }
        return this.mActions;
    }

    private void getDisplayContactsQuickAction() {
        Cursor cursor = null;
        try {
            cursor = getStarredContacts();
            if (cursor == null || cursor.getCount() != 0) {
                updateStarInfoActions(cursor);
                if (cursor != null) {
                    cursor.close();
                }
                return;
            }
            getActionStarred();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void getActionStarred() {
        this.mActions.add(getQuickAction("com.huawei.contacts.action.FAVORITE_CONTACTS", getResources().getString(R.string.contact_button_add_favorites), ActionIcon.createWithResource("com.android.contacts", (int) R.drawable.btn_quick_action_starred), null, "com.android.contacts.hap.activities.FavoriteContactsActivity", "from_to_quickaction", true));
    }

    private Cursor getStarredContacts() {
        String sortOrder;
        Uri uri = Contacts.CONTENT_URI.buildUpon().appendQueryParameter("limit", String.valueOf(3)).build();
        if (ContactDisplayUtils.getNameDisplayOrder() == 1) {
            sortOrder = "sort_key";
        } else {
            sortOrder = "sort_key_alt";
        }
        return getContentResolver().query(uri, COLUMNS, "starred=1", null, sortOrder + " ASC");
    }

    private void updateStarInfoActions(Cursor cursor) {
        String starName = getResources().getString(R.string.missing_name);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex("_id"));
                String name = cursor.getString(cursor.getColumnIndex("display_name"));
                String photoUriString = cursor.getString(cursor.getColumnIndex("photo_uri"));
                Uri contactLookupUri = Contacts.getLookupUri(id, cursor.getString(cursor.getColumnIndex("lookup")));
                ActionIcon starIcon = getContactActionIcon(photoUriString, id);
                if (name != null) {
                    starName = name;
                }
                this.mActions.add(getQuickAction("com.android.contacts.action.QUICK_ACTION_ACTIVITY", starName, starIcon, contactLookupUri, "com.android.contacts.QuickPressPickNumberActivity", "callStarNumber", true));
            }
        }
    }

    private ActionIcon getContactActionIcon(String photoUriString, long contactId) {
        if (photoUriString != null) {
            initDefaultDrawableSize();
            Bitmap photo = ContactPhotoManager.createRoundPhoto(openPhoto(contactId));
            if (photo != null) {
                return ActionIcon.createWithBitmap(photo);
            }
        }
        return ActionIcon.createWithResource((Context) this, (int) R.drawable.ic_contact_picture_holo_light);
    }

    private void initDefaultDrawableSize() {
        Drawable drawable = getResources().getDrawable(R.drawable.ic_contact_picture_holo_light);
        if (this.mDefaultWidth == 0) {
            this.mDefaultWidth = drawable.getIntrinsicWidth();
            this.mDefaultHeigth = drawable.getIntrinsicHeight();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Bitmap openPhoto(long contactId) {
        Bitmap bitmap = null;
        Uri photoUri = Uri.withAppendedPath(ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId), "photo");
        Cursor cursor = getContentResolver().query(photoUri, new String[]{"data15"}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    bitmap = BitmapUtil.decodeBitmapFromBytes(data, this.mDefaultWidth, this.mDefaultHeigth);
                }
                cursor.close();
                return bitmap;
            }
            cursor.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            cursor.close();
        }
    }

    private QuickAction getQuickAction(String action, String title, ActionIcon icon, Uri uri, String className, String extraName, boolean extravalue) {
        ComponentName componentName = new ComponentName("com.android.contacts", className);
        Intent intent = new Intent(action, uri);
        if ("android.intent.action.INSERT".equals(action)) {
            intent.putExtra("from_to_quickaction", true);
        }
        intent.addFlags(32768);
        intent.addFlags(268435456);
        intent.setComponent(componentName);
        intent.putExtra(extraName, extravalue);
        return new QuickAction(title, icon, componentName, PendingIntent.getActivity(this, 0, intent, 134217728).getIntentSender());
    }
}
