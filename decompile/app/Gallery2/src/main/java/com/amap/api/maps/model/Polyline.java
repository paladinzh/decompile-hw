package com.amap.api.maps.model;

import android.os.RemoteException;
import com.autonavi.amap.mapcore.interfaces.IPolyline;
import java.util.List;

public class Polyline {
    private final IPolyline a;

    public Polyline(IPolyline iPolyline) {
        this.a = iPolyline;
    }

    public void remove() {
        try {
            this.a.remove();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public String getId() {
        try {
            return this.a.getId();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void setPoints(List<LatLng> list) {
        try {
            this.a.setPoints(list);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public List<LatLng> getPoints() {
        try {
            return this.a.getPoints();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void setGeodesic(boolean z) {
        try {
            if (this.a.isGeodesic() != z) {
                List points = getPoints();
                this.a.setGeodesic(z);
                setPoints(points);
            }
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public boolean isGeodesic() {
        return this.a.isGeodesic();
    }

    public void setDottedLine(boolean z) {
        this.a.setDottedLine(z);
    }

    public boolean isDottedLine() {
        return this.a.isDottedLine();
    }

    public void setWidth(float f) {
        try {
            this.a.setWidth(f);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public float getWidth() {
        try {
            return this.a.getWidth();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void setColor(int i) {
        try {
            this.a.setColor(i);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public int getColor() {
        try {
            return this.a.getColor();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void setZIndex(float f) {
        try {
            this.a.setZIndex(f);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public float getZIndex() {
        try {
            return this.a.getZIndex();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void setVisible(boolean z) {
        try {
            this.a.setVisible(z);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public boolean isVisible() {
        try {
            return this.a.isVisible();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Polyline)) {
            return false;
        }
        try {
            return this.a.equalsRemote(((Polyline) obj).a);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public int hashCode() {
        try {
            return this.a.hashCodeRemote();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public LatLng getNearestLatLng(LatLng latLng) {
        return this.a.getNearestLatLng(latLng);
    }

    public void setTransparency(float f) {
        this.a.setTransparency(f);
    }

    public void setAboveMaskLayer(boolean z) {
        this.a.setAboveMaskLayer(z);
    }

    public void setCustomTexture(BitmapDescriptor bitmapDescriptor) {
        this.a.setCustomTexture(bitmapDescriptor);
    }

    public void setOptions(PolylineOptions polylineOptions) {
        this.a.setOptions(polylineOptions);
    }

    public PolylineOptions getOptions() {
        return this.a.getOptions();
    }
}
