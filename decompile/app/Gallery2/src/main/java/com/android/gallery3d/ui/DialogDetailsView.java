package com.android.gallery3d.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.amap.api.maps.model.WeightedLatLng;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaDetails;
import com.android.gallery3d.data.MediaDetails.FlashState;
import com.android.gallery3d.data.MediaDetails.MediaDetailsListener;
import com.android.gallery3d.data.MediaDetailsTask;
import com.android.gallery3d.ui.DetailsAddressResolver.AddressResolvingListener;
import com.android.gallery3d.ui.DetailsHelper.CloseListener;
import com.android.gallery3d.ui.DetailsHelper.DetailsSource;
import com.android.gallery3d.ui.DetailsHelper.DetailsViewContainer;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.share.HwCustUtilsWrapper;
import java.util.ArrayList;
import java.util.Map.Entry;

public class DialogDetailsView implements DetailsViewContainer {
    private static final String[] MODEL_HAS_PD = new String[]{"LON-AL00-PD", "LON-L29-PD"};
    private static final String[] MODEL_NO_PD = new String[]{"LON-AL00", "LON-L29"};
    private final GalleryContext mActivity;
    private DetailsAdapter mAdapter;
    private DetailLayout mDetailLayout;
    private MediaDetails mDetails;
    private AlertDialog mDialog;
    private int mIndex;
    private CloseListener mListener;
    private final DetailsSource mSource;

    public static class DetailItem {
        final String mContent;
        final int mDrawId;
        final String mTitle;

        public DetailItem(String title, String content) {
            this.mTitle = title;
            this.mContent = content;
            this.mDrawId = 0;
        }

        public DetailItem(String title, String content, int drawId) {
            this.mTitle = title;
            this.mContent = content;
            this.mDrawId = drawId;
        }
    }

    class DetailsAdapter extends BaseAdapter implements AddressResolvingListener, MediaDetailsListener {
        Context mContext;
        private HwCustDialogDetailsView mCust;
        private final SparseArray<DetailItem> mDetailItems;
        private final ArrayList<DetailItem> mItems;
        private int mLocationIndex;
        private Future<String> mSetSizeTask = null;

        public DetailsAdapter(MediaDetails details, Context context) {
            this.mContext = context;
            this.mItems = new ArrayList(details.size());
            this.mDetailItems = new SparseArray(details.size());
            this.mLocationIndex = -1;
            this.mCust = (HwCustDialogDetailsView) HwCustUtilsWrapper.createObj(HwCustDialogDetailsView.class, new Object[0]);
            setDetails(this.mContext, details);
        }

        public SparseArray<DetailItem> getDetailItems() {
            return this.mDetailItems;
        }

        public void cancelTask() {
            if (this.mSetSizeTask != null) {
                this.mSetSizeTask.cancel();
                this.mSetSizeTask = null;
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        @SuppressLint({"StringFormatMatches"})
        private void setDetails(Context context, MediaDetails details) {
            int orientation = getLengthInteger(details.getDetail(998));
            for (Entry<Integer, Object> detail : details) {
                String value;
                int key;
                String title;
                String titleShort;
                Object[] objArr;
                int drawId = 0;
                boolean omitAreabian = true;
                Object valueObj;
                int length;
                switch (((Integer) detail.getKey()).intValue()) {
                    case 2:
                        valueObj = detail.getValue();
                        if (valueObj != null) {
                            value = valueObj.toString();
                            int index = 0;
                            while (index < DialogDetailsView.MODEL_HAS_PD.length) {
                                if (value.contains(DialogDetailsView.MODEL_HAS_PD[index])) {
                                    value = DialogDetailsView.MODEL_NO_PD[index];
                                } else {
                                    index++;
                                }
                            }
                        } else {
                            continue;
                        }
                    case 3:
                        value = GalleryUtils.getFomattedDateTime(context, ((Long) detail.getValue()).longValue());
                        omitAreabian = false;
                        key = ((Integer) detail.getKey()).intValue();
                        if (GalleryUtils.isFrenchLanguage()) {
                            title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        } else {
                            title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        }
                        titleShort = DetailsHelper.getDetailsShortName(context, key);
                        if (value != null) {
                            if (!details.hasUnit(key)) {
                                objArr = new Object[2];
                                objArr[0] = value;
                                objArr[1] = context.getString(details.getUnit(key));
                                value = String.format("%s %s", objArr);
                            } else if (omitAreabian) {
                                value = String.format("‎%s‎", new Object[]{value});
                            } else {
                                value = String.format("%s", new Object[]{value});
                            }
                        }
                        setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
                        break;
                    case 4:
                        double[] latlng = (double[]) detail.getValue();
                        this.mLocationIndex = this.mItems.size();
                        long start = System.currentTimeMillis();
                        value = DetailsAddressResolver.queryAddressFromCache(this.mContext, latlng);
                        GalleryLog.v("DialogDetailsView", "queryAddressFromCache cost " + (System.currentTimeMillis() - start));
                        if (TextUtils.isEmpty(value)) {
                            value = DetailsHelper.resolveAddress(DialogDetailsView.this.mActivity, latlng, this, true);
                            GalleryLog.v("DialogDetailsView", "not find  FromCache");
                        }
                        key = ((Integer) detail.getKey()).intValue();
                        if (GalleryUtils.isFrenchLanguage()) {
                            title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        } else {
                            title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        }
                        titleShort = DetailsHelper.getDetailsShortName(context, key);
                        if (value != null) {
                            if (!details.hasUnit(key)) {
                                objArr = new Object[2];
                                objArr[0] = value;
                                objArr[1] = context.getString(details.getUnit(key));
                                value = String.format("%s %s", objArr);
                            } else if (omitAreabian) {
                                value = String.format("%s", new Object[]{value});
                            } else {
                                value = String.format("‎%s‎", new Object[]{value});
                            }
                        }
                        setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
                        break;
                    case 5:
                        value = Formatter.formatFileSize(context, ((Long) detail.getValue()).longValue());
                        key = ((Integer) detail.getKey()).intValue();
                        if (GalleryUtils.isFrenchLanguage()) {
                            title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        } else {
                            title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        }
                        titleShort = DetailsHelper.getDetailsShortName(context, key);
                        if (value != null) {
                            if (!details.hasUnit(key)) {
                                objArr = new Object[2];
                                objArr[0] = value;
                                objArr[1] = context.getString(details.getUnit(key));
                                value = String.format("%s %s", objArr);
                            } else if (omitAreabian) {
                                value = String.format("‎%s‎", new Object[]{value});
                            } else {
                                value = String.format("%s", new Object[]{value});
                            }
                        }
                        setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
                        break;
                    case 6:
                        if (orientation % 180 == 0) {
                            length = getLengthInteger(detail.getValue());
                        } else {
                            length = getLengthInteger(details.getDetail(7));
                        }
                        if (length != 0) {
                            value = String.format("%d", new Object[]{Integer.valueOf(length)});
                        } else {
                            continue;
                        }
                    case 7:
                        if (orientation % 180 == 0) {
                            length = getLengthInteger(detail.getValue());
                        } else {
                            length = getLengthInteger(details.getDetail(6));
                        }
                        if (length != 0) {
                            value = String.format("%d", new Object[]{Integer.valueOf(length)});
                        } else {
                            continue;
                        }
                    case 101:
                        value = getExposureTime((String) detail.getValue());
                        key = ((Integer) detail.getKey()).intValue();
                        if (GalleryUtils.isFrenchLanguage()) {
                            title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        } else {
                            title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        }
                        titleShort = DetailsHelper.getDetailsShortName(context, key);
                        if (value != null) {
                            if (!details.hasUnit(key)) {
                                objArr = new Object[2];
                                objArr[0] = value;
                                objArr[1] = context.getString(details.getUnit(key));
                                value = String.format("%s %s", objArr);
                            } else if (omitAreabian) {
                                value = String.format("%s", new Object[]{value});
                            } else {
                                value = String.format("‎%s‎", new Object[]{value});
                            }
                        }
                        setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
                        break;
                    case 102:
                        valueObj = detail.getValue();
                        if (valueObj != null) {
                            value = GalleryUtils.getValueFormat(Long.parseLong(valueObj.toString()));
                        } else {
                            continue;
                        }
                    case OfflineMapStatus.EXCEPTION_SDCARD /*103*/:
                    case 113:
                    case 998:
                    case 999:
                        break;
                    case 104:
                        GalleryLog.i("DialogDetailsView", "F Number String value is :" + ((String) detail.getValue()));
                        value = DialogDetailsView.subZeroAndDot(String.format("%.2f", new Object[]{Double.valueOf(value)}));
                        key = ((Integer) detail.getKey()).intValue();
                        if (GalleryUtils.isFrenchLanguage()) {
                            title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        } else {
                            title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        }
                        titleShort = DetailsHelper.getDetailsShortName(context, key);
                        if (value != null) {
                            if (!details.hasUnit(key)) {
                                objArr = new Object[2];
                                objArr[0] = value;
                                objArr[1] = context.getString(details.getUnit(key));
                                value = String.format("%s %s", objArr);
                            } else if (omitAreabian) {
                                value = String.format("‎%s‎", new Object[]{value});
                            } else {
                                value = String.format("%s", new Object[]{value});
                            }
                        }
                        setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
                        break;
                    case 105:
                        value = DialogDetailsView.subZeroAndDot(String.format("%.1f", new Object[]{detail.getValue()})) + " mm";
                        key = ((Integer) detail.getKey()).intValue();
                        if (GalleryUtils.isFrenchLanguage()) {
                            title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        } else {
                            title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        }
                        titleShort = DetailsHelper.getDetailsShortName(context, key);
                        if (value != null) {
                            if (!details.hasUnit(key)) {
                                objArr = new Object[2];
                                objArr[0] = value;
                                objArr[1] = context.getString(details.getUnit(key));
                                value = String.format("%s %s", objArr);
                            } else if (omitAreabian) {
                                value = String.format("%s", new Object[]{value});
                            } else {
                                value = String.format("‎%s‎", new Object[]{value});
                            }
                        }
                        setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
                        break;
                    case 106:
                        value = context.getString(R.string.focal_length_equivalent_modify, new Object[]{(Integer) detail.getValue(), Integer.valueOf(35)});
                        key = ((Integer) detail.getKey()).intValue();
                        if (GalleryUtils.isFrenchLanguage()) {
                            title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        } else {
                            title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        }
                        titleShort = DetailsHelper.getDetailsShortName(context, key);
                        if (value != null) {
                            if (!details.hasUnit(key)) {
                                objArr = new Object[2];
                                objArr[0] = value;
                                objArr[1] = context.getString(details.getUnit(key));
                                value = String.format("%s %s", objArr);
                            } else if (omitAreabian) {
                                value = String.format("‎%s‎", new Object[]{value});
                            } else {
                                value = String.format("%s", new Object[]{value});
                            }
                        }
                        setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
                        break;
                    case 107:
                        if ("1".equals(detail.getValue())) {
                            value = context.getString(R.string.manual);
                        } else {
                            value = context.getString(R.string.auto);
                        }
                        drawId = "1".equals(detail.getValue()) ? 0 : R.drawable.ic_btn_wb_auto;
                        key = ((Integer) detail.getKey()).intValue();
                        if (GalleryUtils.isFrenchLanguage()) {
                            title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        } else {
                            title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        }
                        titleShort = DetailsHelper.getDetailsShortName(context, key);
                        if (value != null) {
                            if (!details.hasUnit(key)) {
                                objArr = new Object[2];
                                objArr[0] = value;
                                objArr[1] = context.getString(details.getUnit(key));
                                value = String.format("%s %s", objArr);
                            } else if (omitAreabian) {
                                value = String.format("%s", new Object[]{value});
                            } else {
                                value = String.format("‎%s‎", new Object[]{value});
                            }
                        }
                        setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
                        break;
                    case 108:
                        FlashState flash = (FlashState) detail.getValue();
                        if (flash.isFlashFired()) {
                            value = context.getString(R.string.flash_on);
                        } else {
                            value = context.getString(R.string.flash_off);
                        }
                        drawId = getFlashIconDrawableId(flash.mState);
                        key = ((Integer) detail.getKey()).intValue();
                        if (GalleryUtils.isFrenchLanguage()) {
                            title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        } else {
                            title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        }
                        titleShort = DetailsHelper.getDetailsShortName(context, key);
                        if (value != null) {
                            if (!details.hasUnit(key)) {
                                objArr = new Object[2];
                                objArr[0] = value;
                                objArr[1] = context.getString(details.getUnit(key));
                                value = String.format("%s %s", objArr);
                            } else if (omitAreabian) {
                                value = String.format("‎%s‎", new Object[]{value});
                            } else {
                                value = String.format("%s", new Object[]{value});
                            }
                        }
                        setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
                        break;
                    case 109:
                        value = DialogDetailsView.subZeroAndDot(String.format("%.2f", new Object[]{Float.valueOf((String) detail.getValue())}));
                        key = ((Integer) detail.getKey()).intValue();
                        if (GalleryUtils.isFrenchLanguage()) {
                            title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        } else {
                            title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        }
                        titleShort = DetailsHelper.getDetailsShortName(context, key);
                        if (value != null) {
                            if (!details.hasUnit(key)) {
                                objArr = new Object[2];
                                objArr[0] = value;
                                objArr[1] = context.getString(details.getUnit(key));
                                value = String.format("%s %s", objArr);
                            } else if (omitAreabian) {
                                value = String.format("%s", new Object[]{value});
                            } else {
                                value = String.format("‎%s‎", new Object[]{value});
                            }
                        }
                        setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
                        break;
                    case 110:
                        value = (String) detail.getValue();
                        drawId = getMeteringModeIconDrawableId(Short.valueOf(value));
                        key = ((Integer) detail.getKey()).intValue();
                        if (GalleryUtils.isFrenchLanguage()) {
                            title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        } else {
                            title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        }
                        titleShort = DetailsHelper.getDetailsShortName(context, key);
                        if (value != null) {
                            if (!details.hasUnit(key)) {
                                objArr = new Object[2];
                                objArr[0] = value;
                                objArr[1] = context.getString(details.getUnit(key));
                                value = String.format("%s %s", objArr);
                            } else if (omitAreabian) {
                                value = String.format("‎%s‎", new Object[]{value});
                            } else {
                                value = String.format("%s", new Object[]{value});
                            }
                        }
                        setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
                        break;
                    case 111:
                        value = (String) detail.getValue();
                        drawId = getLightSourceIconDrawableId(Short.valueOf(value));
                        key = ((Integer) detail.getKey()).intValue();
                        if (GalleryUtils.isFrenchLanguage()) {
                            title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        } else {
                            title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        }
                        titleShort = DetailsHelper.getDetailsShortName(context, key);
                        if (value != null) {
                            if (!details.hasUnit(key)) {
                                objArr = new Object[2];
                                objArr[0] = value;
                                objArr[1] = context.getString(details.getUnit(key));
                                value = String.format("%s %s", objArr);
                            } else if (omitAreabian) {
                                value = String.format("%s", new Object[]{value});
                            } else {
                                value = String.format("‎%s‎", new Object[]{value});
                            }
                        }
                        setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
                        break;
                    case 150:
                        value = context.getResources().getQuantityString(R.plurals.number_details_new, count, new Object[]{GalleryUtils.getValueFormat((long) ((Integer) detail.getValue()).intValue())});
                        key = ((Integer) detail.getKey()).intValue();
                        if (GalleryUtils.isFrenchLanguage()) {
                            title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        } else {
                            title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        }
                        titleShort = DetailsHelper.getDetailsShortName(context, key);
                        if (value != null) {
                            if (!details.hasUnit(key)) {
                                objArr = new Object[2];
                                objArr[0] = value;
                                objArr[1] = context.getString(details.getUnit(key));
                                value = String.format("%s %s", objArr);
                            } else if (omitAreabian) {
                                value = String.format("‎%s‎", new Object[]{value});
                            } else {
                                value = String.format("%s", new Object[]{value});
                            }
                        }
                        setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
                        break;
                    case 151:
                        if (((Boolean) detail.getValue()).booleanValue()) {
                            value = context.getString(R.string.drm_ro_can_forward);
                        } else {
                            value = context.getString(R.string.drm_ro_can_not_forward);
                        }
                        key = ((Integer) detail.getKey()).intValue();
                        if (GalleryUtils.isFrenchLanguage()) {
                            title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        } else {
                            title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        }
                        titleShort = DetailsHelper.getDetailsShortName(context, key);
                        if (value != null) {
                            if (!details.hasUnit(key)) {
                                objArr = new Object[2];
                                objArr[0] = value;
                                objArr[1] = context.getString(details.getUnit(key));
                                value = String.format("%s %s", objArr);
                            } else if (omitAreabian) {
                                value = String.format("%s", new Object[]{value});
                            } else {
                                value = String.format("‎%s‎", new Object[]{value});
                            }
                        }
                        setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
                        break;
                    case 153:
                        if (((Boolean) detail.getValue()).booleanValue()) {
                            value = context.getString(R.string.drm_ro_license_valid);
                        } else {
                            value = context.getString(R.string.drm_ro_license_invalid);
                        }
                        key = ((Integer) detail.getKey()).intValue();
                        if (GalleryUtils.isFrenchLanguage()) {
                            title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        } else {
                            title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        }
                        titleShort = DetailsHelper.getDetailsShortName(context, key);
                        if (value != null) {
                            if (!details.hasUnit(key)) {
                                objArr = new Object[2];
                                objArr[0] = value;
                                objArr[1] = context.getString(details.getUnit(key));
                                value = String.format("%s %s", objArr);
                            } else if (omitAreabian) {
                                value = String.format("‎%s‎", new Object[]{value});
                            } else {
                                value = String.format("%s", new Object[]{value});
                            }
                        }
                        setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
                        break;
                    case 154:
                        if (((Integer) detail.getValue()).intValue() == 2) {
                            value = context.getString(R.string.drm_ro_operation_display);
                        } else {
                            value = context.getString(R.string.drm_ro_operation_play);
                        }
                        key = ((Integer) detail.getKey()).intValue();
                        if (GalleryUtils.isFrenchLanguage()) {
                            title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        } else {
                            title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        }
                        titleShort = DetailsHelper.getDetailsShortName(context, key);
                        if (value != null) {
                            if (!details.hasUnit(key)) {
                                objArr = new Object[2];
                                objArr[0] = value;
                                objArr[1] = context.getString(details.getUnit(key));
                                value = String.format("%s %s", objArr);
                            } else if (omitAreabian) {
                                value = String.format("%s", new Object[]{value});
                            } else {
                                value = String.format("‎%s‎", new Object[]{value});
                            }
                        }
                        setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
                        break;
                    case 300:
                        long[] size = (long[]) detail.getValue();
                        value = context.getString(R.string.photoshare_size, new Object[]{Formatter.formatFileSize(context, size[1]), Formatter.formatFileSize(context, size[0])});
                        key = ((Integer) detail.getKey()).intValue();
                        if (GalleryUtils.isFrenchLanguage()) {
                            title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        } else {
                            title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        }
                        titleShort = DetailsHelper.getDetailsShortName(context, key);
                        if (value != null) {
                            if (!details.hasUnit(key)) {
                                objArr = new Object[2];
                                objArr[0] = value;
                                objArr[1] = context.getString(details.getUnit(key));
                                value = String.format("%s %s", objArr);
                            } else if (omitAreabian) {
                                value = String.format("‎%s‎", new Object[]{value});
                            } else {
                                value = String.format("%s", new Object[]{value});
                            }
                        }
                        setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
                        break;
                    case 65535:
                        value = getSetSize(context, detail.getValue());
                        key = ((Integer) detail.getKey()).intValue();
                        if (GalleryUtils.isFrenchLanguage()) {
                            title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        } else {
                            title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                        }
                        titleShort = DetailsHelper.getDetailsShortName(context, key);
                        if (value != null) {
                            if (!details.hasUnit(key)) {
                                objArr = new Object[2];
                                objArr[0] = value;
                                objArr[1] = context.getString(details.getUnit(key));
                                value = String.format("%s %s", objArr);
                            } else if (omitAreabian) {
                                value = String.format("%s", new Object[]{value});
                            } else {
                                value = String.format("‎%s‎", new Object[]{value});
                            }
                        }
                        setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
                        break;
                    default:
                        valueObj = detail.getValue();
                        if (valueObj != null) {
                            value = valueObj.toString();
                        } else {
                            continue;
                        }
                }
                key = ((Integer) detail.getKey()).intValue();
                if (GalleryUtils.isFrenchLanguage()) {
                    title = String.format("%s : ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                } else {
                    title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(context, key)});
                }
                titleShort = DetailsHelper.getDetailsShortName(context, key);
                if (value != null) {
                    if (!details.hasUnit(key)) {
                        objArr = new Object[2];
                        objArr[0] = value;
                        objArr[1] = context.getString(details.getUnit(key));
                        value = String.format("%s %s", objArr);
                    } else if (omitAreabian) {
                        value = String.format("‎%s‎", new Object[]{value});
                    } else {
                        value = String.format("%s", new Object[]{value});
                    }
                }
                setProfessionalCameraInfoTitle(context, key, value, drawId, titleShort, title);
            }
        }

        private String getExposureTime(String value) {
            double time = Double.valueOf(value).doubleValue();
            if (time > 0.25d || time == 0.0d) {
                return DialogDetailsView.subZeroAndDot(String.format("%.2f", new Object[]{Double.valueOf(time)})) + " s";
            }
            int tmp = (int) ((WeightedLatLng.DEFAULT_INTENSITY / time) + 0.5d);
            return String.format("%d/%d s", new Object[]{Integer.valueOf(1), Integer.valueOf(tmp)});
        }

        private String getSetSize(Context context, Object valueObj) {
            String str = null;
            if (!(valueObj instanceof MediaDetailsTask)) {
                return null;
            }
            long fileSize;
            MediaDetailsTask task = (MediaDetailsTask) valueObj;
            task.setListener(this, this.mItems.size(), 65535);
            cancelTask();
            this.mSetSizeTask = task.submitJob();
            Object initValue = task.getInitValue();
            if (initValue != null) {
                fileSize = ((Long) initValue).longValue();
            } else {
                fileSize = 0;
            }
            if (fileSize > 0) {
                str = Formatter.formatFileSize(context, fileSize);
            }
            return str;
        }

        private void setProfessionalCameraInfoTitle(Context context, int key, String value, int drawId, String titleShort, String title) {
            SparseArray sparseArray;
            switch (key) {
                case 109:
                    sparseArray = this.mDetailItems;
                    if (titleShort != null) {
                        title = titleShort;
                    }
                    sparseArray.put(key, new DetailItem(title, value, drawId));
                    return;
                case 110:
                case 111:
                    if (drawId != 0) {
                        sparseArray = this.mDetailItems;
                        if (titleShort != null) {
                            title = titleShort;
                        }
                        sparseArray.put(key, new DetailItem(title, value, drawId));
                        return;
                    }
                    return;
                case 112:
                    String v = getMakeNoteValue(context, key, value);
                    if (v != null) {
                        sparseArray = this.mDetailItems;
                        if (titleShort != null) {
                            title = titleShort;
                        }
                        sparseArray.put(key, new DetailItem(title, v, drawId));
                        return;
                    }
                    return;
                default:
                    if (this.mCust == null || !this.mCust.setDetails(this.mItems, title, value, key)) {
                        this.mItems.add(new DetailItem(title, value));
                    }
                    sparseArray = this.mDetailItems;
                    if (titleShort != null) {
                        title = titleShort;
                    }
                    sparseArray.put(key, new DetailItem(title, value, drawId));
                    return;
            }
        }

        private int getFlashIconDrawableId(int state) {
            switch (state) {
                case 1:
                    return R.drawable.ic_btn_flash_on_detail;
                case 9:
                    return R.drawable.ic_btn_flash_on_detail;
                case 24:
                case 25:
                    return R.drawable.ic_btn_flash_auto_detail;
                case 31:
                    return R.drawable.ic_btn_flash_always_on_detail;
                case 137:
                    return R.drawable.ic_btn_flash_always_on_detail;
                default:
                    if ((state & 1) == 0) {
                        return R.drawable.ic_btn_flash_off_detail;
                    }
                    return 0;
            }
        }

        private int getLightSourceIconDrawableId(Short value) {
            try {
                switch (value.shortValue()) {
                    case (short) 1:
                    case (short) 13:
                        return R.drawable.ic_btn_wb_sunny;
                    case (short) 2:
                    case (short) 14:
                        return R.drawable.ic_btn_wb_fluorescent;
                    case (short) 3:
                    case (short) 15:
                        return R.drawable.ic_btn_wb_filament;
                    case (short) 10:
                    case (short) 12:
                        return R.drawable.ic_btn_wb_cloudy;
                    default:
                        return 0;
                }
            } catch (NumberFormatException e) {
                GalleryLog.i("DialogDetailsView", "Short.valueOf() failed in setDetails() method, switch case: INDEX_LIGHT_SOURCE.");
                return 0;
            }
        }

        private int getMeteringModeIconDrawableId(Short value) {
            try {
                switch (value.shortValue()) {
                    case (short) 1:
                    case (short) 5:
                        return R.drawable.ic_btn_square_measure;
                    case (short) 2:
                        return R.drawable.ic_btn_center_measure;
                    case (short) 3:
                        return R.drawable.ic_btn_point_measure;
                    default:
                        return 0;
                }
            } catch (NumberFormatException e) {
                GalleryLog.i("DialogDetailsView", "Short.valueOf() failed in setDetails() method, switch case: INDEX_METERING_MODE.");
                return 0;
            }
        }

        private String getMakeNoteValue(Context context, int key, String value) {
            if (GalleryUtils.findString("Auto", value)) {
                return context.getString(R.string.focus_model_auto);
            }
            if (GalleryUtils.findString("AF_MF", value)) {
                return context.getString(R.string.focus_model_mf);
            }
            if (GalleryUtils.findString("AF_S", value)) {
                return context.getString(R.string.focus_model_s);
            }
            if (GalleryUtils.findString("AF_C", value)) {
                return context.getString(R.string.focus_model_c);
            }
            return null;
        }

        private int getLengthInteger(Object lengthObj) {
            int ret = 0;
            if (lengthObj == null) {
                return ret;
            }
            try {
                ret = Integer.valueOf(lengthObj.toString()).intValue();
            } catch (NumberFormatException e) {
            }
            return ret;
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public boolean isEnabled(int position) {
            return false;
        }

        public int getCount() {
            return this.mItems.size();
        }

        public Object getItem(int position) {
            return DialogDetailsView.this.mDetails.getDetail(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(this.mContext).inflate(R.layout.details, parent, false);
            } else {
                view = convertView;
            }
            ((TextView) view.findViewById(R.id.item_title)).setText(((DetailItem) this.mItems.get(position)).mTitle);
            ((TextView) view.findViewById(R.id.item_content)).setText(((DetailItem) this.mItems.get(position)).mContent);
            return view;
        }

        public void onAddressAvailable(String address) {
            if (address != null && address.trim().length() != 0) {
                String title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(DialogDetailsView.this.mActivity.getAndroidContext(), 4)});
                this.mItems.set(this.mLocationIndex, new DetailItem(title, address));
                this.mDetailItems.put(4, new DetailItem(title, address));
                if (DialogDetailsView.this.mDetailLayout != null) {
                    DialogDetailsView.this.mDetailLayout.setDetails(this.mDetailItems, false);
                }
                notifyDataSetChanged();
            }
        }

        public void onDetailsChange(int index, int key, String value) {
            if (value != null && value.trim().length() != 0) {
                String title = String.format("%s: ", new Object[]{DetailsHelper.getDetailsName(this.mContext, key)});
                this.mItems.set(index, new DetailItem(title, value));
                this.mDetailItems.put(index, new DetailItem(title, value));
                notifyDataSetChanged();
            }
        }
    }

    public DialogDetailsView(GalleryContext activity, DetailsSource source) {
        this.mActivity = activity;
        this.mSource = source;
    }

    public DialogDetailsView(GalleryContext activity, DetailsSource source, DetailLayout view) {
        this.mActivity = activity;
        this.mSource = source;
        this.mDetailLayout = view;
    }

    public void show() {
        reloadDetails();
        if (this.mDetailLayout != null) {
            this.mDetailLayout.setVisibility(0);
        } else if (this.mDialog != null) {
            this.mDialog.show();
        }
    }

    public void hide() {
        if (this.mDetailLayout != null) {
            this.mDetailLayout.setVisibility(8);
        } else if (this.mDialog != null) {
            this.mDialog.dismiss();
        }
    }

    public void reloadDetails() {
        int index = this.mSource.setIndex();
        if (index != -1) {
            MediaDetails details = this.mSource.getDetails();
            if (details != null && (this.mIndex != index || this.mDetails != details)) {
                this.mIndex = index;
                this.mDetails = details;
                setDetails(details);
            }
        }
    }

    private void setDetails(MediaDetails details) {
        if (this.mDetailLayout == null) {
            this.mDialog = new Builder(this.mActivity.getActivityContext(), GalleryUtils.getAlertDialogThemeID(this.mActivity.getActivityContext())).setTitle(this.mActivity.getAndroidContext().getString(R.string.details)).setPositiveButton(R.string.close, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    DialogDetailsView.this.mDialog.dismiss();
                }
            }).create();
            this.mDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    if (DialogDetailsView.this.mListener != null) {
                        DialogDetailsView.this.mListener.onClose();
                    }
                    if (DialogDetailsView.this.mAdapter != null) {
                        DialogDetailsView.this.mAdapter.cancelTask();
                    }
                }
            });
            ListView detailsList = (ListView) this.mDialog.getLayoutInflater().inflate(R.layout.details_list, null, false);
            this.mAdapter = new DetailsAdapter(details, this.mDialog.getContext());
            detailsList.setAdapter(this.mAdapter);
            int padding = this.mActivity.getAndroidContext().getResources().getDimensionPixelSize(R.dimen.alter_dialog_padding_left_right);
            this.mDialog.setView(detailsList, padding, 0, padding, 0);
            return;
        }
        this.mAdapter = new DetailsAdapter(details, this.mDetailLayout.getContext());
        this.mDetailLayout.setDetails(this.mAdapter.getDetailItems());
    }

    public void setCloseListener(CloseListener listener) {
        this.mListener = listener;
    }

    private static String subZeroAndDot(String s) {
        if (s.indexOf(".") > 0) {
            return s.replaceAll("0+?$", "").replaceAll("[.]$", "");
        }
        return s;
    }
}
