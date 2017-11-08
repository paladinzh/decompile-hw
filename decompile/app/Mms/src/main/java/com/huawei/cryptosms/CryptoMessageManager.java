package com.huawei.cryptosms;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.cryptosms.ICryptoMessageService.Stub;

public class CryptoMessageManager {
    private static ICryptoMessageService iCryptoMessageService = null;
    private ConnectCallBack mConnectCallBack;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            CryptoMessageManager.iCryptoMessageService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("CryptoMessageManager", "bindService success");
            CryptoMessageManager.iCryptoMessageService = Stub.asInterface(service);
            if (CryptoMessageManager.this.mConnectCallBack != null) {
                CryptoMessageManager.this.mConnectCallBack.connectSuccess();
            }
        }
    };
    public ICryptoMessageService mService = null;

    public interface ConnectCallBack {
        void connectSuccess();
    }

    public int registerCallback(ICryptoMessageClient client) {
        this.mService = getCryptoMessageService();
        if (this.mService == null) {
            return -1;
        }
        try {
            this.mService.registerCallback(client);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int unregisterCallback(ICryptoMessageClient client) {
        this.mService = getCryptoMessageService();
        if (this.mService == null) {
            return -1;
        }
        try {
            this.mService.unregisterCallback(client);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int activate(int subid, String accountName, String userid, String serviceToken) {
        this.mService = getCryptoMessageService();
        if (this.mService == null) {
            return 5;
        }
        try {
            return this.mService.activate(subid, accountName, userid, serviceToken);
        } catch (RemoteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int deactivate(int subid, String userID, String accountPassword) {
        this.mService = getCryptoMessageService();
        if (this.mService == null) {
            return 5;
        }
        try {
            return this.mService.deactivate(subid, userID, accountPassword);
        } catch (RemoteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int enrollMessage(byte[] text, int subid) {
        this.mService = getCryptoMessageService();
        if (this.mService == null) {
            return 5;
        }
        try {
            return this.mService.enrollMessage(text, subid);
        } catch (RemoteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int localDeactivate(int subid, String userID, String accountPassword) {
        this.mService = getCryptoMessageService();
        if (this.mService == null) {
            return 5;
        }
        try {
            return this.mService.localDeactivate(subid, userID, accountPassword);
        } catch (RemoteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getState(int subid) {
        this.mService = getCryptoMessageService();
        if (this.mService == null) {
            return 5;
        }
        try {
            return this.mService.getState(subid);
        } catch (RemoteException e) {
            e.printStackTrace();
            return 5;
        }
    }

    public String getCloudAccount(int subid) {
        this.mService = getCryptoMessageService();
        if (this.mService == null) {
            return "";
        }
        try {
            return this.mService.getCloudAccount(subid);
        } catch (RemoteException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String encryptData(String plainText) {
        this.mService = getCryptoMessageService();
        if (this.mService == null) {
            return "";
        }
        try {
            return this.mService.encryptData(plainText);
        } catch (RemoteException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String decryptData(String cipherText) {
        this.mService = getCryptoMessageService();
        if (this.mService == null) {
            return "";
        }
        try {
            return this.mService.decryptData(cipherText);
        } catch (RemoteException e) {
            e.printStackTrace();
            return "";
        }
    }

    public byte[] encryptMessage(byte[] msg, byte[] receivedNum, int subId) {
        this.mService = getCryptoMessageService();
        if (this.mService == null) {
            return new byte[0];
        }
        try {
            return this.mService.encryptMessage(msg, receivedNum, subId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public byte[] decryptMessage(byte[] msg, int subId) {
        this.mService = getCryptoMessageService();
        if (this.mService == null) {
            return new byte[0];
        }
        try {
            return this.mService.decryptMessage(msg, subId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public boolean linkToDeath(DeathRecipient deathRecipint) {
        if (deathRecipint != null) {
            this.mService = getCryptoMessageService();
            if (this.mService != null) {
                IBinder serviceBinder = this.mService.asBinder();
                if (serviceBinder != null) {
                    try {
                        serviceBinder.linkToDeath(deathRecipint, 0);
                        return true;
                    } catch (RemoteException e) {
                        Log.e("CryptoMessageManager", "get RemoteException ", e);
                        return false;
                    }
                }
                Log.e("CryptoMessageManager", "LinkToDeath asBinder is error");
                return false;
            }
            Log.e("CryptoMessageManager", "LinkToDeath Service is null");
            return false;
        }
        Log.e("CryptoMessageManager", "LinkToDeath DeathRecipint is null");
        return false;
    }

    public void unLinkToDeath(DeathRecipient deathRecipint) {
        if (deathRecipint != null) {
            IBinder serviceBinder = this.mService.asBinder();
            if (serviceBinder != null) {
                Log.e("CryptoMessageManager", "start unLinkToDeath method");
                serviceBinder.unlinkToDeath(deathRecipint, 0);
            }
        }
    }

    private static ICryptoMessageService getCryptoMessageService() {
        return iCryptoMessageService;
    }

    public void init(Context context) {
        Intent service = new Intent();
        service.setPackage("com.huawei.cryptosms.service");
        service.setComponent(new ComponentName("com.huawei.cryptosms.service", "com.huawei.cryptosms.service.CryptoService"));
        context.bindService(service, this.mConnection, 1);
    }

    public void setmService(ICryptoMessageService mService) {
        this.mService = mService;
    }

    public void setConnectCallBack(ConnectCallBack connectCallBack) {
        this.mConnectCallBack = connectCallBack;
    }
}
