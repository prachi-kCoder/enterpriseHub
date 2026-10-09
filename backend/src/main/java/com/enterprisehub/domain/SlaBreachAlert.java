package com.enterprisehub.domain;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="sla_breach_alerts", uniqueConstraints=@UniqueConstraint(name="uk_sla_alert_request",columnNames="request_id"))
public class SlaBreachAlert { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @Column(name="request_id",nullable=false,unique=true) public Long requestId; @Column(name="created_at",nullable=false,updatable=false) public Instant createdAt=Instant.now(); protected SlaBreachAlert(){} public SlaBreachAlert(Long requestId){this.requestId=requestId;} }
