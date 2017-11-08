package com.android.mms.attachment.datamodel.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.android.mms.attachment.Factory;
import com.huawei.cspcommon.MLog;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NetworkUriImageRequest<D extends UriImageRequestDescriptor> extends ImageRequest<D> {
    public NetworkUriImageRequest(Context context, D descriptor) {
        super(context, descriptor);
        this.mOrientation = 0;
    }

    protected InputStream getInputStreamForResource() throws FileNotFoundException {
        return null;
    }

    protected boolean isGif() throws FileNotFoundException {
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(((UriImageRequestDescriptor) this.mDescriptor).uri.toString()).openConnection();
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE) {
                boolean equalsIgnoreCase = "image/gif".equalsIgnoreCase(httpURLConnection.getContentType());
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                return equalsIgnoreCase;
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            return false;
        } catch (MalformedURLException e) {
            MLog.e("NetworkUriImageRequest", "MalformedUrl for image with url: " + ((UriImageRequestDescriptor) this.mDescriptor).uri.toString(), (Throwable) e);
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (IOException e2) {
            MLog.e("NetworkUriImageRequest", "IOException trying to get inputStream for image with url: " + ((UriImageRequestDescriptor) this.mDescriptor).uri.toString(), (Throwable) e2);
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (Throwable th) {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    public Bitmap loadBitmapInternal() throws IOException {
        InputStream inputStream = null;
        Bitmap bitmap = null;
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(((UriImageRequestDescriptor) this.mDescriptor).uri.toString()).openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE) {
                inputStream = httpURLConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            MLog.e("NetworkUriImageRequest", "MalformedUrl for image with url: " + ((UriImageRequestDescriptor) this.mDescriptor).uri.toString(), (Throwable) e);
            if (inputStream != null) {
                inputStream.close();
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (OutOfMemoryError e2) {
            MLog.e("NetworkUriImageRequest", "OutOfMemoryError for image with url: " + ((UriImageRequestDescriptor) this.mDescriptor).uri.toString(), (Throwable) e2);
            Factory.get().reclaimMemory();
            if (inputStream != null) {
                inputStream.close();
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (IOException e3) {
            MLog.e("NetworkUriImageRequest", "IOException trying to get inputStream for image with url: " + ((UriImageRequestDescriptor) this.mDescriptor).uri.toString(), (Throwable) e3);
            if (inputStream != null) {
                inputStream.close();
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                inputStream.close();
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return bitmap;
    }
}
