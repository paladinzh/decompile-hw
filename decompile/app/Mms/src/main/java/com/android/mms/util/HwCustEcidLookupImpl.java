package com.android.mms.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Telephony.Mms.Inbox;
import android.provider.Telephony.Sms;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import com.android.mms.data.Contact;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.MessageUtils;
import com.huawei.mms.ui.EmuiAvatarImage;
import com.huawei.mms.util.ResEx;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;

public class HwCustEcidLookupImpl extends HwCustEcidLookup {
    private static final boolean DEBUG = false;
    private static final String ECID_AOSP_VERSION = "1.5.1.0";
    private static final Uri LOOKUP_URI = Uri.parse("content://com.cequint.ecid/smslookup");
    private static final int MAX_NOTIFICATION_DELAY = 10000;
    private static final String TAG = "EcidLookup";
    private static boolean excludeEcidLookup = SystemProperties.getBoolean("ro.config.att_nameId", false);
    private static HashMap<String, EcidContact> sCache;
    private static BitmapDrawable sEcidLogo;
    private static EcidContentObserver sEcidObserver;
    private static Handler sHandler;
    private static HashSet<String> sSenders;

    private static class EcidContact {
        String name;
        Runnable pendingNotification;
        String pictureUri;
        boolean queryPending;

        public EcidContact(String n, String pUri, boolean qp, Runnable pn) {
            this.name = n;
            this.pictureUri = pUri;
            this.queryPending = qp;
            this.pendingNotification = pn;
        }

        public EcidContact(String n) {
            this(n, null, false, null);
        }
    }

    private static class EcidContentObserver extends ContentObserver {
        public EcidContentObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                HwCustEcidLookupImpl.invalidateCache(uri.getLastPathSegment());
            }
        }
    }

    public void init(Context aContext) {
        if (excludeEcidLookup && aContext != null) {
            doInit(aContext);
        }
    }

    public boolean getNameIdFeatureEnable() {
        return excludeEcidLookup;
    }

    public void setContactAsStale(Contact aContact, String aNumber) {
        if (excludeEcidLookup && aContact != null && !aContact.isStale() && !aContact.isQueryPending() && hasUpdateFor(aNumber)) {
            aContact.setIsStale(true);
        }
    }

    private boolean hasUpdateFor(String number) {
        if (isValidPhoneNumber(number)) {
            return hasUpdateForImpl(number);
        }
        return false;
    }

    public void setEcidContactName(Context aContext, String aNumber, Contact aContact) {
        if (excludeEcidLookup && aContext != null && !TextUtils.isEmpty(aNumber) && aContact != null) {
            String ecidName = getEcidName(aContext.getContentResolver(), aNumber);
            if (!TextUtils.isEmpty(ecidName)) {
                aContact.setName(ecidName);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setEcidProfilePicture(Context aContext, String aNumber, EmuiAvatarImage aAvatarImage) {
        if (!(!excludeEcidLookup || aContext == null || TextUtils.isEmpty(aNumber) || aAvatarImage == null || TextUtils.isEmpty(getEcidName(aContext.getContentResolver(), aNumber)))) {
            BitmapDrawable ecidDrawbale = getEcidDrawableIfExists(aContext, aNumber);
            if (ecidDrawbale != null) {
                aAvatarImage.setImageDrawable(ecidDrawbale);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Drawable getEcidDrawableIfExists(Context context, String number, Drawable aDrawable) {
        if (!excludeEcidLookup || context == null || !isValidPhoneNumber(number)) {
            return aDrawable;
        }
        Drawable lDrawable = getEcidImageIfExistsImpl(context, number);
        if (lDrawable == null) {
            lDrawable = aDrawable;
        }
        return lDrawable;
    }

    public Bitmap getEcidNotificationAvatar(Context aContext, String aNumber, Bitmap aAvatar) {
        if (!excludeEcidLookup || aContext == null || TextUtils.isEmpty(aNumber) || aAvatar != null) {
            return aAvatar;
        }
        Bitmap newAvatar = getEcidImageIfExists(aContext, aNumber);
        if (newAvatar != null) {
            aAvatar = ResEx.duplicateBitmap(newAvatar, 255);
        }
        return aAvatar;
    }

    public void addSender(Context aContext, Uri aUri) {
        if (excludeEcidLookup && aContext != null && aUri != null) {
            String addr = addSenderImpl(aContext, aUri);
            if (!TextUtils.isEmpty(addr)) {
                Contact.get(addr, false);
            }
        }
    }

    public void addSender(String number) {
        if (excludeEcidLookup && !TextUtils.isEmpty(number)) {
            addSenderImpl(number);
        }
    }

    private boolean shouldWaitForEcidName(String number) {
        if (isValidPhoneNumber(number)) {
            return shouldWaitForEcidNameImpl(number);
        }
        return false;
    }

    private boolean shouldWaitForEcidName(Context aContext, Uri aUri) {
        if (aContext == null || aUri == null) {
            return false;
        }
        return shouldWaitForEcidNameImpl(aContext, aUri);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean delayedNotification(Context aContext, long threadId, boolean isStatusMessage, String addr) {
        if (!excludeEcidLookup || aContext == null || !shouldWaitForEcidName(addr)) {
            return false;
        }
        delayedNotificationImpl(aContext, threadId, isStatusMessage, addr);
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean delayedNotification(Context aContext, long threadId, boolean isStatusMessage, Uri aUri) {
        if (!excludeEcidLookup || aContext == null || aUri == null || !shouldWaitForEcidName(aContext, aUri)) {
            return false;
        }
        delayedNotificationImpl(aContext, threadId, isStatusMessage, aUri);
        return true;
    }

    public String getEcidName(ContentResolver aCr, String aNumber, String aTitle) {
        if (!excludeEcidLookup || aCr == null || !isValidPhoneNumber(aNumber)) {
            return aTitle;
        }
        if (sCache != null && sSenders != null) {
            return getEcidNameImpl(aCr, aNumber, aTitle);
        }
        Log.e(TAG, "EcidLookup not initialized!");
        return aTitle;
    }

    private String getEcidName(ContentResolver aCr, String aNumber) {
        if (aCr == null) {
            return "";
        }
        if (sCache != null && sSenders != null) {
            return getEcidNameImpl(aCr, aNumber, "");
        }
        Log.e(TAG, "EcidLookup not initialized!");
        return "";
    }

    private Bitmap getEcidImageIfExists(Context context, String number) {
        if (!isValidPhoneNumber(number)) {
            return null;
        }
        BitmapDrawable pic = getEcidImageIfExistsImpl(context, number);
        if (pic != null) {
            return pic.getBitmap();
        }
        return null;
    }

    private BitmapDrawable getEcidDrawableIfExists(Context context, String number) {
        if (isValidPhoneNumber(number)) {
            return getEcidImageIfExistsImpl(context, number);
        }
        return null;
    }

    private static void doInit(final Context context) {
        sCache = new HashMap();
        sSenders = new HashSet();
        sEcidObserver = new EcidContentObserver();
        sHandler = new Handler();
        context.getContentResolver().registerContentObserver(LOOKUP_URI, true, sEcidObserver);
        new Thread(new Runnable() {
            public void run() {
                try {
                    HwCustEcidLookupImpl.initSendersCache(context);
                } catch (Exception e) {
                    Log.e(HwCustEcidLookupImpl.TAG, "ERROR: init failed: " + e.getMessage());
                }
            }
        }, "EcidLookup.init").start();
    }

    private static void initSendersCache(Context context) {
        Cursor c = context.getContentResolver().query(Sms.CONTENT_URI, new String[]{"address"}, "type='1' OR type='0'", null, null);
        if (c != null) {
            try {
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    addSenderImpl(c.getString(0));
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                } else {
                    return;
                }
            }
        }
        if (c != null) {
            c.close();
            c = context.getContentResolver().query(Inbox.CONTENT_URI, new String[]{"_id"}, null, null, null);
            if (c != null) {
                try {
                    c.moveToPosition(-1);
                    while (c.moveToNext()) {
                        String id = c.getString(0);
                        if (!TextUtils.isEmpty(id)) {
                            addSenderImpl(context, Inbox.CONTENT_URI.buildUpon().appendPath(id).build());
                        }
                    }
                } catch (Throwable th2) {
                    if (c != null) {
                        c.close();
                    }
                }
            }
            if (c != null) {
                c.close();
            }
        }
    }

    private static void addSenderImpl(String number) {
        if (isValidPhoneNumber(number)) {
            number = normalizeNumber(number);
            synchronized (sSenders) {
                sSenders.add(number);
            }
        }
    }

    private static String addSenderImpl(Context context, Uri uri) {
        String addr = AddressUtils.getFrom(context, uri);
        addSenderImpl(addr);
        if (isValidPhoneNumber(addr)) {
            return addr;
        }
        return "";
    }

    private static boolean isValidPhoneNumber(String number) {
        boolean z = false;
        if (TextUtils.isEmpty(number) || number.indexOf("@") >= 0 || MessageUtils.isAlias(number) || !PhoneNumberUtils.isWellFormedSmsAddress(number)) {
            return false;
        }
        number = PhoneNumberUtils.extractNetworkPortion(number);
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        if (number.length() >= 7) {
            z = true;
        }
        return z;
    }

    private static String normalizeNumber(String number) {
        number = PhoneNumberUtils.normalizeNumber(PhoneNumberUtils.stripSeparators(PhoneNumberUtils.extractNetworkPortion(number)));
        if (number == null) {
            return number;
        }
        if (number.startsWith("+")) {
            number = number.substring(1);
        }
        if (number.startsWith("1")) {
            number = number.substring(1);
        }
        return number;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean hasUpdateForImpl(String number) {
        number = normalizeNumber(number);
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        synchronized (sSenders) {
            if (!sSenders.contains(number)) {
                return false;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean shouldWaitForEcidNameImpl(String number) {
        boolean z = false;
        number = normalizeNumber(number);
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        synchronized (sSenders) {
            if (!sSenders.contains(number)) {
                return false;
            }
        }
    }

    private static boolean shouldWaitForEcidNameImpl(Context context, Uri uri) {
        return shouldWaitForEcidNameImpl(AddressUtils.getFrom(context, uri));
    }

    private static void delayedNotificationImpl(Context context, long threadId, boolean isStatusMessage, String addr) {
        final String number = normalizeNumber(addr);
        final Context context2 = context;
        final long j = threadId;
        final boolean z = isStatusMessage;
        Runnable r = new Runnable() {
            public void run() {
                synchronized (HwCustEcidLookupImpl.sCache) {
                    EcidContact myRec = (EcidContact) HwCustEcidLookupImpl.sCache.get(number);
                    if (myRec != null) {
                        myRec.queryPending = false;
                        myRec.pendingNotification = null;
                    }
                }
                MessagingNotification.nonBlockingUpdateNewMessageIndicator(context2, j, z);
            }
        };
        synchronized (sCache) {
            if (sCache.containsKey(number)) {
                EcidContact rec = (EcidContact) sCache.get(number);
                if (rec.pendingNotification != null) {
                    return;
                }
                rec.pendingNotification = r;
                sHandler.postDelayed(r, 10000);
                return;
            }
        }
    }

    private static void delayedNotificationImpl(Context context, long threadId, boolean isStatusMessage, Uri uri) {
        String addr = AddressUtils.getFrom(context, uri);
        if (!TextUtils.isEmpty(addr)) {
            delayedNotificationImpl(context, threadId, isStatusMessage, addr);
        }
    }

    private static BitmapDrawable getEcidImageIfExistsImpl(Context context, String number) {
        number = normalizeNumber(number);
        BitmapDrawable pic = null;
        synchronized (sCache) {
            EcidContact rec = (EcidContact) sCache.get(number);
            if (!(rec == null || TextUtils.isEmpty(rec.pictureUri))) {
                if (sEcidLogo == null) {
                    sEcidLogo = getImage(context, rec.pictureUri);
                }
                pic = sEcidLogo;
            }
        }
        return pic;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String getEcidNameImpl(ContentResolver cr, String number, String aTitle) {
        if (!isValidPhoneNumber(number)) {
            return aTitle;
        }
        number = normalizeNumber(number);
        synchronized (sCache) {
            if (sCache.containsKey(number)) {
                String lName = ((EcidContact) sCache.get(number)).name;
                if (!TextUtils.isEmpty(lName)) {
                    aTitle = lName;
                }
            }
        }
    }

    private static EcidContact getEcidContactFromCursor(Cursor c) {
        EcidContact ecidname = new EcidContact(null, null, false, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    String cname = getString(c, c.getColumnIndex("cid_pName"));
                    String picUri = getString(c, c.getColumnIndex("cid_pImage"));
                    String bizName = getString(c, c.getColumnIndex("cid_pCompany"));
                    String lastName = getString(c, c.getColumnIndex("cid_pLastName"));
                    String firstName = getString(c, c.getColumnIndex("cid_pFirstName"));
                    String displayName = getString(c, c.getColumnIndex("cid_pDisplayName"));
                    ecidname.queryPending = getBool(c, c.getColumnIndex("cid_pTempValue"));
                    ecidname.pictureUri = picUri;
                    if (!TextUtils.isEmpty(displayName)) {
                        ecidname.name = displayName;
                    } else if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)) {
                        ecidname.name = firstName + " " + lastName;
                    } else if (!TextUtils.isEmpty(firstName)) {
                        ecidname.name = firstName;
                    } else if (!TextUtils.isEmpty(lastName)) {
                        ecidname.name = lastName;
                    } else if (!TextUtils.isEmpty(bizName)) {
                        ecidname.name = bizName;
                    } else if (!TextUtils.isEmpty(cname)) {
                        ecidname.name = cname;
                    }
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
            }
        }
        if (c != null) {
            c.close();
        }
        return ecidname;
    }

    private static Cursor doLookup(ContentResolver cr, String number) {
        Cursor c = null;
        try {
            c = cr.query(LOOKUP_URI, null, number, new String[]{"user"}, null);
        } catch (Exception e) {
        }
        return c;
    }

    private static String getString(Cursor c, int col) {
        return (col <= -1 || c.isNull(col)) ? null : c.getString(col);
    }

    private static boolean getBool(Cursor c, int col) {
        int r = (col <= -1 || c.isNull(col)) ? 0 : c.getInt(col);
        return r == 1;
    }

    private static void invalidateCache(String id) {
        Runnable pendingMsg = null;
        synchronized (sCache) {
            if (TextUtils.isEmpty(id) || id.equals(LOOKUP_URI.getLastPathSegment())) {
                sCache.clear();
            } else {
                EcidContact rec = (EcidContact) sCache.remove(id);
                if (rec != null) {
                    pendingMsg = rec.pendingNotification;
                }
            }
        }
        if (pendingMsg != null) {
            sHandler.removeCallbacks(pendingMsg);
            pendingMsg.run();
        }
    }

    private static BitmapDrawable getImage(Context context, String uri) {
        if (context == null || TextUtils.isEmpty(uri)) {
            return null;
        }
        BitmapDrawable bitmapDrawable = null;
        try {
            Bitmap bmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(Uri.parse(uri)));
            Resources res = context.getResources();
            int notiHeight = res.getDimensionPixelSize(17104902);
            int notiWidth = res.getDimensionPixelSize(17104901);
            if (bmap.getHeight() > notiHeight || bmap.getWidth() > notiWidth) {
                bmap = Bitmap.createScaledBitmap(bmap, notiHeight, notiWidth, true);
            }
            bitmapDrawable = new BitmapDrawable(context.getResources(), bmap);
        } catch (FileNotFoundException e) {
        }
        return bitmapDrawable;
    }
}
