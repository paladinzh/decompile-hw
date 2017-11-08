package com.amap.api.maps.model;

import android.os.RemoteException;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.Animation.AnimationListener;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.interfaces.IMarker;
import com.autonavi.amap.mapcore.interfaces.IMarkerAction;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.ArrayList;

public final class Marker {
    private IMarker a;

    public Marker(IMarker iMarker) {
        this.a = iMarker;
    }

    public void setPeriod(int i) {
        try {
            this.a.setPeriod(i);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public int getPeriod() {
        try {
            return this.a.getPeriod();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void setIcons(ArrayList<BitmapDescriptor> arrayList) {
        try {
            this.a.setIcons(arrayList);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public ArrayList<BitmapDescriptor> getIcons() {
        try {
            return this.a.getIcons();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void remove() {
        try {
            this.a.remove();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void destroy() {
        try {
            if (this.a != null) {
                this.a.destroy();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public String getId() {
        try {
            return this.a.getId();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void setPerspective(boolean z) {
        try {
            this.a.setPerspective(z);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public boolean isPerspective() {
        try {
            return this.a.isPerspective();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void setPosition(LatLng latLng) {
        try {
            this.a.setPosition(latLng);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public LatLng getPosition() {
        try {
            return this.a.getPosition();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void setTitle(String str) {
        try {
            this.a.setTitle(str);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public String getTitle() {
        try {
            return this.a.getTitle();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void setSnippet(String str) {
        try {
            this.a.setSnippet(str);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public String getSnippet() {
        try {
            return this.a.getSnippet();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void setIcon(BitmapDescriptor bitmapDescriptor) {
        if (bitmapDescriptor != null) {
            try {
                this.a.setIcon(bitmapDescriptor);
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }
    }

    public void setAnchor(float f, float f2) {
        try {
            this.a.setAnchor(f, f2);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void setDraggable(boolean z) {
        try {
            this.a.setDraggable(z);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public boolean isDraggable() {
        return this.a.isDraggable();
    }

    public void showInfoWindow() {
        try {
            this.a.showInfoWindow();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void hideInfoWindow() {
        try {
            this.a.hideInfoWindow();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public boolean isInfoWindowShown() {
        return this.a.isInfoWindowShown();
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
        try {
            if (obj instanceof Marker) {
                return this.a.equalsRemote(((Marker) obj).a);
            }
            return false;
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public int hashCode() {
        return this.a.hashCodeRemote();
    }

    public void setObject(Object obj) {
        this.a.setObject(obj);
    }

    public Object getObject() {
        return this.a.getObject();
    }

    public void setRotateAngle(float f) {
        try {
            this.a.setRotateAngle(f);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public float getRotateAngle() {
        return this.a.getRotateAngle();
    }

    public void setToTop() {
        try {
            this.a.set2Top();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void setGeoPoint(IPoint iPoint) {
        this.a.setGeoPoint(iPoint);
    }

    public IPoint getGeoPoint() {
        return this.a.getGeoPoint();
    }

    public void setFlat(boolean z) {
        try {
            this.a.setFlat(z);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public boolean isFlat() {
        return this.a.isFlat();
    }

    public void setPositionByPixels(int i, int i2) {
        this.a.setPositionByPixels(i, i2);
    }

    public void setZIndex(float f) {
        this.a.setZIndex(f);
    }

    public float getZIndex() {
        return this.a.getZIndex();
    }

    public void setAnimation(Animation animation) {
        try {
            this.a.setAnimation(animation);
        } catch (Throwable th) {
        }
    }

    public boolean startAnimation() {
        return this.a.startAnimation();
    }

    public void setAnimationListener(AnimationListener animationListener) {
        this.a.setAnimationListener(animationListener);
    }

    public float getAlpha() {
        IMarkerAction iMarkerAction = this.a.getIMarkerAction();
        if (iMarkerAction == null) {
            return WMElement.CAMERASIZEVALUE1B1;
        }
        return iMarkerAction.getAlpha();
    }

    public void setAlpha(float f) {
        IMarkerAction iMarkerAction = this.a.getIMarkerAction();
        if (iMarkerAction != null) {
            iMarkerAction.setAlpha(f);
        }
    }

    public int getDisplayLevel() {
        IMarkerAction iMarkerAction = this.a.getIMarkerAction();
        if (iMarkerAction == null) {
            return 5;
        }
        return iMarkerAction.getDisplayLevel();
    }

    public MarkerOptions getOptions() {
        IMarkerAction iMarkerAction = this.a.getIMarkerAction();
        if (iMarkerAction == null) {
            return null;
        }
        return iMarkerAction.getOptions();
    }

    public boolean isClickable() {
        IMarkerAction iMarkerAction = this.a.getIMarkerAction();
        if (iMarkerAction == null) {
            return false;
        }
        return iMarkerAction.isClickable();
    }

    public boolean isInfoWindowAutoOverturn() {
        IMarkerAction iMarkerAction = this.a.getIMarkerAction();
        if (iMarkerAction == null) {
            return false;
        }
        return iMarkerAction.isInfoWindowAutoOverturn();
    }

    public boolean isInfoWindowEnable() {
        IMarkerAction iMarkerAction = this.a.getIMarkerAction();
        if (iMarkerAction == null) {
            return false;
        }
        return iMarkerAction.isInfoWindowEnable();
    }

    public void setInfoWindowEnable(boolean z) {
        IMarkerAction iMarkerAction = this.a.getIMarkerAction();
        if (iMarkerAction != null) {
            iMarkerAction.setInfoWindowEnable(z);
        }
    }

    public void setMarkerOptions(MarkerOptions markerOptions) {
        IMarkerAction iMarkerAction = this.a.getIMarkerAction();
        if (iMarkerAction != null) {
            iMarkerAction.setMarkerOptions(markerOptions);
        }
    }

    public void setAutoOverturnInfoWindow(boolean z) {
        IMarkerAction iMarkerAction = this.a.getIMarkerAction();
        if (iMarkerAction != null) {
            iMarkerAction.setAutoOverturnInfoWindow(z);
        }
    }

    public void setClickable(boolean z) {
        IMarkerAction iMarkerAction = this.a.getIMarkerAction();
        if (iMarkerAction != null) {
            iMarkerAction.setClickable(z);
        }
    }

    public void setDisplayLevel(int i) {
        IMarkerAction iMarkerAction = this.a.getIMarkerAction();
        if (iMarkerAction != null) {
            iMarkerAction.setDisplayLevel(i);
        }
    }

    public void setFixingPointEnable(boolean z) {
        IMarkerAction iMarkerAction = this.a.getIMarkerAction();
        if (iMarkerAction != null) {
            iMarkerAction.setFixingPointEnable(z);
        }
    }

    public void setPositionNotUpdate(LatLng latLng) {
        setPosition(latLng);
    }

    public void setRotateAngleNotUpdate(float f) {
        IMarkerAction iMarkerAction = this.a.getIMarkerAction();
        if (iMarkerAction != null) {
            iMarkerAction.setRotateAngleNotUpdate(f);
        }
    }

    public void setBelowMaskLayer(boolean z) {
        this.a.setBelowMaskLayer(z);
    }

    public float getAnchorU() {
        return this.a.getAnchorU();
    }

    public float getAnchorV() {
        return this.a.getAnchorV();
    }
}
