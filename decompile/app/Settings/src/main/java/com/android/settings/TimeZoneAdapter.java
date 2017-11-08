package com.android.settings;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.Filter;
import android.widget.Filter.FilterResults;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import huawei.android.widget.HwSortedTextListAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TimeZoneAdapter extends HwSortedTextListAdapter implements Filterable {
    private static final String TAG = TimeZoneAdapter.class.getCanonicalName();
    private Context mContext;
    private List<? extends Map<String, Object>> mData;
    private SimpleFilter mFilter;
    private String[] mFrom;
    private LayoutInflater mInflater;
    private Object mLock = new Object();
    private CharSequence mQueryConstraint;
    private int mResource;
    private int[] mTo;
    private ArrayList<Map<String, Object>> mUnfilteredData;
    private ViewBinder mViewBinder;

    private class SimpleFilter extends Filter {
        private SimpleFilter() {
        }

        protected FilterResults performFiltering(CharSequence constraint) {
            TimeZoneAdapter.this.mQueryConstraint = constraint;
            FilterResults results = new FilterResults();
            synchronized (TimeZoneAdapter.this.mLock) {
                if (TimeZoneAdapter.this.mUnfilteredData == null) {
                    TimeZoneAdapter.this.mUnfilteredData = new ArrayList(TimeZoneAdapter.this.mData);
                }
                Map<String, Object> item;
                if (TextUtils.isEmpty(constraint)) {
                    ArrayList<Map<String, Object>> list = TimeZoneAdapter.this.mUnfilteredData;
                    for (Map<String, Object> item2 : list) {
                        if (item2 != null) {
                            putHighLightData(item2, null, null, null);
                        }
                    }
                    results.values = list;
                    results.count = list.size();
                } else {
                    ArrayList<Map<String, Object>> unfilteredValues = TimeZoneAdapter.this.mUnfilteredData;
                    int count = unfilteredValues.size();
                    ArrayList<Map<String, Object>> newValues = new ArrayList(count);
                    for (int i = 0; i < count; i++) {
                        item2 = (Map) unfilteredValues.get(i);
                        if (item2 != null) {
                            Object titleData = item2.get("name");
                            String titleText = titleData == null ? "" : titleData.toString();
                            performTextFiltering(newValues, item2);
                            performPinYinFiltering(newValues, item2, titleText);
                            performEnglishFiltering(newValues, item2, titleText);
                            if (!newValues.contains(item2)) {
                                putHighLightData(item2, null, null, null);
                            }
                        }
                    }
                    results.values = newValues;
                    results.count = newValues.size();
                }
            }
            sortByHighLightIndex((ArrayList) results.values);
            return results;
        }

        protected void publishResults(CharSequence constraint, FilterResults results) {
            TimeZoneAdapter.this.mData = (List) results.values;
            if (results.count > 0) {
                TimeZoneAdapter.this.notifyDataSetChanged();
            } else {
                TimeZoneAdapter.this.notifyDataSetInvalidated();
            }
        }

        private void performTextFiltering(ArrayList<Map<String, Object>> filteredList, Map<String, Object> item) {
            if (!TextUtils.isEmpty(TimeZoneAdapter.this.mQueryConstraint) && !filteredList.contains(item)) {
                String constraintString = TimeZoneAdapter.this.mQueryConstraint.toString().toLowerCase(Locale.US);
                int len = TimeZoneAdapter.this.mTo.length;
                for (int j = 0; j < len; j++) {
                    String text = (String) item.get(TimeZoneAdapter.this.mFrom[j]);
                    if (text.toLowerCase(Locale.US).contains(constraintString)) {
                        int start = text.toLowerCase(Locale.US).indexOf(constraintString);
                        putHighLightData(item, TimeZoneAdapter.this.mFrom[j], Integer.valueOf(start), Integer.valueOf(start + constraintString.length()));
                        filteredList.add(item);
                        break;
                    }
                }
            }
        }

        private void performPinYinFiltering(ArrayList<Map<String, Object>> filteredList, Map<String, Object> item, String titleText) {
            if (Locale.CHINESE.getLanguage().equals(Locale.getDefault().getLanguage()) && !filteredList.contains(item)) {
                int[] indexArray = markHighLightIndex((String) item.get("city_full_pin_yin"), (String) item.get("city_initial_pin_yin"), 0);
                if (indexArray.length == 2) {
                    putHighLightData(item, TimeZoneAdapter.this.mFrom[0], Integer.valueOf(indexArray[0]), Integer.valueOf(indexArray[1]));
                    filteredList.add(item);
                }
                if (!filteredList.contains(item)) {
                    int offset = titleText.indexOf("(") + 1;
                    if (offset > 0) {
                        indexArray = markHighLightIndex((String) item.get("country_full_pin_yin"), (String) item.get("country_initial_pin_yin"), offset);
                        if (indexArray.length == 2) {
                            putHighLightData(item, TimeZoneAdapter.this.mFrom[0], Integer.valueOf(indexArray[0]), Integer.valueOf(indexArray[1]));
                            filteredList.add(item);
                        }
                    }
                }
            }
        }

        private void performEnglishFiltering(ArrayList<Map<String, Object>> filteredList, Map<String, Object> item, String titleText) {
            if (!TextUtils.isEmpty(TimeZoneAdapter.this.mQueryConstraint) && !filteredList.contains(item)) {
                String constraintString = TimeZoneAdapter.this.mQueryConstraint.toString().toLowerCase(Locale.US);
                String cityID = (String) item.get("city");
                if (cityID == null) {
                    cityID = "";
                }
                if (cityID.toLowerCase(Locale.US).contains(constraintString)) {
                    if (cityID.toLowerCase(Locale.US).equalsIgnoreCase(constraintString)) {
                        int end = titleText.toLowerCase(Locale.US).indexOf("(");
                        if (end == -1) {
                            end = titleText.length();
                        }
                        putHighLightData(item, TimeZoneAdapter.this.mFrom[0], Integer.valueOf(0), Integer.valueOf(end));
                    } else {
                        putHighLightData(item, null, null, null);
                    }
                    filteredList.add(item);
                }
            }
        }

        private int[] markHighLightIndex(String fullPinYinWithSeparator, String initialPinYin, int offset) {
            if ((TextUtils.isEmpty(fullPinYinWithSeparator) && TextUtils.isEmpty(initialPinYin)) || TextUtils.isEmpty(TimeZoneAdapter.this.mQueryConstraint) || offset < 0) {
                return new int[0];
            }
            String constraintString = TimeZoneAdapter.this.mQueryConstraint.toString().toLowerCase(Locale.US);
            if (fullPinYinWithSeparator == null) {
                fullPinYinWithSeparator = "";
            }
            if (initialPinYin == null) {
                initialPinYin = "";
            }
            if (initialPinYin.indexOf(constraintString) >= 0) {
                return new int[]{initialPinYin.indexOf(constraintString) + offset, (constraintString.length() + initialPinYin.indexOf(constraintString)) + offset};
            }
            if (fullPinYinWithSeparator.replace("-", "").toLowerCase(Locale.US).toLowerCase(Locale.US).contains(constraintString)) {
                String[] allPinYin = fullPinYinWithSeparator.split("-");
                StringBuffer group = new StringBuffer();
                int length = allPinYin.length;
                for (int i = 0; i < length; i++) {
                    for (int j = i; j < length; j++) {
                        group.append(allPinYin[j]);
                        if (group.toString().startsWith(constraintString)) {
                            return new int[]{i + offset, (j + 1) + offset};
                        }
                    }
                    group.delete(0, group.length());
                }
            }
            return new int[0];
        }

        private void sortByHighLightIndex(ArrayList<Map<String, Object>> list) {
            synchronized (TimeZoneAdapter.this.mLock) {
                Collections.sort(list, new Comparator<Map<String, Object>>() {
                    public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
                        Integer lhsStartIndexData = (Integer) lhs.get("high_light_start_index");
                        Integer lhsEndIndexData = (Integer) lhs.get("high_light_end_index");
                        Integer rhsStartIndexData = (Integer) rhs.get("high_light_start_index");
                        Integer rhsEndIndexData = (Integer) rhs.get("high_light_end_index");
                        if (lhsStartIndexData != null && rhsStartIndexData == null) {
                            return -1;
                        }
                        if (lhsStartIndexData == null && rhsStartIndexData != null) {
                            return 1;
                        }
                        if (!(lhsStartIndexData == null || rhsStartIndexData == null)) {
                            int diff = lhsStartIndexData.intValue() - rhsStartIndexData.intValue();
                            if (diff != 0) {
                                return diff;
                            }
                            if (lhsEndIndexData != null && rhsEndIndexData == null) {
                                Log.e(TimeZoneAdapter.TAG, "sortByHighLightIndex.rhsEndIndexData is null!");
                                return -1;
                            } else if (lhsEndIndexData == null && rhsEndIndexData != null) {
                                Log.e(TimeZoneAdapter.TAG, "sortByHighLightIndex.lhsEndIndexData is null!");
                                return 1;
                            } else if (!(lhsEndIndexData == null || rhsEndIndexData == null)) {
                                diff = lhsEndIndexData.intValue() - rhsEndIndexData.intValue();
                                if (diff != 0) {
                                    return diff;
                                }
                            }
                        }
                        return 0;
                    }
                });
            }
        }

        private void putHighLightData(Map<String, Object> item, String column, Integer start, Integer end) {
            item.put("high_light_column", column);
            item.put("high_light_start_index", start);
            item.put("high_light_end_index", end);
        }
    }

    public interface ViewBinder {
        boolean setViewValue(View view, Object obj, String str);
    }

    public TimeZoneAdapter(Context context, int itemLayoutResource, int textViewResourceId, List<? extends Map<String, Object>> objects, String sortKeyName, boolean digitLast, String[] from, int[] to) {
        super(context, itemLayoutResource, textViewResourceId, objects, sortKeyName, digitLast);
        this.mContext = context;
        this.mData = objects;
        this.mResource = itemLayoutResource;
        this.mFrom = (String[]) from.clone();
        this.mTo = (int[]) to.clone();
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        new Handler().postDelayed(new Runnable() {
            public void run() {
                TimeZoneAdapter.this.loadPinYinData();
            }
        }, 200);
    }

    public int getCount() {
        return this.mData.size();
    }

    public Object getItem(int position) {
        return this.mData.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, this.mResource);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
        View v;
        if (convertView == null) {
            v = this.mInflater.inflate(resource, parent, false);
        } else {
            v = convertView;
        }
        synchronized (this.mLock) {
            bindView(position, v);
        }
        return v;
    }

    private void bindView(int position, View view) {
        Map<String, Object> dataSet = (Map) this.mData.get(position);
        if (dataSet != null) {
            ViewBinder binder = this.mViewBinder;
            String[] from = this.mFrom;
            int[] to = this.mTo;
            int count = to.length;
            Object highLightColumnData = dataSet.get("high_light_column");
            String highLightColumnText = highLightColumnData == null ? "" : highLightColumnData.toString();
            Integer startIndexData = (Integer) dataSet.get("high_light_start_index");
            Integer endIndexData = (Integer) dataSet.get("high_light_end_index");
            for (int i = 0; i < count; i++) {
                View v = view.findViewById(to[i]);
                if (v != null) {
                    Object data = dataSet.get(from[i]);
                    String text = data == null ? "" : data.toString();
                    boolean bound = false;
                    if (binder != null) {
                        bound = binder.setViewValue(v, data, text);
                    }
                    if (bound) {
                        continue;
                    } else if (v instanceof Checkable) {
                        if (data instanceof Boolean) {
                            ((Checkable) v).setChecked(((Boolean) data).booleanValue());
                        } else if (v instanceof TextView) {
                            setViewText((TextView) v, text);
                        } else {
                            throw new IllegalStateException(v.getClass().getName() + " should be bound to a Boolean, not a " + (data == null ? "<unknown type>" : data.getClass()));
                        }
                    } else if (v instanceof TextView) {
                        if (!highLightColumnText.equalsIgnoreCase(from[i]) || startIndexData == null || endIndexData == null) {
                            setViewText((TextView) v, text);
                        } else {
                            int start = startIndexData.intValue();
                            int end = endIndexData.intValue();
                            if (start <= -1 || start > text.length() || end <= start || end > text.length()) {
                                setViewText((TextView) v, text);
                            } else {
                                SpannableStringBuilder ssb = new SpannableStringBuilder(text);
                                ssb.setSpan(new ForegroundColorSpan(Utils.getControlColor(this.mContext, this.mContext.getResources().getColor(2131427515))), start, end, 34);
                                ((TextView) v).setText(ssb);
                            }
                        }
                    } else if (!(v instanceof ImageView)) {
                        throw new IllegalStateException(v.getClass().getName() + " is not a " + " view that can be bounds by this SimpleAdapter");
                    } else if (data instanceof Integer) {
                        setViewImage((ImageView) v, ((Integer) data).intValue());
                    } else {
                        setViewImage((ImageView) v, text);
                    }
                }
            }
        }
    }

    public void setViewImage(ImageView v, int value) {
        v.setImageResource(value);
    }

    public void setViewImage(ImageView v, String value) {
        try {
            v.setImageResource(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            v.setImageURI(Uri.parse(value));
        }
    }

    public void setViewText(TextView v, String text) {
        v.setText(text);
    }

    public Filter getFilter() {
        if (this.mFilter == null) {
            this.mFilter = new SimpleFilter();
        }
        return this.mFilter;
    }

    private void loadPinYinData() {
        if (Locale.CHINESE.getLanguage().equals(Locale.getDefault().getLanguage())) {
            synchronized (this.mLock) {
                if (this.mUnfilteredData == null) {
                    this.mUnfilteredData = new ArrayList(this.mData);
                }
                for (Map<String, Object> item : this.mUnfilteredData) {
                    String displayName = (String) item.get("name");
                    if (displayName == null) {
                        displayName = "";
                    }
                    String localizedCity = getCityName(displayName);
                    String localizedCountry = getCountryName(displayName);
                    HashMap<String, String> cityMap = PinyinUtils.getPinYinMap(localizedCity);
                    item.put("city_full_pin_yin", cityMap.get("full_pin_yin"));
                    item.put("city_initial_pin_yin", cityMap.get("initial_pin_yin"));
                    HashMap<String, String> countryMap = PinyinUtils.getPinYinMap(localizedCountry);
                    item.put("country_full_pin_yin", countryMap.get("full_pin_yin"));
                    item.put("country_initial_pin_yin", countryMap.get("initial_pin_yin"));
                }
            }
            notifyDataSetChanged();
        }
    }

    public static String getCityName(String cityAndCountry) {
        String city = cityAndCountry;
        if (TextUtils.isEmpty(cityAndCountry)) {
            return "";
        }
        int index = cityAndCountry.indexOf("(");
        if (index > 0) {
            city = cityAndCountry.substring(0, index).trim();
        }
        return city;
    }

    public static String getCountryName(String cityAndCountry) {
        String country = "";
        if (TextUtils.isEmpty(cityAndCountry)) {
            return "";
        }
        int firstIndex = cityAndCountry.indexOf("(") + 1;
        int lastIndex = cityAndCountry.lastIndexOf(")");
        if (firstIndex > 0 && firstIndex < cityAndCountry.length() && lastIndex > firstIndex) {
            country = cityAndCountry.substring(firstIndex, lastIndex).trim();
        }
        return country;
    }
}
