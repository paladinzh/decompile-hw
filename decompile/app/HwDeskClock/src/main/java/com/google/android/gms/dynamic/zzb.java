package com.google.android.gms.dynamic;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.android.gms.dynamic.zzc.zza;

/* compiled from: Unknown */
public final class zzb extends zza {
    private Fragment zzanb;

    private zzb(Fragment fragment) {
        this.zzanb = fragment;
    }

    public static zzb zza(Fragment fragment) {
        return fragment == null ? null : new zzb(fragment);
    }

    public Bundle getArguments() {
        return this.zzanb.getArguments();
    }

    public int getId() {
        return this.zzanb.getId();
    }

    public boolean getRetainInstance() {
        return this.zzanb.getRetainInstance();
    }

    public String getTag() {
        return this.zzanb.getTag();
    }

    public int getTargetRequestCode() {
        return this.zzanb.getTargetRequestCode();
    }

    public boolean getUserVisibleHint() {
        return this.zzanb.getUserVisibleHint();
    }

    public zzd getView() {
        return zze.zzx(this.zzanb.getView());
    }

    public boolean isAdded() {
        return this.zzanb.isAdded();
    }

    public boolean isDetached() {
        return this.zzanb.isDetached();
    }

    public boolean isHidden() {
        return this.zzanb.isHidden();
    }

    public boolean isInLayout() {
        return this.zzanb.isInLayout();
    }

    public boolean isRemoving() {
        return this.zzanb.isRemoving();
    }

    public boolean isResumed() {
        return this.zzanb.isResumed();
    }

    public boolean isVisible() {
        return this.zzanb.isVisible();
    }

    public void setHasOptionsMenu(boolean hasMenu) {
        this.zzanb.setHasOptionsMenu(hasMenu);
    }

    public void setMenuVisibility(boolean menuVisible) {
        this.zzanb.setMenuVisibility(menuVisible);
    }

    public void setRetainInstance(boolean retain) {
        this.zzanb.setRetainInstance(retain);
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        this.zzanb.setUserVisibleHint(isVisibleToUser);
    }

    public void startActivity(Intent intent) {
        this.zzanb.startActivity(intent);
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        this.zzanb.startActivityForResult(intent, requestCode);
    }

    public void zzn(zzd zzd) {
        this.zzanb.registerForContextMenu((View) zze.zzp(zzd));
    }

    public void zzo(zzd zzd) {
        this.zzanb.unregisterForContextMenu((View) zze.zzp(zzd));
    }

    public zzd zzrq() {
        return zze.zzx(this.zzanb.getActivity());
    }

    public zzc zzrr() {
        return zza(this.zzanb.getParentFragment());
    }

    public zzd zzrs() {
        return zze.zzx(this.zzanb.getResources());
    }

    public zzc zzrt() {
        return zza(this.zzanb.getTargetFragment());
    }
}
