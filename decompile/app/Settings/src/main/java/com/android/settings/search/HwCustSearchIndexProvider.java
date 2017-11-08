package com.android.settings.search;

import android.content.Context;
import android.content.res.Resources;
import android.provider.SearchIndexableResource;
import java.util.List;

public class HwCustSearchIndexProvider {
    public List<SearchIndexableResource> addSoundXmlResourcesToIndex(Context context, List<SearchIndexableResource> searchIdxRes) {
        return searchIdxRes;
    }

    public List<SearchIndexableRaw> addSoundRawDataToIndex(Context context, List<SearchIndexableRaw> searchIdxRaw, Resources res) {
        return searchIdxRaw;
    }

    public List<String> addSoundNonIndexableKeys(Context context, List<String> keys) {
        return keys;
    }

    public List<SearchIndexableResource> addDisplayXmlResourcesToIndex(Context context, List<SearchIndexableResource> searchIdxRes) {
        return searchIdxRes;
    }

    public List<SearchIndexableRaw> addDisplayRawDataToIndex(Context context, List<SearchIndexableRaw> searchIdxRaw, Resources res) {
        return searchIdxRaw;
    }

    public List<String> addDisplayNonIndexableKeys(Context context, List<String> keys) {
        return keys;
    }

    public List<SearchIndexableResource> addMoreAssistanceXmlResourcesToIndex(Context context, List<SearchIndexableResource> searchIdxRes) {
        return searchIdxRes;
    }

    public List<SearchIndexableRaw> addOtherAppsRawDataToIndex(Context context, List<SearchIndexableRaw> searchIdxRaw, Resources res) {
        return searchIdxRaw;
    }

    public List<String> addMoreAssistanceNonIndexableKeys(Context context, List<String> keys) {
        return keys;
    }

    public List<SearchIndexableRaw> addLteSwitchRawDataToIndex(Context context, List<SearchIndexableRaw> searchIdxRaw, Resources res) {
        return searchIdxRaw;
    }

    public List<SearchIndexableResource> addDateTimeXmlResourcesToIndex(Context context, List<SearchIndexableResource> searchIdxRes) {
        return searchIdxRes;
    }

    public List<String> addDateTimeNonIndexableKeys(Context context, List<String> searchIdxRes) {
        return searchIdxRes;
    }

    public List<SearchIndexableRaw> addFingerprintMainRawDataToIndex(Context context, List<SearchIndexableRaw> searchIdxRaw, Resources res) {
        return searchIdxRaw;
    }

    public List<String> addDeviceInfoNonIndexableKeys(Context context, List<String> keys) {
        return keys;
    }

    public boolean hasIndexForNavigation() {
        return false;
    }

    public List<SearchIndexableRaw> addMoreAssistanceRawDataToIndex(Context context, List<SearchIndexableRaw> searchIdxRaw, Resources res) {
        return searchIdxRaw;
    }
}
