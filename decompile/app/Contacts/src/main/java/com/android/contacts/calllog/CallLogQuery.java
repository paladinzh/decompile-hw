package com.android.contacts.calllog;

import android.database.Cursor;
import com.android.contacts.compatibility.CompatUtils;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.EmuiFeatureManager;
import com.google.common.collect.Lists;
import java.util.List;

public final class CallLogQuery {
    public static final int BASE_COLUMN_SIZE = _PROJECTION.length;
    static final Object[] DEFAULT_BASE_VALUE;
    private static final Object[] DEFAULT_BASE_VALUE_INTERNAL = new Object[]{Long.valueOf(0), "", Long.valueOf(0), Long.valueOf(0), Integer.valueOf(0), "", "", "", null, Integer.valueOf(0), null, null, null, null, Long.valueOf(0), null, Integer.valueOf(0), Integer.valueOf(-1), null, Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(0), null, null, Integer.valueOf(0), Integer.valueOf(0)};
    static String[] EXTENDED_PROJECTION;
    static String[] HAP_PROJECTION;
    public static final int IS_PRIMARY = _PROJECTION_INTERNAL.length;
    public static final int POST_DIAL_DIGITS;
    private static int RING_TIMES;
    private static int SECTION;
    private static int SUBCRIPTION;
    public static final int SUBJECT = (_PROJECTION_INTERNAL.length + 1);
    static final String[] _PROJECTION;
    private static final String[] _PROJECTION_INTERNAL = new String[]{"_id", "number", "date", "duration", "type", "countryiso", "voicemail_uri", "geocoded_location", "name", "numbertype", "numberlabel", "lookup_uri", "matched_number", "normalized_number", "photo_id", "formatted_number", "is_read", "presentation", "subscription_component_name", "subscription_id", "features", "data_usage", "transcription", "mark_type", "mark_content", "is_cloud_mark", "mark_count", "call_type"};
    private static final String[] _PROJECTION_RCS_EXTEND = new String[]{"is_primary", "subject"};

    static {
        List<String> projectionList = Lists.newArrayList(_PROJECTION_INTERNAL);
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            projectionList.addAll(Lists.newArrayList(_PROJECTION_RCS_EXTEND));
        }
        List<Object> defaultBaseValueList = Lists.newArrayList(DEFAULT_BASE_VALUE_INTERNAL);
        if (CompatUtils.isNCompatible()) {
            projectionList.add("post_dial_digits");
            POST_DIAL_DIGITS = projectionList.size() - 1;
            defaultBaseValueList.add("");
        } else {
            POST_DIAL_DIGITS = 0;
        }
        _PROJECTION = (String[]) projectionList.toArray(new String[projectionList.size()]);
        DEFAULT_BASE_VALUE = defaultBaseValueList.toArray(new Object[defaultBaseValueList.size()]);
        rebuild();
    }

    public static int getSubscriptionColumnIndex() {
        return SUBCRIPTION;
    }

    public static int getMarkTypeColumnIndex() {
        return 23;
    }

    public static int getMarkContentColumnIndex() {
        return 24;
    }

    public static int getIsCloudMarkColumnIndex() {
        return 25;
    }

    public static int getMarkCountColumnIndex() {
        return 26;
    }

    private static void rebuild() {
        int current_column_index;
        int current_column_index2 = BASE_COLUMN_SIZE;
        boolean isRingTimeDisplEnabled = EmuiFeatureManager.isRingTimesDisplayEnabled(null);
        if (QueryUtil.isSupportDualSim()) {
            current_column_index2++;
        }
        if (isRingTimeDisplEnabled) {
            current_column_index2++;
        }
        HAP_PROJECTION = new String[updateCurrentColumnIndexForEncryptCall(current_column_index2)];
        System.arraycopy(_PROJECTION, 0, HAP_PROJECTION, 0, _PROJECTION.length);
        current_column_index2 = BASE_COLUMN_SIZE;
        if (QueryUtil.isSupportDualSim()) {
            SUBCRIPTION = current_column_index2;
            current_column_index = current_column_index2 + 1;
            HAP_PROJECTION[current_column_index2] = "subscription";
        } else {
            SUBCRIPTION = 0;
            current_column_index = current_column_index2;
        }
        if (isRingTimeDisplEnabled) {
            RING_TIMES = current_column_index;
            current_column_index2 = current_column_index + 1;
            HAP_PROJECTION[current_column_index] = "ring_times";
        } else {
            RING_TIMES = 0;
            current_column_index2 = current_column_index;
        }
        updateHapProjectionForEncryptCall(HAP_PROJECTION, current_column_index2);
        EXTENDED_PROJECTION = new String[(HAP_PROJECTION.length + 1)];
        System.arraycopy(HAP_PROJECTION, 0, EXTENDED_PROJECTION, 0, HAP_PROJECTION.length);
        EXTENDED_PROJECTION[HAP_PROJECTION.length] = "section";
        SECTION = HAP_PROJECTION.length;
    }

    private static void checkAndRebuild() {
        if (!(EmuiFeatureManager.isRingTimesDisplayEnabled(null) && RING_TIMES == 0)) {
            if (!(EmuiFeatureManager.isRingTimesDisplayEnabled(null) || RING_TIMES == 0)) {
            }
            if (!(QueryUtil.isSupportDualSim() && SUBCRIPTION == 0)) {
                if (!!QueryUtil.isSupportDualSim() || SUBCRIPTION == 0) {
                }
            }
            rebuild();
            return;
        }
        rebuild();
        if (!QueryUtil.isSupportDualSim()) {
        }
    }

    public static String[] getProjection() {
        checkAndRebuild();
        String[] projection = new String[HAP_PROJECTION.length];
        System.arraycopy(HAP_PROJECTION, 0, projection, 0, HAP_PROJECTION.length);
        return projection;
    }

    public static Object[] getDefaultExtendedRowForSection(int section) {
        checkAndRebuild();
        Object[] row = new Object[EXTENDED_PROJECTION.length];
        System.arraycopy(DEFAULT_BASE_VALUE, 0, row, 0, BASE_COLUMN_SIZE);
        int i = BASE_COLUMN_SIZE;
        if (QueryUtil.isSupportDualSim()) {
            int current_column_index = i + 1;
            row[i] = Long.valueOf(0);
            i = current_column_index;
        }
        if (EmuiFeatureManager.isRingTimesDisplayEnabled(null)) {
            current_column_index = i + 1;
            row[i] = Long.valueOf(0);
            i = current_column_index;
        }
        row[i] = Integer.valueOf(section);
        return row;
    }

    public static boolean isSectionHeader(Cursor cursor) {
        int section = cursor.getInt(SECTION);
        if (section == 0 || section == 2) {
            return true;
        }
        return false;
    }

    private static void updateHapProjectionForEncryptCall(String[] hAP_PROJECTION, int current_column_index) {
        hAP_PROJECTION[current_column_index] = "encrypt_call";
    }

    private static int updateCurrentColumnIndexForEncryptCall(int current_column_index) {
        return current_column_index + 1;
    }
}
