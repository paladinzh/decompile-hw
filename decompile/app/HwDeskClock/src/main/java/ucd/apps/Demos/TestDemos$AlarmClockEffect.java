package ucd.apps.Demos;

import android.annotation.SuppressLint;
import android.os.SystemClock;
import com.android.deskclock.DeskClockApplication;
import com.android.util.Utils;
import java.security.SecureRandom;
import ucd.ui.framework.core.GLBase;
import ucd.ui.framework.core.Group;
import ucd.ui.widget.effectview.music.AlarmCover;
import ucd.ui.widget.effectview.music.DataAdapter;

public class TestDemos$AlarmClockEffect extends BaseEffectView {
    private AlarmCover alarmClock = null;
    private int clock_circle_offset = 50;
    private RandomDataGenerator generator = null;
    private float[] lmh = new float[3];

    public class RandomDataGenerator extends Thread {
        private long duration = 2000;
        private int maxHAmplitude = 16;
        private int maxLAmplitude = 128;
        private int maxMAmplitude = 64;
        private SecureRandom random = new SecureRandom();
        private boolean running = false;
        private long startTime = 0;

        @SuppressLint({"TrulyRandom"})
        public RandomDataGenerator(GLBase v) {
        }

        public void calc() {
            long curTime = SystemClock.elapsedRealtime();
            long delta = curTime - this.startTime;
            if (delta <= this.duration) {
                updating(((float) delta) / ((float) this.duration));
            } else {
                this.startTime = curTime - (delta - this.duration);
                this.duration = (long) (this.random.nextInt(2000) + 1000);
                this.maxLAmplitude = 128 - this.random.nextInt(64);
                this.maxMAmplitude = 64 - this.random.nextInt(32);
                this.maxHAmplitude = 16 - this.random.nextInt(8);
            }
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (this.running) {
                calc();
            }
        }

        public void generate() {
            this.running = true;
            this.startTime = SystemClock.elapsedRealtime();
            super.start();
        }

        private void updating(float fraction) {
            double radian = Math.toRadians((double) (360.0f * fraction));
            float amplitudeRatio2 = ((float) (Math.cos(0.7853981633974483d + radian) + 1.0d)) / 2.0f;
            float amplitudeRatio3 = ((float) (Math.cos(1.5707963267948966d + radian) + 1.0d)) / 2.0f;
            TestDemos$AlarmClockEffect.this.lmh[0] = ((float) this.maxLAmplitude) * (((float) (Math.sin(radian) + 1.0d)) / 2.0f);
            TestDemos$AlarmClockEffect.this.lmh[1] = ((float) this.maxMAmplitude) * amplitudeRatio2;
            TestDemos$AlarmClockEffect.this.lmh[2] = ((float) this.maxHAmplitude) * amplitudeRatio3;
        }

        public float[] getLMH() {
            return TestDemos$AlarmClockEffect.this.lmh;
        }

        public void stopRunning() {
            this.running = false;
        }
    }

    public void createView(GLBase base, Group group) {
        int sizew;
        int sizeh;
        if (Utils.isLandScreen(DeskClockApplication.getDeskClockApplication())) {
            this.clock_circle_offset = 0;
            sizew = (int) (((float) group.getWidth()) * 1.23f);
            sizeh = (int) (((float) group.getWidth()) * 1.23f);
        } else if (DeskClockApplication.isBtvPadDevice()) {
            this.clock_circle_offset = 0;
            sizew = (int) (((float) group.getWidth()) * 0.85f);
            sizeh = (int) (((float) group.getWidth()) * 0.72f);
        } else {
            sizew = (int) (((float) group.getWidth()) * 0.8f);
            sizeh = (int) (((float) group.getWidth()) * 0.8f);
        }
        this.alarmClock = new AlarmCover(base, sizew, sizeh);
        this.alarmClock.setLocation(((float) (group.getWidth() - sizew)) / 2.0f, (((float) (group.getHeight() - sizeh)) / 2.0f) + ((float) this.clock_circle_offset));
        base.add(this.alarmClock);
        group.setAlpha(0.0f);
        this.generator = new RandomDataGenerator(base);
        this.generator.generate();
        this.alarmClock.setAdapter(new DataAdapter() {
            public float[] getLMH() {
                return TestDemos$AlarmClockEffect.this.generator.getLMH();
            }
        });
    }

    public void destroyView(GLBase base, Group group) {
        if (this.generator != null) {
            this.generator.stopRunning();
        }
    }
}
