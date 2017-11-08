package com.android.systemui.qs.tiles;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.Icon;
import com.android.systemui.qs.QSTile.ResourceIcon;
import com.android.systemui.qs.QSTile.State;
import fyusion.vislib.BuildConfig;
import java.util.Arrays;
import java.util.Objects;

public class IntentTile extends QSTile<State> {
    private int mCurrentUserId;
    private String mIntentPackage;
    private Intent mLastIntent;
    private PendingIntent mOnClick;
    private String mOnClickUri;
    private PendingIntent mOnLongClick;
    private String mOnLongClickUri;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            IntentTile.this.refreshState(intent);
        }
    };

    private static class BytesIcon extends Icon {
        private final byte[] mBytes;

        public BytesIcon(byte[] bytes) {
            this.mBytes = bytes;
        }

        public Drawable getDrawable(Context context) {
            return new BitmapDrawable(context.getResources(), BitmapFactory.decodeByteArray(this.mBytes, 0, this.mBytes.length));
        }

        public boolean equals(Object o) {
            return o instanceof BytesIcon ? Arrays.equals(((BytesIcon) o).mBytes, this.mBytes) : false;
        }

        public String toString() {
            return String.format("BytesIcon[len=%s]", new Object[]{Integer.valueOf(this.mBytes.length)});
        }
    }

    private class PackageDrawableIcon extends Icon {
        private final String mPackage;
        private final int mResId;

        public PackageDrawableIcon(String pkg, int resId) {
            this.mPackage = pkg;
            this.mResId = resId;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof PackageDrawableIcon)) {
                return false;
            }
            PackageDrawableIcon other = (PackageDrawableIcon) o;
            if (Objects.equals(other.mPackage, this.mPackage) && other.mResId == this.mResId) {
                z = true;
            }
            return z;
        }

        public Drawable getDrawable(Context context) {
            try {
                return context.createPackageContext(this.mPackage, 0).getDrawable(this.mResId);
            } catch (Throwable t) {
                Log.w(IntentTile.this.TAG, "Error loading package drawable pkg=" + this.mPackage + " id=" + this.mResId, t);
                return null;
            }
        }

        public String toString() {
            return String.format("PackageDrawableIcon[pkg=%s,id=0x%08x]", new Object[]{this.mPackage, Integer.valueOf(this.mResId)});
        }
    }

    private IntentTile(Host host, String action) {
        super(host);
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter(action));
    }

    protected void handleDestroy() {
        super.handleDestroy();
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    public static QSTile<?> create(Host host, String spec) {
        if (spec != null && spec.startsWith("intent(") && spec.endsWith(")")) {
            String action = spec.substring("intent(".length(), spec.length() - 1);
            if (!action.isEmpty()) {
                return new IntentTile(host, action);
            }
            throw new IllegalArgumentException("Empty intent tile spec action");
        }
        throw new IllegalArgumentException("Bad intent tile spec: " + spec);
    }

    public void setListening(boolean listening) {
    }

    public State newTileState() {
        return new State();
    }

    protected void handleUserSwitch(int newUserId) {
        super.handleUserSwitch(newUserId);
        this.mCurrentUserId = newUserId;
    }

    protected void handleClick() {
        MetricsLogger.action(this.mContext, getMetricsCategory(), this.mIntentPackage);
        sendIntent("click", this.mOnClick, this.mOnClickUri);
    }

    public Intent getLongClickIntent() {
        return null;
    }

    protected void handleLongClick() {
        sendIntent("long-click", this.mOnLongClick, this.mOnLongClickUri);
    }

    private void sendIntent(String type, PendingIntent pi, String uri) {
        if (pi != null) {
            try {
                if (pi.isActivity()) {
                    getHost().startActivityDismissingKeyguard(pi);
                } else {
                    pi.send();
                }
            } catch (Throwable t) {
                Log.w(this.TAG, "Error sending " + type + " intent", t);
            }
        } else if (uri != null) {
            this.mContext.sendBroadcastAsUser(Intent.parseUri(uri, 1), new UserHandle(this.mCurrentUserId));
        }
    }

    public CharSequence getTileLabel() {
        return getState().label;
    }

    protected void handleUpdateState(State state, Object arg) {
        String str;
        Intent intent = (Intent) arg;
        if (intent == null) {
            if (this.mLastIntent != null) {
                intent = this.mLastIntent;
            } else {
                return;
            }
        }
        this.mLastIntent = intent;
        state.contentDescription = intent.getStringExtra("contentDescription");
        state.label = intent.getStringExtra("label");
        state.icon = null;
        byte[] iconBitmap = intent.getByteArrayExtra("iconBitmap");
        if (iconBitmap != null) {
            try {
                state.icon = new BytesIcon(iconBitmap);
            } catch (Throwable t) {
                Log.w(this.TAG, "Error loading icon bitmap, length " + iconBitmap.length, t);
            }
        } else {
            int iconId = intent.getIntExtra("iconId", 0);
            if (iconId != 0) {
                String iconPackage = intent.getStringExtra("iconPackage");
                if (TextUtils.isEmpty(iconPackage)) {
                    state.icon = ResourceIcon.get(iconId);
                } else {
                    state.icon = new PackageDrawableIcon(iconPackage, iconId);
                }
            }
        }
        this.mOnClick = (PendingIntent) intent.getParcelableExtra("onClick");
        this.mOnClickUri = intent.getStringExtra("onClickUri");
        this.mOnLongClick = (PendingIntent) intent.getParcelableExtra("onLongClick");
        this.mOnLongClickUri = intent.getStringExtra("onLongClickUri");
        this.mIntentPackage = intent.getStringExtra("package");
        if (this.mIntentPackage == null) {
            str = BuildConfig.FLAVOR;
        } else {
            str = this.mIntentPackage;
        }
        this.mIntentPackage = str;
    }

    public int getMetricsCategory() {
        return 121;
    }
}
