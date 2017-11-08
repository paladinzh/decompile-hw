package com.fyusion.sdk.share;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
public class c extends AsyncTask<String, Void, String> {
    private static final String a = c.class.getSimpleName();
    private Bitmap b;
    private List<Pair<String, String>> c;

    protected String a(String... strArr) {
        String str = strArr[0];
        Log.d(a, "url " + str);
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (this.b == null || this.b.isRecycled()) {
            Log.d(a, "null bitmap");
            return null;
        }
        try {
            String c;
            this.b = ThumbnailUtils.extractThumbnail(this.b, 250, 250);
            this.b.compress(CompressFormat.JPEG, 100, byteArrayOutputStream);
            this.b.recycle();
            try {
                a aVar = new a(str);
                aVar.a();
                aVar.a("avatar", "avatar", byteArrayOutputStream.toByteArray());
                if (this.c != null) {
                    if (this.c.size() > 0) {
                        for (int i = 0; i < this.c.size(); i++) {
                            aVar.a((String) ((Pair) this.c.get(i)).first, (String) ((Pair) this.c.get(i)).second);
                        }
                    }
                }
                aVar.b();
                c = aVar.c();
            } catch (Throwable th) {
                th.printStackTrace();
                c = null;
            }
            if (c != null) {
                Log.d(a, "response " + c);
                try {
                    if (new JSONObject(c).getInt("success") <= 0) {
                    }
                } catch (JSONException e) {
                }
            }
            return c;
        } catch (Exception e2) {
            e2.printStackTrace();
            this.b.recycle();
            return null;
        }
    }

    protected void a(Bitmap bitmap) {
        this.b = bitmap;
    }

    protected void a(String str) {
        super.onPostExecute(str);
    }

    protected void a(List<Pair<String, String>> list) {
        this.c = list;
    }

    protected /* synthetic */ Object doInBackground(Object[] objArr) {
        return a((String[]) objArr);
    }

    protected /* synthetic */ void onPostExecute(Object obj) {
        a((String) obj);
    }
}
