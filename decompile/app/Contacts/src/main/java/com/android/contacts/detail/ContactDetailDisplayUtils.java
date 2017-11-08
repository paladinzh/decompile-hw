package com.android.contacts.detail;

import android.content.Context;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.contacts.ext.HwCustContactAndProfileInitializer;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.model.Contact;
import com.android.contacts.model.RawContact;
import com.android.contacts.model.dataitem.DataItem;
import com.android.contacts.model.dataitem.OrganizationDataItem;
import com.android.contacts.preference.ContactsPreferences;
import com.android.contacts.util.ContactBadgeUtil;
import com.android.contacts.util.MoreMath;
import com.android.contacts.util.StreamItemEntry;
import com.google.android.gms.R;
import com.google.common.annotations.VisibleForTesting;
import com.huawei.cust.HwCustUtils;

public class ContactDetailDisplayUtils {
    private static HwCustContactAndProfileInitializer mCust;

    private static class TitleAndCompanyCache {
        public String mCompany;
        public String mProfession;

        public TitleAndCompanyCache(String company, String profession) {
            this.mCompany = company;
            this.mProfession = profession;
        }
    }

    private ContactDetailDisplayUtils() {
    }

    static {
        HwCustContactAndProfileInitializer hwCustContactAndProfileInitializer;
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            hwCustContactAndProfileInitializer = (HwCustContactAndProfileInitializer) HwCustUtils.createObj(HwCustContactAndProfileInitializer.class, new Object[0]);
        } else {
            hwCustContactAndProfileInitializer = null;
        }
        mCust = hwCustContactAndProfileInitializer;
    }

    public static CharSequence getDisplayName(Context context, Contact contactData) {
        CharSequence displayName = contactData.getDisplayName();
        CharSequence altDisplayName = contactData.getAltDisplayName();
        ContactsPreferences prefs = new ContactsPreferences(context);
        CharSequence styledName = "";
        if (TextUtils.isEmpty(displayName) || TextUtils.isEmpty(altDisplayName)) {
            styledName = context.getResources().getString(R.string.missing_name);
        } else if (prefs.getDisplayOrder() == 1) {
            styledName = displayName;
        } else {
            styledName = altDisplayName;
        }
        if (mCust != null) {
            return mCust.getDisplayNameForProfileDetails(context, contactData, styledName);
        }
        return styledName;
    }

    public static String getPhoneticName(Context context, Contact contactData) {
        String phoneticName = contactData.getPhoneticName();
        if (TextUtils.isEmpty(phoneticName)) {
            return null;
        }
        return phoneticName;
    }

    public static String getAttribution(Context context, Contact contactData) {
        if (!contactData.isDirectoryEntry()) {
            return null;
        }
        String displayName;
        String directoryDisplayName = contactData.getDirectoryDisplayName();
        String directoryType = contactData.getDirectoryType();
        if (TextUtils.isEmpty(directoryDisplayName)) {
            displayName = directoryType;
        } else {
            displayName = directoryDisplayName;
        }
        return context.getString(R.string.contact_directory_description, new Object[]{displayName});
    }

    public static TitleAndCompanyCache getCompanyInfo(Context context, Contact contactData) {
        for (RawContact rawContact : contactData.getRawContacts()) {
            for (DataItem dataItem : rawContact.getDataItems()) {
                if (dataItem instanceof OrganizationDataItem) {
                    OrganizationDataItem orgDataItem = (OrganizationDataItem) dataItem;
                    return new TitleAndCompanyCache(orgDataItem.getCompany(), orgDataItem.getTitle());
                }
            }
        }
        return null;
    }

    public static void setContactDisplayInfo(Context context, TextView nameView, String name, TextView companyView, String geoInfo) {
        if (!isAnyOneViewIsNull(nameView, companyView)) {
            setDataOrHideIfNone(name, nameView);
            setDataOrHideIfNone(geoInfo, companyView);
        }
    }

    public static void setContactDisplayInfo(Context context, Contact contactData, TextView nameView, TextView companyView, View photoView, View nameContainer, boolean isBlackContact) {
        if (!isAnyOneViewIsNull(nameView, companyView, photoView)) {
            TextView textView = null;
            if (nameContainer != null) {
                textView = (TextView) nameContainer.findViewById(R.id.profession);
            }
            String displayName = (String) getDisplayName(context, contactData);
            TitleAndCompanyCache cache = getCompanyInfo(context, contactData);
            String companyName = "";
            String profession = "";
            if (cache != null) {
                companyName = cache.mCompany;
                profession = cache.mProfession;
            }
            boolean isEmptyCompany = TextUtils.isEmpty(companyName);
            boolean isEmptyProfession = TextUtils.isEmpty(profession);
            if (!(isEmptyCompany && isEmptyProfession) && contactData.getDisplayNameSource() == 30) {
                if (displayName.equals(companyName)) {
                    displayName = companyName;
                    companyName = "";
                } else if (displayName.equals(profession)) {
                    displayName = profession;
                    profession = "";
                }
            }
            setDataOrHideIfNone(displayName, nameView);
            if (isBlackContact) {
                String blackString = context.getString(R.string.added_to_blacklist);
                companyView.setVisibility(0);
                if (textView != null) {
                    textView.setVisibility(8);
                }
                setDataOrHideIfNone(blackString, companyView);
            } else {
                setCompanyAndProfessionViewVisibility(isEmptyCompany, companyView, isEmptyProfession, textView);
                String display = "";
                if (!isEmptyCompany) {
                    display = companyName;
                }
                if (!isEmptyProfession) {
                    if (textView != null) {
                        textView.setText(profession);
                    } else {
                        display = display + HwCustPreloadContacts.EMPTY_STRING + profession;
                    }
                }
                setDataOrHideIfNone(display, companyView);
            }
        }
    }

    private static boolean isAnyOneViewIsNull(View... args) {
        for (View view : args) {
            if (view == null) {
                return true;
            }
        }
        return false;
    }

    private static void setCompanyAndProfessionViewVisibility(boolean isEmptyCompany, TextView companyView, boolean isEmptyProfession, TextView professionView) {
        int i = 8;
        if (!isEmptyCompany || professionView == null) {
            companyView.setVisibility(0);
        } else {
            companyView.setVisibility(8);
        }
        if (professionView != null) {
            if (!isEmptyProfession) {
                i = 0;
            }
            professionView.setVisibility(i);
        }
    }

    public static void configureStarredMenuItem(MenuItem menuStar, boolean isDirectoryEntry, boolean isUserProfile, boolean isStarred) {
        if (!isDirectoryEntry && !isUserProfile && menuStar != null) {
            menuStar.setIcon(isStarred ? R.drawable.menu_sharred_click : R.drawable.menu_sharred_unclick);
            menuStar.setTitle(isStarred ? R.string.contacts_un_starred : R.string.contacts_starred);
        } else if (menuStar != null) {
            menuStar.setVisible(false);
        }
    }

    @VisibleForTesting
    static View addStreamItemText(Context context, StreamItemEntry streamItem, View rootView) {
        TextView attributionView = (TextView) rootView.findViewById(R.id.stream_item_attribution);
        TextView commentsView = (TextView) rootView.findViewById(R.id.stream_item_comments);
        setDataOrHideIfNone(streamItem.getDecodedText(), (TextView) rootView.findViewById(R.id.stream_item_html));
        setDataOrHideIfNone(ContactBadgeUtil.getSocialDate(streamItem, context), attributionView);
        setDataOrHideIfNone(streamItem.getDecodedComments(), commentsView);
        return rootView;
    }

    public static void setDataOrHideIfNone(CharSequence textToDisplay, TextView textView) {
        if (TextUtils.isEmpty(textToDisplay)) {
            textView.setText(null);
            textView.setVisibility(8);
            return;
        }
        textView.setText(textToDisplay.toString().trim());
        textView.setVisibility(0);
    }

    public static void setAlphaOnViewBackground(View view, float alpha) {
        if (view != null) {
            view.setBackgroundColor(Float.valueOf(MoreMath.clamp(alpha, 0.0f, 1.0f) * 255.0f).intValue() << 24);
        }
    }

    public static void configureStarredMenuItem(Context context, View starredMenuItem, boolean isDirectoryEntry, boolean isUserProfile, boolean isStarred) {
        if (!isDirectoryEntry && !isUserProfile && starredMenuItem != null) {
            ((ImageView) starredMenuItem.findViewById(R.id.contact_menuitem_icon)).setImageDrawable(context.getResources().getDrawable(isStarred ? R.drawable.menu_sharred_click : ImmersionUtils.getImmersionImageID(context, R.drawable.menu_sharred_unclick_light, R.drawable.menu_sharred_unclick)));
        } else if (starredMenuItem != null) {
            starredMenuItem.setVisibility(8);
        }
    }
}
