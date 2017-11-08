package com.android.settings;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.net.IConnectivityManager;
import android.net.IConnectivityManager.Stub;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.security.KeyChain;
import android.security.KeyChain.KeyChainConnection;
import android.security.KeyStore;
import android.security.KeyStore.State;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.widget.LockPatternUtils;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import com.huawei.cust.HwCustUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;

public final class CredentialStorage extends Activity {
    private static final /* synthetic */ int[] -android-security-KeyStore$StateSwitchesValues = null;
    private AlertDialog mDialog;
    private HwCustCredentialStorage mHwCustCredentialStorage;
    private Bundle mInstallBundle;
    private final KeyStore mKeyStore = KeyStore.getInstance();
    private ResetDialog mResetDialog;
    private int mRetriesRemaining = -1;

    private class ConfigureKeyGuardDialog implements OnClickListener, OnDismissListener {
        private boolean mConfigureConfirmed;

        private ConfigureKeyGuardDialog() {
            CredentialStorage.this.dismissDialog();
            CredentialStorage.this.mDialog = new Builder(CredentialStorage.this).setTitle(17039380).setMessage(2131626142).setPositiveButton(2131624536, this).setNegativeButton(17039360, this).create();
            CredentialStorage.this.mDialog.setOnDismissListener(this);
            CredentialStorage.this.mDialog.show();
        }

        public void onClick(DialogInterface dialog, int button) {
            this.mConfigureConfirmed = button == -1;
        }

        public void onDismiss(DialogInterface dialog) {
            if (this.mConfigureConfirmed) {
                this.mConfigureConfirmed = false;
                Intent intent = new Intent("android.app.action.SET_NEW_PASSWORD");
                intent.putExtra("minimum_quality", 65536);
                CredentialStorage.this.startActivity(intent);
                return;
            }
            CredentialStorage.this.finish();
        }
    }

    private class ResetDialog implements OnClickListener, OnDismissListener {
        private boolean mResetConfirmed;

        private ResetDialog() {
            CredentialStorage.this.dismissDialog();
            CredentialStorage.this.mDialog = new Builder(CredentialStorage.this).setTitle(17039380).setMessage(2131626134).setPositiveButton(17039370, this).setNegativeButton(17039360, this).create();
            CredentialStorage.this.mDialog.setOnDismissListener(this);
            CredentialStorage.this.mDialog.show();
        }

        public void onClick(DialogInterface dialog, int button) {
            this.mResetConfirmed = button == -1;
        }

        public void onDismiss(DialogInterface dialog) {
            if (this.mResetConfirmed) {
                this.mResetConfirmed = false;
                if (CredentialStorage.this.confirmKeyGuard(2)) {
                    return;
                }
            }
            CredentialStorage.this.finish();
        }
    }

    private class ResetKeyStoreAndKeyChain extends AsyncTask<Void, Void, Boolean> {
        private ResetKeyStoreAndKeyChain() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected Boolean doInBackground(Void... unused) {
            CredentialStorage.this.disconnectVpn();
            new LockPatternUtils(CredentialStorage.this).resetKeyStore(UserHandle.myUserId());
            CredentialStorage.this.mHwCustCredentialStorage.resetKeyStore(CredentialStorage.this.mKeyStore);
            try {
                KeyChainConnection keyChainConnection = KeyChain.bind(CredentialStorage.this);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return Boolean.valueOf(false);
            }
            Boolean valueOf;
            try {
                valueOf = Boolean.valueOf(keyChainConnection.getService().reset());
                try {
                    keyChainConnection.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                return valueOf;
            } catch (RemoteException e3) {
                e3.printStackTrace();
                valueOf = Boolean.valueOf(false);
                return valueOf;
            } catch (Throwable th) {
                try {
                    keyChainConnection.close();
                } catch (Exception e22) {
                    e22.printStackTrace();
                }
            }
        }

        protected void onPostExecute(Boolean success) {
            if (success.booleanValue()) {
                Toast.makeText(CredentialStorage.this, 2131627302, 0).show();
            } else {
                Toast.makeText(CredentialStorage.this, 2131627303, 0).show();
            }
            CredentialStorage.this.finish();
        }
    }

    private class UnlockDialog implements TextWatcher, OnClickListener, OnDismissListener {
        private final Button mButton;
        private final TextView mError;
        private final TextView mOldPassword;
        private boolean mUnlockConfirmed;

        private UnlockDialog() {
            CharSequence text;
            View view = View.inflate(CredentialStorage.this, 2130968695, null);
            if (CredentialStorage.this.mRetriesRemaining == -1) {
                text = CredentialStorage.this.getResources().getText(2131626132);
            } else if (CredentialStorage.this.mRetriesRemaining > 3) {
                text = CredentialStorage.this.getResources().getText(2131626136);
            } else if (CredentialStorage.this.mRetriesRemaining == 1) {
                text = CredentialStorage.this.getResources().getText(2131626137);
            } else {
                text = CredentialStorage.this.getResources().getQuantityString(2131689515, CredentialStorage.this.mRetriesRemaining, new Object[]{Integer.valueOf(this$0.mRetriesRemaining)});
            }
            ((TextView) view.findViewById(2131886417)).setText(text);
            this.mOldPassword = (TextView) view.findViewById(2131886420);
            this.mOldPassword.setVisibility(0);
            this.mOldPassword.addTextChangedListener(this);
            this.mError = (TextView) view.findViewById(2131886418);
            CredentialStorage.this.dismissDialog();
            CredentialStorage.this.mDialog = new Builder(CredentialStorage.this).setView(view).setTitle(2131626131).setPositiveButton(17039370, this).setNegativeButton(17039360, this).create();
            CredentialStorage.this.mDialog.setOnDismissListener(this);
            CredentialStorage.this.mDialog.show();
            this.mButton = CredentialStorage.this.mDialog.getButton(-1);
            this.mButton.setEnabled(false);
        }

        public void afterTextChanged(Editable editable) {
            boolean z = true;
            Button button = this.mButton;
            if (this.mOldPassword != null && this.mOldPassword.getText().length() <= 0) {
                z = false;
            }
            button.setEnabled(z);
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void onClick(DialogInterface dialog, int button) {
            this.mUnlockConfirmed = button == -1;
        }

        public void onDismiss(DialogInterface dialog) {
            if (this.mUnlockConfirmed) {
                this.mUnlockConfirmed = false;
                this.mError.setVisibility(0);
                CredentialStorage.this.mKeyStore.unlock(this.mOldPassword.getText().toString());
                int error = CredentialStorage.this.mKeyStore.getLastError();
                if (error == 1) {
                    CredentialStorage.this.mRetriesRemaining = -1;
                    Toast.makeText(CredentialStorage.this, 2131627304, 0).show();
                    CredentialStorage.this.ensureKeyGuard();
                } else if (error == 3) {
                    CredentialStorage.this.mRetriesRemaining = -1;
                    Toast.makeText(CredentialStorage.this, 2131627302, 0).show();
                    CredentialStorage.this.handleUnlockOrInstall();
                } else if (error >= 10) {
                    CredentialStorage.this.mRetriesRemaining = (error - 10) + 1;
                    CredentialStorage.this.handleUnlockOrInstall();
                }
                return;
            }
            CredentialStorage.this.finish();
        }
    }

    private static /* synthetic */ int[] -getandroid-security-KeyStore$StateSwitchesValues() {
        if (-android-security-KeyStore$StateSwitchesValues != null) {
            return -android-security-KeyStore$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.LOCKED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.UNINITIALIZED.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.UNLOCKED.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -android-security-KeyStore$StateSwitchesValues = iArr;
        return iArr;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mHwCustCredentialStorage = (HwCustCredentialStorage) HwCustUtils.createObj(HwCustCredentialStorage.class, new Object[]{this});
    }

    protected void onResume() {
        super.onResume();
        getWindow().setBackgroundDrawableResource(17170445);
        Intent intent = getIntent();
        String action = intent.getAction();
        if (((UserManager) getSystemService("user")).hasUserRestriction("no_config_credentials")) {
            if ("com.android.credentials.UNLOCK".equals(action) && this.mKeyStore.state() == State.UNINITIALIZED) {
                ensureKeyGuard();
            } else {
                finish();
            }
        } else if (!"com.android.credentials.RESET".equals(action)) {
            if ("com.android.credentials.INSTALL".equals(action) && checkCallerIsCertInstallerOrSelfInProfile()) {
                this.mInstallBundle = intent.getExtras();
            }
            handleUnlockOrInstall();
        } else if (this.mResetDialog == null) {
            this.mResetDialog = new ResetDialog();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        dismissDialog();
    }

    private void handleUnlockOrInstall() {
        if (!isFinishing()) {
            switch (-getandroid-security-KeyStore$StateSwitchesValues()[this.mKeyStore.state().ordinal()]) {
                case 1:
                    UnlockDialog unlockDialog = new UnlockDialog();
                    return;
                case 2:
                    ensureKeyGuard();
                    return;
                case 3:
                    if (checkKeyGuardQuality()) {
                        installIfAvailable();
                        finish();
                        return;
                    }
                    ConfigureKeyGuardDialog configureKeyGuardDialog = new ConfigureKeyGuardDialog();
                    return;
                default:
                    Log.i("CredentialStorage", "handleUnlockOrInstall unknown id");
                    return;
            }
        }
    }

    private void ensureKeyGuard() {
        if (!checkKeyGuardQuality()) {
            ConfigureKeyGuardDialog configureKeyGuardDialog = new ConfigureKeyGuardDialog();
        } else if (!confirmKeyGuard(1)) {
            finish();
        }
    }

    private boolean checkKeyGuardQuality() {
        return new LockPatternUtils(this).getActivePasswordQuality(UserManager.get(this).getCredentialOwnerProfile(UserHandle.myUserId())) >= 65536;
    }

    private boolean isHardwareBackedKey(byte[] keyData) {
        try {
            return KeyChain.isBoundKeyAlgorithm(new AlgorithmId(new ObjectIdentifier(PrivateKeyInfo.getInstance(new ASN1InputStream(new ByteArrayInputStream(keyData)).readObject()).getAlgorithmId().getAlgorithm().getId())).getName());
        } catch (IOException e) {
            Log.e("CredentialStorage", "Failed to parse key data");
            return false;
        }
    }

    private void installIfAvailable() {
        if (this.mInstallBundle != null && !this.mInstallBundle.isEmpty()) {
            Bundle bundle = this.mInstallBundle;
            this.mInstallBundle = null;
            int uid = bundle.getInt("install_as_uid", -1);
            if (uid == -1 || UserHandle.isSameUser(uid, Process.myUid())) {
                int flags;
                if (bundle.containsKey("user_private_key_name")) {
                    String key = bundle.getString("user_private_key_name");
                    byte[] value = bundle.getByteArray("user_private_key_data");
                    flags = 1;
                    if (uid == 1010 && isHardwareBackedKey(value)) {
                        Log.d("CredentialStorage", "Saving private key with FLAG_NONE for WIFI_UID");
                        flags = 0;
                    }
                    if (!this.mKeyStore.importKey(key, value, uid, flags)) {
                        Log.e("CredentialStorage", "Failed to install " + key + " as uid " + uid);
                        return;
                    }
                }
                flags = uid == 1010 ? 0 : 1;
                if (bundle.containsKey("user_certificate_name")) {
                    String certName = bundle.getString("user_certificate_name");
                    if (!this.mKeyStore.put(certName, bundle.getByteArray("user_certificate_data"), uid, flags)) {
                        Log.e("CredentialStorage", "Failed to install " + certName + " as uid " + uid);
                        return;
                    }
                }
                if (bundle.containsKey("ca_certificates_name")) {
                    String caListName = bundle.getString("ca_certificates_name");
                    if (!this.mKeyStore.put(caListName, bundle.getByteArray("ca_certificates_data"), uid, flags)) {
                        Log.e("CredentialStorage", "Failed to install " + caListName + " as uid " + uid);
                        return;
                    }
                }
                if (!(this.mHwCustCredentialStorage == null || this.mHwCustCredentialStorage.installIfAvailable(bundle, this.mKeyStore, uid, flags))) {
                    setResult(-1);
                }
                return;
            }
            int dstUserId = UserHandle.getUserId(uid);
            int myUserId = UserHandle.myUserId();
            if (uid != 1010) {
                Log.e("CredentialStorage", "Failed to install credentials as uid " + uid + ": cross-user installs" + " may only target wifi uids");
            } else {
                startActivityAsUser(new Intent("com.android.credentials.INSTALL").setFlags(33554432).putExtras(bundle), new UserHandle(dstUserId));
            }
        }
    }

    private boolean checkCallerIsCertInstallerOrSelfInProfile() {
        boolean z = true;
        if (TextUtils.equals("com.android.certinstaller", getCallingPackage())) {
            if (getPackageManager().checkSignatures(getCallingPackage(), getPackageName()) != 0) {
                z = false;
            }
            return z;
        }
        try {
            int launchedFromUid = ActivityManagerNative.getDefault().getLaunchedFromUid(getActivityToken());
            if (launchedFromUid == -1) {
                Log.e("CredentialStorage", "com.android.credentials.INSTALL must be started with startActivityForResult");
                return false;
            } else if (!UserHandle.isSameApp(launchedFromUid, Process.myUid())) {
                return false;
            } else {
                UserInfo parentInfo = ((UserManager) getSystemService("user")).getProfileParent(UserHandle.getUserId(launchedFromUid));
                return parentInfo != null && parentInfo.id == UserHandle.myUserId();
            }
        } catch (RemoteException re) {
            re.printStackTrace();
            return false;
        }
    }

    private boolean confirmKeyGuard(int requestCode) {
        return new ChooseLockSettingsHelper(this).launchConfirmationActivity(requestCode, getResources().getText(2131626117), true);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == -1) {
                String password = data.getStringExtra("password");
                if (!TextUtils.isEmpty(password)) {
                    this.mKeyStore.unlock(password);
                    return;
                }
            }
            finish();
        } else if (requestCode == 2) {
            if (resultCode == -1) {
                new ResetKeyStoreAndKeyChain().execute(new Void[0]);
                return;
            }
            finish();
        }
    }

    private void disconnectVpn() {
        IConnectivityManager mService = Stub.asInterface(ServiceManager.getService("connectivity"));
        if (mService != null) {
            try {
                mService.prepareVpn("[Legacy VPN]", "[Legacy VPN]", UserHandle.myUserId());
            } catch (Exception e) {
                MLog.e("CredentialStorage", "VPN disconnect exception" + e.getMessage());
            }
        }
    }

    private void dismissDialog() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.setOnDismissListener(null);
            this.mDialog.dismiss();
        }
    }
}
