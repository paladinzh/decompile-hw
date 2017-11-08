package com.android.settings.dashboard;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.provider.Settings.System;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.ArrayUtils;
import com.android.settings.AirplaneModeEnabler;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;
import com.android.settings.dashboard.conditional.Condition;
import com.android.settings.dashboard.conditional.ConditionAdapterUtils;
import com.android.settings.navigation.NaviUtils;
import com.android.settingslib.SuggestionParser;
import com.android.settingslib.drawer.DashboardCategory;
import com.android.settingslib.drawer.SplitUtils;
import com.android.settingslib.drawer.Tile;
import com.android.settingslib.drawer.TileUtils;
import java.util.ArrayList;
import java.util.List;

public class DashboardAdapter extends Adapter<DashboardItemHolder> implements OnClickListener {
    private static int SUGGESTION_MODE_COLLAPSED = 1;
    private static int SUGGESTION_MODE_DEFAULT = 0;
    private static int SUGGESTION_MODE_EXPANDED = 2;
    private final Activity mActivity;
    public AirplaneModeEnabler mAirplaneModeEnabler;
    private final IconCache mCache;
    private List<DashboardCategory> mCategories;
    private List<Condition> mConditions;
    private final Context mContext;
    public DataEnabler mDataEnabler;
    private Condition mExpandedCondition = null;
    private int mId;
    private final List<Integer> mIds = new ArrayList();
    private boolean mIsShowingAll;
    private final List<Object> mItems = new ArrayList();
    public LteEnabler mLteEnabler;
    private SearchViewClickListener mSearchViewClickListener;
    private SplitSelector mSplitSelector;
    private int mSuggestionMode = SUGGESTION_MODE_DEFAULT;
    private SuggestionParser mSuggestionParser;
    private List<Tile> mSuggestions;
    private final List<Integer> mTypes = new ArrayList();
    public UpdateEnabler mUpdateEnabler;

    public static class DashboardItemHolder extends ViewHolder {
        public final ImageView arrow;
        public final ImageView icon;
        public final TextView summary;
        public final TextView title;

        public DashboardItemHolder(View itemView) {
            super(itemView);
            this.icon = (ImageView) itemView.findViewById(16908294);
            this.title = (TextView) itemView.findViewById(16908310);
            this.summary = (TextView) itemView.findViewById(16908304);
            this.arrow = (ImageView) itemView.findViewById(2131886968);
        }
    }

    public class DashboardSearchItemHolder extends DashboardItemHolder {
        public final SearchView mSearchView;

        public DashboardSearchItemHolder(View itemView) {
            super(itemView);
            this.mSearchView = (SearchView) itemView.findViewById(2131886440);
        }
    }

    public static class DashboardSwitchItemHolder extends DashboardItemHolder {
        public final View mDividerView;
        public final Switch mSwitch;

        public DashboardSwitchItemHolder(View itemView) {
            super(itemView);
            this.mSwitch = (Switch) itemView.findViewById(2131886254);
            this.mDividerView = itemView.findViewById(2131886442);
        }

        public void initSwitchTileState() {
            if (this.mSwitch != null) {
                this.mSwitch.setEnabled(true);
            }
            setDividerVisible(false);
        }

        public void setDividerVisible(boolean visible) {
            if (this.mDividerView != null) {
                this.mDividerView.setVisibility(visible ? 0 : 8);
            }
        }
    }

    public class DashboardUpdateItemHolder extends DashboardItemHolder {
        public final FrameLayout mCounterFrame;

        public DashboardUpdateItemHolder(View itemView) {
            super(itemView);
            this.mCounterFrame = (FrameLayout) itemView.findViewById(2131886443);
        }
    }

    private class HiCloudSummaryUpdater extends AsyncTask<Void, Void, Void> {
        private HiCloudSummaryUpdater() {
        }

        protected Void doInBackground(Void... params) {
            DashboardAdapter.this.setHiCloudSummary();
            return null;
        }

        protected void onPostExecute(Void result) {
            DashboardAdapter.this.notifyDataSetChanged();
        }
    }

    private class HwTrustSpaceSummaryUpdater extends AsyncTask<Void, Void, Void> {
        private HwTrustSpaceSummaryUpdater() {
        }

        protected Void doInBackground(Void... params) {
            DashboardAdapter.this.setHwTrustSpaceSummary();
            return null;
        }

        protected void onPostExecute(Void result) {
            DashboardAdapter.this.notifyDataSetChanged();
        }
    }

    private static class IconCache {
        private final Context mContext;
        private final ArrayMap<Icon, Drawable> mMap = new ArrayMap();

        public IconCache(Context context) {
            this.mContext = context;
        }

        public Drawable getIcon(Icon icon) {
            Drawable drawable = (Drawable) this.mMap.get(icon);
            if (drawable != null) {
                return drawable;
            }
            drawable = icon.loadDrawable(this.mContext);
            this.mMap.put(icon, drawable);
            return drawable;
        }
    }

    public interface SearchViewClickListener {
        void onSearchViewClicked();
    }

    public DashboardAdapter(Activity activity, Context context, SuggestionParser parser, SearchViewClickListener clickListener) {
        this.mActivity = activity;
        this.mContext = context;
        this.mCache = new IconCache(context);
        this.mSuggestionParser = parser;
        setHasStableIds(true);
        setShowingAll(true);
        this.mSplitSelector = new SplitSelector(this, context);
        this.mSearchViewClickListener = clickListener;
    }

    public List<Tile> getSuggestions() {
        return this.mSuggestions;
    }

    public void setSuggestions(List<Tile> suggestions) {
        this.mSuggestions = suggestions;
        recountItems();
    }

    public Tile getTile(ComponentName component) {
        if (this.mCategories == null || component == null) {
            return null;
        }
        for (int i = 0; i < this.mCategories.size(); i++) {
            for (int j = 0; j < ((DashboardCategory) this.mCategories.get(i)).tiles.size(); j++) {
                Tile tile = (Tile) ((DashboardCategory) this.mCategories.get(i)).tiles.get(j);
                if (component.equals(tile.intent.getComponent())) {
                    return tile;
                }
            }
        }
        return null;
    }

    public void setCategories(List<DashboardCategory> categories) {
        this.mCategories = categories;
        new HiCloudSummaryUpdater().execute(new Void[0]);
        new HwTrustSpaceSummaryUpdater().execute(new Void[0]);
        recountItems();
    }

    public void setConditions(List<Condition> conditions) {
        this.mConditions = conditions;
        recountItems();
    }

    public void notifyChanged(Tile tile) {
        notifyDataSetChanged();
    }

    public void setShowingAll(boolean showingAll) {
        this.mIsShowingAll = showingAll;
        recountItems();
    }

    private void recountItems() {
        reset();
        countItem(null, 2130968713, true, 0);
        boolean hasConditions = false;
        boolean hasTopSpacer = false;
        int i = 0;
        while (this.mConditions != null && i < this.mConditions.size()) {
            boolean shouldShow = ((Condition) this.mConditions.get(i)).shouldShow();
            hasConditions |= shouldShow;
            if (hasConditions && !hasTopSpacer) {
                countItem(null, 2130968714, hasConditions, 1);
                hasTopSpacer = true;
            }
            countItem(this.mConditions.get(i), 2130968680, shouldShow, 3000);
            i++;
        }
        boolean hasSuggestions = (this.mSuggestions == null || this.mSuggestions.size() == 0) ? false : true;
        countItem(null, 2130969161, hasSuggestions, 1);
        resetCount();
        if (this.mSuggestions != null) {
            int maxSuggestions;
            if (this.mSuggestionMode == SUGGESTION_MODE_DEFAULT) {
                maxSuggestions = Math.min(2, this.mSuggestions.size());
            } else if (this.mSuggestionMode == SUGGESTION_MODE_EXPANDED) {
                maxSuggestions = this.mSuggestions.size();
            } else {
                maxSuggestions = 0;
            }
            i = 0;
            while (i < this.mSuggestions.size()) {
                countItem(this.mSuggestions.get(i), 2130969162, i < maxSuggestions, 1000);
                i++;
            }
        }
        resetCount();
        i = 0;
        while (this.mCategories != null && i < this.mCategories.size()) {
            DashboardCategory category = (DashboardCategory) this.mCategories.get(i);
            if (!(i == 0 && hasSuggestions && this.mSuggestionMode == SUGGESTION_MODE_COLLAPSED)) {
                countItem(category, 2130968712, this.mIsShowingAll, 2000);
            }
            for (int j = 0; j < category.tiles.size(); j++) {
                Tile tile = (Tile) category.tiles.get(j);
                if (!needIgnoreThisTile(tile)) {
                    if (TileUtils.isSpecialTile(tile)) {
                        countSpecialItem(tile);
                    } else if (TileUtils.isSplitSwitchTile(tile) || TileUtils.isNormalSwitchTile(tile)) {
                        if (this.mIsShowingAll) {
                            r9 = true;
                        } else {
                            r9 = ArrayUtils.contains(DashboardSummary.INITIAL_ITEMS, tile.intent.getComponent().getClassName());
                        }
                        countItem(tile, 2130968715, r9, 2000);
                    } else if (TileUtils.isHuaweiUpdateTile(tile)) {
                        if (this.mIsShowingAll) {
                            r9 = true;
                        } else {
                            r9 = ArrayUtils.contains(DashboardSummary.INITIAL_ITEMS, tile.intent.getComponent().getClassName());
                        }
                        countItem(tile, 2130968717, r9, 2000);
                    } else {
                        if (this.mIsShowingAll) {
                            r9 = true;
                        } else {
                            r9 = ArrayUtils.contains(DashboardSummary.INITIAL_ITEMS, tile.intent.getComponent().getClassName());
                        }
                        countItem(tile, 2130968716, r9, 2000);
                    }
                }
            }
            i++;
        }
        notifyDataSetChanged();
    }

    private void resetCount() {
        this.mId = 0;
    }

    private void reset() {
        this.mItems.clear();
        this.mTypes.clear();
        this.mIds.clear();
        this.mId = 0;
    }

    private void countItem(Object object, int type, boolean add, int nameSpace) {
        if (add) {
            this.mItems.add(object);
            this.mTypes.add(Integer.valueOf(type));
            this.mIds.add(Integer.valueOf(this.mId + nameSpace));
        }
        this.mId++;
    }

    public DashboardItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 2130968715) {
            return new DashboardSwitchItemHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
        }
        if (viewType == 2130968713) {
            return new DashboardSearchItemHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
        }
        if (viewType == 2130968717) {
            return new DashboardUpdateItemHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
        }
        return new DashboardItemHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
    }

    public void onBindViewHolder(DashboardItemHolder holder, int position) {
        boolean z = true;
        switch (((Integer) this.mTypes.get(position)).intValue()) {
            case 2130968680:
                Condition condition = (Condition) this.mItems.get(position);
                if (this.mItems.get(position) != this.mExpandedCondition) {
                    z = false;
                }
                ConditionAdapterUtils.bindViews(condition, holder, z, this, new OnClickListener() {
                    public void onClick(View v) {
                        DashboardAdapter.this.onExpandClick(v);
                    }
                });
                return;
            case 2130968712:
                onBindCategory(holder, (DashboardCategory) this.mItems.get(position));
                holder.itemView.setTag(Integer.valueOf(1));
                return;
            case 2130968713:
                bindSearchTile(holder);
                return;
            case 2130968714:
                holder.itemView.setTag(Integer.valueOf(1));
                return;
            case 2130968715:
                Tile tile1 = (Tile) this.mItems.get(position);
                onBindSwitchTile(holder, tile1);
                holder.itemView.setTag(tile1);
                this.mSplitSelector.checkMarkedView(holder.itemView);
                bindSwitchWithEnabler(holder, tile1);
                setClickListenerForSwitchTile((DashboardSwitchItemHolder) holder, tile1);
                return;
            case 2130968716:
                Tile tile = (Tile) this.mItems.get(position);
                onBindTile(holder, tile);
                holder.itemView.setTag(tile);
                holder.itemView.setOnClickListener(this);
                this.mSplitSelector.checkMarkedView(holder.itemView);
                return;
            case 2130968717:
                Tile update = (Tile) this.mItems.get(position);
                onBindTile(holder, update);
                holder.itemView.setTag(update);
                holder.itemView.setOnClickListener(this);
                bindUpdateEnabler(holder, update);
                return;
            case 2130969092:
                onBindSeeAll(holder);
                return;
            case 2130969161:
                onBindSuggestionHeader(holder);
                holder.itemView.setTag(Integer.valueOf(1));
                return;
            case 2130969162:
                final Tile suggestion = (Tile) this.mItems.get(position);
                onBindTile(holder, suggestion);
                holder.itemView.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        MetricsLogger.action(DashboardAdapter.this.mContext, 386, DashboardAdapter.getSuggestionIdentifier(DashboardAdapter.this.mContext, suggestion));
                        ((SettingsActivity) DashboardAdapter.this.mContext).startSuggestion(suggestion.intent);
                    }
                });
                holder.itemView.findViewById(2131887232).setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        ItemUseStat.getInstance().handleClick(DashboardAdapter.this.mContext, 12, "dismiss suggestion", ItemUseStat.getShortName(suggestion.intent.getComponent().getClassName()));
                        MetricsLogger.action(DashboardAdapter.this.mContext, 387, DashboardAdapter.getSuggestionIdentifier(DashboardAdapter.this.mContext, suggestion));
                        DashboardAdapter.this.disableSuggestion(suggestion);
                        DashboardAdapter.this.mSuggestions.remove(suggestion);
                        DashboardAdapter.this.recountItems();
                    }
                });
                return;
            default:
                return;
        }
    }

    public void disableSuggestion(Tile suggestion) {
        if (this.mSuggestionParser != null && this.mSuggestionParser.dismissSuggestion(suggestion)) {
            this.mContext.getPackageManager().setComponentEnabledSetting(suggestion.intent.getComponent(), 2, 1);
            this.mSuggestionParser.markCategoryDone(suggestion.category);
        }
    }

    private void onBindSuggestionHeader(DashboardItemHolder holder) {
        int i;
        ImageView imageView = holder.icon;
        if (hasMoreSuggestions()) {
            i = 2130838239;
        } else {
            i = 2130838238;
        }
        imageView.setImageResource(i);
        holder.title.setText(this.mContext.getString(2131627129, new Object[]{Integer.valueOf(this.mSuggestions.size())}));
        holder.itemView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (DashboardAdapter.this.hasMoreSuggestions()) {
                    DashboardAdapter.this.mSuggestionMode = DashboardAdapter.SUGGESTION_MODE_EXPANDED;
                } else {
                    DashboardAdapter.this.mSuggestionMode = DashboardAdapter.SUGGESTION_MODE_COLLAPSED;
                }
                DashboardAdapter.this.recountItems();
            }
        });
    }

    private boolean hasMoreSuggestions() {
        if (this.mSuggestionMode == SUGGESTION_MODE_COLLAPSED) {
            return true;
        }
        if (this.mSuggestionMode == SUGGESTION_MODE_DEFAULT) {
            return this.mSuggestions.size() > 2;
        } else {
            return false;
        }
    }

    private void onBindTile(DashboardItemHolder holder, Tile tile) {
        if (holder != null && holder.icon != null && holder.title != null) {
            holder.icon.setImageDrawable(this.mCache.getIcon(tile.icon));
            holder.title.setText(tile.title);
            boolean inSplitMode = SplitUtils.reachSplitSize(this.mActivity);
            if (TextUtils.isEmpty(tile.summary)) {
                holder.summary.setVisibility(8);
            } else {
                int i;
                holder.summary.setText(tile.summary);
                TextView textView = holder.summary;
                if (inSplitMode) {
                    i = 8;
                } else {
                    i = 0;
                }
                textView.setVisibility(i);
            }
            if (inSplitMode) {
                if (holder.arrow != null) {
                    holder.arrow.setVisibility(8);
                }
            } else if (holder.arrow != null) {
                holder.arrow.setVisibility(0);
            }
        }
    }

    private void onBindCategory(DashboardItemHolder holder, DashboardCategory category) {
        if (TextUtils.isEmpty(category.title)) {
            holder.title.setVisibility(8);
        } else {
            holder.title.setText(category.title);
        }
    }

    private void onBindSeeAll(DashboardItemHolder holder) {
        int i;
        TextView textView = holder.title;
        if (this.mIsShowingAll) {
            i = 2131627090;
        } else {
            i = 2131627089;
        }
        textView.setText(i);
        holder.itemView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                DashboardAdapter.this.setShowingAll(!DashboardAdapter.this.mIsShowingAll);
            }
        });
    }

    public long getItemId(int position) {
        return (long) ((Integer) this.mIds.get(position)).intValue();
    }

    public int getItemViewType(int position) {
        return ((Integer) this.mTypes.get(position)).intValue();
    }

    public int getItemCount() {
        return this.mIds.size();
    }

    public void onClick(View v) {
        if (v.getId() == 2131886441) {
            Intent intent = ((Tile) v.getTag()).intent;
            ComponentName component = intent.getComponent();
            if (NaviUtils.isFrontFingerNaviEnabled() && component.getClassName().equals("com.android.settings.fingerprint.FingerprintMainSettingsActivity")) {
                intent.setComponent(new ComponentName(component.getPackageName(), "com.android.settings.fingerprint.FingerprintSettingsActivity"));
            }
            if (Utils.isWifiOnly(this.mContext) && "com.huawei.netassistant.ui.NetAssistantMainActivity".equals(component.getClassName())) {
                intent.setComponent(new ComponentName(component.getPackageName(), "com.huawei.systemmanager.netassistant.traffic.trafficranking.TrafficRankingListActivity"));
            }
            ItemUseStat.getInstance().handleClick(this.mContext, 1, intent.getComponent().getClassName());
            if (((SettingsActivity) this.mContext).openTile((Tile) v.getTag())) {
                this.mSplitSelector.markClick(v);
            }
            return;
        }
        if (v.getTag() == this.mExpandedCondition) {
            MetricsLogger.action(this.mContext, 375, this.mExpandedCondition.getMetricsConstant());
            this.mExpandedCondition.onPrimaryClick();
        } else {
            this.mExpandedCondition = (Condition) v.getTag();
            MetricsLogger.action(this.mContext, 373, this.mExpandedCondition.getMetricsConstant());
            notifyDataSetChanged();
        }
    }

    public void onExpandClick(View v) {
        if (v.getTag() == this.mExpandedCondition) {
            MetricsLogger.action(this.mContext, 374, this.mExpandedCondition.getMetricsConstant());
            this.mExpandedCondition = null;
        } else {
            this.mExpandedCondition = (Condition) v.getTag();
            MetricsLogger.action(this.mContext, 373, this.mExpandedCondition.getMetricsConstant());
        }
        notifyDataSetChanged();
    }

    public Object getItem(long itemId) {
        for (int i = 0; i < this.mIds.size(); i++) {
            if (((long) ((Integer) this.mIds.get(i)).intValue()) == itemId) {
                return this.mItems.get(i);
            }
        }
        return null;
    }

    public static String getSuggestionIdentifier(Context context, Tile suggestion) {
        String packageName = suggestion.intent.getComponent().getPackageName();
        if (packageName.equals(context.getPackageName())) {
            return suggestion.intent.getComponent().getClassName();
        }
        return packageName;
    }

    private boolean needIgnoreThisTile(Tile tile) {
        Intent intent = tile.intent;
        PackageManager pm = this.mContext.getPackageManager();
        if (intent == null) {
            return false;
        }
        ComponentName cname = intent.resolveActivity(pm);
        if (cname == null) {
            return false;
        }
        String className = cname.getClassName();
        if (className.equals("com.huawei.android.dsdscardmanager.HWCardManagerActivity")) {
            if (!Utils.isMultiSimEnabled() || Utils.isChinaTelecomArea() || Utils.isWifiOnly(this.mContext) || !Utils.isOwnerUser()) {
                return true;
            }
        } else if (!className.equals("com.huawei.android.dsdscardmanager.HWCardManagerTabActivity")) {
            return className.equals("com.huawei.trustspace.settings.SettingsActivity") && !Utils.isOwnerUser();
        } else {
            if (!(Utils.isMultiSimEnabled() && Utils.isChinaTelecomArea() && !Utils.isWifiOnly(this.mContext) && Utils.isOwnerUser())) {
                return true;
            }
        }
    }

    private void countSpecialItem(Tile tile) {
        if (TileUtils.isAirplaneModeTile(tile)) {
            updateSpecialTile(tile, this.mContext.getString(2131624580), 2130838343);
            countItem(tile, 2130968715, true, 2000);
        } else if (TileUtils.isTelecom4GModeTile(tile)) {
            updateSpecialTile(tile, this.mContext.getString(2131628020), 2130838337);
            countItem(tile, 2130968715, true, 2000);
        }
    }

    public void updateSpecialTile(Tile tile, String title, int iconResId) {
        if (tile != null) {
            tile.icon = Icon.createWithResource(this.mContext, iconResId);
            tile.title = title;
        }
    }

    private void bindSearchTile(DashboardItemHolder holder) {
        if (holder instanceof DashboardSearchItemHolder) {
            Utils.setSearchViewOnClickListener(((DashboardSearchItemHolder) holder).mSearchView, new OnClickListener() {
                public void onClick(View v) {
                    if (DashboardAdapter.this.mSearchViewClickListener != null) {
                        DashboardAdapter.this.mSearchViewClickListener.onSearchViewClicked();
                    }
                }
            });
        }
    }

    private void bindSwitchWithEnabler(DashboardItemHolder holder, Tile tile) {
        if (holder instanceof DashboardSwitchItemHolder) {
            if (TileUtils.isAirplaneModeTile(tile)) {
                if (this.mAirplaneModeEnabler != null) {
                    this.mAirplaneModeEnabler.setSwitch(((DashboardSwitchItemHolder) holder).mSwitch);
                } else {
                    this.mAirplaneModeEnabler = new AirplaneModeEnabler(this.mContext, ((DashboardSwitchItemHolder) holder).mSwitch);
                    this.mAirplaneModeEnabler.resume();
                }
            } else if (TileUtils.isTelecom4GModeTile(tile)) {
                if (this.mLteEnabler != null) {
                    this.mLteEnabler.setSwitch(((DashboardSwitchItemHolder) holder).mSwitch);
                } else {
                    this.mLteEnabler = new LteEnabler(this.mContext, ((DashboardSwitchItemHolder) holder).mSwitch);
                    this.mLteEnabler.resume();
                }
            } else if (TileUtils.isSplitSwitchTile(tile) && showDataEnable()) {
                if (this.mDataEnabler != null) {
                    this.mDataEnabler.setSwitch(((DashboardSwitchItemHolder) holder).mSwitch);
                } else {
                    this.mDataEnabler = new DataEnabler(this.mContext, ((DashboardSwitchItemHolder) holder).mSwitch);
                    this.mDataEnabler.resume();
                }
            }
        }
    }

    private boolean showDataEnable() {
        if (!Utils.isChinaTelecomArea() || Utils.isWifiOnly(this.mContext)) {
            return false;
        }
        return Utils.hasPackageInfo(this.mContext.getPackageManager(), "com.android.phone");
    }

    private void bindUpdateEnabler(DashboardItemHolder holder, Tile tile) {
        if ((holder instanceof DashboardUpdateItemHolder) && TileUtils.isHuaweiUpdateTile(tile)) {
            if (this.mUpdateEnabler != null) {
                this.mUpdateEnabler.setFrameLayout(((DashboardUpdateItemHolder) holder).mCounterFrame);
            } else {
                this.mUpdateEnabler = new UpdateEnabler(this.mContext, ((DashboardUpdateItemHolder) holder).mCounterFrame);
                this.mUpdateEnabler.resume();
            }
        }
    }

    private void setClickListenerForSwitchTile(DashboardSwitchItemHolder holder, final Tile tile) {
        if (TileUtils.isAirplaneModeTile(tile) || TileUtils.isTelecom4GModeTile(tile)) {
            bindItemViewWithSwitchClickListener(holder);
        } else {
            holder.itemView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (((SettingsActivity) DashboardAdapter.this.mContext).openTile(tile)) {
                        DashboardAdapter.this.mSplitSelector.markClick(v);
                    }
                }
            });
        }
    }

    private void bindItemViewWithSwitchClickListener(final DashboardSwitchItemHolder holder) {
        if (holder != null && holder.itemView != null) {
            holder.itemView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Switch switch_ = holder.mSwitch;
                    if (switch_ != null && switch_.isEnabled()) {
                        switch_.toggle();
                    }
                }
            });
        }
    }

    private void onBindSwitchTile(DashboardItemHolder holder, Tile tile) {
        holder.icon.setImageDrawable(this.mCache.getIcon(tile.icon));
        holder.title.setText(tile.title);
        if (holder instanceof DashboardSwitchItemHolder) {
            DashboardSwitchItemHolder switchItemHolder = (DashboardSwitchItemHolder) holder;
            switchItemHolder.initSwitchTileState();
            if (TileUtils.isSplitSwitchTile(tile)) {
                switchItemHolder.setDividerVisible(true);
            }
        }
    }

    public void setHiCloudSummary() {
        Tile tile = getTile(new ComponentName("com.huawei.hidisk", "com.huawei.android.hicloud.ui.activity.NewHiSyncSettingActivity"));
        if (tile != null) {
            tile.summary = TileUtils.getHwCloudStateInfo(this.mContext);
        }
    }

    public void setHwTrustSpaceSummary() {
        Tile tile = getTile(new ComponentName("com.huawei.trustspace", "com.huawei.trustspace.settings.SettingsActivity"));
        if (tile != null) {
            if (System.getInt(this.mContext.getContentResolver(), "trust_space_switch", 1) == 1) {
                tile.summary = this.mContext.getString(2131626851);
            } else {
                tile.summary = this.mContext.getString(2131626852);
            }
        }
    }

    public void onViewRecycled(DashboardItemHolder holder) {
        if (holder.itemView.getId() == 2131886441 && Utils.getViewBackgroundColor(holder.itemView) == SplitSelector.SELECTOR_COLOR) {
            holder.itemView.setBackgroundColor(0);
        }
    }

    public SplitSelector getmSplitSelector() {
        return this.mSplitSelector;
    }

    public void resumeAllSwitchEnabler() {
        if (!(this.mAirplaneModeEnabler == null || this.mAirplaneModeEnabler.isActive())) {
            this.mAirplaneModeEnabler.resume();
        }
        if (!(this.mDataEnabler == null || this.mDataEnabler.isActive())) {
            this.mDataEnabler.resume();
        }
        if (this.mLteEnabler != null && !this.mLteEnabler.isActive()) {
            this.mLteEnabler.resume();
        }
    }

    public void pauseAllSwitchEnabler() {
        if (this.mAirplaneModeEnabler != null && this.mAirplaneModeEnabler.isActive()) {
            this.mAirplaneModeEnabler.pause();
        }
        if (this.mDataEnabler != null && this.mDataEnabler.isActive()) {
            this.mDataEnabler.pause();
        }
        if (this.mLteEnabler != null && this.mLteEnabler.isActive()) {
            this.mLteEnabler.pause();
        }
    }

    public void resumeUpdateEnabler() {
        if (this.mUpdateEnabler != null && !this.mUpdateEnabler.isActive()) {
            this.mUpdateEnabler.resume();
        }
    }

    public void pauseUpdateEnabler() {
        if (this.mUpdateEnabler != null && this.mUpdateEnabler.isActive()) {
            this.mUpdateEnabler.pause();
        }
    }
}
