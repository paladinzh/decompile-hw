package com.android.settings;

import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.TwoStatePreference;
import com.huawei.bd.Reporter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONObject;

public class ItemUseStat {
    public static final String[] KEY_ANIMATOR_DURATION_SCALE = new String[]{"animator_duration_off", "animator_duration_scale_0_5x", "animator_duration_scale_1x", "animator_duration_scale_1_5x", "animator_duration_scale_2x", "animator_duration_scale_5x", "animator_duration_scale_10x"};
    public static final String[] KEY_BACKGROUND_PROCESS_LIMIT = new String[]{"background_process_limit_standard_limit", "background_process_limit_no_background_process", "background_process_limit_at_most_1_process", "background_process_limit_at_most_2_process", "background_process_limit_at_most_3_process", "background_process_limit_at_most_4_process"};
    public static final String[] KEY_BATTERY_PERCENTAGE = new String[]{"none", "next_to_batery_icon", "in_batery_icon"};
    public static final String[] KEY_CAPTION_FONT_SIZE = new String[]{"caption_fontsize_very_small,", "caption_fontsize_small", "caption_fontsize_very_normal", "caption_fontsize_large", "caption_fontsize_very_large"};
    public static final String[] KEY_CAPTION_STYLE = new String[]{"caption_style_custom", "caption_style_white_on_black", "caption_style_black_on_white", "caption_style_yellow_on_black", "caption_style_yellow_on_blue", "caption_style_use_app_defaults"};
    public static final String[] KEY_CORRECTION_MODE = new String[]{"correction_mode_deuteranomaly", "correction_mode_protanomaly", "correction_mode_tritanomaly"};
    public static final String[] KEY_DEBUG_GPU = new String[]{"debug_gpu_off", "debug_gpu_show_overdraw_areas", "debug_gpu_show_areas_for_deuteranomaly"};
    public static final String[] KEY_DEBUG_NON_RECTANGULAR_CLIP = new String[]{"debug_non_rectangular_clip_off", "debug_non_rectangular_clip_in_blue", "debug_non_rectangular_clip_hilight"};
    public static final String[] KEY_ENABLE_OPENGL_TRACES = new String[]{"enable_opengl_traces_none", "enable_opengl_traces_logcat", "enable_opengl_traces_systrace", "enable_opengl_traces_call_stack_on_glgeterror"};
    public static final String[] KEY_LISTPREFERENCE_ONE_USAGE_LIMIT = new String[]{"one_usage_no_limit", "one_usage_10MB", "one_usage_20MB", "one_usage_50MB", "one_usage_100MB", "one_usage_customal"};
    public static final String[] KEY_LISTPREFERENCE_TOUCH_HOLD_DELAY = new String[]{"touch_hold_delay_short,", "touch_hold_delay_middle", "touch_hold_delay_long"};
    public static final String[] KEY_LOCAL = new String[]{"zh_TW", "zh_HK", "zh_CN"};
    public static final String[] KEY_LOGGER_BUFFER_SIZE = new String[]{"logger_buffer_size_64KB", "logger_buffer_size_256KB", "logger_buffer_size_1MB", "logger_buffer_size_4MB", "logger_buffer_size_16MB"};
    public static final String[] KEY_NOTIFICATION_WAY = new String[]{"notification_way", "notification_icon", "none"};
    public static final String[] KEY_PROFILE_GPU_RENDORING = new String[]{"profile_gpu_rendoring_off", "profile_gpu_rendoring_on_screen_as_bars", "profile_gpu_rendoring_on_screen_as_lines"};
    public static final String[] KEY_SCHEDULED_POWER_OFF_REPEAT = new String[]{"scheduled_power_off_only_once", "scheduled_power_off_mondy_to_friday", "scheduled_power_off_working_days", "scheduled_power_off_everyday", "scheduled_power_off_custom"};
    public static final String[] KEY_SCHEDULED_POWER_ON_REPEAT = new String[]{"scheduled_power_on_only_once", "scheduled_power_on_mondy_to_friday", "scheduled_power_on_working_days", "scheduled_power_on_everyday", "scheduled_power_on_custom"};
    public static final String[] KEY_SELECT_USB_CONFIGURATION = new String[]{"select_usb_configuration_charging_only", "select_usb_configuration_mtp", "select_usb_configuration_ptp", "select_usb_configuration_rndis", "select_usb_configuration_audio_source", "select_usb_configuration_midi"};
    public static final String[] KEY_SIMULATE_COLOR_SPACE = new String[]{"simulate_color_space_off", "simulate_color_space_monochromacy", "simulate_color_space_deuteranomaly", "simulate_color_space_protanomaly", "simulate_color_space_tritanomaly"};
    public static final String[] KEY_SIMULATE_SECONDARY_DISPLAYS = new String[]{"simulate_secondary_none", "simulate_secondary_480p", "simulate_secondary_480p(secure)", "simulate_secondary_720p", "simulate_secondary_720p(secure)", "simulate_secondary_1080p", "simulate_secondary_1080p(secure)", "simulate_secondary_4k", "simulate_secondary_4k(secure)", "simulate_secondary_4k(upscaled)", "simulate_secondary_4k(upscaled,secure)", "simulate_secondary_720p,1080p(dual screen)"};
    public static final String[] KEY_TRANSLATION_ANIMATION_SCALE = new String[]{"translation_animation_off", "translation_animation_scale_0_5x", "translation_animation_scale_1x", "translation_animation_scale_1_5x", "translation_animation_scale_2x", "translation_animation_scale_5x", "translation_animation_scale_10x"};
    public static final String[] KEY_VOLUME_STYLE = new String[]{"volume_stream_music", "volume_stream_ring", "volume_stream_notification", "volume_stream_alarm", "volume_stream_voice_call"};
    public static final String[] KEY_WFC_MODE = new String[]{"mobile_network_preferred", "wifi_preferred", "4G_network_preferred", "wifi_only"};
    public static final String[] KEY_WINDOW_ANIMATION_SCALE = new String[]{"window_animation_off", "window_animation_scale_0_5x", "window_animation_scale_1x", "window_animation_scale_1_5x", "window_animation_scale_2x", "window_animation_scale_5x", "window_animation_scale_10x"};
    private static ItemUseStat sInstance = null;

    public static synchronized ItemUseStat getInstance() {
        ItemUseStat itemUseStat;
        synchronized (ItemUseStat.class) {
            if (sInstance == null) {
                sInstance = new ItemUseStat();
            }
            itemUseStat = sInstance;
        }
        return itemUseStat;
    }

    public void handleTwoStatePreferenceClick(Context context, Preference preference, Object value) {
        if (context != null && preference != null && value != null && (preference instanceof TwoStatePreference)) {
            boolean isChecked = ((Boolean) value).booleanValue();
            if (((TwoStatePreference) preference).isChecked() != isChecked) {
                handleClick(context, 3, preference.getKey(), isChecked ? "on" : "off");
            }
        }
    }

    public void handleNonTwoStatePreferenceClick(Context context, Preference preference) {
        if (context != null && preference != null && !(preference instanceof TwoStatePreference)) {
            handleClick(context, 2, preference.getKey());
        }
    }

    private ItemUseStat() {
    }

    public void handleClick(Context context, int level, String name, int value) {
        handleClick(context, level, name, String.valueOf(value));
    }

    public void handleClick(Context context, int level, String name) {
        if (!Utils.isMonkeyRunning()) {
            String itemName = getShortName(name);
            Map<String, String> mapObj = new LinkedHashMap();
            mapObj.put("name", itemName);
            handleReport(context, level, new JSONObject(mapObj));
        }
    }

    public void handleClick(Context context, int level, String name, String status) {
        if (!Utils.isMonkeyRunning()) {
            String itemName = getShortName(name);
            Map<String, String> mapObj = new LinkedHashMap();
            mapObj.put("name", itemName);
            mapObj.put("status", status);
            handleReport(context, level, new JSONObject(mapObj));
        }
    }

    public void handleClickListPreference(Context context, ListPreference listPreference, CharSequence[] entryValues, String value) {
        if (listPreference != null && (listPreference instanceof ListPreference) && !value.equals(listPreference.getValue())) {
            CharSequence[] values = listPreference.getEntryValues();
            if (values != null) {
                int best = 0;
                for (int i = 0; i < values.length; i++) {
                    if (value.equals(values[i].toString())) {
                        best = i;
                        break;
                    }
                }
                String newValue = "";
                if (entryValues[best] != null) {
                    newValue = entryValues[best].toString();
                }
                handleClick(context, 2, listPreference.getKey(), newValue);
            }
        }
    }

    public void handleClickLocalListPreference(Context context, ListPreference listPreference, String[] entryValues, String value) {
        if (listPreference != null && (listPreference instanceof ListPreference) && !value.equals(listPreference.getValue())) {
            if ("".equals(value)) {
                handleClick(context, 2, listPreference.getKey(), "local_default");
                return;
            }
            for (String str : entryValues) {
                if (value.equals(str.toString())) {
                    handleClick(context, 2, listPreference.getKey(), value);
                    return;
                }
            }
        }
    }

    public void handleClickListDialogPreference(Context context, String[] entryValues, String name, int value) {
        int index = value + 1;
        if (index >= 0 && index < entryValues.length) {
            handleClick(context, 2, name, entryValues[index]);
        }
    }

    public void cacheData(Context context) {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleReport(Context context, int level, JSONObject jsonData) {
        if (!(jsonData == null || context == null || jsonData.length() <= 0)) {
            Reporter.e(context, level, jsonData.toString());
        }
    }

    public static String getShortName(String name) {
        if (name == null) {
            return null;
        }
        return name.substring(name.lastIndexOf(".") + 1);
    }

    public void handleOnPreferenceChange(Context context, Preference preference, Object newValue) {
        if (preference instanceof TwoStatePreference) {
            handleTwoStatePreferenceClick(context, preference, newValue);
        } else if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            handleClickListPreference(context, listPreference, listPreference.getEntryValues(), (String) newValue);
        }
    }
}
