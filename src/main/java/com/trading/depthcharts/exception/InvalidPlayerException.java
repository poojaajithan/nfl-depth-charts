package com.trading.depthcharts.exception;

/**
 * Exception thrown when a player definition is invalid.
 *
 * <p>Used for invalid player numbers, names, or any player validation failure
 * encountered while managing the depth chart.</p>
 * 
 *  @author pajithan
 */

public class InvalidPlayerException extends IllegalArgumentException {
    public InvalidPlayerException(String message) {
        super(message);
    }

}
