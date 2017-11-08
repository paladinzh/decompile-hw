package com.android.settings.notification;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.appcompat.R$id;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.utils.ZenServiceListing;
import com.android.settings.utils.ZenServiceListing.Callback;
import java.lang.ref.WeakReference;
import java.text.Collator;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public abstract class ZenRuleSelectionDialog {
    private static final boolean DEBUG = ZenModeSettings.DEBUG;
    private static final Comparator<ZenRuleInfo> RULE_TYPE_COMPARATOR = new Comparator<ZenRuleInfo>() {
        private final Collator mCollator = Collator.getInstance();

        public int compare(ZenRuleInfo lhs, ZenRuleInfo rhs) {
            int byAppName = this.mCollator.compare(lhs.packageLabel, rhs.packageLabel);
            if (byAppName != 0) {
                return byAppName;
            }
            return this.mCollator.compare(lhs.title, rhs.title);
        }
    };
    private final Context mContext;
    private final AlertDialog mDialog;
    private NotificationManager mNm;
    private final PackageManager mPm;
    private final LinearLayout mRuleContainer;
    private final ZenServiceListing mServiceListing;
    private final Callback mServiceListingCallback;

    /* renamed from: com.android.settings.notification.ZenRuleSelectionDialog$1 */
    class AnonymousClass1 implements Callback {
        final /* synthetic */ ZenRuleSelectionDialog this$0;

        public void onServicesReloaded(Set<ServiceInfo> services) {
            if (ZenRuleSelectionDialog.DEBUG) {
                Log.d("ZenRuleSelectionDialog", "Services reloaded: count=" + services.size());
            }
            Set<ZenRuleInfo> externalRuleTypes = new TreeSet(ZenRuleSelectionDialog.RULE_TYPE_COMPARATOR);
            for (ServiceInfo serviceInfo : services) {
                ZenRuleInfo ri = ZenModeAutomationSettings.getRuleInfo(this.this$0.mPm, serviceInfo);
                if (!(ri == null || ri.configurationActivity == null || !this.this$0.mNm.isNotificationPolicyAccessGrantedForPackage(ri.packageName))) {
                    if (ri.ruleInstanceLimit <= 0 || ri.ruleInstanceLimit >= this.this$0.mNm.getRuleInstanceCount(serviceInfo.getComponentName()) + 1) {
                        externalRuleTypes.add(ri);
                    }
                }
            }
            this.this$0.bindExternalRules(externalRuleTypes);
        }
    }

    /* renamed from: com.android.settings.notification.ZenRuleSelectionDialog$3 */
    class AnonymousClass3 implements OnDismissListener {
        final /* synthetic */ ZenRuleSelectionDialog this$0;

        public void onDismiss(DialogInterface dialog) {
            if (this.this$0.mServiceListing != null) {
                this.this$0.mServiceListing.removeZenCallback(this.this$0.mServiceListingCallback);
            }
        }
    }

    private class LoadIconTask extends AsyncTask<ApplicationInfo, Void, Drawable> {
        private final WeakReference<ImageView> viewReference;

        public LoadIconTask(ImageView view) {
            this.viewReference = new WeakReference(view);
        }

        protected Drawable doInBackground(ApplicationInfo... params) {
            return params[0].loadIcon(ZenRuleSelectionDialog.this.mPm);
        }

        protected void onPostExecute(Drawable icon) {
            if (icon != null) {
                ImageView view = (ImageView) this.viewReference.get();
                if (view != null) {
                    view.setImageDrawable(icon);
                }
            }
        }
    }

    public abstract void onExternalRuleSelected(ZenRuleInfo zenRuleInfo);

    public abstract void onSystemRuleSelected(ZenRuleInfo zenRuleInfo);

    private void bindType(final ZenRuleInfo ri) {
        try {
            ApplicationInfo info = this.mPm.getApplicationInfo(ri.packageName, 0);
            LinearLayout v = (LinearLayout) LayoutInflater.from(this.mContext).inflate(2130969292, null, false);
            new LoadIconTask((ImageView) v.findViewById(2131886147)).execute(new ApplicationInfo[]{info});
            ((TextView) v.findViewById(R$id.title)).setText(ri.title);
            if (!ri.isSystem) {
                TextView subtitle = (TextView) v.findViewById(2131887010);
                subtitle.setText(info.loadLabel(this.mPm));
                subtitle.setVisibility(0);
            }
            v.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ZenRuleSelectionDialog.this.mDialog.dismiss();
                    if (ri.isSystem) {
                        ZenRuleSelectionDialog.this.onSystemRuleSelected(ri);
                    } else {
                        ZenRuleSelectionDialog.this.onExternalRuleSelected(ri);
                    }
                }
            });
            this.mRuleContainer.addView(v);
        } catch (NameNotFoundException e) {
        }
    }

    private void bindExternalRules(Set<ZenRuleInfo> externalRuleTypes) {
        for (ZenRuleInfo ri : externalRuleTypes) {
            bindType(ri);
        }
    }
}
