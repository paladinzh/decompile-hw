package com.huawei.systemmanager.shortcut;

import android.app.ActivityManagerNative;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.ConfigurationEx;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class ShortCutFragment extends Fragment {
    private static final String TAG = "ShortCutFragment";
    private static String alreadyInLauncherDescription = "";
    private static String notInLauncherDescription = "";
    private static String suggestInLauncherDescription = "";
    private Context mContext;
    private View mFragment;
    private ShortCutHelper mHelper = null;
    private LayoutInflater mLayoutInflater;
    private View mOneKeyCleanWidgetView = null;
    private ShortCutFragmentAdapter mShortCutFragmentAdapter = null;
    private HeaderGridView mShortcutGridView = null;

    class LoadDataTask extends AsyncTask<Void, Void, List<ShortCutInfoItem>> {
        LoadDataTask() {
        }

        protected List<ShortCutInfoItem> doInBackground(Void... params) {
            try {
                return ShortCutFragment.this.mHelper.getShortCutItemList();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(List<ShortCutInfoItem> result) {
            super.onPostExecute(result);
            if (result != null) {
                ShortCutFragment.this.updateUI(result);
            }
        }
    }

    private class SwitchItemClickListener implements OnItemClickListener {
        private SwitchItemClickListener() {
        }

        public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
            HwLog.d(ShortCutFragment.TAG, "onListItemClick position:" + position + " id:" + id);
            int itemPosition = position - ShortCutFragment.this.mShortcutGridView.getNumColumns();
            HwLog.d(ShortCutFragment.TAG, "itemPosition:" + itemPosition);
            if (itemPosition >= 0) {
                ShortCutInfoItem item = (ShortCutInfoItem) ShortCutFragment.this.mShortCutFragmentAdapter.getData().get(itemPosition);
                ShortCutViewHolder viewHolder = new ShortCutViewHolder(v);
                boolean newChecked = !item.mIsInLauncher;
                if (!ShortCutHelper.isShieldApps(item.mShortCutNameResId) || Utility.isOwnerUser()) {
                    item.mIsInLauncher = newChecked;
                    viewHolder.mIsInLauncher.setChecked(newChecked);
                    int shortCutNameResId = item.mShortCutNameResId;
                    int shortCutIconResId = item.mShortCutDeskIconResId;
                    Intent destinationIntent = item.mDestinationIntent;
                    int itemTitleResId = item.mShortCutNameResId;
                    if (newChecked) {
                        viewHolder.mShortCutStatusDescription.setText(ShortCutFragment.alreadyInLauncherDescription);
                        item.mShortCutStatusDescriptionResId = R.string.shortcut_already_in_launcher;
                        ShortCutFragment.this.mHelper.createShortCut(ShortCutFragment.this.mContext, shortCutNameResId, shortCutIconResId, destinationIntent);
                    } else {
                        if (ShortCutFragment.this.mHelper.isSuggestToLauncher(itemTitleResId)) {
                            viewHolder.mShortCutStatusDescription.setText(ShortCutFragment.suggestInLauncherDescription);
                            item.mShortCutStatusDescriptionResId = R.string.shortcut_suggest_send_to_launcher;
                        } else {
                            viewHolder.mShortCutStatusDescription.setText(ShortCutFragment.notInLauncherDescription);
                            item.mShortCutStatusDescriptionResId = R.string.shortcut_not_in_launcher;
                        }
                        ShortCutFragment.this.mHelper.delShortcut(ShortCutFragment.this.mContext, shortCutNameResId, destinationIntent);
                    }
                    return;
                }
                viewHolder.mIsInLauncher.setChecked(false);
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        boolean z;
        this.mContext = getActivity();
        this.mLayoutInflater = inflater;
        this.mFragment = this.mLayoutInflater.inflate(R.layout.short_cut_fragment, container, false);
        this.mShortCutFragmentAdapter = new ShortCutFragmentAdapter(this.mContext);
        this.mOneKeyCleanWidgetView = LayoutInflater.from(this.mContext).inflate(R.layout.common_list_item_twolines_image, null);
        ((ImageView) this.mOneKeyCleanWidgetView.findViewById(R.id.image)).setImageResource(R.drawable.ic_widget_preview);
        TextView title = (TextView) this.mOneKeyCleanWidgetView.findViewById(ViewUtil.HWID_TEXT_1);
        TextView description = (TextView) this.mOneKeyCleanWidgetView.findViewById(ViewUtil.HWID_TEXT_2);
        ShortCutHelper.setTextViewMultiLines(description);
        title.setText(R.string.widget_title);
        description.setText(R.string.shortcut_onekey_widget_not_in_launcher);
        this.mHelper = new ShortCutHelper(this.mContext);
        intiString(this.mContext);
        this.mShortcutGridView = (HeaderGridView) this.mFragment.findViewById(R.id.short_cut_header_gridview);
        Context context = GlobalContext.getContext();
        if (GlobalContext.getContext().getResources().getConfiguration().orientation == 2) {
            z = true;
        } else {
            z = false;
        }
        HSMConst.doMultiply(context, z, this.mShortcutGridView);
        ViewGroup viewParent = (ViewGroup) this.mOneKeyCleanWidgetView.getParent();
        if (viewParent != null) {
            viewParent.removeView(this.mOneKeyCleanWidgetView);
        }
        this.mShortcutGridView.addHeaderView(this.mOneKeyCleanWidgetView);
        this.mShortcutGridView.setAdapter(this.mShortCutFragmentAdapter);
        this.mShortcutGridView.setOnItemClickListener(new SwitchItemClickListener());
        initHeaderView();
        return this.mFragment;
    }

    private static void intiString(Context context) {
        if (context != null) {
            alreadyInLauncherDescription = context.getString(R.string.shortcut_already_in_launcher);
            notInLauncherDescription = context.getString(R.string.shortcut_not_in_launcher);
            suggestInLauncherDescription = context.getString(R.string.shortcut_suggest_send_to_launcher);
        }
    }

    private void initHeaderView() {
        this.mOneKeyCleanWidgetView.setVisibility(this.mHelper.isOneKeyCleanWidgetInLauncher(this.mContext) ? 8 : 0);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        HSMConst.doMultiply(GlobalContext.getContext(), newConfig.orientation == 2, this.mShortcutGridView);
    }

    public void onResume() {
        super.onResume();
        if (4 == getLauncherType()) {
            this.mHelper.mDatabaseUri = ShortCutHelper.SHORTCUT_LAUNCHER_DB_DRAWER_PREFIX;
        } else {
            this.mHelper.mDatabaseUri = ShortCutHelper.SHORTCUT_LAUNCHER_DB_PREFIX;
        }
        refreshData();
    }

    public static int getLauncherType() {
        Configuration curConfig = new Configuration();
        try {
            curConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
            ConfigurationEx mExtraConfig = new com.huawei.android.content.res.ConfigurationEx(curConfig).getExtraConfig();
            if (mExtraConfig != null) {
                return mExtraConfig.simpleuiMode;
            }
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        } catch (NoSuchFieldError err) {
            err.printStackTrace();
            return 1;
        } catch (NoClassDefFoundError e2) {
            e2.printStackTrace();
            return 1;
        }
    }

    private void refreshData() {
        new LoadDataTask().execute(new Void[0]);
    }

    private void updateUI(List<ShortCutInfoItem> dataList) {
        this.mShortCutFragmentAdapter.swapData(dataList);
        HwLog.d(TAG, "isOneKeyCleanWidgetExist:" + this.mHelper.isOneKeyCleanWidgetInLauncher(this.mContext));
        initHeaderView();
    }
}
