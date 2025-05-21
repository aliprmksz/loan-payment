package com.ing.loan.payment.dto;

import lombok.Data;

public class PayloanResponse {
    private int installmentsPaid;
    private double totalAmountSpent;
    private boolean isLoanPaid;

    public PayloanResponse(int installmentsPaid, double totalAmountSpent, boolean isLoanPaid) {
        this.installmentsPaid = installmentsPaid;
        this.totalAmountSpent = totalAmountSpent;
        this.isLoanPaid = isLoanPaid;
    }

    public int getInstallmentsPaid() {
        return installmentsPaid;
    }

    public void setInstallmentsPaid(int installmentsPaid) {
        this.installmentsPaid = installmentsPaid;
    }

    public double getTotalAmountSpent() {
        return totalAmountSpent;
    }

    public void setTotalAmountSpent(double totalAmountSpent) {
        this.totalAmountSpent = totalAmountSpent;
    }

    public boolean isLoanPaid() {
        return isLoanPaid;
    }

    public void setLoanPaid(boolean loanPaid) {
        isLoanPaid = loanPaid;
    }
}
