package com.android.contacts.hap.tianyi;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Filter;
import android.widget.Filter.FilterResults;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.contacts.ContactsUtils;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SelectCountryActivity extends ListActivity implements OnChildClickListener {
    private TianyiDisplayAdapter mAdapter;
    private EditText mCountrySearchView;
    private ExpandableListView mList;
    private LinearLayout mSearchLayout;
    private List<TianyiDetail> mTianyiList;
    private Resources resources;

    public static class CountryDetail {
        String countryCode;
        int countryKey;
        String englishName;
        long mId;

        CountryDetail(long id, int countryKey, String countryCode, String aEnglishName) {
            this.mId = id;
            this.countryKey = countryKey;
            this.countryCode = countryCode;
            this.englishName = aEnglishName;
        }
    }

    public class TianyiCountryFilter extends Filter {
        protected FilterResults performFiltering(CharSequence aQuery) {
            CharSequence toLowerCase;
            try {
                toLowerCase = aQuery.toString().toLowerCase(Locale.US);
            } catch (NullPointerException e) {
                toLowerCase = null;
            }
            FilterResults lFilterResults = new FilterResults();
            List<TianyiDetail> mTianyiListFilter = new ArrayList();
            ArrayList<CountryDetail> mCountryListFilter = new ArrayList();
            if (toLowerCase != null && toLowerCase.length() > 0) {
                for (TianyiDetail tianyiDetail : SelectCountryActivity.this.mTianyiList) {
                    for (CountryDetail countryDetail : tianyiDetail.mCountryDetailList) {
                        if (SelectCountryActivity.this.resources.getString(countryDetail.countryKey).toLowerCase(Locale.US).contains(toLowerCase) || countryDetail.englishName.toLowerCase(Locale.US).contains(toLowerCase)) {
                            mCountryListFilter.add(countryDetail);
                        }
                    }
                    if (mCountryListFilter.size() > 0) {
                        mTianyiListFilter.add(new TianyiDetail(tianyiDetail.mId, tianyiDetail.mContinentKey, mCountryListFilter));
                    }
                    mCountryListFilter = new ArrayList();
                }
                lFilterResults.values = mTianyiListFilter;
                lFilterResults.count = mTianyiListFilter.size();
            }
            return lFilterResults;
        }

        protected void publishResults(CharSequence aQuery, FilterResults aFilterResult) {
            if (HwLog.HWDBG) {
                HwLog.d("SelectCountryActivity", "TianyiCountryFilter::in publish result");
            }
            if (aFilterResult != null) {
                SelectCountryActivity.this.mAdapter.setData((List) aFilterResult.values);
            }
        }
    }

    public static class TianyiDetail {
        int mContinentKey;
        ArrayList<CountryDetail> mCountryDetailList;
        int mId;

        TianyiDetail(int id, int continentKey, ArrayList<CountryDetail> countryDetailList) {
            this.mId = id;
            this.mContinentKey = continentKey;
            this.mCountryDetailList = countryDetailList;
        }
    }

    public class TianyiDisplayAdapter extends BaseExpandableListAdapter implements Filterable {
        private Context mContext;
        private Filter mFilter;
        private LayoutInflater mInflater;
        private List<TianyiDetail> mTianyiDataList;

        public TianyiDisplayAdapter(Context context) {
            this.mContext = context;
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        }

        public void setData(List<TianyiDetail> mTianyiList) {
            this.mTianyiDataList = mTianyiList;
            notifyDataSetChanged();
        }

        public Object getChild(int groupPosition, int childPosition) {
            boolean validChild = false;
            if (this.mTianyiDataList == null) {
                return null;
            }
            TianyiDetail mTianyiDetail = (TianyiDetail) this.mTianyiDataList.get(groupPosition);
            if (childPosition >= 0 && childPosition < mTianyiDetail.mCountryDetailList.size()) {
                validChild = true;
            }
            if (validChild) {
                return mTianyiDetail.mCountryDetailList.get(childPosition);
            }
            return null;
        }

        public long getChildId(int groupPosition, int childPosition) {
            CountryDetail child = (CountryDetail) getChild(groupPosition, childPosition);
            if (child != null) {
                return child.mId;
            }
            return Long.MIN_VALUE;
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = this.mInflater.inflate(R.layout.tianyi_list_country, parent, false);
            }
            TextView text1 = (TextView) convertView.findViewById(16908308);
            TextView text2 = (TextView) convertView.findViewById(16908309);
            CountryDetail child = (CountryDetail) getChild(groupPosition, childPosition);
            if (child != null) {
                text1.setText(this.mContext.getString(child.countryKey));
                text2.setText("(" + child.countryCode + ")");
            }
            return convertView;
        }

        public int getChildrenCount(int groupPosition) {
            if (this.mTianyiDataList == null) {
                return 0;
            }
            return ((TianyiDetail) this.mTianyiDataList.get(groupPosition)).mCountryDetailList.size();
        }

        public Object getGroup(int groupPosition) {
            if (this.mTianyiDataList == null) {
                return null;
            }
            return this.mTianyiDataList.get(groupPosition);
        }

        public int getGroupCount() {
            if (this.mTianyiDataList == null) {
                return 0;
            }
            return this.mTianyiDataList.size();
        }

        public long getGroupId(int groupPosition) {
            return (long) groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = this.mInflater.inflate(R.layout.tianyi_list_continent, parent, false);
            }
            TianyiDetail mTianyiDetail = (TianyiDetail) getGroup(groupPosition);
            if (mTianyiDetail != null) {
                ((TextView) convertView.findViewById(16908308)).setText(this.mContext.getString(mTianyiDetail.mContinentKey));
            }
            return convertView;
        }

        public boolean hasStableIds() {
            return true;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public Filter getFilter() {
            if (this.mFilter == null) {
                this.mFilter = new TianyiCountryFilter();
            }
            return this.mFilter;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        createContinentCountryList();
        loadContinentCountryDetails();
        setContentView(R.layout.tianyi_country_list);
        configureSearchLayout();
        this.resources = getResources();
        this.mList = (ExpandableListView) findViewById(16908298);
        this.mList.setFastScrollEnabled(true);
        this.mList.setOnChildClickListener(this);
        this.mList.setHeaderDividersEnabled(true);
        this.mAdapter = new TianyiDisplayAdapter(this);
        this.mAdapter.setData(this.mTianyiList);
        this.mList.setAdapter(this.mAdapter);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }

    private void configureSearchLayout() {
        this.mCountrySearchView = (EditText) findViewById(R.id.tianyi_search_view);
        ContactsUtils.configureSearchViewInputType(this.mCountrySearchView);
        this.mCountrySearchView.setHint(CommonUtilMethods.getSearchViewSpannableHint(getBaseContext(), getResources().getString(17039372), this.mCountrySearchView.getTextSize()));
        this.mCountrySearchView.setCursorVisible(false);
        this.mSearchLayout = (LinearLayout) findViewById(R.id.tianyisearchlayout);
        this.mCountrySearchView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);
                SelectCountryActivity.this.mSearchLayout.setBackgroundResource(R.drawable.textfield_activated_holo_light);
                SelectCountryActivity.this.mCountrySearchView.setCursorVisible(true);
                return true;
            }
        });
        final ImageView searchClearButton = (ImageView) findViewById(R.id.tianyiClearSearchResult);
        searchClearButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SelectCountryActivity.this.mCountrySearchView.setText(null);
            }
        });
        this.mCountrySearchView.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s == null || TextUtils.isEmpty(s.toString().trim())) {
                    SelectCountryActivity.this.mAdapter.setData(SelectCountryActivity.this.mTianyiList);
                    searchClearButton.setVisibility(8);
                    return;
                }
                SelectCountryActivity.this.mAdapter.getFilter().filter(s.toString());
                searchClearButton.setVisibility(0);
            }
        });
    }

    public void onResume() {
        super.onResume();
        this.mSearchLayout.setBackgroundResource(R.drawable.contact_textfield_default_holo_light);
        this.mCountrySearchView.setCursorVisible(false);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                setResult(0);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onStop() {
        if (!(this.mCountrySearchView == null || TextUtils.isEmpty(this.mCountrySearchView.getText().toString()))) {
            this.mCountrySearchView.setText(null);
        }
        super.onStop();
    }

    private void createContinentCountryList() {
        this.mTianyiList = new ArrayList();
    }

    private void loadContinentCountryDetails() {
        ArrayList<CountryDetail> mCountryList = new ArrayList();
        mCountryList.add(new CountryDetail(0, R.string.country_afghanistan, "+93", "Afghanistan"));
        mCountryList.add(new CountryDetail(1, R.string.country_armenia, "+374", "Armenia"));
        mCountryList.add(new CountryDetail(2, R.string.country_azerbaijan, "+994", "Azerbaijan"));
        mCountryList.add(new CountryDetail(3, R.string.country_bahrain, "+973", "Bahrain"));
        mCountryList.add(new CountryDetail(4, R.string.country_bangladesh, "+880", "Bangladesh"));
        mCountryList.add(new CountryDetail(5, R.string.country_bruneiDarussalam, "+773", "Brunei Darussalam"));
        mCountryList.add(new CountryDetail(6, R.string.country_eastTimor, "+770", "East Timor"));
        mCountryList.add(new CountryDetail(7, R.string.country_hongKong, "+852", "Hong Kong"));
        mCountryList.add(new CountryDetail(8, R.string.country_india, "+91", "India"));
        mCountryList.add(new CountryDetail(9, R.string.country_indonesia, "+72", "Indonesia"));
        mCountryList.add(new CountryDetail(10, R.string.country_iran, "+98", "Iran"));
        mCountryList.add(new CountryDetail(11, R.string.country_iraq, "+964", "Iraq"));
        mCountryList.add(new CountryDetail(12, R.string.country_israel, "+972", "Israel"));
        mCountryList.add(new CountryDetail(13, R.string.country_japan, "+81", "Japan"));
        mCountryList.add(new CountryDetail(14, R.string.country_jordan, "+962", "Jordan"));
        mCountryList.add(new CountryDetail(15, R.string.country_kazakhstan, "+7", "Kazakhstan"));
        mCountryList.add(new CountryDetail(16, R.string.country_republicofKorea, "+82", "Republic of Korea"));
        mCountryList.add(new CountryDetail(17, R.string.country_kuwait, "+965", "Kuwait"));
        mCountryList.add(new CountryDetail(18, R.string.country_kirghizstan, "+996", "Kirghizstan"));
        mCountryList.add(new CountryDetail(19, R.string.country_laos, "+856", "Laos"));
        mCountryList.add(new CountryDetail(20, R.string.country_lebanon, "+961", "Lebanon"));
        mCountryList.add(new CountryDetail(21, R.string.country_macao, "+853", "Macao"));
        mCountryList.add(new CountryDetail(22, R.string.country_malaysia, "+70", "Malaysia"));
        mCountryList.add(new CountryDetail(23, R.string.country_maldives, "+960", "Maldives"));
        mCountryList.add(new CountryDetail(24, R.string.country_mongolia, "+976", "Mongolia"));
        mCountryList.add(new CountryDetail(25, R.string.country_nepal, "+977", "Nepal"));
        mCountryList.add(new CountryDetail(26, R.string.country_oman, "+968", "Oman"));
        mCountryList.add(new CountryDetail(27, R.string.country_pakistan, "+92", "Pakistan"));
        mCountryList.add(new CountryDetail(28, R.string.country_palestine, "+970", "Palestine"));
        mCountryList.add(new CountryDetail(29, R.string.country_qatar, "+974", "Qatar"));
        mCountryList.add(new CountryDetail(30, R.string.country_singapore, "+75", "Singapore"));
        mCountryList.add(new CountryDetail(31, R.string.country_srilanka, "+94", "Sri Lanka"));
        mCountryList.add(new CountryDetail(32, R.string.country_syria, "+963", "Syria"));
        mCountryList.add(new CountryDetail(33, R.string.country_taiwan, "+886", "Taiwan"));
        mCountryList.add(new CountryDetail(34, R.string.country_tajikistan, "+992", "Tajikistan"));
        mCountryList.add(new CountryDetail(35, R.string.country_thailand, "+76", "Thailand"));
        mCountryList.add(new CountryDetail(36, R.string.country_turkey, "+90", "Turkey"));
        mCountryList.add(new CountryDetail(37, R.string.country_turkmenistan, "+993", "Turkmenistan"));
        mCountryList.add(new CountryDetail(38, R.string.country_unitedArabEmirates, "+971", "United Arab Emirates"));
        mCountryList.add(new CountryDetail(39, R.string.country_uzbekistan, "+998", "Uzbekistan"));
        mCountryList.add(new CountryDetail(40, R.string.country_vietnam, "+84", "Vietnam"));
        mCountryList.add(new CountryDetail(41, R.string.country_yemen, "+967", "Yemen"));
        this.mTianyiList.add(new TianyiDetail(0, R.string.continent_asia, mCountryList));
        mCountryList = new ArrayList();
        mCountryList.add(new CountryDetail(0, R.string.country_algeria, "+213", "Algeria"));
        mCountryList.add(new CountryDetail(1, R.string.country_angola, "+244", "Angola"));
        mCountryList.add(new CountryDetail(2, R.string.country_benin, "+229", "Benin"));
        mCountryList.add(new CountryDetail(3, R.string.country_botswana, "+267", "Botswana"));
        mCountryList.add(new CountryDetail(4, R.string.country_burkinaFaso, "+226", "Burkina Faso"));
        mCountryList.add(new CountryDetail(5, R.string.country_burundi, "+257", "Burundi"));
        mCountryList.add(new CountryDetail(6, R.string.country_cameroon, "+237", "Cameroon"));
        mCountryList.add(new CountryDetail(7, R.string.country_theCanaryIslands, "+34", "The Canary Islands (Spain)"));
        mCountryList.add(new CountryDetail(8, R.string.country_capeVerde, "+238", "Cape Verde"));
        mCountryList.add(new CountryDetail(9, R.string.country_centralAfricanRepublic, "+236", "Central African Republic"));
        mCountryList.add(new CountryDetail(10, R.string.country_ceuta, "+34952", "Ceuta (Spain)"));
        mCountryList.add(new CountryDetail(11, R.string.country_republicOfTheCongo, "+242", "Republic of the Congo"));
        mCountryList.add(new CountryDetail(12, R.string.country_democraticRepublicOfCongo, "+243", "Democratic Republic of Congo"));
        mCountryList.add(new CountryDetail(13, R.string.country_egypt, "+20", "Egypt"));
        mCountryList.add(new CountryDetail(14, R.string.country_equatorialGuinea, "+240", "Equatorial Guinea"));
        mCountryList.add(new CountryDetail(15, R.string.country_gabon, "+241", "Gabon"));
        mCountryList.add(new CountryDetail(16, R.string.country_gambia, "+220", "Gambia"));
        mCountryList.add(new CountryDetail(17, R.string.country_ghana, "+233", "Ghana"));
        mCountryList.add(new CountryDetail(18, R.string.country_guinea, "+224", "Guinea"));
        mCountryList.add(new CountryDetail(19, R.string.country_kenya, "+254", "Kenya"));
        mCountryList.add(new CountryDetail(20, R.string.country_liberia, "+231", "Liberia"));
        mCountryList.add(new CountryDetail(21, R.string.country_libya, "+218", "Libya"));
        mCountryList.add(new CountryDetail(22, R.string.country_madagascar, "+261", "Madagascar"));
        mCountryList.add(new CountryDetail(23, R.string.country_malawi, "+265", "Malawi"));
        mCountryList.add(new CountryDetail(24, R.string.country_mali, "+223", "Mali"));
        mCountryList.add(new CountryDetail(25, R.string.country_mauritania, "+222", "Mauritania"));
        mCountryList.add(new CountryDetail(26, R.string.country_mauritius, "+230", "Mauritius"));
        mCountryList.add(new CountryDetail(27, R.string.country_morocco, "+212", "Morocco"));
        mCountryList.add(new CountryDetail(28, R.string.country_mozambique, "+258", "Mozambique"));
        mCountryList.add(new CountryDetail(29, R.string.country_namibia, "+264", "Namibia"));
        mCountryList.add(new CountryDetail(30, R.string.country_niger, "+227", "Niger"));
        mCountryList.add(new CountryDetail(31, R.string.country_nigeria, "+234", "Nigeria"));
        mCountryList.add(new CountryDetail(32, R.string.country_senegal, "+221", "Senegal"));
        mCountryList.add(new CountryDetail(33, R.string.country_seychelles, "+248", "Seychelles"));
        mCountryList.add(new CountryDetail(34, R.string.country_sierraLeone, "+232", "Sierra Leone"));
        mCountryList.add(new CountryDetail(35, R.string.country_southAfrica, "+27", "South Africa"));
        mCountryList.add(new CountryDetail(36, R.string.country_sudan, "+249", "Sudan"));
        mCountryList.add(new CountryDetail(37, R.string.country_tanzania, "+255", "Tanzania"));
        mCountryList.add(new CountryDetail(38, R.string.country_tunisia, "+216", "Tunisia"));
        mCountryList.add(new CountryDetail(39, R.string.country_uganda, "+256", "Uganda"));
        mCountryList.add(new CountryDetail(40, R.string.country_zambia, "+260", "Zambia"));
        mCountryList.add(new CountryDetail(41, R.string.country_zanzibar, "+255", "Zanzibar"));
        mCountryList.add(new CountryDetail(42, R.string.country_zimbabwe, "+263", "Zimbabwe"));
        this.mTianyiList.add(new TianyiDetail(1, R.string.continent_africa, mCountryList));
        mCountryList = new ArrayList();
        mCountryList.add(new CountryDetail(0, R.string.country_albania, "+355", "Albania"));
        mCountryList.add(new CountryDetail(1, R.string.country_andorra, "+376", "Andorra"));
        mCountryList.add(new CountryDetail(2, R.string.country_austria, "+43", "Austria"));
        mCountryList.add(new CountryDetail(3, R.string.country_azores, "+351", "Azores"));
        mCountryList.add(new CountryDetail(4, R.string.country_belarus, "+375", "Belarus"));
        mCountryList.add(new CountryDetail(5, R.string.country_belgium, "+32", "Belgium"));
        mCountryList.add(new CountryDetail(6, R.string.country_bosniaAndHerzegovina, "+387", "Bosnia and Herzegovina"));
        mCountryList.add(new CountryDetail(7, R.string.country_bulgaria, "+359", "Bulgaria"));
        mCountryList.add(new CountryDetail(8, R.string.country_corsica, "+495", "Corsica (France)"));
        mCountryList.add(new CountryDetail(9, R.string.country_croatia, "+385", "Croatia"));
        mCountryList.add(new CountryDetail(10, R.string.country_cyprus, "+357", "Cyprus"));
        mCountryList.add(new CountryDetail(11, R.string.country_czechRepublic, "+420", "Czech Republic"));
        mCountryList.add(new CountryDetail(12, R.string.country_denmark, "+45", "Denmark"));
        mCountryList.add(new CountryDetail(13, R.string.country_estonia, "+372", "Estonia"));
        mCountryList.add(new CountryDetail(14, R.string.country_faroeIslands, "+298", "Faroe Islands"));
        mCountryList.add(new CountryDetail(15, R.string.country_finland, "+358", "Finland"));
        mCountryList.add(new CountryDetail(16, R.string.country_france, "+33", "France"));
        mCountryList.add(new CountryDetail(17, R.string.country_georgia, "+995", "Georgia"));
        mCountryList.add(new CountryDetail(18, R.string.country_germany, "+49", "Germany"));
        mCountryList.add(new CountryDetail(19, R.string.country_gibraltar, "+350", "Gibraltar"));
        mCountryList.add(new CountryDetail(20, R.string.country_greece, "+30", "Greece"));
        mCountryList.add(new CountryDetail(21, R.string.country_hungary, "+36", "Hungary"));
        mCountryList.add(new CountryDetail(22, R.string.country_ibiza, "+34", "Ibiza"));
        mCountryList.add(new CountryDetail(23, R.string.country_iceland, "+354", "Iceland"));
        mCountryList.add(new CountryDetail(24, R.string.country_ireland, "+353", "Ireland"));
        mCountryList.add(new CountryDetail(25, R.string.country_italy, "+39", "Italy"));
        mCountryList.add(new CountryDetail(26, R.string.country_jersey, "+44", "Jersey"));
        mCountryList.add(new CountryDetail(27, R.string.country_kosovo, "+381", "Kosovo"));
        mCountryList.add(new CountryDetail(28, R.string.country_latvia, "+371", "Latvia"));
        mCountryList.add(new CountryDetail(29, R.string.country_liechtenstein, "+423", "Liechtenstein"));
        mCountryList.add(new CountryDetail(30, R.string.country_lithuania, "+370", "Lithuania"));
        mCountryList.add(new CountryDetail(31, R.string.country_luxembourg, "+352", "Luxembourg"));
        mCountryList.add(new CountryDetail(32, R.string.country_macedonia, "+389", "Macedonia"));
        mCountryList.add(new CountryDetail(33, R.string.country_madeira, "+351", "Madeira"));
        mCountryList.add(new CountryDetail(34, R.string.country_mallorca, "+34971", "Mallorca"));
        mCountryList.add(new CountryDetail(35, R.string.country_malta, "+356", "Malta"));
        mCountryList.add(new CountryDetail(36, R.string.country_moldova, "+373", "Moldova"));
        mCountryList.add(new CountryDetail(37, R.string.country_monaco, "+377", "Monaco"));
        mCountryList.add(new CountryDetail(38, R.string.country_montenegro, "+382", "Montenegro"));
        mCountryList.add(new CountryDetail(39, R.string.country_netherlands, "+31", "Netherlands"));
        mCountryList.add(new CountryDetail(40, R.string.country_northernIreland, "+44", "Northern Ireland"));
        mCountryList.add(new CountryDetail(41, R.string.country_norway, "+47", "Norway"));
        mCountryList.add(new CountryDetail(42, R.string.country_poland, "+48", "Poland"));
        mCountryList.add(new CountryDetail(43, R.string.country_portugal, "+351", "Portugal"));
        mCountryList.add(new CountryDetail(44, R.string.country_romania, "+40", "Romania"));
        mCountryList.add(new CountryDetail(45, R.string.country_russianFederation, "+7", "Russian Federation"));
        mCountryList.add(new CountryDetail(46, R.string.country_sardinia, "+39", "Sardinia"));
        mCountryList.add(new CountryDetail(47, R.string.country_serbia, "+381", "Serbia"));
        mCountryList.add(new CountryDetail(48, R.string.country_sicily, "+39", "Sicily"));
        mCountryList.add(new CountryDetail(49, R.string.country_slovakia, "+421", "Slovakia"));
        mCountryList.add(new CountryDetail(50, R.string.country_slovenia, "+386", "Slovenia"));
        mCountryList.add(new CountryDetail(51, R.string.country_spain, "+34", "Spain"));
        mCountryList.add(new CountryDetail(52, R.string.country_sweden, "+46", "Sweden"));
        mCountryList.add(new CountryDetail(53, R.string.country_switzerland, "+41", "Switzerland"));
        mCountryList.add(new CountryDetail(54, R.string.country_ukraine, "+380", "Ukraine"));
        mCountryList.add(new CountryDetail(55, R.string.country_unitedKingdom, "+44", "United Kingdom"));
        mCountryList.add(new CountryDetail(56, R.string.country_alandIslands, "+358", "Aland Islands (Finland)"));
        this.mTianyiList.add(new TianyiDetail(2, R.string.continent_europe, mCountryList));
        mCountryList = new ArrayList();
        mCountryList.add(new CountryDetail(0, R.string.country_anguilla, "+1264", "Anguilla"));
        mCountryList.add(new CountryDetail(1, R.string.country_antiguaAndBarbuda, "+1268", "Antigua and Barbuda"));
        mCountryList.add(new CountryDetail(2, R.string.country_aruba, "+297", "Aruba"));
        mCountryList.add(new CountryDetail(3, R.string.country_bahamas, "+1242", "Bahamas"));
        mCountryList.add(new CountryDetail(4, R.string.country_barbados, "+1246", "Barbados"));
        mCountryList.add(new CountryDetail(5, R.string.country_belize, "+501", "Belize"));
        mCountryList.add(new CountryDetail(6, R.string.country_bermuda, "+1441", "Bermuda"));
        mCountryList.add(new CountryDetail(7, R.string.country_canada, "+1", "Canada"));
        mCountryList.add(new CountryDetail(8, R.string.country_caymanIslands, "+1345", "Cayman Islands"));
        mCountryList.add(new CountryDetail(9, R.string.country_cuba, "+53", "Cuba"));
        mCountryList.add(new CountryDetail(10, R.string.country_dominica, "+1767", "Dominica"));
        mCountryList.add(new CountryDetail(11, R.string.country_dominicanRepublic, "+1809", "Dominican Republic"));
        mCountryList.add(new CountryDetail(12, R.string.country_elSalvador, "+503", "El Salvador"));
        mCountryList.add(new CountryDetail(13, R.string.country_grenada, "+1473", "Grenada"));
        mCountryList.add(new CountryDetail(14, R.string.country_guadeloupe, "+590", "Guadeloupe"));
        mCountryList.add(new CountryDetail(15, R.string.country_haiti, "+509", "Haiti"));
        mCountryList.add(new CountryDetail(16, R.string.country_jamaica, "+1876", "Jamaica"));
        mCountryList.add(new CountryDetail(17, R.string.country_marieGalante, "+011", "Marie - Galante"));
        mCountryList.add(new CountryDetail(18, R.string.country_martinique, "+596", "Martinique"));
        mCountryList.add(new CountryDetail(19, R.string.country_mexico, "+52", "Mexico"));
        mCountryList.add(new CountryDetail(20, R.string.country_netherlandsAntilles, "+599", "Netherlands Antilles"));
        mCountryList.add(new CountryDetail(21, R.string.country_nicaragua, "+505", "Nicaragua"));
        mCountryList.add(new CountryDetail(22, R.string.country_panama, "+507", "Panama"));
        mCountryList.add(new CountryDetail(23, R.string.country_puertoRico, "+1787", "Puerto Rico"));
        mCountryList.add(new CountryDetail(24, R.string.country_saintKittsAndNevis, "+869", "Saint Kitts and Nevis"));
        mCountryList.add(new CountryDetail(25, R.string.country_sintMaarten, "+721", "Sint Maarten"));
        mCountryList.add(new CountryDetail(26, R.string.country_turksAndCaicosIslands, "+1649", "Turks and Caicos Islands"));
        mCountryList.add(new CountryDetail(27, R.string.country_USA, "+1", "United States of America"));
        mCountryList.add(new CountryDetail(28, R.string.country_virginIslandsBritish, "+284", "Virgin Islands (British)"));
        mCountryList.add(new CountryDetail(29, R.string.country_virginIslandsUS, "+1340", "Virgin Islands (U.S.)"));
        this.mTianyiList.add(new TianyiDetail(3, R.string.continent_northAmerica, mCountryList));
        mCountryList = new ArrayList();
        mCountryList.add(new CountryDetail(0, R.string.country_australia, "+71", "Australia"));
        mCountryList.add(new CountryDetail(1, R.string.country_fiji, "+779", "Fiji"));
        mCountryList.add(new CountryDetail(2, R.string.country_frenchPolynesia, "+789", "French Polynesia"));
        mCountryList.add(new CountryDetail(3, R.string.country_guam, "+1671", "Guam"));
        mCountryList.add(new CountryDetail(4, R.string.country_newCaledonia, "+787", "New Caledonia"));
        mCountryList.add(new CountryDetail(5, R.string.country_newzealand, "+74", "New Zealand"));
        mCountryList.add(new CountryDetail(6, R.string.country_palau, "+780", "Palau"));
        mCountryList.add(new CountryDetail(7, R.string.country_papuaNewGuinea, "+775", "Papua New Guinea"));
        mCountryList.add(new CountryDetail(8, R.string.country_philippines, "+73", "Philippines"));
        mCountryList.add(new CountryDetail(9, R.string.country_samoa, "+785", "Samoa"));
        mCountryList.add(new CountryDetail(10, R.string.country_tonga, "+776", "Tonga"));
        this.mTianyiList.add(new TianyiDetail(4, R.string.continent_oceania, mCountryList));
        mCountryList = new ArrayList();
        mCountryList.add(new CountryDetail(0, R.string.country_argentina, "+54", "Argentina"));
        mCountryList.add(new CountryDetail(1, R.string.country_bolivia, "+591", "Bolivia"));
        mCountryList.add(new CountryDetail(2, R.string.country_brazil, "+55", "Brazil"));
        mCountryList.add(new CountryDetail(3, R.string.country_chile, "+56", "Chile"));
        mCountryList.add(new CountryDetail(4, R.string.country_columbia, "+57", "Columbia"));
        mCountryList.add(new CountryDetail(5, R.string.country_ecuadoe, "+593", "Ecuador"));
        mCountryList.add(new CountryDetail(6, R.string.country_frenchGuiana, "+594", "French Guiana"));
        mCountryList.add(new CountryDetail(7, R.string.country_guyana, "+592", "Guyana"));
        mCountryList.add(new CountryDetail(8, R.string.country_paraguay, "+595", "Paraguay"));
        mCountryList.add(new CountryDetail(9, R.string.country_peru, "+51", "Peru"));
        mCountryList.add(new CountryDetail(10, R.string.country_surinam, "+597", "Surinam"));
        mCountryList.add(new CountryDetail(11, R.string.country_uruguay, "+598", "Uruguay"));
        mCountryList.add(new CountryDetail(12, R.string.country_venezuela, "+58", "Venezuela"));
        this.mTianyiList.add(new TianyiDetail(5, R.string.continent_southAmerica, mCountryList));
    }

    public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
        if (this.mAdapter == null) {
            return false;
        }
        CountryDetail child = (CountryDetail) this.mAdapter.getChild(groupPosition, childPosition);
        if (child != null) {
            Intent intent = new Intent();
            intent.putExtra("countryName", child.countryKey);
            intent.putExtra("countryCode", child.countryCode);
            setResult(-1, intent);
            finish();
        }
        return true;
    }
}
