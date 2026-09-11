package com.restaurantus.erp.dao.mysql;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restaurantus.erp.domain.LoginAttempt;

public interface LoginAttemptDAO extends JpaRepository<LoginAttempt, Long> {
}
