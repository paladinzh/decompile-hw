package com.android.mms.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.mms.MmsConfig;
import com.android.mms.directory.DirectoryQuery;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.cache.MmsMatchContact;
import com.huawei.cspcommon.util.DialerHighlighter;
import com.huawei.cspcommon.util.HanziToPinyin.Token;
import com.huawei.cspcommon.util.SearchMatch;
import com.huawei.cspcommon.util.SortUtils;
import com.huawei.mms.util.HighLightMatchUtils;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.MatchedContactsHelper;
import com.huawei.mms.util.NumberUtils;
import com.huawei.mms.util.PrefixHighlighter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MatchedContactsListAdpter extends BaseAdapter {
    private Context mContext;
    private List<String> mCurrrentNumbers;
    private DialerHighlighter mDialerHighlighter;
    private char[] mHighlightedPrefix;
    private Runnable mMatchContactsRunnable = new Runnable() {
        private String lastQueryString = null;

        public void run() {
            long startTime = SystemClock.uptimeMillis();
            this.lastQueryString = MatchedContactsListAdpter.this.mSearchKey;
            ArrayList<MmsMatchContact> list = MatchedContactsHelper.getMatchedContacts(MatchedContactsListAdpter.this.mContext, this.lastQueryString);
            HwBackgroundLoader.getUIHandler().removeCallbacksAndMessages(Integer.valueOf(2));
            if (TextUtils.isEmpty(MatchedContactsListAdpter.this.mSearchKey) || !MatchedContactsListAdpter.this.mSearchKey.equals(this.lastQueryString)) {
                MLog.v("MatchedContactsListAdpter", "the query string is empty or changed, do not need refresh UI!!");
                return;
            }
            ArrayList<MmsMatchContact> finalList = MatchedContactsHelper.removeDuplicateMatchContacts(list);
            HwBackgroundLoader.getUIHandler().postAtTime(new RefreshUIRunnable(finalList), Integer.valueOf(2), 0);
            HwMessageUtils.showJlogByID(141, (int) (SystemClock.uptimeMillis() - startTime), "Mms::load " + finalList.size() + " match contacts!");
        }
    };
    private MatchedContactsChangedListener mMatchedContactsChangedListener;
    private PrefixHighlighter mPrefixHighligher;
    private ArrayList<MmsMatchContact> mRecentContactsCache = null;
    private Runnable mRecentContactsRunnable = new Runnable() {
        public void run() {
            long startTime = SystemClock.uptimeMillis();
            ArrayList<MmsMatchContact> list = null;
            synchronized (MatchedContactsListAdpter.class) {
                if (!(MatchedContactsListAdpter.this.mRecentContactsCache == null || MatchedContactsListAdpter.this.mRecentContactsCache.isEmpty())) {
                    list = MatchedContactsListAdpter.this.mRecentContactsCache;
                }
            }
            if (list == null) {
                list = MatchedContactsHelper.initRecentContact(MatchedContactsListAdpter.this.mContext, MmsConfig.getMaxRecentContactsCount());
                synchronized (MatchedContactsListAdpter.class) {
                    MatchedContactsListAdpter.this.mRecentContactsCache = list;
                }
            }
            HwBackgroundLoader.getUIHandler().removeCallbacksAndMessages(Integer.valueOf(1));
            HwBackgroundLoader.getUIHandler().postAtTime(new RefreshUIRunnable(MatchedContactsHelper.removeDuplicateRecentContacts(list, MatchedContactsListAdpter.this.mCurrrentNumbers)), Integer.valueOf(1), 0);
            HwMessageUtils.showJlogByID(140, (int) (SystemClock.uptimeMillis() - startTime), "Mms::load recent contacts!");
        }
    };
    private ArrayList<MmsMatchContact> mRecipientsList = null;
    private String mSearchKey;
    private int mSearchMatchType = 40;
    private SpannableString mSpannable;
    private boolean mTextHighlighted;

    public interface MatchedContactsChangedListener {
        void onMatchedContactsChanged();
    }

    private class RefreshUIRunnable implements Runnable {
        ArrayList<MmsMatchContact> mRecipients;

        RefreshUIRunnable(ArrayList<MmsMatchContact> oldList) {
            this.mRecipients = oldList;
        }

        public void run() {
            MatchedContactsListAdpter.this.setData(this.mRecipients);
        }
    }

    static class ViewHolder {
        ImageView mIconView;
        TextView mNameView;
        TextView mNumberView;

        ViewHolder() {
        }
    }

    public MatchedContactsListAdpter(Context context) {
        this.mContext = context;
        if (this.mContext instanceof Activity) {
            this.mDialerHighlighter = new DialerHighlighter();
        }
        Resources res = this.mContext.getResources();
        int color = HighLightMatchUtils.getControlColor(res);
        if (color != 0) {
            this.mPrefixHighligher = new PrefixHighlighter(color);
        } else {
            this.mPrefixHighligher = new PrefixHighlighter(res.getColor(R.color.incoming_msg_text_color));
        }
    }

    public void setListener(MatchedContactsChangedListener l) {
        this.mMatchedContactsChangedListener = l;
    }

    public int getCount() {
        return this.mRecipientsList != null ? this.mRecipientsList.size() : 0;
    }

    public MmsMatchContact getItem(int position) {
        return this.mRecipientsList != null ? (MmsMatchContact) this.mRecipientsList.get(position) : null;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        MmsMatchContact item = getItem(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.new_message_recipient_list, parent, false);
            holder.mNameView = (TextView) convertView.findViewById(R.id.recipient_list_item_name);
            holder.mNumberView = (TextView) convertView.findViewById(R.id.recipient_list_item_number);
            holder.mIconView = (ImageView) convertView.findViewById(R.id.work_profile_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (item != null) {
            if (TextUtils.isEmpty(item.mName)) {
                holder.mNameView.setVisibility(8);
                holder.mNumberView.setText(NumberUtils.formatAndParseNumber(item.mNumber, null));
                holder.mIconView.setVisibility(8);
            } else {
                holder.mNameView.setVisibility(0);
                holder.mNameView.setText(item.mName);
                if (DirectoryQuery.isEnterpriseContactId(item.mContactId.longValue())) {
                    holder.mIconView.setVisibility(0);
                } else {
                    holder.mIconView.setVisibility(8);
                }
                holder.mNumberView.setText(NumberUtils.formatAndParseNumber(item.mNumber, null));
                this.mSpannable = new SpannableString(item.mName);
                this.mTextHighlighted = false;
                holder.mNameView.setTag(item);
                this.mSearchMatchType = item.getType();
                setHighlightedPrefix(this.mSearchKey);
                if (this.mHighlightedPrefix != null && (this.mSearchMatchType == 40 || this.mSearchMatchType == 32)) {
                    highlighter(item, holder.mNameView, String.valueOf(this.mHighlightedPrefix), null);
                }
                if (!TextUtils.isEmpty(this.mSearchKey)) {
                    setHighlightedPrefix(NumberUtils.getFilterNumber(this.mSearchKey));
                    setNumberTextAndHighter(NumberUtils.formatAndParseNumber(item.mNumber, null), holder.mNumberView);
                }
            }
            if (item.mContactId.longValue() > 0 && (convertView instanceof MatchedContactsListItem)) {
                ((MatchedContactsListItem) convertView).updateAvatarIcon(item.mNumber, item.mName, item.mContactId.longValue(), item.mLookupKey);
            }
        }
        return convertView;
    }

    public void clearRecipientsList() {
        if (this.mRecipientsList != null) {
            this.mRecipientsList.clear();
            setData(null);
            releaseBackgroundLoader();
        }
    }

    private void setData(ArrayList<MmsMatchContact> oldList) {
        if (oldList != null) {
            this.mRecipientsList = new ArrayList(oldList);
        } else {
            this.mRecipientsList = null;
        }
        notifyDataSetChanged();
        if (this.mMatchedContactsChangedListener != null) {
            this.mMatchedContactsChangedListener.onMatchedContactsChanged();
        }
    }

    public ArrayList<MmsMatchContact> getData() {
        return this.mRecipientsList;
    }

    public void clearRecentContactsCache() {
        synchronized (MatchedContactsListAdpter.class) {
            if (this.mRecentContactsCache != null) {
                this.mRecentContactsCache = null;
            }
        }
    }

    private void releaseBackgroundLoader() {
        HwBackgroundLoader.getBackgroundHandler().removeCallbacks(this.mRecentContactsRunnable);
        HwBackgroundLoader.getUIHandler().removeCallbacksAndMessages(Integer.valueOf(1));
        HwBackgroundLoader.getBackgroundHandler().removeCallbacks(this.mMatchContactsRunnable);
        HwBackgroundLoader.getUIHandler().removeCallbacksAndMessages(Integer.valueOf(2));
    }

    public void loadRecentContacts(List<String> numbers) {
        this.mSearchKey = "";
        this.mCurrrentNumbers = numbers;
        HwBackgroundLoader.getBackgroundHandler().removeCallbacks(this.mRecentContactsRunnable);
        HwBackgroundLoader.getBackgroundHandler().removeCallbacks(this.mMatchContactsRunnable);
        HwBackgroundLoader.getBackgroundHandler().postDelayed(this.mRecentContactsRunnable, 100);
    }

    public void loadMatchContacts(String searchKey) {
        this.mSearchKey = searchKey;
        HwBackgroundLoader.getBackgroundHandler().removeCallbacks(this.mMatchContactsRunnable);
        HwBackgroundLoader.getBackgroundHandler().removeCallbacks(this.mRecentContactsRunnable);
        HwBackgroundLoader.getBackgroundHandler().postDelayed(this.mMatchContactsRunnable, 100);
    }

    private void highlighter(MmsMatchContact item, TextView nameView, String prefix, String sortKey) {
        int mode;
        String name;
        ArrayList tokens = null;
        if (item.mName.equals(item.mSortKey)) {
            mode = 0;
            name = item.mName.toLowerCase(Locale.getDefault());
        } else {
            Object[] objs;
            mode = 0;
            DialerHighlighter dialerHighlighter;
            if (Locale.getDefault().getCountry().equalsIgnoreCase("TW")) {
                dialerHighlighter = this.mDialerHighlighter;
                objs = DialerHighlighter.convertToZhuyin(item.mName);
                prefix = prefix.replace("_", "");
            } else {
                dialerHighlighter = this.mDialerHighlighter;
                objs = DialerHighlighter.convertToPinyin(item.mName);
            }
            if (objs.length != 0) {
                ArrayList<Token> tokens2 = objs[0];
                name = objs[1];
                for (Token t : tokens2) {
                    if (t.type != 2) {
                        if (t.type == 3) {
                        }
                    }
                    mode = 1;
                }
            } else {
                return;
            }
        }
        highlightMatchInitials(name, prefix, nameView, item.mType, item, mode, tokens);
    }

    private void highlightMatchInitials(String aTarget, String input, TextView aNameView, int aType, MmsMatchContact item, int aMode, ArrayList<Token> aTokens) {
        if (input != null && input.length() != 0) {
            int[] startIndex;
            int i;
            int begin;
            int end;
            StringBuffer newMatchPinyin = new StringBuffer();
            String language = Locale.getDefault().getCountry();
            if ("JP".equalsIgnoreCase(language) || "KR".equalsIgnoreCase(language)) {
                startIndex = new int[0];
            } else {
                startIndex = SearchMatch.getMatchIndex(aTarget, input, item.mName, false, newMatchPinyin, this.mContext);
            }
            if (newMatchPinyin.length() != 0) {
                aTarget = newMatchPinyin.toString();
            }
            int targetLength = aTarget.length();
            for (i = 0; i < startIndex.length / 2; i++) {
                begin = startIndex[i * 2];
                end = startIndex[(i * 2) + 1];
                if (begin >= 0 && end >= begin && end < targetLength) {
                    if (aMode == 0) {
                        if (item.equals(aNameView.getTag()) && end < this.mSpannable.length()) {
                            this.mSpannable.setSpan(new ForegroundColorSpan(this.mPrefixHighligher.getHighlightColor()), begin, end + 1, 33);
                            setMarqueeText(aNameView, this.mSpannable);
                            this.mTextHighlighted = true;
                        }
                    } else {
                        boolean isChineseOrEnglishchar;
                        if (SortUtils.isChinese(input.charAt(0)) || SortUtils.isZhuyin(input.charAt(0))) {
                            isChineseOrEnglishchar = true;
                        } else {
                            isChineseOrEnglishchar = SortUtils.isEnglish(input.charAt(0));
                        }
                        if (item.equals(aNameView.getTag()) && r16) {
                            highlightChineseItem(begin, end, aNameView, aTarget, aTokens, item);
                        }
                    }
                }
            }
            if (startIndex.length == 0 || !this.mTextHighlighted) {
                int queryIndex = item.mName.toLowerCase(Locale.getDefault()).indexOf(input.toLowerCase(Locale.getDefault()));
                if (queryIndex > -1) {
                    int endIdx = queryIndex + input.length();
                    int maxEndIdx = this.mSpannable.toString().length();
                    if (endIdx > maxEndIdx) {
                        endIdx = maxEndIdx;
                    }
                    this.mSpannable.setSpan(new ForegroundColorSpan(this.mPrefixHighligher.getHighlightColor()), queryIndex, endIdx, 33);
                    setMarqueeText(aNameView, this.mSpannable);
                    this.mTextHighlighted = true;
                } else {
                    startIndex = SearchMatch.filterAndMatchName(item.mName, input.toLowerCase(Locale.getDefault()));
                    if (startIndex.length != 0) {
                        for (i = 0; i < startIndex.length / 2; i++) {
                            begin = startIndex[i * 2];
                            end = startIndex[(i * 2) + 1];
                            if (begin >= 0 && end >= begin && end < item.mName.length()) {
                                if (item.equals(aNameView.getTag())) {
                                    this.mSpannable.setSpan(new ForegroundColorSpan(this.mPrefixHighligher.getHighlightColor()), begin, end + 1, 33);
                                    setMarqueeText(aNameView, this.mSpannable);
                                    this.mTextHighlighted = true;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void highlightChineseItem(int aStart, int aEnd, TextView aNameView, String aKey, ArrayList<Token> aTokens, MmsMatchContact item) {
        Integer[] wordsIndexes = this.mDialerHighlighter.findIndexForWords(aKey, new StringBuffer());
        if (wordsIndexes.length != 0) {
            int keyLength = aKey.length();
            String displayName = this.mSpannable.toString();
            int length = displayName.length();
            if (wordsIndexes.length <= length && wordsIndexes.length <= aTokens.size()) {
                int index = 0;
                int tempStart = 0;
                int charsToHighlight = (aEnd - aStart) + 1;
                int offset = 0;
                while (index < wordsIndexes.length) {
                    if (wordsIndexes[index].intValue() == aStart) {
                        tempStart = index + offset;
                        break;
                    }
                    if (((Token) aTokens.get(index)).type == 1 || ((Token) aTokens.get(index)).type == 4) {
                        offset += ((Token) aTokens.get(index)).source.length() - 1;
                    }
                    index++;
                    while (index + offset < length && (displayName.charAt(index + offset) == ' ' || displayName.charAt(index + offset) == '.')) {
                        offset++;
                    }
                }
                int tempEnd = tempStart;
                int i = tempStart;
                while (charsToHighlight > 0 && i < length && index < wordsIndexes.length) {
                    int tokenType = ((Token) aTokens.get(index)).type;
                    char c = displayName.charAt(i);
                    if (c == ' ' || c == '.') {
                        tempEnd++;
                    } else if (tokenType == 1) {
                        int sourceL = ((Token) aTokens.get(index)).source.length();
                        if (charsToHighlight > sourceL) {
                            charsToHighlight -= sourceL + 1;
                            tempEnd += sourceL;
                        } else {
                            tempEnd += charsToHighlight;
                            charsToHighlight = 0;
                        }
                        index++;
                        i += sourceL - 1;
                    } else if (tokenType == 2 || tokenType == 3) {
                        int intValue;
                        if (index < wordsIndexes.length - 1) {
                            intValue = wordsIndexes[index + 1].intValue() - wordsIndexes[index].intValue();
                        } else {
                            intValue = keyLength - wordsIndexes[index].intValue();
                        }
                        charsToHighlight -= intValue;
                        index++;
                        tempEnd++;
                    } else {
                        tempEnd++;
                    }
                    i++;
                }
                if (tempStart > -1 && tempEnd <= length && tempStart < tempEnd && item.equals(aNameView.getTag())) {
                    this.mSpannable.setSpan(new ForegroundColorSpan(this.mPrefixHighligher.getHighlightColor()), tempStart, tempEnd, 33);
                    setMarqueeText(aNameView, this.mSpannable);
                    this.mTextHighlighted = true;
                }
            }
        }
    }

    private void setMarqueeText(TextView textView, CharSequence text) {
        if (getTextEllipsis(0) == TruncateAt.END) {
            SpannableString spannable = new SpannableString(text);
            spannable.setSpan(TruncateAt.END, 0, spannable.length(), 33);
            textView.setText(spannable);
            return;
        }
        textView.setText(text);
    }

    private TruncateAt getTextEllipsis(int textEllipsisFlag) {
        switch (textEllipsisFlag) {
            case 0:
                return TruncateAt.END;
            case 1:
                return TruncateAt.START;
            default:
                return TruncateAt.END;
        }
    }

    private void setNumberTextAndHighter(String text, TextView view) {
        this.mPrefixHighligher.setText(view, text, this.mHighlightedPrefix);
    }

    private void setHighlightedPrefix(String searchKey) {
        if (TextUtils.isEmpty(searchKey)) {
            this.mHighlightedPrefix = new char[0];
        } else {
            this.mHighlightedPrefix = HighLightMatchUtils.getLowerCaseQueryString(searchKey);
        }
    }
}
