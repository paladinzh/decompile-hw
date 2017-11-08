package tmsdk.common;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* compiled from: Unknown */
public class OfflineVideo implements Comparable<OfflineVideo> {
    public static final int PROGRESS_UNKNOWN = -1;
    public static final int STATUS_DL_UNCOMPLETED = 1;
    public static final int STATUS_READ_COMPLETED = 3;
    public static final int STATUS_READ_UNCOMPLETED = 2;
    public static final int STATUS_UNKNOWN = 0;
    public String mAdapter;
    public String mAppName;
    public int mDownProgress = -1;
    public String mPackage;
    public String mPath;
    public int mPlayProgress = -1;
    public String[] mPlayers;
    public boolean mSelected = false;
    public long mSize = -1;
    public boolean mThumbnailIsImage = false;
    public String mThumnbailPath;
    public String mTitle;

    public static void dumpToFile(List<OfflineVideo> list) {
        dumpToFile(list, getOfflineDatabase());
    }

    public static void dumpToFile(List<OfflineVideo> list, String str) {
        Exception e;
        Throwable th;
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        Object obj = 1;
        if (list != null) {
            Object obj2;
            Iterator it = list.iterator();
            while (true) {
                obj2 = obj;
                if (!it.hasNext()) {
                    break;
                }
                try {
                    ((OfflineVideo) it.next()).writeTo(dataOutputStream);
                    obj = obj2;
                } catch (IOException e2) {
                    e2.printStackTrace();
                    obj = null;
                }
                if (obj != null) {
                }
            }
            obj = obj2;
        }
        try {
            dataOutputStream.flush();
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        if (obj != null) {
            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(str);
                try {
                    fileOutputStream.write(byteArrayOutputStream.toByteArray());
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Exception e4) {
                            e4.printStackTrace();
                        }
                    }
                } catch (Exception e5) {
                    e4 = e5;
                    try {
                        e4.printStackTrace();
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (Exception e42) {
                                e42.printStackTrace();
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (Exception e32) {
                                e32.printStackTrace();
                            }
                        }
                        throw th;
                    }
                }
            } catch (Exception e6) {
                e42 = e6;
                fileOutputStream = null;
                e42.printStackTrace();
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = null;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        }
    }

    public static String getOfflineDatabase() {
        return TMSDKContext.getApplicaionContext().getApplicationInfo().dataDir + "/databases/offlinevideo.db";
    }

    public static OfflineVideo readFrom(DataInputStream dataInputStream) throws IOException {
        OfflineVideo offlineVideo = new OfflineVideo();
        offlineVideo.mPath = dataInputStream.readUTF();
        offlineVideo.mTitle = dataInputStream.readUTF();
        offlineVideo.mPackage = dataInputStream.readUTF();
        offlineVideo.mAppName = dataInputStream.readUTF();
        offlineVideo.mPlayers = dataInputStream.readUTF().split("&");
        offlineVideo.mThumnbailPath = dataInputStream.readUTF();
        offlineVideo.mSize = dataInputStream.readLong();
        offlineVideo.mDownProgress = dataInputStream.readInt();
        offlineVideo.mPlayProgress = dataInputStream.readInt();
        offlineVideo.mAdapter = dataInputStream.readUTF();
        return offlineVideo;
    }

    public static List<OfflineVideo> readOfflineVideos() {
        return readOfflineVideos(getOfflineDatabase());
    }

    public static List<OfflineVideo> readOfflineVideos(String str) {
        InputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(str);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fileInputStream = null;
        }
        if (fileInputStream == null) {
            return null;
        }
        List<OfflineVideo> arrayList = new ArrayList();
        DataInputStream dataInputStream = new DataInputStream(fileInputStream);
        while (dataInputStream.available() > 0) {
            try {
                OfflineVideo readFrom = readFrom(dataInputStream);
                if (readFrom != null) {
                    arrayList.add(readFrom);
                }
            } catch (Exception e2) {
            }
        }
        try {
            dataInputStream.close();
        } catch (Exception e3) {
        }
        try {
            fileInputStream.close();
        } catch (IOException e4) {
            e4.printStackTrace();
        }
        if (arrayList.size() == 0) {
            arrayList = null;
        }
        return arrayList;
    }

    public int compareTo(OfflineVideo offlineVideo) {
        long j = this.mSize - offlineVideo.mSize;
        return ((j > 0 ? 1 : (j == 0 ? 0 : -1)) <= 0 ? 1 : 0) == 0 ? -1 : j == 0 ? 0 : 1;
    }

    public int getStatus() {
        if (this.mDownProgress > 0 && this.mDownProgress < 95) {
            return 1;
        }
        if (this.mPlayProgress == -1) {
            return 0;
        }
        return this.mPlayProgress <= 85 ? 2 : 3;
    }

    public String getStatusDesc() {
        if (this.mDownProgress != -1 && this.mDownProgress < 95) {
            return "未下载完";
        }
        if (this.mPlayProgress == -1) {
            return null;
        }
        return this.mPlayProgress <= 85 ? "未看完" : "已播完";
    }

    public void writeTo(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(this.mPath != null ? this.mPath : "");
        dataOutputStream.writeUTF(this.mTitle != null ? this.mTitle : "");
        dataOutputStream.writeUTF(this.mPackage != null ? this.mPackage : "");
        dataOutputStream.writeUTF(this.mAppName != null ? this.mAppName : "");
        StringBuffer stringBuffer = new StringBuffer();
        if (this.mPlayers != null) {
            for (String append : this.mPlayers) {
                stringBuffer.append(append);
                stringBuffer.append("&");
            }
        }
        dataOutputStream.writeUTF(stringBuffer.toString());
        dataOutputStream.writeUTF(this.mThumnbailPath != null ? this.mThumnbailPath : "");
        dataOutputStream.writeLong(this.mSize);
        dataOutputStream.writeInt(this.mDownProgress);
        dataOutputStream.writeInt(this.mPlayProgress);
        dataOutputStream.writeUTF(this.mAdapter != null ? this.mAdapter : "");
    }
}
