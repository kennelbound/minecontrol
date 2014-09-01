package com.joshjcarrier.minecontrol.framework.profiles;

import com.joshjcarrier.rxautomation.methods.MouseMoveAutomationRunner;

public class PrimaryMouseProfile implements IMouseProfile {
    public int getSensitivityX() {
        return MouseMoveAutomationRunner.PRIMARY_SENSITIVITY_X;
    }

    public int getSensitivityY() {
        return MouseMoveAutomationRunner.PRIMARY_SENSITIVITY_Y;
    }

    public boolean isInvertY() {
        return MouseMoveAutomationRunner.INVERT_Y;
    }

    public void setSensitivityX(int value) {
        MouseMoveAutomationRunner.PRIMARY_SENSITIVITY_X = value;
    }

    public void setSensitivityY(int value) {
        MouseMoveAutomationRunner.PRIMARY_SENSITIVITY_Y = value;
    }

    public void setInvertY(boolean value) {
        MouseMoveAutomationRunner.INVERT_Y = value;
    }
}
