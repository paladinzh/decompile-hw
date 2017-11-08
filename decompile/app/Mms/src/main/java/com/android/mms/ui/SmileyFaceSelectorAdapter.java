package com.android.mms.ui;

import android.content.Context;
import com.android.mms.ui.IconListAdapter.IconListItem;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.Smileys;
import com.android.rcs.RcsCommonConfig;
import java.util.ArrayList;
import java.util.List;

public class SmileyFaceSelectorAdapter extends AttachmentTypeSelectorAdapter {
    private static final int[] DEFAULT_SMILEY_RES_IDS = SmileyParser.getSmileyResIds();

    public SmileyFaceSelectorAdapter(Context context, int resourceId, List<IconListItem> items) {
        super(context, resourceId, (List) items);
    }

    public SmileyFaceSelectorAdapter(Context context, int resourceId) {
        super(context, resourceId, getData(context));
    }

    private static List<IconListItem> getData(Context context) {
        List<IconListItem> data;
        int i;
        if (RcsCommonConfig.isRCSSwitchOn()) {
            data = new ArrayList(21);
            int j = DEFAULT_SMILEY_RES_IDS.length;
            for (i = 0; i < j; i++) {
                AttachmentTypeSelectorAdapter.addItem(data, Smileys.getTitle(i), Smileys.getName(i), DEFAULT_SMILEY_RES_IDS[i], Smileys.getCommnd(i));
            }
            return data;
        }
        String[] EMOJI_TEXTS = SmileyParser.getEmojiTexts(context);
        data = new ArrayList(EMOJI_TEXTS.length);
        for (i = 0; i < EMOJI_TEXTS.length; i++) {
            AttachmentTypeSelectorAdapter.addItem(data, EMOJI_TEXTS[i], EMOJI_TEXTS[i], i, Smileys.getCommnd(i));
        }
        return data;
    }

    public static List<IconListItem> getRecentEmojiData(Context context) {
        String[] EMOJI_TEXTS = SmileyParser.getRecentSmiley();
        if (EMOJI_TEXTS == null) {
            return null;
        }
        int onePageEmojiCount = SmileyParser.isInMultiWindowMode(context) ? SmileyParser.getMultiWindowRecentEmojiCount() : context.getResources().getConfiguration().orientation == 2 ? SmileyParser.getLandspaceRecentEmojiCount() : SmileyParser.getRecentEmojiCount();
        int recentEmojiCount = EMOJI_TEXTS.length < onePageEmojiCount ? EMOJI_TEXTS.length : onePageEmojiCount;
        List<IconListItem> data = new ArrayList(recentEmojiCount);
        for (int i = 0; i < recentEmojiCount; i++) {
            AttachmentTypeSelectorAdapter.addItem(data, EMOJI_TEXTS[i], EMOJI_TEXTS[i], i, Smileys.getCommnd(i));
        }
        return data;
    }
}
