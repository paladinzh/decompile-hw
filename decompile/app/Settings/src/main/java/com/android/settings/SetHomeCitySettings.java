package com.android.settings;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.Settings.ZonePickerActivity;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.xmlpull.v1.XmlPullParserException;

public class SetHomeCitySettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private Preference mHomeCityTimeZone;
    private CustomSwitchPreference mSwitchPreference;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230887);
        this.mSwitchPreference = (CustomSwitchPreference) findPreference("dual_clocks_switch");
        this.mSwitchPreference.setOnPreferenceChangeListener(this);
        this.mHomeCityTimeZone = findPreference("set_home_city");
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        if (activity.getActionBar() != null) {
            activity.getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onResume() {
        boolean z = true;
        super.onResume();
        updatePreferences();
        if (this.mSwitchPreference != null) {
            try {
                CustomSwitchPreference customSwitchPreference = this.mSwitchPreference;
                if (System.getIntForUser(getContentResolver(), "dual_clocks", ActivityManager.getCurrentUser()) != 1) {
                    z = false;
                }
                customSwitchPreference.setChecked(z);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if ("set_home_city".equals(preference.getKey())) {
            ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
            Intent intent = new Intent();
            intent.setClass(getActivity(), ZonePickerActivity.class);
            intent.putExtra("request_type", 1);
            startActivity(intent);
        }
        return super.onPreferenceTreeClick(preference);
    }

    private String getTimeZoneOffset(String id, long date) {
        int offset = TimeZone.getTimeZone(id).getOffset(date);
        int p = Math.abs(offset);
        StringBuilder name = new StringBuilder();
        name.append("GMT");
        if (offset < 0) {
            name.append('-');
        } else {
            name.append('+');
        }
        name.append(p / 3600000);
        name.append(':');
        int min = (p / 60000) % 60;
        if (min < 10) {
            name.append('0');
        }
        name.append(min);
        return name.toString();
    }

    private String getTimeZoneName(String timeZoneId) {
        String timeZoneName = "";
        String homeCityId = System.getStringForUser(getContentResolver(), "keyguard_dual_clocks_home_city_id", ActivityManager.getCurrentUser());
        XmlResourceParser xmlResourceParser = null;
        if (timeZoneId == null) {
            return timeZoneName;
        }
        try {
            xmlResourceParser = getResources().getXml(2131230913);
            do {
            } while (xmlResourceParser.next() != 2);
            xmlResourceParser.next();
            while (xmlResourceParser.getEventType() != 3) {
                while (xmlResourceParser.getEventType() != 2) {
                    if (xmlResourceParser.getEventType() == 1) {
                        if (xmlResourceParser != null) {
                            xmlResourceParser.close();
                        }
                        return timeZoneName;
                    }
                    xmlResourceParser.next();
                }
                if (xmlResourceParser.getName().equals("timezone")) {
                    String id = xmlResourceParser.getAttributeValue(null, "id");
                    String city = xmlResourceParser.getAttributeValue(null, "city");
                    if (timeZoneId.equals(id)) {
                        if (TextUtils.isEmpty(homeCityId)) {
                            timeZoneName = xmlResourceParser.nextText();
                            if (xmlResourceParser != null) {
                                xmlResourceParser.close();
                            }
                            return timeZoneName;
                        } else if (homeCityId.equals(city)) {
                            timeZoneName = xmlResourceParser.nextText();
                            if (xmlResourceParser != null) {
                                xmlResourceParser.close();
                            }
                            return timeZoneName;
                        }
                    }
                }
                while (xmlResourceParser.getEventType() != 3) {
                    xmlResourceParser.next();
                }
                xmlResourceParser.next();
            }
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (XmlPullParserException e) {
            Log.e("SetHomeCitySettings", "Ill-formatted timezones.xml file");
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (IOException e2) {
            Log.e("SetHomeCitySettings", "Unable to read timezones.xml file");
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (Throwable th) {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        }
        return timeZoneName;
    }

    private void updatePreferences() {
        String timeZoneId = System.getStringForUser(getContentResolver(), "keyguard_default_time_zone", ActivityManager.getCurrentUser());
        long l = Calendar.getInstance().getTimeInMillis();
        StringBuilder homeCity = new StringBuilder();
        if (timeZoneId != null) {
            homeCity.append(getTimeZoneName(timeZoneId));
            homeCity.append(" (");
            homeCity.append(getTimeZoneOffset(timeZoneId, l));
            homeCity.append(")");
            if (this.mHomeCityTimeZone != null) {
                this.mHomeCityTimeZone.setSummary(homeCity.toString());
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int i = 0;
        if (this.mSwitchPreference != preference) {
            return false;
        }
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
        Boolean value = (Boolean) newValue;
        ContentResolver contentResolver = getContentResolver();
        String str = "dual_clocks";
        if (value.booleanValue()) {
            i = 1;
        }
        System.putIntForUser(contentResolver, str, i, ActivityManager.getCurrentUser());
        return true;
    }

    public void onPause() {
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
    }
}
