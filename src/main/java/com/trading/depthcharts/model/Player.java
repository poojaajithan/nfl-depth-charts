package com.trading.depthcharts.model;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import com.trading.depthcharts.exception.InvalidPlayerException;

/**
 * Represents a single player on a depth chart.
 *
 * <p>Each player has a jersey number and a properly cased name. Validation ensures the
 * number is within the NFL range and the name is not null, blank, or excessively long.</p>
 * 
 *  @author pajithan
 */

public record Player (int number, String name) {

    public Player {

        if (number < 0 || number > 99) {
            throw new InvalidPlayerException("Player number must be between 0 and 99. Provided: " + number);
        }

        Objects.requireNonNull(name, "Player name cannot be null");
        if (name.isBlank()) {
            throw new InvalidPlayerException("Player name cannot be empty or just whitespace");
        }

        String trimmedName = name.trim();
        if (trimmedName.length() < 2 || trimmedName.length() > 50) {
            throw new InvalidPlayerException("Player name length must be between 2 and 50 characters");
        }

        //convert name to proper case 
        name = formatToProperCase(trimmedName);
    }

    private static String formatToProperCase(String input) {
        return Arrays.stream(input.split("\\s+")) 
                     .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                     .collect(Collectors.joining(" "));
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(number); 
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Player other)) 
            return false;
        return number == other.number;
    }

    @Override
    public String toString() {
        return "(#" + number + ", " + name + ")";
    }
    
}
