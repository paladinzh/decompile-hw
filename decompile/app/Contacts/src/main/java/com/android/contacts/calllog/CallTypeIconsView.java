package com.android.contacts.calllog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.test.NeededForTesting;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.TextUtil;
import com.google.android.gms.R;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;

public class CallTypeIconsView extends View {
    private static Resources sResources;
    private List<Integer> mCallTypes;
    private HashMap<Integer, Integer> mCallTypesCount;
    private List<Long> mFeatureTypes;
    private int mHeight;
    private int mIncomingWidgth;
    private boolean mIsMirror;
    private int mMissedWidth;
    private int mOutgoingWidth;
    private int mVoicemailWidth;
    private int mWidth;
    private Drawable[] normalDrawable;

    private static class Resources {
        public final String desc_incoming_call;
        public final String desc_incoming_video_call;
        public final String desc_missed_call;
        public final String desc_missed_video_call;
        public final String desc_outgoing_call;
        public final String desc_outgoing_video_call;
        public final String desc_rejected_call;
        public final String desc_rejected_video_call;
        public final String desc_unread_voicemail;
        public final String desc_voicemail;
        public final int iconMargin;
        public final Drawable incoming;
        public final Drawable incoming_video;
        private int mIncomingColor;
        private int mMissedColor;
        private int mOutgoingColor;
        public float mSpace;
        public int mSpaceInt = Float.valueOf(this.mSpace).intValue();
        public float mTextSize;
        public float mTextTop;
        private int mVoicemailColor;
        public final Drawable missed;
        public final Drawable missed_video;
        public final Drawable outgoing;
        public final Drawable outgoing_video;
        public final Drawable reject;
        public final Drawable voicemail;

        public Resources(Context context) {
            android.content.res.Resources r = context.getResources();
            this.outgoing = r.getDrawable(R.drawable.ic_call_out);
            this.incoming = r.getDrawable(R.drawable.ic_call_incoming_normal);
            this.missed = r.getDrawable(R.drawable.ic_call_incoming_missed);
            this.reject = r.getDrawable(R.drawable.ic_video_call_incoming_reject);
            this.voicemail = r.getDrawable(R.drawable.contact_icon_voicemail);
            this.iconMargin = r.getDimensionPixelSize(R.dimen.call_log_icon_margin);
            this.mTextSize = context.getResources().getDimension(R.dimen.call_log_count_text_size);
            this.mTextTop = context.getResources().getDimension(R.dimen.call_log_count_text_top);
            this.mSpace = context.getResources().getDimension(R.dimen.call_log_count_space);
            this.mMissedColor = r.getColor(R.color.call_log_missed_call_count);
            this.mIncomingColor = r.getColor(R.color.call_log_secondary_text_color);
            this.mOutgoingColor = r.getColor(R.color.call_log_secondary_text_color);
            this.mVoicemailColor = r.getColor(R.color.call_log_secondary_text_color);
            this.outgoing_video = r.getDrawable(R.drawable.ic_video_call_out);
            this.incoming_video = r.getDrawable(R.drawable.ic_video_call_incoming_normal);
            this.missed_video = r.getDrawable(R.drawable.ic_video_call_incoming_missed);
            this.desc_outgoing_call = r.getString(R.string.content_description_outgoing_call);
            this.desc_incoming_call = r.getString(R.string.content_description_incoming_call);
            this.desc_missed_call = r.getString(R.string.content_description_missed_call);
            this.desc_rejected_call = r.getString(R.string.content_description_rejected_call);
            this.desc_voicemail = r.getString(R.string.content_description_voicemail);
            this.desc_unread_voicemail = r.getString(R.string.content_description_unread_voicemail);
            this.desc_outgoing_video_call = r.getString(R.string.content_description_outgoing_video_call);
            this.desc_incoming_video_call = r.getString(R.string.content_description_incoming_video_call);
            this.desc_missed_video_call = r.getString(R.string.content_description_missed_video_call);
            this.desc_rejected_video_call = r.getString(R.string.content_description_rejected_video_call);
        }

        private int getColor(Integer aCallType) {
            switch (aCallType.intValue()) {
                case 1:
                    return this.mIncomingColor;
                case 2:
                    return this.mOutgoingColor;
                case 3:
                case 5:
                    return this.mMissedColor;
                case 4:
                    return this.mVoicemailColor;
                default:
                    throw new IllegalArgumentException("invalid call type: " + aCallType);
            }
        }
    }

    public CallTypeIconsView(Context context) {
        this(context, null);
        if (sResources == null) {
            sResources = new Resources(context);
        }
    }

    public CallTypeIconsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mCallTypes = Lists.newArrayListWithCapacity(3);
        this.mFeatureTypes = Lists.newArrayListWithCapacity(3);
        this.normalDrawable = new Drawable[1];
        if (sResources == null) {
            sResources = new Resources(context);
        }
        this.mIsMirror = CommonUtilMethods.isLayoutRTL();
    }

    public void clear() {
        this.mCallTypes.clear();
        this.mFeatureTypes.clear();
        this.mWidth = 0;
        this.mHeight = 0;
        invalidate();
    }

    public void add(int callType, long featureType, int readState) {
        this.mCallTypes.add(Integer.valueOf(callType));
        this.mFeatureTypes.add(Long.valueOf(featureType));
        setContentDescription(getCallTypeContentDescription(callType, featureType, readState));
        for (Drawable drawable : getCallTypeDrawable(callType, featureType)) {
            if (drawable != null) {
                this.mWidth += drawable.getIntrinsicWidth() + sResources.iconMargin;
                this.mHeight = Math.max(this.mHeight, drawable.getIntrinsicHeight());
            }
        }
        invalidate();
    }

    @NeededForTesting
    public int getCount() {
        return this.mCallTypes.size();
    }

    @NeededForTesting
    public int getCallType(int index) {
        return ((Integer) this.mCallTypes.get(index)).intValue();
    }

    private Drawable[] getCallTypeDrawable(int callType, long featureType) {
        switch (callType) {
            case 1:
                this.normalDrawable[0] = sResources.incoming;
                if (featureType == 1) {
                    this.normalDrawable[0] = sResources.incoming_video;
                }
                return this.normalDrawable;
            case 2:
                this.normalDrawable[0] = sResources.outgoing;
                if (featureType == 1) {
                    this.normalDrawable[0] = sResources.outgoing_video;
                }
                return this.normalDrawable;
            case 3:
                this.normalDrawable[0] = sResources.missed;
                if (featureType == 1) {
                    this.normalDrawable[0] = sResources.missed_video;
                }
                return this.normalDrawable;
            case 4:
                this.normalDrawable[0] = sResources.voicemail;
                return this.normalDrawable;
            case 5:
                this.normalDrawable[0] = sResources.reject;
                return this.normalDrawable;
            default:
                return this.normalDrawable;
        }
    }

    public String getCallTypeContentDescription(int callType, long featureType, int readState) {
        String contentDescription = "";
        switch (callType) {
            case 1:
                if (featureType == 1) {
                    return sResources.desc_incoming_video_call;
                }
                return sResources.desc_incoming_call;
            case 2:
                if (featureType == 1) {
                    return sResources.desc_outgoing_video_call;
                }
                return sResources.desc_outgoing_call;
            case 3:
                if (featureType == 1) {
                    return sResources.desc_missed_video_call;
                }
                return sResources.desc_missed_call;
            case 4:
                if (readState == 0) {
                    return sResources.desc_unread_voicemail;
                }
                return sResources.desc_voicemail;
            case 5:
                if (featureType == 1) {
                    return sResources.desc_rejected_video_call;
                }
                return sResources.desc_rejected_call;
            default:
                return sResources.desc_incoming_call;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(this.mWidth, this.mHeight);
    }

    protected void onDraw(Canvas canvas) {
        int left = 0;
        int right = 0;
        Paint paint = new Paint();
        paint.setTextSize(sResources.mTextSize);
        for (int index = 0; index < this.mCallTypes.size(); index++) {
            int callType = ((Integer) this.mCallTypes.get(index)).intValue();
            Drawable[] drawables = getCallTypeDrawable(callType, ((Long) this.mFeatureTypes.get(index)).longValue());
            boolean isMoreThanOneDrawable = drawables.length > 1;
            for (Drawable drawable : drawables) {
                if (drawable != null) {
                    if (!this.mIsMirror || isMoreThanOneDrawable) {
                        right = left + drawable.getIntrinsicWidth();
                        drawable.setBounds(left, 0, right, drawable.getIntrinsicHeight());
                        drawable.draw(canvas);
                    }
                    if (this.mCallTypesCount != null && this.mCallTypesCount.containsKey(Integer.valueOf(callType))) {
                        int value = ((Integer) this.mCallTypesCount.get(Integer.valueOf(callType))).intValue();
                        if (value > 1) {
                            if (!this.mIsMirror || isMoreThanOneDrawable) {
                                left = right + sResources.mSpaceInt;
                            }
                            paint.setColor(sResources.getColor(Integer.valueOf(callType)));
                            String countText = "(" + value + ")";
                            canvas.drawText(countText, (float) left, sResources.mTextTop, paint);
                            left += getTextWidth(Integer.valueOf(callType), countText) + sResources.iconMargin;
                        } else if (!this.mIsMirror || isMoreThanOneDrawable) {
                            left = right + sResources.iconMargin;
                        } else {
                            left += sResources.iconMargin;
                        }
                    } else if (!this.mIsMirror || isMoreThanOneDrawable) {
                        left = right + sResources.iconMargin;
                    } else {
                        left += sResources.iconMargin;
                    }
                    if (this.mIsMirror && !isMoreThanOneDrawable) {
                        right = left + drawable.getIntrinsicWidth();
                        drawable.setBounds(left, 0, right, drawable.getIntrinsicHeight());
                        drawable.draw(canvas);
                        left = right + sResources.mSpaceInt;
                    }
                }
            }
        }
    }

    private int getTextWidth(Integer aCallType, String aString) {
        switch (aCallType.intValue()) {
            case 1:
                return this.mIncomingWidgth;
            case 2:
                return this.mOutgoingWidth;
            case 3:
            case 5:
                return this.mMissedWidth;
            case 4:
                return this.mVoicemailWidth;
            default:
                HwLog.i("CallTypeIconsView", "getTextWidth Coming to default case where calculating text width");
                return TextUtil.getTextWidth(aString, sResources.mTextSize);
        }
    }

    public static void resetCallTypeResources(Context context) {
        if (context != null) {
            sResources = new Resources(context);
        }
    }
}
