package com.android.contacts.util;

import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import com.android.contacts.hap.utils.BackgroundGenricHandler;

public class RadarIMonitorUpload {
    public static void captureContactSaveException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadContactSaveException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadContactSaveException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006001);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureContactDeleteException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadContactDeleteException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadContactDeleteException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006002);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureSimContactSaveException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadSimContactsException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadSimContactsException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006003);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureSimContactDeleteException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadSimContactDeleteException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadSimContactDeleteException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006004);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureSimRecordException(final String msg, final String reproduced) {
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadSimRecordException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadSimRecordException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006005);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void markInsertSimContactsComplete(final long spentTime, final String reproduced) {
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadInsertSimContactsComplete(spentTime, reproduced);
            }
        }, 100);
    }

    private static void uploadInsertSimContactsComplete(long spentime, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006006);
        if (eStream != null) {
            eStream.setParam((short) 0, "" + spentime).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureMatchDataListFilterException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadMatchDataListFilterException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadMatchDataListFilterException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006007);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureMultiCursorAddNullToCursorListException(final String methodName, int cursorListSize, final String reproduced) {
        log(" try to add null to cursor list the current cursor size" + cursorListSize, null);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadMultiCursorAddNullToCursorListException(methodName, reproduced);
            }
        }, 100);
    }

    private static void uploadMultiCursorAddNullToCursorListException(String methodname, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006008);
        if (eStream != null) {
            eStream.setParam((short) 0, methodname).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureLoadContactsXmlException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadLoadContactsXmlException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadLoadContactsXmlException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006009);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureSimQueryException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadSimQueryException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadSimQueryException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006013);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureInitSimFactoryException(final String msg, final String reproduced, final int sub) {
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadInitSimFactoryException(msg, reproduced, sub);
            }
        }, 100);
    }

    private static void uploadInitSimFactoryException(String msg, String reproduced, int sub) {
        EventStream eStream = IMonitor.openEventStream(907006014);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, sub).setParam((short) 2, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void capturePhotoManagerException(final String msg, final String reproduced) {
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadPhotoManagerException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadPhotoManagerException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006015);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureExportVcardException(final String msg, final String reproduced) {
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadExportVcardException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadExportVcardException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006016);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureImportVcardException(final String msg, final String reproduced) {
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadImportVcardException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadImportVcardException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006017);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureNfcHandleException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadNfcHandleException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadNfcHandleException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006018);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureNfcImportException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadNfcImportException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadNfcImportException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006019);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureYellowPageException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadYellowPageException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadYellowPageException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006020);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureBlacklistException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadBlacklistException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadBlacklistException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006021);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureTonePlayerException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadTonePlayerException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadTonePlayerException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006022);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureReadMeidException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadReadMeidException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadReadMeidException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006023);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureSimEmailInfoException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadSimEmailInfoException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadSimEmailInfoException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006024);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureReadDefSubException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadReadDefSubException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadReadDefSubException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006025);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureReadSoltIdException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadReadSoltIdException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadReadSoltIdException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006026);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureNumLocationDBException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadNumLocationDBException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadNumLocationDBException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006027);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureSystemSettingException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadSystemSettingException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadSystemSettingException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006028);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureAlphaEncodedException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadAlphaEncodedException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadAlphaEncodedException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006029);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureNLException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadNLException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadNLException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006031);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureWallPaperImgException(final String msg, final String reproduced, Exception e) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadWallPaperImgException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadWallPaperImgException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006032);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    public static void captureCopySimToDbException(final String msg, final String reproduced) {
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadCopySimToDbException(msg, reproduced);
            }
        }, 100);
    }

    private static void uploadCopySimToDbException(String msg, String reproduced) {
        EventStream eStream = IMonitor.openEventStream(907006033);
        if (eStream != null) {
            eStream.setParam((short) 0, msg).setParam((short) 1, reproduced);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }

    private static void log(String msg, Exception e) {
        if (e == null) {
            HwLog.e("RadarIMonitorUpload", msg);
        } else {
            HwLog.e("RadarIMonitorUpload", msg, e);
        }
    }

    public static void capturePbListToDetailException(final long contactId, final String accountType, final String msg) {
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarIMonitorUpload.uploadPbListToDetailException(contactId, accountType, msg);
            }
        }, 100);
    }

    private static void uploadPbListToDetailException(long contactId, String accountType, String msg) {
        EventStream eStream = IMonitor.openEventStream(907006037);
        if (eStream != null) {
            eStream.setParam((short) 0, contactId).setParam((short) 1, accountType).setParam((short) 2, msg);
            IMonitor.sendEvent(eStream);
        }
        IMonitor.closeEventStream(eStream);
    }
}
