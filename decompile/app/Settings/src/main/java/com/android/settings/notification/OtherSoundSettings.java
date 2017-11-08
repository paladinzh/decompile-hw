package com.android.settings.notification;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.SearchIndexableResource;
import android.telephony.TelephonyManager;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OtherSoundSettings extends SettingsPreferenceFragment implements Indexable {
    private static final SettingPref[] PREFS = new SettingPref[]{PREF_DIAL_PAD_TONES, PREF_SCREEN_LOCKING_SOUNDS, PREF_CHARGING_SOUNDS, PREF_DOCKING_SOUNDS, PREF_TOUCH_SOUNDS, PREF_VIBRATE_ON_TOUCH, PREF_DOCK_AUDIO_MEDIA, PREF_EMERGENCY_TONE};
    private static final SettingPref PREF_CHARGING_SOUNDS = new SettingPref(1, "charging_sounds", "charging_sounds_enabled", 1, new int[0]);
    private static final SettingPref PREF_DIAL_PAD_TONES = new SettingPref(2, "dial_pad_tones", "dtmf_tone", 1, new int[0]) {
        public boolean isApplicable(Context context) {
            return Utils.isVoiceCapable(context);
        }
    };
    private static final SettingPref PREF_DOCKING_SOUNDS = new SettingPref(1, "docking_sounds", "dock_sounds_enabled", 1, new int[0]) {
        public boolean isApplicable(Context context) {
            return OtherSoundSettings.hasDockSettings(context);
        }
    };
    private static final SettingPref PREF_DOCK_AUDIO_MEDIA = new SettingPref(1, "dock_audio_media", "dock_audio_media_enabled", 0, 0, 1) {
        public boolean isApplicable(Context context) {
            return OtherSoundSettings.hasDockSettings(context);
        }

        protected String getCaption(Resources res, int value) {
            switch (value) {
                case 0:
                    return res.getString(2131626708);
                case 1:
                    return res.getString(2131626709);
                default:
                    throw new IllegalArgumentException();
            }
        }
    };
    private static final SettingPref PREF_EMERGENCY_TONE = new SettingPref(1, "emergency_tone", "emergency_tone", 0, 1, 2, 0) {
        public boolean isApplicable(Context context) {
            return TelephonyManager.getDefault().getCurrentPhoneType() == 2;
        }

        protected String getCaption(Resources res, int value) {
            switch (value) {
                case 0:
                    return res.getString(2131626710);
                case 1:
                    return res.getString(2131626711);
                case 2:
                    return res.getString(2131626712);
                default:
                    throw new IllegalArgumentException();
            }
        }
    };
    private static final SettingPref PREF_SCREEN_LOCKING_SOUNDS = new SettingPref(2, "screen_locking_sounds", "lockscreen_sounds_enabled", 1, new int[0]);
    private static final SettingPref PREF_TOUCH_SOUNDS = new SettingPref(2, "touch_sounds", "sound_effects_enabled", 1, new int[0]) {
        protected boolean setSetting(final Context context, final int value) {
            AsyncTask.execute(new Runnable() {
                public void run() {
                    AudioManager am = (AudioManager) context.getSystemService("audio");
                    if (value != 0) {
                        am.loadSoundEffects();
                    } else {
                        am.unloadSoundEffects();
                    }
                }
            });
            return super.setSetting(context, value);
        }
    };
    private static final SettingPref PREF_VIBRATE_ON_TOUCH = new SettingPref(2, "vibrate_on_touch", "haptic_feedback_enabled", 1, new int[0]) {
        public boolean isApplicable(Context context) {
            return OtherSoundSettings.hasHaptic(context);
        }
    };
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = 2131230828;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> rt = new ArrayList();
            for (SettingPref pref : OtherSoundSettings.PREFS) {
                if (!pref.isApplicable(context)) {
                    rt.add(pref.getKey());
                }
            }
            return rt;
        }
    };
    private Context mContext;
    private final SettingsObserver mSettingsObserver = new SettingsObserver();

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver() {
            super(new Handler());
        }

        public void register(boolean register) {
            ContentResolver cr = OtherSoundSettings.this.getContentResolver();
            if (register) {
                for (SettingPref pref : OtherSoundSettings.PREFS) {
                    cr.registerContentObserver(pref.getUri(), false, this);
                }
                return;
            }
            cr.unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            for (SettingPref pref : OtherSoundSettings.PREFS) {
                if (pref.getUri().equals(uri)) {
                    pref.update(OtherSoundSettings.this.mContext);
                    return;
                }
            }
        }
    }

    protected int getMetricsCategory() {
        return 73;
    }

    protected int getHelpResource() {
        return 2131626528;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230828);
        this.mContext = getActivity();
        for (SettingPref pref : PREFS) {
            pref.init(this);
        }
    }

    public void onResume() {
        super.onResume();
        this.mSettingsObserver.register(true);
    }

    public void onPause() {
        super.onPause();
        this.mSettingsObserver.register(false);
    }

    private static boolean hasDockSettings(Context context) {
        return context.getResources().getBoolean(2131492871);
    }

    private static boolean hasHaptic(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService("vibrator");
        return vibrator != null ? vibrator.hasVibrator() : false;
    }
}
