package com.android.settings.applications;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.service.voice.VoiceInteractionServiceInfo;
import android.util.AttributeSet;
import android.util.Log;
import com.android.internal.app.AssistUtils;
import com.android.settings.AppListPreferenceWithSettings;
import java.util.ArrayList;
import java.util.List;

public class DefaultAssistPreference extends AppListPreferenceWithSettings {
    private static final String TAG = DefaultAssistPreference.class.getSimpleName();
    private final AssistUtils mAssistUtils;
    private final List<Info> mAvailableAssistants = new ArrayList();

    private static class Info {
        public final ComponentName component;
        public final VoiceInteractionServiceInfo voiceInteractionServiceInfo;

        Info(ComponentName component) {
            this.component = component;
            this.voiceInteractionServiceInfo = null;
        }

        Info(ComponentName component, VoiceInteractionServiceInfo voiceInteractionServiceInfo) {
            this.component = component;
            this.voiceInteractionServiceInfo = voiceInteractionServiceInfo;
        }

        public boolean isVoiceInteractionService() {
            return this.voiceInteractionServiceInfo != null;
        }
    }

    public DefaultAssistPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setShowItemNone(true);
        setDialogTitle(2131626946);
        this.mAssistUtils = new AssistUtils(context);
    }

    protected boolean persistString(String value) {
        Info info = findAssistantByPackageName(value);
        if (info == null) {
            setAssistNone();
            return true;
        }
        if (info.isVoiceInteractionService()) {
            setAssistService(info);
        } else {
            setAssistActivity(info);
        }
        return true;
    }

    private void setAssistNone() {
        Secure.putString(getContext().getContentResolver(), "assistant", "");
        Secure.putString(getContext().getContentResolver(), "voice_interaction_service", "");
        Secure.putString(getContext().getContentResolver(), "voice_recognition_service", getDefaultRecognizer());
        setSummary(getContext().getText(2131626945));
        setSettingsComponent(null);
    }

    private void setAssistService(Info serviceInfo) {
        ComponentName componentName = null;
        String serviceComponentName = serviceInfo.component.flattenToShortString();
        String serviceRecognizerName = new ComponentName(serviceInfo.component.getPackageName(), serviceInfo.voiceInteractionServiceInfo.getRecognitionService()).flattenToShortString();
        Secure.putString(getContext().getContentResolver(), "assistant", serviceComponentName);
        Secure.putString(getContext().getContentResolver(), "voice_interaction_service", serviceComponentName);
        Secure.putString(getContext().getContentResolver(), "voice_recognition_service", serviceRecognizerName);
        setSummary(getEntry());
        String settingsActivity = serviceInfo.voiceInteractionServiceInfo.getSettingsActivity();
        if (settingsActivity != null) {
            componentName = new ComponentName(serviceInfo.component.getPackageName(), settingsActivity);
        }
        setSettingsComponent(componentName);
    }

    private void setAssistActivity(Info activityInfo) {
        Secure.putString(getContext().getContentResolver(), "assistant", activityInfo.component.flattenToShortString());
        Secure.putString(getContext().getContentResolver(), "voice_interaction_service", "");
        Secure.putString(getContext().getContentResolver(), "voice_recognition_service", getDefaultRecognizer());
        setSummary(getEntry());
        setSettingsComponent(null);
    }

    private String getDefaultRecognizer() {
        ResolveInfo resolveInfo = getContext().getPackageManager().resolveService(new Intent("android.speech.RecognitionService"), 128);
        if (resolveInfo != null && resolveInfo.serviceInfo != null) {
            return new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name).flattenToShortString();
        }
        Log.w(TAG, "Unable to resolve default voice recognition service.");
        return "";
    }

    private Info findAssistantByPackageName(String packageName) {
        for (int i = 0; i < this.mAvailableAssistants.size(); i++) {
            Info info = (Info) this.mAvailableAssistants.get(i);
            if (info.component.getPackageName().equals(packageName)) {
                return info;
            }
        }
        return null;
    }

    private void addAssistServices() {
        PackageManager pm = getContext().getPackageManager();
        List<ResolveInfo> services = pm.queryIntentServices(new Intent("android.service.voice.VoiceInteractionService"), 128);
        for (int i = 0; i < services.size(); i++) {
            ResolveInfo resolveInfo = (ResolveInfo) services.get(i);
            VoiceInteractionServiceInfo voiceInteractionServiceInfo = new VoiceInteractionServiceInfo(pm, resolveInfo.serviceInfo);
            if (voiceInteractionServiceInfo.getSupportsAssist()) {
                this.mAvailableAssistants.add(new Info(new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name), voiceInteractionServiceInfo));
            }
        }
    }

    private void addAssistActivities() {
        List<ResolveInfo> activities = getContext().getPackageManager().queryIntentActivities(new Intent("android.intent.action.ASSIST"), 65536);
        for (int i = 0; i < activities.size(); i++) {
            ResolveInfo resolveInfo = (ResolveInfo) activities.get(i);
            this.mAvailableAssistants.add(new Info(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name)));
        }
    }

    public ComponentName getCurrentAssist() {
        return this.mAssistUtils.getAssistComponentForUser(UserHandle.myUserId());
    }

    public void refreshAssistApps() {
        CharSequence charSequence = null;
        this.mAvailableAssistants.clear();
        addAssistServices();
        addAssistActivities();
        List<String> packages = new ArrayList();
        for (int i = 0; i < this.mAvailableAssistants.size(); i++) {
            String packageName = ((Info) this.mAvailableAssistants.get(i)).component.getPackageName();
            if (!packages.contains(packageName)) {
                packages.add(packageName);
            }
        }
        ComponentName currentAssist = getCurrentAssist();
        CharSequence[] charSequenceArr = (CharSequence[]) packages.toArray(new String[packages.size()]);
        if (currentAssist != null) {
            charSequence = currentAssist.getPackageName();
        }
        setPackageNames(charSequenceArr, charSequence);
    }
}
