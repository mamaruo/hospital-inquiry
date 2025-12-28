package com.mamaruo.hospitalinquiry.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 科室实体
 */
@Entity
@Table(name = "departments")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<DoctorProfile> doctors = new ArrayList<>();

    public Department() {}

    public Department(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DoctorProfile> getDoctors() {
        return doctors;
    }

    public void setDoctors(List<DoctorProfile> doctors) {
        this.doctors = doctors;
    }
}
