package com.android.rcs.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import com.android.mms.util.SmileyParser;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

public class RcsSmileyParser {
    private static final int[] RCS_SMILEY_RES_IDS = new int[]{Smileys.getSmileyResource(0), Smileys.getSmileyResource(1), Smileys.getSmileyResource(2), Smileys.getSmileyResource(3), Smileys.getSmileyResource(4), Smileys.getSmileyResource(5), Smileys.getSmileyResource(6), Smileys.getSmileyResource(7), Smileys.getSmileyResource(8), Smileys.getSmileyResource(9), Smileys.getSmileyResource(10), Smileys.getSmileyResource(11), Smileys.getSmileyResource(12), Smileys.getSmileyResource(13), Smileys.getSmileyResource(14), Smileys.getSmileyResource(15), Smileys.getSmileyResource(16), Smileys.getSmileyResource(55), Smileys.getSmileyResource(17), Smileys.getSmileyResource(18), Smileys.getSmileyResource(19), Smileys.getSmileyResource(20), Smileys.getSmileyResource(21), Smileys.getSmileyResource(22), Smileys.getSmileyResource(23), Smileys.getSmileyResource(24), Smileys.getSmileyResource(25), Smileys.getSmileyResource(26), Smileys.getSmileyResource(27), Smileys.getSmileyResource(28), Smileys.getSmileyResource(29), Smileys.getSmileyResource(30), Smileys.getSmileyResource(31), Smileys.getSmileyResource(32), Smileys.getSmileyResource(33), Smileys.getSmileyResource(55), Smileys.getSmileyResource(34), Smileys.getSmileyResource(35), Smileys.getSmileyResource(36), Smileys.getSmileyResource(37), Smileys.getSmileyResource(38), Smileys.getSmileyResource(39), Smileys.getSmileyResource(40), Smileys.getSmileyResource(41), Smileys.getSmileyResource(42), Smileys.getSmileyResource(43), Smileys.getSmileyResource(44), Smileys.getSmileyResource(45), Smileys.getSmileyResource(46), Smileys.getSmileyResource(47), Smileys.getSmileyResource(48), Smileys.getSmileyResource(49), Smileys.getSmileyResource(50), Smileys.getSmileyResource(55), Smileys.getSmileyResource(51), Smileys.getSmileyResource(52), Smileys.getSmileyResource(53), Smileys.getSmileyResource(54), Smileys.getSmileyResource(55)};
    private static final int[] REPEATED_RCS_SMILEY_RES_IDS = new int[]{Smileys.getSmileyResource(0), Smileys.getSmileyResource(1), Smileys.getSmileyResource(2), Smileys.getSmileyResource(2), Smileys.getSmileyResource(2), Smileys.getSmileyResource(3), Smileys.getSmileyResource(3), Smileys.getSmileyResource(3), Smileys.getSmileyResource(3), Smileys.getSmileyResource(3), Smileys.getSmileyResource(3), Smileys.getSmileyResource(3), Smileys.getSmileyResource(4), Smileys.getSmileyResource(5), Smileys.getSmileyResource(5), Smileys.getSmileyResource(5), Smileys.getSmileyResource(5), Smileys.getSmileyResource(5), Smileys.getSmileyResource(6), Smileys.getSmileyResource(6), Smileys.getSmileyResource(6), Smileys.getSmileyResource(6), Smileys.getSmileyResource(6), Smileys.getSmileyResource(6), Smileys.getSmileyResource(6), Smileys.getSmileyResource(7), Smileys.getSmileyResource(8), Smileys.getSmileyResource(8), Smileys.getSmileyResource(8), Smileys.getSmileyResource(9), Smileys.getSmileyResource(9), Smileys.getSmileyResource(9), Smileys.getSmileyResource(9), Smileys.getSmileyResource(9), Smileys.getSmileyResource(9), Smileys.getSmileyResource(9), Smileys.getSmileyResource(10), Smileys.getSmileyResource(10), Smileys.getSmileyResource(10), Smileys.getSmileyResource(10), Smileys.getSmileyResource(10), Smileys.getSmileyResource(11), Smileys.getSmileyResource(11), Smileys.getSmileyResource(11), Smileys.getSmileyResource(11), Smileys.getSmileyResource(12), Smileys.getSmileyResource(12), Smileys.getSmileyResource(12), Smileys.getSmileyResource(13), Smileys.getSmileyResource(13), Smileys.getSmileyResource(13), Smileys.getSmileyResource(13), Smileys.getSmileyResource(13), Smileys.getSmileyResource(13), Smileys.getSmileyResource(13), Smileys.getSmileyResource(14), Smileys.getSmileyResource(14), Smileys.getSmileyResource(14), Smileys.getSmileyResource(15), Smileys.getSmileyResource(15), Smileys.getSmileyResource(15), Smileys.getSmileyResource(16), Smileys.getSmileyResource(16), Smileys.getSmileyResource(16), Smileys.getSmileyResource(17), Smileys.getSmileyResource(18), Smileys.getSmileyResource(18), Smileys.getSmileyResource(18), Smileys.getSmileyResource(19), Smileys.getSmileyResource(19), Smileys.getSmileyResource(19), Smileys.getSmileyResource(20), Smileys.getSmileyResource(20), Smileys.getSmileyResource(20), Smileys.getSmileyResource(21), Smileys.getSmileyResource(21), Smileys.getSmileyResource(21), Smileys.getSmileyResource(22), Smileys.getSmileyResource(23), Smileys.getSmileyResource(23), Smileys.getSmileyResource(23), Smileys.getSmileyResource(27), Smileys.getSmileyResource(28), Smileys.getSmileyResource(29), Smileys.getSmileyResource(30), Smileys.getSmileyResource(30), Smileys.getSmileyResource(38), Smileys.getSmileyResource(39), Smileys.getSmileyResource(40), Smileys.getSmileyResource(41), Smileys.getSmileyResource(44), Smileys.getSmileyResource(45), Smileys.getSmileyResource(46), Smileys.getSmileyResource(50), Smileys.getSmileyResource(51), Smileys.getSmileyResource(52)};
    private static final boolean mIsRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private static boolean useEnLocale = false;
    private Pattern mPattern;
    private String[] mSmileyNames = null;
    private String[] mSmileyTexts = null;
    private String[] mSmileyTextsEx = null;
    private HashMap<String, Integer> mSmileyToRes;

    private static class SmileyTextComparator implements Comparator<String>, Serializable {
        private static final long serialVersionUID = 4706169409345930186L;

        private SmileyTextComparator() {
        }

        public final int compare(String lhs, String rhs) {
            return rhs.length() - lhs.length();
        }
    }

    private static class Smileys {
        private static final int[] sIconIds = new int[]{R.drawable.rcs_def_happy, R.drawable.rcs_def_sad, R.drawable.rcs_def_wink, R.drawable.rcs_def_big, R.drawable.rcs_def_confused, R.drawable.rcs_def_blushing, R.drawable.rcs_def_stick, R.drawable.rcs_def_kiss, R.drawable.rcs_def_shocked, R.drawable.rcs_def_angry, R.drawable.rcs_def_cool, R.drawable.rcs_def_worried, R.drawable.rcs_def_devilish, R.drawable.rcs_def_crying, R.drawable.rcs_def_laughing, R.drawable.rcs_def_straight, R.drawable.rcs_def_angel, R.drawable.rcs_def_nerd, R.drawable.rcs_def_sleepy, R.drawable.rcs_def_rolling, R.drawable.rcs_def_sick, R.drawable.rcs_def_shhh, R.drawable.rcs_def_thinking, R.drawable.rcs_def_raised, R.drawable.rcs_def_rose, R.drawable.rcs_def_cup, R.drawable.rcs_def_drink, R.drawable.rcs_def_idea, R.drawable.rcs_def_love, R.drawable.rcs_def_beer, R.drawable.rcs_def_broken, R.drawable.rcs_def_rock, R.drawable.rcs_def_pirate, R.drawable.rcs_def_silly, R.drawable.rcs_def_applause, R.drawable.rcs_def_penguin, R.drawable.rcs_def_music, R.drawable.rcs_def_star, R.drawable.rcs_def_clock, R.drawable.rcs_def_pizza, R.drawable.rcs_def_money, R.drawable.rcs_def_sheep, R.drawable.rcs_def_pig, R.drawable.rcs_def_sun, R.drawable.rcs_def_rain, R.drawable.rcs_def_umbrella, R.drawable.rcs_def_aeroplane, R.drawable.rcs_def_birthday, R.drawable.rcs_def_party, R.drawable.rcs_def_film, R.drawable.rcs_def_gift, R.drawable.rcs_def_email, R.drawable.rcs_def_phone, R.drawable.rcs_def_wave, R.drawable.rcs_def_hug, R.drawable.csp_selected_all_normal};

        private Smileys() {
        }

        public static int getSmileyResource(int which) {
            return sIconIds[which];
        }
    }

    public RcsSmileyParser(Context context) {
    }

    public boolean isRcsSwitchOn() {
        return mIsRcsOn;
    }

    public void initSmileyParser(Context context, SmileyParser sp) {
        if (mIsRcsOn) {
            synchronized (sp) {
                this.mSmileyTexts = context.getResources().getStringArray(R.array.rcs_smiley_texts);
                this.mSmileyNames = context.getResources().getStringArray(R.array.rcs_smiley_names);
                this.mSmileyTextsEx = context.getResources().getStringArray(R.array.rcs_repeated_rcs_smiley_texts);
                this.mSmileyToRes = buildSmileyToRes(context);
                this.mPattern = buildPattern();
            }
        }
    }

    public String[] getSmileyText() {
        if (mIsRcsOn) {
            return this.mSmileyTexts;
        }
        return new String[0];
    }

    public String[] getSmileyName() {
        if (mIsRcsOn) {
            return this.mSmileyNames;
        }
        return new String[0];
    }

    public HashMap<String, Integer> getSmileyToRes() {
        if (mIsRcsOn) {
            return this.mSmileyToRes;
        }
        return null;
    }

    public Pattern getPattern() {
        if (mIsRcsOn) {
            return this.mPattern;
        }
        return null;
    }

    public int getIconIds(int which) {
        if (mIsRcsOn) {
            return Smileys.getSmileyResource(which);
        }
        return 0;
    }

    public int[] getSmileyResIds() {
        if (mIsRcsOn) {
            return (int[]) RCS_SMILEY_RES_IDS.clone();
        }
        return new int[0];
    }

    private Pattern buildPattern() {
        ArrayList<String> list = sort();
        StringBuilder patternString = new StringBuilder(list.size() * 4);
        patternString.append('(');
        for (int i = 0; i < list.size(); i++) {
            patternString.append(Pattern.quote((String) list.get(i)));
            patternString.append('|');
        }
        patternString.replace(patternString.length() - 1, patternString.length(), ")");
        return Pattern.compile(patternString.toString());
    }

    private HashMap<String, Integer> buildSmileyToRes(Context context) {
        int i;
        if (this.mSmileyTexts == null || RCS_SMILEY_RES_IDS.length != this.mSmileyTexts.length) {
            useEnLocale = true;
            this.mSmileyTexts = getSmileTexts(context, R.array.rcs_smiley_texts);
            if (RCS_SMILEY_RES_IDS.length != this.mSmileyTexts.length) {
                MLog.w("RcsSmileyParser", "buildSmileyToRes Smiley resource ID/text mismatch");
                throw new IllegalStateException("Smiley resource ID/text mismatch");
            }
        }
        HashMap<String, Integer> smileyToRes = new HashMap(this.mSmileyTexts.length);
        for (i = 0; i < this.mSmileyTexts.length; i++) {
            smileyToRes.put(this.mSmileyTexts[i], Integer.valueOf(RCS_SMILEY_RES_IDS[i]));
        }
        if (this.mSmileyTextsEx == null || REPEATED_RCS_SMILEY_RES_IDS.length != this.mSmileyTextsEx.length) {
            this.mSmileyTextsEx = getSmileTexts(context, R.array.rcs_repeated_rcs_smiley_texts);
            if (REPEATED_RCS_SMILEY_RES_IDS.length != this.mSmileyTextsEx.length) {
                MLog.w("RcsSmileyParser", "buildSmileyToResEx Smiley resource ID/text mismatch");
                throw new IllegalStateException("Smiley resource ID/text mismatch");
            }
        }
        for (i = 0; i < this.mSmileyTextsEx.length; i++) {
            smileyToRes.put(this.mSmileyTextsEx[i], Integer.valueOf(REPEATED_RCS_SMILEY_RES_IDS[i]));
        }
        return smileyToRes;
    }

    private ArrayList<String> sort() {
        ArrayList<String> list = new ArrayList();
        if (this.mSmileyToRes == null) {
            return list;
        }
        for (String key : this.mSmileyToRes.keySet()) {
            list.add(key);
        }
        Collections.sort(list, new SmileyTextComparator());
        return list;
    }

    private static Resources getResourceAsLocal(Context context, Locale local) {
        Resources standardResources = context.getResources();
        AssetManager assets = standardResources.getAssets();
        DisplayMetrics metrics = standardResources.getDisplayMetrics();
        Configuration config = new Configuration(standardResources.getConfiguration());
        config.locale = local;
        return new Resources(assets, metrics, config);
    }

    private String[] getSmileTexts(Context context, int smileyText) {
        Resources res = useEnLocale ? getResourceAsLocal(context, Locale.US) : context.getResources();
        return res == null ? new String[0] : res.getStringArray(smileyText);
    }
}
