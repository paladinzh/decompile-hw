package com.google.android.gms.dynamic;

import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.deskclock.alarmclock.MetaballPath;
import com.android.deskclock.alarmclock.PortCallPanelView;

/* compiled from: Unknown */
public interface zzc extends IInterface {

    /* compiled from: Unknown */
    public static abstract class zza extends Binder implements zzc {
        public zza() {
            attachInterface(this, "com.google.android.gms.dynamic.IFragmentWrapper");
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Intent intent = null;
            boolean z = false;
            zzd zzrq;
            IBinder asBinder;
            int id;
            zzc zzrr;
            boolean retainInstance;
            int i;
            switch (code) {
                case 2:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    zzrq = zzrq();
                    reply.writeNoException();
                    if (zzrq != null) {
                        asBinder = zzrq.asBinder();
                    }
                    reply.writeStrongBinder(asBinder);
                    return true;
                case 3:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    Bundle arguments = getArguments();
                    reply.writeNoException();
                    if (arguments == null) {
                        reply.writeInt(0);
                    } else {
                        reply.writeInt(1);
                        arguments.writeToParcel(reply, 1);
                    }
                    return true;
                case MetaballPath.POINT_NUM /*4*/:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    id = getId();
                    reply.writeNoException();
                    reply.writeInt(id);
                    return true;
                case 5:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    zzrr = zzrr();
                    reply.writeNoException();
                    if (zzrr != null) {
                        asBinder = zzrr.asBinder();
                    }
                    reply.writeStrongBinder(asBinder);
                    return true;
                case 6:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    zzrq = zzrs();
                    reply.writeNoException();
                    if (zzrq != null) {
                        asBinder = zzrq.asBinder();
                    }
                    reply.writeStrongBinder(asBinder);
                    return true;
                case 7:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    retainInstance = getRetainInstance();
                    reply.writeNoException();
                    reply.writeInt(!retainInstance ? 0 : 1);
                    return true;
                case 8:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    String tag = getTag();
                    reply.writeNoException();
                    reply.writeString(tag);
                    return true;
                case 9:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    zzrr = zzrt();
                    reply.writeNoException();
                    if (zzrr != null) {
                        asBinder = zzrr.asBinder();
                    }
                    reply.writeStrongBinder(asBinder);
                    return true;
                case 10:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    id = getTargetRequestCode();
                    reply.writeNoException();
                    reply.writeInt(id);
                    return true;
                case 11:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    retainInstance = getUserVisibleHint();
                    reply.writeNoException();
                    if (retainInstance) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 12:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    zzrq = getView();
                    reply.writeNoException();
                    if (zzrq != null) {
                        asBinder = zzrq.asBinder();
                    }
                    reply.writeStrongBinder(asBinder);
                    return true;
                case 13:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    retainInstance = isAdded();
                    reply.writeNoException();
                    if (retainInstance) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 14:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    retainInstance = isDetached();
                    reply.writeNoException();
                    if (retainInstance) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 15:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    retainInstance = isHidden();
                    reply.writeNoException();
                    if (retainInstance) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 16:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    retainInstance = isInLayout();
                    reply.writeNoException();
                    if (retainInstance) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 17:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    retainInstance = isRemoving();
                    reply.writeNoException();
                    if (retainInstance) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 18:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    retainInstance = isResumed();
                    reply.writeNoException();
                    if (retainInstance) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 19:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    retainInstance = isVisible();
                    reply.writeNoException();
                    if (retainInstance) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 20:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    zzn(com.google.android.gms.dynamic.zzd.zza.zzbk(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 21:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    if (data.readInt() != 0) {
                        z = true;
                    }
                    setHasOptionsMenu(z);
                    reply.writeNoException();
                    return true;
                case 22:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    if (data.readInt() != 0) {
                        z = true;
                    }
                    setMenuVisibility(z);
                    reply.writeNoException();
                    return true;
                case 23:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    if (data.readInt() != 0) {
                        z = true;
                    }
                    setRetainInstance(z);
                    reply.writeNoException();
                    return true;
                case 24:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    if (data.readInt() != 0) {
                        z = true;
                    }
                    setUserVisibleHint(z);
                    reply.writeNoException();
                    return true;
                case PortCallPanelView.DEFAUT_RADIUS /*25*/:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    }
                    startActivity(intent);
                    reply.writeNoException();
                    return true;
                case 26:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    }
                    startActivityForResult(intent, data.readInt());
                    reply.writeNoException();
                    return true;
                case 27:
                    data.enforceInterface("com.google.android.gms.dynamic.IFragmentWrapper");
                    zzo(com.google.android.gms.dynamic.zzd.zza.zzbk(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.dynamic.IFragmentWrapper");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    Bundle getArguments() throws RemoteException;

    int getId() throws RemoteException;

    boolean getRetainInstance() throws RemoteException;

    String getTag() throws RemoteException;

    int getTargetRequestCode() throws RemoteException;

    boolean getUserVisibleHint() throws RemoteException;

    zzd getView() throws RemoteException;

    boolean isAdded() throws RemoteException;

    boolean isDetached() throws RemoteException;

    boolean isHidden() throws RemoteException;

    boolean isInLayout() throws RemoteException;

    boolean isRemoving() throws RemoteException;

    boolean isResumed() throws RemoteException;

    boolean isVisible() throws RemoteException;

    void setHasOptionsMenu(boolean z) throws RemoteException;

    void setMenuVisibility(boolean z) throws RemoteException;

    void setRetainInstance(boolean z) throws RemoteException;

    void setUserVisibleHint(boolean z) throws RemoteException;

    void startActivity(Intent intent) throws RemoteException;

    void startActivityForResult(Intent intent, int i) throws RemoteException;

    void zzn(zzd zzd) throws RemoteException;

    void zzo(zzd zzd) throws RemoteException;

    zzd zzrq() throws RemoteException;

    zzc zzrr() throws RemoteException;

    zzd zzrs() throws RemoteException;

    zzc zzrt() throws RemoteException;
}
