package com.android.settings.localepicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.internal.app.LocalePickerWithRegion;
import com.android.internal.app.LocalePickerWithRegion.LocaleSelectedListener;
import com.android.internal.app.LocaleStore;
import com.android.internal.app.LocaleStore.LocaleInfo;
import com.android.settings.HwCustSplitUtils;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.huawei.cust.HwCustUtils;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;

public class LocaleListEditor extends SettingsPreferenceFragment implements LocaleSelectedListener {
    private List<LocaleInfo> feedsList;
    private LocaleDragAndDropAdapter mAdapter;
    private View mAddLanguage;
    private HwCustSplitUtils mHwCustSplitUtils;
    private TextView mLocaleExampleText;
    private Menu mMenu;
    private boolean mRemoveMode;
    private boolean mShowingRemoveDialog;

    protected int getMetricsCategory() {
        return 344;
    }

    public void onCreate(Bundle savedInstanceState) {
        this.mHwCustSplitUtils = (HwCustSplitUtils) HwCustUtils.createObj(HwCustSplitUtils.class, new Object[]{getActivity()});
        this.mHwCustSplitUtils.setControllerShowing(true);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        LocaleStore.fillCache(getContext());
        this.feedsList = new LocaleListHelper().getFeedsList();
        this.mAdapter = new LocaleDragAndDropAdapter(getContext(), this.feedsList);
    }

    public void onDestroy() {
        super.onDestroy();
        this.mHwCustSplitUtils.setControllerShowing(false);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstState) {
        View result = super.onCreateView(inflater, container, savedInstState);
        View myLayout = inflater.inflate(2130968852, (ViewGroup) result);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            getActivity().getActionBar().setTitle(2131624553);
        }
        configureDragAndDrop(myLayout);
        setExampleStr();
        if (SystemProperties.getBoolean("ro.talkback.chn_enable", true)) {
            LocaleListHelper.updateEngine(getPrefContext());
        }
        return result;
    }

    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            this.mRemoveMode = savedInstanceState.getBoolean("localeRemoveMode", false);
            this.mShowingRemoveDialog = savedInstanceState.getBoolean("showingLocaleRemoveDialog", false);
        }
        setRemoveMode(this.mRemoveMode);
        this.mAdapter.restoreState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("localeRemoveMode", this.mRemoveMode);
        outState.putBoolean("showingLocaleRemoveDialog", this.mShowingRemoveDialog);
        this.mAdapter.saveState(outState);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case 2:
                startActivity(new Intent(getContext(), LocaleDeleteActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void setExampleStr() {
        Activity activity = getActivity();
        String exampleStr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(1), 3, 7, 1, 3);
        NumberFormat nf = NumberFormat.getInstance();
        NumberFormat pnf = NumberFormat.getPercentInstance();
        pnf.setMinimumFractionDigits(1);
        String mLanguageName = ((LocaleInfo) this.feedsList.get(0)).getFullNameInUiLanguage();
        String dateStr = DateUtils.formatDateTime(activity, calendar.getTimeInMillis(), 22);
        String timeStr = DateFormat.getTimeFormat(activity).format(calendar.getTime());
        String numberStr = nf.format(12345.67d);
        this.mLocaleExampleText.setText(mLanguageName + "\n" + dateStr + "\n" + timeStr + "\n" + numberStr + "\n" + pnf.format(0.789d));
    }

    private void setRemoveMode(boolean mRemoveMode) {
        this.mRemoveMode = mRemoveMode;
        this.mAdapter.setRemoveMode(mRemoveMode);
        this.mAddLanguage.setVisibility(mRemoveMode ? 4 : 0);
        updateVisibilityOfRemoveMenu();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem menuItem = menu.add(0, 2, 0, 2131628641);
        menuItem.setShowAsAction(0);
        menuItem.setIcon(2130838230);
        super.onCreateOptionsMenu(menu, inflater);
        this.mMenu = menu;
        updateVisibilityOfRemoveMenu();
    }

    private void configureDragAndDrop(View view) {
        RecyclerView list = (RecyclerView) view.findViewById(2131886763);
        LocaleLinearLayoutManager llm = new LocaleLinearLayoutManager(getContext(), this.mAdapter);
        llm.setAutoMeasureEnabled(true);
        list.setLayoutManager(llm);
        list.setHasFixedSize(true);
        this.mAdapter.setRecyclerView(list);
        list.setAdapter(this.mAdapter);
        this.mAddLanguage = view.findViewById(2131886764);
        this.mAddLanguage.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (Utils.hasPackageInfo(LocaleListEditor.this.getActivity().getPackageManager(), "com.huawei.languagedownloader")) {
                    Intent mIntent = new Intent();
                    mIntent.setAction("huawei.intent.action.LANGUAGEDOWNLOADER");
                    try {
                        LocaleListEditor.this.startActivityForResult(mIntent, 1);
                        return;
                    } catch (Exception e) {
                        Log.e("LocaleListEditor", "configureDragAndDrop()-->onClick--> e = " + e);
                        return;
                    }
                }
                try {
                    LocaleListEditor.this.getFragmentManager().beginTransaction().setTransition(4097).replace(LocaleListEditor.this.getId(), LocalePickerWithRegion.createLanguagePicker(LocaleListEditor.this.getContext(), LocaleListEditor.this, false)).addToBackStack("localeListEditor").commit();
                } catch (Exception e2) {
                    Log.e("LocaleListEditor", "configureDragAndDrop()-->onClick--> e = " + e2);
                }
            }
        });
        this.mLocaleExampleText = (TextView) view.findViewById(2131886765);
    }

    public void onLocaleSelected(LocaleInfo locale) {
        ItemUseStat.getInstance().handleClick(getActivity(), 2, "add language", locale.getLocale().toString());
        this.mAdapter.addLocale(locale);
        updateVisibilityOfRemoveMenu();
    }

    private void updateVisibilityOfRemoveMenu() {
        int i = 2;
        boolean z = false;
        if (this.mMenu != null) {
            MenuItem menuItemRemove = this.mMenu.findItem(2);
            if (menuItemRemove != null) {
                if (!this.mRemoveMode) {
                    i = 0;
                }
                menuItemRemove.setShowAsAction(i);
                if (this.mAdapter.getItemCount() > 1) {
                    z = true;
                }
                menuItemRemove.setVisible(z);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 1:
                String addId = data.getStringExtra("localeId");
                this.mAdapter.addLocale(LocaleUtil.getLocaleInfo(addId));
                setAddLanguageTranslated(addId);
                return;
            default:
                return;
        }
    }

    private void setAddLanguageTranslated(String addedId) {
        if (addedId != null) {
            for (LocaleInfo localeinfo : new LocaleListHelper().getFeedsList()) {
                if (addedId.equals(localeinfo.getId())) {
                    localeinfo.setTranslated(true);
                    break;
                }
            }
        }
    }
}
