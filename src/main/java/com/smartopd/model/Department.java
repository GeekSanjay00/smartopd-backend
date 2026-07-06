package com.smartopd.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // General, Eye, Surgery, Ortho

    private String description;

    private boolean isActive = true;

    private int avgWaitMinutes = 0;

    private int totalTokensToday = 0;

    private int currentServing = 0;
}
