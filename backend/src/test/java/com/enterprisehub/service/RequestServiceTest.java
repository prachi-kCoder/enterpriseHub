package com.enterprisehub.service;

import com.enterprisehub.domain.Employee;
import com.enterprisehub.domain.ServiceRequest;
import com.enterprisehub.repo.AuditRepository;
import com.enterprisehub.repo.DepartmentRepository;
import com.enterprisehub.repo.EmployeeRepository;
import com.enterprisehub.repo.ServiceRequestRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RequestServiceTest {
    private RequestService service(ServiceRequestRepository requests, EmployeeRepository employees, JdbcTemplate jdbc) {
        return new RequestService(requests, employees, mock(DepartmentRepository.class), mock(AuditRepository.class), jdbc);
    }

    @Test void priorityRulesMapImpactAndUrgencyToFourLevels() {
        assertEquals("P1", RequestService.priorityFor("CRITICAL", "HIGH"));
        assertEquals("P1", RequestService.priorityFor("HIGH", "CRITICAL"));
        assertEquals("P2", RequestService.priorityFor("HIGH", "MEDIUM"));
        assertEquals("P2", RequestService.priorityFor("MEDIUM", "HIGH"));
        assertEquals("P3", RequestService.priorityFor("MEDIUM", "LOW"));
        assertEquals("P4", RequestService.priorityFor("LOW", "LOW"));
    }

    @Test void slaTargetsAreFourEightTwentyFourAndSeventyTwoHours() {
        assertEquals(4 * 3600L, RequestService.slaSeconds("P1"));
        assertEquals(8 * 3600L, RequestService.slaSeconds("P2"));
        assertEquals(24 * 3600L, RequestService.slaSeconds("P3"));
        assertEquals(72 * 3600L, RequestService.slaSeconds("P4"));
    }

    @Test void slaStateChangesAtTwentyPercentAndCompletesOnResolution() {
        var start = java.time.Instant.parse("2026-10-09T00:00:00Z");
        var due = start.plusSeconds(10_000);
        assertEquals("ON_TRACK", RequestService.slaStatus(start, due, start.plusSeconds(7_999), false));
        assertEquals("AT_RISK", RequestService.slaStatus(start, due, start.plusSeconds(8_000), false));
        assertEquals("BREACHED", RequestService.slaStatus(start, due, due.plusSeconds(1), false));
        assertEquals("COMPLETED", RequestService.slaStatus(start, due, due.plusSeconds(1), true));
    }

    @Test void employeesCannotOverridePriority() {
        var request = new ServiceRequest(); request.id = 12L; request.priority = "P3";
        var employee = new Employee("Taylor", "taylor@example.test", "hash", "EMPLOYEE", null);
        var requests = mock(ServiceRequestRepository.class); when(requests.findById(12L)).thenReturn(Optional.of(request));
        var employees = mock(EmployeeRepository.class); when(employees.findByEmail(employee.email)).thenReturn(Optional.of(employee));
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> service(requests, employees, mock(JdbcTemplate.class)).overridePriority(12L, "HIGH", "HIGH", "Urgent customer impact", employee.email));
        verify(requests, never()).save(any());
    }

    @Test void employeesCannotResolveRequestsAssignedToSomeoneElse() {
        var request=new ServiceRequest(); request.id=16L; request.status="IN_PROGRESS"; request.priority="P2";
        var employee=new Employee("Taylor","taylor2@example.test","hash","EMPLOYEE",null);
        var other=new Employee("Alex","alex@example.test","hash","EMPLOYEE",null); other.id=17L; request.assignee=other;
        var requests=mock(ServiceRequestRepository.class); when(requests.findById(16L)).thenReturn(Optional.of(request));
        var employees=mock(EmployeeRepository.class); when(employees.findByEmail(employee.email)).thenReturn(Optional.of(employee));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->service(requests,employees,mock(JdbcTemplate.class)).action(16L,"resolve",null,employee.email,null));
        verify(requests,never()).save(any());
    }

    @Test void invalidWorkflowTransitionDoesNotPersist() {
        var request=new ServiceRequest(); request.id=18L; request.status="OPEN";
        var manager=new Employee("Jordan","manager2@example.test","hash","MANAGER",null);
        var requests=mock(ServiceRequestRepository.class); when(requests.findById(18L)).thenReturn(Optional.of(request));
        var employees=mock(EmployeeRepository.class); when(employees.findByEmail(manager.email)).thenReturn(Optional.of(manager));
        assertThrows(IllegalArgumentException.class,()->service(requests,employees,mock(JdbcTemplate.class)).action(18L,"approve",null,manager.email,null));
        verify(requests,never()).save(any());
    }

    @Test void paginationUsesStableCreatedTimeSortAndClampsPageSize() {
        var requests=mock(ServiceRequestRepository.class); when(requests.findAll(any(org.springframework.data.jpa.domain.Specification.class),any(org.springframework.data.domain.Pageable.class))).thenReturn(org.springframework.data.domain.Page.empty());
        var manager=new Employee("Jordan","manager@example.test","hash","MANAGER",null); var employees=mock(EmployeeRepository.class); when(employees.findByEmail(manager.email)).thenReturn(Optional.of(manager));
        var result=service(requests,employees,mock(JdbcTemplate.class)).list(null,null,null,-1,500,manager.email);
        assertEquals(0,result.getTotalElements());
        var page=org.mockito.ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(requests).findAll(any(org.springframework.data.jpa.domain.Specification.class),page.capture());
        assertEquals(0,page.getValue().getPageNumber()); assertEquals(100,page.getValue().getPageSize());
    }

    @Test void dashboardReturnsJdbcKpisAndStatusBreakdown() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForMap(anyString())).thenReturn(Map.of("open_requests", 4, "critical_requests", 1, "at_risk_requests", 2, "breached_requests", 1, "average_resolution_seconds", 1800));
        when(jdbc.queryForList(anyString())).thenReturn(java.util.List.of(Map.of("status", "OPEN", "total", 4)));
        var result = service(mock(ServiceRequestRepository.class), mock(EmployeeRepository.class), jdbc).analytics();
        assertEquals(4, result.get("open_requests"));
        assertEquals(1, result.get("critical_requests"));
        assertEquals(2, result.get("at_risk_requests"));
        assertEquals(1, result.get("breached_requests"));
        assertTrue(result.containsKey("byStatus"));
    }
}
