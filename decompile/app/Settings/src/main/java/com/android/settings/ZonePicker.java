package com.android.settings;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filter.FilterListener;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import huawei.android.widget.AlphaIndexerListView;
import huawei.android.widget.HwQuickIndexController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.xmlpull.v1.XmlPullParserException;

public class ZonePicker extends ListFragment implements OnQueryTextListener, FilterListener {
    private AlphaIndexerListView mAlphaIndexerBar;
    private BaseAdapter mAlphabeticalAdapter;
    private String mFirstWidgetCity;
    private String mFirstWidgetDisplayName;
    private String mFirstWidgetTzId;
    private String mFirstWidgetUniqueId;
    private ZoneSelectionListener mListener;
    private HwQuickIndexController mQuickIndexController;
    private int mRequestType = 0;
    private View mResult;
    private TextView mResultCount;
    private SearchView mSearchView;

    public interface ZoneSelectionListener {
        void onZoneSelected(TimeZone timeZone);
    }

    public static TimeZone obtainTimeZoneFromItem(Object item) {
        return TimeZone.getTimeZone((String) ((Map) item).get("id"));
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        boolean z = true;
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getListView().setEmptyView(getView().findViewById(16908292));
        this.mQuickIndexController = new HwQuickIndexController(getListView(), this.mAlphaIndexerBar);
        AlphaIndexerListView alphaIndexerListView = this.mAlphaIndexerBar;
        if (getResources().getConfiguration().orientation != 2) {
            z = false;
        }
        alphaIndexerListView.buildIndexer(z, false);
        this.mQuickIndexController.setOnListen();
        updateAlphaIndexer();
        getListView().setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == 0) {
                    ZonePicker.this.hideSoftInputFromWindow();
                }
                return false;
            }
        });
        if (!(this.mRequestType == 2 || this.mRequestType == 3 || this.mRequestType == 4 || this.mRequestType == 5)) {
            if (this.mRequestType != 6) {
                return;
            }
        }
        this.mSearchView.setQueryHint(getResources().getString(2131628465));
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(2130968957, container, false);
        Utils.forcePrepareCustomPreferencesList(container, view, (ListView) view.findViewById(16908298), true);
        this.mSearchView = (SearchView) view.findViewById(2131886440);
        this.mSearchView.setImeOptions(301989894);
        this.mSearchView.setIconifiedByDefault(true);
        this.mSearchView.setIconified(false);
        this.mSearchView.onActionViewExpanded();
        this.mSearchView.clearFocus();
        this.mSearchView.setFocusable(false);
        this.mSearchView.setOnQueryTextListener(this);
        this.mResult = view.findViewById(2131886927);
        this.mResultCount = (TextView) view.findViewById(2131886928);
        this.mAlphabeticalAdapter = constructTimezoneAdapter(getActivity(), 2130968731, this.mRequestType);
        setListAdapter(this.mAlphabeticalAdapter);
        this.mAlphaIndexerBar = (AlphaIndexerListView) view.findViewById(2131886929);
        return view;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (16908332 == item.getItemId()) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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

    private static List<Map<String, Object>> getZones(Context context) {
        List<Map<String, Object>> myData = new ArrayList();
        long date = Calendar.getInstance().getTimeInMillis();
        boolean isHideJerusalem = System.getInt(context.getContentResolver(), "is_hide_jerusalem", 0) == 1;
        ArrayList arrayList = null;
        int requestType = 0;
        if (context instanceof Activity) {
            Intent intent = ((Activity) context).getIntent();
            if (intent != null) {
                arrayList = intent.getStringArrayListExtra("excluded_unique_ids");
                requestType = intent.getIntExtra("request_type", 0);
                date = intent.getLongExtra("request_date", date);
            }
        }
        if (arrayList == null) {
            arrayList = new ArrayList();
        }
        try {
            XmlResourceParser xrp = context.getResources().getXml(2131230913);
            do {
            } while (xrp.next() != 2);
            xrp.next();
            while (xrp.getEventType() != 3) {
                while (xrp.getEventType() != 2) {
                    if (xrp.getEventType() == 1) {
                        return myData;
                    }
                    xrp.next();
                }
                if (xrp.getName().equals("timezone")) {
                    String id = xrp.getAttributeValue(null, "id");
                    String city = xrp.getAttributeValue(null, "city");
                    String country = xrp.getAttributeValue(null, "country");
                    String displayName = xrp.nextText();
                    String dateStr = "";
                    if (requestType == 2 || requestType == 3 || requestType == 4 || requestType == 5 || requestType == 6) {
                        dateStr = Utils.getFormattedLocalDateString(context, id);
                    } else if (requestType == 8 || requestType == 9) {
                        if (TimeZone.getTimeZone(id).useDaylightTime()) {
                            displayName = displayName + " ☀";
                        }
                    }
                    String uniqueId = id + "_" + city + "_" + country;
                    if (isHideJerusalem && "Asia/Jerusalem".equals(id)) {
                        Log.d("ZonePicker", "getZones.Do not show Asia/Jerusalem");
                    } else if (arrayList.contains(uniqueId)) {
                        Log.d("ZonePicker", "getZones.Do not show excluded uniqueId:" + uniqueId);
                    } else {
                        addItem(myData, id, city, country, displayName, date, dateStr);
                    }
                }
                while (xrp.getEventType() != 3) {
                    xrp.next();
                }
                xrp.next();
            }
            xrp.close();
        } catch (XmlPullParserException e) {
            Log.e("ZonePicker", "Ill-formatted timezones.xml file");
        } catch (IOException e2) {
            Log.e("ZonePicker", "Unable to read timezones.xml file");
        }
        return myData;
    }

    private static void addItem(List<Map<String, Object>> myData, String id, String city, String country, String displayName, long date, String dateStr) {
        HashMap<String, Object> map = new HashMap();
        map.put("id", id);
        map.put("name", displayName);
        int offset = TimeZone.getTimeZone(id).getOffset(date);
        int p = Math.abs(offset);
        StringBuilder name = new StringBuilder();
        name.append("GMT");
        if (offset < 0) {
            name.append('-');
        } else {
            name.append('+');
        }
        name.append(p / 3600000);
        name.append(':');
        int min = (p / 60000) % 60;
        if (min < 10) {
            name.append('0');
        }
        name.append(min);
        map.put("gmt", name.toString());
        map.put("offset", Integer.valueOf(offset));
        map.put("city", city);
        map.put("country", country);
        map.put("date", dateStr);
        myData.add(map);
    }

    public void onListItemClick(ListView listView, View v, int position, long id) {
        if (isResumed()) {
            Map<?, ?> map = (Map) listView.getItemAtPosition(position);
            String tzId = (String) map.get("id");
            Intent data = new Intent();
            data.putExtra("id", tzId);
            String city = (String) map.get("city");
            data.putExtra("city", city);
            String country = (String) map.get("country");
            data.putExtra("country", country);
            String uniqueId = tzId + "_" + city + "_" + country;
            data.putExtra("unique_id", uniqueId);
            String displayName = (String) map.get("name");
            data.putExtra("name", displayName);
            if (this.mRequestType == 1) {
                System.putStringForUser(getActivity().getContentResolver(), "keyguard_default_time_zone", tzId, ActivityManager.getCurrentUser());
                System.putStringForUser(getActivity().getContentResolver(), "keyguard_dual_clocks_home_city_id", city, ActivityManager.getCurrentUser());
                getActivity().finish();
            } else if (this.mRequestType == 3) {
                this.mFirstWidgetTzId = tzId;
                this.mFirstWidgetCity = city;
                this.mFirstWidgetUniqueId = uniqueId;
                this.mFirstWidgetDisplayName = displayName;
                Intent widgetIntent = new Intent("huawei.intent.action.ZONE_PICKER");
                widgetIntent.putExtra("request_type", 4);
                ArrayList<String> excludedUniqueIds = getActivity().getIntent().getStringArrayListExtra("excluded_unique_ids");
                if (excludedUniqueIds == null) {
                    excludedUniqueIds = new ArrayList();
                }
                excludedUniqueIds.add(uniqueId);
                widgetIntent.putStringArrayListExtra("excluded_unique_ids", excludedUniqueIds);
                startActivityForResult(widgetIntent, 3800);
            } else if (this.mRequestType == 4) {
                data.putExtra("second_id", tzId);
                data.putExtra("second_city", city);
                data.putExtra("second_unique_id", uniqueId);
                data.putExtra("second_name", displayName);
                getActivity().setResult(-1, data);
                getActivity().finish();
            } else if (this.mRequestType > 0) {
                getActivity().setResult(-1, data);
                getActivity().finish();
            } else {
                ((AlarmManager) getActivity().getSystemService("alarm")).setTimeZone(tzId);
                TimeZone tz = TimeZone.getTimeZone(tzId);
                if (this.mListener != null) {
                    this.mListener.onZoneSelected(tz);
                } else {
                    getActivity().onBackPressed();
                }
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mRequestType = getActivity().getIntent().getIntExtra("request_type", this.mRequestType);
        switch (this.mRequestType) {
            case 1:
                getActivity().setTitle(2131627506);
                break;
            case 2:
                getActivity().setTitle(2131628470);
                break;
            case 3:
                getActivity().setTitle(2131628466);
                break;
            case 4:
                getActivity().setTitle(2131628467);
                break;
            case 5:
                getActivity().setTitle(2131628468);
                break;
            case 6:
                getActivity().setTitle(2131628469);
                break;
            case 7:
                getActivity().setTitle(2131628471);
                break;
            case 8:
                getActivity().setTitle(2131628472);
                break;
        }
        String activityTitle = getActivity().getIntent().getStringExtra("activity_title");
        if (!TextUtils.isEmpty(activityTitle)) {
            getActivity().setTitle(activityTitle);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (this.mRequestType == 3 && resultCode == -1) {
            data.putExtra("id", this.mFirstWidgetTzId);
            data.putExtra("city", this.mFirstWidgetCity);
            data.putExtra("unique_id", this.mFirstWidgetUniqueId);
            data.putExtra("name", this.mFirstWidgetDisplayName);
            getActivity().setResult(-1, data);
            getActivity().finish();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        boolean z;
        super.onConfigurationChanged(newConfig);
        AlphaIndexerListView alphaIndexerListView = this.mAlphaIndexerBar;
        if (getResources().getConfiguration().orientation == 2) {
            z = true;
        } else {
            z = false;
        }
        alphaIndexerListView.buildIndexer(z, false);
        updateAlphaIndexer();
    }

    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    public boolean onQueryTextChange(String searchString) {
        updateAlphaIndexer();
        Filter filter = ((Filterable) getListView().getAdapter()).getFilter();
        if (filter != null) {
            filter.filter(searchString, this);
        }
        return true;
    }

    public void onFilterComplete(int count) {
        int resultCount = this.mAlphabeticalAdapter.getCount();
        if (resultCount == 0 || TextUtils.isEmpty(this.mSearchView.getQuery())) {
            this.mResult.setVisibility(8);
        } else {
            this.mResult.setVisibility(0);
            this.mResultCount.setText(getResources().getQuantityString(2131689541, resultCount, new Object[]{Integer.valueOf(resultCount)}));
        }
        getListView().post(new Runnable() {
            public void run() {
                ZonePicker.this.getListView().setSelectionAfterHeaderView();
            }
        });
    }

    private void updateAlphaIndexer() {
        boolean sideBarVisble = TextUtils.isEmpty(this.mSearchView.getQuery());
        this.mAlphaIndexerBar.setVisibility(sideBarVisble ? 0 : 8);
        this.mAlphaIndexerBar.setShowPopup(sideBarVisble);
        if (sideBarVisble) {
            getListView().setDivider(getResources().getDrawable(2130838532));
        } else {
            getListView().setDivider(getResources().getDrawable(2130838529));
        }
    }

    protected void hideSoftInputFromWindow() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService("input_method");
        View focus = getActivity().getCurrentFocus();
        if (focus != null) {
            imm.hideSoftInputFromWindow(focus.getApplicationWindowToken(), 2);
            this.mSearchView.clearFocus();
        }
    }

    public static BaseAdapter constructTimezoneAdapter(Context context, int layoutId, int requestType) {
        String[] from = new String[]{"name", "gmt"};
        if (!(requestType == 2 || requestType == 3 || requestType == 4 || requestType == 5)) {
            if (requestType == 6) {
            }
            return new TimeZoneAdapter(context, layoutId, 0, getZones(context), "name", true, from, new int[]{16908308, 16908309});
        }
        from = new String[]{"name", "date"};
        return new TimeZoneAdapter(context, layoutId, 0, getZones(context), "name", true, from, new int[]{16908308, 16908309});
    }
}
