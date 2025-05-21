package com.ing.loan.payment.service;


import com.ing.loan.payment.dto.CreateLoanRequest;
import com.ing.loan.payment.dto.LoanInstallmentDTO;
import com.ing.loan.payment.dto.LoanResponse;
import com.ing.loan.payment.dto.PayloanRequest;
import com.ing.loan.payment.dto.PayloanResponse;
import com.ing.loan.payment.model.Customer;
import com.ing.loan.payment.model.Loan;
import com.ing.loan.payment.model.LoanInstallment;
import com.ing.loan.payment.repository.CustomerRepository;
import com.ing.loan.payment.repository.LoanInstallmentRepository;
import com.ing.loan.payment.repository.LoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
public class LoanService {

    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository installmentRepository;
    private static final List<Integer> VALID_INSTALLMENTS = Arrays.asList(6, 9, 12, 24);

    public LoanService(CustomerRepository customerRepository,
                       LoanRepository loanRepository,
                       LoanInstallmentRepository installmentRepository) {
        this.customerRepository = customerRepository;
        this.loanRepository = loanRepository;
        this.installmentRepository = installmentRepository;
    }

    @Transactional
    public LoanResponse createLoan(CreateLoanRequest request) {
        validateCreateLoanRequest(request);

        Customer customer = customerRepository.findById(Long.valueOf(request.getCustomerId()))
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        double totalLoanAmount = request.getAmount() * (1 + request.getInterestRate());
        if (customer.getCreditLimit() - customer.getUsedCreditLimit() < totalLoanAmount) {
            throw new RuntimeException("Credit limit is not sufficient");
        }

        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setLoanAmount(totalLoanAmount);
        loan.setNumberOfInstallments(request.getNumberOfInstallments());
        loan.setCreateDate(LocalDate.now());
        loan.setPaid(false);
        loanRepository.save(loan);

        double installmentAmount = totalLoanAmount / request.getNumberOfInstallments();
        LocalDate nextMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1);

        for (int i = 0; i < request.getNumberOfInstallments(); i++) {
            LoanInstallment installment = new LoanInstallment();
            installment.setLoan(loan);
            installment.setAmount(installmentAmount);
            installment.setPaidAmount(0);
            installment.setDueDate(nextMonth.plusMonths(i));
            installment.setPaid(false);
            installmentRepository.save(installment);
        }

        customer.setUsedCreditLimit(customer.getUsedCreditLimit() + totalLoanAmount);
        customerRepository.save(customer);

        return new LoanResponse(
                loan.getId(),
                customer.getId(),
                loan.getLoanAmount(),
                loan.getNumberOfInstallments(),
                loan.getCreateDate(),
                loan.isPaid()
        );
    }

    public List<LoanResponse> listLoans(String customerId, Integer numberOfInstallments, Boolean isPaid) {
        Long id = Long.valueOf(customerId);
        if (numberOfInstallments != null && isPaid != null) {
            return loanRepository.findByCustomerIdAndNumberOfInstallmentsAndIsPaid(id, numberOfInstallments, isPaid)
                    .stream().map(loan ->
                    new LoanResponse(
                            loan.getId(),
                            loan.getCustomer().getId(),
                            loan.getLoanAmount(),
                            loan.getNumberOfInstallments(),
                            loan.getCreateDate(),
                            loan.isPaid()
                    )).toList();
        } else if (numberOfInstallments != null) {
            return loanRepository.findByCustomerIdAndNumberOfInstallments(id, numberOfInstallments).stream().map(loan ->
                    new LoanResponse(
                            loan.getId(),
                            loan.getCustomer().getId(),
                            loan.getLoanAmount(),
                            loan.getNumberOfInstallments(),
                            loan.getCreateDate(),
                            loan.isPaid()
                    )).toList();
        } else if (isPaid != null) {
            return loanRepository.findByCustomerIdAndIsPaid(id, isPaid).stream().map(loan ->
                    new LoanResponse(
                            loan.getId(),
                            loan.getCustomer().getId(),
                            loan.getLoanAmount(),
                            loan.getNumberOfInstallments(),
                            loan.getCreateDate(),
                            loan.isPaid()
                    )).toList();
        }
        return loanRepository.findByCustomerId(id).stream().map(loan ->
            new LoanResponse(
                    loan.getId(),
                    loan.getCustomer().getId(),
                    loan.getLoanAmount(),
                    loan.getNumberOfInstallments(),
                    loan.getCreateDate(),
                    loan.isPaid()
            )).toList();
    }

    public List<LoanInstallmentDTO> listInstallments(Long loanId) {
        return installmentRepository.findByLoanIdOrderByDueDate(loanId).stream().map(installment ->
                new LoanInstallmentDTO(
                        installment.getId(),
                        installment.getLoan().getId(),
                        installment.getAmount(),
                        installment.getPaidAmount(),
                        installment.getDueDate(),
                        installment.getPaymentDate(),
                        installment.isPaid()
                )).toList();
    }

    @Transactional
    public PayloanResponse payLoan(PayloanRequest request) {
        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        List<LoanInstallment> installments = installmentRepository.findByLoanIdAndIsPaidFalseOrderByDueDate(request.getLoanId());

        double requestAmount = request.getAmount();
        int installmentsPaid = 0;
        double totalSpent = 0;
        LocalDate currentDate = LocalDate.now();
        LocalDate maxDueDate = currentDate.plusMonths(3).withDayOfMonth(1);

        for (LoanInstallment installment : installments) {
            if (installment.getDueDate().isAfter(maxDueDate)) {
                break;
            }

            double adjustedAmount = calculateAdjustedAmount(installment, currentDate);
            if (requestAmount >= adjustedAmount) {
                installment.setPaid(true);
                installment.setPaidAmount(adjustedAmount);
                installment.setPaymentDate(currentDate);
                requestAmount -= adjustedAmount;
                totalSpent += adjustedAmount;
                installmentsPaid++;
            } else {
                break;
            }
        }

        installmentRepository.saveAll(installments);

        boolean isLoanPaid = installments.stream().allMatch(LoanInstallment::isPaid);
        if (isLoanPaid) {
            loan.setPaid(true);
            Customer customer = loan.getCustomer();
            customer.setUsedCreditLimit(customer.getUsedCreditLimit() - loan.getLoanAmount());
            customerRepository.save(customer);
            loanRepository.save(loan);
        }

        return new PayloanResponse(installmentsPaid, totalSpent, isLoanPaid);
    }

    private void validateCreateLoanRequest(CreateLoanRequest request) {
        if (!VALID_INSTALLMENTS.contains(request.getNumberOfInstallments())) {
            throw new RuntimeException("Invalid number of installments. Must be 6, 9, 12, or 24.");
        }
        if (request.getInterestRate() < 0.1 || request.getInterestRate() > 0.5) {
            throw new RuntimeException("Interest rate must be between 0.1 and 0.5.");
        }
        if (request.getAmount() <= 0) {
            throw new RuntimeException("Loan amount must be positive.");
        }
    }

    private double calculateAdjustedAmount(LoanInstallment installment, LocalDate currentDate) {
        long daysDifference = ChronoUnit.DAYS.between(currentDate, installment.getDueDate());
        double adjustment = installment.getAmount() * 0.001 * Math.abs(daysDifference);
        return daysDifference > 0 ? installment.getAmount() - adjustment : installment.getAmount() + adjustment;
    }

    public boolean isLoanOwner(Long loanId, String username) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        return loan.getCustomer().getUsername().equals(username);
    }

    public boolean isCustomerOwner(Long customerId, String username) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return customer.getUsername().equals(username);
    }
}
