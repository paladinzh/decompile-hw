package com.huawei.rcs.utils.map.abs;

import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.SystemClock;
import android.view.View;
import com.android.mms.attachment.datamodel.data.AttachmentSelectData;
import com.android.mms.attachment.datamodel.data.AttachmentSelectLocation;
import com.android.mms.attachment.ui.mediapicker.MediaPicker;
import com.android.mms.ui.ComposeMessageFragment.MapClickCallback;
import com.android.rcs.ui.RcsGroupChatComposeMessageFragment.RcsGroupChatMapClickCallback;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.ui.MmsEmuiActionBar;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RcsMapFragment extends HwBaseFragment implements MapClickCallback, RcsGroupChatMapClickCallback {
    private AbstractEmuiActionBar mActionbar;
    protected MediaPicker mMediaPicker;

    public static class AddressData {
        public double latitude;
        public double longitude;
        public String subTitle;
        public String title;
    }

    public void showFullView() {
    }

    public void hintFullView() {
    }

    public void setMediaPicker(MediaPicker mediaPicker) {
        this.mMediaPicker = mediaPicker;
    }

    public void setActionbar(AbstractEmuiActionBar actionBar) {
        this.mActionbar = actionBar;
    }

    public AbstractEmuiActionBar getActionbar() {
        return this.mActionbar;
    }

    public void okClick() {
    }

    public void updateActionBar(AbstractEmuiActionBar actionBar) {
    }

    public boolean isNetworkAvailable() {
        NetworkInfo netInformation = ((ConnectivityManager) getContext().getSystemService("connectivity")).getActiveNetworkInfo();
        if (netInformation != null && netInformation.isAvailable() && netInformation.isConnected()) {
            return true;
        }
        return false;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    public boolean chekApkExist(String name) {
        if (name == null || "".equals(name)) {
            return false;
        }
        try {
            getActivity().getPackageManager().getApplicationInfo(name, 128);
            return true;
        } catch (NameNotFoundException e) {
            MLog.e("RcsMapFragment", "chekApkExist: getApplicationInfo failed");
            return false;
        }
    }

    protected AbstractEmuiActionBar createEmuiActionBar(View fragmentRootView) {
        fragmentRootView.findViewById(R.id.rcs_map_action_bar).setVisibility(0);
        return new MmsEmuiActionBar(getActivity(), fragmentRootView.findViewById(R.id.rcs_map_action_bar), null);
    }

    protected void showActionbar(boolean isShow) {
        if (((this.mMediaPicker != null && this.mMediaPicker.isFullScreen()) || isInLandscape()) && getActionbar() != null) {
            getActionbar().setEndIcon(isShow);
        }
    }

    private AttachmentSelectData createAttachmentData(Uri uriItem, AddressData lookAddress) {
        AttachmentSelectData attachmentItem = new AttachmentSelectLocation(8, lookAddress.title, lookAddress.subTitle, "" + lookAddress.latitude, "" + lookAddress.longitude);
        attachmentItem.setAttachmentUri(uriItem);
        return attachmentItem;
    }

    protected void saveSnapshotPic(final Bitmap bitmap, final AddressData lookaAddressData) {
        new Thread(new Runnable() {
            public void run() {
                Uri imgUri;
                Throwable th;
                File file = new File(RcsMapFragment.this.getActivity().getCacheDir(), SystemClock.elapsedRealtime() + ".jpg");
                FileOutputStream fileOutputStream = null;
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    try {
                        if (bitmap.compress(CompressFormat.PNG, 100, out)) {
                            out.flush();
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e) {
                                MLog.e("RcsMapFragment", "saveSnapshotPic: failed close the stream");
                            }
                        }
                        fileOutputStream = out;
                    } catch (FileNotFoundException e2) {
                        fileOutputStream = out;
                        MLog.e("RcsMapFragment", "saveSnapshotPic: filenotfount");
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e3) {
                                MLog.e("RcsMapFragment", "saveSnapshotPic: failed close the stream");
                            }
                        }
                        imgUri = Uri.fromFile(file);
                        if (RcsMapFragment.this.mMediaPicker != null) {
                            RcsMapFragment.this.mMediaPicker.dispatchItemsSelected(RcsMapFragment.this.createAttachmentData(imgUri, lookaAddressData), true);
                        }
                    } catch (IOException e4) {
                        fileOutputStream = out;
                        try {
                            MLog.e("RcsMapFragment", "saveSnapshotPic: io error");
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e5) {
                                    MLog.e("RcsMapFragment", "saveSnapshotPic: failed close the stream");
                                }
                            }
                            imgUri = Uri.fromFile(file);
                            if (RcsMapFragment.this.mMediaPicker != null) {
                                RcsMapFragment.this.mMediaPicker.dispatchItemsSelected(RcsMapFragment.this.createAttachmentData(imgUri, lookaAddressData), true);
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e6) {
                                    MLog.e("RcsMapFragment", "saveSnapshotPic: failed close the stream");
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fileOutputStream = out;
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e7) {
                    MLog.e("RcsMapFragment", "saveSnapshotPic: filenotfount");
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    imgUri = Uri.fromFile(file);
                    if (RcsMapFragment.this.mMediaPicker != null) {
                        RcsMapFragment.this.mMediaPicker.dispatchItemsSelected(RcsMapFragment.this.createAttachmentData(imgUri, lookaAddressData), true);
                    }
                } catch (IOException e8) {
                    MLog.e("RcsMapFragment", "saveSnapshotPic: io error");
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    imgUri = Uri.fromFile(file);
                    if (RcsMapFragment.this.mMediaPicker != null) {
                        RcsMapFragment.this.mMediaPicker.dispatchItemsSelected(RcsMapFragment.this.createAttachmentData(imgUri, lookaAddressData), true);
                    }
                }
                imgUri = Uri.fromFile(file);
                if (RcsMapFragment.this.mMediaPicker != null) {
                    RcsMapFragment.this.mMediaPicker.dispatchItemsSelected(RcsMapFragment.this.createAttachmentData(imgUri, lookaAddressData), true);
                }
            }
        }).start();
    }
}
