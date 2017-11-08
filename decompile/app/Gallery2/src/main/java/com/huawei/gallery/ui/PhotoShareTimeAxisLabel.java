package com.huawei.gallery.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.text.TextPaint;
import com.android.gallery3d.R;
import com.android.gallery3d.data.PhotoShareTimeBucketAlbum.CloudShareGroupData;
import com.android.gallery3d.data.TimeBucketPageViewMode;
import com.huawei.gallery.ui.TimeAxisLabel.BaseSpec;
import com.huawei.gallery.ui.TimeAxisLabel.DaySpec;
import com.huawei.gallery.ui.TimeAxisLabel.TitleArgs;
import com.huawei.gallery.ui.TimeAxisLabel.TitleSpec;

public class PhotoShareTimeAxisLabel extends TimeAxisLabel {
    BaseSpec mCloudSpec;

    public class CloudShareSpec extends DaySpec {
        private TextPaint mMemberPaint;
        private TextPaint mShareMsgPaint;

        public CloudShareSpec(TitleSpec spec) {
            super(spec);
            this.mMemberPaint = spec.mPhotoShareMemberPaint;
            this.mShareMsgPaint = spec.mPhotoShareShareMsgPaint;
        }

        public int drawGroupTitle(Canvas canvas, TitleArgs titleArgs, String title, TitleSpec s) {
            if (titleArgs == null || !(titleArgs.groupData instanceof CloudShareGroupData)) {
                return 0;
            }
            int x;
            int y;
            String shareMsg;
            CloudShareGroupData cloudShareGroupData = titleArgs.groupData;
            String member = cloudShareGroupData.createrNickName;
            if (this.mOwner.mIsTabletProduct) {
                if (this.mOwner.mIsLayoutRtl) {
                    x = (((this.label_width - s.time_line_width) - s.time_line_start_padding) - this.mFirstTitleWidth) - this.mTitleGap;
                } else {
                    x = ((s.time_line_width + s.time_line_start_padding) + this.mFirstTitleWidth) + this.mTitleGap;
                }
                y = (this.label_height - this.label_bottom_margin) - this.mGroupTitleHeight;
            } else {
                if (this.mOwner.mIsLayoutRtl) {
                    x = (this.label_width - s.time_line_width) - s.time_line_start_padding;
                } else {
                    x = s.time_line_width + s.time_line_start_padding;
                }
                y = this.label_top_margin + this.mFirstTitleHeight;
            }
            int offset = 0;
            if (cloudShareGroupData.batch != 0) {
                if (this.mOwner.mIsLayoutRtl) {
                    x -= TimeAxisLabel.getTextWidth(member, this.label_width, this.mMemberPaint);
                }
                offset = TimeAxisLabel.drawText(canvas, x, y, member, this.label_width - x, this.mMemberPaint);
            }
            if (cloudShareGroupData.batch == 0) {
                shareMsg = this.mOwner.mContext.getResources().getString(R.string.photoshare_old_version_upload_tips);
            } else {
                shareMsg = PhotoShareTimeAxisLabel.this.createPictureAndVideoCountString(cloudShareGroupData.count, cloudShareGroupData.videoCount);
            }
            if (this.mOwner.mIsLayoutRtl) {
                if (cloudShareGroupData.batch != 0) {
                    x -= s.label_share_gap;
                }
                x -= TimeAxisLabel.getTextWidth(shareMsg, this.label_width, this.mShareMsgPaint);
            } else {
                if (cloudShareGroupData.batch != 0) {
                    offset += s.label_share_gap;
                }
                x += offset;
            }
            TimeAxisLabel.drawText(canvas, x, y, shareMsg, this.label_width - x, this.mShareMsgPaint);
            return 0;
        }
    }

    public PhotoShareTimeAxisLabel(Context context, TitleSpec titleSpec) {
        super(context, titleSpec);
        setDefaultMode(TimeBucketPageViewMode.DAY);
    }

    public void setDefaultMode(TimeBucketPageViewMode mode) {
        this.mMode = mode;
        BaseSpec cloudShareSpec = new CloudShareSpec(this.mTitleSpec);
        this.mCloudSpec = cloudShareSpec;
        this.mCurrentSpec = cloudShareSpec;
    }

    public void recycleLabel(Bitmap title) {
        this.mCloudSpec.bitmapPool.recycle(title);
    }

    public void clearRecycledLabels() {
        this.mCloudSpec.bitmapPool.clear();
    }

    public boolean needDrawArrow() {
        return false;
    }

    private String createPictureAndVideoCountString(int totalCount, int totalVideoCount) {
        int totalImageCount = totalCount - totalVideoCount;
        String format;
        String videoCount;
        if (totalImageCount == 0 && totalVideoCount > 0) {
            format = this.mContext.getResources().getString(R.string.cloud_share_msg_only_video_or_photo);
            videoCount = this.mContext.getResources().getQuantityString(R.plurals.cloud_share_msg_video_count, totalVideoCount, new Object[]{Integer.valueOf(totalVideoCount)});
            return String.format(format, new Object[]{videoCount});
        } else if (totalImageCount <= 0 || totalVideoCount <= 0) {
            format = this.mContext.getResources().getString(R.string.cloud_share_msg_only_video_or_photo);
            photoCount = this.mContext.getResources().getQuantityString(R.plurals.cloud_share_msg_photo_count, totalCount, new Object[]{Integer.valueOf(totalCount)});
            return String.format(format, new Object[]{photoCount});
        } else {
            photoCount = this.mContext.getResources().getQuantityString(R.plurals.cloud_share_msg_photo_count, totalImageCount, new Object[]{Integer.valueOf(totalImageCount)});
            videoCount = this.mContext.getResources().getQuantityString(R.plurals.cloud_share_msg_video_count, totalVideoCount, new Object[]{Integer.valueOf(totalVideoCount)});
            return String.format(this.mContext.getResources().getString(R.string.cloud_share_msg_photo_video_count), new Object[]{photoCount, videoCount});
        }
    }
}
