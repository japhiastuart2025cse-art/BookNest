package com.booknest.backend.dto;

public class FinesSummaryDTO {
    private double pendingTotal;
    private double dailyRate;
    private int gracePeriodDays;
    private int defaultersCount;
    private double totalSettled;

    public FinesSummaryDTO() {
    }

    public FinesSummaryDTO(double pendingTotal, double dailyRate, int gracePeriodDays, int defaultersCount, double totalSettled) {
        this.pendingTotal = pendingTotal;
        this.dailyRate = dailyRate;
        this.gracePeriodDays = gracePeriodDays;
        this.defaultersCount = defaultersCount;
        this.totalSettled = totalSettled;
    }

    public double getPendingTotal() {
        return pendingTotal;
    }

    public void setPendingTotal(double pendingTotal) {
        this.pendingTotal = pendingTotal;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(double dailyRate) {
        this.dailyRate = dailyRate;
    }

    public int getGracePeriodDays() {
        return gracePeriodDays;
    }

    public void setGracePeriodDays(int gracePeriodDays) {
        this.gracePeriodDays = gracePeriodDays;
    }

    public int getDefaultersCount() {
        return defaultersCount;
    }

    public void setDefaultersCount(int defaultersCount) {
        this.defaultersCount = defaultersCount;
    }

    public double getTotalSettled() {
        return totalSettled;
    }

    public void setTotalSettled(double totalSettled) {
        this.totalSettled = totalSettled;
    }
}
