package com.zeus.tec.event;

import com.zeus.tec.model.tracker.DrillHoleInfo;

public class MergeEvent {
    public DrillHoleInfo info;

    public MergeEvent(DrillHoleInfo info) {
        this.info = info;
    }
}
