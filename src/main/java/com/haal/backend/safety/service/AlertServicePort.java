package com.haal.backend.safety.service;

import com.haal.backend.safety.entity.CheckIn;
import com.haal.backend.safety.entity.SosEvent;

public interface AlertServicePort {
    void sendMissedCheckInAlert(CheckIn overDueCheckIn);
    void sendSosAlert(SosEvent sosEvent);
}
