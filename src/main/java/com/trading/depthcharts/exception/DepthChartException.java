package com.trading.depthcharts.exception;

/**
 * Base exception for depth chart operations.
 *
 * <p>Thrown when a depth chart action fails because of invalid state or conditions
 * specific to managing player depth charts.</p>
 * 
 *  @author pajithan
 */

public class DepthChartException extends RuntimeException {
    public DepthChartException(String message) {
        super(message);
    }

}
