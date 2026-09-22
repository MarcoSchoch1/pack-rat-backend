package com.packrat.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Item {
    
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne @JoinColumn(name = "collection_id")
    private Collection collection;
    private String name;
    @Column(nullable = false)
    private BigDecimal pricePaid;
    @Nullable  
    private BigDecimal priceNow; 
    private Currency currency;
    private LocalDate dateAquired;
    @Enumerated(EnumType.STRING)
    private Condition condition;
    private String marketPlaceLink;
    @CreationTimestamp  
    private LocalDateTime createdAt;
    @UpdateTimestamp 
    private LocalDateTime updatedAt;
}
