package com.android.settings.vpn2;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFrameLayout;
import android.security.Credentials;
import android.security.KeyStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.internal.net.VpnProfile;
import com.android.settings.ListSpinner;
import com.android.settings.SettingsActivity;
import com.huawei.cust.HwCustUtils;
import java.net.InetAddress;

public class VpnAddFragment extends Fragment implements TextWatcher, OnItemSelectedListener, OnClickListener {
    private Button mCancelBtn;
    private TextView mDnsServers;
    private boolean mEditing;
    private HwCustVpnAddFragment mHwCustVpnAddFragment;
    private Spinner mIpsecCaCert;
    private TextView mIpsecIdentifier;
    private TextView mIpsecSecret;
    private Spinner mIpsecServerCert;
    private Spinner mIpsecUserCert;
    private final KeyStore mKeyStore = KeyStore.getInstance();
    private TextView mL2tpSecret;
    private CheckBox mMppe;
    private TextView mName;
    private TextView mPassword;
    private VpnProfile mProfile;
    private TextView mRoutes;
    private Button mSaveBtn;
    private CheckBox mSaveLogin;
    private TextView mSearchDomains;
    private TextView mServer;
    private CheckBox mShowOptions;
    private boolean mShowOptionsChecked;
    private Spinner mType;
    private boolean mUnlocking = false;
    private TextView mUsername;
    private View mView;

    private static class StaticOnCheckedChangeListener implements OnCheckedChangeListener {
        TextView mPasswordView;

        public StaticOnCheckedChangeListener(TextView passwordView) {
            this.mPasswordView = passwordView;
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int i;
            int pos = this.mPasswordView.getSelectionStart();
            TextView textView = this.mPasswordView;
            if (isChecked) {
                i = 144;
            } else {
                i = 128;
            }
            textView.setInputType(i | 1);
            if (pos >= 0) {
                ((EditText) this.mPasswordView).setSelection(pos);
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        boolean z;
        this.mHwCustVpnAddFragment = (HwCustVpnAddFragment) HwCustUtils.createObj(HwCustVpnAddFragment.class, new Object[0]);
        if (savedState != null) {
            VpnProfile profile = VpnProfile.decode(savedState.getString("VpnKey"), savedState.getByteArray("VpnProfile"));
            if (profile != null) {
                this.mEditing = savedState.getBoolean("VpnEditing");
                this.mProfile = profile;
            } else {
                initProfile();
            }
            this.mShowOptionsChecked = savedState.getBoolean("ShowOptions");
        } else {
            initProfile();
        }
        this.mView = inflater.inflate(2130969252, null);
        this.mName = (TextView) this.mView.findViewById(2131886300);
        this.mType = (Spinner) this.mView.findViewById(2131886800);
        this.mServer = (TextView) this.mView.findViewById(2131887396);
        this.mUsername = (TextView) this.mView.findViewById(2131887419);
        this.mPassword = (TextView) this.mView.findViewById(2131887420);
        this.mSearchDomains = (TextView) this.mView.findViewById(2131887412);
        this.mDnsServers = (TextView) this.mView.findViewById(2131887414);
        this.mRoutes = (TextView) this.mView.findViewById(2131887416);
        this.mMppe = (CheckBox) this.mView.findViewById(2131887397);
        this.mL2tpSecret = (TextView) this.mView.findViewById(2131887399);
        this.mIpsecIdentifier = (TextView) this.mView.findViewById(2131887401);
        this.mIpsecSecret = (TextView) this.mView.findViewById(2131887402);
        this.mIpsecUserCert = (Spinner) this.mView.findViewById(2131887404);
        this.mIpsecCaCert = (Spinner) this.mView.findViewById(2131887406);
        this.mIpsecServerCert = (Spinner) this.mView.findViewById(2131887407);
        this.mSaveLogin = (CheckBox) this.mView.findViewById(2131887421);
        this.mShowOptions = (CheckBox) this.mView.findViewById(2131887409);
        this.mCancelBtn = (Button) this.mView.findViewById(2131886342);
        this.mSaveBtn = (Button) this.mView.findViewById(2131886520);
        this.mName.setText(this.mProfile.name);
        ((EditText) this.mName).setSelection(this.mName.getText().length());
        this.mType.setSelection(this.mProfile.type);
        this.mServer.setText(this.mProfile.server);
        if (this.mProfile.saveLogin) {
            this.mUsername.setText(this.mProfile.username);
            this.mPassword.setText(this.mProfile.password);
        }
        this.mSearchDomains.setText(this.mProfile.searchDomains);
        this.mDnsServers.setText(this.mProfile.dnsServers);
        this.mRoutes.setText(this.mProfile.routes);
        this.mMppe.setChecked(this.mProfile.mppe);
        this.mL2tpSecret.setText(this.mProfile.l2tpSecret);
        this.mIpsecIdentifier.setText(this.mProfile.ipsecIdentifier);
        this.mIpsecSecret.setText(this.mProfile.ipsecSecret);
        loadCertificates(this.mIpsecUserCert, "USRPKEY_", 2131624990, this.mProfile.ipsecUserCert);
        loadCertificates(this.mIpsecCaCert, "CACERT_", 2131626376, this.mProfile.ipsecCaCert);
        loadCertificates(this.mIpsecServerCert, "USRCERT_", 2131626377, this.mProfile.ipsecServerCert);
        this.mSaveLogin.setChecked(this.mProfile.saveLogin);
        this.mName.addTextChangedListener(this);
        this.mType.setOnItemSelectedListener(this);
        this.mServer.addTextChangedListener(this);
        this.mUsername.addTextChangedListener(this);
        this.mPassword.addTextChangedListener(this);
        this.mDnsServers.addTextChangedListener(this);
        this.mRoutes.addTextChangedListener(this);
        this.mIpsecSecret.addTextChangedListener(this);
        this.mIpsecUserCert.setOnItemSelectedListener(this);
        this.mShowOptions.setOnClickListener(this);
        this.mCancelBtn.setOnClickListener(this);
        this.mSaveBtn.setOnClickListener(this);
        boolean valid = validate(true);
        if (this.mEditing || !valid) {
            z = true;
        } else {
            z = false;
        }
        this.mEditing = z;
        if (this.mEditing) {
            getActivity().setTitle(2131626383);
            this.mView.findViewById(2131887395).setVisibility(0);
            changeType(this.mProfile.type);
            if (!(this.mProfile.searchDomains.isEmpty() && this.mProfile.dnsServers.isEmpty() && this.mProfile.routes.isEmpty())) {
                showOrHideAdvancedOptions();
            }
        } else {
            getActivity().setTitle(getString(2131626385, new Object[]{this.mProfile.name}));
            this.mView.findViewById(2131887417).setVisibility(0);
            this.mSaveBtn.setText(2131626381);
        }
        super.onCreate(savedState);
        Button button = this.mSaveBtn;
        if (!this.mEditing) {
            valid = validate(false);
        }
        button.setEnabled(valid);
        getActivity().getWindow().setSoftInputMode(20);
        setPasswordView(this.mPassword, this.mView);
        this.mShowOptions.setChecked(this.mShowOptionsChecked);
        showOrHideAdvancedOptions();
        return this.mView;
    }

    public void afterTextChanged(Editable field) {
        this.mSaveBtn.setEnabled(validate(this.mEditing));
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case 2131886342:
                getActivity().onBackPressed();
                return;
            case 2131886520:
                VpnProfile profile = getProfile();
                this.mKeyStore.put("VPN_" + profile.key, profile.encode(), -1, 0);
                Intent intent = new Intent();
                intent.putExtra("profile", profile);
                intent.putExtra("VpnEditing", this.mEditing);
                ((SettingsActivity) getActivity()).finishPreferencePanel(this, -1, intent);
                return;
            case 2131887409:
                showOrHideAdvancedOptions();
                return;
            default:
                return;
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == this.mType) {
            changeType(position);
        }
        this.mSaveBtn.setEnabled(validate(this.mEditing));
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    private void showOrHideAdvancedOptions() {
        if (this.mShowOptions.isChecked()) {
            this.mView.findViewById(2131887410).setVisibility(0);
            this.mShowOptionsChecked = true;
            return;
        }
        this.mView.findViewById(2131887410).setVisibility(8);
        this.mShowOptionsChecked = false;
    }

    private void changeType(int type) {
        this.mMppe.setVisibility(8);
        this.mView.findViewById(2131887398).setVisibility(8);
        this.mView.findViewById(2131887400).setVisibility(8);
        this.mView.findViewById(2131887403).setVisibility(8);
        this.mView.findViewById(2131887405).setVisibility(8);
        switch (type) {
            case 0:
                this.mMppe.setVisibility(0);
                return;
            case 1:
                if (this.mHwCustVpnAddFragment != null) {
                    this.mHwCustVpnAddFragment.setL2TPVisibility(this.mView, 2131887398);
                    return;
                }
                break;
            case 2:
                break;
            case 3:
                this.mView.findViewById(2131887398).setVisibility(0);
                this.mView.findViewById(2131887403).setVisibility(0);
                this.mView.findViewById(2131887405).setVisibility(0);
                return;
            case 4:
                this.mView.findViewById(2131887400).setVisibility(0);
                return;
            case 5:
                this.mView.findViewById(2131887403).setVisibility(0);
                this.mView.findViewById(2131887405).setVisibility(0);
                return;
            case 6:
                this.mView.findViewById(2131887405).setVisibility(0);
                return;
            default:
                return;
        }
        this.mView.findViewById(2131887398).setVisibility(0);
        this.mView.findViewById(2131887400).setVisibility(0);
    }

    private boolean validate(boolean editing) {
        boolean z = true;
        boolean z2 = false;
        if (!editing) {
            if (this.mUsername.getText().length() == 0 || this.mPassword.getText().length() == 0) {
                z = false;
            }
            return z;
        } else if (this.mName.getText().length() == 0 || this.mServer.getText().length() == 0 || !validateAddresses(this.mDnsServers.getText().toString(), false) || !validateAddresses(this.mRoutes.getText().toString(), true)) {
            return false;
        } else {
            switch (this.mType.getSelectedItemPosition()) {
                case 0:
                    return true;
                case 1:
                    if (this.mHwCustVpnAddFragment != null) {
                        return this.mHwCustVpnAddFragment.isSupportL2TP();
                    }
                    break;
                case 2:
                    if (!TextUtils.isEmpty(this.mIpsecSecret.getText().toString())) {
                        z2 = true;
                    }
                    return z2;
                case 3:
                    if (this.mIpsecUserCert.getSelectedItemPosition() == 0) {
                        z = false;
                    }
                    return z;
                case 4:
                    if (this.mIpsecSecret.getText().length() == 0) {
                        z = false;
                    }
                    return z;
                case 5:
                    if (this.mIpsecUserCert.getSelectedItemPosition() == 0) {
                        z = false;
                    }
                    return z;
                case 6:
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    private boolean validateAddresses(String addresses, boolean cidr) {
        try {
            for (String address : addresses.split(" ")) {
                String address2;
                if (!address2.isEmpty()) {
                    int prefixLength = 32;
                    if (cidr) {
                        String[] parts = address2.split("/", 2);
                        address2 = parts[0];
                        prefixLength = Integer.parseInt(parts[1]);
                    }
                    byte[] bytes = InetAddress.parseNumericAddress(address2).getAddress();
                    int integer = (((bytes[3] & 255) | ((bytes[2] & 255) << 8)) | ((bytes[1] & 255) << 16)) | ((bytes[0] & 255) << 24);
                    if (bytes.length == 4 && prefixLength >= 0 && prefixLength <= 32) {
                        if (prefixLength < 32 && (integer << prefixLength) != 0) {
                        }
                    }
                    return false;
                }
            }
            return true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }
    }

    private void loadCertificates(Spinner spinner, String prefix, int firstId, String selected) {
        Context context = getActivity();
        String first = firstId == 0 ? "" : context.getString(firstId);
        String[] certificates = this.mKeyStore.list(prefix);
        if (certificates == null || certificates.length == 0) {
            certificates = new String[]{first};
        } else {
            String[] array = new String[(certificates.length + 1)];
            array[0] = first;
            System.arraycopy(certificates, 0, array, 1, certificates.length);
            certificates = array;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter(context, 17367048, certificates);
        adapter.setDropDownViewResource(17367049);
        spinner.setAdapter(adapter);
        for (int i = 1; i < certificates.length; i++) {
            if (certificates[i].equals(selected)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    boolean isEditing() {
        return this.mEditing;
    }

    VpnProfile getProfile() {
        VpnProfile profile = new VpnProfile(this.mProfile.key);
        profile.name = this.mName.getText().toString();
        profile.type = this.mType.getSelectedItemPosition();
        profile.server = this.mServer.getText().toString().trim();
        profile.username = this.mUsername.getText().toString();
        profile.password = this.mPassword.getText().toString();
        profile.searchDomains = this.mSearchDomains.getText().toString().trim();
        profile.dnsServers = this.mDnsServers.getText().toString().trim();
        profile.routes = this.mRoutes.getText().toString().trim();
        switch (profile.type) {
            case 0:
                profile.mppe = this.mMppe.isChecked();
                break;
            case 1:
                if (this.mHwCustVpnAddFragment != null) {
                    this.mHwCustVpnAddFragment.getL2TPText(profile, this.mL2tpSecret);
                    break;
                }
            case 2:
                profile.l2tpSecret = this.mL2tpSecret.getText().toString();
                profile.ipsecIdentifier = this.mIpsecIdentifier.getText().toString();
                profile.ipsecSecret = this.mIpsecSecret.getText().toString();
                break;
            case 3:
                profile.l2tpSecret = this.mL2tpSecret.getText().toString();
                if (this.mIpsecUserCert.getSelectedItemPosition() != 0) {
                    profile.ipsecUserCert = (String) this.mIpsecUserCert.getSelectedItem();
                }
                if (this.mIpsecCaCert.getSelectedItemPosition() != 0) {
                    profile.ipsecCaCert = (String) this.mIpsecCaCert.getSelectedItem();
                }
                if (this.mIpsecServerCert.getSelectedItemPosition() != 0) {
                    profile.ipsecServerCert = (String) this.mIpsecServerCert.getSelectedItem();
                    break;
                }
                break;
            case 4:
                profile.ipsecIdentifier = this.mIpsecIdentifier.getText().toString();
                profile.ipsecSecret = this.mIpsecSecret.getText().toString();
                break;
            case 5:
                if (this.mIpsecUserCert.getSelectedItemPosition() != 0) {
                    profile.ipsecUserCert = (String) this.mIpsecUserCert.getSelectedItem();
                }
                if (this.mIpsecCaCert.getSelectedItemPosition() != 0) {
                    profile.ipsecCaCert = (String) this.mIpsecCaCert.getSelectedItem();
                }
                if (this.mIpsecServerCert.getSelectedItemPosition() != 0) {
                    profile.ipsecServerCert = (String) this.mIpsecServerCert.getSelectedItem();
                    break;
                }
                break;
            case 6:
                if (this.mIpsecCaCert.getSelectedItemPosition() != 0) {
                    profile.ipsecCaCert = (String) this.mIpsecCaCert.getSelectedItem();
                }
                if (this.mIpsecServerCert.getSelectedItemPosition() != 0) {
                    profile.ipsecServerCert = (String) this.mIpsecServerCert.getSelectedItem();
                    break;
                }
                break;
        }
        profile.saveLogin = this.mSaveLogin.isChecked();
        return profile;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            PreferenceFrameLayout frameLayout = (PreferenceFrameLayout) getActivity().findViewById(16909261);
            if (frameLayout != null) {
                frameLayout.setPaddingRelative(0, frameLayout.getPaddingTop(), 0, frameLayout.getPaddingBottom());
            }
        }
    }

    private void initProfile() {
        this.mProfile = (VpnProfile) getArguments().getParcelable("profile");
        this.mEditing = getArguments().getBoolean("VpnEditing");
    }

    public void onResume() {
        boolean z = false;
        super.onResume();
        if (KeyStore.getInstance().isUnlocked()) {
            this.mUnlocking = false;
            return;
        }
        if (this.mUnlocking) {
            getActivity().finish();
        } else {
            Credentials.getInstance().unlock(getActivity());
        }
        if (!this.mUnlocking) {
            z = true;
        }
        this.mUnlocking = z;
    }

    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        VpnProfile profile = getProfile();
        savedState.putString("VpnKey", profile.key);
        savedState.putByteArray("VpnProfile", profile.encode());
        savedState.putBoolean("VpnEditing", isEditing());
        savedState.putBoolean("ShowOptions", this.mShowOptionsChecked);
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mType instanceof ListSpinner) {
            ((ListSpinner) this.mType).dismissDialog();
        }
        if (this.mIpsecUserCert instanceof ListSpinner) {
            ((ListSpinner) this.mIpsecUserCert).dismissDialog();
        }
        if (this.mIpsecCaCert instanceof ListSpinner) {
            ((ListSpinner) this.mIpsecCaCert).dismissDialog();
        }
        if (this.mIpsecServerCert instanceof ListSpinner) {
            ((ListSpinner) this.mIpsecServerCert).dismissDialog();
        }
    }

    public void setPasswordView(TextView passwordView, View root) {
        CheckBox showPassword = (CheckBox) root.findViewById(2131886368);
        if (showPassword != null) {
            int i;
            if (showPassword.isChecked()) {
                i = 144;
            } else {
                i = 128;
            }
            passwordView.setInputType(i | 1);
            showPassword.setOnCheckedChangeListener(new StaticOnCheckedChangeListener(passwordView));
        }
    }
}
