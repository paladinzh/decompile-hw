package com.android.settings.print;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.Loader;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.database.DataSetObserver;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.print.PrintManager;
import android.print.PrintServicesLoader;
import android.print.PrinterDiscoverySession;
import android.print.PrinterDiscoverySession.OnPrintersChangeListener;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrintServiceInfo;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Filter;
import android.widget.Filter.FilterResults;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsExtUtils;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.Utils.ImmersionIcon;
import com.android.settings.widget.ToggleSwitch;
import com.android.settings.widget.ToggleSwitch.OnBeforeCheckedChangeListener;
import com.android.settings.wifi.WifiExtUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PrintServiceSettingsFragment extends SettingsPreferenceFragment implements LoaderCallbacks<List<PrintServiceInfo>> {
    private Intent mAddPrintersIntent;
    private ComponentName mComponentName;
    private final DataSetObserver mDataObserver = new DataSetObserver() {
        public void onChanged() {
            invalidateOptionsMenuIfNeeded();
            PrintServiceSettingsFragment.this.updateEmptyView();
        }

        public void onInvalidated() {
            invalidateOptionsMenuIfNeeded();
        }

        private void invalidateOptionsMenuIfNeeded() {
            int unfilteredItemCount = PrintServiceSettingsFragment.this.mPrintersAdapter.getUnfilteredCount();
            if (PrintServiceSettingsFragment.this.mLastUnfilteredItemCount > 0 || unfilteredItemCount <= 0) {
                if (PrintServiceSettingsFragment.this.mLastUnfilteredItemCount > 0 && unfilteredItemCount <= 0) {
                }
                PrintServiceSettingsFragment.this.mLastUnfilteredItemCount = unfilteredItemCount;
            }
            PrintServiceSettingsFragment.this.getActivity().invalidateOptionsMenu();
            PrintServiceSettingsFragment.this.mLastUnfilteredItemCount = unfilteredItemCount;
        }
    };
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    Activity activity = PrintServiceSettingsFragment.this.getActivity();
                    if (activity != null && !activity.isFinishing()) {
                        PrintServiceSettingsFragment.this.stopSearch();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private int mLastUnfilteredItemCount;
    private CharSequence mOldActivityTitle;
    private String mPreferenceKey;
    private PrintersAdapter mPrintersAdapter;
    private View mProgressView;
    private SearchView mSearchView;
    private boolean mSearching = true;
    private boolean mServiceEnabled;
    private final SettingsContentObserver mSettingsContentObserver = new SettingsContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            PrintServiceSettingsFragment.this.updateUiForServiceState();
        }
    };
    private Intent mSettingsIntent;
    private ToggleSwitch mToggleSwitch;

    private static abstract class SettingsContentObserver extends ContentObserver {
        public abstract void onChange(boolean z, Uri uri);

        public SettingsContentObserver(Handler handler) {
            super(handler);
        }

        public void register(ContentResolver contentResolver) {
            contentResolver.registerContentObserver(Secure.getUriFor("enabled_print_services"), false, this);
        }

        public void unregister(ContentResolver contentResolver) {
            contentResolver.unregisterContentObserver(this);
        }
    }

    private final class PrintersAdapter extends BaseAdapter implements LoaderCallbacks<List<PrinterInfo>>, Filterable {
        private final List<PrinterInfo> mFilteredPrinters;
        private CharSequence mLastSearchString;
        private final Object mLock;
        private final List<PrinterInfo> mPrinters;

        private PrintersAdapter() {
            this.mLock = new Object();
            this.mPrinters = new ArrayList();
            this.mFilteredPrinters = new ArrayList();
        }

        public void enable() {
            PrintServiceSettingsFragment.this.getLoaderManager().initLoader(1, null, this);
        }

        public void disable() {
            PrintServiceSettingsFragment.this.getLoaderManager().destroyLoader(1);
            if (!PrintServiceSettingsFragment.this.mToggleSwitch.isChecked()) {
                synchronized (this.mLock) {
                    this.mPrinters.clear();
                }
            }
        }

        public int getUnfilteredCount() {
            return this.mPrinters.size();
        }

        public Filter getFilter() {
            return new Filter() {
                protected FilterResults performFiltering(CharSequence constraint) {
                    synchronized (PrintersAdapter.this.mLock) {
                        if (TextUtils.isEmpty(constraint)) {
                            return null;
                        }
                        FilterResults results = new FilterResults();
                        List<PrinterInfo> filteredPrinters = new ArrayList();
                        String constraintLowerCase = constraint.toString().toLowerCase();
                        int printerCount = PrintersAdapter.this.mPrinters.size();
                        for (int i = 0; i < printerCount; i++) {
                            PrinterInfo printer = (PrinterInfo) PrintersAdapter.this.mPrinters.get(i);
                            String name = printer.getName();
                            if (name != null && name.toLowerCase().contains(constraintLowerCase)) {
                                filteredPrinters.add(printer);
                            }
                        }
                        results.values = filteredPrinters;
                        results.count = filteredPrinters.size();
                        return results;
                    }
                }

                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (PrintServiceSettingsFragment.this.getActivity() != null) {
                        synchronized (PrintersAdapter.this.mLock) {
                            PrintersAdapter.this.mLastSearchString = constraint;
                            PrintersAdapter.this.mFilteredPrinters.clear();
                            if (results == null) {
                                PrintersAdapter.this.mFilteredPrinters.addAll(PrintersAdapter.this.mPrinters);
                            } else {
                                PrintersAdapter.this.mFilteredPrinters.addAll(results.values);
                            }
                        }
                        PrintersAdapter.this.notifyDataSetChanged();
                    }
                }
            };
        }

        public int getCount() {
            int size;
            synchronized (this.mLock) {
                size = this.mFilteredPrinters.size();
            }
            return size;
        }

        public Object getItem(int position) {
            Object obj;
            synchronized (this.mLock) {
                obj = this.mFilteredPrinters.get(position);
            }
            return obj;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public boolean isActionable(int position) {
            return ((PrinterInfo) getItem(position)).getStatus() != 3;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = PrintServiceSettingsFragment.this.getActivity().getLayoutInflater().inflate(2130969013, parent, false);
            }
            convertView.setEnabled(isActionable(position));
            PrinterInfo printer = (PrinterInfo) getItem(position);
            CharSequence title = printer.getName();
            CharSequence subtitle = printer.getDescription();
            Drawable icon = PrintServiceSettingsFragment.this.getActivity().getResources().getDrawable(2130838215);
            ((TextView) convertView.findViewById(16908310)).setText(title);
            TextView subtitleView = (TextView) convertView.findViewById(16908304);
            if (TextUtils.isEmpty(subtitle)) {
                subtitleView.setText(null);
                subtitleView.setVisibility(8);
            } else {
                subtitleView.setText(subtitle);
                subtitleView.setVisibility(0);
            }
            ImageView iconView = (ImageView) convertView.findViewById(16908294);
            if (icon != null) {
                iconView.setVisibility(0);
                if (!isActionable(position)) {
                    icon.mutate();
                    TypedValue value = new TypedValue();
                    PrintServiceSettingsFragment.this.getActivity().getTheme().resolveAttribute(16842803, value, true);
                    icon.setAlpha((int) (value.getFloat() * 255.0f));
                }
                iconView.setImageDrawable(icon);
            } else {
                iconView.setVisibility(8);
            }
            return convertView;
        }

        public Loader<List<PrinterInfo>> onCreateLoader(int id, Bundle args) {
            if (id != 1) {
                return null;
            }
            synchronized (this.mLock) {
                this.mPrinters.clear();
                this.mFilteredPrinters.clear();
            }
            PrintServiceSettingsFragment.this.mHandler.removeMessages(1000);
            PrintServiceSettingsFragment.this.mHandler.sendEmptyMessageDelayed(1000, 30000);
            return new PrintersLoader(PrintServiceSettingsFragment.this.getActivity());
        }

        public void onLoadFinished(Loader<List<PrinterInfo>> loader, List<PrinterInfo> printers) {
            synchronized (this.mLock) {
                this.mPrinters.clear();
                int printerCount = printers.size();
                for (int i = 0; i < printerCount; i++) {
                    PrinterInfo printer = (PrinterInfo) printers.get(i);
                    if (printer.getId().getServiceName().equals(PrintServiceSettingsFragment.this.mComponentName)) {
                        this.mPrinters.add(printer);
                    }
                }
                this.mFilteredPrinters.clear();
                this.mFilteredPrinters.addAll(this.mPrinters);
                if (!TextUtils.isEmpty(this.mLastSearchString)) {
                    getFilter().filter(this.mLastSearchString);
                }
            }
            notifyDataSetChanged();
        }

        public void onLoaderReset(Loader<List<PrinterInfo>> loader) {
            synchronized (this.mLock) {
                if (!PrintServiceSettingsFragment.this.mToggleSwitch.isChecked()) {
                    this.mPrinters.clear();
                    this.mFilteredPrinters.clear();
                    this.mLastSearchString = null;
                }
            }
            notifyDataSetInvalidated();
        }
    }

    private static class PrintersLoader extends Loader<List<PrinterInfo>> {
        private PrinterDiscoverySession mDiscoverySession;
        private final Map<PrinterId, PrinterInfo> mPrinters = new LinkedHashMap();

        public PrintersLoader(Context context) {
            super(context);
        }

        public void deliverResult(List<PrinterInfo> printers) {
            if (isStarted()) {
                super.deliverResult(printers);
            }
        }

        protected void onStartLoading() {
            if (!this.mPrinters.isEmpty()) {
                deliverResult(new ArrayList(this.mPrinters.values()));
            }
            onForceLoad();
        }

        protected void onStopLoading() {
            onCancelLoad();
        }

        protected void onForceLoad() {
            loadInternal();
        }

        protected boolean onCancelLoad() {
            return cancelInternal();
        }

        protected void onReset() {
            super.onReset();
            onStopLoading();
            this.mPrinters.clear();
            if (this.mDiscoverySession != null) {
                this.mDiscoverySession.destroy();
                this.mDiscoverySession = null;
            }
        }

        protected void onAbandon() {
            onStopLoading();
        }

        private boolean cancelInternal() {
            if (this.mDiscoverySession == null || !this.mDiscoverySession.isPrinterDiscoveryStarted()) {
                return false;
            }
            this.mDiscoverySession.stopPrinterDiscovery();
            return true;
        }

        private void loadInternal() {
            if (this.mDiscoverySession == null) {
                this.mDiscoverySession = ((PrintManager) getContext().getSystemService("print")).createPrinterDiscoverySession();
                this.mDiscoverySession.setOnPrintersChangeListener(new OnPrintersChangeListener() {
                    public void onPrintersChanged() {
                        PrintersLoader.this.deliverResult(new ArrayList(PrintersLoader.this.mDiscoverySession.getPrinters()));
                    }
                });
            }
            this.mDiscoverySession.startPrinterDiscovery(null);
        }
    }

    protected int getMetricsCategory() {
        return 79;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mServiceEnabled = getArguments().getBoolean("EXTRA_CHECKED");
        String title = getArguments().getString("EXTRA_TITLE");
        if (!TextUtils.isEmpty(title)) {
            getActivity().setTitle(title);
        }
    }

    public void onStart() {
        super.onStart();
        if (WifiExtUtils.isWifiConnected(getActivity())) {
            this.mSearching = true;
        } else {
            this.mSearching = false;
            this.mPrintersAdapter.disable();
        }
        if (this.mToggleSwitch.isChecked()) {
            this.mProgressView.setVisibility(0);
        }
        this.mSettingsContentObserver.register(getContentResolver());
        updateEmptyView();
        updateUiForServiceState();
    }

    public void onPause() {
        this.mSettingsContentObserver.unregister(getContentResolver());
        if (this.mSearchView != null) {
            synchronized (this.mPrintersAdapter.mLock) {
                this.mPrintersAdapter.mPrinters.clear();
                this.mPrintersAdapter.getFilter().filter(null);
            }
            this.mSearchView.setOnQueryTextListener(null);
        }
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    public void onStop() {
        super.onStop();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initComponents();
        updateUiForArguments();
        if (getBackupListView() != null) {
            getBackupListView().setVisibility(0);
        }
    }

    public void onDestroyView() {
        if (this.mOldActivityTitle != null) {
            getActivity().getActionBar().setTitle(this.mOldActivityTitle);
        }
        this.mHandler.removeMessages(1000);
        super.onDestroyView();
        this.mToggleSwitch.setOnBeforeCheckedChangeListener(null);
    }

    private void onPreferenceToggled(String preferenceKey, boolean enabled) {
        ((PrintManager) getContext().getSystemService("print")).setPrintServiceEnabled(this.mComponentName, enabled);
    }

    private ListView getBackupListView() {
        if (getView() == null) {
            return null;
        }
        return (ListView) getView().findViewById(2131886921);
    }

    private void updateEmptyView() {
        if (!this.mToggleSwitch.isChecked() || this.mPrintersAdapter.getUnfilteredCount() <= 0 || this.mPrintersAdapter.getCount() <= 0) {
            ListView listView = getBackupListView();
            if (listView != null) {
                ViewGroup contentRoot = (ViewGroup) listView.getParent();
                View emptyView = listView.getEmptyView();
                if ((!this.mSearching && this.mToggleSwitch.isChecked() && this.mPrintersAdapter.getUnfilteredCount() <= 0) || !WifiExtUtils.isWifiConnected(getActivity())) {
                    if (!(emptyView == null || emptyView.getId() == 2131886555)) {
                        contentRoot.removeView(emptyView);
                        emptyView = null;
                    }
                    if (emptyView == null) {
                        emptyView = getActivity().getLayoutInflater().inflate(2130968769, contentRoot, false);
                        contentRoot.addView(emptyView);
                        listView.setEmptyView(emptyView);
                    }
                    this.mProgressView.setVisibility(8);
                } else if (!this.mToggleSwitch.isChecked()) {
                    if (emptyView != null) {
                        contentRoot.removeView(emptyView);
                    }
                    emptyView = getActivity().getLayoutInflater().inflate(2130968770, contentRoot, false);
                    icon = (ImageView) emptyView.findViewById(2131886560);
                    icon.setImageResource(2130838391);
                    textView = (TextView) emptyView.findViewById(2131886561);
                    icon.setContentDescription(getString(2131625936));
                    textView.setText(2131625936);
                    contentRoot.addView(emptyView);
                    listView.setEmptyView(emptyView);
                    this.mProgressView.setVisibility(8);
                } else if (this.mPrintersAdapter.getUnfilteredCount() <= 0) {
                    if (emptyView != null) {
                        contentRoot.removeView(emptyView);
                    }
                    emptyView = getActivity().getLayoutInflater().inflate(2130968771, contentRoot, false);
                    contentRoot.addView(emptyView);
                    listView.setEmptyView(emptyView);
                    this.mProgressView.setVisibility(0);
                } else if (this.mPrintersAdapter.getCount() <= 0) {
                    if (!(emptyView == null || emptyView.getId() == 2131886559)) {
                        contentRoot.removeView(emptyView);
                        emptyView = null;
                    }
                    if (emptyView == null) {
                        emptyView = getActivity().getLayoutInflater().inflate(2130968770, contentRoot, false);
                        icon = (ImageView) emptyView.findViewById(2131886560);
                        icon.setImageResource(2130838391);
                        textView = (TextView) emptyView.findViewById(2131886561);
                        icon.setContentDescription(getString(2131625927));
                        textView.setText(2131625927);
                        contentRoot.addView(emptyView);
                        listView.setEmptyView(emptyView);
                    }
                } else {
                    this.mSearching = false;
                    getActivity().invalidateOptionsMenu();
                }
            }
        }
    }

    private void updateUiForServiceState() {
        if (this.mServiceEnabled) {
            this.mToggleSwitch.setCheckedInternal(true);
            if (WifiExtUtils.isWifiConnected(getActivity())) {
                this.mPrintersAdapter.enable();
            } else {
                this.mSearching = false;
            }
        } else {
            this.mToggleSwitch.setCheckedInternal(false);
            this.mPrintersAdapter.disable();
        }
        getActivity().invalidateOptionsMenu();
    }

    private void initComponents() {
        this.mPrintersAdapter = new PrintersAdapter();
        this.mPrintersAdapter.registerDataSetObserver(this.mDataObserver);
        this.mToggleSwitch.setOnBeforeCheckedChangeListener(new OnBeforeCheckedChangeListener() {
            public boolean onBeforeCheckedChanged(ToggleSwitch toggleSwitch, boolean checked) {
                if (checked) {
                    System.putIntForUser(PrintServiceSettingsFragment.this.getContentResolver(), "MopriaSwitch", 1, UserHandle.myUserId());
                    PrintServiceSettingsFragment.this.onPreferenceToggled(PrintServiceSettingsFragment.this.mPreferenceKey, true);
                } else {
                    ItemUseStat.getInstance().handleClick(PrintServiceSettingsFragment.this.getActivity(), 3, "mopria_print_service", "off");
                    PrintServiceSettingsFragment.this.onPreferenceToggled(PrintServiceSettingsFragment.this.mPreferenceKey, false);
                }
                return false;
            }
        });
        this.mToggleSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && WifiExtUtils.isWifiConnected(PrintServiceSettingsFragment.this.getActivity())) {
                    PrintServiceSettingsFragment.this.mSearching = true;
                } else {
                    synchronized (PrintServiceSettingsFragment.this.mPrintersAdapter.mLock) {
                        PrintServiceSettingsFragment.this.mPrintersAdapter.mFilteredPrinters.clear();
                        PrintServiceSettingsFragment.this.mPrintersAdapter.mPrinters.clear();
                    }
                    PrintServiceSettingsFragment.this.mPrintersAdapter.getFilter().filter(null);
                }
                PrintServiceSettingsFragment.this.updateEmptyView();
            }
        });
        View divider = getActivity().getLayoutInflater().inflate(2130968755, null, false);
        if (getBackupListView() != null) {
            getBackupListView().addFooterView(divider);
            getBackupListView().setSelector(new ColorDrawable(0));
            getBackupListView().setAdapter(this.mPrintersAdapter);
            getBackupListView().setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    PrinterInfo printer = (PrinterInfo) PrintServiceSettingsFragment.this.mPrintersAdapter.getItem(position);
                    if (printer.getInfoIntent() != null) {
                        try {
                            PrintServiceSettingsFragment.this.getActivity().startIntentSender(printer.getInfoIntent().getIntentSender(), null, 0, 0, 0);
                        } catch (SendIntentException e) {
                            Log.e("PrintServiceSettingsFragment", "Could not execute info intent: %s", e);
                        }
                    }
                }
            });
        }
    }

    private void updateUiForArguments() {
        Bundle arguments = getArguments();
        this.mComponentName = ComponentName.unflattenFromString(arguments.getString("EXTRA_SERVICE_COMPONENT_NAME"));
        this.mPreferenceKey = this.mComponentName.flattenToString();
        boolean enabled = arguments.getBoolean("EXTRA_CHECKED");
        if (getServiceInitState()) {
            ((PrintManager) getContext().getSystemService("print")).setPrintServiceEnabled(this.mComponentName, enabled);
        }
        this.mToggleSwitch.setCheckedInternal(enabled);
        getLoaderManager().initLoader(2, null, this);
        setHasOptionsMenu(true);
    }

    private boolean getServiceInitState() {
        if (System.getIntForUser(getContentResolver(), "MopriaSwitch", 0, UserHandle.myUserId()) == 0) {
            return true;
        }
        return false;
    }

    public Loader<List<PrintServiceInfo>> onCreateLoader(int id, Bundle args) {
        return new PrintServicesLoader((PrintManager) getContext().getSystemService("print"), getContext(), 3);
    }

    public void onLoadFinished(Loader<List<PrintServiceInfo>> loader, List<PrintServiceInfo> services) {
        List<ResolveInfo> resolvedActivities;
        PrintServiceInfo printServiceInfo = null;
        if (services != null) {
            int numServices = services.size();
            for (int i = 0; i < numServices; i++) {
                if (((PrintServiceInfo) services.get(i)).getComponentName().equals(this.mComponentName)) {
                    printServiceInfo = (PrintServiceInfo) services.get(i);
                    break;
                }
            }
        }
        if (printServiceInfo == null) {
            finishFragment();
        }
        this.mServiceEnabled = printServiceInfo.isEnabled();
        if (printServiceInfo.getSettingsActivityName() != null) {
            Intent settingsIntent = new Intent("android.intent.action.MAIN");
            settingsIntent.setComponent(new ComponentName(printServiceInfo.getComponentName().getPackageName(), printServiceInfo.getSettingsActivityName()));
            resolvedActivities = getPackageManager().queryIntentActivities(settingsIntent, 0);
            if (!resolvedActivities.isEmpty() && ((ResolveInfo) resolvedActivities.get(0)).activityInfo.exported) {
                this.mSettingsIntent = settingsIntent;
            }
        } else {
            this.mSettingsIntent = null;
        }
        if (printServiceInfo.getAddPrintersActivityName() != null) {
            Intent addPrintersIntent = new Intent("android.intent.action.MAIN");
            addPrintersIntent.setComponent(new ComponentName(printServiceInfo.getComponentName().getPackageName(), printServiceInfo.getAddPrintersActivityName()));
            resolvedActivities = getPackageManager().queryIntentActivities(addPrintersIntent, 0);
            if (!resolvedActivities.isEmpty() && ((ResolveInfo) resolvedActivities.get(0)).activityInfo.exported) {
                this.mAddPrintersIntent = addPrintersIntent;
            }
        } else {
            this.mAddPrintersIntent = null;
        }
        updateUiForServiceState();
    }

    public void onLoaderReset(Loader<List<PrintServiceInfo>> loader) {
        updateUiForServiceState();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(2132017158, menu);
        int textId = 2131627572;
        if (!this.mSearching) {
            textId = 2131628228;
        }
        menu.add(0, 2, 0, textId).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), this.mSearching ? Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_CLOSE) : Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_SEARCH))).setEnabled(true).setShowAsAction(2);
        MenuItem addPrinters = menu.findItem(2131887654);
        if (this.mServiceEnabled && this.mAddPrintersIntent != null && WifiExtUtils.isWifiConnected(getActivity())) {
            addPrinters.setIntent(this.mAddPrintersIntent);
        } else {
            menu.removeItem(2131887654);
            menu.removeItem(2);
        }
        MenuItem settings = menu.findItem(2131887655);
        if (this.mServiceEnabled && this.mSettingsIntent != null && WifiExtUtils.isWifiConnected(getActivity())) {
            settings.setIntent(this.mSettingsIntent);
        } else {
            menu.removeItem(2131887655);
        }
        MenuItem searchItem = menu.findItem(2131887653);
        if (this.mSearching || !this.mServiceEnabled || this.mPrintersAdapter.getUnfilteredCount() <= 0) {
            menu.removeItem(2131887653);
        } else {
            this.mSearchView = (SearchView) searchItem.getActionView();
            if (this.mSearchView == null) {
                Log.e("==sjd==", "printServiceFragment mSearchView == null");
            } else {
                this.mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
                    public boolean onQueryTextSubmit(String query) {
                        return true;
                    }

                    public boolean onQueryTextChange(String searchString) {
                        PrintServiceSettingsFragment.this.mPrintersAdapter.getFilter().filter(searchString);
                        return true;
                    }
                });
                this.mSearchView.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
                    public void onViewAttachedToWindow(View view) {
                        if (AccessibilityManager.getInstance(PrintServiceSettingsFragment.this.getActivity()).isEnabled()) {
                            view.announceForAccessibility(PrintServiceSettingsFragment.this.getString(2131625946));
                        }
                    }

                    public void onViewDetachedFromWindow(View view) {
                        Activity activity = PrintServiceSettingsFragment.this.getActivity();
                        if (activity != null && !activity.isFinishing() && AccessibilityManager.getInstance(activity).isEnabled()) {
                            view.announceForAccessibility(PrintServiceSettingsFragment.this.getString(2131625947));
                        }
                    }
                });
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(2130969028, container, false);
        LinearLayout switchContainer = (LinearLayout) view.findViewById(2131887008);
        this.mToggleSwitch = new ToggleSwitch(getActivity());
        this.mToggleSwitch.setId(2131886106);
        View switchPreference = inflatePreference(inflater, switchContainer, this.mToggleSwitch);
        ((TextView) switchPreference.findViewById(16908310)).setText(getArguments().getString("EXTRA_TITLE"));
        switchContainer.addView(switchPreference);
        LinearLayout searchContainer = (LinearLayout) view.findViewById(2131887009);
        View searchView = inflater.inflate(2130968967, searchContainer, false);
        ((TextView) searchView.findViewById(16908310)).setText(getActivity().getResources().getString(2131627832));
        this.mProgressView = searchView.findViewById(2131886936);
        searchContainer.addView(searchView);
        TextView printWarningView = (TextView) view.findViewById(2131887006);
        if (!(printWarningView == null || SettingsExtUtils.isGlobalVersion())) {
            printWarningView.setVisibility(0);
            this.mToggleSwitch.setChecked(false);
        }
        return view;
    }

    private static View inflatePreference(LayoutInflater inflater, ViewGroup root, View widget) {
        View view = inflater.inflate(2130969029, root, false);
        ((LinearLayout) view.findViewById(16908312)).addView(widget, new LayoutParams(-2, -2));
        return view;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = item.getIntent();
        Log.e("==sjd==", "onOptionsItemSelected item = " + item.getItemId());
        switch (item.getItemId()) {
            case 2:
                toggleSearchButton();
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "scan_printer");
                return true;
            default:
                if (this.mAddPrintersIntent == intent) {
                    ItemUseStat.getInstance().handleClick(getActivity(), 2, "add_printers");
                } else if (this.mSettingsIntent == intent) {
                    ItemUseStat.getInstance().handleClick(getActivity(), 2, "printer_settings");
                }
                return super.onOptionsItemSelected(item);
        }
    }

    private void stopSearch() {
        this.mHandler.removeMessages(1000);
        this.mSearching = false;
        this.mPrintersAdapter.disable();
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
        this.mProgressView.setVisibility(8);
        updateEmptyView();
    }

    private void toggleSearchButton() {
        if (this.mSearching) {
            stopSearch();
        } else {
            startSearch();
        }
    }

    private void startSearch() {
        this.mPrintersAdapter.getFilter().filter(null);
        synchronized (this.mPrintersAdapter.mLock) {
            this.mPrintersAdapter.mPrinters.clear();
            this.mPrintersAdapter.mFilteredPrinters.clear();
        }
        this.mPrintersAdapter.enable();
        this.mSearching = true;
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
        this.mProgressView.setVisibility(0);
        updateEmptyView();
    }
}
