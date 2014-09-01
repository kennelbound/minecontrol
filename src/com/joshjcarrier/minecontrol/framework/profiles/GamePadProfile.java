package com.joshjcarrier.minecontrol.framework.profiles;

import com.joshjcarrier.minecontrol.ui.models.MouseProfileWrapper;
import com.joshjcarrier.persistence.IStorage;
import com.joshjcarrier.rxautomation.methods.*;
import com.joshjcarrier.rxautomation.persistence.AutomationReader;
import com.joshjcarrier.rxautomation.persistence.AutomationWriter;
import com.joshjcarrier.rxautomation.persistence.IAutomationReader;
import com.joshjcarrier.rxautomation.persistence.IAutomationWriter;
import com.joshjcarrier.rxautomation.projection.BimodalRxAutomationProjection;
import com.joshjcarrier.rxautomation.projection.IRxAutomationProjection;
import com.joshjcarrier.rxautomation.projection.ReplayRxAutomationProjection;
import com.joshjcarrier.rxautomation.projection.ThresholdRxAutomationProjection;
import com.joshjcarrier.rxgamepad.RxGamePad;
import javafx.util.Pair;
import net.java.games.input.Component;
import rx.Observable;
import rx.Subscription;
import rx.util.functions.Action1;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GamePadProfile {
    private final String name;
    private final RxGamePad rxGamePad;
    private final IStorage storage;

    private Subscription activeSubscription;
    private MouseMoveAutomationRunner mouseMoveAutomationRunner = new MouseMoveAutomationRunner();
    private IMouseProfile primaryMouseProfile;
    private IMouseProfile secondaryMouseProfile;

    public GamePadProfile(String name, RxGamePad rxGamePad, IStorage storage){
        this.name = name;
        this.rxGamePad = rxGamePad;
        this.storage = storage;
        this.primaryMouseProfile = new PrimaryMouseProfile();
        this.secondaryMouseProfile = new SecondaryMouseProfile();

        // TODO move and terminate thread elsewhere
        Thread t = new Thread(mouseMoveAutomationRunner);
        t.start();
    }

    public Observable<Pair<IAutomationMethod, Float>> getKeyEvents() {
        ArrayList<Observable<Pair<IAutomationMethod, Float>>> keyEvents = new ArrayList<Observable<Pair<IAutomationMethod, Float>>>();

        for(Map.Entry<Component.Identifier, IAutomationMethod> entry : automationMethodHashMap.entrySet()) {

            IRxAutomationProjection projector = identifierToProjectionMap.get(entry.getKey());
            Observable<Pair<IAutomationMethod, Float>> keyEvent = projector.map(entry.getValue(), this.rxGamePad.getComponentById(entry.getKey()));
            keyEvents.add(keyEvent);
        }

        return Observable.merge(keyEvents);
    }

    public void activate(){
        if(this.activeSubscription != null && !this.activeSubscription.isUnsubscribed()) {
            return;
        }

        this.activeSubscription = getKeyEvents().subscribe(new Action1<Pair<IAutomationMethod, Float>>() {
            @Override
            public void call(Pair<IAutomationMethod, Float> iAutomationMethodFloatPair) {
                iAutomationMethodFloatPair.getKey().automate(iAutomationMethodFloatPair.getValue());
            }
        });
    }

    public void deactivate() {
        if(this.activeSubscription != null) {
            this.activeSubscription.unsubscribe();
        }
    }

    public IAutomationMethod getAutomationMethod(Component.Identifier identifier) {
        IAutomationMethod method = this.automationMethodHashMap.get(identifier);
        if(method == null) {
            method = new NoOpAutomationMethod();
            this.automationMethodHashMap.put(identifier, method);
            this.identifierToProjectionMap.put(identifier, new ThresholdRxAutomationProjection());
        }

        return method;
    }

    public HashMap<Component.Identifier, String> getGamePadButtonLabels() {
        return this.rxGamePad.getButtonLabels();
    }

    public String getName() {
        return this.name;
    }

    public IMouseProfile getPrimaryMouseProfile() {
        return this.primaryMouseProfile;
    }

    public IMouseProfile getSecondaryMouseProfile() {
        return this.secondaryMouseProfile;
    }

    public void restore() {
        for(Map.Entry<Component.Identifier, IAutomationMethod> identifierAutomationMethodEntry : this.automationMethodHashMap.entrySet()) {
            IAutomationReader automationReader = new AutomationReader(getName(), "bind." + identifierAutomationMethodEntry.getKey().toString(), this.storage);

            // chain of command
            IAutomationMethod automationMethod = KeyboardAutomationMethod.load(automationReader);
            if(automationMethod == null) {
                automationMethod = MouseButtonAutomationMethod.load(automationReader);
            }

            if(automationMethod == null) {
                automationMethod = MouseWheelAutomationMethod.load(automationReader);
            }

            if(automationMethod == null) {
                automationMethod = SensitivityAppAutomationMethod.load(automationReader);
            }

            if(automationMethod == null) {
                automationMethod = NoOpAutomationMethod.load(automationReader);
            }

            if(automationMethod != null) {
                this.automationMethodHashMap.put(identifierAutomationMethodEntry.getKey(), automationMethod);
            }
        }
    }

    public void save() {
        for(Map.Entry<Component.Identifier, IAutomationMethod> identifierAutomationMethodEntry : this.automationMethodHashMap.entrySet()) {
            IAutomationWriter writer = new AutomationWriter(getName(), "bind." + identifierAutomationMethodEntry.getKey().toString(), this.storage);
            identifierAutomationMethodEntry.getValue().save(writer);
        }

        this.storage.commit();
    }

    public void setAutomationMethod(Component.Identifier identifier, IAutomationMethod automationMethod) {
        automationMethodHashMap.put(identifier, automationMethod);
        deactivate();
        activate();
        save();
    }

    private HashMap<Component.Identifier, IRxAutomationProjection> identifierToProjectionMap = new HashMap<Component.Identifier, IRxAutomationProjection>()
    {
        public static final long serialVersionUID = 8658388604108766926L;
        {
            put(Component.Identifier.Button._0, new ThresholdRxAutomationProjection());
            put(Component.Identifier.Button._1, new ThresholdRxAutomationProjection());
            put(Component.Identifier.Button._2, new ThresholdRxAutomationProjection());
            put(Component.Identifier.Button._3, new ThresholdRxAutomationProjection());
            put(Component.Identifier.Button._4, new ThresholdRxAutomationProjection());
            put(Component.Identifier.Button._5, new ThresholdRxAutomationProjection());
            put(Component.Identifier.Button._6, new ThresholdRxAutomationProjection());
            put(Component.Identifier.Button._7, new ThresholdRxAutomationProjection());
            put(Component.Identifier.Button._8, new ThresholdRxAutomationProjection());
            put(Component.Identifier.Button._9, new ThresholdRxAutomationProjection());

            put(Component.Identifier.Axis.RX, new BimodalRxAutomationProjection());
            put(Component.Identifier.Axis.RY, new BimodalRxAutomationProjection());

            put(Component.Identifier.Axis.X, new ReplayRxAutomationProjection());
            put(Component.Identifier.Axis.Y, new ReplayRxAutomationProjection());
            put(Component.Identifier.Axis.Z, new ThresholdRxAutomationProjection());
        }
    };

    private HashMap<Component.Identifier, IAutomationMethod> automationMethodHashMap = new HashMap<Component.Identifier, IAutomationMethod>()
    {
        public static final long serialVersionUID = 4658388604108766926L;
        {
            put(Component.Identifier.Button._0, new KeyboardAutomationMethod(KeyEvent.VK_SPACE));
            put(Component.Identifier.Button._1, new KeyboardAutomationMethod(KeyEvent.VK_Q));
            put(Component.Identifier.Button._2, new MouseButtonAutomationMethod(KeyEvent.BUTTON2_MASK));
            put(Component.Identifier.Button._3, new KeyboardAutomationMethod(KeyEvent.VK_E));
            put(Component.Identifier.Button._4, new MouseWheelAutomationMethod(-1));
            put(Component.Identifier.Button._5, new MouseWheelAutomationMethod(1));
            put(Component.Identifier.Button._6, new SensitivityAppAutomationMethod());
            put(Component.Identifier.Button._7, new KeyboardAutomationMethod(KeyEvent.VK_ESCAPE));
            put(Component.Identifier.Button._8, new KeyboardAutomationMethod(KeyEvent.VK_SHIFT));
            put(Component.Identifier.Button._9, new KeyboardAutomationMethod(KeyEvent.VK_SPACE));

            put(Component.Identifier.Axis.RX, mouseMoveAutomationRunner.getXAutomationMethod());
            put(Component.Identifier.Axis.RY, mouseMoveAutomationRunner.getYAutomationMethod());

            put(Component.Identifier.Axis.X, new KeyboardAutomationMethod(KeyEvent.VK_D, KeyEvent.VK_A));
            put(Component.Identifier.Axis.Y, new KeyboardAutomationMethod(KeyEvent.VK_S, KeyEvent.VK_W));
            put(Component.Identifier.Axis.Z, new MouseButtonAutomationMethod(KeyEvent.BUTTON3_MASK));
        }
    };
}
