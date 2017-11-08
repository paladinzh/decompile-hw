package com.huawei.watermark.manager.parse;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.huawei.watermark.manager.parse.WMElement.LogicDelegate;
import com.huawei.watermark.manager.parse.util.WMWeatherService.WeatherData;
import com.huawei.watermark.manager.parse.util.WMWeatherService.WeatherUpdateCallback;
import com.huawei.watermark.wmutil.LocalizeUtil;
import com.huawei.watermark.wmutil.WMBaseUtil;
import com.huawei.watermark.wmutil.WMFileUtil;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMStringUtil;
import com.huawei.watermark.wmutil.WMUIUtil;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class WMWeather extends WMElement {
    private String temperaturedu;
    private int temperatureduh;
    private String temperaturedulayoutgravity;
    private int temperaturedumarginBottom;
    private int temperaturedumarginLeft;
    private int temperaturedumarginRight;
    private int temperaturedumarginTop;
    private String temperaturedutype;
    private int temperatureduw;
    private String temperatureimage;
    private int temperatureimageh;
    private String temperatureimagelayoutgravity;
    private int temperatureimagemarginBottom;
    private int temperatureimagemarginLeft;
    private int temperatureimagemarginRight;
    private int temperatureimagemarginTop;
    private int temperatureimagesinglenumw;
    private String temperatureimagevalue;
    private int temperatureimagew;
    private String temperaturetext;
    private String temperaturetextfontcolor;
    private String temperaturetextfontname;
    private int temperaturetextfontsize;
    private String temperaturetextlayoutgravity;
    private int temperaturetextmarginBottom;
    private int temperaturetextmarginLeft;
    private int temperaturetextmarginRight;
    private int temperaturetextmarginTop;
    private String temperaturetextshadowcolor;
    private float temperaturetextshadowr;
    private float temperaturetextshadowx;
    private float temperaturetextshadowy;
    private String temperaturetexttype;
    private String temperaturetextvalue;
    private String weatherimage;
    private int weatherimageh;
    private String weatherimagelayoutgravity;
    private int weatherimagemarginBottom;
    private int weatherimagemarginLeft;
    private int weatherimagemarginRight;
    private int weatherimagemarginTop;
    private String weatherimagevalue;
    private int weatherimagew;
    private String weathertext;
    private int weathertextfontsize;
    private String weathertextlayoutgravity;
    private int weathertextmarginBottom;
    private int weathertextmarginLeft;
    private int weathertextmarginRight;
    private int weathertextmarginTop;
    private String weathertextvalue;
    private String weatherwinddirectiontext;
    private String weatherwinddirectiontextfontcolor;
    private String weatherwinddirectiontextfontname;
    private int weatherwinddirectiontextfontsize;
    private String weatherwinddirectiontextlayoutgravity;
    private int weatherwinddirectiontextmarginBottom;
    private int weatherwinddirectiontextmarginLeft;
    private int weatherwinddirectiontextmarginRight;
    private int weatherwinddirectiontextmarginTop;
    private String weatherwinddirectiontextshadowcolor;
    private float weatherwinddirectiontextshadowr;
    private float weatherwinddirectiontextshadowx;
    private float weatherwinddirectiontextshadowy;
    private String weatherwinddirectiontextvalue;
    private String weatherwindpowertext;
    private String weatherwindpowertextfontcolor;
    private String weatherwindpowertextfontname;
    private int weatherwindpowertextfontsize;
    private String weatherwindpowertextlayoutgravity;
    private int weatherwindpowertextmarginBottom;
    private int weatherwindpowertextmarginLeft;
    private int weatherwindpowertextmarginRight;
    private int weatherwindpowertextmarginTop;
    private String weatherwindpowertextshadowcolor;
    private float weatherwindpowertextshadowr;
    private float weatherwindpowertextshadowx;
    private float weatherwindpowertextshadowy;
    private String weatherwindpowertextvalue;

    public void initBaseLogicData(Context context, LogicDelegate delegate) {
        super.initBaseLogicData(context, delegate);
    }

    public WMWeather(XmlPullParser parser) {
        super(parser);
        this.temperaturetext = getStringByAttributeName(parser, "temperaturetext", "hide");
        this.temperaturetextvalue = getStringByAttributeName(parser, "temperaturetextvalue", "");
        this.temperaturetexttype = getStringByAttributeName(parser, "temperaturetexttype", "normal");
        this.temperaturetextfontsize = getIntByAttributeName(parser, "temperaturetextfontsize", 0);
        this.temperaturetextfontcolor = getStringByAttributeName(parser, "temperaturetextfontcolor", "#ffffffff");
        this.temperaturetextfontname = getStringByAttributeName(parser, "temperaturetextfontname");
        this.temperaturetextshadowcolor = getStringByAttributeName(parser, "temperaturetextshadowcolor", "#ffffffff");
        this.temperaturetextshadowx = getFloatByAttributeName(parser, "temperaturetextshadowx");
        this.temperaturetextshadowy = getFloatByAttributeName(parser, "temperaturetextshadowy");
        this.temperaturetextshadowr = getFloatByAttributeName(parser, "temperaturetextshadowr");
        this.temperaturetextmarginLeft = getIntByAttributeName(parser, "temperaturetextmarginLeft", 0);
        this.temperaturetextmarginTop = getIntByAttributeName(parser, "temperaturetextmarginTop", 0);
        this.temperaturetextmarginRight = getIntByAttributeName(parser, "temperaturetextmarginRight", 0);
        this.temperaturetextmarginBottom = getIntByAttributeName(parser, "temperaturetextmarginBottom", 0);
        this.temperaturetextlayoutgravity = getStringByAttributeName(parser, "temperaturetextlayoutgravity", "");
        this.temperatureimage = getStringByAttributeName(parser, "temperatureimage", "hide");
        this.temperatureimagevalue = getStringByAttributeName(parser, "temperatureimagevalue", "");
        this.temperatureimagew = getIntByAttributeName(parser, "temperatureimagew", 0);
        this.temperatureimageh = getIntByAttributeName(parser, "temperatureimageh", 0);
        this.temperatureimagesinglenumw = getIntByAttributeName(parser, "temperatureimagesinglenumw", -2);
        this.temperatureimagemarginLeft = getIntByAttributeName(parser, "temperatureimagemarginLeft", 0);
        this.temperatureimagemarginTop = getIntByAttributeName(parser, "temperatureimagemarginTop", 0);
        this.temperatureimagemarginRight = getIntByAttributeName(parser, "temperatureimagemarginRight", 0);
        this.temperatureimagemarginBottom = getIntByAttributeName(parser, "temperatureimagemarginBottom", 0);
        this.temperatureimagelayoutgravity = getStringByAttributeName(parser, "temperatureimagelayoutgravity", "");
        this.temperaturedu = getStringByAttributeName(parser, "temperaturedu", "hide");
        this.temperatureduw = getIntByAttributeName(parser, "temperatureduw", 0);
        this.temperatureduh = getIntByAttributeName(parser, "temperatureduh", 0);
        this.temperaturedumarginLeft = getIntByAttributeName(parser, "temperaturedumarginLeft", 0);
        this.temperaturedumarginTop = getIntByAttributeName(parser, "temperaturedumarginTop", 0);
        this.temperaturedumarginRight = getIntByAttributeName(parser, "temperaturedumarginRight", 0);
        this.temperaturedumarginBottom = getIntByAttributeName(parser, "temperaturedumarginBottom", 0);
        this.temperaturedulayoutgravity = getStringByAttributeName(parser, "temperaturedulayoutgravity", "");
        this.temperaturedutype = getStringByAttributeName(parser, "temperaturedulayoutgravity", "rel");
        this.weatherimage = getStringByAttributeName(parser, "weatherimage", "hide");
        this.weatherimagevalue = getStringByAttributeName(parser, "weatherimagevalue", "");
        this.weatherimagew = getIntByAttributeName(parser, "weatherimagew", 0);
        this.weatherimageh = getIntByAttributeName(parser, "weatherimageh", 0);
        this.weatherimagemarginLeft = getIntByAttributeName(parser, "weatherimagemarginLeft", 0);
        this.weatherimagemarginTop = getIntByAttributeName(parser, "weatherimagemarginTop", 0);
        this.weatherimagemarginRight = getIntByAttributeName(parser, "weatherimagemarginRight", 0);
        this.weatherimagemarginBottom = getIntByAttributeName(parser, "weatherimagemarginBottom", 0);
        this.weatherimagelayoutgravity = getStringByAttributeName(parser, "weatherimagelayoutgravity", "");
        this.weathertext = getStringByAttributeName(parser, "weathertext", "hide");
        this.weathertextvalue = getStringByAttributeName(parser, "weathertextvalue", "");
        this.weathertextfontsize = getIntByAttributeName(parser, "weathertextfontsize", 0);
        this.weathertextmarginLeft = getIntByAttributeName(parser, "weathertextmarginLeft", 0);
        this.weathertextmarginTop = getIntByAttributeName(parser, "weathertextmarginTop", 0);
        this.weathertextmarginRight = getIntByAttributeName(parser, "weathertextmarginRight", 0);
        this.weathertextmarginBottom = getIntByAttributeName(parser, "weathertextmarginBottom", 0);
        this.weathertextlayoutgravity = getStringByAttributeName(parser, "weathertextlayoutgravity", "");
        this.weatherwinddirectiontext = getStringByAttributeName(parser, "weatherwinddirectiontext", "hide");
        this.weatherwinddirectiontextvalue = getStringByAttributeName(parser, "weatherwinddirectiontextvalue", "");
        this.weatherwinddirectiontextfontsize = getIntByAttributeName(parser, "weatherwinddirectiontextfontsize", 0);
        this.weatherwinddirectiontextfontcolor = getStringByAttributeName(parser, "weatherwinddirectiontextfontcolor", "");
        this.weatherwinddirectiontextfontname = getStringByAttributeName(parser, "weatherwinddirectiontextfontname", "");
        this.weatherwinddirectiontextshadowcolor = getStringByAttributeName(parser, "weatherwinddirectiontextshadowcolor", "");
        this.weatherwinddirectiontextshadowx = getFloatByAttributeName(parser, "weatherwinddirectiontextshadowx");
        this.weatherwinddirectiontextshadowy = getFloatByAttributeName(parser, "weatherwinddirectiontextshadowy");
        this.weatherwinddirectiontextshadowr = getFloatByAttributeName(parser, "weatherwinddirectiontextshadowr");
        this.weatherwinddirectiontextmarginLeft = getIntByAttributeName(parser, "weatherwinddirectiontextmarginLeft", 0);
        this.weatherwinddirectiontextmarginTop = getIntByAttributeName(parser, "weatherwinddirectiontextmarginTop", 0);
        this.weatherwinddirectiontextmarginRight = getIntByAttributeName(parser, "weatherwinddirectiontextmarginRight", 0);
        this.weatherwinddirectiontextmarginBottom = getIntByAttributeName(parser, "weatherwinddirectiontextmarginBottom", 0);
        this.weatherwinddirectiontextlayoutgravity = getStringByAttributeName(parser, "weatherwinddirectiontextlayoutgravity", "");
        this.weatherwindpowertext = getStringByAttributeName(parser, "weatherwindpowertext", "hide");
        this.weatherwindpowertextvalue = getStringByAttributeName(parser, "weatherwindpowertextvalue", "");
        this.weatherwindpowertextfontsize = getIntByAttributeName(parser, "weatherwindpowertextfontsize", 0);
        this.weatherwindpowertextfontcolor = getStringByAttributeName(parser, "weatherwindpowertextfontcolor", "");
        this.weatherwindpowertextfontname = getStringByAttributeName(parser, "weatherwindpowertextfontname", "");
        this.weatherwindpowertextshadowcolor = getStringByAttributeName(parser, "weatherwindpowertextshadowcolor", "");
        this.weatherwindpowertextshadowx = getFloatByAttributeName(parser, "weatherwindpowertextshadowx");
        this.weatherwindpowertextshadowy = getFloatByAttributeName(parser, "weatherwindpowertextshadowy");
        this.weatherwindpowertextshadowr = getFloatByAttributeName(parser, "weatherwindpowertextshadowr");
        this.weatherwindpowertextmarginLeft = getIntByAttributeName(parser, "weatherwindpowertextmarginLeft", 0);
        this.weatherwindpowertextmarginTop = getIntByAttributeName(parser, "weatherwindpowertextmarginTop", 0);
        this.weatherwindpowertextmarginRight = getIntByAttributeName(parser, "weatherwindpowertextmarginRight", 0);
        this.weatherwindpowertextmarginBottom = getIntByAttributeName(parser, "weatherwindpowertextmarginBottom", 0);
        this.weatherwindpowertextlayoutgravity = getStringByAttributeName(parser, "weatherwindpowertextlayoutgravity", "");
    }

    public View toView(Context context, WaterMark wm, String parentLayoutMode, int ori) {
        this.mOri = ori;
        final View view = LayoutInflater.from(context).inflate(Integer.valueOf(WMResourceUtil.getLayoutId(context, "wm_jar_weather_common_style")).intValue(), null);
        final LinearLayout temperatureImage = (LinearLayout) view.findViewById(WMResourceUtil.getId(context, "currentTemperatureImage"));
        setTemperatureImageParams(temperatureImage, wm);
        setTemperatureImageValue(temperatureImage, wm);
        final TextView temperatureText = (TextView) view.findViewById(WMResourceUtil.getId(context, "currentTemperatureText"));
        setTemperatureTextParams(temperatureText, wm);
        setTemperatureTextValue(temperatureText);
        final ImageView duimage = (ImageView) view.findViewById(WMResourceUtil.getId(context, "du"));
        setDuImageLayoutParams(duimage, wm);
        setDuImageValue(duimage, context, wm, 0);
        final ImageView weatherDescriptionImage = (ImageView) view.findViewById(WMResourceUtil.getId(context, "currentWeatherDesImage"));
        setWeatherDescriptionImageLayoutParams(weatherDescriptionImage, wm);
        setWeatherDescriptionImageValue(weatherDescriptionImage, wm);
        final TextView weatherDescriptionText = (TextView) view.findViewById(WMResourceUtil.getId(context, "currentWeatherDesText"));
        setWeatherDescriptionTextLayoutParams(weatherDescriptionText, wm);
        setWeatherDescriptionTextValue(weatherDescriptionText);
        final TextView weatherWindDirectionText = (TextView) view.findViewById(WMResourceUtil.getId(context, "currentWeatherWinddirection"));
        setWeatherWindDirectionTextLayoutParams(weatherWindDirectionText, wm);
        setWeatherWindDirectionTextValue(weatherWindDirectionText);
        final TextView weatherWindPowerText = (TextView) view.findViewById(WMResourceUtil.getId(context, "currentWeatherWindpower"));
        setWeatherWindPowerTextLayoutParams(weatherWindPowerText, wm);
        setWeatherWindPowerTextValue(weatherWindPowerText);
        view.setId(wm.generateId(this.id));
        view.setLayoutParams(generateLp(context, wm, parentLayoutMode));
        final WaterMark waterMark = wm;
        final Context context2 = context;
        this.mLogicDelegate.addWeatherUpdateCallback(new WeatherUpdateCallback() {
            public void onWeatherReport(WeatherData data) {
                if (data != null) {
                    View view = view;
                    final LinearLayout linearLayout = temperatureImage;
                    final WaterMark waterMark = waterMark;
                    final TextView textView = temperatureText;
                    final ImageView imageView = duimage;
                    final Context context = context2;
                    final ImageView imageView2 = weatherDescriptionImage;
                    final TextView textView2 = weatherDescriptionText;
                    final TextView textView3 = weatherWindDirectionText;
                    final TextView textView4 = weatherWindPowerText;
                    final WeatherData weatherData = data;
                    view.post(new Runnable() {
                        public void run() {
                            if ("lowandhigh".equalsIgnoreCase(WMWeather.this.temperaturetexttype)) {
                                WMWeather.this.temperaturetextvalue = "" + LocalizeUtil.getLocalizeNumber(weatherData.getTemperatureHigh()) + "/" + LocalizeUtil.getLocalizeNumber(weatherData.getTemperatureLow());
                            } else {
                                WMWeather.this.temperaturetextvalue = "" + LocalizeUtil.getLocalizeNumber(weatherData.getTemperature());
                            }
                            WMWeather.this.temperatureimagevalue = "" + weatherData.getTemperature();
                            WMWeather.this.weatherimagevalue = AnonymousClass1.this.consWeatherImageNameFromText(weatherData.getWeatherIcon());
                            WMWeather.this.weathertextvalue = weatherData.getWeatherDes();
                            if (!WMStringUtil.isEmptyString(weatherData.getWeatherWindDirection())) {
                                WMWeather.this.weatherwinddirectiontextvalue = weatherData.getWeatherWindDirection();
                            }
                            if (!WMStringUtil.isEmptyString(weatherData.getWeatherWindPower())) {
                                WMWeather.this.weatherwindpowertextvalue = weatherData.getWeatherWindPower();
                            }
                            WMWeather.this.setTemperatureImageValue(linearLayout, waterMark);
                            WMWeather.this.setTemperatureTextValue(textView);
                            WMWeather.this.setDuImageValue(imageView, context, waterMark, weatherData.getTempUnit());
                            WMWeather.this.setWeatherDescriptionImageValue(imageView2, waterMark);
                            WMWeather.this.setWeatherDescriptionTextValue(textView2);
                            WMWeather.this.setWeatherWindDirectionTextValue(textView3);
                            WMWeather.this.setWeatherWindPowerTextValue(textView4);
                        }
                    });
                }
            }

            private String consWeatherImageNameFromText(String weather) {
                return weather + ".png";
            }
        });
        return view;
    }

    private void setTemperatureTextParams(TextView temperatureText, WaterMark wm) {
        if ("show".equals(this.temperaturetext)) {
            temperatureText.setVisibility(0);
            float scale = wm.getScale();
            LayoutParams lp = consLayoutGravityParams(consMarginParams((LayoutParams) temperatureText.getLayoutParams(), temperatureText.getContext(), this.temperaturetextmarginLeft, this.temperaturetextmarginTop, this.temperaturetextmarginRight, this.temperaturetextmarginBottom, scale), this.temperaturetextlayoutgravity);
            temperatureText.setTextSize(1, ((float) this.temperaturetextfontsize) * scale);
            temperatureText.setTextColor(Color.parseColor(this.temperaturetextfontcolor));
            temperatureText.setShadowLayer(this.temperaturetextshadowr, this.temperaturetextshadowx, this.temperaturetextshadowy, Color.parseColor(this.temperaturetextshadowcolor));
            temperatureText.setLayoutParams(lp);
        }
    }

    private void setTemperatureTextValue(TextView temperatureText) {
        if ("show".equals(this.temperaturetext)) {
            this.temperaturetextvalue = WMUIUtil.getDecoratorText(temperatureText.getContext(), this.temperaturetextvalue);
            temperatureText.setText(this.temperaturetextvalue);
        }
    }

    private void setTemperatureImageParams(LinearLayout temperatureLayout, WaterMark wm) {
        if ("show".equals(this.temperatureimage)) {
            temperatureLayout.setVisibility(0);
            temperatureLayout.setLayoutParams(consLayoutGravityParams(consMarginParams((LayoutParams) temperatureLayout.getLayoutParams(), temperatureLayout.getContext(), this.temperatureimagemarginLeft, this.temperatureimagemarginTop, this.temperatureimagemarginRight, this.temperatureimagemarginBottom, wm.getScale()), this.temperatureimagelayoutgravity));
        }
    }

    private void setTemperatureImageValue(LinearLayout temperatureLayout, WaterMark wm) {
        if ("show".equals(this.temperatureimage)) {
            WMUIUtil.showNumAndIcon(temperatureLayout, this.temperatureimagevalue, this.temperatureimagesinglenumw, this.h, null, wm.getPath(), wm.getScale(), 0);
        }
    }

    private void setDuImageLayoutParams(ImageView duImage, WaterMark wm) {
        if ("show".equals(this.temperaturedu)) {
            duImage.setVisibility(0);
            int w = this.temperatureduw;
            int h = this.temperatureduh;
            LayoutParams lp = (LayoutParams) duImage.getLayoutParams();
            if ("rel".equalsIgnoreCase(this.temperaturedutype)) {
                if ("show".equals(this.temperaturetext)) {
                    lp.addRule(1, WMResourceUtil.getId(duImage.getContext(), "currentTemperatureText"));
                } else if ("show".equals(this.temperatureimage)) {
                    lp.addRule(1, WMResourceUtil.getId(duImage.getContext(), "currentTemperatureImage"));
                }
            }
            float scale = wm.getScale();
            lp = consLayoutGravityParams(consMarginParams(lp, duImage.getContext(), this.temperaturedumarginLeft, this.temperaturedumarginTop, this.temperaturedumarginRight, this.temperaturedumarginBottom, scale), this.temperaturedulayoutgravity);
            lp.width = WMBaseUtil.dpToPixel((float) w, duImage.getContext());
            lp.height = WMBaseUtil.dpToPixel((float) h, duImage.getContext());
            if (lp.width > 0) {
                lp.width = Math.round(((float) lp.width) * scale);
            }
            if (lp.height > 0) {
                lp.height = Math.round(((float) lp.height) * scale);
            }
        }
    }

    private void setDuImageValue(ImageView duImage, Context context, WaterMark wm, int tempunit) {
        if ("show".equals(this.temperaturedu)) {
            String duimgname = "du.png";
            if (tempunit == 1) {
                duimgname = "du_en.png";
            }
            duImage.setImageBitmap(WMFileUtil.decodeBitmap(context, wm.getPath(), duimgname));
        }
    }

    private void setWeatherDescriptionImageLayoutParams(ImageView weatherDescriptionImage, WaterMark wm) {
        if ("show".equals(this.weatherimage)) {
            weatherDescriptionImage.setVisibility(0);
            int w = this.weatherimagew;
            int h = this.weatherimageh;
            float scale = wm.getScale();
            LayoutParams lp = consLayoutGravityParams(consMarginParams((LayoutParams) weatherDescriptionImage.getLayoutParams(), weatherDescriptionImage.getContext(), this.weatherimagemarginLeft, this.weatherimagemarginTop, this.weatherimagemarginRight, this.weatherimagemarginBottom, scale), this.weatherimagelayoutgravity);
            lp.width = WMBaseUtil.dpToPixel((float) w, weatherDescriptionImage.getContext());
            lp.height = WMBaseUtil.dpToPixel((float) h, weatherDescriptionImage.getContext());
            if (lp.width > 0) {
                lp.width = Math.round(((float) lp.width) * scale);
            }
            if (lp.height > 0) {
                lp.height = Math.round(((float) lp.height) * scale);
            }
            weatherDescriptionImage.setLayoutParams(lp);
        }
    }

    private void setWeatherDescriptionImageValue(ImageView weatherDescriptionImage, WaterMark wm) {
        if ("show".equals(this.weatherimage)) {
            weatherDescriptionImage.setImageBitmap(WMFileUtil.decodeBitmap(weatherDescriptionImage.getContext(), wm.getPath(), this.weatherimagevalue));
        }
    }

    private void setWeatherDescriptionTextLayoutParams(TextView weatherDescriptionText, WaterMark wm) {
        if ("show".equals(this.weathertext)) {
            weatherDescriptionText.setVisibility(0);
            int fontsize = this.weathertextfontsize;
            float scale = wm.getScale();
            LayoutParams lp = consLayoutGravityParams(consMarginParams((LayoutParams) weatherDescriptionText.getLayoutParams(), weatherDescriptionText.getContext(), this.weathertextmarginLeft, this.weathertextmarginTop, this.weathertextmarginRight, this.weathertextmarginBottom, scale), this.weathertextlayoutgravity);
            weatherDescriptionText.setTextSize(1, ((float) fontsize) * scale);
            weatherDescriptionText.setLayoutParams(lp);
        }
    }

    private void setWeatherDescriptionTextValue(TextView weatherDescriptionText) {
        if ("show".equals(this.weathertext)) {
            this.weathertextvalue = WMUIUtil.getDecoratorText(weatherDescriptionText.getContext(), this.weathertextvalue);
            weatherDescriptionText.setText(this.weathertextvalue);
        }
    }

    private void setWeatherWindDirectionTextLayoutParams(TextView weatherWindDirectionText, WaterMark wm) {
        if ("show".equals(this.weatherwinddirectiontext)) {
            weatherWindDirectionText.setVisibility(0);
            int fontsize = this.weatherwinddirectiontextfontsize;
            float scale = wm.getScale();
            LayoutParams lp = consLayoutGravityParams(consMarginParams((LayoutParams) weatherWindDirectionText.getLayoutParams(), weatherWindDirectionText.getContext(), this.weatherwinddirectiontextmarginLeft, this.weatherwinddirectiontextmarginTop, this.weatherwinddirectiontextmarginRight, this.weatherwinddirectiontextmarginBottom, scale), this.weatherwinddirectiontextlayoutgravity);
            weatherWindDirectionText.setTextSize(1, ((float) fontsize) * scale);
            weatherWindDirectionText.setTextColor(Color.parseColor(this.weatherwinddirectiontextfontcolor));
            weatherWindDirectionText.setShadowLayer(this.weatherwinddirectiontextshadowr, this.weatherwinddirectiontextshadowx, this.weatherwinddirectiontextshadowy, Color.parseColor(this.weatherwinddirectiontextshadowcolor));
            weatherWindDirectionText.setLayoutParams(lp);
        }
    }

    private void setWeatherWindDirectionTextValue(TextView weatherWindDirectionText) {
        if ("show".equals(this.weatherwinddirectiontext)) {
            this.weatherwinddirectiontextvalue = WMUIUtil.getDecoratorText(weatherWindDirectionText.getContext(), this.weatherwinddirectiontextvalue);
            weatherWindDirectionText.setText(this.weatherwinddirectiontextvalue);
        }
    }

    private void setWeatherWindPowerTextLayoutParams(TextView weatherWindPowerText, WaterMark wm) {
        if ("show".equals(this.weatherwindpowertext)) {
            weatherWindPowerText.setVisibility(0);
            int fontsize = this.weatherwindpowertextfontsize;
            float scale = wm.getScale();
            LayoutParams lp = consLayoutGravityParams(consMarginParams((LayoutParams) weatherWindPowerText.getLayoutParams(), weatherWindPowerText.getContext(), this.weatherwindpowertextmarginLeft, this.weatherwindpowertextmarginTop, this.weatherwindpowertextmarginRight, this.weatherwindpowertextmarginBottom, scale), this.weatherwindpowertextlayoutgravity);
            weatherWindPowerText.setTextSize(1, ((float) fontsize) * scale);
            weatherWindPowerText.setTextColor(Color.parseColor(this.weatherwindpowertextfontcolor));
            weatherWindPowerText.setShadowLayer(this.weatherwindpowertextshadowr, this.weatherwindpowertextshadowx, this.weatherwindpowertextshadowy, Color.parseColor(this.weatherwindpowertextshadowcolor));
            weatherWindPowerText.setLayoutParams(lp);
        }
    }

    private void setWeatherWindPowerTextValue(TextView weatherWindPowerText) {
        if ("show".equals(this.weatherwindpowertext)) {
            this.weatherwindpowertextvalue = WMUIUtil.getDecoratorText(weatherWindPowerText.getContext(), this.weatherwindpowertextvalue);
            weatherWindPowerText.setText(this.weatherwindpowertextvalue);
        }
    }

    private LayoutParams consLayoutGravityParams(LayoutParams lp, String gravity) {
        List<String> args = WMStringUtil.split(gravity, "|");
        if (args.contains("left")) {
            lp.addRule(9);
        }
        if (args.contains("right")) {
            lp.addRule(11);
        }
        if (args.contains("top")) {
            lp.addRule(10);
        }
        if (args.contains("bottom")) {
            lp.addRule(12);
        }
        if (args.contains("center")) {
            lp.addRule(13);
        }
        if (args.contains("center_horizontal")) {
            lp.addRule(14);
        }
        if (args.contains("center_vertical")) {
            lp.addRule(15);
        }
        return lp;
    }

    private LayoutParams consMarginParams(LayoutParams lp, Context context, int left, int top, int right, int bottom, float scale) {
        lp.setMargins(WMBaseUtil.dpToPixel(((float) left) * scale, context), WMBaseUtil.dpToPixel(((float) top) * scale, context), WMBaseUtil.dpToPixel(((float) right) * scale, context), WMBaseUtil.dpToPixel(((float) bottom) * scale, context));
        return lp;
    }
}
