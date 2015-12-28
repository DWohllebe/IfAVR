package com.vr.daso.ifavr;

import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by ASUS-X56T on 23.12.2015.
 */
public class Interactor implements Cloneable {
    glDrawable[] parent;
    int refIndex;
    final String TAG = "Interactor";

    Interactor(glDrawable[] _parent) {
        parent = _parent;
    }

    Interactor() {
    }

    Interactor(Interactor interactor) {
        // copy over all anonymous functions
//        int i = 0;
//        try {
//            Method method_a = this.getClass().getMethod("onLookedAt");
//            Method method_b = interactor.getClass().getMethod("onLookedAt");
//            method_a = method_b;
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        }
        try {
            Method[] methods = this.getClass().getMethods();
            Method[] new_methods = interactor.getClass().getMethods();
            methods = new_methods;
        }
        catch (ArrayIndexOutOfBoundsException e){
            Log.e(TAG, e.toString() );
        }
    }

    void onLookedAt() {
        Log.d(TAG, "onLookedAt() not defined!");
    }
    void onClicked() {
        Log.d(TAG, "onClickedAt() not defined!");
    }
    void onLookDiscontinued() {
        Log.d(TAG, "onLookDiscontinued() not defined!");
    }

    void setParent(glDrawable[] _parent) {
        parent = _parent;
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
