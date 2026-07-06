package com.smartopd.repository;

import com.smartopd.model.Alert;
import com.smartopd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByUserOrderBySentAtDesc(User user);

    List<Alert> findByIsDeliveredFalse();

    long countByUser(User user);
}