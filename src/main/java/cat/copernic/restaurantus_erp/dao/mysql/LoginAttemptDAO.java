package cat.copernic.restaurantus_erp.dao.mysql;

import org.springframework.data.jpa.repository.JpaRepository;

import cat.copernic.restaurantus_erp.domain.LoginAttempt;

public interface LoginAttemptDAO extends JpaRepository<LoginAttempt, Long> {
}
