package com.itemstorage.dto;

public class DashboardStats {

    private long operationsToday;
    private int activeInventories;
    private int issuedToday;
    private int dischargedCount;
    private long occupiedCells;
    private long freeCells;
    private long totalCells;

    public DashboardStats() {}

    public DashboardStats(long operationsToday, int activeInventories, int issuedToday,
                          int dischargedCount, long occupiedCells, long freeCells, long totalCells) {
        this.operationsToday = operationsToday;
        this.activeInventories = activeInventories;
        this.issuedToday = issuedToday;
        this.dischargedCount = dischargedCount;
        this.occupiedCells = occupiedCells;
        this.freeCells = freeCells;
        this.totalCells = totalCells;
    }

    public long getOperationsToday() { return operationsToday; }
    public void setOperationsToday(long v) { this.operationsToday = v; }

    public int getActiveInventories() { return activeInventories; }
    public void setActiveInventories(int v) { this.activeInventories = v; }

    public int getIssuedToday() { return issuedToday; }
    public void setIssuedToday(int v) { this.issuedToday = v; }

    public int getDischargedCount() { return dischargedCount; }
    public void setDischargedCount(int v) { this.dischargedCount = v; }

    public long getOccupiedCells() { return occupiedCells; }
    public void setOccupiedCells(long v) { this.occupiedCells = v; }

    public long getFreeCells() { return freeCells; }
    public void setFreeCells(long v) { this.freeCells = v; }

    public long getTotalCells() { return totalCells; }
    public void setTotalCells(long v) { this.totalCells = v; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private long operationsToday;
        private int activeInventories;
        private int issuedToday;
        private int dischargedCount;
        private long occupiedCells;
        private long freeCells;
        private long totalCells;

        public Builder operationsToday(long v) { this.operationsToday = v; return this; }
        public Builder activeInventories(int v) { this.activeInventories = v; return this; }
        public Builder issuedToday(int v) { this.issuedToday = v; return this; }
        public Builder dischargedCount(int v) { this.dischargedCount = v; return this; }
        public Builder occupiedCells(long v) { this.occupiedCells = v; return this; }
        public Builder freeCells(long v) { this.freeCells = v; return this; }
        public Builder totalCells(long v) { this.totalCells = v; return this; }

        public DashboardStats build() {
            return new DashboardStats(operationsToday, activeInventories, issuedToday,
                    dischargedCount, occupiedCells, freeCells, totalCells);
        }
    }
}