package com.fyusion.sdk.camera;

import java.util.Objects;

/* compiled from: Unknown */
public final class MotionHints {
    private Hint a;

    /* compiled from: Unknown */
    public enum Hint {
        MOTION_LEFT,
        MOTION_UP,
        MOTION_RIGHT,
        MOTION_DOWN,
        MOVING_BACKWARDS,
        MOVING_TOO_FAST,
        MOVING_CORRECTLY
    }

    public MotionHints(Hint hint) {
        this.a = hint;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(this.a, ((MotionHints) obj).a);
    }

    public Hint getMotionHint() {
        return this.a;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.a});
    }

    public String toString() {
        return "MotionHints{motionHint=" + this.a + '}';
    }
}
