package com.huawei.systemmanager.adblock.ui.connect.request;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.rainbow.comm.request.AbsServerStreamRequest;
import com.huawei.systemmanager.rainbow.comm.request.ICommonRequest.RequestType;
import com.huawei.systemmanager.util.HwLog;
import java.io.InputStream;
import org.json.JSONObject;

public class DlAppIconRequest extends AbsServerStreamRequest {
    private static final String TAG = "AdBlock_DlAppIconRequest";
    private Drawable mIcon;
    private final String mUrl;

    public DlAppIconRequest(String url) {
        this.mUrl = url;
        setNeedDefaultParam(false);
    }

    protected RequestType getRequestType() {
        return RequestType.REQUEST_GET;
    }

    protected String getRequestUrl(RequestType type) {
        return this.mUrl;
    }

    protected void addExtPostRequestParam(Context ctx, JSONObject param) {
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    protected int checkResponseCode(Context ctx, int resultCode) {
        return 0;
    }

    protected boolean parseResponseAndPost(Context ctx, InputStream inputStream) {
        HwLog.d(TAG, "parseResponseAndPost");
        return saveToCache(ctx, inputStream);
    }

    private boolean saveToCache(Context context, InputStream inputStream) {
        boolean z;
        try {
            this.mIcon = new BitmapDrawable(context.getResources(), BitmapFactory.decodeStream(inputStream));
            z = true;
            return z;
        } catch (RuntimeException e) {
            z = TAG;
            HwLog.e(z, "saveToCache", e);
            return false;
        } finally {
            Closeables.close(inputStream);
        }
    }
}
