package com.ing.loan.payment.dto;



import java.time.LocalDate;

public class LoanResponse {
    private Long id;
    private Long customerId;

    private double loanAmount;
    private int numberOfInstallments;
    private LocalDate createDate;
    private boolean isPaid;

    public LoanResponse(Long id, Long customerId, double loanAmount, int numberOfInstallments, LocalDate createDate, boolean isPaid) {
        this.id = id;
        this.customerId = customerId;
        this.loanAmount = loanAmount;
        this.numberOfInstallments = numberOfInstallments;
        this.createDate = createDate;
        this.isPaid = isPaid;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public int getNumberOfInstallments() {
        return numberOfInstallments;
    }

    public LocalDate getCreateDate() {
        return createDate;
    }

    public boolean isPaid() {
        return isPaid;
    }
}
