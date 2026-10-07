package com.packrat.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packrat.backend.entity.Invite;

public interface InviteRepository extends JpaRepository<Invite, UUID>{

    
} 
