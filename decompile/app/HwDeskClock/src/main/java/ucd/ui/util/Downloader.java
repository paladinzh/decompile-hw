package ucd.ui.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Environment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Downloader {
    protected static final String TAG = Downloader.class.getSimpleName();
    private static HashMap<String, ArrayList<Callback>> bitmapDownloadingList = new HashMap();
    private static ExecutorService cachedThreadPool = Executors.newFixedThreadPool(3);

    public interface Callback {
        void onload(Bitmap bitmap, String str);
    }

    /* renamed from: ucd.ui.util.Downloader$1 */
    class AnonymousClass1 implements Runnable {
        private final /* synthetic */ Context val$context;
        private final /* synthetic */ String val$url;

        AnonymousClass1(Context context, String str) {
            this.val$context = context;
            this.val$url = str;
        }

        public void run() {
            Bitmap bitmap = Downloader.getFitBitmap(this.val$context, this.val$url);
            if (bitmap != null) {
                ArrayList<Callback> list = (ArrayList) Downloader.bitmapDownloadingList.get(this.val$url);
                for (int i = 0; i < list.size(); i++) {
                    Callback cb1 = (Callback) list.get(i);
                    if (cb1 != null) {
                        cb1.onload(bitmap, this.val$url);
                    }
                }
                list.clear();
                Downloader.bitmapDownloadingList.remove(this.val$url);
                return;
            }
            Downloader.bitmapDownloadingList.remove(this.val$url);
        }
    }

    @SuppressLint({"DefaultLocale"})
    public static InputStream getDownloadStream(Context context, String url) {
        if (url == null || url.length() == 0) {
            return null;
        }
        if (url.toLowerCase(Locale.getDefault()).startsWith("http://") || url.toLowerCase(Locale.getDefault()).startsWith("https://")) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(6000);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                return conn.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else if (context == null) {
            return null;
        } else {
            String prex = "sdcard://";
            if (url.toLowerCase(Locale.getDefault()).startsWith(prex)) {
                try {
                    return new FileInputStream(new File(Environment.getExternalStorageDirectory(), url.substring(prex.length())));
                } catch (FileNotFoundException e2) {
                    e2.printStackTrace();
                    return null;
                }
            }
            try {
                return context.getResources().getAssets().open(url);
            } catch (FileNotFoundException e3) {
                return null;
            } catch (IOException e4) {
                e4.printStackTrace();
                return null;
            }
        }
    }

    public static void loadImg(Context context, String url, Callback cb) {
        if (bitmapDownloadingList.containsKey(url)) {
            ((ArrayList) bitmapDownloadingList.get(url)).add(cb);
            return;
        }
        ArrayList<Callback> list = new ArrayList();
        list.add(cb);
        bitmapDownloadingList.put(url, list);
        cachedThreadPool.execute(new AnonymousClass1(context, url));
    }

    private static Bitmap getFitBitmap(Context context, String url) {
        try {
            InputStream is = getDownloadStream(context, url);
            if (is == null) {
                return null;
            }
            Options options = new Options();
            options.inJustDecodeBounds = true;
            is.close();
            int w = 1920;
            int h = 1080;
            if (options.outWidth < options.outHeight) {
                w = 1080;
                h = 1920;
            }
            disposeOptions2(options, w, h);
            is = getDownloadStream(context, url);
            if (is == null) {
                return null;
            }
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            is.close();
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected static void disposeOptions2(Options options, int w, int h) {
        int[] newSize = new int[2];
        getFitSize(options.outWidth, options.outHeight, w, h, newSize);
        options.inSampleSize = (int) Math.ceil((double) ((((float) options.outWidth) * 1.0f) / ((float) newSize[0])));
        options.inJustDecodeBounds = false;
    }

    private static void getFitSize(int bw, int bh, int cw, int ch, int[] newSize) {
        if (bw == 0 || bh == 0 || cw == 0 || ch == 0) {
            newSize[0] = 1;
            newSize[1] = 1;
            return;
        }
        if ((((float) bw) * 1.0f) / ((float) bh) >= (((float) cw) * 1.0f) / ((float) ch)) {
            if (bw > cw) {
                bh = (int) (((float) bh) * ((((float) cw) * 1.0f) / ((float) bw)));
                bw = cw;
                newSize[0] = cw;
                newSize[1] = bh;
            } else {
                newSize[0] = bw;
                newSize[1] = bh;
            }
        } else if (bh > ch) {
            bh = ch;
            newSize[0] = (int) (((float) bw) * ((((float) ch) * 1.0f) / ((float) bh)));
            newSize[1] = ch;
        } else {
            newSize[0] = bw;
            newSize[1] = bh;
        }
    }
}
