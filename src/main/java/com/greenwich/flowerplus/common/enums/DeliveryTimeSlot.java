package com.greenwich.flowerplus.common.enums;

import lombok.Getter;

@Getter
public enum DeliveryTimeSlot {
    SLOT_MORNING("09:00 - 12:00"),
    SLOT_NOON("12:00 - 13:00"),
    SLOT_AFTERNOON("13:00 - 17:00"),
    SLOT_EVENING("18:00 - 20:00");

    private final String timeLabel;

    DeliveryTimeSlot(String timeLabel) {
        this.timeLabel = timeLabel;
    }
}
