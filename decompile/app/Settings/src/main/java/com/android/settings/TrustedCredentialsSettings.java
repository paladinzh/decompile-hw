package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.net.http.SslCertificate;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.security.IKeyChainService;
import android.security.KeyChain;
import android.security.KeyChain.KeyChainConnection;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.app.UnlaunchableAppActivity;
import com.android.internal.util.ParcelableString;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.TrustedCredentialsDialogBuilder.DelegateInterface;
import com.android.settingslib.R$string;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

public class TrustedCredentialsSettings extends OptionsMenuFragment implements DelegateInterface {
    private Set<AliasLoader> mAliasLoaders = new ArraySet(2);
    private AliasOperation mAliasOperation;
    private ArraySet<Integer> mConfirmedCredentialUsers;
    private IntConsumer mConfirmingCredentialListener;
    private int mConfirmingCredentialUser;
    private ArrayList<GroupAdapter> mGroupAdapters = new ArrayList(2);
    private final SparseArray<KeyChainConnection> mKeyChainConnectionByProfileId = new SparseArray();
    private KeyguardManager mKeyguardManager;
    private AlertDialog mRemoveDialog;
    private TabHost mTabHost;
    private int mTrustAllCaUserId;
    private AlertDialog mTrustedCertDialog;
    private AlertDialog mTrustedCredentialsDialog;
    private UserManager mUserManager;
    private BroadcastReceiver mWorkProfileChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.MANAGED_PROFILE_AVAILABLE".equals(action) || "android.intent.action.MANAGED_PROFILE_UNAVAILABLE".equals(action) || "android.intent.action.MANAGED_PROFILE_UNLOCKED".equals(action)) {
                for (GroupAdapter adapter : TrustedCredentialsSettings.this.mGroupAdapters) {
                    adapter.load();
                }
            }
        }
    };

    private class AdapterData {
        private final GroupAdapter mAdapter;
        private final SparseArray<List<CertHolder>> mCertHoldersByUserId;
        private final Tab mTab;

        private class AliasLoader extends AsyncTask<Void, Integer, SparseArray<List<CertHolder>>> {
            private View mContentView;
            private Context mContext;
            private ProgressBar mProgressBar;

            public AliasLoader() {
                this.mContext = TrustedCredentialsSettings.this.getActivity();
                TrustedCredentialsSettings.this.mAliasLoaders.add(this);
                for (UserHandle profile : TrustedCredentialsSettings.this.mUserManager.getUserProfiles()) {
                    AdapterData.this.mCertHoldersByUserId.put(profile.getIdentifier(), new ArrayList());
                }
                AdapterData.this.mAdapter.notifyDataSetChanged();
            }

            private boolean shouldSkipProfile(UserHandle userHandle) {
                if (TrustedCredentialsSettings.this.mUserManager.isQuietModeEnabled(userHandle) || !TrustedCredentialsSettings.this.mUserManager.isUserUnlocked(userHandle.getIdentifier())) {
                    return true;
                }
                return false;
            }

            protected void onPreExecute() {
                View content = TrustedCredentialsSettings.this.mTabHost.getTabContentView();
                this.mProgressBar = (ProgressBar) content.findViewById(AdapterData.this.mTab.mProgress);
                this.mContentView = content.findViewById(AdapterData.this.mTab.mContentView);
                this.mProgressBar.setVisibility(0);
                this.mContentView.setVisibility(8);
            }

            protected SparseArray<List<CertHolder>> doInBackground(Void... params) {
                SparseArray<List<CertHolder>> certHoldersByProfile = new SparseArray();
                try {
                    int i;
                    UserHandle profile;
                    int profileId;
                    KeyChainConnection keyChainConnection;
                    List<ParcelableString> aliases;
                    List<UserHandle> profiles = TrustedCredentialsSettings.this.mUserManager.getUserProfiles();
                    int n = profiles.size();
                    SparseArray<List<ParcelableString>> aliasesByProfileId = new SparseArray(n);
                    int max = 0;
                    int progress = 0;
                    for (i = 0; i < n; i++) {
                        profile = (UserHandle) profiles.get(i);
                        profileId = profile.getIdentifier();
                        if (!shouldSkipProfile(profile)) {
                            TrustedCredentialsSettings.this.closeKeyChainConnection(profileId);
                            keyChainConnection = KeyChain.bindAsUser(this.mContext, profile);
                            TrustedCredentialsSettings.this.mKeyChainConnectionByProfileId.put(profileId, keyChainConnection);
                            aliases = AdapterData.this.mTab.getAliases(keyChainConnection.getService());
                            if (isCancelled()) {
                                return new SparseArray();
                            }
                            max += aliases.size();
                            aliasesByProfileId.put(profileId, aliases);
                        }
                    }
                    for (i = 0; i < n; i++) {
                        profile = (UserHandle) profiles.get(i);
                        profileId = profile.getIdentifier();
                        aliases = (List) aliasesByProfileId.get(profileId);
                        if (isCancelled()) {
                            return new SparseArray();
                        }
                        keyChainConnection = (KeyChainConnection) TrustedCredentialsSettings.this.mKeyChainConnectionByProfileId.get(profileId);
                        if (shouldSkipProfile(profile) || aliases == null || keyChainConnection == null) {
                            certHoldersByProfile.put(profileId, new ArrayList(0));
                        } else {
                            IKeyChainService service = keyChainConnection.getService();
                            List<CertHolder> certHolders = new ArrayList(max);
                            int aliasMax = aliases.size();
                            for (int j = 0; j < aliasMax; j++) {
                                String alias = ((ParcelableString) aliases.get(j)).string;
                                certHolders.add(new CertHolder(service, AdapterData.this.mAdapter, AdapterData.this.mTab, alias, KeyChain.toCertificate(service.getEncodedCaCertificate(alias, true)), profileId));
                                r2 = new Integer[2];
                                progress++;
                                r2[0] = Integer.valueOf(progress);
                                r2[1] = Integer.valueOf(max);
                                publishProgress(r2);
                            }
                            Collections.sort(certHolders);
                            certHoldersByProfile.put(profileId, certHolders);
                        }
                    }
                    return certHoldersByProfile;
                } catch (RemoteException e) {
                    Log.e("TrustedCredentialsSettings", "Remote exception while loading aliases." + e.toString());
                    return new SparseArray();
                } catch (InterruptedException e2) {
                    Log.e("TrustedCredentialsSettings", "InterruptedException while loading aliases." + e2.toString());
                    return new SparseArray();
                }
            }

            protected void onProgressUpdate(Integer... progressAndMax) {
                int progress = progressAndMax[0].intValue();
                int max = progressAndMax[1].intValue();
                if (max != this.mProgressBar.getMax()) {
                    this.mProgressBar.setMax(max);
                }
                this.mProgressBar.setProgress(progress);
            }

            protected void onPostExecute(SparseArray<List<CertHolder>> certHolders) {
                AdapterData.this.mCertHoldersByUserId.clear();
                int n = certHolders.size();
                for (int i = 0; i < n; i++) {
                    AdapterData.this.mCertHoldersByUserId.put(certHolders.keyAt(i), (List) certHolders.valueAt(i));
                }
                AdapterData.this.mAdapter.notifyDataSetChanged();
                this.mProgressBar.setVisibility(8);
                this.mContentView.setVisibility(0);
                this.mProgressBar.setProgress(0);
                TrustedCredentialsSettings.this.mAliasLoaders.remove(this);
                showTrustAllCaDialogIfNeeded();
            }

            protected void onCancelled() {
                TrustedCredentialsSettings.this.closeKeyChainConnections();
            }

            private boolean isUserTabAndTrustAllCertMode() {
                return TrustedCredentialsSettings.this.isTrustAllCaCertModeInProgress() && AdapterData.this.mTab == Tab.USER;
            }

            private void showTrustAllCaDialogIfNeeded() {
                if (isUserTabAndTrustAllCertMode()) {
                    List<CertHolder> certHolders = (List) AdapterData.this.mCertHoldersByUserId.get(TrustedCredentialsSettings.this.mTrustAllCaUserId);
                    if (certHolders != null) {
                        List<CertHolder> unapprovedUserCertHolders = new ArrayList();
                        DevicePolicyManager dpm = (DevicePolicyManager) this.mContext.getSystemService(DevicePolicyManager.class);
                        for (CertHolder cert : certHolders) {
                            if (!(cert == null || dpm.isCaCertApproved(cert.mAlias, TrustedCredentialsSettings.this.mTrustAllCaUserId))) {
                                unapprovedUserCertHolders.add(cert);
                            }
                        }
                        if (unapprovedUserCertHolders.size() == 0) {
                            Log.w("TrustedCredentialsSettings", "no cert is pending approval for user " + TrustedCredentialsSettings.this.mTrustAllCaUserId);
                        } else {
                            TrustedCredentialsSettings.this.showTrustAllCaDialog(unapprovedUserCertHolders);
                        }
                    }
                }
            }
        }

        private AdapterData(Tab tab, GroupAdapter adapter) {
            this.mCertHoldersByUserId = new SparseArray();
            this.mAdapter = adapter;
            this.mTab = tab;
        }

        public void remove(CertHolder certHolder) {
            if (this.mCertHoldersByUserId != null) {
                List<CertHolder> certs = (List) this.mCertHoldersByUserId.get(certHolder.mProfileId);
                if (certs != null) {
                    certs.remove(certHolder);
                }
            }
        }
    }

    private class AliasOperation extends AsyncTask<Void, Void, Boolean> {
        private final CertHolder mCertHolder;

        private AliasOperation(CertHolder certHolder) {
            this.mCertHolder = certHolder;
            TrustedCredentialsSettings.this.mAliasOperation = this;
        }

        protected Boolean doInBackground(Void... params) {
            try {
                IKeyChainService service = ((KeyChainConnection) TrustedCredentialsSettings.this.mKeyChainConnectionByProfileId.get(this.mCertHolder.mProfileId)).getService();
                if (!this.mCertHolder.mDeleted) {
                    return Boolean.valueOf(service.deleteCaCertificate(this.mCertHolder.mAlias));
                }
                service.installCaCertificate(this.mCertHolder.mX509Cert.getEncoded());
                return Boolean.valueOf(true);
            } catch (Exception e) {
                Log.w("TrustedCredentialsSettings", "Error while toggling alias " + this.mCertHolder.mAlias, e);
                return Boolean.valueOf(false);
            } catch (Exception e2) {
                e2.printStackTrace();
                return Boolean.valueOf(false);
            }
        }

        protected void onPostExecute(Boolean ok) {
            if (ok.booleanValue()) {
                if (this.mCertHolder.mTab.mSwitch) {
                    this.mCertHolder.mDeleted = !this.mCertHolder.mDeleted;
                } else {
                    this.mCertHolder.mAdapter.remove(this.mCertHolder);
                }
                this.mCertHolder.mAdapter.notifyDataSetChanged();
            } else {
                this.mCertHolder.mAdapter.load();
            }
            TrustedCredentialsSettings.this.mAliasOperation = null;
        }
    }

    static class CertHolder implements Comparable<CertHolder> {
        private final GroupAdapter mAdapter;
        private final String mAlias;
        private boolean mDeleted;
        public int mProfileId;
        private final IKeyChainService mService;
        private final SslCertificate mSslCert;
        private final String mSubjectPrimary;
        private final String mSubjectSecondary;
        private final Tab mTab;
        private final X509Certificate mX509Cert;

        private CertHolder(IKeyChainService service, GroupAdapter adapter, Tab tab, String alias, X509Certificate x509Cert, int profileId) {
            this.mProfileId = profileId;
            this.mService = service;
            this.mAdapter = adapter;
            this.mTab = tab;
            this.mAlias = alias;
            this.mX509Cert = x509Cert;
            this.mSslCert = new SslCertificate(x509Cert);
            String cn = this.mSslCert.getIssuedTo().getCName();
            String o = this.mSslCert.getIssuedTo().getOName();
            String ou = this.mSslCert.getIssuedTo().getUName();
            if (o.isEmpty()) {
                if (cn.isEmpty()) {
                    this.mSubjectPrimary = this.mSslCert.getIssuedTo().getDName();
                    this.mSubjectSecondary = "";
                } else {
                    this.mSubjectPrimary = cn;
                    this.mSubjectSecondary = "";
                }
            } else if (cn.isEmpty()) {
                this.mSubjectPrimary = o;
                this.mSubjectSecondary = ou;
            } else {
                this.mSubjectPrimary = o;
                this.mSubjectSecondary = cn;
            }
            try {
                this.mDeleted = this.mTab.deleted(this.mService, this.mAlias);
            } catch (RemoteException e) {
                Log.e("TrustedCredentialsSettings", "Remote exception while checking if alias " + this.mAlias + " is deleted.", e);
                this.mDeleted = false;
            }
        }

        public int compareTo(CertHolder o) {
            int primary = this.mSubjectPrimary.compareToIgnoreCase(o.mSubjectPrimary);
            if (primary != 0) {
                return primary;
            }
            return this.mSubjectSecondary.compareToIgnoreCase(o.mSubjectSecondary);
        }

        public boolean equals(Object o) {
            if (!(o instanceof CertHolder)) {
                return false;
            }
            return this.mAlias.equals(((CertHolder) o).mAlias);
        }

        public int hashCode() {
            return this.mAlias.hashCode();
        }

        public int getUserId() {
            return this.mProfileId;
        }

        public String getAlias() {
            return this.mAlias;
        }

        public boolean isSystemCert() {
            return this.mTab == Tab.SYSTEM;
        }

        public boolean isDeleted() {
            return this.mDeleted;
        }
    }

    private class ChildAdapter extends BaseAdapter implements OnClickListener, OnItemClickListener {
        private final int[] EMPTY_STATE_SET;
        private final int[] GROUP_EXPANDED_STATE_SET;
        private final LayoutParams HIDE_LAYOUT_PARAMS;
        private final LayoutParams SHOW_LAYOUT_PARAMS;
        private LinearLayout mContainerView;
        private final int mGroupPosition;
        private ViewGroup mHeaderView;
        private ImageView mIndicatorView;
        private boolean mIsListExpanded;
        private ListView mListView;
        private final DataSetObserver mObserver;
        private final GroupAdapter mParent;

        private ChildAdapter(GroupAdapter parent, int groupPosition) {
            this.GROUP_EXPANDED_STATE_SET = new int[]{16842920};
            this.EMPTY_STATE_SET = new int[0];
            this.HIDE_LAYOUT_PARAMS = new LayoutParams(-1, -2);
            this.SHOW_LAYOUT_PARAMS = new LayoutParams(-1, -1, 1.0f);
            this.mObserver = new DataSetObserver() {
                public void onChanged() {
                    super.onChanged();
                    super.notifyDataSetChanged();
                }

                public void onInvalidated() {
                    super.onInvalidated();
                    super.notifyDataSetInvalidated();
                }
            };
            this.mIsListExpanded = true;
            this.mParent = parent;
            this.mGroupPosition = groupPosition;
            this.mParent.registerDataSetObserver(this.mObserver);
        }

        public int getCount() {
            return this.mParent.getChildrenCount(this.mGroupPosition);
        }

        public CertHolder getItem(int position) {
            return this.mParent.getChild(this.mGroupPosition, position);
        }

        public long getItemId(int position) {
            return this.mParent.getChildId(this.mGroupPosition, position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return this.mParent.getChildView(this.mGroupPosition, position, false, convertView, parent);
        }

        public void notifyDataSetChanged() {
            this.mParent.notifyDataSetChanged();
        }

        public void notifyDataSetInvalidated() {
            this.mParent.notifyDataSetInvalidated();
        }

        public void onClick(View view) {
            boolean z = false;
            if (checkGroupExpandableAndStartWarningActivity() && !this.mIsListExpanded) {
                z = true;
            }
            this.mIsListExpanded = z;
            refreshViews();
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
            TrustedCredentialsSettings.this.showCertDialog(getItem(pos));
        }

        public void setContainerViewId(int viewId) {
            this.mContainerView = (LinearLayout) TrustedCredentialsSettings.this.mTabHost.findViewById(viewId);
            this.mContainerView.setVisibility(0);
            this.mListView = (ListView) this.mContainerView.findViewById(2131887264);
            this.mListView.setAdapter(this);
            this.mListView.setOnItemClickListener(this);
            this.mHeaderView = (ViewGroup) this.mContainerView.findViewById(2131887260);
            this.mHeaderView.setOnClickListener(this);
            this.mIndicatorView = (ImageView) this.mHeaderView.findViewById(2131887262);
            this.mIndicatorView.setImageDrawable(getGroupIndicator());
            FrameLayout headerContentContainer = (FrameLayout) this.mHeaderView.findViewById(2131887263);
            headerContentContainer.addView(this.mParent.getGroupView(this.mGroupPosition, true, null, headerContentContainer));
        }

        public void showHeader(boolean showHeader) {
            this.mHeaderView.setVisibility(showHeader ? 0 : 8);
        }

        public void showDivider(boolean showDivider) {
            this.mHeaderView.findViewById(2131887261).setVisibility(showDivider ? 0 : 8);
        }

        public void prepare() {
            this.mIsListExpanded = this.mParent.checkGroupExpandableAndStartWarningActivity(this.mGroupPosition, false);
            refreshViews();
        }

        private boolean checkGroupExpandableAndStartWarningActivity() {
            return this.mParent.checkGroupExpandableAndStartWarningActivity(this.mGroupPosition);
        }

        private void refreshViews() {
            int[] iArr;
            ViewGroup.LayoutParams layoutParams;
            ImageView imageView = this.mIndicatorView;
            if (this.mIsListExpanded) {
                iArr = this.GROUP_EXPANDED_STATE_SET;
            } else {
                iArr = this.EMPTY_STATE_SET;
            }
            imageView.setImageState(iArr, false);
            this.mListView.setVisibility(this.mIsListExpanded ? 0 : 8);
            LinearLayout linearLayout = this.mContainerView;
            if (this.mIsListExpanded) {
                layoutParams = this.SHOW_LAYOUT_PARAMS;
            } else {
                layoutParams = this.HIDE_LAYOUT_PARAMS;
            }
            linearLayout.setLayoutParams(layoutParams);
        }

        private Drawable getGroupIndicator() {
            TypedArray a = TrustedCredentialsSettings.this.getActivity().obtainStyledAttributes(null, R.styleable.ExpandableListView, 16842863, 0);
            Drawable groupIndicator = a.getDrawable(0);
            a.recycle();
            return groupIndicator;
        }
    }

    private class GroupAdapter extends BaseExpandableListAdapter implements OnGroupClickListener, OnChildClickListener {
        private final AdapterData mData;

        private class ViewHolder {
            private TextView mSubjectPrimaryView;
            private TextView mSubjectSecondaryView;
            private Switch mSwitch;

            private ViewHolder() {
            }
        }

        private GroupAdapter(Tab tab) {
            this.mData = new AdapterData(tab, this);
            load();
        }

        public int getGroupCount() {
            return this.mData.mCertHoldersByUserId.size();
        }

        public int getChildrenCount(int groupPosition) {
            List<CertHolder> certHolders = (List) this.mData.mCertHoldersByUserId.valueAt(groupPosition);
            if (certHolders != null) {
                return certHolders.size();
            }
            return 0;
        }

        public UserHandle getGroup(int groupPosition) {
            return new UserHandle(this.mData.mCertHoldersByUserId.keyAt(groupPosition));
        }

        public CertHolder getChild(int groupPosition, int childPosition) {
            return (CertHolder) ((List) this.mData.mCertHoldersByUserId.get(getUserIdByGroup(groupPosition))).get(childPosition);
        }

        public long getGroupId(int groupPosition) {
            return (long) getUserIdByGroup(groupPosition);
        }

        private int getUserIdByGroup(int groupPosition) {
            return this.mData.mCertHoldersByUserId.keyAt(groupPosition);
        }

        public UserInfo getUserInfoByGroup(int groupPosition) {
            return TrustedCredentialsSettings.this.mUserManager.getUserInfo(getUserIdByGroup(groupPosition));
        }

        public long getChildId(int groupPosition, int childPosition) {
            return (long) childPosition;
        }

        public boolean hasStableIds() {
            return false;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = Utils.inflateCategoryHeader((LayoutInflater) TrustedCredentialsSettings.this.getActivity().getSystemService("layout_inflater"), parent);
            }
            TextView title = (TextView) convertView.findViewById(16908310);
            if (getUserInfoByGroup(groupPosition).isManagedProfile()) {
                title.setText(R$string.category_work);
            } else {
                title.setText(R$string.category_personal);
            }
            title.setTextAlignment(6);
            return convertView;
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            return getViewForCertificate(getChild(groupPosition, childPosition), this.mData.mTab, convertView, parent);
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
            TrustedCredentialsSettings.this.showCertDialog(getChild(groupPosition, childPosition));
            return true;
        }

        public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long id) {
            return !checkGroupExpandableAndStartWarningActivity(groupPosition);
        }

        public void load() {
            if (TrustedCredentialsSettings.this.getActivity() != null) {
                AdapterData adapterData = this.mData;
                adapterData.getClass();
                new AliasLoader().execute(new Void[0]);
            }
        }

        public void remove(CertHolder certHolder) {
            this.mData.remove(certHolder);
        }

        public void setExpandableListView(ExpandableListView lv) {
            lv.setAdapter(this);
            lv.setOnGroupClickListener(this);
            lv.setOnChildClickListener(this);
            lv.setVisibility(0);
        }

        public ChildAdapter getChildAdapter(int groupPosition) {
            return new ChildAdapter(this, groupPosition);
        }

        public boolean checkGroupExpandableAndStartWarningActivity(int groupPosition) {
            return checkGroupExpandableAndStartWarningActivity(groupPosition, true);
        }

        public boolean checkGroupExpandableAndStartWarningActivity(int groupPosition, boolean startActivity) {
            UserHandle groupUser = getGroup(groupPosition);
            int groupUserId = groupUser.getIdentifier();
            if (TrustedCredentialsSettings.this.mUserManager.isQuietModeEnabled(groupUser)) {
                Intent intent = UnlaunchableAppActivity.createInQuietModeDialogIntent(groupUserId);
                if (startActivity) {
                    TrustedCredentialsSettings.this.getActivity().startActivity(intent);
                }
                return false;
            } else if (TrustedCredentialsSettings.this.mUserManager.isUserUnlocked(groupUser) || !new LockPatternUtils(TrustedCredentialsSettings.this.getActivity()).isSeparateProfileChallengeEnabled(groupUserId)) {
                return true;
            } else {
                if (startActivity) {
                    TrustedCredentialsSettings.this.startConfirmCredential(groupUserId);
                }
                return false;
            }
        }

        private View getViewForCertificate(CertHolder certHolder, Tab mTab, View convertView, ViewGroup parent) {
            ViewHolder holder;
            boolean z = true;
            if (convertView == null) {
                convertView = LayoutInflater.from(TrustedCredentialsSettings.this.getActivity()).inflate(2130969214, parent, false);
                holder = new ViewHolder();
                holder.mSubjectPrimaryView = (TextView) convertView.findViewById(2131887256);
                holder.mSubjectSecondaryView = (TextView) convertView.findViewById(2131887257);
                holder.mSwitch = (Switch) convertView.findViewById(2131887258);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.mSubjectPrimaryView.setText(certHolder.mSubjectPrimary);
            holder.mSubjectSecondaryView.setText(certHolder.mSubjectSecondary);
            if (mTab.mSwitch) {
                boolean z2;
                Switch -get2 = holder.mSwitch;
                if (certHolder.mDeleted) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                -get2.setChecked(z2);
                Switch -get22 = holder.mSwitch;
                if (TrustedCredentialsSettings.this.mUserManager.hasUserRestriction("no_config_credentials", new UserHandle(certHolder.mProfileId))) {
                    z = false;
                }
                -get22.setEnabled(z);
                holder.mSwitch.setVisibility(0);
            }
            return convertView;
        }
    }

    private enum Tab {
        SYSTEM("system", 2131626406, 2131887265, 2131887266, 2131887268, 2131887269, 2131887270, 2131887267, true),
        USER("user", 2131626407, 2131887271, 2131887272, 2131887274, 2131887275, 2131887276, 2131887273, false);
        
        private final int mContentView;
        private final int mExpandableList;
        private final int mLabel;
        private final int mPersonalList;
        private final int mProgress;
        private final boolean mSwitch;
        private final String mTag;
        private final int mView;
        private final int mWorkList;

        private Tab(String tag, int label, int view, int progress, int personalList, int workList, int expandableList, int contentView, boolean withSwitch) {
            this.mTag = tag;
            this.mLabel = label;
            this.mView = view;
            this.mProgress = progress;
            this.mPersonalList = personalList;
            this.mWorkList = workList;
            this.mExpandableList = expandableList;
            this.mContentView = contentView;
            this.mSwitch = withSwitch;
        }

        private List<ParcelableString> getAliases(IKeyChainService service) throws RemoteException {
            switch (-getcom-android-settings-TrustedCredentialsSettings$TabSwitchesValues()[ordinal()]) {
                case 1:
                    return service.getSystemCaAliases().getList();
                case 2:
                    return service.getUserCaAliases().getList();
                default:
                    throw new AssertionError();
            }
        }

        private boolean deleted(IKeyChainService service, String alias) throws RemoteException {
            boolean z = false;
            switch (-getcom-android-settings-TrustedCredentialsSettings$TabSwitchesValues()[ordinal()]) {
                case 1:
                    if (!service.containsCaAlias(alias)) {
                        z = true;
                    }
                    return z;
                case 2:
                    return false;
                default:
                    throw new AssertionError();
            }
        }
    }

    protected int getMetricsCategory() {
        return 92;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mUserManager = (UserManager) getActivity().getSystemService("user");
        this.mKeyguardManager = (KeyguardManager) getActivity().getSystemService("keyguard");
        this.mTrustAllCaUserId = getActivity().getIntent().getIntExtra("ARG_SHOW_NEW_FOR_USER", -10000);
        this.mConfirmedCredentialUsers = new ArraySet(2);
        this.mConfirmingCredentialUser = -10000;
        if (savedInstanceState != null) {
            this.mConfirmingCredentialUser = savedInstanceState.getInt("ConfirmingCredentialUser", -10000);
            ArrayList<Integer> users = savedInstanceState.getIntegerArrayList("ConfirmedCredentialUsers");
            if (users != null) {
                this.mConfirmedCredentialUsers.addAll(users);
            }
        }
        this.mConfirmingCredentialListener = null;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
        filter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        filter.addAction("android.intent.action.MANAGED_PROFILE_UNLOCKED");
        getActivity().registerReceiver(this.mWorkProfileChangedReceiver, filter);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList("ConfirmedCredentialUsers", new ArrayList(this.mConfirmedCredentialUsers));
        outState.putInt("ConfirmingCredentialUser", this.mConfirmingCredentialUser);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        this.mTabHost = (TabHost) inflater.inflate(2130969217, parent, false);
        this.mTabHost.setup();
        addTab(Tab.SYSTEM);
        addTab(Tab.USER);
        if (getActivity().getIntent() != null && "com.android.settings.TRUSTED_CREDENTIALS_USER".equals(getActivity().getIntent().getAction())) {
            this.mTabHost.setCurrentTabByTag(Tab.USER.mTag);
        }
        return this.mTabHost;
    }

    public void onDestroy() {
        getActivity().unregisterReceiver(this.mWorkProfileChangedReceiver);
        for (AliasLoader aliasLoader : this.mAliasLoaders) {
            aliasLoader.cancel(true);
        }
        this.mAliasLoaders.clear();
        this.mGroupAdapters.clear();
        if (this.mAliasOperation != null) {
            this.mAliasOperation.cancel(true);
            this.mAliasOperation = null;
        }
        closeKeyChainConnections();
        if (this.mTrustedCredentialsDialog != null && this.mTrustedCredentialsDialog.isShowing()) {
            this.mTrustedCredentialsDialog.dismiss();
        }
        if (this.mTrustedCertDialog != null && this.mTrustedCertDialog.isShowing()) {
            this.mTrustedCertDialog.dismiss();
        }
        if (this.mRemoveDialog != null && this.mRemoveDialog.isShowing()) {
            this.mRemoveDialog.dismiss();
        }
        super.onDestroy();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            int userId = this.mConfirmingCredentialUser;
            IntConsumer listener = this.mConfirmingCredentialListener;
            this.mConfirmingCredentialUser = -10000;
            this.mConfirmingCredentialListener = null;
            if (resultCode == -1) {
                this.mConfirmedCredentialUsers.add(Integer.valueOf(userId));
                if (listener != null) {
                    listener.accept(userId);
                }
            }
        }
    }

    private void closeKeyChainConnections() {
        int n = this.mKeyChainConnectionByProfileId.size();
        for (int i = 0; i < n; i++) {
            ((KeyChainConnection) this.mKeyChainConnectionByProfileId.valueAt(i)).close();
        }
        this.mKeyChainConnectionByProfileId.clear();
    }

    private void closeKeyChainConnection(int profileId) {
        KeyChainConnection keyChainConnection = (KeyChainConnection) this.mKeyChainConnectionByProfileId.get(profileId);
        if (keyChainConnection != null) {
            keyChainConnection.close();
            this.mKeyChainConnectionByProfileId.delete(profileId);
        }
    }

    private void addTab(Tab tab) {
        int personalIndex = 0;
        this.mTabHost.addTab(this.mTabHost.newTabSpec(tab.mTag).setIndicator(getActivity().getString(tab.mLabel)).setContent(tab.mView));
        int profilesSize = this.mUserManager.getUserProfiles().size();
        GroupAdapter groupAdapter = new GroupAdapter(tab);
        this.mGroupAdapters.add(groupAdapter);
        if (profilesSize == 1) {
            ChildAdapter adapter = groupAdapter.getChildAdapter(0);
            adapter.setContainerViewId(tab.mPersonalList);
            adapter.prepare();
        } else if (profilesSize == 2) {
            int workIndex;
            if (groupAdapter.getUserInfoByGroup(1).isManagedProfile()) {
                workIndex = 1;
            } else {
                workIndex = 0;
            }
            if (workIndex != 1) {
                personalIndex = 1;
            }
            ChildAdapter personalAdapter = groupAdapter.getChildAdapter(personalIndex);
            personalAdapter.setContainerViewId(tab.mPersonalList);
            personalAdapter.showHeader(true);
            personalAdapter.prepare();
            ChildAdapter workAdapter = groupAdapter.getChildAdapter(workIndex);
            workAdapter.setContainerViewId(tab.mWorkList);
            workAdapter.showHeader(true);
            workAdapter.showDivider(true);
            workAdapter.prepare();
        } else if (profilesSize >= 3) {
            groupAdapter.setExpandableListView((ExpandableListView) this.mTabHost.findViewById(tab.mExpandableList));
        }
    }

    private boolean startConfirmCredential(int userId) {
        Intent newIntent = this.mKeyguardManager.createConfirmDeviceCredentialIntent(null, null, userId);
        if (newIntent == null) {
            return false;
        }
        this.mConfirmingCredentialUser = userId;
        startActivityForResult(newIntent, 1);
        return true;
    }

    public void showRemoveDialog(AlertDialog dialog) {
        this.mRemoveDialog = dialog;
    }

    private boolean isTrustAllCaCertModeInProgress() {
        return this.mTrustAllCaUserId != -10000;
    }

    private void showTrustAllCaDialog(List<CertHolder> unapprovedCertHolders) {
        this.mTrustedCredentialsDialog = new TrustedCredentialsDialogBuilder(getActivity(), this).setCertHolders((CertHolder[]) unapprovedCertHolders.toArray(new CertHolder[unapprovedCertHolders.size()])).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                Activity activity = TrustedCredentialsSettings.this.getActivity();
                if (activity != null) {
                    activity.getIntent().removeExtra("ARG_SHOW_NEW_FOR_USER");
                }
                TrustedCredentialsSettings.this.mTrustAllCaUserId = -10000;
            }
        }).show();
    }

    private void showCertDialog(CertHolder certHolder) {
        this.mTrustedCertDialog = new TrustedCredentialsDialogBuilder(getActivity(), this).setCertHolder(certHolder).show();
    }

    public List<X509Certificate> getX509CertsFromCertHolder(CertHolder certHolder) {
        RemoteException ex;
        Exception e;
        List<X509Certificate> list = null;
        try {
            IKeyChainService service = ((KeyChainConnection) this.mKeyChainConnectionByProfileId.get(certHolder.mProfileId)).getService();
            List<String> chain = service.getCaCertificateChainAliases(certHolder.mAlias, true);
            int n = chain.size();
            List<X509Certificate> certificates = new ArrayList(n);
            int i = 0;
            while (i < n) {
                try {
                    certificates.add(KeyChain.toCertificate(service.getEncodedCaCertificate((String) chain.get(i), true)));
                    i++;
                } catch (RemoteException e2) {
                    ex = e2;
                    list = certificates;
                } catch (Exception e3) {
                    e = e3;
                    list = certificates;
                }
            }
            return certificates;
        } catch (RemoteException e4) {
            ex = e4;
            Log.e("TrustedCredentialsSettings", "RemoteException while retrieving certificate chain for root " + certHolder.mAlias, ex);
            return list;
        } catch (Exception e5) {
            e = e5;
            e.printStackTrace();
            return list;
        }
    }

    public void removeOrInstallCert(CertHolder certHolder) {
        new AliasOperation(certHolder).execute(new Void[0]);
    }

    public boolean startConfirmCredentialIfNotConfirmed(int userId, IntConsumer onCredentialConfirmedListener) {
        if (this.mConfirmedCredentialUsers.contains(Integer.valueOf(userId))) {
            return false;
        }
        boolean result = startConfirmCredential(userId);
        if (result) {
            this.mConfirmingCredentialListener = onCredentialConfirmedListener;
        }
        return result;
    }
}
