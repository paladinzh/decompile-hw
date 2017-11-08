package com.android.mms.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.MmsApp;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.util.RcsSmileyParser;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmileyParser {
    private static final int[] DEFAULT_SMILEY_RES_IDS = new int[]{Smileys.getSmileyResource(0), Smileys.getSmileyResource(1), Smileys.getSmileyResource(2), Smileys.getSmileyResource(3), Smileys.getSmileyResource(4), Smileys.getSmileyResource(5), Smileys.getSmileyResource(6), Smileys.getSmileyResource(7), Smileys.getSmileyResource(8), Smileys.getSmileyResource(9), Smileys.getSmileyResource(10), Smileys.getSmileyResource(11), Smileys.getSmileyResource(12), Smileys.getSmileyResource(13), Smileys.getSmileyResource(14), Smileys.getSmileyResource(15), Smileys.getSmileyResource(16), Smileys.getSmileyResource(17), Smileys.getSmileyResource(18), Smileys.getSmileyResource(19), Smileys.getSmileyResource(20)};
    private static int EMOJI_ROW_COUNT_LANDSPACE = 0;
    private static int EMOJI_ROW_COUNT_MULTIWINDOW = 0;
    private static int EMOJI_ROW_COUNT_PORTRAIT = 0;
    private static int LAND_NATURE_PAGE = (LAND_OBJECTS_PAGE + LAND_OBJECTS_PAGECOUNT);
    private static int LAND_NATURE_PAGECOUNT = 7;
    private static int LAND_NATURE_PAGE_NULLCOUNT = 18;
    private static int LAND_OBJECTS_PAGE = (LAND_RECENTS_PAGE + LAND_PEOPLE_PAGECOUNT);
    private static int LAND_OBJECTS_PAGECOUNT = 6;
    private static int LAND_OBJECTS_PAGE_NULLCOUNT = 6;
    private static int LAND_PEOPLE_PAGECOUNT = 6;
    private static int LAND_PEOPLE_PAGE_NULLCOUNT = 15;
    private static int LAND_PLACES_PAGE = (LAND_NATURE_PAGE + LAND_NATURE_PAGECOUNT);
    private static int LAND_PLACES_PAGECOUNT = 5;
    private static int LAND_PLACES_PAGE_NULLCOUNT = 23;
    private static int LAND_RECENTS_PAGE = LAND_RECENTS_PAGECOUNT;
    private static int LAND_RECENTS_PAGECOUNT = 1;
    private static int LAND_RECENT_EMOJI_COUNT = 28;
    private static int LAND_SYMBOLS_PAGE = (LAND_PLACES_PAGE + LAND_PLACES_PAGECOUNT);
    private static int LAND_SYMBOLS_PAGECOUNT = 8;
    private static int LAND_SYMBOLS_PAGE_NULLCOUNT = 12;
    private static int LAND_TATAL_EMOJI_COUNT = 924;
    private static int LAND_TOTAL_PAGE = (LAND_SYMBOLS_PAGE + LAND_SYMBOLS_PAGECOUNT);
    private static int MULTIWINDOW_NATURE_PAGE = (MULTIWINDOW_OBJECTS_PAGE + MULTIWINDOW_OBJECTS_PAGECOUNT);
    private static int MULTIWINDOW_NATURE_PAGECOUNT = 12;
    private static int MULTIWINDOW_NATURE_PAGE_NULLCOUNT = 14;
    private static int MULTIWINDOW_OBJECTS_PAGE = (MULTIWINDOW_RECENTS_PAGE + MULTIWINDOW_PEOPLE_PAGECOUNT);
    private static int MULTIWINDOW_OBJECTS_PAGECOUNT = 11;
    private static int MULTIWINDOW_OBJECTS_PAGE_NULLCOUNT = 14;
    private static int MULTIWINDOW_PEOPLE_PAGECOUNT = 10;
    private static int MULTIWINDOW_PEOPLE_PAGE_NULLCOUNT = 7;
    private static int MULTIWINDOW_PLACES_PAGE = (MULTIWINDOW_NATURE_PAGE + MULTIWINDOW_NATURE_PAGECOUNT);
    private static int MULTIWINDOW_PLACES_PAGECOUNT = 8;
    private static int MULTIWINDOW_PLACES_PAGE_NULLCOUNT = 11;
    private static int MULTIWINDOW_RECENTS_PAGE = MULTIWINDOW_RECENTS_PAGECOUNT;
    private static int MULTIWINDOW_RECENTS_PAGECOUNT = 1;
    private static int MULTIWINDOW_RECENT_EMOJI_COUNT = 16;
    private static int MULTIWINDOW_SYMBOLS_PAGE = (MULTIWINDOW_PLACES_PAGE + MULTIWINDOW_PLACES_PAGECOUNT);
    private static int MULTIWINDOW_SYMBOLS_PAGECOUNT = 14;
    private static int MULTIWINDOW_SYMBOLS_PAGE_NULLCOUNT = 12;
    private static int MULTIWINDOW_TATAL_EMOJI_COUNT = 896;
    private static int MULTIWINDOW_TOTAL_PAGE = (MULTIWINDOW_SYMBOLS_PAGE + MULTIWINDOW_SYMBOLS_PAGECOUNT);
    private static int NATURE_PAGE = (OBJECTS_PAGE + OBJECTS_PAGECOUNT);
    private static int NATURE_PAGECOUNT = 8;
    private static int NATURE_PAGE_NULLCOUNT = 14;
    private static int OBJECTS_PAGE = (RECENTS_PAGE + PEOPLE_PAGECOUNT);
    private static int OBJECTS_PAGECOUNT = 7;
    private static int OBJECTS_PAGE_NULLCOUNT = 6;
    private static int PEOPLE_PAGECOUNT = 7;
    private static int PEOPLE_PAGE_NULLCOUNT = 15;
    private static int PLACES_PAGE = (NATURE_PAGE + NATURE_PAGECOUNT);
    private static int PLACES_PAGECOUNT = 5;
    private static int PLACES_PAGE_NULLCOUNT = 3;
    private static int RECENTS_PAGE = RECENTS_PAGECOUNT;
    private static int RECENTS_PAGECOUNT = 1;
    private static int RECENT_EMOJI_COUNT = 24;
    private static int SYMBOLS_PAGE = (PLACES_PAGE + PLACES_PAGECOUNT);
    private static int SYMBOLS_PAGECOUNT = 9;
    private static int SYMBOLS_PAGE_NULLCOUNT = 4;
    private static int TATAL_EMOJI_COUNT = 888;
    private static int TOTAL_PAGE = (SYMBOLS_PAGE + SYMBOLS_PAGECOUNT);
    private static final HashMap<String, Integer> mCategoryNameToIdMap = new HashMap();
    private static RcsSmileyParser mCust = null;
    private static String[] mEmojiTexts;
    private static SharedPreferences mPrefs;
    private static String mRecentEmojiTexts;
    private static String[] mSmileyNames = null;
    private static String[] mSmileyTexts = null;
    private static final String[] sCategoryName = new String[]{"recents", "people", "objects", "nature", "places", "symbols"};
    private static SmileyParser sInstance;
    private static boolean useEnLocale = false;
    private float currentDensity;
    private RcsImageCache imageCache;
    private final Context mContext;
    final Pattern mPattern;
    final HashMap<String, Integer> mSmileyToRes;
    private int searchSmileySize = this.mContext.getResources().getDimensionPixelOffset(R.dimen.search_smiley_size);
    private int smileySize = this.mContext.getResources().getDimensionPixelOffset(R.dimen.smiley_size);

    private static class DrawableCache {
        WeakReference<Drawable> drawableReference;
        SMILEY_TYPE faceType;
        int size;

        public DrawableCache(Drawable drawable, SMILEY_TYPE faceType, int size) {
            this.drawableReference = new WeakReference(drawable);
            this.faceType = faceType;
            this.size = size;
        }
    }

    private static class RcsImageCache extends WeakHashMap<Integer, DrawableCache> {
        private RcsImageCache() {
        }
    }

    public enum SMILEY_TYPE {
        LIST_TEXTVIEW,
        MESSAGE_TEXTVIEW,
        SEARCH_MESSAGE_TEXTVIEW,
        MESSAGE_EDITTEXT,
        CONV_LIST_TEXTVIEW
    }

    public static class Smileys {
        private static final int[] sIconIds = new int[]{R.drawable.emo_im_happy, R.drawable.emo_im_sad, R.drawable.emo_im_winking, R.drawable.emo_im_tongue_sticking_out, R.drawable.emo_im_surprised, R.drawable.emo_im_kissing, R.drawable.emo_im_yelling, R.drawable.emo_im_cool, R.drawable.emo_im_money_mouth, R.drawable.emo_im_foot_in_mouth, R.drawable.emo_im_embarrassed, R.drawable.emo_im_angel, R.drawable.emo_im_undecided, R.drawable.emo_im_crying, R.drawable.emo_im_lips_are_sealed, R.drawable.emo_im_laughing, R.drawable.emo_im_wtf, R.drawable.emo_im_weep, R.drawable.emo_im_shock, R.drawable.emo_im_jiong, R.drawable.csp_selected_all_normal};

        public static int getSmileyResource(int which) {
            if (SmileyParser.mCust == null || !SmileyParser.mCust.isRcsSwitchOn()) {
                return sIconIds[which];
            }
            return SmileyParser.mCust.getIconIds(which);
        }

        public static String getTitle(int index) {
            return SmileyParser.getSmileyText(index);
        }

        public static int getCommnd(int index) {
            return index;
        }

        public static String getName(int index) {
            return SmileyParser.getSmileyName(index);
        }
    }

    public static SmileyParser getInstance() {
        if (sInstance == null) {
            init(MmsApp.getApplication().getApplicationContext());
        }
        return sInstance;
    }

    public static void init(Context context) {
        if (sInstance == null) {
            sInstance = new SmileyParser(context);
        }
    }

    public RcsSmileyParser getHwCust() {
        return mCust;
    }

    private static void setHwCust(Context context) {
        mCust = new RcsSmileyParser(context);
    }

    private SmileyParser(Context context) {
        MLog.i("Mms/SmileyParser", "SmileyParser init");
        this.mContext = context;
        setHwCust(context);
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.imageCache = new RcsImageCache();
        }
        this.currentDensity = context.getResources().getDisplayMetrics().density;
        if (mCust == null || !mCust.isRcsSwitchOn()) {
            initSmileyText(this.mContext);
            this.mSmileyToRes = buildSmileyToRes(this.mContext);
            this.mPattern = buildPattern();
        } else {
            mCust.initSmileyParser(this.mContext, this);
            synchronized (SmileyParser.class) {
                MLog.i("Mms/SmileyParser", "mCust.getSmileyText(),getSmileyName()");
                mSmileyTexts = mCust.getSmileyText();
                mSmileyNames = mCust.getSmileyName();
            }
            this.mSmileyToRes = mCust.getSmileyToRes();
            this.mPattern = mCust.getPattern();
        }
        for (int i = 0; i < sCategoryName.length; i++) {
            mCategoryNameToIdMap.put(sCategoryName[i], Integer.valueOf(i));
        }
        initPrefs(this.mContext);
        initSmileyPageCount(this.mContext);
    }

    private static synchronized void initPrefs(Context context) {
        synchronized (SmileyParser.class) {
            MLog.i("Mms/SmileyParser", "initPrefs start");
            mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    private static synchronized void initSmileyText(Context context) {
        synchronized (SmileyParser.class) {
            MLog.i("Mms/SmileyParser", "initSmileyText start");
            mSmileyTexts = context.getResources().getStringArray(R.array.default_smiley_texts);
            mSmileyNames = context.getResources().getStringArray(R.array.default_smiley_names);
        }
    }

    public static String getSmileyName(int index) {
        if (mSmileyNames != null && index < mSmileyNames.length) {
            return mSmileyNames[index];
        }
        MLog.e("Mms/SmileyParser", "getSmileyName has an error.");
        return "";
    }

    public static String[] getEmojiTexts(Context context) {
        return parseEmojiTexts(context);
    }

    private static synchronized String[] parseEmojiTexts(Context context) {
        synchronized (SmileyParser.class) {
            int[] emojiUnicodes;
            int recentEmojiCount;
            int i;
            boolean isLand = isLand(context);
            if (isInMultiWindowMode(context)) {
                emojiUnicodes = getEmojiTotalTextsArray(context, 2);
                recentEmojiCount = MULTIWINDOW_RECENT_EMOJI_COUNT;
            } else if (isLand) {
                emojiUnicodes = getEmojiTotalTextsArray(context, 1);
                recentEmojiCount = LAND_RECENT_EMOJI_COUNT;
            } else {
                emojiUnicodes = getEmojiTotalTextsArray(context, 0);
                recentEmojiCount = RECENT_EMOJI_COUNT;
            }
            int emojiLength = emojiUnicodes.length;
            mEmojiTexts = new String[emojiLength];
            for (i = 0; i < emojiLength; i++) {
                mEmojiTexts[i] = emojiUnicodes[i] == 0 ? null : new String(Character.toChars(emojiUnicodes[i]));
            }
            mRecentEmojiTexts = mPrefs.getString("recent_emoji", mRecentEmojiTexts);
            if (mRecentEmojiTexts == null) {
                String[] strArr = mEmojiTexts;
                return strArr;
            }
            String[] splitEmojiTexts = mRecentEmojiTexts.split("-");
            int recentEmojiLengths = splitEmojiTexts.length < recentEmojiCount ? splitEmojiTexts.length : recentEmojiCount;
            for (i = 0; i < recentEmojiLengths; i++) {
                mEmojiTexts[i] = splitEmojiTexts[i];
            }
            strArr = mEmojiTexts;
            return strArr;
        }
    }

    private static int[] getEmojiTotalTextsArray(Context context, int currentStateFlag) {
        int[] people = context.getResources().getIntArray(R.array.default_emoji_unicode_people);
        int[] objects = context.getResources().getIntArray(R.array.default_emoji_unicode_objects);
        int[] nature = context.getResources().getIntArray(R.array.default_emoji_unicode_nature);
        int[] places = context.getResources().getIntArray(R.array.default_emoji_unicode_places);
        int[] symbols = context.getResources().getIntArray(R.array.default_emoji_unicode_symbols);
        int[] emojiUnicodes;
        int[] recent;
        switch (currentStateFlag) {
            case 0:
                emojiUnicodes = new int[TATAL_EMOJI_COUNT];
                recent = new int[RECENT_EMOJI_COUNT];
                System.arraycopy(recent, 0, emojiUnicodes, 0, recent.length);
                System.arraycopy(people, 0, emojiUnicodes, recent.length, people.length);
                System.arraycopy(new int[PEOPLE_PAGE_NULLCOUNT], 0, emojiUnicodes, recent.length + people.length, PEOPLE_PAGE_NULLCOUNT);
                System.arraycopy(objects, 0, emojiUnicodes, (recent.length + people.length) + PEOPLE_PAGE_NULLCOUNT, objects.length);
                System.arraycopy(new int[OBJECTS_PAGE_NULLCOUNT], 0, emojiUnicodes, ((recent.length + people.length) + PEOPLE_PAGE_NULLCOUNT) + objects.length, OBJECTS_PAGE_NULLCOUNT);
                System.arraycopy(nature, 0, emojiUnicodes, (((recent.length + people.length) + PEOPLE_PAGE_NULLCOUNT) + objects.length) + OBJECTS_PAGE_NULLCOUNT, nature.length);
                System.arraycopy(new int[NATURE_PAGE_NULLCOUNT], 0, emojiUnicodes, ((((recent.length + people.length) + PEOPLE_PAGE_NULLCOUNT) + objects.length) + OBJECTS_PAGE_NULLCOUNT) + nature.length, NATURE_PAGE_NULLCOUNT);
                System.arraycopy(places, 0, emojiUnicodes, (((((recent.length + people.length) + PEOPLE_PAGE_NULLCOUNT) + objects.length) + OBJECTS_PAGE_NULLCOUNT) + nature.length) + NATURE_PAGE_NULLCOUNT, places.length);
                System.arraycopy(new int[PLACES_PAGE_NULLCOUNT], 0, emojiUnicodes, ((((((recent.length + people.length) + PEOPLE_PAGE_NULLCOUNT) + objects.length) + OBJECTS_PAGE_NULLCOUNT) + nature.length) + NATURE_PAGE_NULLCOUNT) + places.length, PLACES_PAGE_NULLCOUNT);
                System.arraycopy(symbols, 0, emojiUnicodes, (((((((recent.length + people.length) + PEOPLE_PAGE_NULLCOUNT) + objects.length) + OBJECTS_PAGE_NULLCOUNT) + nature.length) + NATURE_PAGE_NULLCOUNT) + places.length) + PLACES_PAGE_NULLCOUNT, symbols.length);
                System.arraycopy(new int[SYMBOLS_PAGE_NULLCOUNT], 0, emojiUnicodes, ((((((((recent.length + people.length) + PEOPLE_PAGE_NULLCOUNT) + objects.length) + OBJECTS_PAGE_NULLCOUNT) + nature.length) + NATURE_PAGE_NULLCOUNT) + places.length) + PLACES_PAGE_NULLCOUNT) + symbols.length, SYMBOLS_PAGE_NULLCOUNT);
                return emojiUnicodes;
            case 1:
                emojiUnicodes = new int[LAND_TATAL_EMOJI_COUNT];
                recent = new int[LAND_RECENT_EMOJI_COUNT];
                System.arraycopy(recent, 0, emojiUnicodes, 0, recent.length);
                System.arraycopy(people, 0, emojiUnicodes, recent.length, people.length);
                System.arraycopy(new int[LAND_PEOPLE_PAGE_NULLCOUNT], 0, emojiUnicodes, recent.length + people.length, LAND_PEOPLE_PAGE_NULLCOUNT);
                System.arraycopy(objects, 0, emojiUnicodes, (recent.length + people.length) + LAND_PEOPLE_PAGE_NULLCOUNT, objects.length);
                System.arraycopy(new int[LAND_OBJECTS_PAGE_NULLCOUNT], 0, emojiUnicodes, ((recent.length + people.length) + LAND_PEOPLE_PAGE_NULLCOUNT) + objects.length, LAND_OBJECTS_PAGE_NULLCOUNT);
                System.arraycopy(nature, 0, emojiUnicodes, (((recent.length + people.length) + LAND_PEOPLE_PAGE_NULLCOUNT) + objects.length) + LAND_OBJECTS_PAGE_NULLCOUNT, nature.length);
                System.arraycopy(new int[LAND_NATURE_PAGE_NULLCOUNT], 0, emojiUnicodes, ((((recent.length + people.length) + LAND_PEOPLE_PAGE_NULLCOUNT) + objects.length) + LAND_OBJECTS_PAGE_NULLCOUNT) + nature.length, LAND_NATURE_PAGE_NULLCOUNT);
                System.arraycopy(places, 0, emojiUnicodes, (((((recent.length + people.length) + LAND_PEOPLE_PAGE_NULLCOUNT) + objects.length) + LAND_OBJECTS_PAGE_NULLCOUNT) + nature.length) + LAND_NATURE_PAGE_NULLCOUNT, places.length);
                System.arraycopy(new int[LAND_PLACES_PAGE_NULLCOUNT], 0, emojiUnicodes, ((((((recent.length + people.length) + LAND_PEOPLE_PAGE_NULLCOUNT) + objects.length) + LAND_OBJECTS_PAGE_NULLCOUNT) + nature.length) + LAND_NATURE_PAGE_NULLCOUNT) + places.length, LAND_PLACES_PAGE_NULLCOUNT);
                System.arraycopy(symbols, 0, emojiUnicodes, (((((((recent.length + people.length) + LAND_PEOPLE_PAGE_NULLCOUNT) + objects.length) + LAND_OBJECTS_PAGE_NULLCOUNT) + nature.length) + LAND_NATURE_PAGE_NULLCOUNT) + places.length) + LAND_PLACES_PAGE_NULLCOUNT, symbols.length);
                System.arraycopy(new int[LAND_SYMBOLS_PAGE_NULLCOUNT], 0, emojiUnicodes, ((((((((recent.length + people.length) + LAND_PEOPLE_PAGE_NULLCOUNT) + objects.length) + LAND_OBJECTS_PAGE_NULLCOUNT) + nature.length) + LAND_NATURE_PAGE_NULLCOUNT) + places.length) + LAND_PLACES_PAGE_NULLCOUNT) + symbols.length, LAND_SYMBOLS_PAGE_NULLCOUNT);
                return emojiUnicodes;
            case 2:
                emojiUnicodes = new int[MULTIWINDOW_TATAL_EMOJI_COUNT];
                recent = new int[MULTIWINDOW_RECENT_EMOJI_COUNT];
                System.arraycopy(recent, 0, emojiUnicodes, 0, recent.length);
                System.arraycopy(people, 0, emojiUnicodes, recent.length, people.length);
                System.arraycopy(new int[MULTIWINDOW_PEOPLE_PAGE_NULLCOUNT], 0, emojiUnicodes, recent.length + people.length, MULTIWINDOW_PEOPLE_PAGE_NULLCOUNT);
                System.arraycopy(objects, 0, emojiUnicodes, (recent.length + people.length) + MULTIWINDOW_PEOPLE_PAGE_NULLCOUNT, objects.length);
                System.arraycopy(new int[MULTIWINDOW_OBJECTS_PAGE_NULLCOUNT], 0, emojiUnicodes, ((recent.length + people.length) + MULTIWINDOW_PEOPLE_PAGE_NULLCOUNT) + objects.length, MULTIWINDOW_OBJECTS_PAGE_NULLCOUNT);
                System.arraycopy(nature, 0, emojiUnicodes, (((recent.length + people.length) + MULTIWINDOW_PEOPLE_PAGE_NULLCOUNT) + objects.length) + MULTIWINDOW_OBJECTS_PAGE_NULLCOUNT, nature.length);
                System.arraycopy(new int[MULTIWINDOW_NATURE_PAGE_NULLCOUNT], 0, emojiUnicodes, ((((recent.length + people.length) + MULTIWINDOW_PEOPLE_PAGE_NULLCOUNT) + objects.length) + MULTIWINDOW_OBJECTS_PAGE_NULLCOUNT) + nature.length, MULTIWINDOW_NATURE_PAGE_NULLCOUNT);
                System.arraycopy(places, 0, emojiUnicodes, (((((recent.length + people.length) + MULTIWINDOW_PEOPLE_PAGE_NULLCOUNT) + objects.length) + MULTIWINDOW_OBJECTS_PAGE_NULLCOUNT) + nature.length) + MULTIWINDOW_NATURE_PAGE_NULLCOUNT, places.length);
                System.arraycopy(new int[MULTIWINDOW_PLACES_PAGE_NULLCOUNT], 0, emojiUnicodes, ((((((recent.length + people.length) + MULTIWINDOW_PEOPLE_PAGE_NULLCOUNT) + objects.length) + MULTIWINDOW_OBJECTS_PAGE_NULLCOUNT) + nature.length) + MULTIWINDOW_NATURE_PAGE_NULLCOUNT) + places.length, MULTIWINDOW_PLACES_PAGE_NULLCOUNT);
                System.arraycopy(symbols, 0, emojiUnicodes, (((((((recent.length + people.length) + MULTIWINDOW_PEOPLE_PAGE_NULLCOUNT) + objects.length) + MULTIWINDOW_OBJECTS_PAGE_NULLCOUNT) + nature.length) + MULTIWINDOW_NATURE_PAGE_NULLCOUNT) + places.length) + MULTIWINDOW_PLACES_PAGE_NULLCOUNT, symbols.length);
                System.arraycopy(new int[MULTIWINDOW_SYMBOLS_PAGE_NULLCOUNT], 0, emojiUnicodes, ((((((((recent.length + people.length) + MULTIWINDOW_PEOPLE_PAGE_NULLCOUNT) + objects.length) + MULTIWINDOW_OBJECTS_PAGE_NULLCOUNT) + nature.length) + MULTIWINDOW_NATURE_PAGE_NULLCOUNT) + places.length) + MULTIWINDOW_PLACES_PAGE_NULLCOUNT) + symbols.length, MULTIWINDOW_SYMBOLS_PAGE_NULLCOUNT);
                return emojiUnicodes;
            default:
                emojiUnicodes = new int[0];
                MLog.e("Mms/SmileyParser", "use wrong emoji index, please check");
                return emojiUnicodes;
        }
    }

    public static synchronized void setRecentEmojiTexts(String text) {
        synchronized (SmileyParser.class) {
            mRecentEmojiTexts = mPrefs.getString("recent_emoji", mRecentEmojiTexts);
            if (mRecentEmojiTexts == null) {
                mRecentEmojiTexts = text;
                mPrefs.edit().putString("recent_emoji", mRecentEmojiTexts).apply();
                return;
            }
            String[] splitEmojiTexts = mRecentEmojiTexts.split("-");
            if (TextUtils.equals(splitEmojiTexts[0], text)) {
                return;
            }
            int splitEmojiTextCount = splitEmojiTexts.length < LAND_RECENT_EMOJI_COUNT ? splitEmojiTexts.length : LAND_RECENT_EMOJI_COUNT;
            int similar = 0;
            boolean hadSimilarEmoji = false;
            for (int i = 0; i < splitEmojiTextCount; i++) {
                if (text.equals(splitEmojiTexts[i])) {
                    hadSimilarEmoji = true;
                    similar = i;
                    break;
                }
            }
            int j;
            if (hadSimilarEmoji) {
                mRecentEmojiTexts = "";
                for (j = 0; j < splitEmojiTextCount; j++) {
                    if (similar != j) {
                        mRecentEmojiTexts += "-" + splitEmojiTexts[j];
                    }
                }
                mRecentEmojiTexts = text + mRecentEmojiTexts;
                mPrefs.edit().putString("recent_emoji", mRecentEmojiTexts).apply();
                return;
            }
            if (splitEmojiTextCount == LAND_RECENT_EMOJI_COUNT) {
                mRecentEmojiTexts = "";
                for (j = 0; j < splitEmojiTextCount - 1; j++) {
                    mRecentEmojiTexts += "-" + splitEmojiTexts[j];
                }
                mRecentEmojiTexts = text + mRecentEmojiTexts;
            } else {
                mRecentEmojiTexts = text + "-" + mRecentEmojiTexts;
            }
            mPrefs.edit().putString("recent_emoji", mRecentEmojiTexts).apply();
        }
    }

    public static synchronized String[] getRecentSmiley() {
        String[] recentStrings;
        synchronized (SmileyParser.class) {
            String recentString = mPrefs.getString("recent_emoji", null);
            recentStrings = null;
            if (recentString != null) {
                recentStrings = recentString.split("-");
            }
        }
        return recentStrings;
    }

    public static int[] getSmileyResIds() {
        if (mCust == null || !mCust.isRcsSwitchOn()) {
            return (int[]) DEFAULT_SMILEY_RES_IDS.clone();
        }
        return mCust.getSmileyResIds();
    }

    private static synchronized HashMap<String, Integer> buildSmileyToRes(Context context) {
        HashMap<String, Integer> smileyToRes;
        synchronized (SmileyParser.class) {
            if (mSmileyTexts == null || DEFAULT_SMILEY_RES_IDS.length != mSmileyTexts.length) {
                useEnLocale = true;
                mSmileyTexts = getSmileTexts(context);
                if (DEFAULT_SMILEY_RES_IDS.length != mSmileyTexts.length) {
                    throw new IllegalStateException("Smiley resource ID/text mismatch");
                }
            }
            smileyToRes = new HashMap(mSmileyTexts.length);
            for (int i = 0; i < mSmileyTexts.length; i++) {
                smileyToRes.put(mSmileyTexts[i], Integer.valueOf(DEFAULT_SMILEY_RES_IDS[i]));
            }
        }
        return smileyToRes;
    }

    public static synchronized String getSmileyText(int index) {
        String str;
        synchronized (SmileyParser.class) {
            str = mSmileyTexts[index];
        }
        return str;
    }

    private Pattern buildPattern() {
        StringBuilder patternString;
        synchronized (SmileyParser.class) {
            int len = mSmileyTexts.length - 1;
            patternString = new StringBuilder(len * 3);
            patternString.append('(');
            for (int i = 0; i < len; i++) {
                patternString.append(Pattern.quote(mSmileyTexts[i]));
                patternString.append('|');
            }
            MLog.i("Mms/SmileyParser", "buildPattern");
        }
        patternString.replace(patternString.length() - 1, patternString.length(), ")");
        return Pattern.compile(patternString.toString());
    }

    public CharSequence addSmileySpans(CharSequence text, SMILEY_TYPE smileyType) {
        return addSmileySpans(text, smileyType, (float) ContentUtil.FONT_SIZE_NORMAL);
    }

    public CharSequence addSmileySpans(CharSequence text, SMILEY_TYPE smileyType, float scale) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        SpannableStringBuilder builder = new SpannableStringBuilder();
        addSmileySpans(text, smileyType, builder, scale);
        return builder;
    }

    public void addSmileySpans(CharSequence text, SMILEY_TYPE smileyType, SpannableStringBuilder builder, float fontscale) {
        builder.clear();
        builder.append(text);
        Matcher matcher = this.mPattern.matcher(text);
        while (matcher.find()) {
            String faceText = matcher.group();
            int resId = ((Integer) this.mSmileyToRes.get(faceText)).intValue();
            Object obj = faceText;
            if (resId > 0) {
                Object tmp = createImageSpan(resId, smileyType, fontscale);
                if (tmp != null) {
                    obj = tmp;
                }
            }
            builder.setSpan(obj, matcher.start(), matcher.end(), 33);
        }
    }

    public void addSmileySpans(CharSequence text, SMILEY_TYPE smileyType, SpannableStringBuilder builder) {
        addSmileySpans(text, smileyType, builder, ContentUtil.FONT_SIZE_NORMAL);
    }

    public ImageSpan createImageSpan(int resId, SMILEY_TYPE faceType, float scale) {
        int size;
        DrawableCache drawableCache;
        Exception e;
        float fontScale = this.mContext.getResources().getConfiguration().fontScale;
        if (SMILEY_TYPE.MESSAGE_TEXTVIEW == faceType || SMILEY_TYPE.MESSAGE_EDITTEXT == faceType) {
            size = (int) ((((float) this.smileySize) * fontScale) * scale);
        } else if (SMILEY_TYPE.SEARCH_MESSAGE_TEXTVIEW == faceType) {
            size = this.searchSmileySize;
        } else if (SMILEY_TYPE.CONV_LIST_TEXTVIEW == faceType) {
            size = this.mContext.getResources().getDimensionPixelOffset(R.dimen.conv_list_smiley_size);
        } else {
            size = this.smileySize;
        }
        if (this.imageCache != null) {
            drawableCache = (DrawableCache) this.imageCache.get(Integer.valueOf(resId));
        } else {
            drawableCache = null;
        }
        if (!(drawableCache == null || drawableCache.drawableReference == null)) {
            Drawable d = (Drawable) drawableCache.drawableReference.get();
            if (d != null && drawableCache.faceType == faceType && drawableCache.size == size) {
                return new ImageSpan(d, getImageSpanAlign(faceType));
            }
        }
        try {
            Drawable drawable = this.mContext.getResources().getDrawable(resId);
            if (SMILEY_TYPE.LIST_TEXTVIEW == faceType) {
                drawable.setBounds(0, size / 2, size, (size * 3) / 2);
            } else if (SMILEY_TYPE.CONV_LIST_TEXTVIEW == faceType || SMILEY_TYPE.MESSAGE_TEXTVIEW == faceType) {
                drawable.setBounds(0, size / 4, size, (size * 5) / 4);
            } else {
                drawable.setBounds(0, 0, size, size);
            }
            DrawableCache drawableCache2 = new DrawableCache(drawable, faceType, size);
            try {
                if (this.imageCache != null) {
                    this.imageCache.put(Integer.valueOf(resId), drawableCache2);
                }
                return new ImageSpan(drawable, getImageSpanAlign(faceType));
            } catch (Exception e2) {
                e = e2;
                e.printStackTrace();
                return null;
            }
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            return null;
        }
    }

    private int getImageSpanAlign(SMILEY_TYPE faceType) {
        if (SMILEY_TYPE.LIST_TEXTVIEW == faceType || SMILEY_TYPE.MESSAGE_TEXTVIEW == faceType || SMILEY_TYPE.MESSAGE_EDITTEXT != faceType) {
            return 1;
        }
        return 0;
    }

    private static Resources getResourceAsLocal(Context context, Locale local) {
        Resources standardResources = context.getResources();
        AssetManager assets = standardResources.getAssets();
        DisplayMetrics metrics = standardResources.getDisplayMetrics();
        Configuration config = new Configuration(standardResources.getConfiguration());
        config.locale = local;
        return new Resources(assets, metrics, config);
    }

    public static synchronized String[] getSmileTexts(Context context) {
        String[] stringArray;
        synchronized (SmileyParser.class) {
            Resources res = useEnLocale ? getResourceAsLocal(context, Locale.US) : context.getResources();
            stringArray = res == null ? new String[0] : res.getStringArray(R.array.default_smiley_texts);
        }
        return stringArray;
    }

    public static int getCategoryNum() {
        return sCategoryName.length;
    }

    public static String getCategoryName(int categoryId, int categoryPageId) {
        return sCategoryName[categoryId] + "-" + categoryPageId;
    }

    public static int getCategoryIdByTabId(String name) {
        return ((Integer) mCategoryNameToIdMap.get(name.split("-")[0])).intValue();
    }

    public static int getCategoryIdByPagerPosition(int position, boolean isConfigurationChanged, Context context) {
        boolean isLand = isLand(context);
        boolean isInMultiWindow = isInMultiWindowMode(context);
        if (isConfigurationChanged && !isInMultiWindow) {
            isLand = !isLand;
        }
        if (isInMultiWindow) {
            if (position == 0) {
                return 0;
            }
            if (position > 0 && position < MULTIWINDOW_OBJECTS_PAGE) {
                return 1;
            }
            if (MULTIWINDOW_OBJECTS_PAGE <= position && position < MULTIWINDOW_NATURE_PAGE) {
                return 2;
            }
            if (MULTIWINDOW_NATURE_PAGE <= position && position < MULTIWINDOW_PLACES_PAGE) {
                return 3;
            }
            if (MULTIWINDOW_PLACES_PAGE <= position && position < MULTIWINDOW_SYMBOLS_PAGE) {
                return 4;
            }
            if (MULTIWINDOW_SYMBOLS_PAGE <= position) {
                return 5;
            }
            return -1;
        } else if (isLand) {
            if (position == 0) {
                return 0;
            }
            if (position > 0 && position < LAND_OBJECTS_PAGE) {
                return 1;
            }
            if (LAND_OBJECTS_PAGE <= position && position < LAND_NATURE_PAGE) {
                return 2;
            }
            if (LAND_NATURE_PAGE <= position && position < LAND_PLACES_PAGE) {
                return 3;
            }
            if (LAND_PLACES_PAGE <= position && position < LAND_SYMBOLS_PAGE) {
                return 4;
            }
            if (LAND_SYMBOLS_PAGE <= position) {
                return 5;
            }
            return -1;
        } else if (position == 0) {
            return 0;
        } else {
            if (position > 0 && position < OBJECTS_PAGE) {
                return 1;
            }
            if (OBJECTS_PAGE <= position && position < NATURE_PAGE) {
                return 2;
            }
            if (NATURE_PAGE <= position && position < PLACES_PAGE) {
                return 3;
            }
            if (PLACES_PAGE <= position && position < SYMBOLS_PAGE) {
                return 4;
            }
            if (SYMBOLS_PAGE <= position) {
                return 5;
            }
            return -1;
        }
    }

    public static int getCategoryIdByPagerPositionWhenMultiWindowChanged(int position, boolean isInMultiWindowMode, Context context) {
        boolean isLand = MmsApp.getApplication().getApplicationContext().getResources().getConfiguration().orientation == 2;
        if (isInMultiWindowMode) {
            if (isLand) {
                if (position == 0) {
                    return 0;
                }
                if (position > 0 && position < LAND_OBJECTS_PAGE) {
                    return 1;
                }
                if (LAND_OBJECTS_PAGE <= position && position < LAND_NATURE_PAGE) {
                    return 2;
                }
                if (LAND_NATURE_PAGE <= position && position < LAND_PLACES_PAGE) {
                    return 3;
                }
                if (LAND_PLACES_PAGE <= position && position < LAND_SYMBOLS_PAGE) {
                    return 4;
                }
                if (LAND_SYMBOLS_PAGE <= position) {
                    return 5;
                }
                return -1;
            } else if (position == 0) {
                return 0;
            } else {
                if (position > 0 && position < OBJECTS_PAGE) {
                    return 1;
                }
                if (OBJECTS_PAGE <= position && position < NATURE_PAGE) {
                    return 2;
                }
                if (NATURE_PAGE <= position && position < PLACES_PAGE) {
                    return 3;
                }
                if (PLACES_PAGE <= position && position < SYMBOLS_PAGE) {
                    return 4;
                }
                if (SYMBOLS_PAGE <= position) {
                    return 5;
                }
                return -1;
            }
        } else if (position == 0) {
            return 0;
        } else {
            if (position > 0 && position < MULTIWINDOW_OBJECTS_PAGE) {
                return 1;
            }
            if (MULTIWINDOW_OBJECTS_PAGE <= position && position < MULTIWINDOW_NATURE_PAGE) {
                return 2;
            }
            if (MULTIWINDOW_NATURE_PAGE <= position && position < MULTIWINDOW_PLACES_PAGE) {
                return 3;
            }
            if (MULTIWINDOW_PLACES_PAGE <= position && position < MULTIWINDOW_SYMBOLS_PAGE) {
                return 4;
            }
            if (MULTIWINDOW_SYMBOLS_PAGE <= position) {
                return 5;
            }
            return -1;
        }
    }

    public static int getCategoryPageCount(int categoryId, Context context) {
        boolean isLand = isLand(context);
        if (isInMultiWindowMode(context)) {
            switch (categoryId) {
                case 0:
                    return MULTIWINDOW_RECENTS_PAGECOUNT;
                case 1:
                    return MULTIWINDOW_PEOPLE_PAGECOUNT;
                case 2:
                    return MULTIWINDOW_OBJECTS_PAGECOUNT;
                case 3:
                    return MULTIWINDOW_NATURE_PAGECOUNT;
                case 4:
                    return MULTIWINDOW_PLACES_PAGECOUNT;
                case 5:
                    return MULTIWINDOW_SYMBOLS_PAGECOUNT;
                default:
                    return -1;
            }
        } else if (isLand) {
            switch (categoryId) {
                case 0:
                    return LAND_RECENTS_PAGECOUNT;
                case 1:
                    return LAND_PEOPLE_PAGECOUNT;
                case 2:
                    return LAND_OBJECTS_PAGECOUNT;
                case 3:
                    return LAND_NATURE_PAGECOUNT;
                case 4:
                    return LAND_PLACES_PAGECOUNT;
                case 5:
                    return LAND_SYMBOLS_PAGECOUNT;
                default:
                    return -1;
            }
        } else {
            switch (categoryId) {
                case 0:
                    return RECENTS_PAGECOUNT;
                case 1:
                    return PEOPLE_PAGECOUNT;
                case 2:
                    return OBJECTS_PAGECOUNT;
                case 3:
                    return NATURE_PAGECOUNT;
                case 4:
                    return PLACES_PAGECOUNT;
                case 5:
                    return SYMBOLS_PAGECOUNT;
                default:
                    return -1;
            }
        }
    }

    public static int getEmojiPagePosition(int categoryId, Context context) {
        boolean isLand = isLand(context);
        if (isInMultiWindowMode(context)) {
            switch (categoryId) {
                case 0:
                    return 0;
                case 1:
                    return MULTIWINDOW_RECENTS_PAGECOUNT;
                case 2:
                    return MULTIWINDOW_OBJECTS_PAGE;
                case 3:
                    return MULTIWINDOW_NATURE_PAGE;
                case 4:
                    return MULTIWINDOW_PLACES_PAGE;
                case 5:
                    return MULTIWINDOW_SYMBOLS_PAGE;
                default:
                    return -1;
            }
        } else if (isLand) {
            switch (categoryId) {
                case 0:
                    return 0;
                case 1:
                    return LAND_RECENTS_PAGECOUNT;
                case 2:
                    return LAND_OBJECTS_PAGE;
                case 3:
                    return LAND_NATURE_PAGE;
                case 4:
                    return LAND_PLACES_PAGE;
                case 5:
                    return LAND_SYMBOLS_PAGE;
                default:
                    return -1;
            }
        } else {
            switch (categoryId) {
                case 0:
                    return 0;
                case 1:
                    return RECENTS_PAGECOUNT;
                case 2:
                    return OBJECTS_PAGE;
                case 3:
                    return NATURE_PAGE;
                case 4:
                    return PLACES_PAGE;
                case 5:
                    return SYMBOLS_PAGE;
                default:
                    return -1;
            }
        }
    }

    public static int getPageIndicatorPosition(int categoryID, int position, Context context) {
        boolean isLand = isLand(context);
        if (isInMultiWindowMode(context)) {
            switch (categoryID) {
                case 0:
                    return 0;
                case 1:
                    return position - MULTIWINDOW_RECENTS_PAGE;
                case 2:
                    return position - MULTIWINDOW_OBJECTS_PAGE;
                case 3:
                    return position - MULTIWINDOW_NATURE_PAGE;
                case 4:
                    return position - MULTIWINDOW_PLACES_PAGE;
                case 5:
                    return position < MULTIWINDOW_TOTAL_PAGE ? position - MULTIWINDOW_SYMBOLS_PAGE : MULTIWINDOW_SYMBOLS_PAGECOUNT - 1;
                default:
                    return -1;
            }
        } else if (isLand) {
            switch (categoryID) {
                case 0:
                    return 0;
                case 1:
                    return position - LAND_RECENTS_PAGE;
                case 2:
                    return position - LAND_OBJECTS_PAGE;
                case 3:
                    return position - LAND_NATURE_PAGE;
                case 4:
                    return position - LAND_PLACES_PAGE;
                case 5:
                    return position < LAND_TOTAL_PAGE ? position - LAND_SYMBOLS_PAGE : LAND_SYMBOLS_PAGECOUNT - 1;
                default:
                    return -1;
            }
        } else {
            switch (categoryID) {
                case 0:
                    return 0;
                case 1:
                    return position - RECENTS_PAGE;
                case 2:
                    return position - OBJECTS_PAGE;
                case 3:
                    return position - NATURE_PAGE;
                case 4:
                    return position - PLACES_PAGE;
                case 5:
                    return position < TOTAL_PAGE ? position - SYMBOLS_PAGE : SYMBOLS_PAGECOUNT - 1;
                default:
                    return -1;
            }
        }
    }

    private static boolean isLand(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        if (context.getResources().getConfiguration().orientation == 2) {
            z = true;
        }
        return z;
    }

    public static boolean isInMultiWindowMode(Context context) {
        if (context instanceof Activity) {
            return ((Activity) context).isInMultiWindowMode();
        }
        return false;
    }

    private static void initSmileyPageCount(Context context) {
        if (context == null) {
            MLog.e("Mms/SmileyParser", "initSmileyPageCount, but context is null");
            return;
        }
        Resources res = context.getResources();
        if (res == null) {
            MLog.e("Mms/SmileyParser", "initSmileyPageCount, but resources is null");
            return;
        }
        int rowCountPortrait = res.getInteger(R.integer.row_smile);
        int rowCountLandspace = res.getInteger(R.integer.row_land_emoji);
        int rowCountMultiwindow = res.getInteger(R.integer.row_multiwindow_emoji);
        if (EMOJI_ROW_COUNT_PORTRAIT == rowCountPortrait && EMOJI_ROW_COUNT_LANDSPACE == rowCountLandspace && EMOJI_ROW_COUNT_MULTIWINDOW == rowCountMultiwindow) {
            MLog.e("Mms/SmileyParser", "initSmileyPageCount, already init smiley page and count");
            return;
        }
        int length;
        EMOJI_ROW_COUNT_PORTRAIT = rowCountPortrait;
        EMOJI_ROW_COUNT_LANDSPACE = rowCountLandspace;
        EMOJI_ROW_COUNT_MULTIWINDOW = rowCountMultiwindow;
        int columCountPortrait = res.getInteger(R.integer.smile_colum_emoji);
        int columCountLandspace = res.getInteger(R.integer.smile_colum_land_emoji);
        int columCountMultiwindow = res.getInteger(R.integer.smile_colum_multiwindow_emoji);
        RECENT_EMOJI_COUNT = columCountPortrait * rowCountPortrait;
        LAND_RECENT_EMOJI_COUNT = columCountLandspace * rowCountLandspace;
        MULTIWINDOW_RECENT_EMOJI_COUNT = columCountMultiwindow * rowCountMultiwindow;
        int[] people = res.getIntArray(R.array.default_emoji_unicode_people);
        int[] objects = res.getIntArray(R.array.default_emoji_unicode_objects);
        int[] nature = res.getIntArray(R.array.default_emoji_unicode_nature);
        int[] places = res.getIntArray(R.array.default_emoji_unicode_places);
        int[] symbols = res.getIntArray(R.array.default_emoji_unicode_symbols);
        PEOPLE_PAGE_NULLCOUNT = RECENT_EMOJI_COUNT - (people.length % RECENT_EMOJI_COUNT);
        OBJECTS_PAGE_NULLCOUNT = RECENT_EMOJI_COUNT - (objects.length % RECENT_EMOJI_COUNT);
        NATURE_PAGE_NULLCOUNT = RECENT_EMOJI_COUNT - (nature.length % RECENT_EMOJI_COUNT);
        PLACES_PAGE_NULLCOUNT = RECENT_EMOJI_COUNT - (places.length % RECENT_EMOJI_COUNT);
        SYMBOLS_PAGE_NULLCOUNT = RECENT_EMOJI_COUNT - (symbols.length % RECENT_EMOJI_COUNT);
        RECENTS_PAGECOUNT = res.getInteger(R.integer.recent_emoji_page_count);
        PEOPLE_PAGECOUNT = PEOPLE_PAGE_NULLCOUNT == 0 ? people.length / RECENT_EMOJI_COUNT : (people.length / RECENT_EMOJI_COUNT) + 1;
        OBJECTS_PAGECOUNT = OBJECTS_PAGE_NULLCOUNT == 0 ? objects.length / RECENT_EMOJI_COUNT : (objects.length / RECENT_EMOJI_COUNT) + 1;
        NATURE_PAGECOUNT = NATURE_PAGE_NULLCOUNT == 0 ? nature.length / RECENT_EMOJI_COUNT : (nature.length / RECENT_EMOJI_COUNT) + 1;
        PLACES_PAGECOUNT = PLACES_PAGE_NULLCOUNT == 0 ? places.length / RECENT_EMOJI_COUNT : (places.length / RECENT_EMOJI_COUNT) + 1;
        if (SYMBOLS_PAGE_NULLCOUNT == 0) {
            length = symbols.length / RECENT_EMOJI_COUNT;
        } else {
            length = (symbols.length / RECENT_EMOJI_COUNT) + 1;
        }
        SYMBOLS_PAGECOUNT = length;
        RECENTS_PAGE = RECENTS_PAGECOUNT;
        OBJECTS_PAGE = RECENTS_PAGE + PEOPLE_PAGECOUNT;
        NATURE_PAGE = OBJECTS_PAGE + OBJECTS_PAGECOUNT;
        PLACES_PAGE = NATURE_PAGE + NATURE_PAGECOUNT;
        SYMBOLS_PAGE = PLACES_PAGE + PLACES_PAGECOUNT;
        TOTAL_PAGE = SYMBOLS_PAGE + SYMBOLS_PAGECOUNT;
        TATAL_EMOJI_COUNT = RECENT_EMOJI_COUNT * TOTAL_PAGE;
        LAND_PEOPLE_PAGE_NULLCOUNT = LAND_RECENT_EMOJI_COUNT - (people.length % LAND_RECENT_EMOJI_COUNT);
        LAND_OBJECTS_PAGE_NULLCOUNT = LAND_RECENT_EMOJI_COUNT - (objects.length % LAND_RECENT_EMOJI_COUNT);
        LAND_NATURE_PAGE_NULLCOUNT = LAND_RECENT_EMOJI_COUNT - (nature.length % LAND_RECENT_EMOJI_COUNT);
        LAND_PLACES_PAGE_NULLCOUNT = LAND_RECENT_EMOJI_COUNT - (places.length % LAND_RECENT_EMOJI_COUNT);
        LAND_SYMBOLS_PAGE_NULLCOUNT = LAND_RECENT_EMOJI_COUNT - (symbols.length % LAND_RECENT_EMOJI_COUNT);
        LAND_RECENTS_PAGECOUNT = res.getInteger(R.integer.recent_emoji_page_count);
        LAND_PEOPLE_PAGECOUNT = LAND_PEOPLE_PAGE_NULLCOUNT == 0 ? people.length / LAND_RECENT_EMOJI_COUNT : (people.length / LAND_RECENT_EMOJI_COUNT) + 1;
        LAND_OBJECTS_PAGECOUNT = LAND_OBJECTS_PAGE_NULLCOUNT == 0 ? objects.length / LAND_RECENT_EMOJI_COUNT : (objects.length / LAND_RECENT_EMOJI_COUNT) + 1;
        LAND_NATURE_PAGECOUNT = LAND_NATURE_PAGE_NULLCOUNT == 0 ? nature.length / LAND_RECENT_EMOJI_COUNT : (nature.length / LAND_RECENT_EMOJI_COUNT) + 1;
        LAND_PLACES_PAGECOUNT = LAND_PLACES_PAGE_NULLCOUNT == 0 ? places.length / LAND_RECENT_EMOJI_COUNT : (places.length / LAND_RECENT_EMOJI_COUNT) + 1;
        if (LAND_SYMBOLS_PAGE_NULLCOUNT == 0) {
            length = symbols.length / LAND_RECENT_EMOJI_COUNT;
        } else {
            length = (symbols.length / LAND_RECENT_EMOJI_COUNT) + 1;
        }
        LAND_SYMBOLS_PAGECOUNT = length;
        LAND_RECENTS_PAGE = LAND_RECENTS_PAGECOUNT;
        LAND_OBJECTS_PAGE = LAND_RECENTS_PAGE + LAND_PEOPLE_PAGECOUNT;
        LAND_NATURE_PAGE = LAND_OBJECTS_PAGE + LAND_OBJECTS_PAGECOUNT;
        LAND_PLACES_PAGE = LAND_NATURE_PAGE + LAND_NATURE_PAGECOUNT;
        LAND_SYMBOLS_PAGE = LAND_PLACES_PAGE + LAND_PLACES_PAGECOUNT;
        LAND_TOTAL_PAGE = LAND_SYMBOLS_PAGE + LAND_SYMBOLS_PAGECOUNT;
        LAND_TATAL_EMOJI_COUNT = LAND_RECENT_EMOJI_COUNT * LAND_TOTAL_PAGE;
        MULTIWINDOW_PEOPLE_PAGE_NULLCOUNT = MULTIWINDOW_RECENT_EMOJI_COUNT - (people.length % MULTIWINDOW_RECENT_EMOJI_COUNT);
        MULTIWINDOW_OBJECTS_PAGE_NULLCOUNT = MULTIWINDOW_RECENT_EMOJI_COUNT - (objects.length % MULTIWINDOW_RECENT_EMOJI_COUNT);
        MULTIWINDOW_NATURE_PAGE_NULLCOUNT = MULTIWINDOW_RECENT_EMOJI_COUNT - (nature.length % MULTIWINDOW_RECENT_EMOJI_COUNT);
        MULTIWINDOW_PLACES_PAGE_NULLCOUNT = MULTIWINDOW_RECENT_EMOJI_COUNT - (places.length % MULTIWINDOW_RECENT_EMOJI_COUNT);
        MULTIWINDOW_SYMBOLS_PAGE_NULLCOUNT = MULTIWINDOW_RECENT_EMOJI_COUNT - (symbols.length % MULTIWINDOW_RECENT_EMOJI_COUNT);
        MULTIWINDOW_RECENTS_PAGECOUNT = res.getInteger(R.integer.recent_emoji_page_count);
        MULTIWINDOW_PEOPLE_PAGECOUNT = MULTIWINDOW_PEOPLE_PAGE_NULLCOUNT == 0 ? people.length / MULTIWINDOW_RECENT_EMOJI_COUNT : (people.length / MULTIWINDOW_RECENT_EMOJI_COUNT) + 1;
        MULTIWINDOW_OBJECTS_PAGECOUNT = MULTIWINDOW_OBJECTS_PAGE_NULLCOUNT == 0 ? objects.length / MULTIWINDOW_RECENT_EMOJI_COUNT : (objects.length / MULTIWINDOW_RECENT_EMOJI_COUNT) + 1;
        MULTIWINDOW_NATURE_PAGECOUNT = MULTIWINDOW_NATURE_PAGE_NULLCOUNT == 0 ? nature.length / MULTIWINDOW_RECENT_EMOJI_COUNT : (nature.length / MULTIWINDOW_RECENT_EMOJI_COUNT) + 1;
        MULTIWINDOW_PLACES_PAGECOUNT = MULTIWINDOW_PLACES_PAGE_NULLCOUNT == 0 ? places.length / MULTIWINDOW_RECENT_EMOJI_COUNT : (places.length / MULTIWINDOW_RECENT_EMOJI_COUNT) + 1;
        MULTIWINDOW_SYMBOLS_PAGECOUNT = MULTIWINDOW_SYMBOLS_PAGE_NULLCOUNT == 0 ? symbols.length / MULTIWINDOW_RECENT_EMOJI_COUNT : (symbols.length / MULTIWINDOW_RECENT_EMOJI_COUNT) + 1;
        MULTIWINDOW_RECENTS_PAGE = MULTIWINDOW_RECENTS_PAGECOUNT;
        MULTIWINDOW_OBJECTS_PAGE = MULTIWINDOW_RECENTS_PAGE + MULTIWINDOW_PEOPLE_PAGECOUNT;
        MULTIWINDOW_NATURE_PAGE = MULTIWINDOW_OBJECTS_PAGE + MULTIWINDOW_OBJECTS_PAGECOUNT;
        MULTIWINDOW_PLACES_PAGE = MULTIWINDOW_NATURE_PAGE + MULTIWINDOW_NATURE_PAGECOUNT;
        MULTIWINDOW_SYMBOLS_PAGE = MULTIWINDOW_PLACES_PAGE + MULTIWINDOW_PLACES_PAGECOUNT;
        MULTIWINDOW_TOTAL_PAGE = MULTIWINDOW_SYMBOLS_PAGE + MULTIWINDOW_SYMBOLS_PAGECOUNT;
        MULTIWINDOW_TATAL_EMOJI_COUNT = MULTIWINDOW_RECENT_EMOJI_COUNT * MULTIWINDOW_TOTAL_PAGE;
    }

    public static int getRecentEmojiCount() {
        return RECENT_EMOJI_COUNT;
    }

    public static int getLandspaceRecentEmojiCount() {
        return LAND_RECENT_EMOJI_COUNT;
    }

    public static int getMultiWindowRecentEmojiCount() {
        return MULTIWINDOW_RECENT_EMOJI_COUNT;
    }
}
