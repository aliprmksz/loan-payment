package com.ing.loan.payment.dto;


import java.time.LocalDate;

public class LoanInstallmentDTO {
    private Long id;

    private Long loanId;

    private double amount;
    private double paidAmount;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private boolean isPaid;

    public LoanInstallmentDTO(Long id, Long loanId, double amount, double paidAmount, LocalDate dueDate, LocalDate paymentDate, boolean isPaid) {
        this.id = id;
        this.loanId = loanId;
        this.amount = amount;
        this.paidAmount = paidAmount;
        this.dueDate = dueDate;
        this.paymentDate = paymentDate;
        this.isPaid = isPaid;
    }

    public Long getId() {
        return id;
    }

    public Long getLoanId() {
        return loanId;
    }

    public double getAmount() {
        return amount;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public boolean isPaid() {
        return isPaid;
    }
}
