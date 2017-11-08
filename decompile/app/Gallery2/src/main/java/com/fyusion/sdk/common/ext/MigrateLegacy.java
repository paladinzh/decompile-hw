package com.fyusion.sdk.common.ext;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import com.fyusion.sdk.common.FyuseSDK;
import com.fyusion.sdk.common.FyuseSDKException;
import com.fyusion.sdk.common.ext.util.FyuseUtils;
import java.io.File;

/* compiled from: Unknown */
public class MigrateLegacy {
    private MigrateListener a;
    private String b;
    private boolean c = false;

    /* compiled from: Unknown */
    public interface MigrateListener {
        void onError(@NonNull String str);

        void onLegacyNotInstalled();

        void onNotSupportsMigrate();

        void onSuccess(@NonNull File[] fileArr);
    }

    private static ResultReceiver a(ResultReceiver resultReceiver) {
        Parcel obtain = Parcel.obtain();
        resultReceiver.writeToParcel(obtain, 0);
        obtain.setDataPosition(0);
        ResultReceiver resultReceiver2 = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(obtain);
        obtain.recycle();
        return resultReceiver2;
    }

    public static MigrateLegacy build() {
        return new MigrateLegacy();
    }

    public static boolean isLegacyInstalled() {
        boolean z = false;
        if (FyuseSDK.getContext() == null || FyuseSDK.getContext().isRestricted()) {
            return false;
        }
        PackageManager packageManager = FyuseSDK.getContext().getPackageManager();
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.fyusion.sdk", "com.fyusion.fyuse.Camera.CameraActivity"));
        if (packageManager.queryIntentActivities(intent, 0).size() > 0) {
            z = true;
        }
        return z;
    }

    public MigrateLegacy convertToNewContainer() {
        this.c = true;
        return this;
    }

    public void migrate() throws FyuseSDKException {
        if (FyuseSDK.getContext() == null || FyuseSDK.getContext().isRestricted()) {
            throw new FyuseSDKException("Context is null");
        }
        PackageManager packageManager = FyuseSDK.getContext().getPackageManager();
        if (isLegacyInstalled()) {
            final Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.fyusion.sdk", "com.fyusion.sdk.MigrateFyusesService"));
            if (this.b != null) {
                intent.putExtra("EXTRA_TARGET_PATH", this.b);
            }
            intent.putExtra("EXTRA_LISTENER", a(new ResultReceiver(this, new Handler()) {
                final /* synthetic */ MigrateLegacy a;

                protected void onReceiveResult(int i, Bundle bundle) {
                    super.onReceiveResult(i, bundle);
                    if (i < 0) {
                        if (this.a.a != null) {
                            this.a.a.onError("Migrate error");
                        }
                    } else if (bundle != null && bundle.containsKey("fyuses")) {
                        File[] fileArr = (File[]) bundle.getSerializable("fyuses");
                        if (this.a.c && fileArr != null && fileArr.length > 0) {
                            for (int i2 = 0; i2 < fileArr.length; i2++) {
                                if (fileArr[i2].exists()) {
                                    FyuseUtils.convertV1ToV3(fileArr[i2]);
                                }
                            }
                        }
                        if (this.a.a != null) {
                            MigrateListener b = this.a.a;
                            if (fileArr == null) {
                                fileArr = new File[0];
                            }
                            b.onSuccess(fileArr);
                        }
                    } else if (this.a.a != null) {
                        this.a.a.onSuccess(new File[0]);
                    }
                }
            }));
            if (packageManager.queryIntentServices(intent, 0).size() <= 0) {
                if (this.a != null) {
                    this.a.onNotSupportsMigrate();
                }
            } else if (FyuseSDK.getContext().startService(intent) == null) {
                Intent intent2 = new Intent();
                intent2.setComponent(new ComponentName("com.fyusion.sdk", "com.fyusion.sdk.MigrateFyusesActivity"));
                intent2.setFlags(268500992);
                FyuseSDK.getContext().startActivity(intent2);
                new Handler().postDelayed(new Runnable(this) {
                    final /* synthetic */ MigrateLegacy b;

                    /* JADX WARNING: inconsistent code. */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public void run() {
                        if (!(FyuseSDK.getContext() == null || FyuseSDK.getContext().isRestricted() || FyuseSDK.getContext().startService(intent) != null || this.b.a == null)) {
                            this.b.a.onError("Failed to start migrate");
                        }
                    }
                }, 50);
            }
            return;
        }
        if (this.a != null) {
            this.a.onLegacyNotInstalled();
        }
    }

    public MigrateLegacy setDestinationFolder(String str) {
        if (!(str == null || str.isEmpty())) {
            if (File.separator.charAt(0) == str.charAt(str.length() - 1)) {
                this.b = str;
            } else {
                this.b = str + File.separator;
            }
        }
        return this;
    }

    public MigrateLegacy setListener(MigrateListener migrateListener) {
        this.a = migrateListener;
        return this;
    }
}
