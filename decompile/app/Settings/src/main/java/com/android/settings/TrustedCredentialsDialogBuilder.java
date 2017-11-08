package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.admin.DevicePolicyManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.pm.UserInfo;
import android.net.http.SslCertificate;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import com.android.internal.widget.LockPatternUtils;
import com.android.settingslib.RestrictedLockUtils;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

class TrustedCredentialsDialogBuilder extends Builder {
    private final DialogEventHandler mDialogEventHandler;

    public interface DelegateInterface {
        List<X509Certificate> getX509CertsFromCertHolder(CertHolder certHolder);

        void removeOrInstallCert(CertHolder certHolder);

        void showRemoveDialog(AlertDialog alertDialog);

        boolean startConfirmCredentialIfNotConfirmed(int i, IntConsumer intConsumer);
    }

    private static class DialogEventHandler implements OnShowListener, OnClickListener {
        private final Activity mActivity;
        private CertHolder[] mCertHolders = new CertHolder[0];
        private int mCurrentCertIndex = -1;
        private View mCurrentCertLayout = null;
        private final DelegateInterface mDelegate;
        private AlertDialog mDialog;
        private final DevicePolicyManager mDpm;
        private boolean mNeedsApproval;
        private Button mNegativeButton;
        private Button mPositiveButton;
        private final LinearLayout mRootContainer;
        private final UserManager mUserManager;

        final /* synthetic */ class -void_onClickTrust__LambdaImpl0 implements IntConsumer {
            private /* synthetic */ DialogEventHandler val$this;

            public /* synthetic */ -void_onClickTrust__LambdaImpl0(DialogEventHandler dialogEventHandler) {
                this.val$this = dialogEventHandler;
            }

            public void accept(int arg0) {
                this.val$this.-com_android_settings_TrustedCredentialsDialogBuilder$DialogEventHandler-mthref-0(arg0);
            }
        }

        public DialogEventHandler(Activity activity, DelegateInterface delegate) {
            this.mActivity = activity;
            this.mDpm = (DevicePolicyManager) activity.getSystemService(DevicePolicyManager.class);
            this.mUserManager = (UserManager) activity.getSystemService(UserManager.class);
            this.mDelegate = delegate;
            this.mRootContainer = new LinearLayout(this.mActivity);
            this.mRootContainer.setOrientation(1);
        }

        public void setDialog(AlertDialog dialog) {
            this.mDialog = dialog;
        }

        public void setCertHolders(CertHolder[] certHolder) {
            this.mCertHolders = certHolder;
        }

        public void onShow(DialogInterface dialogInterface) {
            nextOrDismiss();
        }

        public void onClick(View view) {
            if (view == this.mPositiveButton) {
                if (this.mNeedsApproval) {
                    onClickTrust();
                } else {
                    onClickOk();
                }
            } else if (view == this.mNegativeButton) {
                onClickRemove();
            }
        }

        private void onClickOk() {
            nextOrDismiss();
        }

        private void onClickTrust() {
            CertHolder certHolder = getCurrentCertInfo();
            if (!this.mDelegate.startConfirmCredentialIfNotConfirmed(certHolder.getUserId(), new -void_onClickTrust__LambdaImpl0())) {
                this.mDpm.approveCaCert(certHolder.getAlias(), certHolder.getUserId(), true);
                nextOrDismiss();
            }
        }

        /* synthetic */ void -com_android_settings_TrustedCredentialsDialogBuilder$DialogEventHandler-mthref-0(int i) {
            onCredentialConfirmed(i);
        }

        private void onClickRemove() {
            final CertHolder certHolder = getCurrentCertInfo();
            AlertDialog dialog = new Builder(this.mActivity).setMessage(getButtonConfirmation(certHolder)).setPositiveButton(17039379, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    DialogEventHandler.this.mDelegate.removeOrInstallCert(certHolder);
                    dialog.dismiss();
                    DialogEventHandler.this.nextOrDismiss();
                }
            }).setNegativeButton(17039369, null).create();
            this.mDelegate.showRemoveDialog(dialog);
            dialog.show();
        }

        private void onCredentialConfirmed(int userId) {
            if (this.mDialog.isShowing() && this.mNeedsApproval && getCurrentCertInfo() != null && getCurrentCertInfo().getUserId() == userId) {
                onClickTrust();
            }
        }

        private CertHolder getCurrentCertInfo() {
            return this.mCurrentCertIndex < this.mCertHolders.length ? this.mCertHolders[this.mCurrentCertIndex] : null;
        }

        private void nextOrDismiss() {
            this.mCurrentCertIndex++;
            while (this.mCurrentCertIndex < this.mCertHolders.length && getCurrentCertInfo() == null) {
                this.mCurrentCertIndex++;
            }
            if (this.mCurrentCertIndex >= this.mCertHolders.length) {
                this.mDialog.dismiss();
                return;
            }
            updateViewContainer();
            updatePositiveButton();
            updateNegativeButton();
        }

        private boolean isUserSecure(int userId) {
            LockPatternUtils lockPatternUtils = new LockPatternUtils(this.mActivity);
            if (lockPatternUtils.isSecure(userId)) {
                return true;
            }
            UserInfo parentUser = this.mUserManager.getProfileParent(userId);
            if (parentUser == null) {
                return false;
            }
            return lockPatternUtils.isSecure(parentUser.id);
        }

        private void updatePositiveButton() {
            int i;
            boolean z = false;
            CertHolder certHolder = getCurrentCertInfo();
            if (!(certHolder.isSystemCert() || !isUserSecure(certHolder.getUserId()) || this.mDpm.isCaCertApproved(certHolder.getAlias(), certHolder.getUserId()))) {
                z = true;
            }
            this.mNeedsApproval = z;
            boolean isProfileOrDeviceOwner = RestrictedLockUtils.getProfileOrDeviceOwner(this.mActivity, certHolder.getUserId()) != null;
            Activity activity = this.mActivity;
            if (isProfileOrDeviceOwner || !this.mNeedsApproval) {
                i = 17039370;
            } else {
                i = 2131626411;
            }
            this.mPositiveButton = updateButton(-1, activity.getText(i));
        }

        private void updateNegativeButton() {
            int i;
            CertHolder certHolder = getCurrentCertInfo();
            boolean showRemoveButton = !this.mUserManager.hasUserRestriction("no_config_credentials", new UserHandle(certHolder.getUserId()));
            this.mNegativeButton = updateButton(-2, this.mActivity.getText(getButtonLabel(certHolder)));
            Button button = this.mNegativeButton;
            if (showRemoveButton) {
                i = 0;
            } else {
                i = 8;
            }
            button.setVisibility(i);
        }

        private Button updateButton(int buttonType, CharSequence displayText) {
            this.mDialog.setButton(buttonType, displayText, (DialogInterface.OnClickListener) null);
            Button button = this.mDialog.getButton(buttonType);
            button.setText(displayText);
            button.setOnClickListener(this);
            return button;
        }

        private void updateViewContainer() {
            LinearLayout nextCertLayout = getCertLayout(getCurrentCertInfo());
            if (this.mCurrentCertLayout == null) {
                this.mCurrentCertLayout = nextCertLayout;
                this.mRootContainer.addView(this.mCurrentCertLayout);
                return;
            }
            animateViewTransition(nextCertLayout);
        }

        private LinearLayout getCertLayout(CertHolder certHolder) {
            final ArrayList<View> views = new ArrayList();
            ArrayList<String> titles = new ArrayList();
            List<X509Certificate> certificates = this.mDelegate.getX509CertsFromCertHolder(certHolder);
            if (certificates != null) {
                for (X509Certificate certificate : certificates) {
                    SslCertificate sslCert = new SslCertificate(certificate);
                    views.add(sslCert.inflateCertificateView(this.mActivity));
                    titles.add(sslCert.getIssuedTo().getCName());
                }
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter(this.mActivity, 17367048, titles);
            arrayAdapter.setDropDownViewResource(17367049);
            Spinner spinner = new Spinner(this.mActivity);
            spinner.setAdapter(arrayAdapter);
            spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    int i = 0;
                    while (i < views.size()) {
                        ((View) views.get(i)).setVisibility(i == position ? 0 : 8);
                        i++;
                    }
                }

                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            LinearLayout certLayout = new LinearLayout(this.mActivity);
            certLayout.setOrientation(1);
            certLayout.addView(spinner);
            int i = 0;
            while (i < views.size()) {
                View certificateView = (View) views.get(i);
                certificateView.setVisibility(i == 0 ? 0 : 8);
                certLayout.addView(certificateView);
                i++;
            }
            return certLayout;
        }

        private static int getButtonConfirmation(CertHolder certHolder) {
            if (!certHolder.isSystemCert()) {
                return 2131626414;
            }
            if (certHolder.isDeleted()) {
                return 2131626412;
            }
            return 2131626413;
        }

        private static int getButtonLabel(CertHolder certHolder) {
            if (!certHolder.isSystemCert()) {
                return 2131626410;
            }
            if (certHolder.isDeleted()) {
                return 2131626409;
            }
            return 2131626408;
        }

        private void animateViewTransition(final View nextCertView) {
            animateOldContent(new Runnable() {
                public void run() {
                    DialogEventHandler.this.addAndAnimateNewContent(nextCertView);
                }
            });
        }

        private void animateOldContent(Runnable callback) {
            this.mCurrentCertLayout.animate().alpha(0.0f).setDuration(300).setInterpolator(AnimationUtils.loadInterpolator(this.mActivity, 17563663)).withEndAction(callback).start();
        }

        private void addAndAnimateNewContent(View nextCertLayout) {
            this.mCurrentCertLayout = nextCertLayout;
            this.mRootContainer.removeAllViews();
            this.mRootContainer.addView(nextCertLayout);
            this.mRootContainer.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    DialogEventHandler.this.mRootContainer.removeOnLayoutChangeListener(this);
                    DialogEventHandler.this.mCurrentCertLayout.setTranslationX((float) DialogEventHandler.this.mRootContainer.getWidth());
                    DialogEventHandler.this.mCurrentCertLayout.animate().translationX(0.0f).setInterpolator(AnimationUtils.loadInterpolator(DialogEventHandler.this.mActivity, 17563662)).setDuration(200).start();
                }
            });
        }
    }

    public TrustedCredentialsDialogBuilder(Activity activity, DelegateInterface delegate) {
        super(activity);
        this.mDialogEventHandler = new DialogEventHandler(activity, delegate);
        initDefaultBuilderParams();
    }

    public TrustedCredentialsDialogBuilder setCertHolder(CertHolder certHolder) {
        return setCertHolders(certHolder == null ? new CertHolder[0] : new CertHolder[]{certHolder});
    }

    public TrustedCredentialsDialogBuilder setCertHolders(CertHolder[] certHolders) {
        this.mDialogEventHandler.setCertHolders(certHolders);
        return this;
    }

    public AlertDialog create() {
        AlertDialog dialog = super.create();
        dialog.setOnShowListener(this.mDialogEventHandler);
        this.mDialogEventHandler.setDialog(dialog);
        return dialog;
    }

    private void initDefaultBuilderParams() {
        setTitle(17040581);
        setView(this.mDialogEventHandler.mRootContainer);
        setPositiveButton(2131626411, null);
        setNegativeButton(17039370, null);
    }
}
