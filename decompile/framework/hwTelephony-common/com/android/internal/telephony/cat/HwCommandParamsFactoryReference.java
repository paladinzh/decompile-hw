package com.android.internal.telephony.cat;

import com.android.internal.telephony.cat.AbstractCommandParamsFactory.CommandParamsFactoryReference;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.List;

public class HwCommandParamsFactoryReference implements CommandParamsFactoryReference {
    private static CommandParamsFactoryUtils commandParamsFactoryUtils = ((CommandParamsFactoryUtils) EasyInvokeFactory.getInvokeUtils(CommandParamsFactoryUtils.class));
    private CommandParamsFactory mCommandParamsFactory;

    public HwCommandParamsFactoryReference(CommandParamsFactory commandParamsFactory) {
        CatLog.d(this, "construct HwCommandParamsFactoryReference ");
        this.mCommandParamsFactory = commandParamsFactory;
    }

    public boolean processLanguageNotification(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d(this, "processLanguageNotification");
        String Language = null;
        switch (cmdDet.commandQualifier) {
            case 1:
                CatLog.d(this, "commandQualifier 0x01");
                ComprehensionTlv ctlv = commandParamsFactoryUtils.searchForTag(this.mCommandParamsFactory, ComprehensionTlvTag.LANGUAGE, ctlvs);
                if (ctlv != null) {
                    Language = ValueParser.retrieveTextString(ctlv);
                    break;
                }
                throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
            default:
                CatLog.d(this, "commandQualifier 0x00");
                break;
        }
        commandParamsFactoryUtils.setCmdParams(this.mCommandParamsFactory, new HwCommandParams(cmdDet, Language));
        return false;
    }
}
