package com.enterprisehub.repo;
import com.enterprisehub.domain.*; import org.junit.jupiter.api.Test; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest; import org.springframework.dao.OptimisticLockingFailureException; import org.springframework.transaction.PlatformTransactionManager; import org.springframework.transaction.TransactionDefinition; import org.springframework.transaction.support.TransactionTemplate; import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest @org.springframework.transaction.annotation.Transactional(propagation=org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED) @org.springframework.test.context.TestPropertySource(properties={"spring.flyway.enabled=false","spring.jpa.hibernate.ddl-auto=create-drop"}) class ServiceRequestConcurrencyTest {
 @Autowired ServiceRequestRepository requests; @Autowired EmployeeRepository employees; @Autowired DepartmentRepository departments; @Autowired PlatformTransactionManager transactions;
 @Test void staleConcurrentUpdateIsRejectedByVersionColumn(){
  var department=departments.save(new Department("Concurrency Test","CT")); var employee=employees.save(new Employee("Test Employee","concurrency@example.test","hash","EMPLOYEE",department));
  var request=new ServiceRequest();request.title="Original";request.description="Concurrency test";request.category="IT";request.priority="P3";request.priorityReason="test";request.status="OPEN";request.requester=employee;request.department=department;request.slaDueAt=java.time.Instant.now().plusSeconds(3600);request=requests.saveAndFlush(request);Long id=request.id;
  var tx=new TransactionTemplate(transactions);tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  ServiceRequest stale=tx.execute(status->requests.findById(id).orElseThrow());
  tx.executeWithoutResult(status->{var current=requests.findById(id).orElseThrow();current.title="First writer";requests.saveAndFlush(current);});
  stale.title="Stale writer";
  assertThrows(OptimisticLockingFailureException.class,()->tx.executeWithoutResult(status->{requests.saveAndFlush(stale);}));
 }
}
