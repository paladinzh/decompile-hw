package com.android.settings.voice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.provider.Settings.Secure;
import android.service.voice.VoiceInteractionServiceInfo;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

public final class VoiceInputHelper {
    final ArrayList<InteractionInfo> mAvailableInteractionInfos = new ArrayList();
    final List<ResolveInfo> mAvailableRecognition;
    final ArrayList<RecognizerInfo> mAvailableRecognizerInfos = new ArrayList();
    final List<ResolveInfo> mAvailableVoiceInteractions;
    final Context mContext;
    ComponentName mCurrentRecognizer;
    ComponentName mCurrentVoiceInteraction;

    public static class BaseInfo implements Comparable {
        public final CharSequence appLabel;
        public final ComponentName componentName;
        public final String key = this.componentName.flattenToShortString();
        public final CharSequence label;
        public final String labelStr;
        public final ServiceInfo service;
        public final ComponentName settings;

        public BaseInfo(PackageManager pm, ServiceInfo _service, String _settings) {
            ComponentName componentName = null;
            this.service = _service;
            this.componentName = new ComponentName(_service.packageName, _service.name);
            if (_settings != null) {
                componentName = new ComponentName(_service.packageName, _settings);
            }
            this.settings = componentName;
            this.label = _service.loadLabel(pm);
            this.labelStr = this.label.toString();
            this.appLabel = _service.applicationInfo.loadLabel(pm);
        }

        public int compareTo(Object another) {
            return this.labelStr.compareTo(((BaseInfo) another).labelStr);
        }
    }

    public static class InteractionInfo extends BaseInfo {
        public final VoiceInteractionServiceInfo serviceInfo;

        public InteractionInfo(PackageManager pm, VoiceInteractionServiceInfo _service) {
            super(pm, _service.getServiceInfo(), _service.getSettingsActivity());
            this.serviceInfo = _service;
        }
    }

    public static class RecognizerInfo extends BaseInfo {
        public RecognizerInfo(PackageManager pm, ServiceInfo _service, String _settings) {
            super(pm, _service, _settings);
        }
    }

    public VoiceInputHelper(Context context) {
        this.mContext = context;
        this.mAvailableVoiceInteractions = this.mContext.getPackageManager().queryIntentServices(new Intent("android.service.voice.VoiceInteractionService"), 128);
        this.mAvailableRecognition = this.mContext.getPackageManager().queryIntentServices(new Intent("android.speech.RecognitionService"), 128);
    }

    public void buildUi() {
        int i;
        String currentSetting = Secure.getString(this.mContext.getContentResolver(), "voice_interaction_service");
        if (currentSetting == null || currentSetting.isEmpty()) {
            this.mCurrentVoiceInteraction = null;
        } else {
            this.mCurrentVoiceInteraction = ComponentName.unflattenFromString(currentSetting);
        }
        ArraySet<ComponentName> interactorRecognizers = new ArraySet();
        int size = this.mAvailableVoiceInteractions.size();
        for (i = 0; i < size; i++) {
            ResolveInfo resolveInfo = (ResolveInfo) this.mAvailableVoiceInteractions.get(i);
            VoiceInteractionServiceInfo info = new VoiceInteractionServiceInfo(this.mContext.getPackageManager(), resolveInfo.serviceInfo);
            if (info.getParseError() != null) {
                Log.w("VoiceInteractionService", "Error in VoiceInteractionService " + resolveInfo.serviceInfo.packageName + "/" + resolveInfo.serviceInfo.name + ": " + info.getParseError());
            } else {
                this.mAvailableInteractionInfos.add(new InteractionInfo(this.mContext.getPackageManager(), info));
                interactorRecognizers.add(new ComponentName(resolveInfo.serviceInfo.packageName, info.getRecognitionService()));
            }
        }
        Collections.sort(this.mAvailableInteractionInfos);
        currentSetting = Secure.getString(this.mContext.getContentResolver(), "voice_recognition_service");
        if (currentSetting == null || currentSetting.isEmpty()) {
            this.mCurrentRecognizer = null;
        } else {
            this.mCurrentRecognizer = ComponentName.unflattenFromString(currentSetting);
        }
        size = this.mAvailableRecognition.size();
        i = 0;
        while (i < size) {
            ServiceInfo si;
            XmlResourceParser xmlResourceParser;
            String str;
            resolveInfo = (ResolveInfo) this.mAvailableRecognition.get(i);
            if (interactorRecognizers.contains(new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name))) {
                si = resolveInfo.serviceInfo;
                xmlResourceParser = null;
                str = null;
            } else {
                si = resolveInfo.serviceInfo;
                xmlResourceParser = null;
                str = null;
            }
            try {
                xmlResourceParser = si.loadXmlMetaData(this.mContext.getPackageManager(), "android.speech");
                if (xmlResourceParser == null) {
                    throw new XmlPullParserException("No android.speech meta-data for " + si.packageName);
                }
                Resources res = this.mContext.getPackageManager().getResourcesForApplication(si.applicationInfo);
                AttributeSet attrs = Xml.asAttributeSet(xmlResourceParser);
                int type;
                do {
                    type = xmlResourceParser.next();
                    if (type == 1) {
                        break;
                    }
                } while (type != 2);
                if ("recognition-service".equals(xmlResourceParser.getName())) {
                    TypedArray array = res.obtainAttributes(attrs, R.styleable.RecognitionService);
                    str = array.getString(0);
                    array.recycle();
                    if (xmlResourceParser != null) {
                        xmlResourceParser.close();
                    }
                    this.mAvailableRecognizerInfos.add(new RecognizerInfo(this.mContext.getPackageManager(), resolveInfo.serviceInfo, str));
                    i++;
                } else {
                    throw new XmlPullParserException("Meta-data does not start with recognition-service tag");
                }
            } catch (XmlPullParserException e) {
                Log.e("VoiceInputHelper", "error parsing recognition service meta-data", e);
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
            } catch (IOException e2) {
                Log.e("VoiceInputHelper", "error parsing recognition service meta-data", e2);
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
            } catch (NameNotFoundException e3) {
                Log.e("VoiceInputHelper", "error parsing recognition service meta-data", e3);
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
            } catch (Throwable th) {
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
            }
        }
        Collections.sort(this.mAvailableRecognizerInfos);
    }
}
