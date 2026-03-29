package com.trading.depthcharts.model;

public enum Sport {
    NFL(53, 5), 
    NBA(15, 3), 
    MLB(26, 3);

    private final int maxRosterSize;
    private final int maxDepthPerPosition;

    Sport(int maxRosterSize, int maxDepthPerPosition) {
        this.maxRosterSize = maxRosterSize;
        this.maxDepthPerPosition = maxDepthPerPosition;
    }

    public int getMaxRosterSize() {
        return maxRosterSize;
    }

    public int getMaxDepthPerPosition() {
        return maxDepthPerPosition;
    }
}
