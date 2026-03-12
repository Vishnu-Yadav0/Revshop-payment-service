package com.revshop.payment.repository;

import com.revshop.payment.model.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentsRepository extends JpaRepository<Payments, Integer> {
    List<Payments> findByOrderId(Integer orderId);
    Optional<Payments> findByTransactionId(String transactionId);
}
