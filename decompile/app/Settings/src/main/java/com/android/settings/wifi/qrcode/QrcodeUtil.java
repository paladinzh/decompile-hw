package com.android.settings.wifi.qrcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.huawei.android.net.wifi.NfcWifiManagerEx;
import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.EncodeHintType;
import com.huawei.zxing.WriterException;
import com.huawei.zxing.common.BitMatrix;
import com.huawei.zxing.qrcode.QRCodeWriter;
import java.util.Hashtable;
import java.util.List;

public class QrcodeUtil {
    private Context mContext;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    QrcodeUtil.this.showQrcodeBitmap();
                    return;
                default:
                    return;
            }
        }
    };
    private ImageView mImageView;
    private Bitmap mQrcodeBitmap;
    private String mSsid = null;
    private WifiManager mWifiManager;

    public QrcodeUtil(Context context, String ssid, View mView, WifiManager wifiManager) {
        this.mContext = context;
        this.mSsid = ssid;
        this.mImageView = (ImageView) mView.findViewById(2131887484);
        this.mWifiManager = wifiManager;
    }

    public static Bitmap createQRCodeBitmap(String str, int width, int height) throws WriterException {
        Hashtable<EncodeHintType, Object> hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.MARGIN, Integer.valueOf(0));
        BitMatrix bitMatrix = new QRCodeWriter().encode(str, BarcodeFormat.QR_CODE, width, height, hints);
        int[] pixels = new int[(width * height)];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitMatrix.get(x, y)) {
                    pixels[(y * width) + x] = -16777216;
                } else {
                    pixels[(y * width) + x] = -1;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public void getQrcodeBitmap() {
        new Thread(new Runnable() {
            public void run() {
                String str = String.format("WIFI:T:%s;S:%s;P:%s;;", new Object[]{QrcodeUtil.this.getSecurity(), QrcodeUtil.this.mSsid, QrcodeUtil.this.getPsk()});
                try {
                    int width = QrcodeUtil.this.mContext.getResources().getDimensionPixelSize(2131558664);
                    QrcodeUtil.this.mQrcodeBitmap = QrcodeUtil.createQRCodeBitmap(str, width, width);
                    QrcodeUtil.this.mHandler.sendEmptyMessage(1);
                } catch (WriterException e) {
                    e.printStackTrace();
                    Log.e("QrcodeUtil", "qrcode bitmap exception!");
                }
            }
        }).start();
    }

    public String getSecurity() {
        List<ScanResult> results = this.mWifiManager.getScanResults();
        if (results != null) {
            for (ScanResult result : results) {
                if (result != null && this.mSsid.equals(result.SSID)) {
                    return getSecurityString(result);
                }
            }
        }
        return null;
    }

    public static String getSecurityString(ScanResult result) {
        if (result == null || result.capabilities == null) {
            return "nopass";
        }
        if (result.capabilities.contains("WEP")) {
            return "WEP";
        }
        if (result.capabilities.contains("PSK")) {
            return "WPA";
        }
        return "nopass";
    }

    public String getPsk() {
        String src = NfcWifiManagerEx.getWpaSuppConfig(null);
        if (src == null || !src.contains(this.mSsid)) {
            return null;
        }
        return src.substring(src.indexOf(this.mSsid) + this.mSsid.length());
    }

    public void showQrcodeBitmap() {
        if (this.mImageView != null) {
            this.mImageView.setImageBitmap(this.mQrcodeBitmap);
        }
    }

    public void forRecycle() {
        if (this.mQrcodeBitmap != null && !this.mQrcodeBitmap.isRecycled()) {
            this.mQrcodeBitmap.recycle();
            this.mQrcodeBitmap = null;
        }
    }
}
