package com.maintainx.expense_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FundSummaryResponse {

    private Double totalCollection;

    private Double totalExpenses;

    private Double remainingFund;
}