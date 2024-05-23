package com.zeus.tec.event;

import com.zeus.tec.device.tracker.TrackerCollectData;

public class TrackerCollectDataEvent {
    public TrackerCollectData data;

    public TrackerCollectDataEvent(TrackerCollectData data) {
        this.data = data;
    }
}
