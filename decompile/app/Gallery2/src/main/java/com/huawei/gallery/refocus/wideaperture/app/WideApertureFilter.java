package com.huawei.gallery.refocus.wideaperture.app;

import com.android.gallery3d.R;

public enum WideApertureFilter {
    NONE(R.string.Label_Gallery_Allfocus_effect_none, R.drawable.allfocus_original, R.drawable.allfocus_mono_original, 0),
    PENCIL(R.string.Label_Gallery_Allfocus_effect_pencil, R.drawable.allfocus_pencil, R.drawable.allfocus_mono_pencil, 1),
    COMIC(R.string.Label_Gallery_Allfocus_effect_comic, R.drawable.allfocus_comic, R.drawable.allfocus_mono_comic, 2),
    MONO(R.string.Label_Gallery_Allfocus_effect_mono, R.drawable.allfocus_mono, R.drawable.allfocus_mono, 3),
    PINFOCUS(R.string.Label_Gallery_Allfocus_effect_pinfocus, R.drawable.allfocus_pinfocus, R.drawable.allfocus_mono_pinfocus, 4),
    MINIATRUE(R.string.Label_Gallery_Allfocus_effect_miniature, R.drawable.allfocus_miniature, R.drawable.allfocus_mono_miniature, 5);
    
    private int mColorfulIconResID;
    private int mMonoIconResID;
    private int mTextResID;
    private int mType;

    private WideApertureFilter(int textResID, int colorfulIconResID, int monoIconResIde, int filterType) {
        this.mTextResID = textResID;
        this.mColorfulIconResID = colorfulIconResID;
        this.mMonoIconResID = monoIconResIde;
        this.mType = filterType;
    }

    public static WideApertureFilter getFilter(int filterID) {
        return values()[filterID];
    }

    public static int getFilterCount() {
        return values().length;
    }

    public int getFilterNameID() {
        return this.mTextResID;
    }

    public int getFilterColorfulIconID() {
        return this.mColorfulIconResID;
    }

    public int getFilterMonoIconId() {
        return this.mMonoIconResID;
    }

    public int getFilterType() {
        return this.mType;
    }
}
