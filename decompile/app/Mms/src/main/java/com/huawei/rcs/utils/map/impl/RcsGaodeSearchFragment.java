package com.huawei.rcs.utils.map.impl;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.Query;
import com.google.android.gms.R;
import com.huawei.mms.ui.HwBaseFragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RcsGaodeSearchFragment extends HwBaseFragment {
    private SimpleAdapter adapter;
    private ArrayList<Map<String, String>> addressList = new ArrayList();
    private TextView bakButton;
    private ProgressBar bar;
    private String city;
    private ListView listView;
    private PoiSearch poiSearch;
    OnPoiSearchListener poiSearchListener = new OnPoiSearchListener() {
        public void onPoiSearched(PoiResult result, int i) {
            if (i == 1000) {
                RcsGaodeSearchFragment.this.addressList.clear();
                List<PoiItem> poiList = result.getPois();
                for (int n = 0; n < poiList.size(); n++) {
                    Map<String, String> item = new HashMap();
                    item.put("title", ((PoiItem) poiList.get(n)).getTitle());
                    item.put("subtitle", ((PoiItem) poiList.get(n)).getSnippet());
                    item.put("latitude", ((PoiItem) poiList.get(n)).getLatLonPoint().getLatitude() + "");
                    item.put("longitude", ((PoiItem) poiList.get(n)).getLatLonPoint().getLongitude() + "");
                    RcsGaodeSearchFragment.this.addressList.add(item);
                }
                RcsGaodeSearchFragment.this.bar.setVisibility(8);
                RcsGaodeSearchFragment.this.listView.setVisibility(0);
                RcsGaodeSearchFragment.this.adapter.notifyDataSetChanged();
                return;
            }
            Log.e("RcsGaodeSearchFragment", "poisearch return error");
        }

        public void onPoiItemSearched(PoiItem arg0, int arg1) {
        }
    };
    private SearchView searchView;

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        View view = inflater.inflate(R.layout.rcs_map_search, viewGroup, false);
        init(view);
        return view;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.city = getIntent().getStringExtra("city");
    }

    private void init(View view) {
        ActionBar actionbar = getActivity().getActionBar();
        actionbar.setDisplayShowHomeEnabled(false);
        actionbar.setDisplayShowCustomEnabled(true);
        actionbar.setDisplayOptions(16);
        LinearLayout actionbarView = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.rcs_map_search_actionbar, null);
        actionbar.setCustomView(actionbarView, new LayoutParams(-1, -2));
        this.searchView = (SearchView) actionbarView.findViewById(R.id.search_view);
        this.bakButton = (TextView) actionbarView.findViewById(R.id.back_button);
        this.bakButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                ((InputMethodManager) RcsGaodeSearchFragment.this.getActivity().getSystemService("input_method")).hideSoftInputFromWindow(RcsGaodeSearchFragment.this.searchView.getWindowToken(), 0);
                RcsGaodeSearchFragment.this.getActivity().onBackPressed();
            }
        });
        this.bar = (ProgressBar) view.findViewById(R.id.bar);
        this.poiSearch = new PoiSearch(getActivity(), null);
        this.poiSearch.setOnPoiSearchListener(this.poiSearchListener);
        this.listView = (ListView) view.findViewById(R.id.addList);
        this.adapter = new SimpleAdapter(getActivity(), this.addressList, R.layout.rcs_map_item, new String[]{"title", "subtitle"}, new int[]{R.id.mapitem_title, R.id.mapitem_sub});
        this.listView.setAdapter(this.adapter);
        this.searchView.setSubmitButtonEnabled(true);
        this.searchView.setOnQueryTextListener(new OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                return false;
            }

            public boolean onQueryTextSubmit(String qurery) {
                ((InputMethodManager) RcsGaodeSearchFragment.this.getActivity().getSystemService("input_method")).hideSoftInputFromWindow(RcsGaodeSearchFragment.this.searchView.getWindowToken(), 0);
                if (RcsGaodeSearchFragment.this.searchView.getQuery() == null || RcsGaodeSearchFragment.this.searchView.getQuery().toString().equals("")) {
                    return false;
                }
                RcsGaodeSearchFragment.this.bar.setVisibility(0);
                RcsGaodeSearchFragment.this.listView.setVisibility(8);
                RcsGaodeSearchFragment.this.poiSearch.setQuery(new Query(RcsGaodeSearchFragment.this.searchView.getQuery().toString(), null, RcsGaodeSearchFragment.this.city));
                RcsGaodeSearchFragment.this.poiSearch.searchPOIAsyn();
                return true;
            }
        });
        this.listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String latitude = (String) ((Map) RcsGaodeSearchFragment.this.addressList.get(position)).get("latitude");
                String longitude = (String) ((Map) RcsGaodeSearchFragment.this.addressList.get(position)).get("longitude");
                String title = (String) ((Map) RcsGaodeSearchFragment.this.addressList.get(position)).get("title");
                String subtitle = (String) ((Map) RcsGaodeSearchFragment.this.addressList.get(position)).get("subtitle");
                Intent intent = new Intent();
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("title", title);
                intent.putExtra("subtitle", subtitle);
                RcsGaodeSearchFragment.this.getActivity().setResult(-1, intent);
                RcsGaodeSearchFragment.this.finishSelf(true);
            }
        });
    }
}
