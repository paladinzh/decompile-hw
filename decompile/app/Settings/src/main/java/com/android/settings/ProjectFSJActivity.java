package com.android.settings;

import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;

public class ProjectFSJActivity extends PreferenceActivity {
    private String TAG = "ProjectFSJActivity";
    private PreferenceScreen parentPreference;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230850);
        this.parentPreference = (PreferenceScreen) findPreference("project_fsj_screen");
        setupPreferenceOfEmui("emui_version", "ro.build.version.emui");
        setupPreferenceOfSerialno("serial_number");
        setupPreferenceOfProductID("product_ID");
    }

    private String getResStr(String str) {
        if (str == null) {
            return null;
        }
        String resStr = new BigInteger(str.substring(0, 8), 16).xor(new BigInteger(str.substring(str.length() - 8, str.length()), 16)).toString(16);
        char[] buffer = new char[8];
        Arrays.fill(buffer, '0');
        int i = resStr.length() - 1;
        while (i >= 0) {
            buffer[i] = resStr.charAt(i);
            if (resStr.charAt(i) >= 'a' && resStr.charAt(i) <= 'f') {
                buffer[i] = (char) ((resStr.charAt(i) - 97) + 48);
            }
            i--;
        }
        return new String(buffer);
    }

    private String getSrcStr() {
        return SystemProperties.get("ro.serialno", "").toUpperCase(Locale.US);
    }

    private String getSHA256Str(String str) {
        if (str == null) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA256");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
            byte[] byteArray = messageDigest.digest();
            StringBuffer sha256StrBuff = new StringBuffer();
            for (int i = 0; i < byteArray.length; i++) {
                if (Integer.toHexString(byteArray[i] & 255).length() == 1) {
                    sha256StrBuff.append("0").append(Integer.toHexString(byteArray[i] & 255));
                } else {
                    sha256StrBuff.append(Integer.toHexString(byteArray[i] & 255));
                }
            }
            return sha256StrBuff.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(this.TAG, e.toString());
            return null;
        } catch (UnsupportedEncodingException e2) {
            Log.e(this.TAG, e2.toString());
            e2.printStackTrace();
            return null;
        }
    }

    protected void setupPreferenceOfEmui(String preferenceStr, String property) {
        if (this.parentPreference != null) {
            Preference emuiPreference = this.parentPreference.findPreference(preferenceStr);
            if (emuiPreference != null) {
                String emuiVersionStr = SystemProperties.get(property, "");
                if (!removePreferenceIfStringIsEmpty(emuiPreference, emuiVersionStr)) {
                    String emuiSummary = showEmuiVersion(emuiVersionStr);
                    if (!removePreferenceIfStringIsEmpty(emuiPreference, emuiSummary)) {
                        emuiPreference.setSummary(emuiSummary);
                    }
                }
            }
        }
    }

    private String showEmuiVersion(String emVersion) {
        String[] str = emVersion.replace("_", " ").split(" ");
        if (Locale.CHINA.toString().equals(getResources().getConfiguration().locale.toString())) {
            str[0] = str[0].replace("UI", getString(2131624363));
            str[0] = str[0].replace("Emotion", "EMUI ");
        } else {
            str[0] = str[0].replace("EmotionUI", "EMUI");
        }
        String formatEmVersion = "";
        if (str.length > 3) {
            if (str[2].equals("BetaRel")) {
                return str[0] + " " + str[1] + "\n" + getResources().getString(2131627290) + " " + str[3];
            }
            if (str[2].equals("LiveRel")) {
                return str[0] + " " + str[1] + "\n" + getResources().getString(2131627291) + " " + str[3];
            }
            return formatEmVersion;
        } else if (str.length == 2) {
            return str[0] + " " + str[1];
        } else {
            return formatEmVersion;
        }
    }

    private void setupPreferenceOfSerialno(String preferenceSr) {
        if (this.parentPreference != null) {
            Preference serialnoPreference = this.parentPreference.findPreference(preferenceSr);
            if (serialnoPreference != null) {
                String serialnoStr = getSrcStr();
                if (!removePreferenceIfStringIsEmpty(serialnoPreference, serialnoStr)) {
                    serialnoPreference.setSummary(serialnoStr);
                }
            }
        }
    }

    private void setupPreferenceOfProductID(String preferenceSr) {
        if (this.parentPreference != null) {
            Preference productIDPreference = this.parentPreference.findPreference(preferenceSr);
            if (productIDPreference != null) {
                String srcStr = getSrcStr();
                if (!removePreferenceIfStringIsEmpty(productIDPreference, srcStr)) {
                    String resStr = getResStr(getSHA256Str(srcStr));
                    if (!removePreferenceIfStringIsEmpty(productIDPreference, resStr)) {
                        productIDPreference.setSummary(resStr);
                    }
                }
            }
        }
    }

    private boolean removePreferenceIfStringIsEmpty(Preference preference, String str) {
        if (this.parentPreference == null) {
            return false;
        }
        if (str != null && !str.isEmpty()) {
            return false;
        }
        this.parentPreference.removePreference(preference);
        Log.d(this.TAG, preference.getKey() + " preference is remove, because the String is null");
        return true;
    }
}
