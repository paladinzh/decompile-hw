package com.android.contacts.profile;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.ParseException;
import android.net.Uri;
import android.net.WebAddress;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Profile;
import android.support.v4.content.FileProvider;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import com.android.contacts.CallUtil;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.ContactsUtils;
import com.android.contacts.detail.ContactDetailAdapter.DetailViewEntry;
import com.android.contacts.detail.ContactDetailDisplayUtils;
import com.android.contacts.detail.ContactDetailHelper;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.roaming.IsPhoneNetworkRoamingUtils;
import com.android.contacts.hap.roaming.RoamingPhoneGatherUtils;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.Contact;
import com.android.contacts.model.RawContact;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.dataitem.DataItem;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.model.dataitem.EmailDataItem;
import com.android.contacts.model.dataitem.EventDataItem;
import com.android.contacts.model.dataitem.ImDataItem;
import com.android.contacts.model.dataitem.NicknameDataItem;
import com.android.contacts.model.dataitem.NoteDataItem;
import com.android.contacts.model.dataitem.OrganizationDataItem;
import com.android.contacts.model.dataitem.PhoneDataItem;
import com.android.contacts.model.dataitem.RelationDataItem;
import com.android.contacts.model.dataitem.SipAddressDataItem;
import com.android.contacts.model.dataitem.StructuredNameDataItem;
import com.android.contacts.model.dataitem.StructuredPostalDataItem;
import com.android.contacts.model.dataitem.WebsiteDataItem;
import com.android.contacts.util.ContactLoaderUtils;
import com.android.contacts.util.DataStatus;
import com.android.contacts.util.DateUtils;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.LunarUtils;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.PhoneNumberFormatter;
import com.android.contacts.util.StructuredPostalUtils;
import com.google.android.gms.R;
import com.google.android.gms.actions.SearchIntents;
import com.google.android.gms.common.Scopes;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileUtils {
    private static final /* synthetic */ int[] -com-android-contacts-profile-ProfileUtils$HalfTypeSwitchesValues = null;
    private static String TAG = ProfileUtils.class.getSimpleName();
    private static boolean mHasPhone;
    private static boolean mHasSip;

    public interface ProfileListener {
        void deleteProfile();
    }

    public static class ContactEntriesObject {
        public ArrayList<DetailViewEntry> companyEntries = new ArrayList();
        public ArrayList<DetailViewEntry> emailEntries = new ArrayList();
        public ArrayList<DetailViewEntry> eventEntries = new ArrayList();
        public ArrayList<DetailViewEntry> imEntries = new ArrayList();
        public ArrayList<DetailViewEntry> nameEntries = new ArrayList();
        public ArrayList<DetailViewEntry> nicknameEntries = new ArrayList();
        public ArrayList<DetailViewEntry> noteEntries = new ArrayList();
        private final Map<AccountType, List<DetailViewEntry>> otherEntriesMap = new HashMap();
        public ArrayList<DetailViewEntry> phoneEntries = new ArrayList();
        public ArrayList<DetailViewEntry> phoneticNameEntries = new ArrayList();
        public ArrayList<DetailViewEntry> positionEntries = new ArrayList();
        public ArrayList<DetailViewEntry> postalEntries = new ArrayList();
        private ArrayList<String> qrCompanys = new ArrayList();
        private ArrayList<String> qrEmails = new ArrayList();
        private ArrayList<String> qrIms = new ArrayList();
        private ArrayList<String> qrJobTitles = new ArrayList();
        private ArrayList<String> qrNames = new ArrayList();
        private ArrayList<String> qrNotes = new ArrayList();
        private ArrayList<String> qrNumbers = new ArrayList();
        private ArrayList<String> qrPostals = new ArrayList();
        private ArrayList<String> qrWebsites = new ArrayList();
        public HashMap<String, ArrayList<String>> qrcodeDataInfo = new HashMap();
        public ArrayList<DetailViewEntry> relationEntries = new ArrayList();
        public ArrayList<DetailViewEntry> sipEntries = new ArrayList();
        private ArrayList<DetailViewEntry> statusEntriesForIm = new ArrayList();
        public ArrayList<DetailViewEntry> websiteEntries = new ArrayList();
    }

    public enum HalfType {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        ALL
    }

    private static /* synthetic */ int[] -getcom-android-contacts-profile-ProfileUtils$HalfTypeSwitchesValues() {
        if (-com-android-contacts-profile-ProfileUtils$HalfTypeSwitchesValues != null) {
            return -com-android-contacts-profile-ProfileUtils$HalfTypeSwitchesValues;
        }
        int[] iArr = new int[HalfType.values().length];
        try {
            iArr[HalfType.ALL.ordinal()] = 5;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[HalfType.BOTTOM.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[HalfType.LEFT.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[HalfType.RIGHT.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[HalfType.TOP.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        -com-android-contacts-profile-ProfileUtils$HalfTypeSwitchesValues = iArr;
        return iArr;
    }

    public static ContactEntriesObject buildContactData(Contact contactData, Context context) {
        mHasPhone = PhoneCapabilityTester.isPhone(context);
        mHasSip = PhoneCapabilityTester.isSipPhone(context);
        if (contactData == null || contactData.getRawContacts() == null) {
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "buildEntries() contactData == null");
            }
            return null;
        }
        ContactEntriesObject entriesObject = new ContactEntriesObject();
        entriesObject.qrcodeDataInfo.clear();
        entriesObject.qrNumbers.clear();
        entriesObject.qrNames.clear();
        entriesObject.qrEmails.clear();
        entriesObject.qrPostals.clear();
        entriesObject.qrCompanys.clear();
        entriesObject.qrJobTitles.clear();
        entriesObject.qrWebsites.clear();
        entriesObject.qrNotes.clear();
        entriesObject.qrIms.clear();
        entriesObject.phoneEntries.clear();
        for (RawContact rawContact : contactData.getRawContacts()) {
            long rawContactId = rawContact.getId().longValue();
            AccountType accountType = rawContact.getAccountType(context);
            for (DataItem dataItem : rawContact.getDataItems()) {
                DetailViewEntry entry;
                dataItem.setRawContactId(rawContactId);
                if (dataItem.getMimeType() != null) {
                    DataKind kind = AccountTypeManager.getInstance(context).getKindOrFallback(accountType, dataItem.getMimeType());
                    if (kind != null) {
                        entry = DetailViewEntry.fromValues(context, dataItem, contactData.isDirectoryEntry(), contactData.getDirectoryId(), kind);
                        entry.maxLines = kind.maxLinesForDisplay;
                        boolean hasData = !TextUtils.isEmpty(entry.data);
                        boolean isSuperPrimary = dataItem.isSuperPrimary();
                        if ((dataItem instanceof StructuredNameDataItem) || "vnd.android.huawei.cursor.item/ringtone".equals(dataItem.getMimeType())) {
                            if (dataItem instanceof StructuredNameDataItem) {
                                entriesObject.qrNames.add(entry.data);
                                entriesObject.nameEntries.add(entry);
                            }
                        } else if ((dataItem instanceof PhoneDataItem) && hasData) {
                            boolean isNeedDealWithRoamingData;
                            String callNumberString;
                            if (contactData.getNameRawContactId() != -1 && EmuiFeatureManager.isChinaArea()) {
                                entry.location = dataItem.getContentValues().getAsString("data6");
                            }
                            entry.isYellowPage = contactData.isYellowPage();
                            PhoneDataItem phone = (PhoneDataItem) dataItem;
                            entry.mOriginalPhoneNum = phone.getNumber();
                            entry.normalizedNumber = phone.getNormalizedNumber();
                            String roamingFormString = null;
                            if (IsPhoneNetworkRoamingUtils.isRoamingDealWithNumber(context, entry.mOriginalPhoneNum)) {
                                isNeedDealWithRoamingData = IsPhoneNetworkRoamingUtils.isPhoneNetworkRoamging();
                            } else {
                                isNeedDealWithRoamingData = false;
                            }
                            if (isNeedDealWithRoamingData) {
                                roamingFormString = IsPhoneNetworkRoamingUtils.getPhoneNumber(context, entry, phone);
                                entry.roamingData = roamingFormString;
                            } else if (entry.normalizedNumber == null || entry.normalizedNumber.length() == 0) {
                                entry.setNullOriginalRoamingData(true);
                            }
                            if (EmuiFeatureManager.isChinaArea()) {
                                entry.data = ContactsUtils.getChinaFormatNumber(phone.getFormattedPhoneNumber());
                            } else {
                                entry.data = phone.getFormattedPhoneNumber();
                            }
                            entriesObject.qrNumbers.add(entry.data);
                            if (isNeedDealWithRoamingData) {
                                boolean isNumberMatchCurrentCountry = false;
                                if (!entry.isNullOriginalRoamingData) {
                                    isNumberMatchCurrentCountry = IsPhoneNetworkRoamingUtils.isNumberMatchCurrentCountry(entry.data, entry.roamingData, context);
                                }
                                if (isNumberMatchCurrentCountry) {
                                    callNumberString = entry.data;
                                } else {
                                    callNumberString = roamingFormString;
                                }
                            } else {
                                callNumberString = entry.data;
                            }
                            Intent pIntent = null;
                            if (mHasPhone) {
                                pIntent = CallUtil.getCallIntent(callNumberString);
                                if (entry.isNullOriginalRoamingData) {
                                    pIntent.putExtra(" pref_Original_Normalized_Number_Is_Null", true);
                                    pIntent.putExtra("pref_Original_Numbe", entry.data);
                                    pIntent.putExtra("pref_Dial_Number", callNumberString);
                                }
                                if (entry.getRoamingPhoneGatherUtils() != null) {
                                    RoamingPhoneGatherUtils.disposeSingleCardRoamingPhoneItem(context, entry.getRoamingPhoneGatherUtils(), pIntent);
                                }
                            }
                            Intent phoneIntent = pIntent;
                            if (mHasPhone) {
                                entry.intent = phoneIntent;
                            } else {
                                entry.intent = null;
                            }
                            entry.isPrimary = isSuperPrimary;
                            if (entry.isPrimary) {
                                entriesObject.phoneEntries.add(0, entry);
                            } else {
                                entriesObject.phoneEntries.add(entry);
                                String tempNum = PhoneNumberUtils.stripSeparators(PhoneNumberFormatter.parsePhoneNumber(entry.data));
                            }
                        } else if ((dataItem instanceof EmailDataItem) && hasData) {
                            entry.intent = new Intent("android.intent.action.SENDTO", Uri.fromParts("mailto", entry.data, null));
                            entry.isPrimary = isSuperPrimary;
                            if (entry.isPrimary) {
                                entriesObject.emailEntries.add(0, entry);
                            } else {
                                entriesObject.emailEntries.add(entry);
                            }
                            entriesObject.qrEmails.add(entry.data);
                            if (contactData.getStatuses() != null) {
                                status = (DataStatus) contactData.getStatuses().get(Long.valueOf(entry.id));
                                if (status != null) {
                                    DetailViewEntry imEntry = DetailViewEntry.fromValues(context, ImDataItem.createFromEmail((EmailDataItem) dataItem), contactData.isDirectoryEntry(), contactData.getDirectoryId(), kind);
                                    imEntry.setPresence(status.getPresence());
                                    imEntry.maxLines = kind.maxLinesForDisplay;
                                    entriesObject.imEntries.add(imEntry);
                                }
                            }
                        } else if ((dataItem instanceof StructuredPostalDataItem) && hasData) {
                            entry.intent = StructuredPostalUtils.getViewPostalAddressIntent(entry.data);
                            entriesObject.postalEntries.add(entry);
                            entriesObject.qrPostals.add(entry.data);
                        } else if ((dataItem instanceof ImDataItem) && hasData) {
                            if (contactData.getStatuses() != null) {
                                status = (DataStatus) contactData.getStatuses().get(Long.valueOf(entry.id));
                                if (status != null) {
                                    entry.setPresence(status.getPresence());
                                }
                            }
                            if (!ContactDetailHelper.isEntryAlreadyExisted(entriesObject.imEntries, entry)) {
                                entriesObject.imEntries.add(entry);
                                entriesObject.qrIms.add(entry.data);
                            }
                        } else if (dataItem instanceof OrganizationDataItem) {
                            OrganizationDataItem orgDataItem = (OrganizationDataItem) dataItem;
                            String company = orgDataItem.getCompany();
                            String title = orgDataItem.getTitle();
                            entriesObject.qrCompanys.add(company);
                            entriesObject.qrJobTitles.add(title);
                            entry.data = company;
                            entry.typeString = null;
                            entry.kind = context.getString(R.string.ghostData_company);
                            entriesObject.companyEntries.add(entry);
                            DetailViewEntry entryTemp = new DetailViewEntry();
                            entryTemp.data = title;
                            entryTemp.kind = context.getString(R.string.ghostData_title);
                            entryTemp.typeString = null;
                            entryTemp.mimetype = entry.mimetype;
                            entriesObject.positionEntries.add(entryTemp);
                        } else if ((dataItem instanceof NicknameDataItem) && hasData) {
                            boolean duplicatesTitle = (contactData.getNameRawContactId() > rawContactId ? 1 : (contactData.getNameRawContactId() == rawContactId ? 0 : -1)) == 0 ? contactData.getDisplayNameSource() == 35 : false;
                            if (!duplicatesTitle) {
                                entry.uri = null;
                                entriesObject.nicknameEntries.add(entry);
                            }
                        } else if ((dataItem instanceof NoteDataItem) && hasData) {
                            entry.uri = null;
                            entriesObject.noteEntries.add(entry);
                            entriesObject.qrNotes.add(entry.data);
                        } else if ((dataItem instanceof WebsiteDataItem) && hasData) {
                            entry.uri = null;
                            try {
                                entry.intent = new Intent("android.intent.action.VIEW", Uri.parse(new WebAddress(entry.data).toString()));
                            } catch (ParseException e) {
                                HwLog.e(TAG, "Couldn't parse website");
                            }
                            entriesObject.websiteEntries.add(entry);
                            entriesObject.qrWebsites.add(entry.data);
                        } else if ((dataItem instanceof SipAddressDataItem) && hasData) {
                            if (!PhoneCapabilityTester.isChinaTelecomCustomer(context)) {
                                entry.uri = null;
                                if (mHasSip) {
                                    entry.intent = CallUtil.getCallIntent(Uri.fromParts("sip", entry.data, null));
                                } else {
                                    entry.intent = null;
                                }
                                entriesObject.sipEntries.add(entry);
                            }
                        } else if ((dataItem instanceof EventDataItem) && hasData) {
                            HwLog.i(TAG, "ContactDetailFragment : entry.data  ");
                            if (entry.type != 4 || LunarUtils.supportLunarAccount(rawContact.getAccountTypeString(), context)) {
                                if (LunarUtils.hasYear(context, entry.data)) {
                                    entry.data = LunarUtils.getCurrentYear() + entry.data.substring(1, entry.data.length());
                                }
                                boolean isLunarBirthday = entry.typeString.equals(context.getString(R.string.event_lunar_birthday));
                                if (LunarUtils.checkTimeValidity(false, entry.data)) {
                                    long millis;
                                    if (isLunarBirthday) {
                                        LunarUtils.initYearAndWeek(context);
                                        millis = LunarUtils.getNextLunarBirthday(entry.data).longValue();
                                        entry.data = LunarUtils.titleSolarToLunar(context, entry.data);
                                    } else {
                                        millis = LunarUtils.getNextSolarBirthday(entry.data, true);
                                        entry.data = DateUtils.formatDate(context, entry.data);
                                    }
                                    if (millis != 0) {
                                        entry.intent = LunarUtils.getEventIntent(millis);
                                        entry.uri = null;
                                        if (!ContactDetailHelper.isEntryAlreadyExisted(entriesObject.eventEntries, entry)) {
                                            entriesObject.eventEntries.add(entry);
                                        }
                                    }
                                }
                            }
                        } else if ((dataItem instanceof RelationDataItem) && hasData) {
                            entry.intent = new Intent("android.intent.action.SEARCH");
                            entry.intent.putExtra(SearchIntents.EXTRA_QUERY, entry.data);
                            entry.intent.setType("vnd.android.cursor.dir/contact");
                            if (!ContactDetailHelper.isEntryAlreadyExisted(entriesObject.relationEntries, entry)) {
                                entriesObject.relationEntries.add(entry);
                            }
                        } else if ("vnd.android.huawei.cursor.item/status_update".equals(dataItem.getMimeType()) && hasData) {
                            entry.kind = context.getString(R.string.str_title_res_for_status);
                            entriesObject.statusEntriesForIm.add(entry);
                        } else {
                            entry.intent = new Intent("android.intent.action.VIEW");
                            entry.intent.setDataAndType(entry.uri, entry.mimetype);
                            entry.intent.addFlags(536870912);
                            entry.data = dataItem.buildDataString(context, kind);
                            if (!TextUtils.isEmpty(entry.data)) {
                                entry.intent.setFlags(335544320);
                                if (entriesObject.otherEntriesMap.containsKey(accountType)) {
                                    ((List) entriesObject.otherEntriesMap.get(accountType)).add(entry);
                                } else {
                                    List<DetailViewEntry> listEntries = new ArrayList();
                                    listEntries.add(entry);
                                    entriesObject.otherEntriesMap.put(accountType, listEntries);
                                }
                            }
                        }
                    }
                }
            }
            String phoneticName = ContactDetailDisplayUtils.getPhoneticName(context, contactData);
            if (!TextUtils.isEmpty(phoneticName)) {
                String phoneticNameKindTitle = context.getString(R.string.name_phonetic);
                entry = new DetailViewEntry();
                entry.kind = phoneticNameKindTitle;
                entry.data = phoneticName;
                entry.mimetype = "#phoneticName";
                entriesObject.phoneticNameEntries.add(entry);
            }
        }
        if (entriesObject.phoneticNameEntries.size() > 0) {
            ((DetailViewEntry) entriesObject.phoneticNameEntries.get(0)).isFristEntry = true;
        }
        if (entriesObject.phoneEntries.size() > 0) {
            ((DetailViewEntry) entriesObject.phoneEntries.get(0)).isFristEntry = true;
        }
        if (entriesObject.emailEntries.size() > 0) {
            ((DetailViewEntry) entriesObject.emailEntries.get(0)).isFristEntry = true;
        }
        if (entriesObject.eventEntries.size() > 0) {
            ((DetailViewEntry) entriesObject.eventEntries.get(0)).isFristEntry = true;
        }
        if (entriesObject.postalEntries.size() > 0) {
            ((DetailViewEntry) entriesObject.postalEntries.get(0)).isFristEntry = true;
        }
        if (entriesObject.websiteEntries.size() > 0) {
            ((DetailViewEntry) entriesObject.websiteEntries.get(0)).isFristEntry = true;
        }
        if (entriesObject.imEntries.size() > 0) {
            ((DetailViewEntry) entriesObject.imEntries.get(0)).isFristEntry = true;
        }
        if (entriesObject.relationEntries.size() > 0) {
            ((DetailViewEntry) entriesObject.relationEntries.get(0)).isFristEntry = true;
        }
        entriesObject.qrcodeDataInfo.put("phone", entriesObject.qrNumbers);
        entriesObject.qrcodeDataInfo.put("name", entriesObject.qrNames);
        entriesObject.qrcodeDataInfo.put(Scopes.EMAIL, entriesObject.qrEmails);
        entriesObject.qrcodeDataInfo.put("postal", entriesObject.qrPostals);
        entriesObject.qrcodeDataInfo.put("company", entriesObject.qrCompanys);
        entriesObject.qrcodeDataInfo.put("job_title", entriesObject.qrJobTitles);
        entriesObject.qrcodeDataInfo.put("URL_KEY", entriesObject.qrWebsites);
        entriesObject.qrcodeDataInfo.put("NOTE_KEY", entriesObject.qrNotes);
        entriesObject.qrcodeDataInfo.put("IM", entriesObject.qrIms);
        return entriesObject;
    }

    public static Bundle buildQRCodeBundle(Contact contactData, HashMap<String, ArrayList<String>> qrcodeDataInfo) {
        Bundle lBundle = getQRCodeEntry(contactData, qrcodeDataInfo);
        if (!(qrcodeDataInfo == null || qrcodeDataInfo.size() == 0)) {
            ArrayList<String> notes = (ArrayList) qrcodeDataInfo.get("NOTE_KEY");
            ArrayList<String> ims = (ArrayList) qrcodeDataInfo.get("IM");
            StringBuffer noteInfo = new StringBuffer();
            if (notes != null && notes.size() > 0) {
                noteInfo.append((String) notes.get(0));
            }
            if (ims != null && ims.size() > 0) {
                if (noteInfo.length() > 0) {
                    noteInfo.append("\n");
                }
                noteInfo.append((String) ims.get(0));
            }
            lBundle.putString("NOTE_KEY", String.valueOf(noteInfo));
        }
        return lBundle;
    }

    private static Bundle getQRCodeEntry(Contact contactData, HashMap<String, ArrayList<String>> qrcodeDataInfo) {
        Bundle bundle = new Bundle();
        if (!(qrcodeDataInfo == null || qrcodeDataInfo.size() == 0 || contactData == null)) {
            putEntryForQRCode("phone", (ArrayList) qrcodeDataInfo.get("phone"), 3, bundle);
            putEntryForQRCode(Scopes.EMAIL, (ArrayList) qrcodeDataInfo.get(Scopes.EMAIL), 3, bundle);
            putEntryForQRCode("postal", (ArrayList) qrcodeDataInfo.get("postal"), 1, bundle);
            if (contactData.isYellowPage()) {
                ArrayList<String> yellowName = new ArrayList();
                yellowName.add(contactData.getDisplayName());
                putEntryForQRCode("name", yellowName, 1, bundle);
            } else {
                putEntryForQRCode("name", (ArrayList) qrcodeDataInfo.get("name"), 1, bundle);
            }
            putEntryForQRCode("company", (ArrayList) qrcodeDataInfo.get("company"), 1, bundle);
            putEntryForQRCode("URL_KEY", (ArrayList) qrcodeDataInfo.get("URL_KEY"), 1, bundle);
            putEntryForQRCode("job_title", (ArrayList) qrcodeDataInfo.get("job_title"), 1, bundle);
        }
        return bundle;
    }

    private static void putEntryForQRCode(String mTitleName, ArrayList<String> mEntries, int limit, Bundle bundle) {
        if (mEntries != null && mEntries.size() != 0) {
            String keyFirst = mTitleName;
            String keySecond = "secondary_" + mTitleName;
            String keyThird = "tertiary_" + mTitleName;
            String[] keys = new String[]{mTitleName, keySecond, keyThird};
            ArrayList<String> entries = new ArrayList();
            if (mEntries.size() > limit) {
                int count = 0;
                for (String s : mEntries) {
                    if (count > limit) {
                        break;
                    } else if (!entries.contains(s)) {
                        entries.add(s);
                        count++;
                    }
                }
                mEntries = entries;
            }
            int noMoreThanKeysLength = Math.min(Math.min(mEntries.size(), limit), keys.length);
            for (int i = 0; i < noMoreThanKeysLength; i++) {
                bundle.putString(keys[i], (String) mEntries.get(i));
            }
        }
    }

    public static Uri getProfileLookupUri(Context mContext) {
        if (mContext == null) {
            return null;
        }
        Cursor cursor = mContext.getContentResolver().query(Profile.CONTENT_URI, new String[]{"_id", "lookup"}, null, null, null);
        Uri profileLookupUri = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    profileLookupUri = ContactLoaderUtils.ensureIsContactUri(mContext.getContentResolver(), Contacts.getLookupUri(cursor.getLong(0), cursor.getString(1)));
                    if (cursor != null) {
                        cursor.close();
                    }
                    return profileLookupUri;
                }
            } catch (Exception e) {
                HwLog.e(TAG, "" + e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    public static Bitmap getRoundCornerImage(Bitmap bitmap, int roundPixels, HalfType half) {
        if (bitmap == null) {
            return bitmap;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap roundConcerImage = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(roundConcerImage);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, width, height);
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawRoundRect(rectF, (float) roundPixels, (float) roundPixels, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();
        switch (-getcom-android-contacts-profile-ProfileUtils$HalfTypeSwitchesValues()[half.ordinal()]) {
            case 1:
                return Bitmap.createBitmap(roundConcerImage, 0, height - roundPixels, width, height - roundPixels);
            case 2:
                return Bitmap.createBitmap(roundConcerImage, 0, 0, width - roundPixels, height);
            case 3:
                return Bitmap.createBitmap(roundConcerImage, width - roundPixels, 0, width - roundPixels, height);
            case 4:
                return Bitmap.createBitmap(roundConcerImage, 0, 0, width, height - roundPixels);
            default:
                return roundConcerImage;
        }
    }

    public static void setProfileContainerData(TextView textView, ArrayList<DetailViewEntry> entries) {
        if (textView != null && entries != null) {
            String data = null;
            if (entries.size() > 0) {
                data = ((DetailViewEntry) entries.get(0)).data;
            }
            ContactDetailDisplayUtils.setDataOrHideIfNone(data, textView);
        }
    }

    public static Bitmap cutBitmapAndScale(Bitmap bitmap, int targetWidth, int targetHeight, boolean needScale, boolean addShadow) {
        if (bitmap == null) {
            return bitmap;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float standard = ((float) targetWidth) / ((float) targetHeight);
        if (((float) width) / ((float) height) >= standard) {
            width = (int) (((float) height) * standard);
        } else {
            height = (int) (((float) width) / standard);
        }
        Bitmap cutAndScaleBitmap = Bitmap.createBitmap(bitmap, (bitmap.getWidth() - width) / 2, (bitmap.getHeight() - height) / 2, width, height);
        if (needScale) {
            cutAndScaleBitmap = Bitmap.createScaledBitmap(cutAndScaleBitmap, targetWidth, targetHeight, false);
        }
        if (addShadow) {
            cutAndScaleBitmap = addShadow(cutAndScaleBitmap);
        }
        return cutAndScaleBitmap;
    }

    private static Bitmap addShadow(Bitmap bm) {
        GradientDrawable mBackShadowDrawableLR = new GradientDrawable(Orientation.TOP_BOTTOM, new int[]{0, Integer.MIN_VALUE});
        mBackShadowDrawableLR.setBounds(0, 0, bm.getWidth(), bm.getHeight());
        mBackShadowDrawableLR.draw(new Canvas(bm));
        return bm;
    }

    public static Bitmap drawColorBitmap(int color, int targetWidth, int targetHeight) {
        Bitmap bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStyle(Style.FILL);
        canvas.drawRect(0.0f, 0.0f, (float) targetWidth, (float) targetHeight, paint);
        return bitmap;
    }

    public static Uri getBitmapFileUri(Context context, Bitmap bitmap) {
        return getBitmapFileUri(context, bitmap, Boolean.valueOf(false));
    }

    public static Uri getBitmapFileUri(Context context, Bitmap bitmap, Boolean needRecycle) {
        FileNotFoundException e;
        Uri result;
        Exception ex;
        Throwable th;
        if (context == null) {
            return null;
        }
        FileOutputStream fileOutputStream = null;
        File file = new File(context.getExternalCacheDir(), "profile.jpg");
        try {
            FileOutputStream mFileOutPutStream = new FileOutputStream(file);
            if (bitmap != null) {
                try {
                    bitmap.compress(CompressFormat.JPEG, 30, mFileOutPutStream);
                } catch (FileNotFoundException e2) {
                    e = e2;
                    fileOutputStream = mFileOutPutStream;
                    HwLog.w(TAG, "getBitmap " + e.getMessage());
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.flush();
                            fileOutputStream.close();
                        } catch (IOException e3) {
                            HwLog.w(TAG, "getBitmap " + e3.getMessage());
                        }
                    }
                    bitmap.recycle();
                    if (!file.exists()) {
                        return null;
                    }
                    file.setReadable(true, false);
                    result = null;
                    try {
                        return FileProvider.getUriForFile(context, "com.android.contacts.files", file);
                    } catch (Exception e4) {
                        HwLog.i(TAG, "Failed to find configured root");
                        return result;
                    } catch (Throwable th2) {
                        return result;
                    }
                } catch (Exception e5) {
                    ex = e5;
                    fileOutputStream = mFileOutPutStream;
                    try {
                        HwLog.w(TAG, "getBitmap " + ex.getMessage());
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.flush();
                                fileOutputStream.close();
                            } catch (IOException e32) {
                                HwLog.w(TAG, "getBitmap " + e32.getMessage());
                            }
                        }
                        bitmap.recycle();
                        if (!file.exists()) {
                            return null;
                        }
                        file.setReadable(true, false);
                        result = null;
                        return FileProvider.getUriForFile(context, "com.android.contacts.files", file);
                    } catch (Throwable th3) {
                        th = th3;
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.flush();
                                fileOutputStream.close();
                            } catch (IOException e322) {
                                HwLog.w(TAG, "getBitmap " + e322.getMessage());
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    fileOutputStream = mFileOutPutStream;
                    if (fileOutputStream != null) {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                    throw th;
                }
            }
            if (mFileOutPutStream != null) {
                try {
                    mFileOutPutStream.flush();
                    mFileOutPutStream.close();
                } catch (IOException e3222) {
                    HwLog.w(TAG, "getBitmap " + e3222.getMessage());
                }
            }
            fileOutputStream = mFileOutPutStream;
        } catch (FileNotFoundException e6) {
            e = e6;
            HwLog.w(TAG, "getBitmap " + e.getMessage());
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
            bitmap.recycle();
            if (!file.exists()) {
                return null;
            }
            file.setReadable(true, false);
            result = null;
            return FileProvider.getUriForFile(context, "com.android.contacts.files", file);
        } catch (Exception e7) {
            ex = e7;
            HwLog.w(TAG, "getBitmap " + ex.getMessage());
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
            bitmap.recycle();
            if (!file.exists()) {
                return null;
            }
            file.setReadable(true, false);
            result = null;
            return FileProvider.getUriForFile(context, "com.android.contacts.files", file);
        }
        if (bitmap != null && needRecycle.booleanValue()) {
            bitmap.recycle();
        }
        if (!file.exists()) {
            return null;
        }
        file.setReadable(true, false);
        result = null;
        return FileProvider.getUriForFile(context, "com.android.contacts.files", file);
    }

    public static Bitmap covertViewToBitmap(View view) {
        if (view == null || view.getWidth() <= 0 || view.getHeight() <= 0) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        return bitmap;
    }

    public static Bitmap colorBitmap(Bitmap bitmap, int destWidth, int destHeight, float roundPX, int color) {
        if (bitmap == null || destWidth <= 0 || destHeight <= 0 || ((double) roundPX) <= 0.0d) {
            return null;
        }
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Bitmap outputBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(outputBitmap);
            Paint paint = new Paint();
            Rect rect = new Rect(0, 0, width, height);
            RectF rectF = new RectF(rect);
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPX, roundPX, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            bitmap.recycle();
            return outputBitmap;
        } catch (RuntimeException e) {
            return bitmap;
        } catch (Exception e2) {
            return bitmap;
        }
    }

    public static String buildShareTextCard(ContactEntriesObject entriesObject) {
        if (entriesObject == null) {
            return null;
        }
        StringBuilder strBuilder = new StringBuilder();
        buildEntryForShareText(entriesObject.nameEntries, strBuilder);
        buildEntryForShareText(entriesObject.companyEntries, strBuilder);
        buildEntryForShareText(entriesObject.positionEntries, strBuilder);
        buildEntryForShareText(entriesObject.phoneticNameEntries, strBuilder);
        buildEntryForShareText(entriesObject.phoneEntries, strBuilder);
        buildEntryForShareText(entriesObject.emailEntries, strBuilder);
        buildEntryForShareText(entriesObject.nicknameEntries, strBuilder);
        buildEntryForShareText(entriesObject.websiteEntries, strBuilder);
        buildEntryForShareText(entriesObject.postalEntries, strBuilder);
        buildEntryForShareText(entriesObject.eventEntries, strBuilder);
        buildEntryForShareText(entriesObject.noteEntries, strBuilder);
        buildEntryForShareText(entriesObject.imEntries, strBuilder);
        buildEntryForShareText(entriesObject.relationEntries, strBuilder);
        buildEntryForShareText(entriesObject.sipEntries, strBuilder);
        return strBuilder.toString();
    }

    private static void buildEntryForShareText(ArrayList<DetailViewEntry> entries, StringBuilder strBuilder) {
        if (entries != null && strBuilder != null) {
            for (int i = 0; i < entries.size(); i++) {
                DetailViewEntry entry = (DetailViewEntry) entries.get(i);
                if (!(entry == null || TextUtils.isEmpty(entry.data))) {
                    String title = entry.typeString;
                    if (title == null || title.length() == 0) {
                        title = entry.kind;
                    } else if ("vnd.android.cursor.item/postal-address_v2".equals(entry.mimetype)) {
                        title = title + entry.kind;
                    }
                    strBuilder.append(title).append(":").append(entry.data).append("\n");
                }
            }
        }
    }

    public static void setMeListResource(ImageView photoImageView, Context context, Uri profileLookupUri) {
        if (profileLookupUri != null) {
            loadMePhoto(photoImageView, context, profileLookupUri);
        } else {
            getDefaultPhoto(photoImageView, context);
        }
    }

    private static void getDefaultPhoto(ImageView photoImageView, Context context) {
        if (photoImageView != null && context != null) {
            photoImageView.setImageDrawable(ContactPhotoManager.getDefaultAvatarDrawableForContact(context.getResources(), false, new DefaultImageRequest(null, String.valueOf(getDefaultColorIndex(context)), 1, true)));
        }
    }

    public static int getDefaultColorIndex(Context context) {
        int index = 0;
        if (context != null) {
            TypedArray sColors = context.getResources().obtainTypedArray(R.array.letter_tile_colors_default);
            int defColor = context.getColor(R.color.profile_simple_card_default_bg_color);
            for (int i = 0; i < sColors.length(); i++) {
                if (sColors.getColor(i, defColor * 2) == defColor) {
                    index = i;
                }
            }
            sColors.recycle();
        }
        return index;
    }

    public static void loadMePhoto(ImageView photoImageView, Context context, Uri profileLookupUri) {
        if (photoImageView != null && context != null && profileLookupUri != null) {
            String[] PROJECTION = new String[]{"data15"};
            Cursor dataCursor = context.getContentResolver().query(Uri.withAppendedPath(profileLookupUri, "entities"), PROJECTION, "mimetype=?", new String[]{"vnd.android.cursor.item/photo"}, null);
            if (dataCursor != null) {
                try {
                    if (dataCursor.moveToNext()) {
                        byte[] photoData = dataCursor.getBlob(0);
                        if (photoData != null && photoData.length > 0) {
                            photoImageView.setImageBitmap(ContactPhotoManager.createRoundPhoto(BitmapFactory.decodeByteArray(photoData, 0, photoData.length, null)));
                            photoImageView.setScaleType(ScaleType.FIT_XY);
                        }
                        if (dataCursor != null) {
                            dataCursor.close();
                        }
                    }
                } catch (RuntimeException e) {
                    HwLog.e(TAG, "RuntimeException: load me photo fail!");
                    if (dataCursor != null) {
                        dataCursor.close();
                    }
                } catch (Exception e2) {
                    HwLog.e(TAG, "Non-RuntimeException: load me photo fail!");
                    if (dataCursor != null) {
                        dataCursor.close();
                    }
                } catch (Throwable th) {
                    if (dataCursor != null) {
                        dataCursor.close();
                    }
                }
            }
            getDefaultPhoto(photoImageView, context);
            if (dataCursor != null) {
                dataCursor.close();
            }
        }
    }

    public static boolean isProfileUri(Uri uri) {
        boolean z = false;
        if (uri == null || uri.toString() == null) {
            return false;
        }
        if (uri.toString().indexOf(Scopes.PROFILE) != -1) {
            z = true;
        }
        return z;
    }
}
