package com.vr.daso.ifavr;

/**
 * Created by ASUS-X56T on 23.12.2015.
 */
public class Interactor {
    glDrawable[] parent;

    Interactor(glDrawable[] _parent) {
        parent = _parent;
    }

    Interactor() {
    }

    void onLookedAt() {}
    void onClicked() {}
    void onLookDiscontinued() {}

    void setParent(glDrawable[] _parent) {
        parent = _parent;
    }
}
