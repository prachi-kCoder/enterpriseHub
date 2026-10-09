package com.enterprisehub.service;
import com.enterprisehub.domain.SlaBreachAlert; import com.enterprisehub.repo.SlaBreachAlertRepository; import com.enterprisehub.repo.ServiceRequestRepository; import java.time.Instant; import org.springframework.dao.DataIntegrityViolationException; import org.springframework.scheduling.annotation.Scheduled; import org.springframework.stereotype.Component; import org.springframework.transaction.annotation.Transactional;
@Component public class SlaBreachMonitor {
 private final ServiceRequestRepository requests; private final SlaBreachAlertRepository alerts;
 public SlaBreachMonitor(ServiceRequestRepository requests,SlaBreachAlertRepository alerts){this.requests=requests;this.alerts=alerts;}
 @Scheduled(fixedDelayString="${app.sla-monitor-interval-ms:60000}") @Transactional public void recordNewBreaches(){for(var r:requests.findByResolvedAtIsNullAndSlaDueAtBeforeAndStatusNotIn(Instant.now(),java.util.List.of("RESOLVED","REJECTED"))){if(alerts.existsByRequestId(r.id))continue;try{alerts.saveAndFlush(new SlaBreachAlert(r.id));}catch(DataIntegrityViolationException ignored){/* another scheduler instance recorded it */}}}
}
