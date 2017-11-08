package com.android.settings;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.os.Handler;
import android.os.Looper;
import android.support.v14.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Display.ColorTransform;
import java.util.ArrayList;

public class ColorModePreference extends SwitchPreference implements DisplayListener {
    private int mCurrentIndex;
    private ArrayList<ColorTransformDescription> mDescriptions;
    private Display mDisplay;
    private DisplayManager mDisplayManager = ((DisplayManager) getContext().getSystemService(DisplayManager.class));

    private static class ColorTransformDescription {
        private int colorTransform;
        private String summary;
        private String title;
        private ColorTransform transform;

        private ColorTransformDescription() {
        }
    }

    public ColorModePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getTransformsCount() {
        return this.mDescriptions.size();
    }

    public void startListening() {
        this.mDisplayManager.registerDisplayListener(this, new Handler(Looper.getMainLooper()));
    }

    public void stopListening() {
        this.mDisplayManager.unregisterDisplayListener(this);
    }

    public void onDisplayAdded(int displayId) {
        if (displayId == 0) {
            updateCurrentAndSupported();
        }
    }

    public void onDisplayChanged(int displayId) {
        if (displayId == 0) {
            updateCurrentAndSupported();
        }
    }

    public void onDisplayRemoved(int displayId) {
    }

    public void updateCurrentAndSupported() {
        boolean z;
        this.mDisplay = this.mDisplayManager.getDisplay(0);
        this.mDescriptions = new ArrayList();
        Resources resources = getContext().getResources();
        int[] transforms = resources.getIntArray(17236020);
        String[] titles = resources.getStringArray(2131361827);
        String[] descriptions = resources.getStringArray(2131361828);
        int i = 0;
        while (i < transforms.length) {
            if (!(transforms[i] == -1 || i == 1)) {
                ColorTransformDescription desc = new ColorTransformDescription();
                desc.colorTransform = transforms[i];
                desc.title = titles[i];
                desc.summary = descriptions[i];
                this.mDescriptions.add(desc);
            }
            i++;
        }
        ColorTransform[] supportedColorTransforms = this.mDisplay.getSupportedColorTransforms();
        for (i = 0; i < supportedColorTransforms.length; i++) {
            int j = 0;
            while (j < this.mDescriptions.size()) {
                if (((ColorTransformDescription) this.mDescriptions.get(j)).colorTransform == supportedColorTransforms[i].getColorTransform() && ((ColorTransformDescription) this.mDescriptions.get(j)).transform == null) {
                    ((ColorTransformDescription) this.mDescriptions.get(j)).transform = supportedColorTransforms[i];
                    break;
                }
                j++;
            }
        }
        i = 0;
        while (i < this.mDescriptions.size()) {
            if (((ColorTransformDescription) this.mDescriptions.get(i)).transform == null) {
                int i2 = i - 1;
                this.mDescriptions.remove(i);
                i = i2;
            }
            i++;
        }
        ColorTransform currentTransform = this.mDisplay.getColorTransform();
        this.mCurrentIndex = -1;
        for (i = 0; i < this.mDescriptions.size(); i++) {
            if (((ColorTransformDescription) this.mDescriptions.get(i)).colorTransform == currentTransform.getColorTransform()) {
                this.mCurrentIndex = i;
                break;
            }
        }
        if (this.mCurrentIndex == 1) {
            z = true;
        } else {
            z = false;
        }
        setChecked(z);
    }

    protected boolean persistBoolean(boolean value) {
        if (this.mDescriptions.size() == 2) {
            ColorTransformDescription desc = (ColorTransformDescription) this.mDescriptions.get(value ? 1 : 0);
            this.mDisplay.requestColorTransform(desc.transform);
            this.mCurrentIndex = this.mDescriptions.indexOf(desc);
        }
        return true;
    }
}
