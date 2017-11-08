package com.android.gallery3d.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.android.gallery3d.common.BlobCache;
import com.android.gallery3d.settings.GallerySettings;
import com.autonavi.amap.mapcore.VirtualEarthProjection;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ReverseGeocoder {
    private ConnectivityManager mConnectivityManager;
    private Context mContext;
    private BlobCache mGeoCache;
    private Geocoder mGeocoder = new Geocoder(this.mContext);

    public ReverseGeocoder(Context context) {
        this.mContext = context;
        this.mGeoCache = CacheManager.getCache(context, "rev_geocoding", 1000, 512000, 0);
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
    }

    public static long genLocationKey(double latitude, double longitude) {
        return (long) (((((latitude + 90.0d) * 2.0d) * 90.0d) + (VirtualEarthProjection.MaxLongitude + longitude)) * 6378137.0d);
    }

    public String getAddress(double lat, double lng) {
        Address addr = lookupAddress(lat, lng, true);
        if (addr == null) {
            return null;
        }
        String addrAdminArea = checkNull(addr.getAdminArea());
        String addrLocality = checkNull(addr.getLocality());
        String addrSubLocality = checkNull(addr.getSubLocality());
        String addressToReturn = null;
        if (!addrSubLocality.isEmpty()) {
            addressToReturn = addrSubLocality;
        }
        if (!addrLocality.isEmpty()) {
            if (addressToReturn == null) {
                addressToReturn = addrLocality;
            } else {
                addressToReturn = addressToReturn + "," + addrLocality;
            }
        }
        if (addressToReturn == null && !addrAdminArea.isEmpty()) {
            addressToReturn = addrAdminArea;
        }
        return addressToReturn;
    }

    private String checkNull(String locality) {
        if (locality == null) {
            return "";
        }
        if (locality.equals("null")) {
            return "";
        }
        return locality;
    }

    public Address lookupAddress(double latitude, double longitude, boolean useCache) {
        return lookupAddress(latitude, longitude, useCache, null);
    }

    public Address lookupAddress(double latitude, double longitude, boolean useCache, boolean[] noneInNetwork) {
        long locationKey = (long) (((((90.0d + latitude) * 2.0d) * 90.0d) + (VirtualEarthProjection.MaxLongitude + longitude)) * 6378137.0d);
        byte[] bArr = null;
        if (useCache) {
            try {
                if (this.mGeoCache != null) {
                    synchronized (this.mGeoCache) {
                        bArr = this.mGeoCache.lookup(locationKey);
                    }
                }
            } catch (IOException e) {
                GalleryLog.w("ReverseGeocoder", "lookupAddress IOException:" + e);
                return null;
            } catch (Exception e2) {
                GalleryLog.w("ReverseGeocoder", "lookupAddress Exception:" + e2);
                return null;
            }
        }
        Address address = null;
        NetworkInfo networkInfo = this.mConnectivityManager.getActiveNetworkInfo();
        Locale locale;
        int numAddressLines;
        int i;
        if (bArr != null && bArr.length != 0) {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bArr));
            GalleryLog.printDFXLog("ReverseGeocoder");
            String language = readUTF(dis);
            String country = readUTF(dis);
            String variant = readUTF(dis);
            locale = null;
            GalleryLog.printDFXLog("ReverseGeocoder");
            if (language != null) {
                Locale locale2;
                if (country == null) {
                    locale2 = new Locale(language);
                } else if (variant == null) {
                    locale2 = new Locale(language, country);
                } else {
                    locale2 = new Locale(language, country, variant);
                }
            }
            if (locale == null) {
                dis.close();
                return null;
            } else if (locale.getLanguage().equals(Locale.getDefault().getLanguage())) {
                address = new Address(locale);
                address.setThoroughfare(readUTF(dis));
                numAddressLines = dis.readInt();
                for (i = 0; i < numAddressLines; i++) {
                    address.setAddressLine(i, readUTF(dis));
                }
                address.setFeatureName(readUTF(dis));
                address.setLocality(readUTF(dis));
                address.setSubLocality(readUTF(dis));
                address.setAdminArea(readUTF(dis));
                address.setSubAdminArea(readUTF(dis));
                address.setCountryName(readUTF(dis));
                address.setCountryCode(readUTF(dis));
                address.setPostalCode(readUTF(dis));
                address.setPremises(readUTF(dis));
                address.setSubThoroughfare(readUTF(dis));
                dis.close();
            } else {
                dis.close();
                return lookupAddress(latitude, longitude, false);
            }
        } else if (networkInfo == null || !networkInfo.isConnected()) {
            return null;
        } else {
            boolean useNetwork = GallerySettings.getBoolean(this.mContext, GallerySettings.KEY_USE_NETWORK, false);
            if (GalleryUtils.IS_CHINESE_VERSION && !useNetwork) {
                return null;
            }
            List<Address> addresses = this.mGeocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                address = (Address) addresses.get(0);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(bos);
                locale = address.getLocale();
                writeUTF(dataOutputStream, locale.getLanguage());
                writeUTF(dataOutputStream, locale.getCountry());
                writeUTF(dataOutputStream, locale.getVariant());
                writeUTF(dataOutputStream, address.getThoroughfare());
                numAddressLines = address.getMaxAddressLineIndex();
                dataOutputStream.writeInt(numAddressLines);
                for (i = 0; i < numAddressLines; i++) {
                    writeUTF(dataOutputStream, address.getAddressLine(i));
                }
                writeUTF(dataOutputStream, address.getFeatureName());
                writeUTF(dataOutputStream, address.getLocality());
                writeUTF(dataOutputStream, address.getSubLocality());
                writeUTF(dataOutputStream, address.getAdminArea());
                writeUTF(dataOutputStream, address.getSubAdminArea());
                writeUTF(dataOutputStream, address.getCountryName());
                writeUTF(dataOutputStream, address.getCountryCode());
                writeUTF(dataOutputStream, address.getPostalCode());
                writeUTF(dataOutputStream, address.getPremises());
                writeUTF(dataOutputStream, address.getSubThoroughfare());
                dataOutputStream.flush();
                if (this.mGeoCache != null) {
                    synchronized (this.mGeoCache) {
                        this.mGeoCache.insert(locationKey, bos.toByteArray());
                    }
                }
                dataOutputStream.close();
            } else if (noneInNetwork != null) {
                if (noneInNetwork.length >= 1) {
                    noneInNetwork[0] = true;
                }
            }
        }
        return address;
    }

    public Address lookupAddressFromCache(double latitude, double longitude) {
        long locationKey = (long) (((((90.0d + latitude) * 2.0d) * 90.0d) + (VirtualEarthProjection.MaxLongitude + longitude)) * 6378137.0d);
        byte[] cachedLocation = null;
        try {
            if (this.mGeoCache != null) {
                synchronized (this.mGeoCache) {
                    cachedLocation = this.mGeoCache.lookup(locationKey);
                }
            }
            Address cachedAddress = null;
            if (cachedLocation != null && cachedLocation.length > 0) {
                DataInputStream cachedDis = new DataInputStream(new ByteArrayInputStream(cachedLocation));
                String language = readUTF(cachedDis);
                String country = readUTF(cachedDis);
                String variant = readUTF(cachedDis);
                Locale locale = null;
                if (language != null) {
                    if (country == null) {
                        GalleryLog.printDFXLog("ReverseGeocoder");
                        locale = new Locale(language);
                    } else if (variant == null) {
                        locale = new Locale(language, country);
                    } else {
                        locale = new Locale(language, country, variant);
                    }
                }
                if (locale == null || !locale.getLanguage().equals(Locale.getDefault().getLanguage())) {
                    cachedDis.close();
                    return null;
                }
                cachedAddress = new Address(locale);
                cachedAddress.setThoroughfare(readUTF(cachedDis));
                int numAddressLines = cachedDis.readInt();
                for (int i = 0; i < numAddressLines; i++) {
                    cachedAddress.setAddressLine(i, readUTF(cachedDis));
                }
                cachedAddress.setFeatureName(readUTF(cachedDis));
                cachedAddress.setLocality(readUTF(cachedDis));
                cachedAddress.setSubLocality(readUTF(cachedDis));
                cachedAddress.setAdminArea(readUTF(cachedDis));
                cachedAddress.setSubAdminArea(readUTF(cachedDis));
                cachedAddress.setCountryName(readUTF(cachedDis));
                cachedAddress.setCountryCode(readUTF(cachedDis));
                cachedAddress.setPostalCode(readUTF(cachedDis));
                cachedAddress.setPremises(readUTF(cachedDis));
                cachedAddress.setSubThoroughfare(readUTF(cachedDis));
                cachedDis.close();
            }
            return cachedAddress;
        } catch (IOException e) {
            return null;
        } catch (Exception e2) {
            return null;
        }
    }

    public static final void writeUTF(DataOutputStream dos, String string) throws IOException {
        if (string == null) {
            dos.writeUTF("");
        } else {
            dos.writeUTF(string);
        }
    }

    public static final String readUTF(DataInputStream dis) throws IOException {
        String retVal = dis.readUTF();
        if (retVal.length() == 0) {
            return null;
        }
        return retVal;
    }
}
