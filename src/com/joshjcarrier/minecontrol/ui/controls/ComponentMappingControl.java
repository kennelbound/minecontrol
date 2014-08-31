package com.joshjcarrier.minecontrol.ui.controls;

import com.joshjcarrier.minecontrol.framework.input.AutomationBinding;
import com.joshjcarrier.minecontrol.framework.input.ButtonMapping;
import com.joshjcarrier.minecontrol.framework.input.ButtonMappingType;
import com.joshjcarrier.minecontrol.ui.actions.SimpleAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collection;

public class ComponentMappingControl extends JPanel {
    private static final long serialVersionUID = -3729294863824556950L;

    private final JComboBox buttonMappingComboBox;
    private final JCheckBox keyToggleModeCheckBox;
    private final ButtonMapping unboundMapping = new ButtonMapping(ButtonMappingType.Unbound, 0);
    private Action updateAction;

    public ComponentMappingControl(Collection<AutomationBinding> bindings) {
        this.setLayout(new GridBagLayout());
        GridBagConstraints gridConstraints = new GridBagConstraints();

        gridConstraints.gridx = 0;
        gridConstraints.weightx = 1;
        ArrayList<AutomationBinding> selectableButtonMappings = new ArrayList<AutomationBinding>();
        //selectableButtonMappings.add(unboundMapping);
        selectableButtonMappings.addAll(bindings);
        this.buttonMappingComboBox = new JComboBox(selectableButtonMappings.toArray());
//        this.buttonMappingComboBox.setSelectedItem(unboundMapping);
        this.buttonMappingComboBox.setAction(new SimpleAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ButtonMapping mapping = (ButtonMapping)buttonMappingComboBox.getSelectedItem();

                // toggle mode only enabled for keyboard
                if(mapping.getMappingType() != ButtonMappingType.Keyboard)
                {
                    keyToggleModeCheckBox.setEnabled(false);
                    keyToggleModeCheckBox.setSelected(false);
                }
                else
                {
                    // resync toggle mode state
                    keyToggleModeCheckBox.setSelected(mapping.isToggleMode());
                    keyToggleModeCheckBox.setEnabled(true);
                }

                notifyItemStateChanged();
            }
        });
        this.add(this.buttonMappingComboBox, gridConstraints);

        gridConstraints.gridx = 1;
        gridConstraints.weighty = 0;
        this.keyToggleModeCheckBox = new JCheckBox();
        this.keyToggleModeCheckBox.setAction(new SimpleAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ((ButtonMapping)buttonMappingComboBox.getSelectedItem()).setIsToggleMode(keyToggleModeCheckBox.isSelected());
                notifyItemStateChanged();
            }
        });
        this.keyToggleModeCheckBox.setText("Toggle mode");
        this.keyToggleModeCheckBox.setToolTipText("When selected, the mapped key will continue to be held until the button is pressed again.");
        this.add(this.keyToggleModeCheckBox, gridConstraints);
    }

    public void setAction(Action a)
    {
        this.updateAction = a;
    }

    private void notifyItemStateChanged()
    {
        if (this.updateAction != null)
        {
            this.updateAction.actionPerformed(new ActionEvent(this, ItemEvent.ITEM_STATE_CHANGED, ""));
        }
    }
}
