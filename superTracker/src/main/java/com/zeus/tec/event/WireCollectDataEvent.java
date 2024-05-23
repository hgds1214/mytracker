package com.zeus.tec.event;

import com.zeus.tec.device.tracker.CompassData;

public class WireCollectDataEvent {
    public CompassData compassData;

    public WireCollectDataEvent(CompassData compassData) {
        this.compassData = compassData;
    }
}
