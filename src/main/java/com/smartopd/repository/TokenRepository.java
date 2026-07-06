package com.smartopd.repository;

import com.smartopd.enums.TokenStatus;
import com.smartopd.model.Department;
import com.smartopd.model.Token;
import com.smartopd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    List<Token> findByUserOrderByBookedAtDesc(User user);

    List<Token> findByDepartmentAndStatus(Department department, TokenStatus status);

    boolean existsByUserAndStatusIn(User user, List<TokenStatus> statuses);

    Optional<Token> findByTokenNumber(String tokenNumber);

    @Query("SELECT COUNT(t) FROM Token t WHERE t.department = :department AND t.bookedAt >= :startOfDay")
    int countTodayTokensByDepartment(Department department, LocalDateTime startOfDay);
}
