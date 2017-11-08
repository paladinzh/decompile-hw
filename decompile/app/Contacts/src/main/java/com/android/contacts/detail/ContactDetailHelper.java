package com.android.contacts.detail;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ServiceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.CallUtil;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsUtils;
import com.android.contacts.activities.ContactInfoFragment;
import com.android.contacts.calllog.IntentProvider;
import com.android.contacts.detail.ContactDetailAdapter.DetailViewEntry;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.blacklist.BlacklistCommonUtils;
import com.android.contacts.hap.provider.ContactsAppProvider;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.list.ShortcutIntentBuilder;
import com.android.contacts.list.ShortcutIntentBuilder.OnShortcutIntentCreatedListener;
import com.android.contacts.model.Contact;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneNumberFormatter;
import com.android.contacts.util.TextUtil;
import com.google.android.gms.R;
import com.huawei.cust.HwCustUtils;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService.Stub;
import java.util.ArrayList;

public class ContactDetailHelper {
    private static HwCustContactDetailHelper mContactDetailHelper = ((HwCustContactDetailHelper) HwCustUtils.createObj(HwCustContactDetailHelper.class, new Object[0]));

    public static class ContactRoamingInfo {
        private String normalizedNumber;
        private String number;

        public ContactRoamingInfo(String number, String normalizedNumber) {
            this.number = number;
            this.normalizedNumber = normalizedNumber;
        }
    }

    public static void handleBlackListMenuAction(CharSequence aMenuTitle, Fragment fragment, FragmentManager fragmentManager, ArrayList<String> phoneNumberList, String contactName) {
        IHarassmentInterceptionService mService = Stub.asInterface(ServiceManager.getService("com.huawei.harassmentinterception.service.HarassmentInterceptionService"));
        if (aMenuTitle.equals(fragment.getActivity().getText(R.string.contact_menu_add_to_blacklist))) {
            if (fragment instanceof ContactInfoFragment) {
                if (((ContactInfoFragment) fragment).getIntent().getBooleanExtra("INTENT_FROM_DIALER", false)) {
                    StatisticalHelper.report(1174);
                } else {
                    StatisticalHelper.report(1117);
                }
                BlacklistCommonUtils.handleNumberBlockList(fragment.getActivity(), mService, phoneNumberList, contactName, 0);
            }
        } else if (aMenuTitle.equals(fragment.getActivity().getResources().getText(R.string.contact_menu_remove_from_blacklist))) {
            StatisticalHelper.report(1205);
            BlacklistCommonUtils.handleNumberBlockList(fragment.getActivity(), mService, phoneNumberList, contactName, 1);
        }
    }

    public static void createLauncherShortcutWithContact(Activity aActivity, Uri aLookupUri) {
        createLauncherShortcutWithContact(aActivity, aLookupUri, null, null);
    }

    public static void createLauncherShortcutWithContact(Activity aActivity, Uri aLookupUri, String shortcutAction) {
        createLauncherShortcutWithContact(aActivity, aLookupUri, shortcutAction, null);
    }

    public static void createLauncherShortcutWithContact(final Activity aActivity, Uri aLookupUri, final String shortcutAction, final ContactRoamingInfo numberObject) {
        Activity parentActivity = aActivity;
        ShortcutIntentBuilder builder = new ShortcutIntentBuilder(aActivity, new OnShortcutIntentCreatedListener() {
            public void onShortcutIntentCreated(Uri uri, Intent shortcutIntent) {
                shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                if (("com.android.contacts.action.CHOOSE_SUB".equals(shortcutAction) || "com.android.contacts.action.CHOOSE_SUB_HUAWEI".equals(shortcutAction)) && numberObject != null) {
                    Intent roamingIntent = (Intent) shortcutIntent.getParcelableExtra("android.intent.extra.shortcut.INTENT");
                    IntentProvider.addRoamingDataIntent(roamingIntent, shortcutIntent.getStringExtra("android.intent.extra.shortcut.NAME"), numberObject.normalizedNumber, null, null, 0);
                    shortcutIntent.removeExtra("android.intent.extra.shortcut.INTENT");
                    shortcutIntent.putExtra("android.intent.extra.shortcut.INTENT", roamingIntent);
                }
                aActivity.sendBroadcast(shortcutIntent);
                if (EmuiFeatureManager.isProductCustFeatureEnable() && ContactDetailHelper.mContactDetailHelper != null) {
                    ContactDetailHelper.mContactDetailHelper.showContactsShortcutToast(shortcutIntent, aActivity);
                }
                StatisticalHelper.report(1118);
            }
        });
        if (shortcutAction == null) {
            builder.createContactShortcutIntent(aLookupUri);
        } else if (numberObject == null || TextUtils.isEmpty(numberObject.number)) {
            builder.createPhoneNumberShortcutIntent(aLookupUri, shortcutAction);
        } else {
            builder.createContactPhoneNumberShortcutIntent(aLookupUri, shortcutAction, numberObject.number);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void shareContact(Contact mContactData, Context context) {
        synchronized (ContactDetailHelper.class) {
            if (mContactData == null || context == null) {
            } else {
                Uri shareUri;
                if (mContactData.isYellowPage()) {
                    shareUri = ContactsAppProvider.YELLOW_PAGE_AS_CARD_URI.buildUpon().appendEncodedPath(String.valueOf(mContactData.getId())).build();
                } else {
                    shareUri = Uri.withAppendedPath(Contacts.CONTENT_VCARD_URI, Uri.encode(mContactData.getLookupKey()));
                    if (mContactData.isUserProfile()) {
                        shareUri = getPreAuthorizedUri(shareUri, context);
                    }
                }
                Intent intent = new Intent("android.intent.action.SEND");
                intent.setType("text/x-vcard");
                intent.putExtra("android.intent.extra.STREAM", shareUri);
                intent.putExtra("from_contact_profile", true);
                try {
                    context.startActivity(Intent.createChooser(intent, context.getText(R.string.contact_menu_share_contacts)));
                    StatisticalHelper.report(1113);
                } catch (ActivityNotFoundException e) {
                    HwLog.e("ContactDetailHelper", "No activity found for intent: " + intent);
                    Toast.makeText(context, R.string.quickcontact_missing_app_Toast, 0).show();
                }
            }
        }
    }

    private static Uri getPreAuthorizedUri(Uri uri, Context context) {
        Bundle uriBundle = new Bundle();
        uriBundle.putParcelable("uri_to_authorize", uri);
        Bundle authResponse = context.getContentResolver().call(ContactsContract.AUTHORITY_URI, "authorize", null, uriBundle);
        if (authResponse != null) {
            return (Uri) authResponse.getParcelable("authorized_uri");
        }
        return uri;
    }

    public static void starContact(MenuItem menuStar, Uri lookupUri, Contact contact, Context context, TextView nameTextView, View imageView) {
        boolean z = false;
        if (lookupUri != null && contact != null) {
            boolean z2;
            boolean isStarred = contact.getStarred();
            boolean isDirectoryEntry = contact.isDirectoryEntry();
            boolean isUserProfile = contact.isUserProfile();
            if (isStarred) {
                z2 = false;
            } else {
                z2 = true;
            }
            ContactDetailDisplayUtils.configureStarredMenuItem(menuStar, isDirectoryEntry, isUserProfile, z2);
            Context applicationContext = context.getApplicationContext();
            if (!isStarred) {
                z = true;
            }
            Intent intent = ContactSaveService.createSetStarredIntent(applicationContext, lookupUri, z);
            StatisticalHelper.report(!isStarred ? 1104 : 1105);
            context.startService(intent);
        }
    }

    public static boolean isContactJoined(Contact contact) {
        if (contact == null || contact.getRawContacts().size() <= 1) {
            return false;
        }
        return true;
    }

    public static void dialOutByProximity(String number, boolean isMultiSimEnable, Context context, boolean hasCallLogRecord, int lastCallSimType) {
        String phoneNumber = PhoneNumberFormatter.parsePhoneNumber(number);
        if (phoneNumber != null && ContactsUtils.okToDialByMotion(phoneNumber, 1, context.getApplicationContext())) {
            if (isMultiSimEnable) {
                int chooseSlot;
                if (SimFactoryManager.isExtremeSimplicityMode()) {
                    chooseSlot = SimFactoryManager.getDefaultSimcard();
                } else if (hasCallLogRecord) {
                    chooseSlot = lastCallSimType;
                } else {
                    chooseSlot = SimFactoryManager.getSlotidBasedOnSubscription(SimFactoryManager.getDefaultSubscription());
                }
                boolean okToCallByCurrentSlot = SimFactoryManager.isSimEnabled(chooseSlot);
                if (!okToCallByCurrentSlot) {
                    if (chooseSlot == 0) {
                        chooseSlot = 1;
                    } else {
                        chooseSlot = 0;
                    }
                    okToCallByCurrentSlot = SimFactoryManager.isSimEnabled(chooseSlot);
                }
                if (okToCallByCurrentSlot) {
                    if (HwLog.HWFLOW) {
                        HwLog.i("ContactDetailHelper", "start dial by motion :" + chooseSlot);
                    }
                    CommonUtilMethods.dialNumberByProximity(context, Uri.fromParts("tel", phoneNumber, null), SimFactoryManager.getSubscriptionIdBasedOnSlot(chooseSlot), true, true);
                }
            } else if (SimFactoryManager.isSimEnabled(0)) {
                HwLog.d("ContactDetailHelper", "dialOutByProximity");
                Intent intent = CallUtil.getCallIntent(phoneNumber, 0);
                intent.putExtra("dial_by_proximity", true);
                context.startActivity(intent);
                StatisticalHelper.reportDialPortal(context.getApplicationContext(), 1);
            }
        }
    }

    public static boolean isEntryAlreadyExisted(ArrayList<DetailViewEntry> entryList, DetailViewEntry entry) {
        int size = entryList.size();
        if (size < 1) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            DetailViewEntry tempEntry = (DetailViewEntry) entryList.get(i);
            if (TextUtil.stringOrNullEquals(entry.kind, tempEntry.kind) && entry.type == tempEntry.type && TextUtil.stringOrNullEquals(entry.data, tempEntry.data)) {
                return true;
            }
        }
        return false;
    }

    public static void starContact(View item, Uri lookupUri, Contact contact, Context context, TextView nameTextView, View imageView) {
        boolean z = false;
        if (lookupUri != null && contact != null) {
            boolean z2;
            boolean isStarred = contact.getStarred();
            boolean isDirectoryEntry = contact.isDirectoryEntry();
            boolean isUserProfile = contact.isUserProfile();
            if (isStarred) {
                z2 = false;
            } else {
                z2 = true;
            }
            ContactDetailDisplayUtils.configureStarredMenuItem(context, item, isDirectoryEntry, isUserProfile, z2);
            Context applicationContext = context.getApplicationContext();
            if (!isStarred) {
                z = true;
            }
            Intent intent = ContactSaveService.createSetStarredIntent(applicationContext, lookupUri, z);
            StatisticalHelper.report(!isStarred ? 1104 : 1105);
            context.startService(intent);
        }
    }
}
