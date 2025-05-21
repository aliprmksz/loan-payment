package com.ing.loan.payment.repository;



import com.ing.loan.payment.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByCustomerId(Long customerId);
    List<Loan> findByCustomerIdAndNumberOfInstallments(Long customerId, int numberOfInstallments);
    List<Loan> findByCustomerIdAndIsPaid(Long customerId, boolean isPaid);
    List<Loan> findByCustomerIdAndNumberOfInstallmentsAndIsPaid(Long customerId, int numberOfInstallments, boolean isPaid);
}
