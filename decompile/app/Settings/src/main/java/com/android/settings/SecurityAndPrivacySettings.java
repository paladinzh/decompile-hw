package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Xml;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SecurityAndPrivacySettings extends MoreSettings implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            Resources res = context.getResources();
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = res.getString(2131628921);
            data.screenTitle = res.getString(2131628921);
            result.add(data);
            Intent intent = new Intent().setClassName("com.huawei.trustspace", "com.huawei.trustspace.settings.SettingsActivity");
            if (Utils.isOwnerUser() && Utils.hasIntentActivity(context.getPackageManager(), intent)) {
                String screenTitle = res.getString(2131628838);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838428;
                data.intentAction = "com.android.settings.action.unknown";
                data.intentTargetClass = "com.huawei.trustspace.settings.SettingsActivity";
                data.intentTargetPackage = "com.huawei.trustspace";
                result.add(data);
            }
            String action = "com.huawei.android.remotecontrol.PHONEFINDER_ENTTRANCE";
            if (!SecurityAndPrivacySettings.shouldRemovePhoneFinder(context) && Utils.hasIntentActivity(context.getPackageManager(), action)) {
                screenTitle = res.getString(2131628916);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838428;
                data.intentAction = action;
                result.add(data);
            }
            Context appContext = context.getApplicationContext();
            intent = new Intent().setClassName("com.android.mms", "com.huawei.mms.ui.MsimSmsEncryptSetting");
            if (!SecurityAndPrivacySettings.shouldRemoveEncryptedSms(context, appContext) && Utils.hasIntentActivity(context.getPackageManager(), intent)) {
                screenTitle = res.getString(2131628918);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838428;
                data.intentAction = "com.android.settings.action.unknown";
                data.intentTargetClass = "com.huawei.mms.ui.MsimSmsEncryptSetting";
                data.intentTargetPackage = "com.android.mms";
                result.add(data);
            }
            if (Utils.isOwnerUser()) {
                if (!Utils.isWifiOnly(context)) {
                    if (Utils.hasIntentActivity(context.getPackageManager(), new Intent().setClassName("com.huawei.systemmanager", "com.huawei.harassmentinterception.ui.InterceptionActivity"))) {
                        screenTitle = res.getString(2131628919);
                        data = new SearchIndexableRaw(context);
                        data.title = screenTitle;
                        data.screenTitle = screenTitle;
                        data.iconResId = 2130838428;
                        data.intentAction = "com.android.settings.action.unknown";
                        data.intentTargetClass = "com.huawei.harassmentinterception.ui.InterceptionActivity";
                        data.intentTargetPackage = "com.huawei.systemmanager";
                        result.add(data);
                    }
                }
                if (Utils.hasIntentActivity(context.getPackageManager(), new Intent().setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.antivirus.ui.AntiVirusActivity"))) {
                    screenTitle = res.getString(2131628922);
                    data = new SearchIndexableRaw(context);
                    data.title = screenTitle;
                    data.screenTitle = screenTitle;
                    data.iconResId = 2130838428;
                    data.intentAction = "com.android.settings.action.unknown";
                    data.intentTargetClass = "com.huawei.systemmanager.antivirus.ui.AntiVirusActivity";
                    data.intentTargetPackage = "com.huawei.systemmanager";
                    result.add(data);
                }
                if (Utils.hasIntentActivity(context.getPackageManager(), new Intent().setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.applock.password.AuthEnterAppLockActivity"))) {
                    screenTitle = res.getString(2131628914);
                    data = new SearchIndexableRaw(context);
                    data.title = screenTitle;
                    data.screenTitle = screenTitle;
                    data.iconResId = 2130838428;
                    data.intentAction = "com.android.settings.action.unknown";
                    data.intentTargetClass = "com.huawei.systemmanager.applock.password.AuthEnterAppLockActivity";
                    data.intentTargetPackage = "com.huawei.systemmanager";
                    result.add(data);
                }
            }
            if (Utils.hasIntentActivity(context.getPackageManager(), new Intent().setClassName("com.huawei.hidisk", "com.huawei.hidisk.strongbox.ui.activity.StrongBoxActivity"))) {
                screenTitle = res.getString(2131628920);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838428;
                data.intentAction = "com.android.settings.action.unknown";
                data.intentTargetClass = "com.huawei.hidisk.strongbox.ui.activity.StrongBoxActivity";
                data.intentTargetPackage = "com.huawei.hidisk";
                result.add(data);
            }
            return result;
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230855);
        setHasOptionsMenu(true);
    }

    protected void updatePreferenceList() {
        Preference preference;
        super.updatePreferenceList();
        PreferenceScreen root = getPreferenceScreen();
        Context context = getActivity();
        Context appContext = context.getApplicationContext();
        if (shouldRemovePhoneFinder(context)) {
            preference = root.findPreference("finderphone_settings");
            if (preference != null) {
                root.removePreference(preference);
            }
        }
        if (shouldRemoveTrustSpace()) {
            preference = root.findPreference("trustspace_settings");
            if (preference != null) {
                root.removePreference(preference);
            }
        } else {
            setHwTrustSpaceSummary();
        }
        if (shouldRemoveEncryptedSms(context, appContext)) {
            preference = root.findPreference("encryptedsms_settings");
            if (preference != null) {
                root.removePreference(preference);
            }
        }
        if (!Utils.isOwnerUser()) {
            Preference preference_HARRASSMENT_INTERCEPTION = root.findPreference("harrassmentinterception_settings");
            Preference preference_ANTI_VIRUS = root.findPreference("antivirus_settings");
            Preference preference_APP_LOCK = root.findPreference("applock_settings");
            if (preference_HARRASSMENT_INTERCEPTION != null) {
                root.removePreference(preference_HARRASSMENT_INTERCEPTION);
            }
            if (preference_ANTI_VIRUS != null) {
                root.removePreference(preference_ANTI_VIRUS);
            }
            if (preference_APP_LOCK != null) {
                root.removePreference(preference_APP_LOCK);
            }
        }
        if (Utils.isWifiOnly(context)) {
            preference = root.findPreference("harrassmentinterception_settings");
            if (preference != null) {
                root.removePreference(preference);
            }
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        if ("finderphone_settings".equals(preference.getKey())) {
            Intent mIntent = Utils.getPhoneFinderIntent();
            mIntent.setAction("com.huawei.android.remotecontrol.PHONEFINDER_ENTTRANCE");
            mIntent.setFlags(268435456);
            mIntent.addCategory("android.intent.category.DEFAULT");
            getActivity().startActivity(mIntent);
            new HwAnimationReflection(getActivity()).overrideTransition(1);
        }
        if ("strongbox_settings".equals(preference.getKey())) {
            Intent intent = new Intent();
            intent.setClassName("com.huawei.hidisk", "com.huawei.hidisk.strongbox.ui.activity.StrongBoxActivity");
            intent.setFlags(268435456);
            intent.addCategory("android.intent.category.DEFAULT");
            Utils.cancelSplit(getActivity(), intent);
            getActivity().startActivity(intent);
        }
        return super.onPreferenceTreeClick(preference);
    }

    private static boolean shouldRemovePhoneFinder(Context context) {
        return (Utils.isPhoneFinderActivityExist(context.getPackageManager(), "com.huawei.android.remotecontrol.PHONEFINDER_ENTTRANCE") && Utils.isAntiTheftSupported() && Utils.isOwnerUser()) ? SettingsExtUtils.isGlobalVersion() : true;
    }

    private static boolean shouldRemoveTrustSpace() {
        return Utils.isOwnerUser() ? SettingsExtUtils.isGlobalVersion() : true;
    }

    private static boolean shouldRemoveEncryptedSms(Context context, Context appContext) {
        if (isEnableCryptoSms(context) && Utils.isOwnerUser() && isExistsServiceApk(appContext)) {
            return false;
        }
        return true;
    }

    private static boolean isEnableCryptoSms(Context context) {
        boolean result = false;
        Iterable cfgFileList = null;
        try {
            cfgFileList = HwCfgFilePolicy.getCfgFileList("xml/mms_config.xml", 0);
        } catch (NoClassDefFoundError e) {
            MLog.e("SecurityAndPrivacySettings", "class HwCfgFilePolicy not found error");
        }
        if (r2 == null || r2.size() == 0) {
            result = readMmsConfigXML(context, "/system/etc/xml/mms_config.xml");
            if (readMmsConfigXML(context, "data/cust/xml/mms_config.xml")) {
                return true;
            }
            return result;
        }
        for (File cfg : r2) {
            if (readMmsConfigXML(context, cfg.getPath())) {
                result = true;
            }
        }
        return result;
    }

    private static boolean readMmsConfigXML(Context context, String path) {
        IOException e;
        XmlPullParserException e2;
        Throwable th;
        boolean enableCryptoSms = false;
        XmlPullParser xmlPullParser = null;
        InputStream inputStream = null;
        try {
            InputStream in = new FileInputStream(path);
            try {
                xmlPullParser = Xml.newPullParser();
                xmlPullParser.setInput(in, null);
                enableCryptoSms = getCryptoMmsStat(context, xmlPullParser);
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e3) {
                        e = e3;
                        inputStream = in;
                        MLog.e("SecurityAndPrivacySettings", e.getMessage());
                        return enableCryptoSms;
                    } catch (XmlPullParserException e4) {
                        inputStream = in;
                        MLog.e("SecurityAndPrivacySettings", "load " + path + " MmsSettings XmlPullParserException ");
                        return enableCryptoSms;
                    }
                }
                inputStream = in;
                if (xmlPullParser != null) {
                    try {
                        if (!XmlResourceParser.class.isInstance(xmlPullParser)) {
                            xmlPullParser.setInput(null);
                        }
                    } catch (IOException e5) {
                        e = e5;
                        MLog.e("SecurityAndPrivacySettings", e.getMessage());
                        return enableCryptoSms;
                    } catch (XmlPullParserException e6) {
                        MLog.e("SecurityAndPrivacySettings", "load " + path + " MmsSettings XmlPullParserException ");
                        return enableCryptoSms;
                    }
                }
            } catch (FileNotFoundException e7) {
                inputStream = in;
                MLog.e("SecurityAndPrivacySettings", "load " + path + " MmsSettings caught FileNotFoundException");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e8) {
                        MLog.e("SecurityAndPrivacySettings", e8.getMessage());
                    } catch (XmlPullParserException e9) {
                        MLog.e("SecurityAndPrivacySettings", "load " + path + " MmsSettings XmlPullParserException ");
                    }
                }
                xmlPullParser.setInput(null);
                return enableCryptoSms;
            } catch (XmlPullParserException e10) {
                e2 = e10;
                inputStream = in;
                try {
                    MLog.e("SecurityAndPrivacySettings", "load " + path + " MmsSettings caught XmlPullParserException");
                    MLog.e("SecurityAndPrivacySettings", "load " + path + " MmsSettings caught " + e2.getMessage());
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e82) {
                            MLog.e("SecurityAndPrivacySettings", e82.getMessage());
                        } catch (XmlPullParserException e11) {
                            MLog.e("SecurityAndPrivacySettings", "load " + path + " MmsSettings XmlPullParserException ");
                        }
                    }
                    xmlPullParser.setInput(null);
                    return enableCryptoSms;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e822) {
                            MLog.e("SecurityAndPrivacySettings", e822.getMessage());
                        } catch (XmlPullParserException e12) {
                            MLog.e("SecurityAndPrivacySettings", "load " + path + " MmsSettings XmlPullParserException ");
                        }
                    }
                    if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                        xmlPullParser.setInput(null);
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = in;
                if (inputStream != null) {
                    inputStream.close();
                }
                xmlPullParser.setInput(null);
                throw th;
            }
        } catch (FileNotFoundException e13) {
            MLog.e("SecurityAndPrivacySettings", "load " + path + " MmsSettings caught FileNotFoundException");
            if (inputStream != null) {
                inputStream.close();
            }
            if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                xmlPullParser.setInput(null);
            }
            return enableCryptoSms;
        } catch (XmlPullParserException e14) {
            e2 = e14;
            MLog.e("SecurityAndPrivacySettings", "load " + path + " MmsSettings caught XmlPullParserException");
            MLog.e("SecurityAndPrivacySettings", "load " + path + " MmsSettings caught " + e2.getMessage());
            if (inputStream != null) {
                inputStream.close();
            }
            if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                xmlPullParser.setInput(null);
            }
            return enableCryptoSms;
        }
        return enableCryptoSms;
    }

    private static boolean getCryptoMmsStat(Context context, XmlPullParser parser) {
        boolean z = false;
        try {
            beginDocument(parser, "mms_config");
            while (true) {
                nextElement(parser);
                String tag = parser.getName();
                if (tag == null) {
                    break;
                }
                String name = parser.getAttributeName(0);
                String value = parser.getAttributeValue(0);
                String text = null;
                if (parser.next() == 4) {
                    text = parser.getText();
                }
                if (isStringEqual("name", name) && isStringEqual("bool", tag) && isStringEqual("enableCryptoSms", value)) {
                    z = "true".equalsIgnoreCase(text);
                }
            }
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (parser instanceof KXmlParser) {
                try {
                    ((KXmlParser) parser).close();
                } catch (IOException e) {
                    MLog.e("SecurityAndPrivacySettings", "loadMmsSettings: fail close the parser");
                }
            }
        } catch (XmlPullParserException e2) {
            MLog.e("SecurityAndPrivacySettings", "loadMmsSettings caught " + e2.getMessage());
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (parser instanceof KXmlParser) {
                try {
                    ((KXmlParser) parser).close();
                } catch (IOException e3) {
                    MLog.e("SecurityAndPrivacySettings", "loadMmsSettings: fail close the parser");
                }
            }
        } catch (IOException e4) {
            MLog.e("SecurityAndPrivacySettings", "loadMmsSettings caught " + e4.getMessage());
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (parser instanceof KXmlParser) {
                try {
                    ((KXmlParser) parser).close();
                } catch (IOException e5) {
                    MLog.e("SecurityAndPrivacySettings", "loadMmsSettings: fail close the parser");
                }
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (parser instanceof KXmlParser) {
                try {
                    ((KXmlParser) parser).close();
                } catch (IOException e6) {
                    MLog.e("SecurityAndPrivacySettings", "loadMmsSettings: fail close the parser");
                }
            }
        }
        return z;
    }

    private static final void beginDocument(XmlPullParser parser, String firstElementName) throws XmlPullParserException, IOException {
        int type;
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type != 2) {
            throw new XmlPullParserException("No start tag found");
        } else if (parser.getName() == null || !parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found" + parser.getName() + ", expected " + firstElementName);
        }
    }

    private static final void nextElement(XmlPullParser parser) throws XmlPullParserException, IOException {
        int type;
        do {
            type = parser.next();
            if (type == 2) {
                return;
            }
        } while (type != 1);
    }

    private static final boolean isStringEqual(String cfgTag, String xmlTag) {
        int cfgLen = cfgTag.length();
        if (cfgLen != xmlTag.length()) {
            return false;
        }
        for (int i = cfgLen - 1; i >= 0; i--) {
            if (cfgTag.charAt(i) != xmlTag.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isExistsServiceApk(Context context) {
        try {
            context.getPackageManager().getApplicationInfo("com.huawei.cryptosms.service", 128);
            return true;
        } catch (NameNotFoundException e) {
            MLog.v("SecurityAndPrivacySettings", "NameNotFound!");
            return false;
        }
    }

    private void setHwTrustSpaceSummary() {
        Preference trustSpacePreference = findPreference("trustspace_settings");
        if (trustSpacePreference == null) {
            return;
        }
        if (System.getInt(getContentResolver(), "trust_space_switch", 1) == 1) {
            trustSpacePreference.setSummary(2131626851);
        } else {
            trustSpacePreference.setSummary(2131626852);
        }
    }
}
