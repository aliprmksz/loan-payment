package com.ing.loan.payment.controller;


import com.ing.loan.payment.dto.CreateLoanRequest;
import com.ing.loan.payment.dto.LoanInstallmentDTO;
import com.ing.loan.payment.dto.LoanResponse;
import com.ing.loan.payment.dto.PayloanRequest;
import com.ing.loan.payment.dto.PayloanResponse;
import com.ing.loan.payment.model.Loan;
import com.ing.loan.payment.model.LoanInstallment;
import com.ing.loan.payment.service.LoanService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER')and @loanService.isCustomerOwner(#request.customerId, principal))")
    public LoanResponse createLoan(@RequestBody CreateLoanRequest request) {
        return loanService.createLoan(request);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and @loanService.isCustomerOwner(#customerId, principal))")
    public List<LoanResponse> listLoans(@PathVariable String customerId,
                                @RequestParam(required = false) Integer numberOfInstallments,
                                @RequestParam(required = false) Boolean isPaid) {
        return loanService.listLoans(customerId, numberOfInstallments, isPaid);
    }

    @GetMapping("/{loanId}/installments")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and @loanService.isLoanOwner(#loanId, principal))")
    public List<LoanInstallmentDTO> listInstallments(@PathVariable Long loanId) {
        return loanService.listInstallments(loanId);
    }

    @PostMapping("/pay")
    @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and @loanService.isLoanOwner(#request.loanId, principal))")
    public PayloanResponse payLoan(@RequestBody PayloanRequest request) {
        return loanService.payLoan(request);
    }
}
