package com.android.settings.colortemper;

import android.util.Slog;
import java.io.IOException;

public class ColorTemperMgr {
    private float b;
    private float g;
    private float mMaxRadius = 330.0f;
    private ColorXmlReader mXmlReader = new ColorXmlReader();
    private float r;
    private float x_user_set;
    private float xout;
    private float y_user_set;
    private float yout;

    public ColorTemperMgr() {
        try {
            if (!this.mXmlReader.getConfig()) {
                Slog.e("ColorTemperMgr", "getConfig failed! loadDefaultConfig");
                this.mXmlReader.loadDefaultConfig();
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.mXmlReader.loadDefaultConfig();
        }
    }

    public void setRadius(float maxRadius) {
        this.mMaxRadius = maxRadius;
    }

    public void getRGBGain(float x, float y) {
        float theta;
        float alphar = this.mXmlReader.getAlphaR();
        float alphag = this.mXmlReader.getAlphaG();
        float alphab = this.mXmlReader.getAlphaB();
        float alphac = this.mXmlReader.getAlphaC();
        float alpham = this.mXmlReader.getAlphaM();
        float alphay = this.mXmlReader.getAlphaY();
        Slog.i("ColorTemperMgr", "r=" + alphar + ",g=" + alphag + ",b=" + alphab + ",c=" + alphac + ",m=" + alpham + ",y=" + alphay);
        float x0 = this.mMaxRadius;
        float y0 = this.mMaxRadius;
        float x1 = x - x0;
        float y1 = y - y0;
        float rho0 = (float) Math.sqrt((double) ((x1 * x1) + (y1 * y1)));
        if (y1 > 0.0f) {
            theta = (float) Math.asin((double) (x1 / rho0));
            if (theta < 0.0f) {
                theta += 6.283185f;
            }
        } else {
            theta = 3.1415925f - ((float) Math.asin((double) (x1 / rho0)));
        }
        theta = (theta / 3.1415925f) * 180.0f;
        float rho = rho0 / this.mMaxRadius;
        if (rho > 1.0f) {
            gainCalc(1.0f, theta, alphar, alphag, alphab, alphac, alpham, alphay);
            this.xout = (float) Math.floor((double) ((this.mMaxRadius * x1) / rho0));
            this.yout = (float) Math.floor((double) ((this.mMaxRadius * y1) / rho0));
            this.xout += x0;
            this.yout += y0;
        }
        if (rho <= 1.0f) {
            gainCalc(rho, theta, alphar, alphag, alphab, alphac, alpham, alphay);
            this.xout = x;
            this.yout = y;
        }
        ColorTemperUtils.getInstance().setOutX(this.xout);
        ColorTemperUtils.getInstance().setOutY(this.yout);
    }

    public void userCTValue2Coord(int ct_value, int max_ct_value) {
        float x0 = this.mMaxRadius;
        float y0 = this.mMaxRadius;
        int std_ct_value = (max_ct_value + 1) / 2;
        float ratio;
        if (ct_value >= std_ct_value) {
            ratio = ((((float) ct_value) * 1.0f) - (((float) std_ct_value) * 1.0f)) / (((float) max_ct_value) - (((float) std_ct_value) * 1.0f));
            float y_blue = y0 + (this.mMaxRadius * 0.5f);
            this.x_user_set = interpo(x0, x0 + ((this.mMaxRadius * 0.5f) * ((float) Math.sqrt(3.0d))), ratio);
            this.y_user_set = interpo(y0, y_blue, ratio);
        } else {
            ratio = ((((float) std_ct_value) * 1.0f) - (((float) ct_value) * 1.0f)) / (((float) std_ct_value) * 1.0f);
            float y_yellow = y0 - (this.mMaxRadius * 0.5f);
            this.x_user_set = interpo(x0, x0 - ((this.mMaxRadius * 0.5f) * ((float) Math.sqrt(3.0d))), ratio);
            this.y_user_set = interpo(y0, y_yellow, ratio);
        }
        ColorTemperUtils.getInstance().setUserX(this.x_user_set);
        ColorTemperUtils.getInstance().setUserY(this.y_user_set);
    }

    public boolean isInCircle(float x, float y) {
        boolean m_inCircle = true;
        float x1 = x - this.mMaxRadius;
        float y1 = y - this.mMaxRadius;
        float rho = ((float) Math.sqrt((double) ((x1 * x1) + (y1 * y1)))) / this.mMaxRadius;
        if (rho > 1.0f) {
            m_inCircle = false;
        }
        if (rho <= 1.0f) {
            return true;
        }
        return m_inCircle;
    }

    private float interpo(float x, float y, float a) {
        if (a < 0.0f || a > 1.0f) {
            return 0.0f;
        }
        return ((1.0f - a) * x) + (y * a);
    }

    private void colorInterpo(float r1, float g1, float b1, float r2, float g2, float b2, float rho, float f) {
        if (rho >= 0.0f && rho <= 1.0f && f >= 0.0f && f <= 1.0f) {
            float rr = interpo(r1, r2, f);
            float gg = interpo(g1, g2, f);
            float bb = interpo(b1, b2, f);
            this.r = interpo(1.0f, rr, rho);
            this.g = interpo(1.0f, gg, rho);
            this.b = interpo(1.0f, bb, rho);
        }
    }

    private void gainCalc(float rho, float theta, float alphar, float alphag, float alphab, float alphac, float alpham, float alphay) {
        if (rho > 0.0f && rho <= 1.0f) {
            int i = (int) Math.floor((double) (theta / 60.0f));
            float f = (theta / 60.0f) - ((float) i);
            switch (i) {
                case 0:
                    colorInterpo(alphac, 1.0f, 1.0f, alphab, alphab, 1.0f, rho, f);
                    break;
                case 1:
                    colorInterpo(alphab, alphab, 1.0f, 1.0f, alpham, 1.0f, rho, f);
                    break;
                case 2:
                    colorInterpo(1.0f, alpham, 1.0f, 1.0f, alphar, alphar, rho, f);
                    break;
                case 3:
                    colorInterpo(1.0f, alphar, alphar, 1.0f, 1.0f, alphay, rho, f);
                    break;
                case 4:
                    colorInterpo(1.0f, 1.0f, alphay, alphag, 1.0f, alphag, rho, f);
                    break;
                case 5:
                    colorInterpo(alphag, 1.0f, alphag, alphac, 1.0f, 1.0f, rho, f);
                    break;
            }
        }
        this.r = 1.0f;
        this.g = 1.0f;
        this.b = 1.0f;
        ColorTemperUtils.getInstance().setR(this.r);
        ColorTemperUtils.getInstance().setG(this.g);
        ColorTemperUtils.getInstance().setB(this.b);
    }
}
