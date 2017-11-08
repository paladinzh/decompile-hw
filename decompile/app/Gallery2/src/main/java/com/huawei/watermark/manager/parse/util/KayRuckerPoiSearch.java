package com.huawei.watermark.manager.parse.util;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.os.Handler;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.ServiceSettings;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.Query;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import com.huawei.watermark.decoratorclass.WMLog;
import com.huawei.watermark.manager.parse.util.IHWPoiSearch.OnPoiSearchCallback;
import com.huawei.watermark.wmutil.WMCollectionUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class KayRuckerPoiSearch implements IHWPoiSearch, OnPoiSearchListener {
    private static final String TAG = ("CAMERA3WATERMARK_" + KayRuckerPoiSearch.class.getSimpleName());
    private Context mContext;
    private int mCurrentPage = 0;
    private Query mCurrentQuery;
    private List<PoiItem> mFirstPoiItemsRes;
    private Query mFirstQuery;
    private Handler mHandler;
    private Location mLocation;
    private OnPoiSearchCallback mOnPoiSearchCallback;
    private PoiSearch mSearch;
    private List<PoiItem> mSecondPoiItemsRes;
    private Query mSecondQuery;

    public KayRuckerPoiSearch(Context mContext) {
        ServiceSettings.getInstance().setProtocol(2);
        this.mContext = mContext;
        this.mHandler = new Handler(mContext.getMainLooper());
    }

    public void poiSearch(Location loc, OnPoiSearchCallback mOnPoiSearchCallback) {
        this.mOnPoiSearchCallback = mOnPoiSearchCallback;
        this.mLocation = loc;
        this.mCurrentPage = 0;
        this.mFirstQuery = new Query("", "05|06|07|08|10|11|14|190600");
        this.mFirstQuery.setPageSize(20);
        this.mFirstQuery.setPageNum(this.mCurrentPage);
        this.mSecondQuery = new Query("", "09|13|16|17|18|190600");
        this.mSecondQuery.setPageSize(10);
        this.mSecondQuery.setPageNum(this.mCurrentPage);
        doFirstSearch();
    }

    private void doFirstSearch() {
        WMLog.i(TAG, "start doFirstSearch");
        doSearch(this.mFirstQuery);
    }

    private void doSecondSearch() {
        WMLog.i(TAG, "start doSecondSearch");
        doSearch(this.mSecondQuery);
    }

    private void doSearch(final Query query) {
        final LatLonPoint point = new LatLonPoint(this.mLocation.getLatitude(), this.mLocation.getLongitude());
        this.mHandler.post(new Runnable() {
            public void run() {
                WMLog.i(KayRuckerPoiSearch.TAG, "start search");
                KayRuckerPoiSearch.this.mCurrentPage = 0;
                if (KayRuckerPoiSearch.this.mContext != null) {
                    KayRuckerPoiSearch.this.mCurrentQuery = query;
                    KayRuckerPoiSearch.this.mSearch = new PoiSearch(KayRuckerPoiSearch.this.mContext, KayRuckerPoiSearch.this.mCurrentQuery);
                    KayRuckerPoiSearch.this.mSearch.setOnPoiSearchListener(KayRuckerPoiSearch.this);
                    KayRuckerPoiSearch.this.mSearch.setBound(new SearchBound(point, 500, true));
                    KayRuckerPoiSearch.this.mSearch.searchPOIAsyn();
                }
            }
        });
    }

    public void onPoiSearched(PoiResult result, int rCode) {
        if (rCode == 0 && result != null && result.getPois() != null) {
            WMLog.i(TAG, "result page number:" + result.getPageCount());
            WMLog.i(TAG, "size:" + result.getPois().size());
            Query query;
            int i;
            if (this.mCurrentQuery == this.mFirstQuery) {
                getFirstPointItemsRes(result);
                if (result.getPois().size() != 0 || result.getPageCount() - 1 <= this.mCurrentPage) {
                    doSecondSearch();
                } else {
                    query = this.mFirstQuery;
                    i = this.mCurrentPage + 1;
                    this.mCurrentPage = i;
                    query.setPageNum(i);
                    this.mSearch.searchPOIAsyn();
                }
            } else if (this.mCurrentQuery == this.mSecondQuery) {
                getSecondPoiItemsRes(result);
                if (result.getPageCount() - 1 > this.mCurrentPage) {
                    query = this.mSecondQuery;
                    i = this.mCurrentPage + 1;
                    this.mCurrentPage = i;
                    query.setPageNum(i);
                    this.mSearch.searchPOIAsyn();
                    return;
                }
                List<Address> mCurrentLocationItems = new ArrayList();
                if (!isEmptyCollection()) {
                    getCurrentLocationItems(mCurrentLocationItems);
                    if (this.mOnPoiSearchCallback != null) {
                        this.mOnPoiSearchCallback.onPoiSearched(mCurrentLocationItems);
                    }
                }
            }
        }
    }

    private void getCurrentLocationItems(List<Address> mCurrentLocationItems) {
        for (PoiItem poiItem : this.mFirstPoiItemsRes) {
            mCurrentLocationItems.add(poiItemToAddress(poiItem));
        }
        for (PoiItem poiItem2 : this.mSecondPoiItemsRes) {
            mCurrentLocationItems.add(poiItemToAddress(poiItem2));
        }
    }

    private boolean isEmptyCollection() {
        return WMCollectionUtil.isEmptyCollection(this.mFirstPoiItemsRes) ? WMCollectionUtil.isEmptyCollection(this.mSecondPoiItemsRes) : false;
    }

    private void getSecondPoiItemsRes(PoiResult result) {
        if (this.mSecondPoiItemsRes == null) {
            this.mSecondPoiItemsRes = result.getPois();
        } else {
            this.mSecondPoiItemsRes.addAll(result.getPois());
        }
    }

    private void getFirstPointItemsRes(PoiResult result) {
        if (this.mFirstPoiItemsRes == null) {
            this.mFirstPoiItemsRes = result.getPois();
        } else {
            this.mFirstPoiItemsRes.addAll(result.getPois());
        }
    }

    public void onPoiItemDetailSearched(PoiItemDetail poiItemDetail, int i) {
    }

    private Address poiItemToAddress(PoiItem poiItem) {
        if (poiItem == null) {
            return null;
        }
        Address address = new Address(Locale.getDefault());
        address.setLocality(poiItem.getTitle());
        address.setLongitude(poiItem.getLatLonPoint().getLongitude());
        address.setLatitude(poiItem.getLatLonPoint().getLatitude());
        return address;
    }
}
