package com.packrat.backend.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity 
@Getter @Setter 
@NoArgsConstructor 
public class Invite {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(optional = false) @JoinColumn(name = "created_by_id")
    private User createdBy;
    @CreationTimestamp 
    private LocalDateTime createdAt;
    @ManyToOne @JoinColumn(name = "used_by_id")
    private User usedBy;
    private LocalDateTime usedAt;
    private LocalDateTime validUntil;
}
