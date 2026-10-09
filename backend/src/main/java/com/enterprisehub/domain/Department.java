package com.enterprisehub.domain;
import jakarta.persistence.*;
@Entity @Table(name="departments") public class Department { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @Column(nullable=false,unique=true) public String name; @Column(nullable=false,unique=true) public String code; protected Department(){} public Department(String name,String code){this.name=name;this.code=code;} }
