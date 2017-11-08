package com.android.settings.voice;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings.Secure;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import com.android.settings.AppListPreferenceWithSettings;
import com.android.settings.voice.VoiceInputHelper.InteractionInfo;
import com.android.settings.voice.VoiceInputHelper.RecognizerInfo;
import java.util.ArrayList;
import java.util.List;

public class VoiceInputListPreference extends AppListPreferenceWithSettings {
    private ComponentName mAssistRestrict;
    private final List<Integer> mAvailableIndexes = new ArrayList();
    private VoiceInputHelper mHelper;

    private class CustomAdapter extends ArrayAdapter<CharSequence> {
        public CustomAdapter(Context context, CharSequence[] objects) {
            super(context, 17367266, 16908308, objects);
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public boolean isEnabled(int position) {
            return VoiceInputListPreference.this.mAvailableIndexes.contains(Integer.valueOf(position));
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            view.setEnabled(isEnabled(position));
            return view;
        }
    }

    public VoiceInputListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogTitle(2131626951);
    }

    protected ListAdapter createListAdapter() {
        super.createListAdapter();
        return new CustomAdapter(getContext(), getEntries());
    }

    protected boolean persistString(String value) {
        int i;
        for (i = 0; i < this.mHelper.mAvailableInteractionInfos.size(); i++) {
            InteractionInfo info = (InteractionInfo) this.mHelper.mAvailableInteractionInfos.get(i);
            if (info.key.equals(value)) {
                Secure.putString(getContext().getContentResolver(), "voice_interaction_service", value);
                Secure.putString(getContext().getContentResolver(), "voice_recognition_service", new ComponentName(info.service.packageName, info.serviceInfo.getRecognitionService()).flattenToShortString());
                setSummary(getEntry());
                setSettingsComponent(info.settings);
                return true;
            }
        }
        for (i = 0; i < this.mHelper.mAvailableRecognizerInfos.size(); i++) {
            RecognizerInfo info2 = (RecognizerInfo) this.mHelper.mAvailableRecognizerInfos.get(i);
            if (info2.key.equals(value)) {
                Secure.putString(getContext().getContentResolver(), "voice_interaction_service", "");
                Secure.putString(getContext().getContentResolver(), "voice_recognition_service", value);
                setSummary(getEntry());
                setSettingsComponent(info2.settings);
                return true;
            }
        }
        setSettingsComponent(null);
        return true;
    }

    public void setPackageNames(CharSequence[] packageNames, CharSequence defaultPackageName) {
        super.setPackageNames(packageNames, defaultPackageName);
    }

    public void setAssistRestrict(ComponentName assistRestrict) {
        this.mAssistRestrict = assistRestrict;
    }

    public void refreshVoiceInputs() {
        int i;
        this.mHelper = new VoiceInputHelper(getContext());
        this.mHelper.buildUi();
        String assistKey = this.mAssistRestrict == null ? "" : this.mAssistRestrict.flattenToShortString();
        this.mAvailableIndexes.clear();
        List<CharSequence> entries = new ArrayList();
        List<CharSequence> values = new ArrayList();
        for (i = 0; i < this.mHelper.mAvailableInteractionInfos.size(); i++) {
            InteractionInfo info = (InteractionInfo) this.mHelper.mAvailableInteractionInfos.get(i);
            entries.add(info.appLabel);
            values.add(info.key);
            if (info.key.contentEquals(assistKey)) {
                this.mAvailableIndexes.add(Integer.valueOf(i));
            }
        }
        boolean assitIsService = !this.mAvailableIndexes.isEmpty();
        int serviceCount = entries.size();
        for (i = 0; i < this.mHelper.mAvailableRecognizerInfos.size(); i++) {
            RecognizerInfo info2 = (RecognizerInfo) this.mHelper.mAvailableRecognizerInfos.get(i);
            entries.add(info2.label);
            values.add(info2.key);
            if (!assitIsService) {
                this.mAvailableIndexes.add(Integer.valueOf(serviceCount + i));
            }
        }
        setEntries((CharSequence[]) entries.toArray(new CharSequence[entries.size()]));
        setEntryValues((CharSequence[]) values.toArray(new CharSequence[values.size()]));
        setEnabled(!entries.isEmpty());
        if (this.mHelper.mCurrentVoiceInteraction != null) {
            setValue(this.mHelper.mCurrentVoiceInteraction.flattenToShortString());
        } else if (this.mHelper.mCurrentRecognizer != null) {
            setValue(this.mHelper.mCurrentRecognizer.flattenToShortString());
        } else {
            setValue(null);
        }
    }

    public ComponentName getCurrentService() {
        if (this.mHelper.mCurrentVoiceInteraction != null) {
            return this.mHelper.mCurrentVoiceInteraction;
        }
        if (this.mHelper.mCurrentRecognizer != null) {
            return this.mHelper.mCurrentRecognizer;
        }
        return null;
    }
}
