package com.google.android.gms.dynamic;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.android.gms.dynamic.zzc.zza;

@SuppressLint({"NewApi"})
/* compiled from: Unknown */
public final class zzb extends zza {
    private Fragment zzavH;

    private zzb(Fragment fragment) {
        this.zzavH = fragment;
    }

    public static zzb zza(Fragment fragment) {
        return fragment == null ? null : new zzb(fragment);
    }

    public Bundle getArguments() {
        return this.zzavH.getArguments();
    }

    public int getId() {
        return this.zzavH.getId();
    }

    public boolean getRetainInstance() {
        return this.zzavH.getRetainInstance();
    }

    public String getTag() {
        return this.zzavH.getTag();
    }

    public int getTargetRequestCode() {
        return this.zzavH.getTargetRequestCode();
    }

    public boolean getUserVisibleHint() {
        return this.zzavH.getUserVisibleHint();
    }

    public zzd getView() {
        return zze.zzC(this.zzavH.getView());
    }

    public boolean isAdded() {
        return this.zzavH.isAdded();
    }

    public boolean isDetached() {
        return this.zzavH.isDetached();
    }

    public boolean isHidden() {
        return this.zzavH.isHidden();
    }

    public boolean isInLayout() {
        return this.zzavH.isInLayout();
    }

    public boolean isRemoving() {
        return this.zzavH.isRemoving();
    }

    public boolean isResumed() {
        return this.zzavH.isResumed();
    }

    public boolean isVisible() {
        return this.zzavH.isVisible();
    }

    public void setHasOptionsMenu(boolean hasMenu) {
        this.zzavH.setHasOptionsMenu(hasMenu);
    }

    public void setMenuVisibility(boolean menuVisible) {
        this.zzavH.setMenuVisibility(menuVisible);
    }

    public void setRetainInstance(boolean retain) {
        this.zzavH.setRetainInstance(retain);
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        this.zzavH.setUserVisibleHint(isVisibleToUser);
    }

    public void startActivity(Intent intent) {
        this.zzavH.startActivity(intent);
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        this.zzavH.startActivityForResult(intent, requestCode);
    }

    public void zzn(zzd zzd) {
        this.zzavH.registerForContextMenu((View) zze.zzp(zzd));
    }

    public void zzo(zzd zzd) {
        this.zzavH.unregisterForContextMenu((View) zze.zzp(zzd));
    }

    public zzd zztV() {
        return zze.zzC(this.zzavH.getActivity());
    }

    public zzc zztW() {
        return zza(this.zzavH.getParentFragment());
    }

    public zzd zztX() {
        return zze.zzC(this.zzavH.getResources());
    }

    public zzc zztY() {
        return zza(this.zzavH.getTargetFragment());
    }
}
