package com.android.settings.localepicker;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.LocaleList;
import android.provider.Settings.Secure;
import android.util.Log;
import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocaleStore;
import com.android.internal.app.LocaleStore.LocaleInfo;
import com.android.settings.ItemUseStat;
import com.android.settings.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocaleListHelper {
    private List<LocaleInfo> mFeedsList = getUserLocaleList();

    public List<LocaleInfo> getFeedsList() {
        return this.mFeedsList;
    }

    private List<LocaleInfo> getUserLocaleList() {
        List<LocaleInfo> result = new ArrayList();
        LocaleList localeList = LocalePicker.getLocales();
        for (int i = 0; i < localeList.size(); i++) {
            result.add(LocaleStore.getLocaleInfo(localeList.get(i)));
        }
        return result;
    }

    public boolean isSelectAll() {
        if (this.mFeedsList == null || this.mFeedsList.size() == 0) {
            return false;
        }
        for (LocaleInfo li : this.mFeedsList) {
            if (!li.getChecked()) {
                return false;
            }
        }
        return true;
    }

    public int getCheckedCount() {
        if (this.mFeedsList == null || this.mFeedsList.size() == 0) {
            return 0;
        }
        int result = 0;
        for (LocaleInfo li : this.mFeedsList) {
            if (li.getChecked()) {
                result++;
            }
        }
        return result;
    }

    public boolean isSelect() {
        if (this.mFeedsList == null || this.mFeedsList.size() == 0) {
            return false;
        }
        for (LocaleInfo mLocaleInfo : this.mFeedsList) {
            if (mLocaleInfo.getChecked()) {
                return true;
            }
        }
        return false;
    }

    public void setAllUnCheck() {
        if (this.mFeedsList != null && this.mFeedsList.size() != 0) {
            for (int i = 0; i < this.mFeedsList.size(); i++) {
                ((LocaleInfo) this.mFeedsList.get(i)).setChecked(false);
            }
        }
    }

    public void setAllCheck() {
        if (this.mFeedsList != null && this.mFeedsList.size() != 0) {
            for (int i = 0; i < this.mFeedsList.size(); i++) {
                ((LocaleInfo) this.mFeedsList.get(i)).setChecked(true);
            }
        }
    }

    public void removeChecked(Context context) {
        if (this.mFeedsList != null && this.mFeedsList.size() != 0) {
            int i;
            for (i = this.mFeedsList.size() - 1; i >= 0; i--) {
                if (((LocaleInfo) this.mFeedsList.get(i)).getChecked()) {
                    ItemUseStat.getInstance().handleClick(context, 2, "remove language", ((LocaleInfo) this.mFeedsList.get(i)).getLocale().toString());
                    this.mFeedsList.remove(i);
                }
            }
            int count = this.mFeedsList.size();
            Locale[] newList = new Locale[count];
            for (i = 0; i < count; i++) {
                newList[i] = ((LocaleInfo) this.mFeedsList.get(i)).getLocale();
            }
            LocaleList ll = new LocaleList(newList);
            try {
                LocaleList.setDefault(ll);
            } catch (NullPointerException e) {
                Log.d("LocaleListHelper", "ll is null");
                e.printStackTrace();
            } catch (IllegalArgumentException e2) {
                Log.d("LocaleListHelper", "ll is empty");
                e2.printStackTrace();
            }
            LocalePicker.updateLocales(ll);
        }
    }

    public static boolean isDefaultEngineNotSet(Context context) {
        boolean z = true;
        if (context == null) {
            return true;
        }
        if (Secure.getString(context.getContentResolver(), "tts_default_synth") != null) {
            z = false;
        }
        return z;
    }

    public static void updateEngine(Context context) {
        if (context != null) {
            List<LocaleInfo> feedsList = new LocaleListHelper().getFeedsList();
            PackageManager packageManager = context.getPackageManager();
            if (Utils.isChinaArea() && Utils.hasPackageInfo(packageManager, "com.iflytek.speechsuite") && ((LocaleInfo) feedsList.get(0)).toString().startsWith("zh")) {
                Secure.putString(context.getContentResolver(), "tts_default_synth", "com.iflytek.speechsuite");
            }
            if (Utils.isChinaArea() && Utils.hasPackageInfo(packageManager, "com.svox.pico") && !((LocaleInfo) feedsList.get(0)).toString().startsWith("zh")) {
                Secure.putString(context.getContentResolver(), "tts_default_synth", "com.svox.pico");
            }
        }
    }
}
