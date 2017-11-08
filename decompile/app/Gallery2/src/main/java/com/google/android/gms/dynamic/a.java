package com.google.android.gms.dynamic;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.common.GooglePlayServicesUtil;
import java.util.Iterator;
import java.util.LinkedList;

/* compiled from: Unknown */
public abstract class a<T extends LifecycleDelegate> {
    private T Fp;
    private Bundle Fq;
    private LinkedList<a> Fr;
    private final d<T> Fs = new d<T>(this) {
        final /* synthetic */ a Ft;

        {
            this.Ft = r1;
        }

        public void a(T t) {
            this.Ft.Fp = t;
            Iterator it = this.Ft.Fr.iterator();
            while (it.hasNext()) {
                ((a) it.next()).b(this.Ft.Fp);
            }
            this.Ft.Fr.clear();
            this.Ft.Fq = null;
        }
    };

    /* compiled from: Unknown */
    private interface a {
        void b(LifecycleDelegate lifecycleDelegate);

        int getState();
    }

    private void a(Bundle bundle, a aVar) {
        if (this.Fp == null) {
            if (this.Fr == null) {
                this.Fr = new LinkedList();
            }
            this.Fr.add(aVar);
            if (bundle != null) {
                if (this.Fq != null) {
                    this.Fq.putAll(bundle);
                } else {
                    this.Fq = (Bundle) bundle.clone();
                }
            }
            a(this.Fs);
            return;
        }
        aVar.b(this.Fp);
    }

    private void aO(int i) {
        while (!this.Fr.isEmpty() && ((a) this.Fr.getLast()).getState() >= i) {
            this.Fr.removeLast();
        }
    }

    public static void b(FrameLayout frameLayout) {
        final Context context = frameLayout.getContext();
        final int isGooglePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        CharSequence b = GooglePlayServicesUtil.b(context, isGooglePlayServicesAvailable, -1);
        CharSequence b2 = GooglePlayServicesUtil.b(context, isGooglePlayServicesAvailable);
        View linearLayout = new LinearLayout(frameLayout.getContext());
        linearLayout.setOrientation(1);
        linearLayout.setLayoutParams(new LayoutParams(-2, -2));
        frameLayout.addView(linearLayout);
        View textView = new TextView(frameLayout.getContext());
        textView.setLayoutParams(new LayoutParams(-2, -2));
        textView.setText(b);
        linearLayout.addView(textView);
        if (b2 != null) {
            View button = new Button(context);
            button.setLayoutParams(new LayoutParams(-2, -2));
            button.setText(b2);
            linearLayout.addView(button);
            button.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    context.startActivity(GooglePlayServicesUtil.a(context, isGooglePlayServicesAvailable, -1));
                }
            });
        }
    }

    protected abstract void a(d<T> dVar);

    public T fj() {
        return this.Fp;
    }

    public void onCreate(final Bundle savedInstanceState) {
        a(savedInstanceState, new a(this) {
            final /* synthetic */ a Ft;

            public void b(LifecycleDelegate lifecycleDelegate) {
                this.Ft.Fp.onCreate(savedInstanceState);
            }

            public int getState() {
                return 1;
            }
        });
    }

    public void onDestroy() {
        if (this.Fp == null) {
            aO(1);
        } else {
            this.Fp.onDestroy();
        }
    }

    public void onLowMemory() {
        if (this.Fp != null) {
            this.Fp.onLowMemory();
        }
    }

    public void onPause() {
        if (this.Fp == null) {
            aO(5);
        } else {
            this.Fp.onPause();
        }
    }

    public void onResume() {
        a(null, new a(this) {
            final /* synthetic */ a Ft;

            {
                this.Ft = r1;
            }

            public void b(LifecycleDelegate lifecycleDelegate) {
                this.Ft.Fp.onResume();
            }

            public int getState() {
                return 5;
            }
        });
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.Fp != null) {
            this.Fp.onSaveInstanceState(outState);
        } else if (this.Fq != null) {
            outState.putAll(this.Fq);
        }
    }
}
