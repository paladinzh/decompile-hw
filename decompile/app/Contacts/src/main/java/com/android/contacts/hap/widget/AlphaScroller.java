package com.android.contacts.hap.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.FastScroller;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.list.ContactEntryListFragment.OnOverLayActionListener;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.widget.PinnedHeaderListView;
import com.google.android.gms.R;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AlphaScroller extends FastScroller {
    private String[] ALPHABETS;
    private String BULLET_CHAR = "•";
    private String[] EN_ALPHABETS;
    private String[] FULL_ALPHABETS;
    private int action = -1;
    private boolean isAnimationFinished;
    private boolean isUpToDown;
    private int mActiveTextColor;
    private int mAlphaScrollerMarginStart;
    private int mAlphaScrollerWidth;
    private float mAlphaTextSize = 0.0f;
    private float mAlphaTextSizeScale = 1.0f;
    private List<String> mAlphabet = new ArrayList();
    private int mBottomGap;
    private Context mContext;
    private boolean mDisableAlphaScroller;
    private int mDrawNum = 0;
    private int mFirstNativeIndex = -1;
    private float mGapBetweenAlpha;
    private boolean mHasNativeIndexer;
    private int mInactiveTextColor;
    private boolean mIncludeStar = true;
    private float mInitThumbY = 0.0f;
    private int mInvalidTextColor;
    private boolean mIsNativeIndexerShown;
    private boolean mIsRtl;
    private AbsListView mList;
    private BaseAdapter mListAdapter;
    private int mListOffset;
    private float mMinAlphaTextSize;
    private int mNormalBottomGap;
    private OnOverLayActionListener mOverLayListener;
    private RectF mOverlayPos;
    private int mOverlaySize;
    private Paint mPaint;
    private int mPixelShift;
    private int mScaledTouchSlop;
    private SectionIndexer mSectionIndexer;
    private int[] mSectionPosition;
    private String mSectionText;
    private Object[] mSections;
    private int mSelectedTextColor;
    private long mStartTime;
    private int mState;
    private int mThemeColor;
    private Drawable mThumbDrawable;
    private int mTopGap = 0;
    private int mX;
    private final float normalDensity = 2.0f;
    private Typeface sRobotoMediumFont;
    private Typeface sRobotoRegularFont;
    private int touchIndex = 0;

    public void initTextSize(android.content.Context r7, android.content.res.Resources r8) {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r6 = this;
        r3 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r5 = 2131558624; // 0x7f0d00e0 float:1.874257E38 double:1.053129888E-314;
        r4 = 0;
        r2 = r7.getResources();
        r2 = r2.getDisplayMetrics();
        r0 = r2.density;
        r2 = 0;
        r6.mMinAlphaTextSize = r2;	 Catch:{ NotFoundException -> 0x003e, all -> 0x0058 }
        r2 = (r0 > r3 ? 1 : (r0 == r3 ? 0 : -1));	 Catch:{ NotFoundException -> 0x003e, all -> 0x0058 }
        if (r2 <= 0) goto L_0x002f;	 Catch:{ NotFoundException -> 0x003e, all -> 0x0058 }
    L_0x0017:
        r2 = 2131559404; // 0x7f0d03ec float:1.8744151E38 double:1.0531302736E-314;	 Catch:{ NotFoundException -> 0x003e, all -> 0x0058 }
        r2 = r8.getDimensionPixelSize(r2);	 Catch:{ NotFoundException -> 0x003e, all -> 0x0058 }
        r2 = (float) r2;	 Catch:{ NotFoundException -> 0x003e, all -> 0x0058 }
        r6.mMinAlphaTextSize = r2;	 Catch:{ NotFoundException -> 0x003e, all -> 0x0058 }
    L_0x0021:
        r2 = r6.mMinAlphaTextSize;
        r2 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1));
        if (r2 != 0) goto L_0x002e;
    L_0x0027:
        r2 = r8.getDimensionPixelSize(r5);
        r2 = (float) r2;
        r6.mMinAlphaTextSize = r2;
    L_0x002e:
        return;
    L_0x002f:
        r2 = (r0 > r3 ? 1 : (r0 == r3 ? 0 : -1));
        if (r2 != 0) goto L_0x004d;
    L_0x0033:
        r2 = 2131559405; // 0x7f0d03ed float:1.8744153E38 double:1.053130274E-314;
        r2 = r8.getDimensionPixelSize(r2);	 Catch:{ NotFoundException -> 0x003e, all -> 0x0058 }
        r2 = (float) r2;	 Catch:{ NotFoundException -> 0x003e, all -> 0x0058 }
        r6.mMinAlphaTextSize = r2;	 Catch:{ NotFoundException -> 0x003e, all -> 0x0058 }
        goto L_0x0021;
    L_0x003e:
        r1 = move-exception;
        r2 = r6.mMinAlphaTextSize;
        r2 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1));
        if (r2 != 0) goto L_0x002e;
    L_0x0045:
        r2 = r8.getDimensionPixelSize(r5);
        r2 = (float) r2;
        r6.mMinAlphaTextSize = r2;
        goto L_0x002e;
    L_0x004d:
        r2 = 2131559406; // 0x7f0d03ee float:1.8744155E38 double:1.0531302746E-314;
        r2 = r8.getDimensionPixelSize(r2);	 Catch:{ NotFoundException -> 0x003e, all -> 0x0058 }
        r2 = (float) r2;	 Catch:{ NotFoundException -> 0x003e, all -> 0x0058 }
        r6.mMinAlphaTextSize = r2;	 Catch:{ NotFoundException -> 0x003e, all -> 0x0058 }
        goto L_0x0021;
    L_0x0058:
        r2 = move-exception;
        r3 = r6.mMinAlphaTextSize;
        r3 = (r3 > r4 ? 1 : (r3 == r4 ? 0 : -1));
        if (r3 != 0) goto L_0x0066;
    L_0x005f:
        r3 = r8.getDimensionPixelSize(r5);
        r3 = (float) r3;
        r6.mMinAlphaTextSize = r3;
    L_0x0066:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.widget.AlphaScroller.initTextSize(android.content.Context, android.content.res.Resources):void");
    }

    public void setIncludeStar(boolean mIncludeStar) {
        this.mIncludeStar = mIncludeStar;
        this.mFirstNativeIndex = mIncludeStar ? 2 : 1;
    }

    public AlphaScroller(Context aContext, AbsListView aListView, boolean includeStar) {
        int i = 1;
        super(aListView, 0);
        this.mIncludeStar = includeStar;
        if (this.mIncludeStar) {
            i = 2;
        }
        this.mFirstNativeIndex = i;
        this.mList = aListView;
        init(aContext);
    }

    public String[] removeStarIndexer(String[] str) {
        String[] noStarStr = new String[(str.length - 1)];
        for (int i = 0; i < noStarStr.length; i++) {
            noStarStr[i] = str[i + 1];
        }
        return noStarStr;
    }

    public void init(Context aContext) {
        this.mContext = aContext;
        if (this.mContext != null) {
            int dpi = this.mContext.getResources().getInteger(R.integer.density_size);
            if (dpi != 0) {
                this.mAlphaTextSizeScale = ((float) this.mContext.getResources().getDisplayMetrics().densityDpi) / ((float) dpi);
            }
            this.mState = 0;
            Resources res = this.mContext.getResources();
            this.mIsRtl = CommonUtilMethods.isLayoutRTL();
            this.mHasNativeIndexer = res.getBoolean(R.bool.has_native_indexer);
            if (this.mHasNativeIndexer) {
                String[] tempAlphaArr = res.getStringArray(R.array.alphabet_indexer);
                if (!this.mIncludeStar) {
                    tempAlphaArr = removeStarIndexer(tempAlphaArr);
                }
                this.ALPHABETS = new String[(tempAlphaArr.length + 1)];
                System.arraycopy(tempAlphaArr, 0, this.ALPHABETS, 0, tempAlphaArr.length);
                int firstNativePos = 1;
                for (int i = 0; i < this.ALPHABETS.length; i++) {
                    if ("#".equals(this.ALPHABETS[i])) {
                        firstNativePos = i;
                        break;
                    }
                }
                if (this.mIncludeStar) {
                    this.EN_ALPHABETS = new String[]{"☆", "#", this.ALPHABETS[firstNativePos + 1], "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
                } else {
                    this.EN_ALPHABETS = new String[]{"#", this.ALPHABETS[firstNativePos + 1], "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
                }
                this.ALPHABETS[this.ALPHABETS.length - 1] = this.EN_ALPHABETS[this.EN_ALPHABETS.length - 1];
                String[] tempFullAlphaArr = res.getStringArray(R.array.full_alphabetic_indexer);
                if (!this.mIncludeStar) {
                    tempFullAlphaArr = removeStarIndexer(tempFullAlphaArr);
                }
                if (tempFullAlphaArr[tempFullAlphaArr.length / 2].equals(this.EN_ALPHABETS[(this.EN_ALPHABETS.length / 2) + 1])) {
                    this.FULL_ALPHABETS = new String[(tempAlphaArr.length + 1)];
                    System.arraycopy(tempAlphaArr, 0, this.FULL_ALPHABETS, 0, tempAlphaArr.length);
                } else {
                    this.FULL_ALPHABETS = new String[(tempFullAlphaArr.length + 1)];
                    System.arraycopy(tempFullAlphaArr, 0, this.FULL_ALPHABETS, 0, tempFullAlphaArr.length);
                }
                this.FULL_ALPHABETS[this.FULL_ALPHABETS.length - 1] = this.EN_ALPHABETS[this.EN_ALPHABETS.length - 1];
            } else {
                this.ALPHABETS = res.getStringArray(R.array.alphabet_indexer);
                this.FULL_ALPHABETS = res.getStringArray(R.array.full_alphabetic_indexer);
                if (!this.mIncludeStar) {
                    this.ALPHABETS = removeStarIndexer(this.ALPHABETS);
                    this.FULL_ALPHABETS = removeStarIndexer(this.FULL_ALPHABETS);
                }
            }
            if (!this.mHasNativeIndexer || isInNativeSection()) {
                this.mAlphabet = new ArrayList(Arrays.asList(this.ALPHABETS));
            } else {
                this.mAlphabet = new ArrayList(Arrays.asList(this.EN_ALPHABETS));
            }
            this.mBottomGap = res.getDimensionPixelSize(R.dimen.alphaScroller_listview_bottom_gap);
            if ((this.mContext instanceof Activity) && ((Activity) this.mContext).isInMultiWindowMode()) {
                this.mNormalBottomGap = res.getDimensionPixelSize(R.dimen.alphaScroller_listview_normal_multiwindonbottom_gap);
            } else {
                this.mNormalBottomGap = res.getDimensionPixelSize(R.dimen.alphaScroller_listview_normal_bottom_gap);
            }
            this.mAlphaScrollerWidth = res.getDimensionPixelSize(R.dimen.alphaScroller_listview_width);
            this.mOverlaySize = res.getDimensionPixelSize(R.dimen.fastscroll_overlay_size);
            initTextSize(this.mContext, res);
            this.mPixelShift = res.getDimensionPixelSize(R.dimen.alphaScroller_pixel_shift);
            this.mAlphaScrollerMarginStart = res.getDimensionPixelSize(R.dimen.alphaScroller_margin_start);
            useThumbDrawable(res.getDrawable(R.drawable.dial_num_9_blk));
            this.mOverlayPos = new RectF();
            this.mPaint = new Paint();
            this.mPaint.setAntiAlias(true);
            this.mPaint.setTextAlign(Align.CENTER);
            this.mPaint.setTextSize(this.mMinAlphaTextSize);
            this.mPaint.setAlpha(0);
            this.mPaint.setStyle(Style.FILL_AND_STROKE);
            handleOverLayPosition(this.mList.getWidth(), this.mList.getHeight());
            this.mSectionText = (String) this.mAlphabet.get(0);
            this.mScaledTouchSlop = ViewConfiguration.get(this.mContext).getScaledTouchSlop();
            int color = ImmersionUtils.getControlColor(this.mContext.getResources());
            if (color != 0) {
                this.mThemeColor = color;
            } else {
                this.mThemeColor = this.mContext.getResources().getColor(R.color.people_app_theme_color);
            }
            this.mActiveTextColor = this.mContext.getResources().getColor(R.color.alpha_scroller_inactive_text_selected);
            this.mInactiveTextColor = this.mContext.getResources().getColor(R.color.alpha_scroller_inactive_text);
            this.mSelectedTextColor = this.mContext.getResources().getColor(R.color.alpha_scroller_text_selected_color);
            this.mInvalidTextColor = this.mContext.getResources().getColor(R.color.alpha_scroller_invalid_text_selected_color);
            this.sRobotoRegularFont = Typeface.createFromFile("/system/fonts/Roboto-Regular.ttf");
            this.sRobotoMediumFont = Typeface.createFromFile("/system/fonts/Roboto-Medium.ttf");
        }
    }

    private boolean isInNativeSection(String sectionName) {
        if (Collator.getInstance().compare(sectionName, "A") < 0) {
            return true;
        }
        return false;
    }

    private boolean isInNativeSection() {
        if (this.mSectionText == null || this.mSections == null || this.mSections.length <= 0) {
            return false;
        }
        String strDetect = this.mSectionText;
        if ("☆".equals(strDetect) || "#".equals(strDetect)) {
            if (1 != this.mSections.length) {
                int i = 0;
                while (i + 1 < this.mSections.length) {
                    i++;
                    strDetect = this.mSections[i];
                    if (!"#".equals(strDetect)) {
                        break;
                    }
                }
            }
            return false;
        }
        return isInNativeSection(strDetect);
    }

    private void calculateVariables(boolean indexerChanged) {
        int bottom;
        int viewWidth = this.mList.getWidth();
        if (this.mAlphabet.size() < 15) {
            bottom = this.mBottomGap;
            this.mTopGap = bottom;
        } else {
            bottom = this.mNormalBottomGap;
            this.mTopGap = bottom;
        }
        int viewHeight = (this.mList.getHeight() - bottom) - this.mTopGap;
        if (this.mAlphaTextSize == 0.0f || indexerChanged) {
            float temp = ((float) viewHeight) / ((float) this.mAlphabet.size());
            this.mAlphaTextSize = this.mMinAlphaTextSize;
            this.mGapBetweenAlpha = temp - this.mAlphaTextSize;
        }
        int left = viewWidth - this.mAlphaScrollerWidth;
        this.mThumbDrawable.setAlpha(0);
        this.mThumbDrawable.setBounds(left, 0, viewWidth, viewHeight);
        if (this.mIsRtl) {
            this.mX = this.mAlphaScrollerMarginStart;
        } else {
            this.mX = this.mAlphaScrollerMarginStart + left;
        }
    }

    public void draw(Canvas canvas) {
        int i;
        calculateVariables(false);
        this.mThumbDrawable.draw(canvas);
        Paint paint = this.mPaint;
        paint.setTextSize(this.mAlphaTextSize * this.mAlphaTextSizeScale);
        if (1 == this.mState) {
            paint.setColor(this.mActiveTextColor);
        } else {
            paint.setColor(this.mInactiveTextColor);
        }
        paint.setTypeface(this.sRobotoRegularFont);
        int length = this.mAlphabet.size();
        for (i = 0; i < length; i++) {
            float top;
            if ("☆".equals(this.mAlphabet.get(i))) {
                top = (((float) i) * (this.mAlphaTextSize + this.mGapBetweenAlpha)) + ((float) this.mTopGap);
                drawStar(canvas, (int) (((float) this.mX) - (this.mAlphaTextSize / 2.0f)), (int) (this.mGapBetweenAlpha + top), (int) (((float) this.mX) + (this.mAlphaTextSize / 2.0f)), (int) ((this.mGapBetweenAlpha + top) + this.mAlphaTextSize), false);
            } else {
                top = (((float) (i + 1)) * (this.mAlphaTextSize + this.mGapBetweenAlpha)) + ((float) this.mTopGap);
                if (includeIndexer((String) this.mAlphabet.get(i), i) && (1 == this.mState || 2 == this.mState)) {
                    paint.setColor(this.mActiveTextColor);
                } else {
                    paint.setColor(this.mInvalidTextColor);
                }
                canvas.drawText(((String) this.mAlphabet.get(i)).replace("劃", ""), (float) this.mX, top - ((float) this.mPixelShift), paint);
            }
        }
        updateIndexerState();
        this.isAnimationFinished = false;
        int equalPos = -1;
        for (i = 0; i < length; i++) {
            if (equalsChar((String) this.mAlphabet.get(i), this.mSectionText, i) && includeIndexer(this.mSectionText, this.touchIndex)) {
                equalPos = i;
                break;
            }
        }
        if (equalPos != -1) {
            paint.setColor(this.mThemeColor);
            if ("☆".equals(this.mAlphabet.get(equalPos))) {
                top = (((float) equalPos) * (this.mAlphaTextSize + this.mGapBetweenAlpha)) + ((float) this.mTopGap);
                drawStar(canvas, (int) (((float) this.mX) - (this.mAlphaTextSize / 2.0f)), (int) (this.mGapBetweenAlpha + top), (int) (((float) this.mX) + (this.mAlphaTextSize / 2.0f)), (int) ((this.mGapBetweenAlpha + top) + this.mAlphaTextSize), true);
                return;
            }
            top = (((float) (equalPos + 1)) * (this.mAlphaTextSize + this.mGapBetweenAlpha)) + ((float) this.mTopGap);
            paint.setColor(this.mSelectedTextColor);
            paint.setTypeface(this.sRobotoMediumFont);
            canvas.drawText(((String) this.mAlphabet.get(equalPos)).replace("劃", ""), (float) this.mX, top - ((float) this.mPixelShift), paint);
        }
    }

    private void updateIndexerState() {
        if (!this.mHasNativeIndexer) {
            return;
        }
        if (((String) this.mAlphabet.get(this.mAlphabet.size() / 2)).equals(this.ALPHABETS[this.ALPHABETS.length / 2])) {
            this.mIsNativeIndexerShown = true;
        } else {
            this.mIsNativeIndexerShown = false;
        }
    }

    public boolean needAnimation() {
        return this.mStartTime != 0;
    }

    public boolean isAnimationFinished() {
        return this.isAnimationFinished;
    }

    public void drawTextWithAnimation(Canvas canvas) {
        int i;
        float top;
        calculateVariables(false);
        Paint paint = this.mPaint;
        paint.setColor(this.mContext.getResources().getColor(R.color.alpha_scroller_inactive_text));
        paint.setTextSize(this.mAlphaTextSize);
        if (this.mDrawNum <= this.mAlphabet.size()) {
            if (this.isUpToDown) {
                for (i = 0; i < this.mDrawNum; i++) {
                    if ("☆".equals(this.mAlphabet.get(i))) {
                        top = (((float) i) * (this.mAlphaTextSize + this.mGapBetweenAlpha)) + ((float) this.mTopGap);
                        drawStar(canvas, (int) (((float) this.mX) - (this.mAlphaTextSize / 2.0f)), (int) (this.mGapBetweenAlpha + top), (int) (((float) this.mX) + (this.mAlphaTextSize / 2.0f)), (int) ((this.mGapBetweenAlpha + top) + this.mAlphaTextSize), false);
                    } else {
                        canvas.drawText(((String) this.mAlphabet.get(i)).replace("劃", ""), (float) this.mX, ((((float) (i + 1)) * (this.mAlphaTextSize + this.mGapBetweenAlpha)) + ((float) this.mTopGap)) - ((float) this.mPixelShift), paint);
                    }
                }
            } else {
                int len = this.mAlphabet.size() - 1;
                for (i = len; i >= (len + 1) - this.mDrawNum; i--) {
                    if ("☆".equals(this.mAlphabet.get(i))) {
                        top = (((float) i) * (this.mAlphaTextSize + this.mGapBetweenAlpha)) + ((float) this.mTopGap);
                        drawStar(canvas, (int) (((float) this.mX) - (this.mAlphaTextSize / 2.0f)), (int) (this.mGapBetweenAlpha + top), (int) (((float) this.mX) + (this.mAlphaTextSize / 2.0f)), (int) ((this.mGapBetweenAlpha + top) + this.mAlphaTextSize), false);
                    } else {
                        canvas.drawText(((String) this.mAlphabet.get(i)).replace("劃", ""), (float) this.mX, ((((float) (i + 1)) * (this.mAlphaTextSize + this.mGapBetweenAlpha)) + ((float) this.mTopGap)) - ((float) this.mPixelShift), paint);
                    }
                }
            }
        }
        updateIndexerState();
        if (this.mDrawNum >= this.mAlphabet.size()) {
            int equalPos = -1;
            int length = this.mAlphabet.size();
            for (i = 0; i < length; i++) {
                if (equalsChar((String) this.mAlphabet.get(i), this.mSectionText, i) && includeIndexer(this.mSectionText, this.touchIndex)) {
                    equalPos = i;
                    break;
                }
            }
            if (equalPos != -1) {
                paint.setColor(this.mContext.getResources().getColor(R.color.people_app_theme_color));
                if ("☆".equals(this.mAlphabet.get(equalPos))) {
                    top = (((float) equalPos) * (this.mAlphaTextSize + this.mGapBetweenAlpha)) + ((float) this.mTopGap);
                    drawStar(canvas, (int) (((float) this.mX) - (this.mAlphaTextSize / 2.0f)), (int) (this.mGapBetweenAlpha + top), (int) (((float) this.mX) + (this.mAlphaTextSize / 2.0f)), (int) ((this.mGapBetweenAlpha + top) + this.mAlphaTextSize), true);
                } else {
                    canvas.drawText(((String) this.mAlphabet.get(equalPos)).replace("劃", ""), (float) this.mX, ((((float) (equalPos + 1)) * (this.mAlphaTextSize + this.mGapBetweenAlpha)) + ((float) this.mTopGap)) - ((float) this.mPixelShift), paint);
                }
            }
            this.mStartTime = 0;
            this.mDrawNum = 0;
            return;
        }
        this.mDrawNum++;
        this.isAnimationFinished = false;
        if (this.mDrawNum == this.mAlphabet.size()) {
            this.isAnimationFinished = true;
        }
    }

    private void drawStar(Canvas canvas, int left, int top, int right, int bottom, boolean isHighlight) {
        Drawable drawable;
        if (isHighlight) {
            drawable = this.mContext.getResources().getDrawable(R.drawable.ic_star_highlight);
        } else {
            drawable = this.mContext.getResources().getDrawable(R.drawable.ic_star_normal);
        }
        drawable.setBounds(left, top, right, bottom);
        drawable.draw(canvas);
    }

    private void scrollTo(int itemPos) {
        if (this.mSectionIndexer != null && this.mSectionPosition != null) {
            Object[] sections = this.mSections;
            int sectionIndex = this.touchIndex;
            if (sectionIndex >= this.mAlphabet.size() || sectionIndex < 0) {
                if (this.action == 1) {
                    this.mList.invalidate();
                }
            } else if (includeIndexer((String) this.mAlphabet.get(sectionIndex), this.touchIndex)) {
                if (sections != null && sections.length > 0 && sectionIndex < this.mAlphabet.size()) {
                    int index = this.mSectionIndexer.getPositionForSection(this.mSectionPosition[sectionIndex]);
                    if (index == -1) {
                        index += this.mList.getCount();
                    }
                    int lPositionToScroll = index + this.mListOffset;
                    if (sectionIndex == 0) {
                        lPositionToScroll = 0;
                    }
                    if (((String) this.mAlphabet.get(sectionIndex)).equals((String) this.mSections[0])) {
                        lPositionToScroll = 0;
                    }
                    if (this.mList instanceof ExpandableListView) {
                        ExpandableListView expList = this.mList;
                        expList.setSelectionFromTop(expList.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(lPositionToScroll)), 0);
                    } else if (this.mList instanceof ListView) {
                        ((ListView) this.mList).setSelectionFromTop(lPositionToScroll, 0);
                    } else {
                        this.mList.setSelection(lPositionToScroll);
                    }
                    updateIndexer();
                }
                if (sectionIndex >= 0 && sectionIndex < this.mAlphabet.size()) {
                    String mTemp = (String) this.mAlphabet.get(sectionIndex);
                    if (mTemp.equals(this.BULLET_CHAR)) {
                        this.mSectionText = getValidBullet(sectionIndex);
                    } else {
                        this.mSectionText = mTemp;
                    }
                    String str = this.mSectionText;
                }
                if (sections != null && sections.length <= 1) {
                    this.mList.invalidate();
                }
                if (includeIndexer(this.mSectionText, this.touchIndex) && this.mOverLayListener != null) {
                    this.mOverLayListener.onStateChange(itemPos, this.mSectionText, true);
                }
            } else {
                if (this.action == 1) {
                    this.mList.invalidate();
                }
            }
        }
    }

    public void setOverLayIndexerListener(OnOverLayActionListener listener) {
        this.mOverLayListener = listener;
    }

    public void setOverLayIndexer(int index) {
        if (this.mList != null) {
            getSectionsFromIndexer();
        }
        if (this.mSectionIndexer != null && this.mSections != null) {
            int i = this.mSectionIndexer.getSectionForPosition(index);
            String mTemp = this.mSections[i];
            if (mTemp.equals(this.BULLET_CHAR)) {
                this.mSectionText = getValidBullet(i);
            } else {
                this.mSectionText = mTemp;
            }
            updateIndexer();
        }
    }

    private void updateIndexer() {
        if (!this.mHasNativeIndexer) {
            return;
        }
        if (isInNativeSection()) {
            if (!this.mIsNativeIndexerShown) {
                toggleToNativeIndexer(false);
            }
        } else if (this.mIsNativeIndexerShown) {
            toggleToNativeIndexer(true);
        }
    }

    public boolean includeIndexer(String sectionTxt) {
        if (this.mSections != null) {
            Collator coll = Collator.getInstance();
            coll.setStrength(0);
            for (String section : this.mSections) {
                if (coll.equals(section, sectionTxt)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean includeIndexer(String sectionTxt, int index) {
        boolean z = false;
        if (sectionTxt == null || this.mSections == null) {
            return false;
        }
        if (!sectionTxt.equals(this.BULLET_CHAR)) {
            return includeIndexer(sectionTxt);
        }
        if (getValidBullet(index) != null) {
            z = true;
        }
        return z;
    }

    private String getValidBullet(int index) {
        if (this.mSections == null || index >= this.FULL_ALPHABETS.length) {
            return null;
        }
        String[] bullet_list = this.FULL_ALPHABETS[index].split(HwCustPreloadContacts.EMPTY_STRING);
        for (int i = 0; i < bullet_list.length; i++) {
            for (String section : this.mSections) {
                if (section.equals(bullet_list[i])) {
                    return bullet_list[i];
                }
            }
        }
        return null;
    }

    protected void populateIndexerArray(Object[] sections) {
        this.mSections = sections;
        this.mDisableAlphaScroller = false;
        if (sections.length <= 0) {
            this.mDisableAlphaScroller = true;
            return;
        }
        int i = 0;
        int j = 0;
        this.mSectionPosition = new int[this.mAlphabet.size()];
        if (sections.length <= 0) {
            this.mDisableAlphaScroller = true;
            return;
        }
        if (sections[0].equals("@")) {
            j = 1;
        }
        while (i < this.mAlphabet.size()) {
            if (j >= sections.length) {
                this.mSectionPosition[i] = j - 1;
            } else if (equalsChar((String) this.mAlphabet.get(i), sections[j].toString(), i)) {
                this.mSectionPosition[i] = j;
                int offset = 1;
                if (((String) this.mAlphabet.get(i)).equals(this.BULLET_CHAR) && j < sections.length - 1) {
                    while (j + offset < sections.length && this.FULL_ALPHABETS[i].contains(sections[j + offset].toString())) {
                        offset++;
                    }
                }
                j += offset;
            } else if (handleUnknownSection(sections[j].toString())) {
                j++;
                i--;
            } else {
                this.mSectionPosition[i] = j;
            }
            i++;
        }
    }

    private void getSectionsFromIndexer() {
        Adapter adapter = this.mList.getAdapter();
        this.mSectionIndexer = null;
        if (adapter instanceof HeaderViewListAdapter) {
            this.mListOffset = ((HeaderViewListAdapter) adapter).getHeadersCount();
            adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }
        if (adapter instanceof SectionIndexer) {
            this.mListAdapter = (BaseAdapter) adapter;
            this.mSectionIndexer = (SectionIndexer) adapter;
            this.mSections = this.mSectionIndexer.getSections();
            return;
        }
        this.mListAdapter = (BaseAdapter) adapter;
        this.mSections = new String[]{HwCustPreloadContacts.EMPTY_STRING};
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0) {
            if (isPointInside(ev.getX(), ev.getY())) {
                setState(2);
                this.mInitThumbY = ev.getY();
                return true;
            }
            this.mInitThumbY = 0.0f;
        } else if (ev.getAction() == 2) {
            if (isPointInside(ev.getX(), ev.getY())) {
                return true;
            }
        } else if (ev.getAction() == 1 && this.mState == 2) {
            onTouchEvent(ev);
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent me) {
        if (this.mDisableAlphaScroller) {
            setState(0);
            return false;
        } else if (this.mList == null) {
            return false;
        } else {
            this.action = me.getAction();
            int itemPos = (int) (((me.getY() - ((float) this.mTopGap)) / ((float) (this.mList.getHeight() - (this.mTopGap * 2)))) * ((float) this.mAlphabet.size()));
            this.touchIndex = itemPos;
            if (this.action == 0) {
                if (isPointInside(me.getX(), me.getY())) {
                    setState(2);
                    if (this.mListAdapter == null) {
                        getSectionsFromIndexer();
                    }
                    if (evaluate()) {
                        return true;
                    }
                    scrollTo(itemPos);
                    return true;
                }
            } else if (this.action == 1) {
                if (this.mState == 1 || this.mState == 2) {
                    StatisticalHelper.report(1152);
                    scrollTo(itemPos);
                    onActionUp();
                    return true;
                }
            } else if (this.action == 3) {
                if (this.mState == 2) {
                    onActionUp();
                    return true;
                }
            } else if (this.action == 2) {
                if (this.mState == 1) {
                    if (evaluate()) {
                        return true;
                    }
                    scrollTo(itemPos);
                    return true;
                } else if (this.mState == 2 && Math.abs(me.getY() - this.mInitThumbY) > ((float) this.mScaledTouchSlop)) {
                    setState(1);
                    if (this.mListAdapter == null) {
                        getSectionsFromIndexer();
                    }
                    this.mList.requestDisallowInterceptTouchEvent(true);
                    if (evaluate()) {
                        return true;
                    }
                    scrollTo(itemPos);
                    cancelFling();
                    return true;
                }
            }
            return false;
        }
    }

    private boolean evaluate() {
        if (this.touchIndex != this.mFirstNativeIndex || this.mIsNativeIndexerShown) {
            if (this.touchIndex == this.mAlphabet.size() - 1 && this.mIsNativeIndexerShown && this.mList != null && !includeIndexer((String) this.mAlphabet.get(this.touchIndex), this.touchIndex)) {
                this.mList.setSelection(this.mList.getCount() - 1);
                return true;
            }
        } else if (!(this.mList == null || includeIndexer((String) this.mAlphabet.get(this.touchIndex), this.touchIndex))) {
            this.mList.setSelection(0);
            return true;
        }
        return false;
    }

    private void toggleToNativeIndexer(boolean isNative) {
        List arrayList;
        boolean z;
        this.mAlphabet.clear();
        if (isNative) {
            arrayList = new ArrayList(Arrays.asList(this.EN_ALPHABETS));
        } else {
            arrayList = new ArrayList(Arrays.asList(this.ALPHABETS));
        }
        this.mAlphabet = arrayList;
        if (isNative) {
            z = false;
        } else {
            z = true;
        }
        this.isUpToDown = z;
        this.isAnimationFinished = false;
        populateIndexerArray(this.mSections);
        calculateVariables(true);
        this.mStartTime = System.currentTimeMillis();
    }

    private void onActionUp() {
        if (this.mList != null) {
            this.mList.requestDisallowInterceptTouchEvent(false);
            if (this.mList instanceof PinnedHeaderListView) {
                this.mList.mIsHeaderScroll = false;
            }
        }
        setState(0);
        if (this.mOverLayListener != null) {
            this.mOverLayListener.onStateChange(-1, this.mSectionText, false);
        }
    }

    boolean isPointInside(float x, float y) {
        boolean z = true;
        if (this.mIsRtl) {
            if (x >= ((float) this.mAlphaScrollerWidth)) {
                z = false;
            }
            return z;
        }
        if (x <= ((float) (this.mList.getWidth() - this.mAlphaScrollerWidth))) {
            z = false;
        }
        return z;
    }

    private void cancelFling() {
        MotionEvent cancelFling = MotionEvent.obtain(0, 0, 3, 0.0f, 0.0f, 0);
        this.mList.onTouchEvent(cancelFling);
        cancelFling.recycle();
    }

    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (h > oldh / 2) {
            this.mAlphaTextSize = 0.0f;
        }
        if (this.mOverlayPos != null) {
            handleOverLayPosition(w, h);
        }
    }

    private void handleOverLayPosition(int width, int height) {
        if (this.mThumbDrawable != null) {
            this.mThumbDrawable.setBounds(width - this.mAlphaScrollerWidth, 0, width, height);
        }
        RectF pos = this.mOverlayPos;
        pos.left = ((float) (width - this.mOverlaySize)) / 2.0f;
        pos.right = pos.left + ((float) this.mOverlaySize);
        pos.top = ((float) height) / 10.0f;
        pos.bottom = pos.top + ((float) this.mOverlaySize);
    }

    private void useThumbDrawable(Drawable drawable) {
        this.mThumbDrawable = drawable;
    }

    public void setState(int aState) {
        this.mState = aState;
    }

    private boolean equalsChar(String a, String b) {
        boolean z = true;
        if (a.length() != b.length()) {
            return false;
        }
        Collator coll = Collator.getInstance();
        coll.setStrength(0);
        if (coll.equals(a, b)) {
            return true;
        }
        int asciiA = a.charAt(0);
        int asciiB = b.charAt(0);
        if (asciiB < 65313 || asciiB > 65538) {
            return false;
        }
        if (asciiA != (asciiB - 65313) + 65) {
            z = false;
        }
        return z;
    }

    private boolean equalsChar(String a, String b, int i) {
        if (!a.equals(this.BULLET_CHAR)) {
            return equalsChar(a, b);
        }
        String[] bullet_list = this.FULL_ALPHABETS[i].split(HwCustPreloadContacts.EMPTY_STRING);
        for (String equals : bullet_list) {
            if (equals.equals(b)) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean handleUnknownSection(String aSection) {
        if (aSection.equals(HwCustPreloadContacts.EMPTY_STRING) || aSection.equals("@") || !isKnownAlpha(aSection)) {
            return true;
        }
        return false;
    }

    public boolean isKnownAlpha(String text) {
        List<String> actualList = new ArrayList();
        for (int i = 0; i < this.mAlphabet.size(); i++) {
            if (((String) this.mAlphabet.get(i)).equals(this.BULLET_CHAR)) {
                actualList.addAll(Arrays.asList(this.FULL_ALPHABETS[i].split(HwCustPreloadContacts.EMPTY_STRING)));
            } else {
                actualList.add((String) this.mAlphabet.get(i));
            }
        }
        return actualList.contains(text);
    }
}
