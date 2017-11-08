package com.android.settings.accessibility;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceFrameLayout;
import android.preference.PreferenceFrameLayout.LayoutParams;
import android.provider.Settings.Secure;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.TwoStatePreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.accessibility.CaptioningManager;
import android.view.accessibility.CaptioningManager.CaptionStyle;
import com.android.internal.widget.SubtitleView;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.accessibility.ListDialogPreference.OnValueChangedListener;
import com.android.settingslib.Utils;
import com.android.settingslib.accessibility.AccessibilityUtils;
import java.util.Locale;

public class CaptionPropertiesFragment extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnValueChangedListener {
    private static final int[] CAPTIONING_OPACITY_SELECTOR_TITLES = new int[]{25, 50, 75, 100};
    private ColorPreference mBackgroundColor;
    private ColorPreference mBackgroundOpacity;
    private CaptioningManager mCaptioningManager;
    private TwoStatePreference mCaptioningSwitch;
    private PreferenceCategory mCustom;
    private ColorPreference mEdgeColor;
    private EdgeTypePreference mEdgeType;
    private ListPreference mFontSize;
    private ColorPreference mForegroundColor;
    private ColorPreference mForegroundOpacity;
    private LocalePreference mLocale;
    private PresetPreference mPreset;
    private SubtitleView mPreviewText;
    private View mPreviewViewport;
    private View mPreviewWindow;
    private boolean mShowingCustom;
    private ListPreference mTypeface;
    private ColorPreference mWindowColor;
    private ColorPreference mWindowOpacity;

    protected int getMetricsCategory() {
        return 3;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mCaptioningManager = (CaptioningManager) getSystemService("captioning");
        addPreferencesFromResource(2131230751);
        initializeAllPreferences();
        updateAllPreferences();
        refreshShowingCustom();
        installUpdateListeners();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(2130968666, container, false);
        if (container instanceof PreferenceFrameLayout) {
            ((LayoutParams) rootView.getLayoutParams()).removeBorders = true;
        }
        ((ViewGroup) rootView.findViewById(2131886347)).addView(super.onCreateView(inflater, container, savedInstanceState), -1, -1);
        return rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean enabled = this.mCaptioningManager.isEnabled();
        this.mPreviewText = (SubtitleView) view.findViewById(2131886346);
        this.mPreviewText.setVisibility(enabled ? 0 : 4);
        this.mPreviewWindow = view.findViewById(2131886345);
        this.mPreviewViewport = view.findViewById(2131886343);
        this.mPreviewViewport.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                CaptionPropertiesFragment.this.refreshPreviewText();
            }
        });
        this.mCaptioningSwitch.setChecked(enabled);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setPreferencesEnabled(this.mCaptioningManager.isEnabled());
        refreshPreviewText();
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    private void refreshPreviewText() {
        Context context = getActivity();
        if (context != null) {
            SubtitleView preview = this.mPreviewText;
            if (preview != null) {
                applyCaptionProperties(this.mCaptioningManager, preview, this.mPreviewViewport, this.mCaptioningManager.getRawUserStyle());
                Locale locale = this.mCaptioningManager.getLocale();
                if (locale != null) {
                    preview.setText(AccessibilityUtils.getTextForLocale(context, locale, 2131625893));
                } else {
                    preview.setText(2131625893);
                }
                CaptionStyle style = this.mCaptioningManager.getUserStyle();
                if (style.hasWindowColor()) {
                    this.mPreviewWindow.setBackgroundColor(style.windowColor);
                } else {
                    this.mPreviewWindow.setBackgroundColor(CaptionStyle.DEFAULT.windowColor);
                }
            }
        }
    }

    public static void applyCaptionProperties(CaptioningManager manager, SubtitleView previewText, View previewWindow, int styleId) {
        previewText.setStyle(styleId);
        Context context = previewText.getContext();
        ContentResolver cr = context.getContentResolver();
        float fontScale = manager.getFontScale();
        if (previewWindow != null) {
            previewText.setTextSize((0.0533f * (((float) Math.max(previewWindow.getWidth() * 9, previewWindow.getHeight() * 16)) / 16.0f)) * fontScale);
        } else {
            previewText.setTextSize(context.getResources().getDimension(2131558627) * fontScale);
        }
        Locale locale = manager.getLocale();
        if (locale != null) {
            previewText.setText(AccessibilityUtils.getTextForLocale(context, locale, 2131625894));
        } else {
            previewText.setText(2131625894);
        }
    }

    private void initializeAllPreferences() {
        this.mLocale = (LocalePreference) findPreference("captioning_locale");
        this.mFontSize = (ListPreference) findPreference("captioning_font_size");
        Resources res = getResources();
        int[] presetValues = res.getIntArray(2131361902);
        String[] presetTitles = res.getStringArray(2131361976);
        this.mPreset = (PresetPreference) findPreference("captioning_preset");
        this.mPreset.setValues(presetValues);
        this.mPreset.setTitles(presetTitles);
        this.mCustom = (PreferenceCategory) findPreference("custom");
        this.mShowingCustom = true;
        int[] colorValues = res.getIntArray(2131361898);
        String[] colorTitles = res.getStringArray(2131361897);
        this.mForegroundColor = (ColorPreference) this.mCustom.findPreference("captioning_foreground_color");
        this.mForegroundColor.setTitles(colorTitles);
        this.mForegroundColor.setValues(colorValues);
        int[] opacityValues = res.getIntArray(2131361900);
        String[] opacityTitles = buildCaptioningOpacitySelectorTitles(getActivity());
        this.mForegroundOpacity = (ColorPreference) this.mCustom.findPreference("captioning_foreground_opacity");
        this.mForegroundOpacity.setTitles(opacityTitles);
        this.mForegroundOpacity.setValues(opacityValues);
        this.mEdgeColor = (ColorPreference) this.mCustom.findPreference("captioning_edge_color");
        this.mEdgeColor.setTitles(colorTitles);
        this.mEdgeColor.setValues(colorValues);
        int[] bgColorValues = new int[(colorValues.length + 1)];
        String[] bgColorTitles = new String[(colorTitles.length + 1)];
        System.arraycopy(colorValues, 0, bgColorValues, 1, colorValues.length);
        System.arraycopy(colorTitles, 0, bgColorTitles, 1, colorTitles.length);
        bgColorValues[0] = 0;
        bgColorTitles[0] = getString(2131625898);
        this.mBackgroundColor = (ColorPreference) this.mCustom.findPreference("captioning_background_color");
        this.mBackgroundColor.setTitles(bgColorTitles);
        this.mBackgroundColor.setValues(bgColorValues);
        this.mBackgroundOpacity = (ColorPreference) this.mCustom.findPreference("captioning_background_opacity");
        this.mBackgroundOpacity.setTitles(opacityTitles);
        this.mBackgroundOpacity.setValues(opacityValues);
        this.mWindowColor = (ColorPreference) this.mCustom.findPreference("captioning_window_color");
        this.mWindowColor.setTitles(bgColorTitles);
        this.mWindowColor.setValues(bgColorValues);
        this.mWindowOpacity = (ColorPreference) this.mCustom.findPreference("captioning_window_opacity");
        this.mWindowOpacity.setTitles(opacityTitles);
        this.mWindowOpacity.setValues(opacityValues);
        this.mEdgeType = (EdgeTypePreference) this.mCustom.findPreference("captioning_edge_type");
        this.mTypeface = (ListPreference) this.mCustom.findPreference("captioning_typeface");
        this.mCaptioningSwitch = (TwoStatePreference) findPreference("accessibility_captioning");
    }

    private void installUpdateListeners() {
        this.mPreset.setOnValueChangedListener(this);
        this.mForegroundColor.setOnValueChangedListener(this);
        this.mForegroundOpacity.setOnValueChangedListener(this);
        this.mEdgeColor.setOnValueChangedListener(this);
        this.mBackgroundColor.setOnValueChangedListener(this);
        this.mBackgroundOpacity.setOnValueChangedListener(this);
        this.mWindowColor.setOnValueChangedListener(this);
        this.mWindowOpacity.setOnValueChangedListener(this);
        this.mEdgeType.setOnValueChangedListener(this);
        this.mTypeface.setOnPreferenceChangeListener(this);
        this.mFontSize.setOnPreferenceChangeListener(this);
        this.mLocale.setOnPreferenceChangeListener(this);
        this.mCaptioningSwitch.setOnPreferenceChangeListener(this);
    }

    private void updateAllPreferences() {
        this.mPreset.setValue(this.mCaptioningManager.getRawUserStyle());
        this.mFontSize.setValue(Float.toString(this.mCaptioningManager.getFontScale()));
        CaptionStyle attrs = CaptionStyle.getCustomStyle(getContentResolver());
        this.mEdgeType.setValue(attrs.edgeType);
        this.mEdgeColor.setValue(attrs.edgeColor);
        parseColorOpacity(this.mForegroundColor, this.mForegroundOpacity, attrs.hasForegroundColor() ? attrs.foregroundColor : 16777215);
        parseColorOpacity(this.mBackgroundColor, this.mBackgroundOpacity, attrs.hasBackgroundColor() ? attrs.backgroundColor : 16777215);
        parseColorOpacity(this.mWindowColor, this.mWindowOpacity, attrs.hasWindowColor() ? attrs.windowColor : 16777215);
        String rawTypeface = attrs.mRawTypeface;
        ListPreference listPreference = this.mTypeface;
        if (rawTypeface == null) {
            rawTypeface = "";
        }
        listPreference.setValue(rawTypeface);
        String rawLocale = this.mCaptioningManager.getRawLocale();
        LocalePreference localePreference = this.mLocale;
        if (rawLocale == null) {
            rawLocale = "";
        }
        localePreference.setValue(rawLocale);
    }

    private void parseColorOpacity(ColorPreference color, ColorPreference opacity, int value) {
        int colorValue;
        int opacityValue;
        if (!CaptionStyle.hasColor(value)) {
            colorValue = 16777215;
            opacityValue = (value & 255) << 24;
        } else if ((value >>> 24) == 0) {
            colorValue = 0;
            opacityValue = (value & 255) << 24;
        } else {
            colorValue = value | -16777216;
            opacityValue = value & -16777216;
        }
        opacity.setValue(16777215 | opacityValue);
        color.setValue(colorValue);
    }

    private int mergeColorOpacity(ColorPreference color, ColorPreference opacity) {
        int colorValue = color.getValue();
        int opacityValue = opacity.getValue();
        if (!CaptionStyle.hasColor(colorValue)) {
            return 16776960 | Color.alpha(opacityValue);
        }
        if (colorValue == 0) {
            return Color.alpha(opacityValue);
        }
        return (16777215 & colorValue) | (-16777216 & opacityValue);
    }

    private void refreshShowingCustom() {
        boolean customPreset;
        if (this.mPreset.getValue() == -1) {
            customPreset = true;
        } else {
            customPreset = false;
        }
        if (!customPreset && this.mShowingCustom) {
            getPreferenceScreen().removePreference(this.mCustom);
            this.mShowingCustom = false;
        } else if (customPreset && !this.mShowingCustom) {
            getPreferenceScreen().addPreference(this.mCustom);
            this.mShowingCustom = true;
        }
    }

    public void onValueChanged(ListDialogPreference preference, int value) {
        ContentResolver cr = getActivity().getContentResolver();
        if (this.mForegroundColor == preference || this.mForegroundOpacity == preference) {
            Secure.putInt(cr, "accessibility_captioning_foreground_color", mergeColorOpacity(this.mForegroundColor, this.mForegroundOpacity));
        } else if (this.mBackgroundColor == preference || this.mBackgroundOpacity == preference) {
            Secure.putInt(cr, "accessibility_captioning_background_color", mergeColorOpacity(this.mBackgroundColor, this.mBackgroundOpacity));
        } else if (this.mWindowColor == preference || this.mWindowOpacity == preference) {
            Secure.putInt(cr, "accessibility_captioning_window_color", mergeColorOpacity(this.mWindowColor, this.mWindowOpacity));
        } else if (this.mEdgeColor == preference) {
            Secure.putInt(cr, "accessibility_captioning_edge_color", value);
        } else if (this.mPreset == preference) {
            ItemUseStat.getInstance().handleClickListDialogPreference(getActivity(), ItemUseStat.KEY_CAPTION_STYLE, preference.getKey(), value);
            Secure.putInt(cr, "accessibility_captioning_preset", value);
            refreshShowingCustom();
        } else if (this.mEdgeType == preference) {
            Secure.putInt(cr, "accessibility_captioning_edge_type", value);
        }
        refreshPreviewText();
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        int i = 0;
        ContentResolver cr = getActivity().getContentResolver();
        if (this.mTypeface == preference) {
            Secure.putString(cr, "accessibility_captioning_typeface", (String) value);
        } else if (this.mFontSize == preference) {
            ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mFontSize, ItemUseStat.KEY_CAPTION_FONT_SIZE, (String) value);
            Secure.putFloat(cr, "accessibility_captioning_font_scale", Float.parseFloat((String) value));
        } else if (this.mLocale == preference) {
            ItemUseStat.getInstance().handleClickLocalListPreference(getActivity(), this.mLocale, ItemUseStat.KEY_LOCAL, (String) value);
            Secure.putString(cr, "accessibility_captioning_locale", (String) value);
        } else if (this.mCaptioningSwitch == preference) {
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, value);
            boolean isChecked = ((Boolean) value).booleanValue();
            Secure.putInt(getActivity().getContentResolver(), "accessibility_captioning_enabled", isChecked ? 1 : 0);
            setPreferencesEnabled(isChecked);
            if (this.mPreviewText != null) {
                SubtitleView subtitleView = this.mPreviewText;
                if (!isChecked) {
                    i = 4;
                }
                subtitleView.setVisibility(i);
            }
            return true;
        }
        refreshPreviewText();
        return true;
    }

    private void setPreferencesEnabled(boolean enabled) {
        int COUNT = getPreferenceScreen().getPreferenceCount();
        for (int i = 0; i < COUNT; i++) {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference != this.mCaptioningSwitch) {
                preference.setEnabled(enabled);
            }
        }
    }

    public void onPause() {
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        return super.onPreferenceTreeClick(preference);
    }

    protected String[] buildCaptioningOpacitySelectorTitles(Context context) {
        return new String[]{Utils.formatPercentage(CAPTIONING_OPACITY_SELECTOR_TITLES[0]), Utils.formatPercentage(CAPTIONING_OPACITY_SELECTOR_TITLES[1]), Utils.formatPercentage(CAPTIONING_OPACITY_SELECTOR_TITLES[2]), Utils.formatPercentage(CAPTIONING_OPACITY_SELECTOR_TITLES[3])};
    }
}
