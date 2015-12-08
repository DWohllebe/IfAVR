package com.vr.daso.ifavr;

/**
 * Created by Daniel on 08.12.2015.
 */
public interface Animated {
    void AnimationStep(float[] model);
    void startAnimation();
    void stopAnimation();
    void pauseAnimation();
}
