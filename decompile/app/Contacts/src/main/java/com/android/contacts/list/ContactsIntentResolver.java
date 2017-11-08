package com.android.contacts.list;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.rcs.list.RcsContactsIntentResolver;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.google.android.gms.actions.SearchIntents;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.location.LocationRequest;

public class ContactsIntentResolver {
    public static boolean INTENT_FROM_EMAIL = false;
    private final Activity mContext;
    private RcsContactsIntentResolver mRcsCust = null;

    public ContactsIntentResolver(Activity context) {
        this.mContext = context;
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mRcsCust = new RcsContactsIntentResolver();
        }
    }

    private void actionPick(ContactsRequest request, Intent intent) {
        String resolvedType = intent.resolveType(this.mContext);
        if ("vnd.android.cursor.dir/contact".equals(resolvedType)) {
            if (intent.getBooleanExtra("com.huawei.android.contacts.SelectAndCreate", false)) {
                request.setActionCode(70);
            } else {
                request.setActionCode(60);
            }
        } else if ("vnd.android.cursor.dir/person".equals(resolvedType)) {
            request.setActionCode(60);
            request.setLegacyCompatibilityMode(true);
        } else if ("vnd.android.cursor.dir/phone_v2".equals(resolvedType)) {
            request.setActionCode(90);
        } else if ("vnd.android.cursor.dir/phone".equals(resolvedType)) {
            request.setActionCode(90);
            request.setLegacyCompatibilityMode(true);
        } else if ("vnd.android.cursor.dir/postal-address_v2".equals(resolvedType)) {
            request.setActionCode(100);
        } else if ("vnd.android.cursor.dir/postal-address".equals(resolvedType)) {
            request.setActionCode(100);
            request.setLegacyCompatibilityMode(true);
        } else if ("vnd.android.cursor.dir/email_v2".equals(resolvedType)) {
            request.setActionCode(LocationRequest.PRIORITY_NO_POWER);
        }
    }

    private void actionCreateShortCut(ContactsRequest request, Intent intent) {
        String component = intent.getComponent().getClassName();
        if (component.equals("alias.DialShortcut")) {
            request.setActionCode(120);
        } else if (component.equals("alias.MessageShortcut")) {
            request.setActionCode(130);
        } else {
            request.setActionCode(110);
        }
    }

    private void actionGetContent(ContactsRequest request, Intent intent) {
        String type = intent.getType();
        if ("vnd.android.cursor.item/contact".equals(type)) {
            request.setActionCode(70);
        } else if ("vnd.android.cursor.item/phone_v2".equals(type)) {
            request.setActionCode(90);
        } else if ("vnd.android.cursor.item/phone".equals(type)) {
            request.setActionCode(90);
            request.setLegacyCompatibilityMode(true);
        } else if ("vnd.android.cursor.item/postal-address_v2".equals(type)) {
            request.setActionCode(100);
        } else if ("vnd.android.cursor.item/postal-address".equals(type)) {
            request.setActionCode(100);
            request.setLegacyCompatibilityMode(true);
        } else if ("vnd.android.cursor.item/person".equals(type)) {
            request.setActionCode(70);
            request.setLegacyCompatibilityMode(true);
        } else if (this.mRcsCust == null || !this.mRcsCust.isAnyMimeType(type)) {
            request.setValid(false);
        } else {
            this.mRcsCust.setFtVcardRequest(request);
        }
    }

    private void actionSearch(ContactsRequest request, Intent intent) {
        String query = intent.getStringExtra(SearchIntents.EXTRA_QUERY);
        if (TextUtils.isEmpty(query)) {
            query = intent.getStringExtra("phone");
        }
        if (TextUtils.isEmpty(query)) {
            query = intent.getStringExtra(Scopes.EMAIL);
        }
        request.setQueryString(query);
        request.setSearchMode(true);
    }

    private void actionView(ContactsRequest request, Intent intent) {
        String resolvedType = intent.resolveType(this.mContext);
        Uri lUri = intent.getData();
        if ("vnd.android.cursor.dir/contact".equals(resolvedType) || "vnd.android.cursor.dir/person".equals(resolvedType)) {
            request.setActionCode(16);
        } else if ("vnd.android.cursor.dir/calls".equals(resolvedType) || "vnd.android.cursor.item/voicemail".equals(resolvedType)) {
            request.setActionCode(141);
            request.setActivityTitle("com.android.contacts.activities.CallLogActivity");
        } else if (lUri == null || !"tel".equals(lUri.getScheme())) {
            request.setActionCode(140);
            request.setContactUri(lUri);
            intent.setAction("android.intent.action.VIEW");
            intent.setData(null);
        } else {
            request.setActionCode(142);
        }
    }

    public ContactsRequest resolveIntent(Intent intent) {
        ContactsRequest request = new ContactsRequest();
        String action = intent.getAction();
        if (setHAPActionCode(intent, request)) {
            return request;
        }
        String lComponentName;
        HwLog.i("ContactsIntentResolver", "Called with action: " + action);
        if ("com.android.contacts.action.LIST_DEFAULT".equals(action)) {
            request.setActionCode(10);
        } else if ("com.android.contacts.action.LIST_ALL_CONTACTS".equals(action)) {
            request.setActionCode(15);
        } else if ("com.android.contacts.action.LIST_CONTACTS_WITH_PHONES".equals(action)) {
            request.setActionCode(17);
        } else if ("com.android.contacts.action.LIST_STARRED".equals(action)) {
            request.setActionCode(30);
        } else if ("com.android.contacts.action.LIST_FREQUENT".equals(action)) {
            request.setActionCode(40);
        } else if ("com.android.contacts.action.LIST_STREQUENT".equals(action)) {
            request.setActionCode(50);
        } else if ("com.android.contacts.action.LIST_GROUP".equals(action)) {
            request.setActionCode(20);
        } else if ("android.intent.action.PICK".equals(action)) {
            actionPick(request, intent);
        } else if ("android.intent.action.CREATE_SHORTCUT".equals(action)) {
            actionCreateShortCut(request, intent);
        } else if ("android.intent.action.GET_CONTENT".equals(action)) {
            actionGetContent(request, intent);
        } else if ("android.intent.action.INSERT_OR_EDIT".equals(action)) {
            request.setActionCode(80);
        } else if ("android.intent.action.SEARCH".equals(action)) {
            actionSearch(request, intent);
        } else if ("android.intent.action.VIEW".equals(action)) {
            actionView(request, intent);
        } else if ("com.android.contacts.action.FILTER_CONTACTS".equals(action)) {
            request.setActionCode(10);
            Bundle extras = intent.getExtras();
            if (extras != null) {
                request.setQueryString(extras.getString("com.android.contacts.extra.FILTER_TEXT"));
                ContactsRequest originalRequest = (ContactsRequest) extras.get("originalRequest");
                if (originalRequest != null) {
                    request.copyFrom(originalRequest);
                }
            }
            request.setSearchMode(true);
        } else if ("android.provider.Contacts.SEARCH_SUGGESTION_CLICKED".equals(action)) {
            Uri data = intent.getData();
            request.setActionCode(140);
            request.setContactUri(data);
            request.setSearchDetail(true);
            intent.setAction("android.intent.action.VIEW");
            intent.setData(null);
        } else if ("android.intent.action.DIAL".equals(action) || "com.android.phone.action.TOUCH_DIALER".equals(action)) {
            request.setActionCode(142);
        } else if ("com.android.phone.action.RECENT_CALLS".equals(action)) {
            request.setActionCode(141);
        } else if ("android.intent.action.MAIN".equals(action)) {
            lComponentName = intent.getComponent().getClassName();
            if (lComponentName.equals("com.android.contacts.activities.DialtactsActivity") || lComponentName.equals("com.android.contacts.CalllogLauncher") || lComponentName.equals("com.android.contacts.FavoritesLauncher") || lComponentName.equals("com.android.contacts.activities.PeopleActivity")) {
                request.setActionCode(143);
                request.setActivityTitle(lComponentName);
            }
        } else if ("com.huawei.android.intent.action.CALL".equals(action)) {
            request.setActionCode(141);
        } else if ("android.intent.action.HAP_ADD_COMPANY_MEMBERS".equals(action)) {
            request.setActionCode(144);
        } else if ("com.android.contacts.action.LIST_CONTACTS".equals(action)) {
            request.setActionCode(145);
        } else if (this.mRcsCust != null) {
            this.mRcsCust.resolveIntent(intent, request, this.mContext);
        }
        String title = intent.getStringExtra("com.android.contacts.extra.TITLE_EXTRA");
        if (title != null) {
            request.setActivityTitle(title);
        } else if ("android.intent.action.SEARCH".equals(action)) {
            request.setActivityTitle(this.mContext.getString(R.string.contact_hint_findContacts));
        } else if (TextUtils.isEmpty(request.getActivityTitle())) {
            lComponentName = intent.getComponent().getClassName();
            request.setActivityTitle(lComponentName);
            request.setDescription(getActivityDescription(lComponentName));
        } else {
            request.setDescription(getActivityDescription(intent.getComponent().getClassName()));
        }
        return request;
    }

    private String getActivityDescription(String activityName) {
        if (this.mContext == null) {
            return null;
        }
        if ("com.android.contacts.activities.DialtactsActivity".equals(activityName)) {
            return this.mContext.getString(R.string.dialer);
        }
        if ("com.android.contacts.activities.PeopleActivity".equals(activityName)) {
            return this.mContext.getString(R.string.contactsList);
        }
        return null;
    }

    private boolean setHAPActionCode(Intent aIntent, ContactsRequest aRequest) {
        String lAction = aIntent.getAction();
        if ("com.huawei.community.action.MULTIPLE_PICK".equals(lAction)) {
            String type = aIntent.resolveType(this.mContext);
            if ("vnd.android.cursor.item/email_v2".equals(type) || "vnd.android.cursor.dir/email_v2".equals(type)) {
                INTENT_FROM_EMAIL = true;
                aRequest.setActionCode(212);
            } else {
                INTENT_FROM_EMAIL = false;
                if ("vnd.android.cursor.dir/phone_v2".equals(type) || "vnd.android.cursor.item/phone_v2".equals(type)) {
                    if (aIntent.getBooleanExtra("com.huawei.community.action.ADD_EMAIL", false)) {
                        aRequest.setActionCode(210);
                    } else {
                        aRequest.setActionCode(211);
                    }
                } else if ("vnd.android.cursor.item/himessage".equals(type)) {
                    aRequest.setActionCode(213);
                } else if ("vnd.android.cursor.item/rcs".equals(type)) {
                    aRequest.setActionCode(214);
                } else if ("vnd.android.cursor.item/phone_mail_rcse_msgplus".equals(type)) {
                    aRequest.setActionCode(215);
                } else if ("vnd.android.cursor.item/phone_rcse_msgplus".equals(type)) {
                    aRequest.setActionCode(216);
                } else if (this.mRcsCust == null || !this.mRcsCust.isRcsContact4MsgMimeType(type)) {
                    aRequest.setValid(false);
                } else {
                    this.mRcsCust.setHAPActionCode(aRequest);
                }
            }
            return true;
        } else if ("android.intent.action.HAP_ADD_FAVORITES".equals(lAction)) {
            aRequest.setActionCode(201);
            return true;
        } else if ("android.intent.action.HAP_ADD_GROUP_MEMBERS".equals(lAction)) {
            aRequest.setActionCode(205);
            return true;
        } else if ("android.intent.action.HAP_REMOVE_GROUP_MEMBERS".equals(lAction)) {
            aRequest.setActionCode(221);
            return true;
        } else if ("android.intent.action.HAP_COPY_FROM_SIM".equals(lAction)) {
            aRequest.setActionCode(207);
            return true;
        } else if ("android.intent.action.HAP_COPY_TO_ACCOUNT".equals(lAction)) {
            if (aIntent.getBooleanExtra("export_to_sim", false)) {
                aRequest.setActionCode(218);
            } else {
                aRequest.setActionCode(206);
            }
            return true;
        } else if ("android.intent.action.HAP_DELETE_CONTACTS".equals(lAction)) {
            aRequest.setActionCode(203);
            return true;
        } else if ("android.intent.action.HAP_SHARE_CONTACTS".equals(lAction)) {
            aRequest.setActionCode(204);
            return true;
        } else if ("android.intent.action.HAP_REMOVE_FAVORITES".equals(lAction)) {
            aRequest.setActionCode(202);
            return true;
        } else if ("android.intent.action.HAP_SEND_GROUP_MESSAGE".equals(lAction)) {
            if (aIntent.getParcelableExtra("extra_group_uri") == null) {
                aRequest.setValid(false);
            } else {
                aRequest.setActionCode(208);
            }
            return true;
        } else if ("android.intent.action.HAP_SEND_GROUP_MAIL".equals(lAction)) {
            if (aIntent.getParcelableExtra("extra_group_uri") == null) {
                aRequest.setValid(false);
            } else {
                aRequest.setActionCode(209);
            }
            return true;
        } else if ("android.intent.action.HAP_EXPORT_CONTACTS".equals(lAction)) {
            aRequest.setActionCode(217);
            return true;
        } else if ("com.huawei.android.groups.multiple.delete".equals(lAction)) {
            aRequest.setActionCode(219);
            return true;
        } else if (!"com.huawei.android.intent.action.ADD_PRIVATE_CONTACT".equals(lAction)) {
            return false;
        } else {
            aRequest.setActionCode(220);
            return true;
        }
    }
}
