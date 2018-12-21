package org.onap.ccsdk.apps.controllerblueprints.service.utils;

public enum CbaStateEnum {

    DRAFT(0), VALIDATED(1), APPROVED(2);
    int state;

    CbaStateEnum(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
