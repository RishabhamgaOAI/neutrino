package com.observeai.platform.realtime.neutrino.scheduler;

import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.CallState;
import com.observeai.platform.realtime.neutrino.data.store.CallRepository;
import com.observeai.platform.realtime.neutrino.redis.RedisValueStore;
import com.observeai.platform.realtime.neutrino.service.CallStateManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class CallCleanerScheduledTask {

    private final ExecutorService executorService = Executors.newFixedThreadPool(20);
    private final CallRepository callRepository;
    private final CallStateManager callStateManager;
    private final RedisValueStore redisValueStore;

    @Async
    @Scheduled(fixedRateString = "300000")
    public void scheduleFixedRateTaskAsync() {

        Set<Call> calls = callRepository.getCalls();
        for (Call call : calls) {
            if (call == null)
                continue;

            if (canBeEndedForcefully(call)) {
                executorService.submit(() -> endCall(call));
            }
        }
    }

    private void endCall(Call call){
        log.info("Forcefully ending call for ObserveCallId: {}, vendorCallId: {}, accountId:{}, userId:{}",
                call.getObserveCallId(), call.getStartMessage().getVendorCallId(), call.getStartMessage().getAccountId(), call.getStartMessage().getAgentId());
        callStateManager.updateState(call, CallState.ENDED);
        callRepository.removeCall(call);
        log.info("Ended call forcefully for ObserveCallId: {}, vendorCallId: {}, accountId:{}, userId:{}",
                call.getObserveCallId(), call.getStartMessage().getVendorCallId(), call.getStartMessage().getAccountId(), call.getStartMessage().getAgentId());
    }

    private boolean canBeEndedForcefully(Call call){
        return isCallConsiderable(call) && isCallActive(call) && isCallEligibleForEnd(call);
    }

    private boolean isCallConsiderable(Call call){
        return "FIVE9".equals(call.getVendor()) && !call.isSecondaryStream();
    }

    private boolean isCallActive(Call call){
        return call.getStateTransitions().contains(CallState.ACTIVE_PROCESSING) && !call.getStateTransitions().contains(CallState.ENDED);
    }

    private boolean isCallEligibleForEnd(Call call){
        String key = call.getObserveCallId() + "-" + "keep-alive-timestamp";
        Optional<Long> lastKeepAliveTimestamp = redisValueStore.get(key, Long.class);
        if (!lastKeepAliveTimestamp.isPresent()) {
            return false;
        }
        long currentTime = System.currentTimeMillis();

        return (currentTime - lastKeepAliveTimestamp.get()) > 5 * 60 * 1000;
    }

}
