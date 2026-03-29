package com.trading.depthcharts;

import com.trading.depthcharts.model.Player;
import com.trading.depthcharts.model.Sport;
import com.trading.depthcharts.service.DepthChartManager;

import java.util.List;

public class DepthChartApplication {
    public static void main(String[] args) {
        
        DepthChartManager manager = new DepthChartManager(Sport.NFL, "TB", "Tampa Bay Buccaneers");
        
        // initialize Players
        Player tomBrady = new Player(12, "Tom Brady");
        Player blaineGabbert = new Player(11, "Blaine Gabbert");
        Player kyleTrask = new Player(2, "Kyle Trask");

        Player mikeEvans = new Player(13, "Mike Evans");
        Player jaelonDarden = new Player(1, "Jaelon Darden");
        Player scottMiller = new Player(10, "Scott Miller");

        // add Players to Depth Chart
        System.out.println("--- Adding Players ---");
        manager.addPlayerToDepthChart("QB", tomBrady, 0);
        manager.addPlayerToDepthChart("QB", blaineGabbert, 1);
        manager.addPlayerToDepthChart("QB", kyleTrask, 2);

        manager.addPlayerToDepthChart("LWR", mikeEvans, 0);
        manager.addPlayerToDepthChart("LWR", jaelonDarden, 1);
        manager.addPlayerToDepthChart("LWR", scottMiller, 2);
        System.out.println("Players added successfully.\n");

        // test getBackups for various scenarios
        System.out.println("--- Testing getBackups ---");
        printBackups("QB", tomBrady, manager.getBackups("QB", tomBrady));
        
        // *Fixed typo in PDF: Jaelon Darden is LWR, not QB
        printBackups("LWR", jaelonDarden, manager.getBackups("LWR", jaelonDarden)); 
        
        printBackups("QB", mikeEvans, manager.getBackups("QB", mikeEvans)); 
        
        printBackups("QB", blaineGabbert, manager.getBackups("QB", blaineGabbert));
        printBackups("QB", kyleTrask, manager.getBackups("QB", kyleTrask));
        System.out.println();

        // print Full Depth Chart
        System.out.println("--- Full Depth Chart (Before Removal) ---");
        manager.getFullDepthChart();
        System.out.println();

        // test Removal 
        System.out.println("--- Testing removePlayerFromDepthChart ---");
        // *Fixed typo from PDF: Removing from LWR instead of WR
        List<Player> removed = manager.removePlayerFromDepthChart("LWR", mikeEvans);
        System.out.println("Removed: " + removed);
        System.out.println();

        // print Full Depth Chart Again
        System.out.println("--- Full Depth Chart (After Removal) ---");
        manager.getFullDepthChart();
    }

    // Helper method just to format the console output nicely for our test
    private static void printBackups(String position, Player player, List<Player> backups) {
        if (backups.isEmpty()) {
            System.out.println("Backups for " + player.name() + ": <NO LIST>");
        } else {
            System.out.println("Backups for " + player.name() + ": " + backups);
        }
    }
}
