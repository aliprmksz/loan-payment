package com.ing.loan.payment.service;


import com.ing.loan.payment.dto.CreateLoanRequest;
import com.ing.loan.payment.dto.LoanResponse;
import com.ing.loan.payment.dto.PayloanRequest;
import com.ing.loan.payment.model.Customer;
import com.ing.loan.payment.model.Loan;
import com.ing.loan.payment.model.LoanInstallment;
import com.ing.loan.payment.repository.CustomerRepository;
import com.ing.loan.payment.repository.LoanInstallmentRepository;
import com.ing.loan.payment.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoanServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanInstallmentRepository installmentRepository;

    @InjectMocks
    private LoanService loanService;

    private Customer customer;
    private Loan loan;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        customer = new Customer();
        customer.setId(1L);
        customer.setCreditLimit(10000);
        customer.setUsedCreditLimit(0);
        customer.setUsername("testuser");

        loan = new Loan();
        loan.setId(1L);
        loan.setCustomer(customer);
        loan.setLoanAmount(1200);
        loan.setNumberOfInstallments(12);
        loan.setCreateDate(LocalDate.now());
        loan.setPaid(false);
    }

    @Test
    void createLoan_success() {
        CreateLoanRequest request = new CreateLoanRequest();
        request.setCustomerId("1");
        request.setAmount(1000);
        request.setInterestRate(0.2);
        request.setNumberOfInstallments(12);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        when(installmentRepository.save(any(LoanInstallment.class))).thenReturn(new LoanInstallment());

        LoanResponse result = loanService.createLoan(request);

        assertNotNull(result);
        assertEquals(1200, result.getLoanAmount());
        verify(customerRepository).save(customer);
        verify(loanRepository).save(any(Loan.class));
        verify(installmentRepository, times(12)).save(any(LoanInstallment.class));
    }

    @Test
    void createLoan_insufficientCreditLimit() {
        CreateLoanRequest request = new CreateLoanRequest();
        request.setCustomerId("1");
        request.setAmount(10000);
        request.setInterestRate(0.2);
        request.setNumberOfInstallments(12);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertThrows(RuntimeException.class, () -> loanService.createLoan(request));
    }

    @Test
    void payLoan_success_remain_installment() {
        PayloanRequest request = new PayloanRequest();
        request.setLoanId(1L);
        request.setAmount(300);

        LoanInstallment installment1 = new LoanInstallment();
        installment1.setId(1L);
        installment1.setLoan(loan);
        installment1.setAmount(100);
        installment1.setDueDate(LocalDate.now().plusMonths(1));
        installment1.setPaid(false);

        LoanInstallment installment2 = new LoanInstallment();
        installment2.setId(2L);
        installment2.setLoan(loan);
        installment2.setAmount(100);
        installment2.setDueDate(LocalDate.now().plusMonths(2));
        installment2.setPaid(false);

        LoanInstallment installment3 = new LoanInstallment();
        installment3.setId(3L);
        installment3.setLoan(loan);
        installment3.setAmount(100);
        installment3.setDueDate(LocalDate.now().plusMonths(3));
        installment3.setPaid(false);


        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(installmentRepository.findByLoanIdAndIsPaidFalseOrderByDueDate(1L))
                .thenReturn(Arrays.asList(installment1, installment2, installment3));

        var response = loanService.payLoan(request);


        LocalDate currentDate = LocalDate.now();
        long daysDifference1 = ChronoUnit.DAYS.between(currentDate, installment1.getDueDate());
        long daysDifference2 = ChronoUnit.DAYS.between(currentDate, installment2.getDueDate());
        double adjustment1 = 100 * 0.001 * daysDifference1;
        double adjustment2 = 100 * 0.001 * daysDifference2;
        double expectedAmount1 = 100 - adjustment1;
        double expectedAmount2 = 100 - adjustment2;
        double expectedTotal = expectedAmount1 + expectedAmount2 ;

        assertEquals(2, response.getInstallmentsPaid());
        assertEquals(expectedTotal, response.getTotalAmountSpent(), 0.01);
        assertFalse(response.isLoanPaid());
        verify(installmentRepository).saveAll(anyList());
    }

    @Test
    void payLoan_success_all_installment_paid() {
        PayloanRequest request = new PayloanRequest();
        request.setLoanId(1L);
        request.setAmount(300);

        LoanInstallment installment1 = new LoanInstallment();
        installment1.setId(1L);
        installment1.setLoan(loan);
        installment1.setAmount(100);
        installment1.setDueDate(LocalDate.now().plusMonths(1));
        installment1.setPaid(false);

        LoanInstallment installment2 = new LoanInstallment();
        installment2.setId(2L);
        installment2.setLoan(loan);
        installment2.setAmount(100);
        installment2.setDueDate(LocalDate.now().plusMonths(2));
        installment2.setPaid(false);

        LoanInstallment installment3 = new LoanInstallment();
        installment3.setId(3L);
        installment3.setLoan(loan);
        installment3.setAmount(100);
        installment3.setDueDate(LocalDate.now().plusMonths(2));
        installment3.setPaid(false);

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(installmentRepository.findByLoanIdAndIsPaidFalseOrderByDueDate(1L))
                .thenReturn(Arrays.asList(installment1, installment2, installment3));

        var response = loanService.payLoan(request);


        LocalDate currentDate = LocalDate.now();
        long daysDifference1 = ChronoUnit.DAYS.between(currentDate, installment1.getDueDate());
        long daysDifference2 = ChronoUnit.DAYS.between(currentDate, installment2.getDueDate());
        long daysDifference3 = ChronoUnit.DAYS.between(currentDate, installment3.getDueDate());
        double adjustment1 = 100 * 0.001 * daysDifference1;
        double adjustment2 = 100 * 0.001 * daysDifference2;
        double adjustment3 = 100 * 0.001 * daysDifference3;
        double expectedAmount1 = 100 - adjustment1;
        double expectedAmount2 = 100 - adjustment2;
        double expectedAmount3 = 100 - adjustment3;
        double expectedTotal = expectedAmount1 + expectedAmount2 + expectedAmount3;

        assertEquals(3, response.getInstallmentsPaid());
        assertEquals(expectedTotal, response.getTotalAmountSpent(), 0.01);
        assertTrue(response.isLoanPaid());
        verify(installmentRepository).saveAll(anyList());
    }
}