package com.fsocial.postservice;

import com.fsocial.postservice.scheduler.InterestDecayScheduler;
import com.fsocial.postservice.services.InterestGraphService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterestDecaySchedulerTest {

    @Mock
    private InterestGraphService interestGraphService;

    @InjectMocks
    private InterestDecayScheduler scheduler;

    @Test
    @DisplayName("Scheduler calls applyDecay with correct BRD parameters")
    void applyDailyDecay_callsServiceWithCorrectParams() {
        scheduler.applyDailyDecay();
        verify(interestGraphService).applyDecay(0.95, 0.1);
    }

    @Test
    @DisplayName("Scheduler does not throw if service throws")
    void applyDailyDecay_gracefulOnError() {
        doThrow(new RuntimeException("MongoDB error")).when(interestGraphService)
                .applyDecay(anyDouble(), anyDouble());
        // Should not propagate
        scheduler.applyDailyDecay();
        verify(interestGraphService).applyDecay(0.95, 0.1);
    }
}
