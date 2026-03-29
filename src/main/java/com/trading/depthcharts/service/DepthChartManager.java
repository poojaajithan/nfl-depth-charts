package com.trading.depthcharts.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.trading.depthcharts.exception.DepthChartException;
import com.trading.depthcharts.model.Player;
import com.trading.depthcharts.model.Position;
import com.trading.depthcharts.model.Sport;

/**
 * A service class responsible for managing the depth chart of a sports team.
 * * <p>This manager provides functionality to add players to specific positions,
 * gracefully handle depth reassignments, remove active players, and query backups.
 * * <p><b>Architecture Note:</b> 
 * State is maintained in-memory using a {@link java.util.LinkedHashMap} to preserve 
 * the insertion order of positions for deterministic console output. 
 * * <p><b>Thread Safety:</b>
 * This current implementation relies on standard collections and is not thread-safe. 
 * If accessed concurrently by multiple threads, external synchronization is required.
 *
 * @author pajithan
 */

public class DepthChartManager {
    private final Map<String, List<Player>> depthChart;
    private final String teamNameShort;
    private final String teamNameLong;
    private final Sport sport;

    public DepthChartManager(Sport sport, String teamNameShort, String teamNameLong) {
        if (sport == null) {
            throw new DepthChartException("Sport type is required to initialize depth chart.");
        }
        
        if (teamNameShort == null || teamNameShort.trim().length() < 2 || teamNameShort.trim().length() > 3) {
            throw new DepthChartException("Invalid team short name. Must be 2-3 characters.");
        }
        
        if (teamNameLong == null || teamNameLong.trim().isEmpty()) {
            throw new DepthChartException("Team long name cannot be null or empty.");
        }

        this.sport = sport;
        this.teamNameShort = teamNameShort.trim().toUpperCase();
        this.teamNameLong = teamNameLong.trim();
        this.depthChart = new LinkedHashMap<>();
    }
    
    /**
     * Adds a player to the depth chart at a given position.
     * @param position      The position (e.g., "QB", "LWR")
     * @param player        The Player object
     * @param positionDepth The 0-indexed depth. If null, appends to the end of the chart.
     * @throws DepthChartException if roster limits are exceeded or data is invalid.
     */

    public void addPlayerToDepthChart(String position, Player player, Integer positionDepth) {
        String formatPosition = validatePosition(position);
        Objects.requireNonNull(player, "Player cannot be null");

        if (positionDepth != null) {
            if (positionDepth < 0) {
                throw new DepthChartException("Position depth cannot be negative. Provided: " + positionDepth);
            }
            if (positionDepth >= sport.getMaxDepthPerPosition()) {
                throw new DepthChartException("Requested depth " + positionDepth + " exceeds the maximum allowed depth of " + sport.getMaxDepthPerPosition() + "for " + sport.name());
            }
        }

        List<Player> playersList = depthChart.computeIfAbsent(formatPosition, k -> new ArrayList<>());
        
        if (getTotalPlayersCount() >= sport.getMaxRosterSize() && !playersList.contains(player)){
            throw new DepthChartException("Maximum roster limit of " + sport.getMaxRosterSize() + " reached.");
        }
        
        if (playersList.size() >= sport.getMaxDepthPerPosition() && !playersList.contains(player)){
            throw new DepthChartException("Maximum depth of " + sport.getMaxDepthPerPosition() + " reached for position: " + formatPosition);
        }

        // if player present, remove it first to prevent a duplicate entry
        playersList.remove(player);

        if (positionDepth == null || positionDepth > playersList.size()){
            playersList.add(player);
            logAction("ADD", player, formatPosition, "Appended to end of chart.");
        }
        else{
            playersList.add(positionDepth, player);
            logAction("ADD", player, formatPosition, "Inserted at depth " + positionDepth + ".");
        }
    }

    /**
     * Removes a specified player from the depth chart for a given position.
     * @param position The position from which the player should be removed (e.g., "LWR", "QB").
     * @param player   The Player object to be removed. Equality is determined by the player's unique number.
     * @return A List containing the removed Player instance if successful. 
     * Returns an empty List if the position does not exist, or if the player is not found at that position.
     */
    public List<Player> removePlayerFromDepthChart(String position, Player player){
        String formatPosition = validatePosition(position);
        Objects.requireNonNull(player, "Player cannot be null");

        List<Player> playersList = depthChart.getOrDefault(formatPosition, List.of());
        if (playersList.isEmpty()){
            return List.of();
        }
        
        int removePlayerIndex = playersList.indexOf(player);
        if (removePlayerIndex >= 0){
            Player removedPlayer = playersList.remove(removePlayerIndex);
            logAction("REMOVE", player, formatPosition, "Removed successfully.");

            if (playersList.isEmpty()) {
                depthChart.remove(formatPosition);
            }

            return List.of(removedPlayer);
        }
        
        return List.of();
    }

    /**
     * Retrieves a list of backup players for a specific player at a given position.
     * A backup is defined as any player with a lower position depth (i.e., listed after 
     * the specified player in the depth chart).
     * @param position The position to query (e.g., "QB", "LWR").
     * @param player   The Player object for whom backups are being requested.
     * @return A safely encapsulated List of backup Players. 
     * Returns an empty List if the position does not exist, the player is not 
     * listed at that position, or the player has no backups.
     */
    public List<Player> getBackups(String position, Player player){
        String formatPosition = validatePosition(position);
        Objects.requireNonNull(player, "Player cannot be null");

        List<Player> playersList = depthChart.getOrDefault(formatPosition, List.of());
        
        if (playersList.isEmpty()){
            logAction("QUERY", player, formatPosition, "No active players found at position.");
            return List.of();
        }

        int playerIndex = playersList.indexOf(player);
        if (playerIndex >= 0){
            List<Player> backups = new ArrayList<>(playersList.subList(playerIndex + 1, playersList.size()));
            logAction("QUERY", player, formatPosition, "Found " + backups.size() + " backup(s).");
            return backups;
        }

        logAction("QUERY", player, formatPosition, "Player is not currently listed at this position.");
        return List.of();
    }

    /**
     * Prints the full depth chart to the console.
     * Iterates through all tracked positions and prints the active players 
     * in their correct depth order. Positions are printed in the order 
     * they were initially added to the system.
     * Positions with no active players are skipped to maintain clean output.
     */
    public void getFullDepthChart(){
        
        System.out.println("=== " + teamNameLong + " (" + sport + ") Depth Chart ===");        
        
        if (depthChart.isEmpty()) {
            System.out.println("The depth chart is currently empty.");
            return;
        }

        for(Map.Entry<String, List<Player>> entry : depthChart.entrySet()){
            String position = entry.getKey();
            List<Player> players = entry.getValue();

            //if position is empty, skip printing it
            if (players.isEmpty()){
                continue;
            }

            String formattedPlayersOutput = players.stream()
                                                    .map(Player::toString)
                                                    .collect(Collectors.joining(", "));
                                                    
            System.out.println(position + " - " + formattedPlayersOutput);
        }
    }

    /**
     * Validates and sanitizes the position string.
     * @param position The raw position string.
     * @return The trimmed, uppercase version of the position.
     * @throws DepthChartException if the position is null or empty
     */
    private String validatePosition(String position) {
        if (position == null || position.isBlank()) { 
            throw new DepthChartException("Position cannot be null or empty");
        }
        
        String formatPosition =  position.trim().toUpperCase();
        try{
            Position.valueOf(formatPosition); 
            return formatPosition;
        }
        catch(IllegalArgumentException e)
        {
            throw new DepthChartException("Invalid position: " + formatPosition + " for sport: " + sport);
        }
    }

    private int getTotalPlayersCount() {
        return depthChart.values()
                        .stream()
                        .mapToInt(List::size)
                        .sum();     
    }

    private void logAction(String action, Player player, String position, String details) {
        System.out.printf("[%s] %s | %s at %s | %s%n", 
            teamNameShort, action, player.name(), position, details);
    }
}
