package com.smartopd.model;

import com.smartopd.enums.TokenStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tokenNumber; // MQ-GEN-042

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    private TokenStatus status = TokenStatus.WAITING;

    private LocalDateTime bookedAt;

    private LocalDateTime servedAt;

    private int position;

    @PrePersist
    public void prePersist() {
        bookedAt = LocalDateTime.now();
    }
}
