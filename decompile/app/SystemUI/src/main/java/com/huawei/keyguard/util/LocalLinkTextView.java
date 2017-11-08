package com.huawei.keyguard.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.keyguard.R$bool;
import com.android.keyguard.R$color;
import com.android.keyguard.R$id;
import com.android.keyguard.R$layout;
import com.android.keyguard.R$string;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.support.magazine.ApkScanner;
import com.huawei.keyguard.support.magazine.BigPictureInfo;
import com.huawei.keyguard.support.magazine.MagazineUtils;
import com.huawei.keyguard.support.magazine.MagazineWallpaper;
import com.huawei.openalliance.ad.inter.constant.EventType;
import fyusion.vislib.BuildConfig;
import java.io.File;
import java.util.List;

public class LocalLinkTextView extends TextView {
    private String mDetailDescription;
    private String mDetailDescriptionBeforeAsc = BuildConfig.FLAVOR;
    private String mDetailLinkText;
    private LinkHandler mLinkHandler;
    private int mMaxCharCount = 105;
    private Runnable mResizeListener = null;
    private int mStrOrigLine = 0;
    private boolean mSupportLink = false;
    private LinkTaskWorker mWork;
    private boolean mlinkHit;

    private static abstract class LinkClickSpan extends ClickableSpan {
        private Context mContext;

        protected abstract void scanApksInBackground(String[] strArr, String str, String str2);

        protected abstract void startUrl(String str);

        LinkClickSpan(Context context) {
            this.mContext = context;
        }

        public void onClick(View widget) {
            respondLink();
            HwLockScreenReporter.reportMagazinePictureInfo(this.mContext, 108, 0);
        }

        private void respondLink() {
            BigPictureInfo info = MagazineWallpaper.getInst(this.mContext).getPictureInfo(0);
            if (info == null) {
                HwLog.w("LocalLinkTextView", "respondEntrance current picture is null");
                return;
            }
            String pkgname = info.getBigPackagename();
            String download = info.getDownload();
            String contentUrl = info.getContentUrl();
            HwLockScreenReporter.reportAdEvent(this.mContext, info, EventType.CLICK);
            if (TextUtils.isEmpty(pkgname)) {
                startUrl(contentUrl);
            } else if (HwUnlockUtils.isKeyguardAudioVideoEnable()) {
                startKeyguardAudioVideoProc(info, pkgname, contentUrl);
            } else {
                Intent intent = this.mContext.getPackageManager().getLaunchIntentForPackage(pkgname);
                if (intent == null) {
                    HwUnlockUtils.vibrate(this.mContext);
                    scanApksInBackground(((StorageManager) this.mContext.getSystemService("storage")).getVolumePaths(), pkgname, download);
                } else {
                    HwKeyguardPolicy.getInst().startActivity(intent, true);
                }
            }
        }

        private void startKeyguardAudioVideoProc(final BigPictureInfo info, final String nPkgname, final String nContentUrl) {
            new AsyncTask<Object, Void, Void>() {
                boolean appExist = false;
                String appVer = info.getAppVer().trim();
                List<PackageInfo> appcationsList = null;
                Drawable currentDrawable = MagazineUtils.getAppIcon(LinkClickSpan.this.mContext, nPkgname);
                int flag = 0;
                int localVersion = 0;
                String tipTextStr = BuildConfig.FLAVOR;
                int userId = 0;

                protected void onPostExecute(Void v) {
                    if (this.appExist) {
                        if (MagazineUtils.compareAppVersion(this.localVersion, this.appVer)) {
                            this.tipTextStr = MagazineUtils.getAppUpdateDialogText(LinkClickSpan.this.mContext, nPkgname);
                            LocalLinkTextView.showTipsAlertDialog(this.currentDrawable, this.tipTextStr, nPkgname, true, LinkClickSpan.this.mContext);
                        } else {
                            LinkClickSpan.this.goToApp(nPkgname, nContentUrl, this.appVer);
                        }
                    } else if (TextUtils.isEmpty(nContentUrl) || TextUtils.isEmpty(this.appVer)) {
                        HwLog.e("LocalLinkTextView", "startKeyguardAudioVideoProc():parameters is error !");
                    } else {
                        this.tipTextStr = MagazineUtils.getAppDownloadDialogText(LinkClickSpan.this.mContext, nPkgname);
                        LocalLinkTextView.showTipsAlertDialog(this.currentDrawable, this.tipTextStr, nPkgname, false, LinkClickSpan.this.mContext);
                    }
                }

                protected Void doInBackground(Object... params) {
                    this.localVersion = MagazineUtils.getApkVersionCode(LinkClickSpan.this.mContext, nPkgname);
                    PackageManager pm = LinkClickSpan.this.mContext.getPackageManager();
                    this.userId = OsUtils.getCurrentUser();
                    if (pm != null) {
                        this.appcationsList = pm.getInstalledPackagesAsUser(this.flag, this.userId);
                    }
                    if (this.appcationsList != null) {
                        for (PackageInfo packageInfo : this.appcationsList) {
                            if (nPkgname.equals(packageInfo.packageName)) {
                                this.appExist = true;
                                break;
                            }
                        }
                    }
                    HwLog.w("LocalLinkTextView", "appcationsList is null.");
                    return null;
                }
            }.execute(new Object[0]);
        }

        private void goToApp(String pkgname, String contentUrl, String appVer) {
            if (TextUtils.isEmpty(pkgname) || TextUtils.isEmpty(contentUrl) || TextUtils.isEmpty(appVer)) {
                HwLog.e("LocalLinkTextView", "goToApp():parameters is error!");
                return;
            }
            Uri uri = Uri.parse(contentUrl);
            Intent intent = new Intent();
            intent.setFlags(805306368);
            if (TextUtils.equals("com.huawei.himovie", pkgname)) {
                intent.setAction(pkgname + ".PUSH");
                intent.setPackage("com.huawei.himovie");
                intent.putExtra("pushtype", 2);
                intent.putExtra("vodid", contentUrl);
            } else {
                intent.setAction("android.intent.action.VIEW");
                intent.setData(uri);
            }
            try {
                HwKeyguardPolicy.getInst().startActivity(intent, true);
            } catch (ActivityNotFoundException e) {
                HwLog.e("LocalLinkTextView", "ActivityNotFoundException: Exception = " + e);
            } catch (Exception ex) {
                HwLog.e("LocalLinkTextView", "showTipsAlertDialog(): Exception = " + ex);
            }
        }
    }

    private class CustomClickSpan extends LinkClickSpan {
        CustomClickSpan() {
            super(LocalLinkTextView.this.mContext);
        }

        protected void scanApksInBackground(String[] paths, String pkgname, String download) {
            if (LocalLinkTextView.this.mWork == null) {
                LocalLinkTextView.this.mWork = new LinkTaskWorker(LocalLinkTextView.this.mContext);
            }
            if (!LocalLinkTextView.this.mWork.mIsScanningApk && paths != null && paths.length != 0) {
                LocalLinkTextView.this.mWork.mIsScanningApk = true;
                LocalLinkTextView.this.mWork.execute(paths, pkgname, download);
            }
        }

        protected void startUrl(String strUrl) {
            LocalLinkTextView.this.startUrl(strUrl);
        }

        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(LocalLinkTextView.this.getResources().getColor(R$color.magazine_detail_link_text_color));
            if (HwUnlockUtils.isKeyguardAudioVideoEnable()) {
                ds.setUnderlineText(false);
            } else {
                ds.setUnderlineText(true);
            }
        }
    }

    private class LinkHandler extends Handler {
        private LinkHandler() {
        }

        private void stopLineTextCheck() {
            removeMessages(100);
            removeMessages(101);
            removeMessages(102);
            removeMessages(103);
            removeMessages(104);
            removeMessages(106);
        }

        private void startLineTextCheck() {
            int lineCount = LocalLinkTextView.this.getLineCount();
            if (2 < lineCount) {
                sendEmptyMessage(100);
                return;
            }
            LocalLinkTextView.this.mStrOrigLine = lineCount;
            sendEmptyMessage(102);
        }

        public void handleMessage(Message msg) {
            if (LocalLinkTextView.this.mDetailDescription != null) {
                HwLog.w("LocalLinkTextView", "LocalLinkTextView Handle Message. " + msg.what);
                switch (msg.what) {
                    case 100:
                        LocalLinkTextView.this.mDetailDescription = LocalLinkTextView.this.mDetailDescription.substring(0, LocalLinkTextView.this.mDetailDescription.length() - 15);
                        LocalLinkTextView.this.setText(LocalLinkTextView.this.mDetailDescription);
                        post(new Runnable() {
                            public void run() {
                                LinkHandler.this.sendMsgDec();
                            }
                        });
                        break;
                    case 101:
                        LocalLinkTextView.this.setText(LocalLinkTextView.this.mDetailDescription + "..." + LocalLinkTextView.this.mDetailLinkText);
                        post(new Runnable() {
                            public void run() {
                                LinkHandler.this.sendMsgAsc();
                            }
                        });
                        break;
                    case 102:
                        LocalLinkTextView.this.mDetailDescriptionBeforeAsc = LocalLinkTextView.this.mDetailDescription;
                        LocalLinkTextView.this.setText(LocalLinkTextView.this.mDetailDescription + LocalLinkTextView.this.mDetailLinkText);
                        post(new Runnable() {
                            public void run() {
                                LinkHandler.this.sendMsgAscDirect();
                            }
                        });
                        break;
                    case 103:
                        setSetDetailStyle(LocalLinkTextView.this.mDetailDescription, "...", LocalLinkTextView.this.mDetailLinkText);
                        break;
                    case 104:
                        setSetDetailStyle(LocalLinkTextView.this.mDetailDescription, BuildConfig.FLAVOR, LocalLinkTextView.this.mDetailLinkText);
                        break;
                    case 106:
                        LocalLinkTextView.this.mDetailDescription = LocalLinkTextView.this.mDetailDescription.substring(0, LocalLinkTextView.this.mDetailDescription.length() - 7);
                        LocalLinkTextView.this.setText(LocalLinkTextView.this.mDetailDescription);
                        post(new Runnable() {
                            public void run() {
                                LinkHandler.this.sendMsgHalfDec();
                            }
                        });
                        break;
                }
            }
        }

        private void setSetDetailStyle(String mainDescription, String divider, String linkUrl) {
            String str = mainDescription + divider + linkUrl;
            SpannableString spanStr = new SpannableString(str);
            if (TextUtils.isEmpty(linkUrl)) {
                LocalLinkTextView.this.setMovementMethod(null);
            } else {
                LocalLinkTextView.this.setVisibility(0);
                spanStr.setSpan(new CustomClickSpan(), str.indexOf(linkUrl), (str.indexOf(linkUrl) + linkUrl.length()) - 1, 33);
                LocalLinkTextView.this.setText(spanStr);
                LocalLinkTextView.this.setMovementMethod(LocalLinkMovementMethod.getInstance());
            }
            LocalLinkTextView.this.noticeTextChanged();
            HwLog.w("LocalLinkTextView", "LocalLinkTextView setSetDetailStyle " + LocalLinkTextView.this.mResizeListener + "  " + str);
        }

        private void sendMsgDec() {
            if (2 < LocalLinkTextView.this.getLineCount()) {
                sendEmptyMessage(100);
            } else {
                sendEmptyMessage(101);
            }
        }

        private void sendMsgHalfDec() {
            if (2 < LocalLinkTextView.this.getLineCount()) {
                sendEmptyMessage(106);
            } else {
                sendEmptyMessage(101);
            }
        }

        private void sendMsgAscDirect() {
            int lineCount = LocalLinkTextView.this.getLineCount();
            if (LocalLinkTextView.this.mStrOrigLine == lineCount || 2 >= lineCount) {
                LocalLinkTextView.this.mDetailDescription = LocalLinkTextView.this.mDetailDescriptionBeforeAsc;
                sendEmptyMessage(104);
                return;
            }
            LocalLinkTextView.this.mDetailDescription = LocalLinkTextView.this.mDetailDescriptionBeforeAsc;
            sendEmptyMessage(106);
        }

        private void sendMsgAsc() {
            if (2 < LocalLinkTextView.this.getLineCount()) {
                sendEmptyMessage(106);
            } else {
                sendEmptyMessage(103);
            }
        }
    }

    private class LinkTaskWorker extends ApkScanner {
        private LinkTaskWorker(Context context) {
            super(context);
        }

        protected OnClickListener getDialogListener(final boolean isDownloaded, final String apkPath, final String urlPath) {
            return new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (isDownloaded) {
                        LocalLinkTextView.this.startInstallApk(apkPath);
                    } else {
                        LocalLinkTextView.this.startUrl(urlPath);
                    }
                }
            };
        }
    }

    public static class LocalLinkMovementMethod extends LinkMovementMethod {
        private static LocalLinkMovementMethod sInstance = null;

        public static synchronized LocalLinkMovementMethod getInstance() {
            LocalLinkMovementMethod localLinkMovementMethod;
            synchronized (LocalLinkMovementMethod.class) {
                if (sInstance == null) {
                    sInstance = new LocalLinkMovementMethod();
                }
                localLinkMovementMethod = sInstance;
            }
            return localLinkMovementMethod;
        }

        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            int action = event.getAction();
            if (action != 1 && action != 0) {
                return Touch.onTouchEvent(widget, buffer, event);
            }
            int x = (((int) event.getX()) - widget.getTotalPaddingLeft()) + widget.getScrollX();
            int y = (((int) event.getY()) - widget.getTotalPaddingTop()) + widget.getScrollY();
            Layout layout = widget.getLayout();
            int off = layout.getOffsetForHorizontal(layout.getLineForVertical(y), (float) x);
            ClickableSpan[] link = (ClickableSpan[]) buffer.getSpans(off, off, ClickableSpan.class);
            if (link.length != 0) {
                if (action == 1) {
                    link[0].onClick(widget);
                } else if (action == 0) {
                    Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
                }
                if (widget instanceof LocalLinkTextView) {
                    ((LocalLinkTextView) widget).mlinkHit = true;
                }
                return true;
            }
            Selection.removeSelection(buffer);
            Touch.onTouchEvent(widget, buffer, event);
            return false;
        }
    }

    public LocalLinkTextView(Context context) {
        super(context);
    }

    public LocalLinkTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LocalLinkTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getResources().getBoolean(R$bool.kg_tablet_enable)) {
            this.mMaxCharCount = 210;
        }
        this.mDetailLinkText = getResources().getString(R$string.emui30_keyguard_other_imagedetail_details) + " ";
        this.mLinkHandler = new LinkHandler();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mSupportLink) {
            return super.onTouchEvent(event);
        }
        this.mlinkHit = false;
        super.onTouchEvent(event);
        return this.mlinkHit;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mWork != null) {
            this.mWork.resetWorkState();
        }
    }

    private void setMaxShowLines(int max) {
        if (max != getMaxLines()) {
            HwLog.e("LocalLinkTextView", "setDescription setMaxShowLines line is : " + max);
            setMaxLines(max);
        }
    }

    public void setDescription(String detail, String worksDes, boolean useLink) {
        this.mSupportLink = useLink;
        this.mLinkHandler.stopLineTextCheck();
        if (useLink) {
            HwLog.e("LocalLinkTextView", "setDescription with uselink line!");
            setMaxShowLines(3);
            this.mDetailDescriptionBeforeAsc = BuildConfig.FLAVOR;
            if (this.mMaxCharCount < detail.length()) {
                detail = detail.substring(0, this.mMaxCharCount);
            }
            this.mDetailDescription = detail;
            if (!TextUtils.isEmpty(worksDes)) {
                this.mDetailLinkText = worksDes + " ";
            } else if (HwUnlockUtils.isKeyguardAudioVideoEnable()) {
                this.mDetailLinkText = getResources().getString(R$string.keyguard_description_more_detail) + " ";
            } else {
                this.mDetailLinkText = getResources().getString(R$string.emui30_keyguard_other_imagedetail_details) + " ";
            }
            setVisibility(4);
            setText(this.mDetailDescription);
            post(new Runnable() {
                public void run() {
                    LocalLinkTextView.this.mLinkHandler.startLineTextCheck();
                }
            });
            return;
        }
        HwLog.e("LocalLinkTextView", "setDescription without uselink line!");
        setMovementMethod(null);
        setMaxShowLines(2);
        setText(detail);
        noticeTextChanged();
    }

    private void startUrl(String strUrl) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(Uri.parse(strUrl));
        intent.putExtra("magazineunlock", true);
        HwKeyguardPolicy.getInst().startActivity(intent, true);
    }

    private void startInstallApk(String path) {
        if (path == null) {
            HwLog.e("LocalLinkTextView", "startInstallApk path is null");
            return;
        }
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
        intent.addFlags(1);
        intent.addFlags(268435456);
        HwKeyguardPolicy.getInst().startActivity(intent, true);
    }

    public void setResizeListener(Runnable run) {
        this.mResizeListener = run;
    }

    private static void showTipsAlertDialog(Drawable currentIcon, String text, final String packageName, boolean isNeedUpdate, Context context) {
        String keyguardUpdateOrDownloadText = BuildConfig.FLAVOR;
        if (!TextUtils.isEmpty(text) && currentIcon != null && packageName != null) {
            if (isNeedUpdate) {
                keyguardUpdateOrDownloadText = context.getString(R$string.keyguard_goto_update);
            } else {
                keyguardUpdateOrDownloadText = context.getString(R$string.keyguard_goto_download);
            }
            ContextThemeWrapper wContext = MagazineUtils.getHwThemeContext(context, "androidhwext:style/Theme.Emui.Dialog");
            if (wContext != null) {
                View tipsDialogView = LayoutInflater.from(wContext).inflate(R$layout.magazine_download_update_dialog_view, null);
                ImageView tipIcon = (ImageView) tipsDialogView.findViewById(R$id.magzine_icom_tip);
                ((TextView) tipsDialogView.findViewById(R$id.magazine_text_tip)).setText(text);
                tipIcon.setImageDrawable(currentIcon);
                AlertDialog alertDialog = new Builder(wContext).setView(tipsDialogView).setNegativeButton(R$string.keyguard_download_update_cancle, null).setPositiveButton(keyguardUpdateOrDownloadText, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent mIntent = new Intent();
                        mIntent.addFlags(268468224);
                        mIntent.setAction("com.huawei.appmarket.intent.action.AppDetail");
                        mIntent.putExtra("APP_PACKAGENAME", packageName);
                        mIntent.setPackage("com.huawei.appmarket");
                        try {
                            HwKeyguardPolicy.getInst().startActivity(mIntent, true);
                        } catch (ActivityNotFoundException e) {
                            HwLog.e("LocalLinkTextView", "ActivityNotFoundException: appmarket is not found! Exception=" + e);
                        } catch (Exception ex) {
                            HwLog.e("LocalLinkTextView", "showTipsAlertDialog(): Exception = " + ex);
                        }
                    }
                }).create();
                alertDialog.getWindow().setType(2009);
                alertDialog.show();
            }
        }
    }

    private void noticeTextChanged() {
        this.mLinkHandler.postDelayed(new Runnable() {
            public void run() {
                if (LocalLinkTextView.this.mResizeListener != null) {
                    LocalLinkTextView.this.mResizeListener.run();
                }
            }
        }, 10);
    }
}
