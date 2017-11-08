package com.huawei.gallery.barcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import com.android.gallery3d.R;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class BarcodeDecoder {
    private static Class<?> MultiFormatReader;
    private static Class<?> ParsedResult;
    private static Class<?> Result;
    private static Class<?> ResultHandlerFactory;
    private static Class<?> ResultPoint;
    private static Class<?> barcodeFormat;
    private static Method decodeWithBitmap_method;
    private static Method encodeQRCodeContents;
    private static Class<?> encoder;
    private static Method getResultPoints_method;
    private static Method getType_method;
    private static Method getX_method;
    private static Method getY_method;
    public static final HashMap<String, Integer> mBarcodeType = new HashMap();
    private static boolean mIsSupportBarcode;
    private static Method parseResult_method;

    static {
        mIsSupportBarcode = false;
        MultiFormatReader = null;
        Result = null;
        ResultHandlerFactory = null;
        ParsedResult = null;
        ResultPoint = null;
        encoder = null;
        barcodeFormat = null;
        decodeWithBitmap_method = null;
        parseResult_method = null;
        getType_method = null;
        getResultPoints_method = null;
        getX_method = null;
        getY_method = null;
        encodeQRCodeContents = null;
        mBarcodeType.put("ADDRESSBOOK", Integer.valueOf(R.string.barcode_type_contacts));
        mBarcodeType.put("EMAIL_ADDRESS", Integer.valueOf(R.string.barcode_type_email));
        mBarcodeType.put("URI", Integer.valueOf(R.string.barcode_type_uri));
        mBarcodeType.put("TEXT", Integer.valueOf(R.string.barcode_type_text));
        mBarcodeType.put("TEL", Integer.valueOf(R.string.barcode_type_tel));
        mBarcodeType.put("SMS", Integer.valueOf(R.string.barcode_type_sms));
        mBarcodeType.put("WIFI", Integer.valueOf(R.string.barcode_type_wifi));
        mBarcodeType.put("PRODUCT", Integer.valueOf(R.string.barcode_type_product));
        mBarcodeType.put("GEO", Integer.valueOf(65535));
        mBarcodeType.put("CALENDAR", Integer.valueOf(65535));
        mBarcodeType.put("ISBN", Integer.valueOf(R.string.barcode_type_isbn));
        try {
            MultiFormatReader = Class.forName("com.huawei.zxing.MultiFormatReader");
            Result = Class.forName("com.huawei.zxing.Result");
            ResultHandlerFactory = Class.forName("com.huawei.zxing.resultdispatch.ResultHandlerFactory");
            ParsedResult = Class.forName("com.huawei.zxing.client.result.ParsedResult");
            ResultPoint = Class.forName("com.huawei.zxing.ResultPoint");
            encoder = Class.forName("com.huawei.zxing.encode.QRCodeEncoder");
            barcodeFormat = Class.forName("com.huawei.zxing.BarcodeFormat");
            decodeWithBitmap_method = MultiFormatReader.getMethod("decodeWithBitmap", new Class[]{Bitmap.class});
            parseResult_method = ResultHandlerFactory.getMethod("parseResult", new Class[]{Result});
            getType_method = ParsedResult.getMethod("getType", new Class[0]);
            getResultPoints_method = Result.getMethod("getResultPoints", new Class[0]);
            getX_method = ResultPoint.getMethod("getX", new Class[0]);
            getY_method = ResultPoint.getMethod("getY", new Class[0]);
            encodeQRCodeContents = encoder.getMethod("encodeQRCodeContents", new Class[]{Bundle.class, Bitmap.class, String.class, barcodeFormat});
            mIsSupportBarcode = true;
        } catch (ClassNotFoundException e) {
            mIsSupportBarcode = false;
        } catch (NoSuchMethodException e2) {
            mIsSupportBarcode = false;
        }
    }

    public static Bitmap encoder(Context context, Bundle bundle, String type) {
        try {
            Constructor<?> constructor = encoder.getConstructor(new Class[]{Context.class});
            return (Bitmap) encodeQRCodeContents.invoke(constructor.newInstance(new Object[]{context}), new Object[]{bundle, null, type, null});
        } catch (IllegalArgumentException e) {
            return null;
        } catch (IllegalAccessException e2) {
            return null;
        } catch (InvocationTargetException e3) {
            return null;
        } catch (InstantiationException e4) {
            return null;
        } catch (NoSuchMethodException e5) {
            return null;
        }
    }
}
