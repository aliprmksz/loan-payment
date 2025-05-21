package com.ing.loan.payment.repository;



import com.ing.loan.payment.model.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long> {
    List<LoanInstallment> findByLoanIdOrderByDueDate(Long loanId);
    List<LoanInstallment> findByLoanIdAndIsPaidFalseOrderByDueDate(Long loanId);
}
