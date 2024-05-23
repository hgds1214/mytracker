package com.zeus.tec.event;

import com.zeus.tec.device.tracker.AdjustResultData;
import com.zeus.tec.device.tracker.AdjustResultV2Data;

public class AdjustResultEvent {
    public AdjustResultData resultData;
    public AdjustResultV2Data arv2Data;

    public AdjustResultEvent(AdjustResultV2Data arv2Data) {
        this.arv2Data = arv2Data;
    }

    public AdjustResultEvent(AdjustResultData resultData) {
        this.resultData = resultData;
    }
}
