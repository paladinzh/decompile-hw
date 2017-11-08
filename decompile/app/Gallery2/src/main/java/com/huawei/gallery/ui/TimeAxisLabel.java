package com.huawei.gallery.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.format.DateUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.app.Config$LocalCameraAlbumPage;
import com.android.gallery3d.data.BitmapPool;
import com.android.gallery3d.data.TimeBucketPageViewMode;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.gallery.data.AbsGroupData;
import com.huawei.gallery.share.HwImageView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeAxisLabel {
    private static final /* synthetic */ int[] -com-android-gallery3d-data-TimeBucketPageViewModeSwitchesValues = null;
    protected Context mContext;
    BaseSpec mCurrentSpec;
    BaseSpec mDaySpec;
    protected boolean mIsLayoutRtl;
    protected boolean mIsTabletProduct;
    protected TimeBucketPageViewMode mMode;
    BaseSpec mMonthSpec;
    private SimpleDateFormat mSQLFormat = new SimpleDateFormat("yyyyMMdd");
    protected TitleSpec mTitleSpec;

    public abstract class BaseSpec {
        public BitmapPool bitmapPool;
        public int label_bottom_margin;
        public int label_height = 0;
        public int label_min_height;
        public int label_top_margin;
        public int label_width;
        private final LazyLoadedBitmap mAddrIcon;
        protected int mFirstTitleHeight;
        protected int mFirstTitleWidth = 0;
        protected int mGroupTitleHeight;
        protected TextPaint mGroupTitleTextPaint;
        protected TimeAxisLabel mOwner = TimeAxisLabel.this;
        protected TextPaint mTimeTextPaint;
        protected int mTitleGap;

        public abstract String[] setText(TitleArgs titleArgs, TitleEntrySetListener titleEntrySetListener);

        public BaseSpec(TitleSpec spec, int addrIconId) {
            this.label_top_margin = spec.time_line_top_magin;
            this.label_bottom_margin = spec.time_line_bottom_margin;
            this.mTimeTextPaint = spec.mTimeTextPaint;
            this.mFirstTitleHeight = spec.mTimeTextHeight;
            this.mTitleGap = spec.time_line_title_gap;
            this.mGroupTitleTextPaint = spec.mGroupTitleTextPaint;
            this.mGroupTitleHeight = spec.mGroupTitleTextHeight;
            this.mAddrIcon = new LazyLoadedBitmap(addrIconId);
            this.label_width = spec.label_width;
            this.label_min_height = spec.label_min_height;
            this.label_height = Math.max(this.label_height, this.label_min_height);
            this.bitmapPool = new BitmapPool(this.label_width, this.label_height, 8);
        }

        public int drawFirstTitle(Canvas canvas, String text, TitleSpec s, boolean isNeedDrawAddress) {
            int x;
            int y;
            if (this.mOwner.mIsLayoutRtl) {
                x = ((this.label_width - s.time_line_width) - s.time_line_start_padding) - TimeAxisLabel.getTextWidth(text, this.label_width, this.mTimeTextPaint);
            } else {
                x = s.time_line_width + s.time_line_start_padding;
            }
            if (!isNeedDrawAddress || this.mOwner.mIsTabletProduct) {
                y = (this.label_height - this.label_bottom_margin) - this.mFirstTitleHeight;
            } else {
                y = this.label_top_margin;
            }
            this.mFirstTitleWidth = TimeAxisLabel.drawText(canvas, x, y, text, this.label_width - x, this.mTimeTextPaint);
            return this.mFirstTitleWidth;
        }

        public int drawGroupTitle(Canvas canvas, TitleArgs titleArgs, String title, TitleSpec s) {
            if (title == null) {
                return 0;
            }
            int x;
            int y;
            if (this.mOwner.mIsTabletProduct) {
                if (this.mOwner.mIsLayoutRtl) {
                    x = ((((this.label_width - s.time_line_width) - s.time_line_start_padding) - this.mFirstTitleWidth) - this.mTitleGap) - TimeAxisLabel.getTextWidth(title, this.label_width, this.mGroupTitleTextPaint);
                } else {
                    x = ((s.time_line_width + s.time_line_start_padding) + this.mFirstTitleWidth) + this.mTitleGap;
                }
                y = (this.label_height - this.label_bottom_margin) - this.mGroupTitleHeight;
            } else {
                if (this.mOwner.mIsLayoutRtl) {
                    x = ((this.label_width - s.time_line_width) - s.time_line_start_padding) - TimeAxisLabel.getTextWidth(title, this.label_width, this.mGroupTitleTextPaint);
                } else {
                    x = s.time_line_width + s.time_line_start_padding;
                }
                y = this.label_top_margin + this.mFirstTitleHeight;
            }
            return TimeAxisLabel.drawText(canvas, x, y, title, this.label_width - x, this.mGroupTitleTextPaint);
        }

        public int getGroupTitleHeight() {
            return this.mGroupTitleHeight;
        }

        public void drawArrow(Canvas canvas, int offset, TitleSpec s) {
            Bitmap arrow = this.mAddrIcon.get();
            if (arrow != null) {
                int x;
                int y;
                if (this.mOwner.mIsTabletProduct) {
                    if (this.mOwner.mIsLayoutRtl) {
                        x = (this.label_width - (((((s.time_line_width + s.time_line_start_padding) + this.mFirstTitleWidth) + this.mTitleGap) + offset) + 10)) - arrow.getWidth();
                    } else {
                        x = ((((s.time_line_width + s.time_line_start_padding) + this.mFirstTitleWidth) + this.mTitleGap) + offset) + 10;
                    }
                    y = (((this.label_height - this.label_bottom_margin) - (this.mGroupTitleHeight / 2)) - (arrow.getHeight() / 2)) - 2;
                } else {
                    if (this.mOwner.mIsLayoutRtl) {
                        x = (this.label_width - (((s.time_line_width + s.time_line_start_padding) + offset) + 10)) - arrow.getWidth();
                    } else {
                        x = ((s.time_line_width + s.time_line_start_padding) + offset) + 10;
                    }
                    y = (((this.label_top_margin + this.mFirstTitleHeight) + (this.mGroupTitleHeight / 2)) - (arrow.getHeight() / 2)) - 2;
                }
                canvas.drawBitmap(arrow, (float) x, (float) y, null);
            }
        }

        public void drawTransparent(Canvas canvas, TitleSpec s) {
            if (this.label_height > this.label_min_height) {
                int x = TimeAxisLabel.this.mIsLayoutRtl ? 0 : s.time_line_width;
                int y = this.label_min_height;
                canvas.save();
                canvas.clipRect(x, y, TimeAxisLabel.this.mIsLayoutRtl ? this.label_width - s.time_line_width : this.label_width, this.label_height);
                canvas.drawColor(16777215, Mode.SRC);
                canvas.restore();
            }
        }

        protected boolean isNeedShowYear(Calendar labelCalendar, Calendar now, TitleArgs titleArgs, TitleEntrySetListener titleEntrySetListener) {
            boolean z = false;
            if (isInSameYear(labelCalendar, now)) {
                return false;
            }
            if (titleArgs.index == 0) {
                return true;
            }
            String[] string = titleEntrySetListener.getGroupData(titleArgs.index - 1).defaultTitle.split("-");
            if (string == null) {
                return true;
            }
            Date lastLabelDate = formatStringToDate(string[0]);
            if (lastLabelDate == null) {
                return true;
            }
            Calendar lastLabelCalendar = Calendar.getInstance();
            lastLabelCalendar.setTime(lastLabelDate);
            if (!isInSameYear(labelCalendar, lastLabelCalendar)) {
                z = true;
            }
            return z;
        }

        protected boolean isInSameYear(Calendar calendar, Calendar anotherCalendar) {
            return calendar.get(1) == anotherCalendar.get(1);
        }

        protected Date formatStringToDate(String formatString) {
            try {
                return TimeAxisLabel.this.mSQLFormat.parse(formatString);
            } catch (ParseException e) {
                return null;
            }
        }
    }

    public class DaySpec extends BaseSpec {
        public DaySpec(TitleSpec spec) {
            super(spec, R.drawable.ic_gallery_map_pin);
        }

        public String[] setText(TitleArgs titleArgs, TitleEntrySetListener titleEntrySetListener) {
            String[] rs = new String[1];
            Date date = formatStringToDate(titleArgs.groupData.defaultTitle.split("-")[0]);
            if (date == null) {
                if (titleArgs.groupData.isCloudHistroyData) {
                    rs[0] = TimeAxisLabel.this.mContext.getResources().getString(R.string.photoshare_old_version_upload_tips);
                } else {
                    rs[0] = "";
                }
                return rs;
            }
            int dateTitleFormatFlag;
            Calendar labelCalendar = Calendar.getInstance();
            labelCalendar.setTime(date);
            Calendar now = Calendar.getInstance();
            if (isInSameYear(labelCalendar, now)) {
                if (now.get(6) == labelCalendar.get(6)) {
                    rs[0] = TimeAxisLabel.this.mContext.getResources().getString(R.string.today);
                    return rs;
                } else if (now.get(6) - labelCalendar.get(6) == 1) {
                    rs[0] = TimeAxisLabel.this.mContext.getResources().getString(R.string.yesterday);
                    return rs;
                }
            }
            if (isNeedShowYear(labelCalendar, now, titleArgs, titleEntrySetListener)) {
                dateTitleFormatFlag = 20;
            } else {
                dateTitleFormatFlag = 24;
            }
            rs[0] = DateUtils.formatDateTime(TimeAxisLabel.this.mContext, date.getTime(), dateTitleFormatFlag);
            return rs;
        }
    }

    public interface GroupAddressListener {
        String getAddressStringFromCache(int i, boolean z, JobContext jobContext);
    }

    private class LazyLoadedBitmap {
        private Bitmap mBitmap;
        private int mResId;

        public LazyLoadedBitmap(int resId) {
            this.mResId = resId;
        }

        public synchronized Bitmap get() {
            if (this.mBitmap == null) {
                this.mBitmap = HwImageView.drawableToBitmap(TimeAxisLabel.this.mContext.getResources().getDrawable(this.mResId));
            }
            return this.mBitmap;
        }
    }

    public class MonthSpec extends BaseSpec {
        public MonthSpec(TitleSpec spec) {
            super(spec, R.drawable.ic_gallery_map_pin);
        }

        public String[] setText(TitleArgs titleArgs, TitleEntrySetListener titleEntrySetListener) {
            String[] rs = new String[1];
            Date date = formatStringToDate(titleArgs.groupData.defaultTitle.split("-")[0]);
            if (date == null) {
                rs[0] = "";
                return rs;
            }
            int dateTitleFormatFlag;
            Calendar labelCalendar = Calendar.getInstance();
            labelCalendar.setTime(date);
            if (isNeedShowYear(labelCalendar, Calendar.getInstance(), titleArgs, titleEntrySetListener)) {
                dateTitleFormatFlag = 36;
            } else {
                dateTitleFormatFlag = 40;
                if (GalleryUtils.isChineseLanguage()) {
                    dateTitleFormatFlag = 131112;
                }
            }
            rs[0] = DateUtils.formatDateTime(TimeAxisLabel.this.mContext, date.getTime(), dateTitleFormatFlag);
            return rs;
        }
    }

    public static class TitleArgs {
        public String address;
        public AbsGroupData groupData;
        public int index;
        public boolean isAddressDrew = false;
    }

    private class TitleBitmapJob extends BaseJob<Bitmap> {
        private boolean DEBUG = false;
        private final String mAddress;
        private String mFirstText;
        private final int mIndex;
        private final GroupAddressListener mListener;
        private final TitleArgs mTitleArgs;
        private String mTitleText;

        public TitleBitmapJob(TitleEntrySetListener titleEntrySetListener, TitleArgs titleArgs, GroupAddressListener listener) {
            this.mFirstText = TimeAxisLabel.this.mCurrentSpec.setText(titleArgs, titleEntrySetListener)[0].toUpperCase(Locale.US);
            this.mTitleText = formatTitle(titleArgs.groupData.defaultTitle);
            this.mAddress = titleArgs.address;
            this.mListener = listener;
            this.mIndex = titleArgs.index;
            this.mTitleArgs = titleArgs;
        }

        private String formatTitle(String title) {
            String[] t = title.split("-");
            int len = t.length;
            if (len < 1) {
                return null;
            }
            StringBuilder titleString = new StringBuilder();
            try {
                titleString.append(DateUtils.formatDateTime(TimeAxisLabel.this.mContext, TimeAxisLabel.this.mSQLFormat.parse(t[0]).getTime(), 131092));
                if (!(len <= 1 || t[0] == null || t[0].equals(t[1]))) {
                    titleString.append(" ");
                    titleString.append("-");
                    titleString.append(" ");
                    titleString.append(DateUtils.formatDateTime(TimeAxisLabel.this.mContext, TimeAxisLabel.this.mSQLFormat.parse(t[1]).getTime(), 131092));
                }
                return titleString.toString();
            } catch (ParseException e) {
                return null;
            }
        }

        public Bitmap run(JobContext jc) {
            if (this.DEBUG) {
                return null;
            }
            Bitmap bitmap;
            TitleSpec s = TimeAxisLabel.this.mTitleSpec;
            BaseSpec bs = TimeAxisLabel.this.mCurrentSpec;
            synchronized (this) {
                bitmap = bs.bitmapPool.getBitmap();
                int labelWidth = bs.label_width;
                int labelHeight = bs.label_height;
            }
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(TimeAxisLabel.this.mContext.getResources().getDisplayMetrics(), labelWidth, labelHeight, Config.ARGB_8888);
            }
            Canvas canvas = new Canvas(bitmap);
            canvas.save();
            canvas.clipRect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawColor(s.background_color, Mode.SRC);
            canvas.restore();
            GroupTitlePool.put(this.mTitleArgs.groupData.defaultTitle, this.mFirstText, bs.mTimeTextPaint);
            String title = null;
            if (this.mAddress != null) {
                title = this.mAddress;
            } else if (this.mListener != null) {
                title = this.mListener.getAddressStringFromCache(this.mIndex, TimeAxisLabel.this.mCurrentSpec == TimeAxisLabel.this.mDaySpec, jc);
            }
            boolean isAddressAvailable = true;
            if (title == null || title.equals("HAS_LOCATION_ITEM")) {
                isAddressAvailable = false;
            }
            this.mTitleArgs.isAddressDrew = isAddressAvailable;
            boolean isPhotoShareTimeBucket = s.label_tag == 3;
            if (jc.isCancelled()) {
                return null;
            }
            boolean isNeedDrawGroupTitle = !isAddressAvailable ? isPhotoShareTimeBucket && !this.mTitleArgs.groupData.isCloudHistroyData : true;
            bs.drawFirstTitle(canvas, this.mFirstText, s, isNeedDrawGroupTitle);
            if (isNeedDrawGroupTitle) {
                if (jc.isCancelled()) {
                    return null;
                }
                int offset = bs.drawGroupTitle(canvas, this.mTitleArgs, title, s);
                if (TimeAxisLabel.this.needDrawArrow() && s.label_tag != 2) {
                    if (jc.isCancelled()) {
                        return null;
                    }
                    bs.drawArrow(canvas, offset, s);
                }
            }
            if (jc.isCancelled()) {
                return null;
            }
            bs.drawTransparent(canvas, s);
            return bitmap;
        }

        public String workContent() {
            return String.format("create label bitmap, firstText: %s, titleText: %s, addressText: %s", new Object[]{this.mFirstText, this.mTitleText, this.mAddress});
        }
    }

    public static class TitleSpec {
        public int background_color;
        public int bardian_time_line_group_title_text_color;
        public int label_member_text_color;
        public int label_member_text_size;
        public int label_min_height;
        public int label_share_gap;
        public int label_share_text_color;
        public int label_share_text_size;
        public int label_tag;
        public int label_width;
        public int mGroupTitleTextHeight;
        public TextPaint mGroupTitleTextPaint;
        public TextPaint mPhotoShareMemberPaint = Config$LocalCameraAlbumPage.createTextPaint(this.titleLabelSpec.label_member_text_size, this.titleLabelSpec.label_member_text_color);
        public TextPaint mPhotoShareShareMsgPaint = Config$LocalCameraAlbumPage.createTextPaint(this.titleLabelSpec.label_share_text_size, this.titleLabelSpec.label_share_text_color);
        public int mTimeTextHeight;
        public TextPaint mTimeTextPaint;
        public int time_line_bottom_margin;
        public int time_line_group_title_text_size;
        public int time_line_icon;
        public int time_line_start_padding;
        public int time_line_text_color;
        public int time_line_text_size;
        public int time_line_title_gap;
        public int time_line_top_magin;
        public int time_line_width;
    }

    private static /* synthetic */ int[] -getcom-android-gallery3d-data-TimeBucketPageViewModeSwitchesValues() {
        if (-com-android-gallery3d-data-TimeBucketPageViewModeSwitchesValues != null) {
            return -com-android-gallery3d-data-TimeBucketPageViewModeSwitchesValues;
        }
        int[] iArr = new int[TimeBucketPageViewMode.values().length];
        try {
            iArr[TimeBucketPageViewMode.DAY.ordinal()] = 2;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[TimeBucketPageViewMode.MONTH.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        -com-android-gallery3d-data-TimeBucketPageViewModeSwitchesValues = iArr;
        return iArr;
    }

    public TimeAxisLabel(Context context, TitleSpec titleSpec) {
        this.mContext = context;
        this.mTitleSpec = titleSpec;
        this.mDaySpec = new DaySpec(titleSpec);
        this.mMonthSpec = new MonthSpec(titleSpec);
        setDefaultMode(TimeBucketPageViewMode.DAY);
        this.mIsLayoutRtl = GalleryUtils.isLayoutRTL();
        this.mIsTabletProduct = GalleryUtils.isTabletProduct(context);
    }

    public void setDefaultMode(TimeBucketPageViewMode mode) {
        this.mMode = mode;
        switch (-getcom-android-gallery3d-data-TimeBucketPageViewModeSwitchesValues()[mode.ordinal()]) {
            case 1:
                this.mCurrentSpec = this.mMonthSpec;
                return;
            default:
                this.mCurrentSpec = this.mDaySpec;
                return;
        }
    }

    public int getCurrentTitleModeValue() {
        return ((this.mMode == TimeBucketPageViewMode.DAY ? 0 : 1) << 16) + this.mTitleSpec.label_tag;
    }

    static int getTextWidth(String text, int limit, TextPaint p) {
        return (int) Math.ceil((double) p.measureText(TextUtils.ellipsize(text, p, (float) limit, TruncateAt.END).toString()));
    }

    static int drawText(Canvas canvas, int x, int y, String text, int limit, TextPaint p) {
        int textWidth;
        synchronized (p) {
            text = TextUtils.ellipsize(text, p, (float) limit, TruncateAt.END).toString();
            canvas.drawText(text, (float) x, (float) (y - p.getFontMetricsInt().ascent), p);
            textWidth = getTextWidth(text, limit, p);
        }
        return textWidth;
    }

    public void recycleLabel(Bitmap title) {
        int width = title.getWidth();
        int height = title.getHeight();
        if (width == this.mDaySpec.label_width && height == this.mDaySpec.label_height) {
            this.mDaySpec.bitmapPool.recycle(title);
        } else {
            this.mMonthSpec.bitmapPool.recycle(title);
        }
    }

    public void clearRecycledLabels() {
        this.mDaySpec.bitmapPool.clear();
        this.mMonthSpec.bitmapPool.clear();
    }

    public boolean needDrawArrow() {
        return true;
    }

    public Job<Bitmap> requestTitle(TitleEntrySetListener titleEntrySetListener, TitleArgs titleArgs, GroupAddressListener listener) {
        return new TitleBitmapJob(titleEntrySetListener, titleArgs, listener);
    }
}
