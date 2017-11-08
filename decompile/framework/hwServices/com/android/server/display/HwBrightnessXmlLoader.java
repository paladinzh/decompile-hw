package com.android.server.display;

import android.graphics.PointF;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.server.input.HwCircleAnimation;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class HwBrightnessXmlLoader {
    private static final boolean HWDEBUG;
    private static final boolean HWFLOW;
    private static final String TAG = "HwBrightnessXmlLoader";
    private static final String XML_EXT = ".xml";
    private static final String XML_NAME = "LABCConfig.xml";
    private static final String XML_NAME_NOEXT = "LABCConfig";
    private static Data mData = new Data();
    private static HwBrightnessXmlLoader mLoader;
    private final int mDeviceActualBrightnessLevel;

    public static class Data implements Cloneable {
        public boolean allowLabcUseProximity = false;
        public boolean animatedStepRoundEnabled = false;
        public boolean animationEqualRatioEnable = false;
        public float autoFastTimeFor255 = 0.5f;
        public int brighenDebounceTime = 1000;
        public int brighenDebounceTimeForSmallThr = 4000;
        public int brightTimeDelay = 1000;
        public boolean brightTimeDelayEnable = false;
        public float brightTimeDelayLuxThreshold = 30.0f;
        public float brightenDebounceTimeParaBig = 0.0f;
        public float brightenDeltaLuxPara = 0.0f;
        public float brightenGradualTime = HwCircleAnimation.SMALL_ALPHA;
        public int brightenThresholdFor255 = 1254;
        public List<PointF> brightenlinePoints = new ArrayList();
        public boolean brightnessCalibrationEnabled = false;
        public long coverModeBrightenResponseTime = 1000;
        public long coverModeDarkenResponseTime = 1000;
        public float coverModeFirstLux = 2210.0f;
        public int darkTimeDelay = 10000;
        public float darkTimeDelayBeta0 = 0.0f;
        public float darkTimeDelayBeta1 = HwCircleAnimation.SMALL_ALPHA;
        public float darkTimeDelayBeta2 = 0.333f;
        public boolean darkTimeDelayEnable = false;
        public float darkTimeDelayLuxThreshold = 50.0f;
        public int darkenCurrentFor255 = 1300;
        public int darkenDebounceTime = 4000;
        public int darkenDebounceTimeForSmallThr = 8000;
        public float darkenDebounceTimeParaBig = HwCircleAnimation.SMALL_ALPHA;
        public float darkenDeltaLuxPara = HwCircleAnimation.SMALL_ALPHA;
        public float darkenGradualTime = 3.0f;
        public float darkenGradualTimeMax = 3.0f;
        public float darkenGradualTimeMin = 0.0f;
        public int darkenTargetFor255 = 1254;
        public List<PointF> darkenlinePoints = new ArrayList();
        public List<PointF> defaultBrighnessLinePoints = new ArrayList();
        public float defaultBrightness = 100.0f;
        public float dimTime = 3.0f;
        public int inDoorThreshold = 4000;
        public boolean lastCloseScreenEnable = false;
        public int lightSensorRateMills = 300;
        public int manualBrighenDebounceTime = 3000;
        public List<PointF> manualBrightenlinePoints = new ArrayList();
        public int manualBrightnessMaxLimit = 255;
        public int manualBrightnessMinLimit = 4;
        public int manualDarkenDebounceTime = 3000;
        public List<PointF> manualDarkenlinePoints = new ArrayList();
        public float manualFastTimeFor255 = 0.5f;
        public boolean manualMode = false;
        public int outDoorThreshold = 8000;
        public int postMaxMinAvgFilterNoFilterNum = 6;
        public int postMaxMinAvgFilterNum = 5;
        public int postMeanFilterNoFilterNum = 4;
        public int postMeanFilterNum = 3;
        public int postMethodNum = 2;
        public int powerOnFastResponseLuxNum = 8;
        public int preMeanFilterNoFilterNum = 7;
        public int preMeanFilterNum = 3;
        public int preMethodNum = 0;
        public float preWeightedMeanFilterAlpha = 0.5f;
        public float preWeightedMeanFilterLuxTh = 12.0f;
        public int preWeightedMeanFilterMaxFuncLuxNum = 3;
        public int preWeightedMeanFilterNoFilterNum = 7;
        public int preWeightedMeanFilterNum = 3;
        public int proximityNegativeDebounceTime = 3000;
        public int proximityPositiveDebounceTime = CPUFeature.MSG_THERMAL_LIMITED;
        public float ratioForBrightnenSmallThr = HwCircleAnimation.SMALL_ALPHA;
        public float ratioForDarkenSmallThr = HwCircleAnimation.SMALL_ALPHA;
        public boolean reportValueWhenSensorOnChange = true;
        public float sceneAmbientLuxMaxWeight = 0.5f;
        public float sceneAmbientLuxMinWeight = 0.5f;
        public int sceneGapPoints = 29;
        public int sceneMaxPoints = 0;
        public int sceneMinPoints = 29;
        public float screenBrightnessMaxNit = 530.0f;
        public float screenBrightnessMinNit = 2.0f;
        public int stabilityConstant = 5;
        public int stabilityTime1 = 20;
        public int stabilityTime2 = 10;
        public boolean useVariableStep = false;

        protected Object clone() throws CloneNotSupportedException {
            Data newData = (Data) super.clone();
            newData.brightenlinePoints = cloneList(this.brightenlinePoints);
            newData.darkenlinePoints = cloneList(this.darkenlinePoints);
            newData.defaultBrighnessLinePoints = cloneList(this.defaultBrighnessLinePoints);
            newData.manualBrightenlinePoints = cloneList(this.manualBrightenlinePoints);
            newData.manualDarkenlinePoints = cloneList(this.manualDarkenlinePoints);
            return newData;
        }

        private List<PointF> cloneList(List<PointF> list) {
            if (list == null) {
                return null;
            }
            List<PointF> newList = new ArrayList();
            for (PointF point : list) {
                newList.add(new PointF(point.x, point.y));
            }
            return newList;
        }

        public void printData() {
            if (HwBrightnessXmlLoader.HWFLOW) {
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() lightSensorRateMills=" + this.lightSensorRateMills + ", brighenDebounceTime=" + this.brighenDebounceTime + ", darkenDebounceTime=" + this.darkenDebounceTime + ", brightenDebounceTimeParaBig=" + this.brightenDebounceTimeParaBig + ", darkenDebounceTimeParaBig=" + this.darkenDebounceTimeParaBig + ", brightenDeltaLuxPara=" + this.brightenDeltaLuxPara + ", darkenDeltaLuxPara=" + this.darkenDeltaLuxPara);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() stabilityConstant=" + this.stabilityConstant + ", stabilityTime1=" + this.stabilityTime1 + ", stabilityTime2=" + this.stabilityTime2 + ", brighenDebounceTimeForSmallThr=" + this.brighenDebounceTimeForSmallThr + ", darkenDebounceTimeForSmallThr=" + this.darkenDebounceTimeForSmallThr + ", ratioForBrightnenSmallThr=" + this.ratioForBrightnenSmallThr + ", ratioForDarkenSmallThr=" + this.ratioForDarkenSmallThr);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() darkTimeDelayEnable=" + this.darkTimeDelayEnable + ", darkTimeDelay=" + this.darkTimeDelay + ", darkTimeDelayLuxThreshold=" + this.darkTimeDelayLuxThreshold + ", coverModeFirstLux=" + this.coverModeFirstLux + ", lastCloseScreenEnable=" + this.lastCloseScreenEnable + ", coverModeBrightenResponseTime=" + this.coverModeBrightenResponseTime + ", coverModeDarkenResponseTime=" + this.coverModeDarkenResponseTime + ", postMaxMinAvgFilterNoFilterNum=" + this.postMaxMinAvgFilterNoFilterNum + ", postMaxMinAvgFilterNum=" + this.postMaxMinAvgFilterNum);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() brightTimeDelayEnable=" + this.brightTimeDelayEnable + ", brightTimeDelay=" + this.brightTimeDelay + ", brightTimeDelayLuxThreshold=" + this.brightTimeDelayLuxThreshold + ", preMethodNum=" + this.preMethodNum + ", preMeanFilterNoFilterNum=" + this.preMeanFilterNoFilterNum + ", preMeanFilterNum=" + this.preMeanFilterNum + ", postMethodNum=" + this.postMethodNum + ", postMeanFilterNoFilterNum=" + this.postMeanFilterNoFilterNum + ", postMeanFilterNum=" + this.postMeanFilterNum);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() preWeightedMeanFilterNoFilterNum=" + this.preWeightedMeanFilterNoFilterNum + ",preWeightedMeanFilterNum=" + this.preWeightedMeanFilterNum + ",preWeightedMeanFilterMaxFuncLuxNum=" + this.preWeightedMeanFilterMaxFuncLuxNum + ",preWeightedMeanFilterAlpha=" + this.preWeightedMeanFilterAlpha + ",preWeightedMeanFilterLuxTh=" + this.preWeightedMeanFilterLuxTh);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() darkTimeDelayBeta0=" + this.darkTimeDelayBeta0 + ",darkTimeDelayBeta1=" + this.darkTimeDelayBeta1 + ",darkTimeDelayBeta2=" + this.darkTimeDelayBeta2);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() powerOnFastResponseLuxNum=" + this.powerOnFastResponseLuxNum);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() sceneMaxPoints=" + this.sceneMaxPoints + ",sceneGapPoints=" + this.sceneGapPoints + ",sceneMinPoints=" + this.sceneMinPoints + ",sceneAmbientLuxMaxWeight=" + this.sceneAmbientLuxMaxWeight + ",sceneAmbientLuxMinWeight=" + this.sceneAmbientLuxMinWeight);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() animationEqualRatioEnable=" + this.animationEqualRatioEnable + ",screenBrightnessMinNit=" + this.screenBrightnessMinNit + ",screenBrightnessMaxNit=" + this.screenBrightnessMaxNit);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() brightenlinePoints=" + this.brightenlinePoints);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() darkenlinePoints=" + this.darkenlinePoints);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() defaultBrightness=" + this.defaultBrightness + ", brightnessCalibrationEnabled=" + this.brightnessCalibrationEnabled);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() defaultBrighnessLinePoints=" + this.defaultBrighnessLinePoints);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() brightenGradualTime=" + this.brightenGradualTime + ", darkenGradualTime=" + this.darkenGradualTime + ", brightenThresholdFor255=" + this.brightenThresholdFor255 + ", darkenTargetFor255=" + this.darkenTargetFor255 + ", darkenCurrentFor255=" + this.darkenCurrentFor255 + ", autoFastTimeFor255=" + this.autoFastTimeFor255 + ", manualFastTimeFor255=" + this.manualFastTimeFor255);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() dimTime=" + this.dimTime + ", useVariableStep=" + this.useVariableStep + ", darkenGradualTimeMax=" + this.darkenGradualTimeMax + ", darkenGradualTimeMin=" + this.darkenGradualTimeMin + ", animatedStepRoundEnabled=" + this.animatedStepRoundEnabled + ", reportValueWhenSensorOnChange=" + this.reportValueWhenSensorOnChange + ", allowLabcUseProximity=" + this.allowLabcUseProximity + ", proximityPositiveDebounceTime=" + this.proximityPositiveDebounceTime + ", proximityNegativeDebounceTime=" + this.proximityNegativeDebounceTime);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() manualMode=" + this.manualMode + ", manualBrightnessMaxLimit=" + this.manualBrightnessMaxLimit + ", manualBrightnessMinLimit=" + this.manualBrightnessMinLimit + ", outDoorThreshold=" + this.outDoorThreshold + ", inDoorThreshold=" + this.inDoorThreshold + ", manualBrighenDebounceTime=" + this.manualBrighenDebounceTime + ", manualDarkenDebounceTime=" + this.manualDarkenDebounceTime);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() manualBrightenlinePoints=" + this.manualBrightenlinePoints);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() manualDarkenlinePoints=" + this.manualDarkenlinePoints);
            }
        }

        public void loadDefaultConfig() {
            if (HwBrightnessXmlLoader.HWFLOW) {
                Slog.i(HwBrightnessXmlLoader.TAG, "loadDefaultConfig()");
            }
            this.lightSensorRateMills = 300;
            this.brighenDebounceTime = 1000;
            this.darkenDebounceTime = 4000;
            this.brightenDebounceTimeParaBig = 0.0f;
            this.darkenDebounceTimeParaBig = HwCircleAnimation.SMALL_ALPHA;
            this.brightenDeltaLuxPara = 0.0f;
            this.darkenDeltaLuxPara = HwCircleAnimation.SMALL_ALPHA;
            this.stabilityConstant = 5;
            this.stabilityTime1 = 20;
            this.stabilityTime2 = 10;
            this.brighenDebounceTimeForSmallThr = 4000;
            this.darkenDebounceTimeForSmallThr = 8000;
            this.ratioForBrightnenSmallThr = HwCircleAnimation.SMALL_ALPHA;
            this.ratioForDarkenSmallThr = HwCircleAnimation.SMALL_ALPHA;
            this.darkTimeDelayEnable = false;
            this.darkTimeDelay = 10000;
            this.darkTimeDelayLuxThreshold = 50.0f;
            this.coverModeFirstLux = 2210.0f;
            this.lastCloseScreenEnable = false;
            this.coverModeBrightenResponseTime = 1000;
            this.coverModeDarkenResponseTime = 1000;
            this.postMaxMinAvgFilterNoFilterNum = 6;
            this.postMaxMinAvgFilterNum = 5;
            this.brightTimeDelayEnable = false;
            this.brightTimeDelay = 1000;
            this.brightTimeDelayLuxThreshold = 30.0f;
            this.preMethodNum = 0;
            this.preMeanFilterNoFilterNum = 7;
            this.preMeanFilterNum = 3;
            this.postMethodNum = 2;
            this.postMeanFilterNoFilterNum = 4;
            this.postMeanFilterNum = 3;
            this.preWeightedMeanFilterNoFilterNum = 7;
            this.preWeightedMeanFilterNum = 3;
            this.preWeightedMeanFilterMaxFuncLuxNum = 3;
            this.preWeightedMeanFilterAlpha = 0.5f;
            this.preWeightedMeanFilterLuxTh = 12.0f;
            this.darkTimeDelayBeta0 = 0.0f;
            this.darkTimeDelayBeta1 = HwCircleAnimation.SMALL_ALPHA;
            this.darkTimeDelayBeta2 = 0.333f;
            this.powerOnFastResponseLuxNum = 8;
            this.sceneMaxPoints = 0;
            this.sceneGapPoints = 29;
            this.sceneMinPoints = 29;
            this.sceneAmbientLuxMaxWeight = 0.5f;
            this.sceneAmbientLuxMinWeight = 0.5f;
            this.animationEqualRatioEnable = false;
            this.screenBrightnessMinNit = 2.0f;
            this.screenBrightnessMaxNit = 530.0f;
            this.brightenlinePoints.clear();
            this.brightenlinePoints.add(new PointF(0.0f, 15.0f));
            this.brightenlinePoints.add(new PointF(2.0f, 15.0f));
            this.brightenlinePoints.add(new PointF(10.0f, 19.0f));
            this.brightenlinePoints.add(new PointF(20.0f, 219.0f));
            this.brightenlinePoints.add(new PointF(100.0f, 539.0f));
            this.brightenlinePoints.add(new PointF(1000.0f, 989.0f));
            this.brightenlinePoints.add(new PointF(40000.0f, 989.0f));
            this.darkenlinePoints.clear();
            this.darkenlinePoints.add(new PointF(0.0f, HwCircleAnimation.SMALL_ALPHA));
            this.darkenlinePoints.add(new PointF(HwCircleAnimation.SMALL_ALPHA, HwCircleAnimation.SMALL_ALPHA));
            this.darkenlinePoints.add(new PointF(20.0f, 20.0f));
            this.darkenlinePoints.add(new PointF(40.0f, 20.0f));
            this.darkenlinePoints.add(new PointF(100.0f, 80.0f));
            this.darkenlinePoints.add(new PointF(600.0f, 580.0f));
            this.darkenlinePoints.add(new PointF(1180.0f, 580.0f));
            this.darkenlinePoints.add(new PointF(1200.0f, 600.0f));
            this.darkenlinePoints.add(new PointF(1800.0f, 600.0f));
            this.darkenlinePoints.add(new PointF(40000.0f, 38800.0f));
            this.defaultBrightness = 100.0f;
            this.brightnessCalibrationEnabled = false;
            this.defaultBrighnessLinePoints.clear();
            this.defaultBrighnessLinePoints.add(new PointF(0.0f, 4.0f));
            this.defaultBrighnessLinePoints.add(new PointF(25.0f, 46.5f));
            this.defaultBrighnessLinePoints.add(new PointF(1995.0f, 140.7f));
            this.defaultBrighnessLinePoints.add(new PointF(4000.0f, 255.0f));
            this.defaultBrighnessLinePoints.add(new PointF(40000.0f, 255.0f));
            this.brightenGradualTime = HwCircleAnimation.SMALL_ALPHA;
            this.darkenGradualTime = 3.0f;
            this.brightenThresholdFor255 = 1254;
            this.darkenTargetFor255 = 1254;
            this.darkenCurrentFor255 = 1300;
            this.autoFastTimeFor255 = 0.5f;
            this.manualFastTimeFor255 = 0.5f;
            this.dimTime = 3.0f;
            this.useVariableStep = false;
            this.darkenGradualTimeMax = 3.0f;
            this.darkenGradualTimeMin = 0.0f;
            this.animatedStepRoundEnabled = false;
            this.reportValueWhenSensorOnChange = true;
            this.allowLabcUseProximity = false;
            this.proximityPositiveDebounceTime = CPUFeature.MSG_THERMAL_LIMITED;
            this.proximityNegativeDebounceTime = 3000;
            this.manualMode = false;
            this.manualBrightnessMaxLimit = 255;
            this.manualBrightnessMinLimit = 4;
            this.outDoorThreshold = 8000;
            this.inDoorThreshold = 4000;
            this.manualBrighenDebounceTime = 3000;
            this.manualDarkenDebounceTime = 3000;
            this.manualBrightenlinePoints.clear();
            this.manualBrightenlinePoints.add(new PointF(0.0f, 1000.0f));
            this.manualBrightenlinePoints.add(new PointF(1000.0f, 5000.0f));
            this.manualBrightenlinePoints.add(new PointF(40000.0f, 10000.0f));
            this.manualDarkenlinePoints.clear();
            this.manualDarkenlinePoints.add(new PointF(0.0f, HwCircleAnimation.SMALL_ALPHA));
            this.manualDarkenlinePoints.add(new PointF(500.0f, 10.0f));
            this.manualDarkenlinePoints.add(new PointF(1000.0f, 500.0f));
            this.manualDarkenlinePoints.add(new PointF(2000.0f, 1000.0f));
            this.manualDarkenlinePoints.add(new PointF(40000.0f, 30000.0f));
        }
    }

    private static class Element_AllowLabcUseProximity extends HwXmlElement {
        private Element_AllowLabcUseProximity() {
        }

        public String getName() {
            return "AllowLabcUseProximity";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.allowLabcUseProximity = HwXmlElement.string2Boolean(parser.nextText());
            return true;
        }
    }

    private static class Element_AnimatedStepRoundEnabled extends HwXmlElement {
        private Element_AnimatedStepRoundEnabled() {
        }

        public String getName() {
            return "AnimatedStepRoundEnabled";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.animatedStepRoundEnabled = HwXmlElement.string2Boolean(parser.nextText());
            return true;
        }
    }

    private static class Element_AnimationEqualRatioEnable extends HwXmlElement {
        private Element_AnimationEqualRatioEnable() {
        }

        public String getName() {
            return "AnimationEqualRatioEnable";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.animationEqualRatioEnable = HwXmlElement.string2Boolean(parser.nextText());
            return true;
        }
    }

    private static class Element_AutoFastTimeFor255 extends HwXmlElement {
        private Element_AutoFastTimeFor255() {
        }

        public String getName() {
            return "AutoFastTimeFor255";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.autoFastTimeFor255 = HwXmlElement.string2Float(parser.nextText());
            return true;
        }
    }

    private static class Element_BrighenDebounceTime extends HwXmlElement {
        private Element_BrighenDebounceTime() {
        }

        public String getName() {
            return "BrighenDebounceTime";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.brighenDebounceTime = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.brighenDebounceTime > 0;
        }
    }

    private static class Element_BrighenDebounceTimeForSmallThr extends HwXmlElement {
        private Element_BrighenDebounceTimeForSmallThr() {
        }

        public String getName() {
            return "BrighenDebounceTimeForSmallThr";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.brighenDebounceTimeForSmallThr = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.brighenDebounceTimeForSmallThr > 0;
        }
    }

    private static class Element_BrightTimeDelay extends HwXmlElement {
        private Element_BrightTimeDelay() {
        }

        public String getName() {
            return "BrightTimeDelay";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.brightTimeDelay = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_BrightTimeDelayEnable extends HwXmlElement {
        private Element_BrightTimeDelayEnable() {
        }

        public String getName() {
            return "BrightTimeDelayEnable";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.brightTimeDelayEnable = HwXmlElement.string2Boolean(parser.nextText());
            return true;
        }
    }

    private static class Element_BrightTimeDelayLuxThreshold extends HwXmlElement {
        private Element_BrightTimeDelayLuxThreshold() {
        }

        public String getName() {
            return "BrightTimeDelayLuxThreshold";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.brightTimeDelayLuxThreshold = HwXmlElement.string2Float(parser.nextText());
            return true;
        }
    }

    private static class Element_BrightenDebounceTimeParaBig extends HwXmlElement {
        private Element_BrightenDebounceTimeParaBig() {
        }

        public String getName() {
            return "BrightenDebounceTimeParaBig";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.brightenDebounceTimeParaBig = HwXmlElement.string2Float(parser.nextText());
            return true;
        }
    }

    private static class Element_BrightenDeltaLuxPara extends HwXmlElement {
        private Element_BrightenDeltaLuxPara() {
        }

        public String getName() {
            return "BrightenDeltaLuxPara";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.brightenDeltaLuxPara = HwXmlElement.string2Float(parser.nextText());
            return true;
        }
    }

    private static class Element_BrightenGradualTime extends HwXmlElement {
        private Element_BrightenGradualTime() {
        }

        public String getName() {
            return "BrightenGradualTime";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.brightenGradualTime = HwXmlElement.string2Float(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.brightenGradualTime > 0.0f;
        }
    }

    private static class Element_BrightenThresholdFor255 extends HwXmlElement {
        private Element_BrightenThresholdFor255() {
        }

        public String getName() {
            return "BrightenThresholdFor255";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.brightenThresholdFor255 = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_BrightenlinePoints extends HwXmlElement {
        private Element_BrightenlinePoints() {
        }

        public String getName() {
            return "BrightenlinePoints";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_BrightenlinePoints_Point extends HwXmlElement {
        private Element_BrightenlinePoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.brightenlinePoints = HwXmlElement.parsePointFList(parser, HwBrightnessXmlLoader.mData.brightenlinePoints);
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.brightenlinePoints);
        }
    }

    private static class Element_BrightnessCalibrationEnabled extends HwXmlElement {
        private Element_BrightnessCalibrationEnabled() {
        }

        public String getName() {
            return "BrightnessCalibrationEnabled";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.brightnessCalibrationEnabled = HwXmlElement.string2Boolean(parser.nextText());
            return true;
        }
    }

    private static class Element_CoverModeBrightenResponseTime extends HwXmlElement {
        private Element_CoverModeBrightenResponseTime() {
        }

        public String getName() {
            return "CoverModeBrightenResponseTime";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.coverModeBrightenResponseTime = HwXmlElement.string2Long(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.coverModeBrightenResponseTime >= 0;
        }
    }

    private static class Element_CoverModeDarkenResponseTime extends HwXmlElement {
        private Element_CoverModeDarkenResponseTime() {
        }

        public String getName() {
            return "CoverModeDarkenResponseTime";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.coverModeDarkenResponseTime = HwXmlElement.string2Long(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.coverModeDarkenResponseTime >= 0;
        }
    }

    private static class Element_CoverModeFirstLux extends HwXmlElement {
        private Element_CoverModeFirstLux() {
        }

        public String getName() {
            return "CoverModeFirstLux";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.coverModeFirstLux = HwXmlElement.string2Float(parser.nextText());
            return true;
        }
    }

    private static class Element_DarkTimeDelay extends HwXmlElement {
        private Element_DarkTimeDelay() {
        }

        public String getName() {
            return "DarkTimeDelay";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.darkTimeDelay = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_DarkTimeDelayBeta0 extends HwXmlElement {
        private Element_DarkTimeDelayBeta0() {
        }

        public String getName() {
            return "DarkTimeDelayBeta0";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.darkTimeDelayBeta0 = HwXmlElement.string2Float(parser.nextText());
            return true;
        }
    }

    private static class Element_DarkTimeDelayBeta1 extends HwXmlElement {
        private Element_DarkTimeDelayBeta1() {
        }

        public String getName() {
            return "DarkTimeDelayBeta1";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.darkTimeDelayBeta1 = HwXmlElement.string2Float(parser.nextText());
            return true;
        }
    }

    private static class Element_DarkTimeDelayBeta2 extends HwXmlElement {
        private Element_DarkTimeDelayBeta2() {
        }

        public String getName() {
            return "DarkTimeDelayBeta2";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.darkTimeDelayBeta2 = HwXmlElement.string2Float(parser.nextText());
            return true;
        }
    }

    private static class Element_DarkTimeDelayEnable extends HwXmlElement {
        private Element_DarkTimeDelayEnable() {
        }

        public String getName() {
            return "DarkTimeDelayEnable";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.darkTimeDelayEnable = HwXmlElement.string2Boolean(parser.nextText());
            return true;
        }
    }

    private static class Element_DarkTimeDelayLuxThreshold extends HwXmlElement {
        private Element_DarkTimeDelayLuxThreshold() {
        }

        public String getName() {
            return "DarkTimeDelayLuxThreshold";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.darkTimeDelayLuxThreshold = HwXmlElement.string2Float(parser.nextText());
            return true;
        }
    }

    private static class Element_DarkenCurrentFor255 extends HwXmlElement {
        private Element_DarkenCurrentFor255() {
        }

        public String getName() {
            return "DarkenCurrentFor255";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.darkenCurrentFor255 = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_DarkenDebounceTime extends HwXmlElement {
        private Element_DarkenDebounceTime() {
        }

        public String getName() {
            return "DarkenDebounceTime";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.darkenDebounceTime = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.darkenDebounceTime > 0;
        }
    }

    private static class Element_DarkenDebounceTimeForSmallThr extends HwXmlElement {
        private Element_DarkenDebounceTimeForSmallThr() {
        }

        public String getName() {
            return "DarkenDebounceTimeForSmallThr";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.darkenDebounceTimeForSmallThr = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.darkenDebounceTimeForSmallThr > 0;
        }
    }

    private static class Element_DarkenDebounceTimeParaBig extends HwXmlElement {
        private Element_DarkenDebounceTimeParaBig() {
        }

        public String getName() {
            return "DarkenDebounceTimeParaBig";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.darkenDebounceTimeParaBig = HwXmlElement.string2Float(parser.nextText());
            return true;
        }
    }

    private static class Element_DarkenDeltaLuxPara extends HwXmlElement {
        private Element_DarkenDeltaLuxPara() {
        }

        public String getName() {
            return "DarkenDeltaLuxPara";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.darkenDeltaLuxPara = HwXmlElement.string2Float(parser.nextText());
            return true;
        }
    }

    private static class Element_DarkenGradualTime extends HwXmlElement {
        private Element_DarkenGradualTime() {
        }

        public String getName() {
            return "DarkenGradualTime";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.darkenGradualTime = HwXmlElement.string2Float(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.darkenGradualTime > 0.0f;
        }
    }

    private static class Element_DarkenGradualTimeMax extends HwXmlElement {
        private Element_DarkenGradualTimeMax() {
        }

        public String getName() {
            return "DarkenGradualTimeMax";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.darkenGradualTimeMax = HwXmlElement.string2Float(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.darkenGradualTimeMax > 0.0f;
        }
    }

    private static class Element_DarkenGradualTimeMin extends HwXmlElement {
        private Element_DarkenGradualTimeMin() {
        }

        public String getName() {
            return "DarkenGradualTimeMin";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.darkenGradualTimeMin = HwXmlElement.string2Float(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.darkenGradualTimeMin >= 0.0f;
        }
    }

    private static class Element_DarkenTargetFor255 extends HwXmlElement {
        private Element_DarkenTargetFor255() {
        }

        public String getName() {
            return "DarkenTargetFor255";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.darkenTargetFor255 = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_DarkenlinePoints extends HwXmlElement {
        private Element_DarkenlinePoints() {
        }

        public String getName() {
            return "DarkenlinePoints";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_DarkenlinePoints_Point extends HwXmlElement {
        private Element_DarkenlinePoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.darkenlinePoints = HwXmlElement.parsePointFList(parser, HwBrightnessXmlLoader.mData.darkenlinePoints);
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.darkenlinePoints);
        }
    }

    private static class Element_DefaultBrightness extends HwXmlElement {
        private Element_DefaultBrightness() {
        }

        public String getName() {
            return "DefaultBrightness";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.defaultBrightness = HwXmlElement.string2Float(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.defaultBrightness > 0.0f;
        }
    }

    private static class Element_DefaultBrightnessPoints extends HwXmlElement {
        private Element_DefaultBrightnessPoints() {
        }

        public String getName() {
            return "DefaultBrightnessPoints";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_DefaultBrightnessPoints_Point extends HwXmlElement {
        private Element_DefaultBrightnessPoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.defaultBrighnessLinePoints = HwXmlElement.parsePointFList(parser, HwBrightnessXmlLoader.mData.defaultBrighnessLinePoints);
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.defaultBrighnessLinePoints);
        }
    }

    private static class Element_DimTime extends HwXmlElement {
        private Element_DimTime() {
        }

        public String getName() {
            return "DimTime";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.dimTime = HwXmlElement.string2Float(parser.nextText());
            return true;
        }
    }

    private static class Element_InDoorThreshold extends HwXmlElement {
        private Element_InDoorThreshold() {
        }

        public String getName() {
            return "InDoorThreshold";
        }

        protected boolean isOptional() {
            return !HwBrightnessXmlLoader.mData.manualMode;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.inDoorThreshold = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.inDoorThreshold <= HwBrightnessXmlLoader.mData.outDoorThreshold;
        }
    }

    private class Element_LABCConfig extends HwXmlElement {
        private boolean mParseStarted;

        private Element_LABCConfig() {
        }

        public String getName() {
            return HwBrightnessXmlLoader.XML_NAME_NOEXT;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (this.mParseStarted) {
                return false;
            }
            if (HwBrightnessXmlLoader.this.mDeviceActualBrightnessLevel == 0) {
                if (this.HWFLOW) {
                    Slog.i(this.TAG, "actualDeviceLevel = 0, load started");
                }
                this.mParseStarted = true;
                return true;
            }
            String deviceLevelString = parser.getAttributeValue(null, MemoryConstant.MEM_FILECACHE_ITEM_LEVEL);
            if (deviceLevelString == null || deviceLevelString.length() == 0) {
                if (this.HWFLOW) {
                    Slog.i(this.TAG, "actualDeviceLevel = " + HwBrightnessXmlLoader.this.mDeviceActualBrightnessLevel + ", but can't find level in XML, load start");
                }
                this.mParseStarted = true;
                return true;
            } else if (HwXmlElement.string2Int(deviceLevelString) != HwBrightnessXmlLoader.this.mDeviceActualBrightnessLevel) {
                return false;
            } else {
                if (this.HWFLOW) {
                    Slog.i(this.TAG, "actualDeviceLevel = " + HwBrightnessXmlLoader.this.mDeviceActualBrightnessLevel + ", find matched level in XML, load start");
                }
                this.mParseStarted = true;
                return true;
            }
        }
    }

    private static class Element_LastCloseScreenEnable extends HwXmlElement {
        private Element_LastCloseScreenEnable() {
        }

        public String getName() {
            return "LastCloseScreenEnable";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.lastCloseScreenEnable = HwXmlElement.string2Boolean(parser.nextText());
            return true;
        }
    }

    private static class Element_LightSensorRateMills extends HwXmlElement {
        private Element_LightSensorRateMills() {
        }

        public String getName() {
            return "lightSensorRateMills";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.lightSensorRateMills = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_ManualBrighenDebounceTime extends HwXmlElement {
        private Element_ManualBrighenDebounceTime() {
        }

        public String getName() {
            return "ManualBrighenDebounceTime";
        }

        protected boolean isOptional() {
            return !HwBrightnessXmlLoader.mData.manualMode;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.manualBrighenDebounceTime = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.manualBrighenDebounceTime >= 0;
        }
    }

    private static class Element_ManualBrightenLinePoints extends HwXmlElement {
        private Element_ManualBrightenLinePoints() {
        }

        public String getName() {
            return "ManualBrightenLinePoints";
        }

        protected boolean isOptional() {
            return !HwBrightnessXmlLoader.mData.manualMode;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_ManualBrightenLinePoints_Point extends HwXmlElement {
        private Element_ManualBrightenLinePoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.manualBrightenlinePoints = HwXmlElement.parsePointFList(parser, HwBrightnessXmlLoader.mData.manualBrightenlinePoints);
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.manualBrightenlinePoints);
        }
    }

    private static class Element_ManualBrightnessMaxLimit extends HwXmlElement {
        private Element_ManualBrightnessMaxLimit() {
        }

        public String getName() {
            return "ManualBrightnessMaxLimit";
        }

        protected boolean isOptional() {
            return !HwBrightnessXmlLoader.mData.manualMode;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.manualBrightnessMaxLimit = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.manualBrightnessMaxLimit > 0;
        }
    }

    private static class Element_ManualBrightnessMinLimit extends HwXmlElement {
        private Element_ManualBrightnessMinLimit() {
        }

        public String getName() {
            return "ManualBrightnessMinLimit";
        }

        protected boolean isOptional() {
            return !HwBrightnessXmlLoader.mData.manualMode;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.manualBrightnessMinLimit = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.manualBrightnessMinLimit <= 255;
        }
    }

    private static class Element_ManualDarkenDebounceTime extends HwXmlElement {
        private Element_ManualDarkenDebounceTime() {
        }

        public String getName() {
            return "ManualDarkenDebounceTime";
        }

        protected boolean isOptional() {
            return !HwBrightnessXmlLoader.mData.manualMode;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.manualDarkenDebounceTime = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.manualDarkenDebounceTime >= 0;
        }
    }

    private static class Element_ManualDarkenLinePoints extends HwXmlElement {
        private Element_ManualDarkenLinePoints() {
        }

        public String getName() {
            return "ManualDarkenLinePoints";
        }

        protected boolean isOptional() {
            return !HwBrightnessXmlLoader.mData.manualMode;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_ManualDarkenLinePoints_Point extends HwXmlElement {
        private Element_ManualDarkenLinePoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.manualDarkenlinePoints = HwXmlElement.parsePointFList(parser, HwBrightnessXmlLoader.mData.manualDarkenlinePoints);
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.manualDarkenlinePoints);
        }
    }

    private static class Element_ManualFastTimeFor255 extends HwXmlElement {
        private Element_ManualFastTimeFor255() {
        }

        public String getName() {
            return "ManualFastTimeFor255";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.manualFastTimeFor255 = HwXmlElement.string2Float(parser.nextText());
            return true;
        }
    }

    private static class Element_ManualMode extends HwXmlElement {
        private Element_ManualMode() {
        }

        public String getName() {
            return "ManualMode";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.manualMode = HwXmlElement.string2Int(parser.nextText()) == 1;
            return true;
        }
    }

    private static class Element_OutDoorThreshold extends HwXmlElement {
        private Element_OutDoorThreshold() {
        }

        public String getName() {
            return "OutDoorThreshold";
        }

        protected boolean isOptional() {
            return !HwBrightnessXmlLoader.mData.manualMode;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.outDoorThreshold = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_PostMaxMinAvgFilterNoFilterNum extends HwXmlElement {
        private Element_PostMaxMinAvgFilterNoFilterNum() {
        }

        public String getName() {
            return "PostMaxMinAvgFilterNoFilterNum";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.postMaxMinAvgFilterNoFilterNum = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_PostMaxMinAvgFilterNum extends HwXmlElement {
        private Element_PostMaxMinAvgFilterNum() {
        }

        public String getName() {
            return "PostMaxMinAvgFilterNum";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.postMaxMinAvgFilterNum = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.postMaxMinAvgFilterNum > 0 && HwBrightnessXmlLoader.mData.postMaxMinAvgFilterNum <= HwBrightnessXmlLoader.mData.postMaxMinAvgFilterNoFilterNum;
        }
    }

    private static class Element_PostMeanFilterNoFilterNum extends HwXmlElement {
        private Element_PostMeanFilterNoFilterNum() {
        }

        public String getName() {
            return "PostMeanFilterNoFilterNum";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.postMeanFilterNoFilterNum = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_PostMeanFilterNum extends HwXmlElement {
        private Element_PostMeanFilterNum() {
        }

        public String getName() {
            return "PostMeanFilterNum";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.postMeanFilterNum = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.postMeanFilterNum > 0 && HwBrightnessXmlLoader.mData.postMeanFilterNum <= HwBrightnessXmlLoader.mData.postMeanFilterNoFilterNum;
        }
    }

    private static class Element_PostMethodNum extends HwXmlElement {
        private Element_PostMethodNum() {
        }

        public String getName() {
            return "PostMethodNum";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.postMethodNum = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_PowerOnFastResponseLuxNum extends HwXmlElement {
        private Element_PowerOnFastResponseLuxNum() {
        }

        public String getName() {
            return "PowerOnFastResponseLuxNum";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.powerOnFastResponseLuxNum = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.powerOnFastResponseLuxNum > 0;
        }
    }

    private static class Element_PreMeanFilterNoFilterNum extends HwXmlElement {
        private Element_PreMeanFilterNoFilterNum() {
        }

        public String getName() {
            return "PreMeanFilterNoFilterNum";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.preMeanFilterNoFilterNum = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_PreMeanFilterNum extends HwXmlElement {
        private Element_PreMeanFilterNum() {
        }

        public String getName() {
            return "PreMeanFilterNum";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.preMeanFilterNum = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.preMeanFilterNum > 0 && HwBrightnessXmlLoader.mData.preMeanFilterNum <= HwBrightnessXmlLoader.mData.preMeanFilterNoFilterNum;
        }
    }

    private static class Element_PreMethodNum extends HwXmlElement {
        private Element_PreMethodNum() {
        }

        public String getName() {
            return "PreMethodNum";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.preMethodNum = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_PreProcessing extends HwXmlElement {
        private Element_PreProcessing() {
        }

        public String getName() {
            return "PreProcessing";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_PreWeightedMeanFilterAlpha extends HwXmlElement {
        private Element_PreWeightedMeanFilterAlpha() {
        }

        public String getName() {
            return "PreWeightedMeanFilterAlpha";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.preWeightedMeanFilterAlpha = HwXmlElement.string2Float(parser.nextText());
            return true;
        }
    }

    private static class Element_PreWeightedMeanFilterLuxTh extends HwXmlElement {
        private Element_PreWeightedMeanFilterLuxTh() {
        }

        public String getName() {
            return "PreWeightedMeanFilterLuxTh";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.preWeightedMeanFilterLuxTh = HwXmlElement.string2Float(parser.nextText());
            return true;
        }
    }

    private static class Element_PreWeightedMeanFilterMaxFuncLuxNum extends HwXmlElement {
        private Element_PreWeightedMeanFilterMaxFuncLuxNum() {
        }

        public String getName() {
            return "PreWeightedMeanFilterMaxFuncLuxNum";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.preWeightedMeanFilterMaxFuncLuxNum = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.preWeightedMeanFilterMaxFuncLuxNum > 0;
        }
    }

    private static class Element_PreWeightedMeanFilterNoFilterNum extends HwXmlElement {
        private Element_PreWeightedMeanFilterNoFilterNum() {
        }

        public String getName() {
            return "PreWeightedMeanFilterNoFilterNum";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.preWeightedMeanFilterNoFilterNum = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.preWeightedMeanFilterNoFilterNum > 0;
        }
    }

    private static class Element_PreWeightedMeanFilterNum extends HwXmlElement {
        private Element_PreWeightedMeanFilterNum() {
        }

        public String getName() {
            return "PreWeightedMeanFilterNum";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.preWeightedMeanFilterNum = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.preWeightedMeanFilterNum > 0 && HwBrightnessXmlLoader.mData.preWeightedMeanFilterNum <= HwBrightnessXmlLoader.mData.preWeightedMeanFilterNoFilterNum;
        }
    }

    private static class Element_Proximity extends HwXmlElement {
        private Element_Proximity() {
        }

        public String getName() {
            return "Proximity";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_ProximityNegativeDebounceTime extends HwXmlElement {
        private Element_ProximityNegativeDebounceTime() {
        }

        public String getName() {
            return "ProximityNegativeDebounceTime";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.proximityNegativeDebounceTime = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_ProximityPositiveDebounceTime extends HwXmlElement {
        private Element_ProximityPositiveDebounceTime() {
        }

        public String getName() {
            return "ProximityPositiveDebounceTime";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.proximityPositiveDebounceTime = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_RatioForBrightnenSmallThr extends HwXmlElement {
        private Element_RatioForBrightnenSmallThr() {
        }

        public String getName() {
            return "RatioForBrightnenSmallThr";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.ratioForBrightnenSmallThr = HwXmlElement.string2Float(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.ratioForBrightnenSmallThr > 0.0f;
        }
    }

    private static class Element_RatioForDarkenSmallThr extends HwXmlElement {
        private Element_RatioForDarkenSmallThr() {
        }

        public String getName() {
            return "RatioForDarkenSmallThr";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.ratioForDarkenSmallThr = HwXmlElement.string2Float(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.ratioForDarkenSmallThr > 0.0f;
        }
    }

    private static class Element_ReportValueWhenSensorOnChange extends HwXmlElement {
        private Element_ReportValueWhenSensorOnChange() {
        }

        public String getName() {
            return "ReportValueWhenSensorOnChange";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.reportValueWhenSensorOnChange = HwXmlElement.string2Boolean(parser.nextText());
            return true;
        }
    }

    private static class Element_SceneAmbientLuxMaxWeight extends HwXmlElement {
        private Element_SceneAmbientLuxMaxWeight() {
        }

        public String getName() {
            return "SceneAmbientLuxMaxWeight";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.sceneAmbientLuxMaxWeight = HwXmlElement.string2Float(parser.nextText());
            return true;
        }
    }

    private static class Element_SceneAmbientLuxMinWeight extends HwXmlElement {
        private Element_SceneAmbientLuxMinWeight() {
        }

        public String getName() {
            return "SceneAmbientLuxMinWeight";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.sceneAmbientLuxMinWeight = HwXmlElement.string2Float(parser.nextText());
            return true;
        }
    }

    private static class Element_SceneGapPoints extends HwXmlElement {
        private Element_SceneGapPoints() {
        }

        public String getName() {
            return "SceneGapPoints";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.sceneGapPoints = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_SceneMaxPoints extends HwXmlElement {
        private Element_SceneMaxPoints() {
        }

        public String getName() {
            return "SceneMaxPoints";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.sceneMaxPoints = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_SceneMinPoints extends HwXmlElement {
        private Element_SceneMinPoints() {
        }

        public String getName() {
            return "SceneMinPoints";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.sceneMinPoints = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_SceneProcessing extends HwXmlElement {
        private Element_SceneProcessing() {
        }

        public String getName() {
            return "SceneProcessing";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_ScreenBrightnessMaxNit extends HwXmlElement {
        private Element_ScreenBrightnessMaxNit() {
        }

        public String getName() {
            return "ScreenBrightnessMaxNit";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.screenBrightnessMaxNit = HwXmlElement.string2Float(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.screenBrightnessMaxNit > HwBrightnessXmlLoader.mData.screenBrightnessMinNit;
        }
    }

    private static class Element_ScreenBrightnessMinNit extends HwXmlElement {
        private Element_ScreenBrightnessMinNit() {
        }

        public String getName() {
            return "ScreenBrightnessMinNit";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.screenBrightnessMinNit = HwXmlElement.string2Float(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.screenBrightnessMinNit > 0.0f;
        }
    }

    private static class Element_StabilityConstant extends HwXmlElement {
        private Element_StabilityConstant() {
        }

        public String getName() {
            return "StabilityConstant";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.stabilityConstant = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_StabilityTime1 extends HwXmlElement {
        private Element_StabilityTime1() {
        }

        public String getName() {
            return "StabilityTime1";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.stabilityTime1 = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_StabilityTime2 extends HwXmlElement {
        private Element_StabilityTime2() {
        }

        public String getName() {
            return "StabilityTime2";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.stabilityTime2 = HwXmlElement.string2Int(parser.nextText());
            return true;
        }
    }

    private static class Element_VariableStep extends HwXmlElement {
        private Element_VariableStep() {
        }

        public String getName() {
            return "VariableStep";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.useVariableStep = true;
            return true;
        }

        protected boolean checkValue() {
            return HwBrightnessXmlLoader.mData.darkenGradualTimeMin <= HwBrightnessXmlLoader.mData.darkenGradualTimeMax;
        }
    }

    static {
        boolean z = true;
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable(TAG, 3) : false : true;
        HWDEBUG = isLoggable;
        if (!Log.HWINFO) {
            z = Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false;
        }
        HWFLOW = z;
    }

    public static synchronized Data getData(int deviceActualBrightnessLevel) {
        Data retData;
        Throwable th;
        synchronized (HwBrightnessXmlLoader.class) {
            Data retData2;
            try {
                if (mLoader == null) {
                    mLoader = new HwBrightnessXmlLoader(deviceActualBrightnessLevel);
                }
                retData2 = (Data) mData.clone();
                if (retData2 == null) {
                    retData = new Data();
                    retData.loadDefaultConfig();
                    retData2 = retData;
                }
            } catch (Exception e) {
                Slog.e(TAG, "getData() failed! " + e);
                retData = new Data();
                retData.loadDefaultConfig();
                retData2 = retData;
            } catch (Throwable th2) {
                th = th2;
                retData2 = retData;
                throw th;
            }
        }
        return retData2;
    }

    private HwBrightnessXmlLoader(int deviceActualBrightnessLevel) {
        if (HWDEBUG) {
            Slog.d(TAG, "HwBrightnessXmlLoader()");
        }
        this.mDeviceActualBrightnessLevel = deviceActualBrightnessLevel;
        if (!parseXml(getXmlPath())) {
            mData.loadDefaultConfig();
        }
        mData.printData();
    }

    private boolean parseXml(String xmlPath) {
        if (xmlPath == null) {
            Slog.e(TAG, "parseXml() error! xmlPath is null");
            return false;
        }
        if (HWFLOW) {
            Slog.i(TAG, "parseXml() getXmlPath = " + xmlPath);
        }
        HwXmlParser xmlParser = new HwXmlParser(xmlPath);
        registerElement(xmlParser);
        if (!xmlParser.parse()) {
            Slog.e(TAG, "parseXml() error! xmlParser.parse() failed!");
            return false;
        } else if (xmlParser.check()) {
            if (HWFLOW) {
                Slog.i(TAG, "parseXml() load success!");
            }
            return true;
        } else {
            Slog.e(TAG, "parseXml() error! xmlParser.check() failed!");
            return false;
        }
    }

    private void registerElement(HwXmlParser parser) {
        HwXmlElement rootElement = parser.registerRootElement(new Element_LABCConfig());
        rootElement.registerChildElement(new Element_LightSensorRateMills());
        rootElement.registerChildElement(new Element_BrighenDebounceTime());
        rootElement.registerChildElement(new Element_DarkenDebounceTime());
        rootElement.registerChildElement(new Element_BrightenDebounceTimeParaBig());
        rootElement.registerChildElement(new Element_DarkenDebounceTimeParaBig());
        rootElement.registerChildElement(new Element_BrightenDeltaLuxPara());
        rootElement.registerChildElement(new Element_DarkenDeltaLuxPara());
        rootElement.registerChildElement(new Element_StabilityConstant());
        rootElement.registerChildElement(new Element_StabilityTime1());
        rootElement.registerChildElement(new Element_StabilityTime2());
        rootElement.registerChildElement(new Element_BrighenDebounceTimeForSmallThr());
        rootElement.registerChildElement(new Element_DarkenDebounceTimeForSmallThr());
        rootElement.registerChildElement(new Element_RatioForBrightnenSmallThr());
        rootElement.registerChildElement(new Element_RatioForDarkenSmallThr());
        rootElement.registerChildElement(new Element_DarkTimeDelayEnable());
        rootElement.registerChildElement(new Element_DarkTimeDelay());
        rootElement.registerChildElement(new Element_DarkTimeDelayLuxThreshold());
        rootElement.registerChildElement(new Element_CoverModeFirstLux());
        rootElement.registerChildElement(new Element_LastCloseScreenEnable());
        rootElement.registerChildElement(new Element_CoverModeBrightenResponseTime());
        rootElement.registerChildElement(new Element_CoverModeDarkenResponseTime());
        rootElement.registerChildElement(new Element_PostMaxMinAvgFilterNoFilterNum());
        rootElement.registerChildElement(new Element_PostMaxMinAvgFilterNum());
        rootElement.registerChildElement(new Element_PreWeightedMeanFilterNoFilterNum());
        rootElement.registerChildElement(new Element_PreWeightedMeanFilterNum());
        rootElement.registerChildElement(new Element_PreWeightedMeanFilterMaxFuncLuxNum());
        rootElement.registerChildElement(new Element_PreWeightedMeanFilterAlpha());
        rootElement.registerChildElement(new Element_PreWeightedMeanFilterLuxTh());
        rootElement.registerChildElement(new Element_DarkTimeDelayBeta0());
        rootElement.registerChildElement(new Element_DarkTimeDelayBeta1());
        rootElement.registerChildElement(new Element_DarkTimeDelayBeta2());
        rootElement.registerChildElement(new Element_PowerOnFastResponseLuxNum());
        rootElement.registerChildElement(new Element_AnimationEqualRatioEnable());
        rootElement.registerChildElement(new Element_ScreenBrightnessMinNit());
        rootElement.registerChildElement(new Element_ScreenBrightnessMaxNit());
        HwXmlElement sceneProcessing = rootElement.registerChildElement(new Element_SceneProcessing());
        sceneProcessing.registerChildElement(new Element_SceneMaxPoints());
        sceneProcessing.registerChildElement(new Element_SceneGapPoints());
        sceneProcessing.registerChildElement(new Element_SceneMinPoints());
        sceneProcessing.registerChildElement(new Element_SceneAmbientLuxMaxWeight());
        sceneProcessing.registerChildElement(new Element_SceneAmbientLuxMinWeight());
        HwXmlElement preProcessing = rootElement.registerChildElement(new Element_PreProcessing());
        preProcessing.registerChildElement(new Element_BrightTimeDelayEnable());
        preProcessing.registerChildElement(new Element_BrightTimeDelay());
        preProcessing.registerChildElement(new Element_BrightTimeDelayLuxThreshold());
        preProcessing.registerChildElement(new Element_PreMethodNum());
        preProcessing.registerChildElement(new Element_PreMeanFilterNoFilterNum());
        preProcessing.registerChildElement(new Element_PreMeanFilterNum());
        preProcessing.registerChildElement(new Element_PostMethodNum());
        preProcessing.registerChildElement(new Element_PostMeanFilterNoFilterNum());
        preProcessing.registerChildElement(new Element_PostMeanFilterNum());
        rootElement.registerChildElement(new Element_BrightenlinePoints()).registerChildElement(new Element_BrightenlinePoints_Point());
        rootElement.registerChildElement(new Element_DarkenlinePoints()).registerChildElement(new Element_DarkenlinePoints_Point());
        rootElement.registerChildElement(new Element_DefaultBrightness());
        rootElement.registerChildElement(new Element_BrightnessCalibrationEnabled());
        rootElement.registerChildElement(new Element_DefaultBrightnessPoints()).registerChildElement(new Element_DefaultBrightnessPoints_Point());
        rootElement.registerChildElement(new Element_BrightenGradualTime());
        rootElement.registerChildElement(new Element_DarkenGradualTime());
        rootElement.registerChildElement(new Element_BrightenThresholdFor255());
        rootElement.registerChildElement(new Element_DarkenTargetFor255());
        rootElement.registerChildElement(new Element_DarkenCurrentFor255());
        rootElement.registerChildElement(new Element_AutoFastTimeFor255());
        rootElement.registerChildElement(new Element_ManualFastTimeFor255());
        rootElement.registerChildElement(new Element_DimTime());
        HwXmlElement variableStep = rootElement.registerChildElement(new Element_VariableStep());
        variableStep.registerChildElement(new Element_DarkenGradualTimeMax());
        variableStep.registerChildElement(new Element_DarkenGradualTimeMin());
        variableStep.registerChildElement(new Element_AnimatedStepRoundEnabled());
        rootElement.registerChildElement(new Element_ReportValueWhenSensorOnChange());
        HwXmlElement proximity = rootElement.registerChildElement(new Element_Proximity());
        proximity.registerChildElement(new Element_AllowLabcUseProximity());
        proximity.registerChildElement(new Element_ProximityPositiveDebounceTime());
        proximity.registerChildElement(new Element_ProximityNegativeDebounceTime());
        rootElement.registerChildElement(new Element_ManualMode());
        rootElement.registerChildElement(new Element_ManualBrightnessMaxLimit());
        rootElement.registerChildElement(new Element_ManualBrightnessMinLimit());
        rootElement.registerChildElement(new Element_OutDoorThreshold());
        rootElement.registerChildElement(new Element_InDoorThreshold());
        rootElement.registerChildElement(new Element_ManualBrighenDebounceTime());
        rootElement.registerChildElement(new Element_ManualDarkenDebounceTime());
        rootElement.registerChildElement(new Element_ManualBrightenLinePoints()).registerChildElement(new Element_ManualBrightenLinePoints_Point());
        rootElement.registerChildElement(new Element_ManualDarkenLinePoints()).registerChildElement(new Element_ManualDarkenLinePoints_Point());
    }

    private String getXmlPath() {
        String xmlPath = String.format("/xml/lcd/%s_%s%s", new Object[]{XML_NAME_NOEXT, SystemProperties.get("ro.config.devicecolor"), XML_EXT});
        File xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 0);
        if (HWFLOW) {
            Slog.i(TAG, "screenColor=" + screenColor + ",screenColorxmlPath=" + xmlPath);
        }
        if (xmlFile == null) {
            xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s", new Object[]{XML_NAME}), 0);
            if (xmlFile == null) {
                Slog.e(TAG, "getXmlPath() error! can't find xml file.");
                return null;
            }
        }
        return xmlFile.getAbsolutePath();
    }

    private static boolean checkPointsListIsOK(List<PointF> list) {
        if (list == null) {
            Slog.e(TAG, "checkPointsListIsOK() error! list is null");
            return false;
        } else if (list.size() < 3 || list.size() >= 100) {
            Slog.e(TAG, "checkPointsListIsOK() error! list size=" + list.size() + " is out of range");
            return false;
        } else {
            PointF lastPoint = null;
            for (PointF point : list) {
                if (lastPoint == null || point.x > lastPoint.x) {
                    lastPoint = point;
                } else {
                    Slog.e(TAG, "checkPointsListIsOK() error! x in list isn't a increasing sequence, " + point.x + "<=" + lastPoint.x);
                    return false;
                }
            }
            return true;
        }
    }
}
