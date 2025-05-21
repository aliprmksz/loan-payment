package com.ing.loan.payment.dto;

import lombok.Data;

@Data
public class PayloanRequest {
    private Long loanId;
    private double amount;

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}