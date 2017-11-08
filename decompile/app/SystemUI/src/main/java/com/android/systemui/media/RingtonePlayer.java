package com.android.systemui.media;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.IAudioService;
import android.media.IRingtonePlayer;
import android.media.IRingtonePlayer.Stub;
import android.media.Ringtone;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import com.android.systemui.SystemUI;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class RingtonePlayer extends SystemUI {
    private final NotificationPlayer mAsyncPlayer = new NotificationPlayer("RingtonePlayer");
    private IAudioService mAudioService;
    private IRingtonePlayer mCallback = new Stub() {
        public void play(IBinder token, Uri uri, AudioAttributes aa, float volume, boolean looping) throws RemoteException {
            Client client;
            HwLog.i("RingtonePlayer", "play(token=" + token + ", uri=" + uri + ", uid=" + Binder.getCallingUid() + ")");
            synchronized (RingtonePlayer.this.mClients) {
                client = (Client) RingtonePlayer.this.mClients.get(token);
                if (client == null) {
                    HwLog.i("RingtonePlayer", "client == null");
                    UserHandle user = Binder.getCallingUserHandle();
                    if (Binder.getCallingUid() == 1000) {
                        user = new UserHandle(UserSwitchUtils.getCurrentUser());
                    }
                    client = new Client(token, uri, user, aa);
                    token.linkToDeath(client, 0);
                    RingtonePlayer.this.mClients.put(token, client);
                }
            }
            client.mRingtone.setLooping(looping);
            client.mRingtone.setVolume(volume);
            client.mRingtone.play();
        }

        public void stop(IBinder token) {
            Log.i("RingtonePlayer", "stop(token=" + token + ")");
            synchronized (RingtonePlayer.this.mClients) {
                Client client = (Client) RingtonePlayer.this.mClients.remove(token);
            }
            if (client != null) {
                client.mToken.unlinkToDeath(client, 0);
                client.mRingtone.stop();
            }
        }

        public boolean isPlaying(IBinder token) {
            Log.i("RingtonePlayer", "isPlaying(token=" + token + ")");
            synchronized (RingtonePlayer.this.mClients) {
                Client client = (Client) RingtonePlayer.this.mClients.get(token);
            }
            if (client != null) {
                return client.mRingtone.isPlaying();
            }
            return false;
        }

        public void setPlaybackProperties(IBinder token, float volume, boolean looping) {
            synchronized (RingtonePlayer.this.mClients) {
                Client client = (Client) RingtonePlayer.this.mClients.get(token);
            }
            if (client != null) {
                client.mRingtone.setVolume(volume);
                client.mRingtone.setLooping(looping);
            }
        }

        public void playAsync(Uri uri, UserHandle user, boolean looping, AudioAttributes aa) {
            Log.i("RingtonePlayer", "playAsync(uri=" + uri + ", user=" + user + ")");
            if (Binder.getCallingUid() != 1000) {
                throw new SecurityException("Async playback only available from system UID.");
            }
            if (UserHandle.ALL.equals(user)) {
                user = UserHandle.SYSTEM;
            }
            RingtonePlayer.this.mAsyncPlayer.play(RingtonePlayer.this.getContextForUser(user), uri, looping, aa);
        }

        public void stopAsync() {
            Log.i("RingtonePlayer", "stopAsync()");
            if (Binder.getCallingUid() != 1000) {
                throw new SecurityException("Async playback only available from system UID.");
            }
            RingtonePlayer.this.mAsyncPlayer.stop();
        }

        public String getTitle(Uri uri) {
            return Ringtone.getTitle(RingtonePlayer.this.getContextForUser(Binder.getCallingUserHandle()), uri, false, false);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public ParcelFileDescriptor openRingtone(Uri uri) {
            Throwable th = null;
            ContentResolver resolver = RingtonePlayer.this.getContextForUser(Binder.getCallingUserHandle()).getContentResolver();
            if (uri.toString().startsWith(Media.EXTERNAL_CONTENT_URI.toString())) {
                Cursor cursor = null;
                try {
                    cursor = resolver.query(uri, new String[]{"is_ringtone", "is_alarm", "is_notification"}, null, null, null);
                    if (cursor.moveToFirst()) {
                        if (cursor.getInt(0) == 0 && cursor.getInt(1) == 0) {
                        }
                        ParcelFileDescriptor openFileDescriptor = resolver.openFileDescriptor(uri, "r");
                        if (cursor != null) {
                            try {
                                cursor.close();
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        }
                        if (th == null) {
                            return openFileDescriptor;
                        }
                        throw th;
                    }
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Throwable th3) {
                            th = th3;
                        }
                    }
                    if (th != null) {
                        throw th;
                    }
                } catch (IOException e) {
                    throw new SecurityException(e);
                } catch (Throwable th4) {
                    Throwable th5 = th4;
                    Throwable th6 = null;
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Throwable th7) {
                            if (th6 == null) {
                                th6 = th7;
                            } else if (th6 != th7) {
                                th6.addSuppressed(th7);
                            }
                        }
                    }
                    if (th6 != null) {
                        throw th6;
                    }
                    throw th5;
                }
            }
            throw new SecurityException("Uri is not ringtone, alarm, or notification: " + uri);
        }
    };
    private final HashMap<IBinder, Client> mClients = new HashMap();

    private class Client implements DeathRecipient {
        private final Ringtone mRingtone;
        private final IBinder mToken;

        public Client(IBinder token, Uri uri, UserHandle user, AudioAttributes aa) {
            this.mToken = token;
            this.mRingtone = new Ringtone(RingtonePlayer.this.getContextForUser(user), false);
            this.mRingtone.setAudioAttributes(aa);
            this.mRingtone.setUri(uri);
        }

        public void binderDied() {
            Log.d("RingtonePlayer", "binderDied() token=" + this.mToken);
            synchronized (RingtonePlayer.this.mClients) {
                RingtonePlayer.this.mClients.remove(this.mToken);
            }
            this.mRingtone.stop();
        }
    }

    public void start() {
        this.mAsyncPlayer.setUsesWakeLock(this.mContext);
        this.mAudioService = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        try {
            this.mAudioService.setRingtonePlayer(this.mCallback);
        } catch (RemoteException e) {
            Log.e("RingtonePlayer", "Problem registering RingtonePlayer: " + e);
        }
    }

    private Context getContextForUser(UserHandle user) {
        try {
            return this.mContext.createPackageContextAsUser("android", 0, user);
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Clients:");
        synchronized (this.mClients) {
            for (Client client : this.mClients.values()) {
                pw.print("  mToken=");
                pw.print(client.mToken);
                pw.print(" mUri=");
                pw.println(client.mRingtone.getUri());
            }
        }
    }
}
