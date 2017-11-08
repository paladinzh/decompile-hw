package com.fyusion.sdk.ext.shareinterface;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.FyuseSDKException;
import com.fyusion.sdk.common.ext.util.FyuseUtils;
import com.fyusion.sdk.share.FyuseShare;
import com.fyusion.sdk.share.ShareListener;
import com.fyusion.sdk.viewer.FyuseException;
import com.fyusion.sdk.viewer.FyuseViewer;
import com.fyusion.sdk.viewer.RequestListener;
import com.fyusion.sdk.viewer.ext.localfyuse.LocalFyuseView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class FyuseShareActivity extends Activity {
    private Uri a;
    private LocalFyuseView b;
    private ProgressBar c;
    private EditText d;
    private GridView e;
    private b f;
    private boolean g = true;
    private boolean h = false;
    private boolean i = false;
    private boolean j = false;

    /* compiled from: Unknown */
    interface a {
        void a(b bVar);
    }

    /* compiled from: Unknown */
    class b {
        String a;
        String b;
        String c;
        Drawable d;
        final /* synthetic */ FyuseShareActivity e;

        b(FyuseShareActivity fyuseShareActivity, String str, String str2, String str3, Drawable drawable) {
            this.e = fyuseShareActivity;
            this.a = str;
            this.b = str2;
            this.c = str3;
            this.d = drawable;
        }
    }

    /* compiled from: Unknown */
    class c extends BaseAdapter {
        final /* synthetic */ FyuseShareActivity a;
        private List<b> b;
        private LayoutInflater c;
        private a d;

        private c(FyuseShareActivity fyuseShareActivity, Context context, List<b> list, a aVar) {
            this.a = fyuseShareActivity;
            this.b = list;
            this.c = (LayoutInflater) context.getSystemService("layout_inflater");
            this.d = aVar;
        }

        public b a(int i) {
            return (b) this.b.get(i);
        }

        public int getCount() {
            return this.b.size();
        }

        public /* synthetic */ Object getItem(int i) {
            return a(i);
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = this.c.inflate(R.layout.share_interface_service_item, viewGroup, false);
            }
            final b a = a(i);
            ImageView imageView = (ImageView) view.findViewById(R.id.service_icon);
            ((TextView) view.findViewById(R.id.service_label)).setText(a.c);
            imageView.setImageDrawable(a.d);
            view.setOnClickListener(new OnClickListener(this) {
                final /* synthetic */ c b;

                public void onClick(View view) {
                    this.b.d.a(a);
                }
            });
            return view;
        }
    }

    private static int a(float f, Context context) {
        return (int) ((((float) context.getResources().getDisplayMetrics().densityDpi) / 160.0f) * f);
    }

    private Uri a(Uri uri) {
        Cursor query;
        Throwable th;
        Cursor cursor = null;
        if ("file".equals(uri.getScheme())) {
            return uri;
        }
        try {
            query = getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
            if (query != null) {
                try {
                    if (query.moveToFirst()) {
                        File file = new File(query.getString(0));
                        if (file.exists()) {
                            Uri fromFile = Uri.fromFile(file);
                            if (query != null) {
                                query.close();
                            }
                            return fromFile;
                        }
                    }
                } catch (Exception e) {
                    try {
                        DLog.e("FSI", "Was not able to get the Uri from the cursor");
                        if (query != null) {
                            query.close();
                        }
                        return uri;
                    } catch (Throwable th2) {
                        cursor = query;
                        th = th2;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
            }
            if (query != null) {
                query.close();
            }
        } catch (Exception e2) {
            query = null;
            DLog.e("FSI", "Was not able to get the Uri from the cursor");
            if (query != null) {
                query.close();
            }
            return uri;
        } catch (Throwable th3) {
            th = th3;
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        return uri;
    }

    private void a() {
        String str = "com.fyusion.fyuse";
        try {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=com.fyusion.fyuse")).addFlags(268435456));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=com.fyusion.fyuse")).addFlags(268435456));
        }
    }

    private void a(final String str) {
        View inflate = getLayoutInflater().inflate(R.layout.dialog_content, null);
        Builder builder = new Builder(new ContextThemeWrapper(this, 16974120));
        String string = getResources().getString(R.string.m_UPLOADED_FYUSE);
        Object[] objArr = new Object[1];
        String str2 = (this.d != null && this.d.getText().length() > 0) ? "\"" + this.d.getText() + "\"" : "";
        objArr[0] = str2;
        builder.setMessage(String.format(string, objArr).replace("  ", " "));
        builder.setView(inflate).setPositiveButton(getResources().getString(R.string.m_OPEN_LINK_TITLE).replace("?", ""), new DialogInterface.OnClickListener(this) {
            final /* synthetic */ FyuseShareActivity b;

            public void onClick(DialogInterface dialogInterface, int i) {
                this.b.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://fyu.se/v/" + str)));
                this.b.d();
            }
        });
        builder.setNegativeButton(R.string.m_CLOSE, new DialogInterface.OnClickListener(this) {
            final /* synthetic */ FyuseShareActivity a;

            {
                this.a = r1;
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                this.a.d();
            }
        });
        builder.setOnCancelListener(new OnCancelListener(this) {
            final /* synthetic */ FyuseShareActivity a;

            {
                this.a = r1;
            }

            public void onCancel(DialogInterface dialogInterface) {
                this.a.d();
            }
        });
        AlertDialog create = builder.create();
        create.show();
        ((TextView) create.findViewById(16908299)).setTextSize(14.0f);
    }

    private void b() {
        Button button = (Button) findViewById(R.id.shareBtn);
        button.setText(getResources().getText(R.string.m_POST));
        this.c.setProgress(0);
        this.c.setVisibility(8);
        button.setOnClickListener(new OnClickListener(this) {
            final /* synthetic */ FyuseShareActivity a;

            {
                this.a = r1;
            }

            public void onClick(View view) {
                if (FyuseUtils.isFyuseProcessed(FyuseShareInterface.a(this.a, this.a.a))) {
                    this.a.i();
                    this.a.c();
                }
            }
        });
    }

    private void b(String str) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/plain");
        intent.setPackage(this.f.b);
        intent.putExtra("android.intent.extra.TEXT", "https://fyu.se/v/" + str);
        startActivity(Intent.createChooser(intent, "Share Fyuse to.."));
    }

    private void c() {
        Button button = (Button) findViewById(R.id.shareBtn);
        button.setText(getResources().getText(R.string.m_CANCEL));
        button.setOnClickListener(new OnClickListener(this) {
            final /* synthetic */ FyuseShareActivity a;

            {
                this.a = r1;
            }

            public void onClick(View view) {
                this.a.e();
                this.a.b();
            }
        });
    }

    private void d() {
        setResult(-1);
        finish();
    }

    private void e() {
        if (this.a != null) {
            FyuseShare.cancelShare(a(this.a));
        }
    }

    private void f() {
        e();
        setResult(0);
        finish();
    }

    private void g() {
        if (this.a != null) {
            FyuseShare.cancelShare(a(this.a));
        }
        setResult(1);
        finish();
    }

    private void h() throws FyuseSDKException {
        float f = 56.0f;
        if (this.b != null) {
            int i;
            File a = FyuseShareInterface.a(this, this.a);
            boolean isFyuseProcessed = FyuseUtils.isFyuseProcessed(a);
            Display defaultDisplay = getWindowManager().getDefaultDisplay();
            Point point = new Point();
            defaultDisplay.getSize(point);
            int i2 = point.y;
            int i3 = point.x;
            if (i3 <= i2) {
                i = 0;
            } else {
                boolean z = true;
            }
            final ImageView imageView = (ImageView) findViewById(R.id.fv_image_view);
            if (imageView != null) {
                Bitmap thumbnail = FyuseUtils.getThumbnail(a);
                if (thumbnail != null) {
                    int i4;
                    if (thumbnail.getWidth() <= thumbnail.getHeight()) {
                        i4 = 0;
                    } else {
                        boolean z2 = true;
                    }
                    LayoutParams layoutParams = (LayoutParams) this.b.getLayoutParams();
                    int a2;
                    if (i4 == 0) {
                        if (i == 0) {
                            f = 72.0f;
                        }
                        a2 = a(f, (Context) this);
                        i4 = i2 - (a2 * 3);
                        i = (i4 * 9) / 16;
                        layoutParams.setMargins(0, a2, 0, 0);
                    } else {
                        a2 = a(i == 0 ? 192.0f : 56.0f, (Context) this);
                        if (i == 0) {
                            i = i3 - (a(24.0f, (Context) this) * 2);
                            i4 = (i * 9) / 16;
                        } else {
                            i4 = i2 - (a2 * 3);
                            i = (i4 * 16) / 9;
                        }
                        layoutParams.setMargins(0, a2, 0, 0);
                    }
                    if (!isFyuseProcessed) {
                        imageView.setVisibility(0);
                        imageView.setImageBitmap(thumbnail);
                        imageView.setLayoutParams(layoutParams);
                    }
                    layoutParams.width = i;
                    layoutParams.height = i4;
                    this.b.setLayoutParams(layoutParams);
                }
            }
            FyuseViewer.with((Activity) this).load(a).highRes(true).listener(new RequestListener(this) {
                final /* synthetic */ FyuseShareActivity b;

                public boolean onLoadFailed(@Nullable FyuseException fyuseException, Object obj) {
                    return true;
                }

                public void onProgress(int i) {
                    if (imageView != null) {
                        this.b.runOnUiThread(new Runnable(this) {
                            final /* synthetic */ AnonymousClass2 a;

                            {
                                this.a = r1;
                            }

                            public void run() {
                                imageView.setVisibility(4);
                            }
                        });
                    }
                }

                public boolean onResourceReady(Object obj) {
                    if (imageView != null) {
                        this.b.runOnUiThread(new Runnable(this) {
                            final /* synthetic */ AnonymousClass2 a;

                            {
                                this.a = r1;
                            }

                            public void run() {
                                imageView.setVisibility(4);
                            }
                        });
                    }
                    return false;
                }
            }).into(this.b);
        }
    }

    private void i() {
        try {
            FyuseShare share = FyuseShare.init().setShareListener(new ShareListener(this) {
                final /* synthetic */ FyuseShareActivity a;

                {
                    this.a = r1;
                }

                public void onError(Exception exception) {
                    this.a.g();
                }

                public void onProgress(int i) {
                    if (this.a.c.getVisibility() != 0) {
                        this.a.runOnUiThread(new Runnable(this) {
                            final /* synthetic */ AnonymousClass3 a;

                            {
                                this.a = r1;
                            }

                            public void run() {
                                this.a.a.c.setVisibility(0);
                            }
                        });
                    }
                    this.a.c.setProgress(i);
                }

                public void onSuccess(String str) {
                    if (this.a.f != null) {
                        this.a.b(str);
                    }
                    if (this.a.h) {
                        this.a.d();
                    } else {
                        this.a.a(str);
                    }
                }

                public void onSuccess(String str, Bitmap bitmap) {
                }

                public void onSuccess(String str, String str2) {
                }
            }).share(a(this.a));
            if (!this.g) {
                share.makePrivate();
            }
            if (this.d != null) {
                if (this.d.getText().length() > 0) {
                    share.withDescription(this.d.getText().toString());
                }
            }
            share.start();
        } catch (FyuseSDKException e) {
            DLog.e("FyuseShare", "FyuseShare error: " + e.getMessage());
            g();
        }
    }

    protected List<b> a(Context context) {
        List arrayList = new ArrayList();
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/plain");
        List<ResolveInfo> queryIntentActivities = context.getPackageManager().queryIntentActivities(intent, 0);
        if (!queryIntentActivities.isEmpty()) {
            for (ResolveInfo resolveInfo : queryIntentActivities) {
                try {
                    intent.setPackage(resolveInfo.activityInfo.packageName);
                    b bVar = new b(this, resolveInfo.activityInfo.name, resolveInfo.activityInfo.packageName, resolveInfo.loadLabel(context.getPackageManager()).toString(), resolveInfo.loadIcon(context.getPackageManager()));
                    if (resolveInfo.activityInfo.packageName.contains("facebook") || resolveInfo.activityInfo.packageName.contains("whatsapp") || resolveInfo.activityInfo.packageName.contains("google") || resolveInfo.activityInfo.packageName.contains("twitter")) {
                        arrayList.add(0, bVar);
                    } else {
                        arrayList.add(bVar);
                    }
                } catch (Exception e) {
                }
            }
        }
        return arrayList;
    }

    public void onBackPressed() {
        if (this.e != null && this.e.getVisibility() == 0) {
            this.e.setVisibility(8);
        } else {
            f();
        }
    }

    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        if (getIntent() == null || getIntent().getData() == null) {
            f();
            return;
        }
        if (!(getIntent() == null || getIntent().getExtras() == null)) {
            this.h = getIntent().getBooleanExtra("BUNDLE_DISMISS", this.h);
            this.i = getIntent().getBooleanExtra("BUNDLE_ENABLE_LOCATION", this.i);
            this.j = getIntent().getBooleanExtra("BUNDLE_ENABLE_SHARE", this.j);
        }
        requestWindowFeature(1);
        getWindow().addFlags(2048);
        getWindow().clearFlags(1024);
        setTheme(16973840);
        this.a = getIntent().getData();
        if (!FyuseShareInterface.a(FyuseShareInterface.a(this, this.a))) {
            g();
        }
        setContentView(R.layout.share_interface);
        this.b = (LocalFyuseView) findViewById(R.id.fv_image);
        this.c = (ProgressBar) findViewById(R.id.upload_progress);
        this.d = (EditText) findViewById(R.id.description);
        final ImageView imageView = (ImageView) findViewById(R.id.fyuse_visibility_icon);
        final TextView textView = (TextView) findViewById(R.id.fyuse_visibility);
        findViewById(R.id.fyuse_visibility_click).setOnClickListener(new OnClickListener(this) {
            final /* synthetic */ FyuseShareActivity c;

            public void onClick(View view) {
                boolean z = false;
                if (this.c.g) {
                    textView.setText(view.getResources().getText(R.string.m_PRIVATE));
                    if (VERSION.SDK_INT <= 21) {
                        imageView.setImageDrawable(this.c.getResources().getDrawable(R.drawable.ico_lock_locked));
                    } else {
                        imageView.setImageDrawable(this.c.getDrawable(R.drawable.ico_lock_locked));
                    }
                } else {
                    textView.setText(view.getResources().getText(R.string.m_PUBLIC));
                    if (VERSION.SDK_INT <= 21) {
                        imageView.setImageDrawable(this.c.getResources().getDrawable(R.drawable.ico_lock_unlocked));
                    } else {
                        imageView.setImageDrawable(this.c.getDrawable(R.drawable.ico_lock_unlocked));
                    }
                }
                FyuseShareActivity fyuseShareActivity = this.c;
                if (!this.c.g) {
                    z = true;
                }
                fyuseShareActivity.g = z;
            }
        });
        b();
        this.d.setInputType(1);
        this.d.setImeOptions(6);
        if (this.j) {
            this.e = (GridView) findViewById(R.id.select_services_grid);
            View findViewById = findViewById(R.id.share_icon);
            findViewById.setVisibility(0);
            findViewById.setOnClickListener(new OnClickListener(this) {
                final /* synthetic */ FyuseShareActivity a;

                {
                    this.a = r1;
                }

                public void onClick(View view) {
                    this.a.e.setVisibility(0);
                }
            });
            this.e.setAdapter(new c(this, a((Context) this), new a(this) {
                final /* synthetic */ FyuseShareActivity a;

                {
                    this.a = r1;
                }

                public void a(b bVar) {
                    this.a.e.setVisibility(8);
                    this.a.f = bVar;
                    if (bVar != null) {
                        ((ImageView) this.a.findViewById(R.id.share_icon_selected)).setImageDrawable(this.a.f.d);
                    }
                }
            }));
        }
        findViewById(R.id.download_fyuse_click_area).setOnClickListener(new OnClickListener(this) {
            final /* synthetic */ FyuseShareActivity a;

            {
                this.a = r1;
            }

            public void onClick(View view) {
                this.a.a();
            }
        });
    }

    protected void onResume() {
        super.onResume();
        try {
            h();
        } catch (FyuseSDKException e) {
            DLog.e("FyuseViewer", "Error viewing the fyuse: " + e.getMessage());
            g();
        }
    }

    protected void onStop() {
        FyuseViewer.get(this).clearMemory();
        super.onStop();
    }
}
