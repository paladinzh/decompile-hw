package com.android.gallery3d.ui;

import android.content.Context;
import android.location.Address;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReverseGeocoder;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class DetailsAddressResolver {
    private final GalleryContext mContext;
    private final Handler mHandler;
    private final GalleryAddressStack mQueryFromCacheStack = new GalleryAddressStack();
    private final GalleryAddressStack mQueryFromNetworkStack = new GalleryAddressStack();
    private ResolveAddressFromCacheTask mResolveAddressFromCacheTask;
    private ResolveAddressFromNetworkTask mResolveAddressFromNetworkTask;

    public interface AddressResolvingListener {
        void onAddressAvailable(String str);
    }

    private static class GalleryAddress {
        public volatile boolean mCancel = false;
        public double[] mLatlng;
        public AddressResolvingListener mListener;
        public boolean mNeedResponseForUI;
        public GalleryAddress mNext;
        public boolean mResolveAllInfo;

        public GalleryAddress(double[] latlng, AddressResolvingListener listener, boolean resolveAllInfo, boolean needResponseForUI) {
            this.mLatlng = latlng;
            this.mListener = listener;
            this.mResolveAllInfo = resolveAllInfo;
            this.mNeedResponseForUI = needResponseForUI;
        }
    }

    private static class GalleryAddressStack {
        private GalleryAddress mCurrent;
        private GalleryAddress mHead;

        private GalleryAddressStack() {
        }

        public GalleryAddress pop() {
            GalleryAddress address = this.mHead;
            if (address != null) {
                this.mHead = address.mNext;
            }
            this.mCurrent = address;
            return address;
        }

        public void push(GalleryAddress address) {
            address.mNext = this.mHead;
            this.mHead = address;
        }

        public void clean() {
            this.mHead = null;
        }

        public void cancel(AddressResolvingListener listener) {
            boolean z;
            if (this.mCurrent != null) {
                GalleryAddress galleryAddress = this.mCurrent;
                if (this.mCurrent.mListener == listener) {
                    z = true;
                } else {
                    z = false;
                }
                galleryAddress.mCancel = z;
            }
            for (GalleryAddress address = this.mHead; address != null; address = address.mNext) {
                if (address.mListener == listener) {
                    z = true;
                } else {
                    z = false;
                }
                address.mCancel = z;
            }
        }

        public void cancelAll() {
            if (this.mCurrent != null) {
                this.mCurrent.mCancel = true;
            }
            for (GalleryAddress address = this.mHead; address != null; address = address.mNext) {
                address.mCancel = true;
            }
        }
    }

    private class ResolveAddressFromCacheTask extends Thread {
        private volatile boolean mActive;

        private ResolveAddressFromCacheTask() {
            this.mActive = true;
        }

        public void run() {
            Process.setThreadPriority(10);
            setName("ResolveAddressFromCacheTask");
            while (this.mActive) {
                synchronized (DetailsAddressResolver.this) {
                    GalleryAddress address = DetailsAddressResolver.this.mQueryFromCacheStack.pop();
                    if (address == null) {
                        Utils.waitWithoutInterrupt(DetailsAddressResolver.this);
                    }
                }
                if (!(address == null || DetailsAddressResolver.this.resolveAddressFromCache(address))) {
                    DetailsAddressResolver.this.queryFromNetWork(address);
                }
            }
        }

        public void terminate() {
            this.mActive = false;
        }
    }

    private class ResolveAddressFromNetworkTask extends Thread {
        private volatile boolean mActive;

        private ResolveAddressFromNetworkTask() {
            this.mActive = true;
        }

        public void run() {
            Process.setThreadPriority(10);
            setName("ResolveAddressFromNetworkTask");
            while (this.mActive) {
                synchronized (DetailsAddressResolver.this) {
                    GalleryAddress address = DetailsAddressResolver.this.mQueryFromNetworkStack.pop();
                    if (address == null) {
                        Utils.waitWithoutInterrupt(DetailsAddressResolver.this);
                    }
                }
                if (address != null) {
                    DetailsAddressResolver.this.resolveAddressFromNetwork(address);
                }
            }
        }

        public void terminate() {
            this.mActive = false;
        }
    }

    public DetailsAddressResolver(GalleryContext context) {
        this.mContext = context;
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    private void resume() {
        this.mResolveAddressFromCacheTask = new ResolveAddressFromCacheTask();
        this.mResolveAddressFromNetworkTask = new ResolveAddressFromNetworkTask();
        this.mResolveAddressFromCacheTask.start();
        this.mResolveAddressFromNetworkTask.start();
    }

    @SuppressWarnings({"NN_NAKED_NOTIFY"})
    public void pause() {
        synchronized (this) {
            this.mQueryFromCacheStack.cancelAll();
            this.mQueryFromNetworkStack.cancelAll();
            this.mQueryFromCacheStack.clean();
            this.mQueryFromNetworkStack.clean();
        }
        if (this.mResolveAddressFromCacheTask != null) {
            this.mResolveAddressFromCacheTask.terminate();
            this.mResolveAddressFromCacheTask = null;
        }
        if (this.mResolveAddressFromNetworkTask != null) {
            this.mResolveAddressFromNetworkTask.terminate();
            this.mResolveAddressFromNetworkTask = null;
        }
        synchronized (this) {
            notifyAll();
        }
    }

    public String resolveAddress(double[] latlng, AddressResolvingListener listener, boolean resolveAllInfo, boolean needResponseForUI) {
        if (this.mResolveAddressFromCacheTask == null) {
            resume();
        }
        queryFromCache(new GalleryAddress(latlng, listener, resolveAllInfo, needResponseForUI));
        return GalleryUtils.formatLatitudeLongitude("(%f,%f)", latlng[0], latlng[1]);
    }

    private synchronized void queryFromCache(GalleryAddress address) {
        this.mQueryFromCacheStack.push(address);
        notifyAll();
    }

    private synchronized void queryFromNetWork(GalleryAddress address) {
        this.mQueryFromNetworkStack.push(address);
        notifyAll();
    }

    private boolean resolveAddressFromCache(GalleryAddress address) {
        Address resolvedAddress = new ReverseGeocoder(this.mContext.getAndroidContext()).lookupAddressFromCache(address.mLatlng[0], address.mLatlng[1]);
        if (resolvedAddress == null) {
            return false;
        }
        resolveAddressDone(address, resolvedAddress);
        return true;
    }

    public static String queryAddressFromCache(Context context, double[] latlng) {
        Address resolvedAddress = new ReverseGeocoder(context).lookupAddressFromCache(latlng[0], latlng[1]);
        if (resolvedAddress == null) {
            return null;
        }
        return getAddressText(resolveAllInfo(resolvedAddress));
    }

    private void resolveAddressFromNetwork(GalleryAddress address) {
        Address resolvedAddress = null;
        try {
            if (!address.mCancel) {
                resolveAddressDone(address, new ReverseGeocoder(this.mContext.getAndroidContext()).lookupAddress(address.mLatlng[0], address.mLatlng[1], false));
            }
        } finally {
            resolveAddressDone(address, resolvedAddress);
        }
    }

    private void resolveAddressDone(final GalleryAddress address, final Address resolvedAddress) {
        if (address.mCancel) {
            if (address.mNeedResponseForUI) {
                address.mListener.onAddressAvailable(null);
            }
            return;
        }
        if (address.mNeedResponseForUI) {
            updateLocation(resolvedAddress, address, true);
        } else {
            this.mHandler.post(new Runnable() {
                public void run() {
                    DetailsAddressResolver.this.updateLocation(resolvedAddress, address, false);
                }
            });
        }
    }

    private void updateLocation(Address resolvedAddress, GalleryAddress address, boolean isAlwaysCallBack) {
        if (resolvedAddress != null) {
            String[] parts;
            if (address.mResolveAllInfo) {
                parts = resolveAllInfo(resolvedAddress);
            } else {
                String showedAddress = null;
                StringBuilder stringBuilder = new StringBuilder();
                String format = this.mContext.getResources().getString(R.string.one_area_one_subarea);
                String adminArea = resolvedAddress.getAdminArea();
                String area = resolvedAddress.getLocality();
                String subArea = resolvedAddress.getSubLocality();
                if (!isStrEmpty(subArea) && !isStrEmpty(area)) {
                    stringBuilder.append(String.format(format, new Object[]{area, subArea}));
                } else if (isStrEmpty(subArea) && !isStrEmpty(area) && !isStrEmpty(adminArea)) {
                    stringBuilder.append(String.format(format, new Object[]{adminArea, area}));
                } else if (!isStrEmpty(subArea) && isStrEmpty(area) && !isStrEmpty(adminArea)) {
                    stringBuilder.append(String.format(format, new Object[]{adminArea, subArea}));
                } else if (!(isStrEmpty(subArea) && isStrEmpty(area) && isStrEmpty(adminArea))) {
                    showedAddress = !isStrEmpty(subArea) ? subArea : !isStrEmpty(area) ? area : adminArea;
                }
                if (stringBuilder.length() != 0) {
                    showedAddress = stringBuilder.toString();
                    showedAddress = showedAddress.substring(0, showedAddress.length() - 1);
                }
                parts = new String[]{showedAddress};
            }
            address.mListener.onAddressAvailable(getAddressText(parts));
        } else if (isAlwaysCallBack) {
            address.mListener.onAddressAvailable(null);
        }
    }

    private static String[] resolveAllInfo(Address resolvedAddress) {
        String[] parts = new String[]{resolvedAddress.getAdminArea(), resolvedAddress.getSubAdminArea(), resolvedAddress.getLocality(), resolvedAddress.getSubLocality(), resolvedAddress.getThoroughfare(), resolvedAddress.getSubThoroughfare(), resolvedAddress.getPostalCode(), resolvedAddress.getCountryName()};
        if (!(resolvedAddress.getAdminArea() == null || resolvedAddress.getLocality() == null || !resolvedAddress.getAdminArea().equals(resolvedAddress.getLocality()))) {
            parts[0] = null;
        }
        return parts;
    }

    private static String getAddressText(String[] parts) {
        StringBuilder addressText = new StringBuilder();
        int i = 0;
        while (i < parts.length) {
            if (!(parts[i] == null || parts[i].isEmpty())) {
                if (addressText.length() != 0) {
                    addressText.append(", ");
                }
                addressText.append(parts[i]);
            }
            i++;
        }
        return addressText.toString();
    }

    private boolean isStrEmpty(String str) {
        if (str == null || str.equals("")) {
            return true;
        }
        return false;
    }

    public void cancel(AddressResolvingListener listener) {
        synchronized (this) {
            this.mQueryFromCacheStack.cancel(listener);
            this.mQueryFromNetworkStack.cancel(listener);
        }
    }
}
