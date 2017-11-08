package com.android.mms.ui;

import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.QuickContact;
import android.util.AttributeSet;
import android.view.View;
import android.widget.QuickContactBadge;
import cn.com.xy.sms.sdk.HarassNumberUtil;
import com.android.mms.data.Contact;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.AsyncQueryHandlerEx;
import com.huawei.harassmentinterception.service.BlacklistCommonUtils;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwNumberMatchUtils;
import com.huawei.mms.util.NumberUtils.AddrMatcher;

public class AvatarView extends QuickContactBadge {
    static final String[] EMAIL_LOOKUP_PROJECTION = new String[]{"contact_id", "lookup"};
    static final String[] PHONE_LOOKUP_PROJECTION = new String[]{HarassNumberUtil.NUMBER, "_id", "lookup"};
    private long mContactId;
    private String mContactPhone;
    private Uri mContactUri;
    Context mContext;
    private QueryHandler mQueryHandler;

    private class QueryHandler extends AsyncQueryHandlerEx {
        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Uri lookupUri = null;
            Uri uri = null;
            boolean trigger = false;
            if (3 == token || 1 == token) {
                if (3 == token) {
                    trigger = true;
                    try {
                        uri = Uri.fromParts("tel", (String) cookie, null);
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                if (cursor == null) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                } else if (AvatarView.isNumberMatchedFW(cookie, cursor)) {
                    lookupUri = Contacts.getLookupUri(cursor.getLong(1), cursor.getString(2));
                }
            }
            if (2 == token || token == 0) {
                if (2 == token) {
                    trigger = true;
                    uri = Uri.fromParts("mailto", (String) cookie, null);
                }
                if (cursor != null && cursor.moveToFirst()) {
                    lookupUri = Contacts.getLookupUri(cursor.getLong(0), cursor.getString(1));
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            AvatarView.this.mContactId = 0;
            AvatarView.this.mContactUri = lookupUri;
            AvatarView.this.onContactUriChanged();
            if (trigger && lookupUri != null) {
                try {
                    AvatarView.this.showContactDetail(AvatarView.this.getContext(), AvatarView.this.mContactUri);
                } catch (ActivityNotFoundException ex) {
                    MLog.w("AvatarView", "contact detail activity not found >>> " + ex);
                    QuickContact.showQuickContact(AvatarView.this.getContext(), AvatarView.this, lookupUri, 3, new String[]{"vnd.android.cursor.item/contact"});
                }
            } else if (uri != null) {
                AvatarView.this.showOrCreateContactDialog((String) cookie, uri);
            }
        }
    }

    public AvatarView(Context context) {
        super(context);
        this.mQueryHandler = null;
        this.mContactUri = null;
        this.mContactId = 0;
        this.mContactPhone = null;
        this.mContext = null;
    }

    public AvatarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvatarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mQueryHandler = null;
        this.mContactUri = null;
        this.mContactId = 0;
        this.mContactPhone = null;
        this.mContext = null;
        this.mContext = context;
        this.mQueryHandler = new QueryHandler(context.getContentResolver());
        setOnClickListener(this);
    }

    private boolean isAssigned() {
        return (this.mContactUri == null && this.mContactPhone == null && this.mContactId == 0) ? false : true;
    }

    private void onContactUriChanged() {
        setEnabled(isAssigned());
    }

    public void onClick(View v) {
        if (this.mContactId != 0 && this.mContactUri == null) {
            this.mContactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, this.mContactId);
        }
        if (this.mContactUri != null) {
            try {
                showContactDetail(getContext(), this.mContactUri);
            } catch (ActivityNotFoundException ex) {
                MLog.w("AvatarView", "contact detail activity not found >>> " + ex);
                QuickContact.showQuickContact(getContext(), this, this.mContactUri, 3, new String[]{"vnd.android.cursor.item/contact"});
            }
        } else if (this.mContactPhone != null) {
            this.mQueryHandler.startQuery(3, this.mContactPhone, Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, this.mContactPhone), PHONE_LOOKUP_PROJECTION, null, null, null);
        }
    }

    private void showContactDetail(Context context, Uri lookupUri) {
        Intent intent = new Intent("android.intent.action.VIEW", lookupUri);
        intent.setFlags(524288);
        context.startActivity(intent);
    }

    private CharSequence[] getDialogItems(String address) {
        Resources resource = this.mContext.getResources();
        String unavailableContact = resource.getString(R.string.contact_unavailable_create_contact);
        String savetoExistContact = resource.getString(R.string.saveto_existed_contact);
        if (Contact.isEmailAddress(address)) {
            return new CharSequence[]{unavailableContact, savetoExistContact};
        } else if (!BlacklistCommonUtils.isBlacklistFeatureEnable()) {
            return new CharSequence[]{unavailableContact, savetoExistContact};
        } else if (BlacklistCommonUtils.checkPhoneNumberFromBlockItem(BlacklistCommonUtils.getBlacklistService(), address)) {
            return new CharSequence[]{unavailableContact, savetoExistContact, resource.getString(R.string.menu_remove_from_blacklist)};
        } else {
            return new CharSequence[]{unavailableContact, savetoExistContact, resource.getString(R.string.menu_add_to_blacklist)};
        }
    }

    private String getUriByAddress(String address) {
        if (Contact.isEmailAddress(address)) {
            return "mailto:" + address;
        }
        return "tel:" + address;
    }

    private void showOrCreateContactDialog(final String address, Uri createUri) {
        Builder builder = new Builder(this.mContext);
        builder.setTitle(address);
        builder.create();
        builder.setItems(getDialogItems(address), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        HwMessageUtils.saveNewContact(AvatarView.this.getUriByAddress(address), AvatarView.this.getContext());
                        return;
                    case 1:
                        HwMessageUtils.saveExistContact(AvatarView.this.getUriByAddress(address), AvatarView.this.getContext());
                        return;
                    case 2:
                        if (BlacklistCommonUtils.checkPhoneNumberFromBlockItem(BlacklistCommonUtils.getBlacklistService(), address)) {
                            BlacklistCommonUtils.handleNumberBlockList(BlacklistCommonUtils.getBlacklistService(), address, R.id.menu_remove_from_blacklist);
                            BlacklistCommonUtils.toastAddOrRemoveBlacklistInfo(AvatarView.this.mContext, false);
                            return;
                        }
                        BlacklistCommonUtils.comfirmAddContactToBlacklist(AvatarView.this.mContext, address);
                        return;
                    default:
                        return;
                }
            }
        });
        builder.show();
    }

    private static boolean isNumberMatchedFW(Object cookie, Cursor cursor) {
        if (cursor.getCount() < 1) {
            return false;
        }
        String originNumber = (String) cookie;
        int index = HwNumberMatchUtils.getMatchedIndex(cursor, originNumber, HarassNumberUtil.NUMBER);
        if (index >= 0 && cursor.moveToPosition(index)) {
            return true;
        }
        if (-2 == index) {
            return isNumberMatchedHW(originNumber, cursor);
        }
        return false;
    }

    private static boolean isNumberMatchedHW(String originNumber, Cursor cursor) {
        if (!cursor.moveToFirst()) {
            return false;
        }
        int matchedPos = -1;
        int finalMatchType = 0;
        do {
            int matchType = AddrMatcher.isNumberMatch(originNumber, cursor.getString(cursor.getColumnIndexOrThrow(HarassNumberUtil.NUMBER)));
            if (matchType > finalMatchType) {
                finalMatchType = matchType;
                matchedPos = cursor.getPosition();
            }
            if (9 == finalMatchType) {
                return true;
            }
        } while (cursor.moveToNext());
        return matchedPos != -1 && cursor.moveToPosition(matchedPos);
    }

    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (RuntimeException e) {
            MLog.e("AvatarView", e.getMessage());
        }
    }
}
