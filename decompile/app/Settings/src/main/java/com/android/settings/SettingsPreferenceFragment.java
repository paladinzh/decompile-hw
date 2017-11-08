package com.android.settings;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.XmlRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceGroupAdapter;
import android.support.v7.preference.PreferenceRecyclerViewAccessibilityDelegate;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.R$layout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.settings.CustomDialogPreference.CustomPreferenceDialogFragment;
import com.android.settings.CustomListPreference.CustomListPreferenceDialogFragment;
import com.android.settings.RestrictedListPreference.RestrictedListPreferenceDialogFragment;
import com.android.settings.applications.LayoutPreference;
import com.android.settingslib.HelpUtils;
import java.util.UUID;

public abstract class SettingsPreferenceFragment extends InstrumentedPreferenceFragment implements DialogCreatable {
    private HighlightablePreferenceGroupAdapter mAdapter;
    private boolean mAnimationAllowed;
    private ViewGroup mButtonBar;
    private ContentResolver mContentResolver;
    private Adapter mCurrentRootAdapter;
    private AdapterDataObserver mDataSetObserver = new AdapterDataObserver() {
        public void onChanged() {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }
    };
    private SettingsDialogFragment mDialogFragment;
    private View mEmptyView;
    private LayoutPreference mFooter;
    private LayoutPreference mHeader;
    private String mHelpUri;
    private boolean mIsDataSetObserverRegistered = false;
    private LinearLayoutManager mLayoutManager;
    protected ViewGroup mPinnedHeaderFrameLayout;
    private ArrayMap<String, Preference> mPreferenceCache = new ArrayMap();
    private boolean mPreferenceHighlighted = false;
    private String mPreferenceKey;

    public static class HighlightablePreferenceGroupAdapter extends PreferenceGroupAdapter {
        private int mHighlightPosition = -1;

        public HighlightablePreferenceGroupAdapter(PreferenceGroup preferenceGroup) {
            super(preferenceGroup);
        }

        public void highlight(int position) {
            this.mHighlightPosition = position;
            notifyDataSetChanged();
        }

        public void onBindViewHolder(PreferenceViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            if (position == this.mHighlightPosition) {
                View v = holder.itemView;
                if (v.getBackground() != null) {
                    v.getBackground().setHotspot((float) (v.getWidth() / 2), (float) (v.getHeight() / 2));
                }
                v.setPressed(true);
                v.setPressed(false);
                this.mHighlightPosition = -1;
            }
        }
    }

    public static class SettingsDialogFragment extends DialogFragment {
        private int mDialogId;
        private OnCancelListener mOnCancelListener;
        private OnDismissListener mOnDismissListener;
        private Fragment mParentFragment;

        public SettingsDialogFragment(DialogCreatable fragment, int dialogId) {
            this.mDialogId = dialogId;
            if (fragment instanceof Fragment) {
                this.mParentFragment = (Fragment) fragment;
                return;
            }
            throw new IllegalArgumentException("fragment argument must be an instance of " + Fragment.class.getName());
        }

        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            if (this.mParentFragment != null) {
                outState.putInt("key_dialog_id", this.mDialogId);
                outState.putInt("key_parent_fragment_id", this.mParentFragment.getId());
            }
        }

        public void onStart() {
            super.onStart();
            if (this.mParentFragment != null && (this.mParentFragment instanceof SettingsPreferenceFragment)) {
                ((SettingsPreferenceFragment) this.mParentFragment).onDialogShowing();
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                this.mDialogId = savedInstanceState.getInt("key_dialog_id", 0);
                this.mParentFragment = getParentFragment();
                int mParentFragmentId = savedInstanceState.getInt("key_parent_fragment_id", -1);
                if (this.mParentFragment == null) {
                    this.mParentFragment = getFragmentManager().findFragmentById(mParentFragmentId);
                }
                if (!(this.mParentFragment instanceof DialogCreatable)) {
                    Object name;
                    StringBuilder stringBuilder = new StringBuilder();
                    if (this.mParentFragment != null) {
                        name = this.mParentFragment.getClass().getName();
                    } else {
                        name = Integer.valueOf(mParentFragmentId);
                    }
                    throw new IllegalArgumentException(stringBuilder.append(name).append(" must implement ").append(DialogCreatable.class.getName()).toString());
                } else if (this.mParentFragment instanceof SettingsPreferenceFragment) {
                    ((SettingsPreferenceFragment) this.mParentFragment).mDialogFragment = this;
                }
            }
            return ((DialogCreatable) this.mParentFragment).onCreateDialog(this.mDialogId);
        }

        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            if (this.mOnCancelListener != null) {
                this.mOnCancelListener.onCancel(dialog);
            }
        }

        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            if (this.mOnDismissListener != null) {
                this.mOnDismissListener.onDismiss(dialog);
            }
        }

        public int getDialogId() {
            return this.mDialogId;
        }

        public void onDetach() {
            super.onDetach();
            if ((this.mParentFragment instanceof SettingsPreferenceFragment) && ((SettingsPreferenceFragment) this.mParentFragment).mDialogFragment == this) {
                ((SettingsPreferenceFragment) this.mParentFragment).mDialogFragment = null;
            }
        }
    }

    public void onCreate(Bundle icicle) {
        int helpResource;
        super.onCreate(icicle);
        if (icicle != null) {
            this.mPreferenceHighlighted = icicle.getBoolean("android:preference_highlighted");
        }
        Bundle arguments = getArguments();
        if (arguments == null || !arguments.containsKey("help_uri_resource")) {
            helpResource = getHelpResource();
        } else {
            helpResource = arguments.getInt("help_uri_resource");
        }
        if (helpResource != 0) {
            this.mHelpUri = getResources().getString(helpResource);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        this.mPinnedHeaderFrameLayout = (ViewGroup) root.findViewById(2131886470);
        this.mButtonBar = (ViewGroup) root.findViewById(2131886327);
        RecyclerView listView = getListView();
        if (listView != null) {
            setDivider(null);
            listView.addItemDecoration(new SettingsDividerDecoration(getContext(), useNormalDividerOnly()));
        }
        return root;
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    public void addPreferencesFromResource(@XmlRes int preferencesResId) {
        super.addPreferencesFromResource(preferencesResId);
        checkAvailablePrefs(getPreferenceScreen());
    }

    public boolean useNormalDividerOnly() {
        return false;
    }

    private void checkAvailablePrefs(PreferenceGroup preferenceGroup) {
        if (preferenceGroup != null) {
            for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
                Preference pref = preferenceGroup.getPreference(i);
                if ((pref instanceof SelfAvailablePreference) && !((SelfAvailablePreference) pref).isAvailable(getContext())) {
                    preferenceGroup.removePreference(pref);
                } else if (pref instanceof PreferenceGroup) {
                    checkAvailablePrefs((PreferenceGroup) pref);
                }
            }
        }
    }

    public View setPinnedHeaderView(int layoutResId) {
        View pinnedHeader = getActivity().getLayoutInflater().inflate(layoutResId, this.mPinnedHeaderFrameLayout, false);
        setPinnedHeaderView(pinnedHeader);
        return pinnedHeader;
    }

    public void setPinnedHeaderView(View pinnedHeader) {
        this.mPinnedHeaderFrameLayout.addView(pinnedHeader);
        this.mPinnedHeaderFrameLayout.setVisibility(0);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("android:preference_highlighted", this.mPreferenceHighlighted);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onResume() {
        super.onResume();
        Bundle args = getArguments();
        if (args != null) {
            this.mPreferenceKey = args.getString(":settings:fragment_args_key");
            highlightPreferenceIfNeeded();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (16908332 == item.getItemId()) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onBindPreferences() {
        registerObserverIfNeeded();
    }

    protected void onUnbindPreferences() {
        unregisterObserverIfNeeded();
    }

    public void setLoading(boolean loading, boolean animate) {
        Utils.handleLoadingContainer(getView().findViewById(2131886754), getListView(), !loading, animate);
    }

    public void registerObserverIfNeeded() {
        if (!this.mIsDataSetObserverRegistered) {
            if (this.mCurrentRootAdapter != null) {
                this.mCurrentRootAdapter.unregisterAdapterDataObserver(this.mDataSetObserver);
            }
            this.mCurrentRootAdapter = getListView().getAdapter();
            this.mCurrentRootAdapter.registerAdapterDataObserver(this.mDataSetObserver);
            this.mIsDataSetObserverRegistered = true;
            onDataSetChanged();
        }
    }

    public void unregisterObserverIfNeeded() {
        if (this.mIsDataSetObserverRegistered) {
            if (this.mCurrentRootAdapter != null) {
                this.mCurrentRootAdapter.unregisterAdapterDataObserver(this.mDataSetObserver);
                this.mCurrentRootAdapter = null;
            }
            this.mIsDataSetObserverRegistered = false;
        }
    }

    public void highlightPreferenceIfNeeded() {
        if (isAdded() && !this.mPreferenceHighlighted && !TextUtils.isEmpty(this.mPreferenceKey)) {
            highlightPreference(this.mPreferenceKey);
        }
    }

    protected void onDataSetChanged() {
        highlightPreferenceIfNeeded();
        updateEmptyView();
    }

    public LayoutPreference getHeaderView() {
        return this.mHeader;
    }

    protected void setHeaderView(View view) {
        this.mHeader = new LayoutPreference(getPrefContext(), view);
        addPreferenceToTop(this.mHeader);
    }

    private void addPreferenceToTop(LayoutPreference preference) {
        preference.setOrder(-1);
        if (getPreferenceScreen() != null) {
            getPreferenceScreen().addPreference(preference);
        }
    }

    protected void setFooterView(View v) {
        LayoutPreference layoutPreference = null;
        if (v != null) {
            layoutPreference = new LayoutPreference(getPrefContext(), v);
        }
        setFooterView(layoutPreference);
    }

    private void setFooterView(LayoutPreference footer) {
        if (!(getPreferenceScreen() == null || this.mFooter == null)) {
            getPreferenceScreen().removePreference(this.mFooter);
        }
        if (footer != null) {
            this.mFooter = footer;
            this.mFooter.setOrder(2147483646);
            if (getPreferenceScreen() != null) {
                getPreferenceScreen().addPreference(this.mFooter);
                return;
            }
            return;
        }
        this.mFooter = null;
    }

    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (!(preferenceScreen == null || preferenceScreen.isAttached())) {
            preferenceScreen.setShouldUseGeneratedIds(this.mAnimationAllowed);
        }
        super.setPreferenceScreen(preferenceScreen);
        if (preferenceScreen != null) {
            if (this.mHeader != null) {
                preferenceScreen.addPreference(this.mHeader);
            }
            if (this.mFooter != null) {
                preferenceScreen.addPreference(this.mFooter);
            }
        }
    }

    private void updateEmptyView() {
        int i = 1;
        int i2 = 0;
        if (this.mEmptyView != null) {
            if (getPreferenceScreen() != null) {
                int i3;
                int preferenceCount = getPreferenceScreen().getPreferenceCount();
                if (this.mHeader != null) {
                    i3 = 1;
                } else {
                    i3 = 0;
                }
                i3 = preferenceCount - i3;
                if (this.mFooter == null) {
                    i = 0;
                }
                boolean show = i3 - i <= 0;
                View view = this.mEmptyView;
                if (!show) {
                    i2 = 8;
                }
                view.setVisibility(i2);
            } else {
                this.mEmptyView.setVisibility(0);
            }
        }
    }

    public void setEmptyView(View v) {
        if (this.mEmptyView != null) {
            this.mEmptyView.setVisibility(8);
        }
        this.mEmptyView = v;
        updateEmptyView();
    }

    private int canUseListViewForHighLighting(String key) {
        if (getListView() == null) {
            return -1;
        }
        Adapter adapter = getListView().getAdapter();
        if (adapter == null || !(adapter instanceof PreferenceGroupAdapter)) {
            return -1;
        }
        return findListPositionFromKey((PreferenceGroupAdapter) adapter, key);
    }

    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R$layout.preference_recyclerview, parent, false);
        recyclerView.setLayoutManager(onCreateLayoutManager());
        recyclerView.setAccessibilityDelegateCompat(new PreferenceRecyclerViewAccessibilityDelegate(recyclerView));
        return recyclerView;
    }

    public LayoutManager onCreateLayoutManager() {
        this.mLayoutManager = new LinearLayoutManager(getContext());
        return this.mLayoutManager;
    }

    protected Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        this.mAdapter = new HighlightablePreferenceGroupAdapter(preferenceScreen);
        return this.mAdapter;
    }

    protected void setAnimationAllowed(boolean animationAllowed) {
        this.mAnimationAllowed = animationAllowed;
    }

    protected void cacheRemoveAllPrefs(PreferenceGroup group) {
        this.mPreferenceCache = new ArrayMap();
        int N = group.getPreferenceCount();
        for (int i = 0; i < N; i++) {
            Preference p = group.getPreference(i);
            if (!TextUtils.isEmpty(p.getKey())) {
                this.mPreferenceCache.put(p.getKey(), p);
            }
        }
    }

    protected Preference getCachedPreference(String key) {
        return this.mPreferenceCache != null ? (Preference) this.mPreferenceCache.remove(key) : null;
    }

    protected void removeCachedPrefs(PreferenceGroup group) {
        for (Preference p : this.mPreferenceCache.values()) {
            group.removePreference(p);
        }
    }

    protected int getCachedCount() {
        return this.mPreferenceCache.size();
    }

    private void highlightPreference(String key) {
        final int position = canUseListViewForHighLighting(key);
        if (position >= 0) {
            this.mPreferenceHighlighted = true;
            this.mLayoutManager.scrollToPosition(position);
            getView().postDelayed(new Runnable() {
                public void run() {
                    SettingsPreferenceFragment.this.mAdapter.highlight(position);
                }
            }, 600);
        }
    }

    private int findListPositionFromKey(PreferenceGroupAdapter adapter, String key) {
        int count = adapter.getItemCount();
        for (int n = 0; n < count; n++) {
            String preferenceKey = adapter.getItem(n).getKey();
            if (preferenceKey != null && preferenceKey.equals(key)) {
                return n;
            }
        }
        return -1;
    }

    protected void removePreference(String key) {
        Preference pref = findPreference(key);
        if (pref != null) {
            getPreferenceScreen().removePreference(pref);
        }
    }

    protected int getHelpResource() {
        return 2131626522;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.mHelpUri != null && getActivity() != null) {
            HelpUtils.prepareHelpMenuItem(getActivity(), menu, this.mHelpUri, getClass().getName());
        }
    }

    public final void finishFragment() {
        getActivity().onBackPressed();
    }

    protected ContentResolver getContentResolver() {
        Context context = getActivity();
        if (context != null) {
            this.mContentResolver = context.getContentResolver();
        }
        return this.mContentResolver;
    }

    protected Object getSystemService(String name) {
        return getActivity().getSystemService(name);
    }

    protected PackageManager getPackageManager() {
        return getActivity().getPackageManager();
    }

    public void onDetach() {
        if (isRemoving() && this.mDialogFragment != null) {
            this.mDialogFragment.dismiss();
            this.mDialogFragment = null;
        }
        super.onDetach();
    }

    protected void showDialog(int dialogId) {
        if (this.mDialogFragment != null) {
            Log.e("SettingsPreference", "Old dialog fragment not null!");
        }
        Fragment f = getChildFragmentManager().findFragmentByTag(Integer.toString(dialogId));
        if (f == null || !f.isAdded()) {
            try {
                this.mDialogFragment = new SettingsDialogFragment(this, dialogId);
                this.mDialogFragment.show(getChildFragmentManager(), Integer.toString(dialogId));
            } catch (IllegalStateException exp) {
                Log.e("SettingsPreference", exp.toString());
            }
        }
    }

    public Dialog onCreateDialog(int dialogId) {
        return null;
    }

    protected void removeDialog(int dialogId) {
        if (this.mDialogFragment != null && this.mDialogFragment.getDialogId() == dialogId) {
            this.mDialogFragment.dismiss();
            this.mDialogFragment = null;
        }
    }

    protected void setOnCancelListener(OnCancelListener listener) {
        if (this.mDialogFragment != null) {
            this.mDialogFragment.mOnCancelListener = listener;
        }
    }

    protected void setOnDismissListener(OnDismissListener listener) {
        if (this.mDialogFragment != null) {
            this.mDialogFragment.mOnDismissListener = listener;
        }
    }

    public void onDialogShowing() {
    }

    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment f;
        if (preference.getKey() == null) {
            preference.setKey(UUID.randomUUID().toString());
        }
        if (preference instanceof RestrictedListPreference) {
            f = RestrictedListPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof CustomListPreference) {
            f = CustomListPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof CustomDialogPreference) {
            f = CustomPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof CustomEditTextPreference) {
            f = CustomEditTextPreference.CustomPreferenceDialogFragment.newInstance(preference.getKey());
        } else {
            super.onDisplayPreferenceDialog(preference);
            return;
        }
        f.setTargetFragment(this, 0);
        f.show(getFragmentManager(), "dialog_preference");
        onDialogShowing();
    }

    protected boolean hasNextButton() {
        try {
            return ((ButtonBarHandler) getActivity()).hasNextButton();
        } catch (Exception e) {
            return false;
        }
    }

    protected Button getNextButton() {
        try {
            return ((ButtonBarHandler) getActivity()).getNextButton();
        } catch (Exception e) {
            return null;
        }
    }

    public void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            } else {
                activity.finish();
            }
        }
    }

    protected Intent getIntent() {
        if (getActivity() == null) {
            return null;
        }
        return getActivity().getIntent();
    }

    protected void setResult(int result, Intent intent) {
        if (getActivity() != null) {
            getActivity().setResult(result, intent);
        }
    }

    protected void setResult(int result) {
        if (getActivity() != null) {
            getActivity().setResult(result);
        }
    }

    protected final Context getPrefContext() {
        return getPreferenceManager().getContext();
    }

    public boolean startFragment(Fragment caller, String fragmentClass, int titleRes, int requestCode, Bundle extras) {
        Activity activity = getActivity();
        if (activity instanceof SettingsActivity) {
            ((SettingsActivity) activity).startPreferencePanel(fragmentClass, extras, titleRes, null, caller, requestCode);
            return true;
        }
        Log.w("SettingsPreference", "Parent isn't SettingsActivity nor PreferenceActivity, thus there's no way to launch the given Fragment (name: " + fragmentClass + ", requestCode: " + requestCode + ")");
        return false;
    }

    public DialogFragment getDialogFragment() {
        return this.mDialogFragment;
    }

    protected boolean removePreference(PreferenceGroup group, String keyToRemove) {
        Preference pref = findPreference(keyToRemove);
        if (group == null || pref == null) {
            return false;
        }
        return group.removePreference(pref);
    }

    protected boolean removePreference(String groupKey, String keyToRemove) {
        return removePreference((PreferenceGroup) findPreference(groupKey), keyToRemove);
    }

    protected void removeEmptyCategory(String keyToRemove) {
        PreferenceGroup group = (PreferenceGroup) findPreference(keyToRemove);
        if (group != null && group.getPreferenceCount() <= 0) {
            removePreference(keyToRemove);
        }
    }

    public void applyLowPowerMode(boolean isLowPowerMode) {
    }
}
