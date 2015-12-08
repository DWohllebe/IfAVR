package com.vr.daso.ifavr;

/**
 * Created by Daniel on 08.12.2015.
 */
public class Animator implements Animated {
    boolean animationEnabled = false;
    boolean animationPaused = false;

    public void AnimationStep(float[] _model) {}  // override this method

    public void pauseAnimation() {
        animationPaused = true;
    }
    public void startAnimation() {
        animationEnabled = true;
        animationPaused = false;
    }
    public void stopAnimation() {
        animationEnabled = false;
    }

    public boolean isPaused() {
        return animationPaused;
    }

    public boolean isEnabled() {
        return animationEnabled;
    }
}
