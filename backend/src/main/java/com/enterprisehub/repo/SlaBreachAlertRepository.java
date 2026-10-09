package com.enterprisehub.repo;
import com.enterprisehub.domain.SlaBreachAlert; import org.springframework.data.jpa.repository.JpaRepository;
public interface SlaBreachAlertRepository extends JpaRepository<SlaBreachAlert,Long>{boolean existsByRequestId(Long requestId);}
