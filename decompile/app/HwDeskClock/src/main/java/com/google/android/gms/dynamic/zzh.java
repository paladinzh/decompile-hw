package com.google.android.gms.dynamic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import com.google.android.gms.dynamic.zzc.zza;

/* compiled from: Unknown */
public final class zzh extends zza {
    private Fragment zzadi;

    private zzh(Fragment fragment) {
        this.zzadi = fragment;
    }

    public static zzh zza(Fragment fragment) {
        return fragment == null ? null : new zzh(fragment);
    }

    public Bundle getArguments() {
        return this.zzadi.getArguments();
    }

    public int getId() {
        return this.zzadi.getId();
    }

    public boolean getRetainInstance() {
        return this.zzadi.getRetainInstance();
    }

    public String getTag() {
        return this.zzadi.getTag();
    }

    public int getTargetRequestCode() {
        return this.zzadi.getTargetRequestCode();
    }

    public boolean getUserVisibleHint() {
        return this.zzadi.getUserVisibleHint();
    }

    public zzd getView() {
        return zze.zzx(this.zzadi.getView());
    }

    public boolean isAdded() {
        return this.zzadi.isAdded();
    }

    public boolean isDetached() {
        return this.zzadi.isDetached();
    }

    public boolean isHidden() {
        return this.zzadi.isHidden();
    }

    public boolean isInLayout() {
        return this.zzadi.isInLayout();
    }

    public boolean isRemoving() {
        return this.zzadi.isRemoving();
    }

    public boolean isResumed() {
        return this.zzadi.isResumed();
    }

    public boolean isVisible() {
        return this.zzadi.isVisible();
    }

    public void setHasOptionsMenu(boolean hasMenu) {
        this.zzadi.setHasOptionsMenu(hasMenu);
    }

    public void setMenuVisibility(boolean menuVisible) {
        this.zzadi.setMenuVisibility(menuVisible);
    }

    public void setRetainInstance(boolean retain) {
        this.zzadi.setRetainInstance(retain);
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        this.zzadi.setUserVisibleHint(isVisibleToUser);
    }

    public void startActivity(Intent intent) {
        this.zzadi.startActivity(intent);
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        this.zzadi.startActivityForResult(intent, requestCode);
    }

    public void zzn(zzd zzd) {
        this.zzadi.registerForContextMenu((View) zze.zzp(zzd));
    }

    public void zzo(zzd zzd) {
        this.zzadi.unregisterForContextMenu((View) zze.zzp(zzd));
    }

    public zzd zzrq() {
        return zze.zzx(this.zzadi.getActivity());
    }

    public zzc zzrr() {
        return zza(this.zzadi.getParentFragment());
    }

    public zzd zzrs() {
        return zze.zzx(this.zzadi.getResources());
    }

    public zzc zzrt() {
        return zza(this.zzadi.getTargetFragment());
    }
}
