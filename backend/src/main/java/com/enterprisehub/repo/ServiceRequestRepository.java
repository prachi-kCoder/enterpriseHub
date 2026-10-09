package com.enterprisehub.repo;
import com.enterprisehub.domain.ServiceRequest; import java.time.Instant; import java.util.List; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest,Long>,JpaSpecificationExecutor<ServiceRequest>{ java.util.Optional<ServiceRequest> findByIdempotencyKey(String idempotencyKey); List<ServiceRequest> findByResolvedAtIsNullAndSlaDueAtBeforeAndStatusNotIn(Instant now,List<String> excludedStatuses); }
