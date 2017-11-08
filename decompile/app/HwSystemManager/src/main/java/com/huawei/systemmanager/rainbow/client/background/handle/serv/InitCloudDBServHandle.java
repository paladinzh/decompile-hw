package com.huawei.systemmanager.rainbow.client.background.handle.serv;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.rainbow.client.background.handle.IIntentHandler;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys;
import com.huawei.systemmanager.rainbow.client.helper.LocalSharedPrefrenceHelper;
import com.huawei.systemmanager.rainbow.client.parsexml.ConfigFileParser;
import com.huawei.systemmanager.rainbow.client.parsexml.ControlBlackAppListXmlParse;
import com.huawei.systemmanager.rainbow.client.parsexml.ControlWhiteAppListXmlParse;
import com.huawei.systemmanager.rainbow.client.parsexml.MessageSafeConfigFileParser;
import com.huawei.systemmanager.rainbow.client.parsexml.PermissionFeatureXmlParse;
import com.huawei.systemmanager.rainbow.client.parsexml.PermissionOuterTableParse;
import com.huawei.systemmanager.rainbow.client.parsexml.PhonenumXmlParse;
import com.huawei.systemmanager.rainbow.client.parsexml.PushappXmlParse;
import com.huawei.systemmanager.rainbow.client.parsexml.VersionMapXmlParse;
import com.huawei.systemmanager.rainbow.client.util.UpdateOuterTableUtil;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.BackgroundValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.CompetitorConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.MessageSafeConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NotificationConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.StartupConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.UnifiedPowerAppsConfigConfigFile;
import com.huawei.systemmanager.util.HwLog;

public class InitCloudDBServHandle implements IIntentHandler {
    private static final String TAG = "InitCloudDBServHandle";

    public void handleIntent(Context ctx, Intent intent) {
        initCloudXmlTask(ctx);
    }

    private void initCloudXmlTask(Context ctx) {
        LocalSharedPrefrenceHelper sharedService = new LocalSharedPrefrenceHelper(ctx);
        if (sharedService.getBoolean(CloudSpfKeys.CLOUD_XML_DATA_INITED, false)) {
            HwLog.d(TAG, "updateVersionInfo begin!");
            updateVersionInfo(ctx);
            initSimplePackageInfo(ctx, true);
            return;
        }
        parseCloudXml(ctx);
        sharedService.putBoolean(CloudSpfKeys.CLOUD_XML_DATA_INITED, true);
    }

    private void parseCloudXml(Context ctx) {
        initSimplePackageInfo(ctx, false);
        PermissionOuterTableParse.initPermissionOuterTable(ctx);
        PermissionFeatureXmlParse.initPermissionXml(ctx);
        ConfigFileParser.initConfigTable(ctx, NotificationConfigFile.XML_PATH, NotificationConfigFile.CONTENT_OUTERTABLE_URI);
        ConfigFileParser.initConfigTable(ctx, "cloud/config/unifiedPowerApps.xml", UnifiedPowerAppsConfigConfigFile.CONTENT_OUTERTABLE_URI);
        ConfigFileParser.initConfigTable(ctx, StartupConfigFile.XML_PATH, StartupConfigFile.CONTENT_OUTERTABLE_URI);
        ConfigFileParser.initConfigTable(ctx, CompetitorConfigFile.XML_PATH, CompetitorConfigFile.CONTENT_OUTERTABLE_URI);
        ConfigFileParser.initConfigTable(ctx, BackgroundValues.XML_PATH, BackgroundValues.CONTENT_OUTERTABLE_URI);
        MessageSafeConfigFileParser.initConfigTable(ctx, MessageSafeConfigFile.XML_PATH);
        UpdateOuterTableUtil.updateOuterTable(ctx);
        initMapXmlPackageInfo(ctx);
    }

    private void initSimplePackageInfo(Context ctx, boolean updateStatus) {
        new ControlBlackAppListXmlParse(ctx).putXmlInfoIntoDB(updateStatus);
        new ControlWhiteAppListXmlParse(ctx).putXmlInfoIntoDB(updateStatus);
        new PhonenumXmlParse(ctx).putXmlInfoIntoDB(updateStatus);
        new PushappXmlParse(ctx).putXmlInfoIntoDB(updateStatus);
    }

    private void initMapXmlPackageInfo(Context ctx) {
        new VersionMapXmlParse(ctx).putXmlInfoIntoDB();
    }

    private void updateVersionInfo(Context ctx) {
        new VersionMapXmlParse(ctx).updateFeatureVersionFlag();
    }
}
