package ecom.market.throttlingserver.controllers;

import ecom.market.throttlingserver.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@Slf4j
public class MainController {

    @Value("${request.limit.params.max_number_of_ip_requests_in_x_minutes}")
    private int MAX_NUMBER_OF_IP_REQUESTS_IN_X_MINUTES;
    @Value("${request.limit.params.time_limit_in_minutes}")
    private long TIME_LIMIT_IN_MINUTES;

    class RequestsRateInTime {
        volatile AtomicInteger numberOfIpRequestsInXMinutes;

        public void setTimestampOfCurrentNumberOfRequests(OffsetDateTime timestampOfCurrentNumberOfRequests) {
            this.timestampOfCurrentNumberOfRequests = timestampOfCurrentNumberOfRequests;
        }

        volatile OffsetDateTime timestampOfCurrentNumberOfRequests;

        public RequestsRateInTime(int numberOfIpRequestsInXMinutes, OffsetDateTime timestampOfCurrentNumberOfRequests) {
            this.numberOfIpRequestsInXMinutes = new AtomicInteger(numberOfIpRequestsInXMinutes);
            this.timestampOfCurrentNumberOfRequests = timestampOfCurrentNumberOfRequests;
        }

        public boolean checkIfRequestsNumberExceeded(int maxNumberOfIpRequestsInXMinutes) {
            return numberOfIpRequestsInXMinutes.get() > maxNumberOfIpRequestsInXMinutes ? true : false;
        }
    }

    private final ConcurrentHashMap<String, RequestsRateInTime> mapIpToNumberOfIpCalls = new ConcurrentHashMap<>();

    @GetMapping("/test_throttling_controller")
    public ResponseEntity<String> handleGetRequest() {
        log.info("MainController.handleGetRequest() started");

        log.info("MAX_NUMBER_OF_IP_REQUESTS_IN_X_MINUTES: {}", MAX_NUMBER_OF_IP_REQUESTS_IN_X_MINUTES);
        log.info("TIME_LIMIT_IN_MINUTES: {}", TIME_LIMIT_IN_MINUTES);

        String remoteAddress = HttpUtils.getRemoteIP(
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest());
        log.info("Request remoteAddress: {}", remoteAddress);

        mapIpToNumberOfIpCalls.putIfAbsent(remoteAddress, new RequestsRateInTime(0,
                OffsetDateTime.now(Clock.systemUTC())));

        //saving this RequestsRateInTime for further use regardless of its possible flushing
        //in this or concurrent threads:
        var numberOfCallsAndLastCallTimestamp = mapIpToNumberOfIpCalls.get(remoteAddress);
        log.info("numberOfIpRequestsInXMinutes: {}",
                numberOfCallsAndLastCallTimestamp.numberOfIpRequestsInXMinutes.incrementAndGet());
        mapIpToNumberOfIpCalls.put(remoteAddress, numberOfCallsAndLastCallTimestamp);

        //flush requests number if the timeLimit is exceeded:
        if (Duration.between(numberOfCallsAndLastCallTimestamp.timestampOfCurrentNumberOfRequests,
                OffsetDateTime.now(Clock.systemUTC())).compareTo(Duration.ofMinutes(TIME_LIMIT_IN_MINUTES)) > 0) {
            mapIpToNumberOfIpCalls.put(remoteAddress, new RequestsRateInTime(0,
                    OffsetDateTime.now(Clock.systemUTC())));
        }

        if (numberOfCallsAndLastCallTimestamp.checkIfRequestsNumberExceeded(MAX_NUMBER_OF_IP_REQUESTS_IN_X_MINUTES)) {
            //NOTE: using .build() to return empty body here, see: https://stackoverflow.com/a/51735848
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } else {
            return ResponseEntity.status(HttpStatus.OK).build();
        }
    }
}
