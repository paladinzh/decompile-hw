package com.android.contacts.util;

import android.content.ContentProviderResult;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.hap.utils.BackgroundGenricHandler;
import com.autonavi.amap.mapcore.MapCore;
import java.util.LinkedList;

public class ExceptionCapture {
    private static boolean isOriginalAvailable = false;
    private static boolean sCheckInsertSimContacts = false;
    private static long sInsertSimContactsStartTime;
    private static final LinkedList<Integer> sTestSteps = new LinkedList();

    private static class CheckBatchResultThread implements Runnable {
        private String mMsg;
        private ContentProviderResult[] mResults;
        private int mScene;

        CheckBatchResultThread(ContentProviderResult[] results, int scene, String msg) {
            this.mResults = results;
            this.mScene = scene;
            this.mMsg = msg;
        }

        public void run() {
            int i = 0;
            if (this.mResults != null) {
                ContentProviderResult[] contentProviderResultArr = this.mResults;
                int length = contentProviderResultArr.length;
                while (i < length) {
                    ContentProviderResult result = contentProviderResultArr[i];
                    if (result.uri != null || result.count.intValue() > 0) {
                        i++;
                    } else {
                        switch (this.mScene) {
                            case 1701:
                                ExceptionCapture.captureContactSaveException(this.mMsg, null);
                                break;
                            case 1702:
                                ExceptionCapture.captureContactDeleteException(this.mMsg, null);
                                break;
                            case 1703:
                                ExceptionCapture.captureSimContactSaveException(this.mMsg, null);
                                break;
                            case 1704:
                                ExceptionCapture.captureSimContactDeleteException(this.mMsg, null);
                                break;
                        }
                    }
                }
            }
        }
    }

    public static void checkSimContactSaveResult(ContentProviderResult[] results, String msg) {
        BackgroundGenricHandler.getInstance().postDelayed(new CheckBatchResultThread(results, 1703, msg), 100);
    }

    public static void checkContactSaveResult(ContentProviderResult[] results, String msg) {
        BackgroundGenricHandler.getInstance().postDelayed(new CheckBatchResultThread(results, 1701, msg), 100);
    }

    public static void checkSimContactDeleteResult(ContentProviderResult[] results, String msg) {
        BackgroundGenricHandler.getInstance().postDelayed(new CheckBatchResultThread(results, 1704, msg), 100);
    }

    public static void checkContactDeleteResult(ContentProviderResult[] results, String msg) {
        BackgroundGenricHandler.getInstance().postDelayed(new CheckBatchResultThread(results, 1702, msg), 100);
    }

    public static void reportScene(int scene) {
        if (sTestSteps.size() >= 10) {
            sTestSteps.removeFirst();
        }
        sTestSteps.addLast(Integer.valueOf(scene));
    }

    private static String appendScene(String msg) {
        boolean flag = true;
        StringBuilder tmpSteps = new StringBuilder();
        if (msg != null) {
            tmpSteps.append(msg).append(HwCustPreloadContacts.EMPTY_STRING);
        } else {
            tmpSteps.append("");
        }
        for (Integer intValue : sTestSteps) {
            int step = intValue.intValue();
            if (!flag) {
                tmpSteps.append("-");
            }
            tmpSteps.append(step);
            flag = false;
        }
        return tmpSteps.toString();
    }

    public static void captureSimQueryException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 65, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1736);
        } else {
            RadarIMonitorUpload.captureSimQueryException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureInitSimFactoryException(String msg, int sub) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), null, 65, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1737);
        } else {
            RadarIMonitorUpload.captureInitSimFactoryException(msg, appendScene("reproduce:"), sub);
        }
    }

    public static void captureExportVcardException(String msg) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), null, 67, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1740);
        } else {
            RadarIMonitorUpload.captureExportVcardException(msg, appendScene("reproduce:"));
        }
    }

    public static void captureImportVcardException(String msg) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), null, 67, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1741);
        } else {
            RadarIMonitorUpload.captureImportVcardException(msg, appendScene("reproduce:"));
        }
    }

    public static void capturePhotoManagerException(String msg) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), null, 67, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1739);
        } else {
            RadarIMonitorUpload.capturePhotoManagerException(msg, appendScene("reproduce:"));
        }
    }

    public static void captureNfcHandleException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 67, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1742);
        } else {
            RadarIMonitorUpload.captureNfcHandleException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureNfcImportException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 67, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1743);
        } else {
            RadarIMonitorUpload.captureNfcImportException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureYellowPageException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 67, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1749);
        } else {
            RadarIMonitorUpload.captureYellowPageException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureBlacklistException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 67, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1750);
        } else {
            RadarIMonitorUpload.captureBlacklistException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureTonePlayerException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 67, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1751);
        } else {
            RadarIMonitorUpload.captureTonePlayerException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureReadMeidException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 67, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1752);
        } else {
            RadarIMonitorUpload.captureReadMeidException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureSimEmailInfoException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 67, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1753);
        } else {
            RadarIMonitorUpload.captureSimEmailInfoException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureReadDefSubException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 67, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1754);
        } else {
            RadarIMonitorUpload.captureReadDefSubException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureReadSoltIdException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 67, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1755);
        } else {
            RadarIMonitorUpload.captureReadSoltIdException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureNumLocationDBException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 67, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1756);
        } else {
            RadarIMonitorUpload.captureNumLocationDBException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureSystemSettingException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 67, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1757);
        } else {
            RadarIMonitorUpload.captureSystemSettingException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureAlphaEncodedException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 67, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1758);
        } else {
            RadarIMonitorUpload.captureAlphaEncodedException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureNLException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 67, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1760);
        } else {
            RadarIMonitorUpload.captureNLException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureWallPaperImgException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 67, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1761);
        } else {
            RadarIMonitorUpload.captureWallPaperImgException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureSimContactSaveException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 65, 100, 1703);
        } else {
            RadarIMonitorUpload.captureSimContactSaveException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureContactSaveException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 65, 100, 1701);
        } else {
            RadarIMonitorUpload.captureContactSaveException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureSimContactDeleteException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 65, 100, 1704);
        } else {
            RadarIMonitorUpload.captureSimContactDeleteException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureContactDeleteException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 65, 100, 1702);
        } else {
            RadarIMonitorUpload.captureContactDeleteException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureSimRecordException(String msg) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), null, 65, 100, 1709);
        } else {
            RadarIMonitorUpload.captureSimRecordException(msg, appendScene("reproduce:"));
        }
    }

    public static void markInsertSimContactsStart() {
        sInsertSimContactsStartTime = System.currentTimeMillis();
        sCheckInsertSimContacts = true;
    }

    public static void markInsertSimContactsComplete(String msg) {
        if (sCheckInsertSimContacts) {
            sCheckInsertSimContacts = false;
            long spentTime = System.currentTimeMillis() - sInsertSimContactsStartTime;
            if (3600000 > spentTime && spentTime > 150000) {
                HwLog.e("ExceptionCapture", "insert sim contacts to contacts2.db overtime, spentTime = " + spentTime + ", threshold = " + 150000);
                if (isOriginalAvailable) {
                    RadarHwExceptionUpload.uploadLogExt(appendScene(msg + spentTime), null, 67, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1711);
                } else {
                    RadarIMonitorUpload.markInsertSimContactsComplete(spentTime, appendScene("reproduce:"));
                }
            }
        }
    }

    public static void captureMatchDataListFilterException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 67, 100, 1715);
        } else {
            RadarIMonitorUpload.captureMatchDataListFilterException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureMultiCursorAddNullToCursorListException(String methodName, int cursorListSize) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(methodName), null, 67, 100, 1718);
        } else {
            RadarIMonitorUpload.captureMultiCursorAddNullToCursorListException(methodName, cursorListSize, appendScene("reproduce:"));
        }
    }

    public static void captureLoadContactsXmlException(String msg, Exception e) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), e, 67, 100, 1719);
        } else {
            RadarIMonitorUpload.captureLoadContactsXmlException(msg, appendScene("reproduce:"), e);
        }
    }

    public static void captureCopySimToDbException(String msg) {
        if (isOriginalAvailable) {
            RadarHwExceptionUpload.uploadLogExt(appendScene(msg), null, 65, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, 1762);
        } else {
            RadarIMonitorUpload.captureCopySimToDbException(msg, appendScene("reproduce:"));
        }
    }
}
