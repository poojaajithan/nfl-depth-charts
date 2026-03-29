package com.trading.depthcharts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.trading.depthcharts.exception.DepthChartException;
import com.trading.depthcharts.model.Player;
import com.trading.depthcharts.model.Position;
import com.trading.depthcharts.model.Sport;
import com.trading.depthcharts.service.DepthChartManager;

public class DepthChartManagerTest {
    
    private DepthChartManager manager;
    private Player tomBrady;
    private Player blaineGabbert;
    private Player kyleTrask;
    private Player mikeEvans;
    private Player jaelonDarden;
    private Player scottMiller;
    private Player joshWells;

    @BeforeEach
    void setUp() {
        manager = new DepthChartManager(Sport.NFL, "TB", "Tampa Bay Buccaneers");

        tomBrady = new Player(12, "Tom Brady");
        blaineGabbert = new Player(11, "Blaine Gabbert");
        kyleTrask = new Player(2, "Kyle Trask");

        mikeEvans = new Player(13, "Mike Evans");
        jaelonDarden = new Player(1, "Jaelon Darden");
        scottMiller = new Player(10, "Scott Miller");

        joshWells = new Player(72, "Josh Wells");
    }

    @Test
    void testAddPlayer() {
        manager.addPlayerToDepthChart("QB", tomBrady, 0);

        List<Player> removed = manager.removePlayerFromDepthChart("QB", tomBrady);
        
        assertEquals(1, removed.size(), "Player should have been added successfully.");
        assertEquals(tomBrady, removed.get(0), "Removed player should be Tom Brady.");
    }

    @Test
    void testAddPlayerInsertMiddle() {
        manager.addPlayerToDepthChart("QB", tomBrady, 0);
        manager.addPlayerToDepthChart("QB", kyleTrask, 1); 

        //insert Blaine Gabbert at index 1 (this should push Kyle Trask down)
        manager.addPlayerToDepthChart("QB", blaineGabbert, 1);

        List<Player> backups = manager.getBackups("QB", tomBrady);
        assertEquals(2, backups.size());
        assertEquals(blaineGabbert, backups.get(0)); 
        assertEquals(kyleTrask, backups.get(1));
    }

    @Test
    void testAddPlayerInvalidPosition() {
        Exception exception = Assertions.assertThrows(
            DepthChartException.class, 
            () -> manager.addPlayerToDepthChart("ABC", tomBrady, 0)
        );
        
        assertTrue(exception.getMessage().contains("Invalid position: ABC for sport: NFL"), 
    "Exception message should mention the invalid position and sport.");
    }

    @Test
    void testAddPlayerInvalidPositionDepth() {
        manager.addPlayerToDepthChart("QB", tomBrady, null);

        Exception exception = Assertions.assertThrows(
            DepthChartException.class, 
            () -> manager.addPlayerToDepthChart("QB", blaineGabbert, 99)
        );

        assertTrue(exception.getMessage().contains("exceeds the maximum allowed depth"));
    }

    @Test
    void testAddPlayerNullPostion() {
        Exception exception = Assertions.assertThrows(
                        DepthChartException.class, 
                        () -> manager.addPlayerToDepthChart(null, tomBrady, 0)
        );
        assertEquals("Position cannot be null or empty", exception.getMessage());
    }

    @Test
    void testAddPlayerNullPlayer() {
        try 
        {
            manager.addPlayerToDepthChart("QB", null, 0);
            fail("Throw a NPE for null player");
        } 
        catch (NullPointerException e) 
        {
            assertEquals("Player cannot be null", e.getMessage());
        }
    }

    @Test
    void testAddPlayerAtNegativePositionDepth() {
        manager.addPlayerToDepthChart("QB", tomBrady, 0);

        Exception exception = Assertions.assertThrows(
            DepthChartException.class, 
            () -> manager.addPlayerToDepthChart("QB", blaineGabbert, -1)
        );

        assertTrue(exception.getMessage().contains("cannot be negative"),
            "The exception message should clearly state that depth cannot be negative.");
    }

    @Test
    void testAddPlayerMaxDepth() {
        manager.addPlayerToDepthChart("QB", tomBrady, 0);
        manager.addPlayerToDepthChart("QB", blaineGabbert, 1);
        manager.addPlayerToDepthChart("QB", kyleTrask, 2);
        manager.addPlayerToDepthChart("QB", new Player(3, "Backup Three"), 3);
        manager.addPlayerToDepthChart("QB", new Player(4, "Backup Four"), 4);

        Player extraPlayer = new Player(5, "Too Many");
        Exception exception = Assertions.assertThrows(
            DepthChartException.class, 
            () -> manager.addPlayerToDepthChart("QB", extraPlayer, 5)
        );

        assertTrue(exception.getMessage().contains("exceeds the maximum allowed depth"));    }

    @Test
    void testAddPlayerMaxRosterSize() {
        Position[] allPositions = Position.values();
        
        for (int i = 0; i < Sport.NFL.getMaxRosterSize(); i++) {
            String pos = allPositions[i % allPositions.length].name(); 
            manager.addPlayerToDepthChart(pos, new Player(i, "Player " + i), null);
        }
        
        Player extraPlayer = new Player(99, "Extra Player");
        Exception exception = Assertions.assertThrows(
            DepthChartException.class, 
            () -> manager.addPlayerToDepthChart("QB", extraPlayer, 0)
        );
        assertTrue(exception.getMessage().contains("Maximum roster limit of " + Sport.NFL.getMaxRosterSize() + " reached"));

        Player existingPlayer = new Player(0, "Player 0"); 
        String originalPos = allPositions[0].name(); 
        
        try {
            manager.addPlayerToDepthChart(originalPos, existingPlayer, 1);
        } catch (DepthChartException e) {
            fail("Moving existing player should not throw exception. Failed with: " + e.getMessage());
        }
    }

    @Test
    void testPlayerMultiplePositions() {
        // Josh Wells can play in both LT and RT
        manager.addPlayerToDepthChart("LT", joshWells, 0);
        manager.addPlayerToDepthChart("RT", joshWells, 0);

        // Remove from LT
        manager.removePlayerFromDepthChart("LT", joshWells);
        assertTrue(manager.getBackups("LT", joshWells).isEmpty(), "Should be removed from LT");

        manager.addPlayerToDepthChart("RT", kyleTrask, 1);
        List<Player> rtBackups = manager.getBackups("RT", joshWells);
    
        assertEquals(1, rtBackups.size(), "Josh Wells should still be at RT");
        assertEquals(kyleTrask, rtBackups.get(0), "Kyle Trask should be backup RT");
    }

    @Test
    void testRemovePlayer() {
        manager.addPlayerToDepthChart("LWR", mikeEvans, 0);

        List<Player> removed = manager.removePlayerFromDepthChart("LWR", mikeEvans);

        assertEquals(1, removed.size());
        assertEquals(mikeEvans, removed.get(0));
        
        // check no backup is present
        assertTrue(manager.getBackups("LWR", mikeEvans).isEmpty());
    }

    @Test
    void testRemovePlayerNonExistent() {
        List<Player> removed = manager.removePlayerFromDepthChart("QB", tomBrady);
        assertTrue(removed.isEmpty(), "Removing a non-existent player should return an empty list.");
    }

    @Test
    void testRemovePlayerNullPosition() {
        Exception exception = Assertions.assertThrows(
            DepthChartException.class, 
            () -> manager.removePlayerFromDepthChart(null, tomBrady)
        );
        assertEquals("Position cannot be null or empty", exception.getMessage());
    }

    @Test
    void testRemovePlayerNullPlayer() {
        try 
        {
            manager.removePlayerFromDepthChart("QB", null);
            fail("Throw a NPE for null player");
        } 
        catch (NullPointerException e) 
        {
            assertEquals("Player cannot be null", e.getMessage());
        }
    }

    @Test
    void testRemovePlayerFromEmptyPosition() {
        // Position TE exists in map but is empty
        manager.addPlayerToDepthChart("TE", kyleTrask, 0);
        manager.removePlayerFromDepthChart("TE", kyleTrask); // Now it's empty
        
        List<Player> removed = manager.removePlayerFromDepthChart("TE", tomBrady);
        assertTrue(removed.isEmpty(), "Mst return empty list when removing from an empty position.");
    }

    @Test
    void testRemovePlayerNotFoundInPosition() {
        // position QB has players, but Mike Evans is not present in it
        manager.addPlayerToDepthChart("QB", tomBrady, 0);
        List<Player> removed = manager.removePlayerFromDepthChart("QB", mikeEvans);
        assertTrue(removed.isEmpty(), "Should return empty list when the player is not found in specified position.");
    }

    @Test
    void testRemovePlayerInvalidPosition() {
        Exception exception = Assertions.assertThrows(
                DepthChartException.class, 
            () -> manager.removePlayerFromDepthChart("ABC", tomBrady)
        );
        
        assertTrue(exception.getMessage().contains("Invalid position"), 
            "Exception message should mention the invalid position.");
    }

    @Test
    void testGetBackups() {
        manager.addPlayerToDepthChart("QB", tomBrady, 0);
        manager.addPlayerToDepthChart("QB", blaineGabbert, 1);
        manager.addPlayerToDepthChart("QB", kyleTrask, 2);

        List<Player> backups = manager.getBackups("QB", blaineGabbert);
        assertEquals(1, backups.size());
        assertEquals(kyleTrask, backups.get(0));
    }

    @Test
    void testGetBackupsNullPosition() {
        Exception exception = Assertions.assertThrows(
                DepthChartException.class, 
                () -> manager.getBackups(null, tomBrady)
        );
        assertEquals("Position cannot be null or empty", exception.getMessage());
    }

    @Test
    void testGetBackupsNullPlayer() {
        try 
        {
            manager.getBackups("QB", null);
            fail("Throw a NPE for null player");
        } 
        catch (NullPointerException e) 
        {
            assertEquals("Player cannot be null", e.getMessage());
        }
    }

    @Test
    void testGetBackupsPositionMissing() {
        // Position K for Kickerwas never added to map
        List<Player> backups = manager.getBackups("K", tomBrady);
        assertTrue(backups.isEmpty(), "Must return empty list for a non-existent position.");
    }

    @Test
    void testGetBackupsPositionEmpty() {
        // Position TE exists in map but has 0 players
        manager.addPlayerToDepthChart("TE", kyleTrask, 0);
        manager.removePlayerFromDepthChart("TE", kyleTrask); 
        
        List<Player> backups = manager.getBackups("TE", tomBrady);   
        assertTrue(backups.isEmpty(), "Should return empty list for a position that exists but is empty.");
    }

    @Test
    void testGetBackupsNoBackupPresent() {
        manager.addPlayerToDepthChart("QB", tomBrady, 0);

        List<Player> backups = manager.getBackups("QB", tomBrady);
        assertTrue(backups.isEmpty(), "Player at the end of the chart should return an empty backups list.");
    }

    @Test
    void testGetBackupsInvalidPosition() {
        Exception exception = Assertions.assertThrows(
            DepthChartException.class, 
            () -> manager.getBackups("ABC", tomBrady)
        );
        
        assertTrue(exception.getMessage().contains("Invalid position"), 
            "Exception message should mention the invalid position.");
    }

    @Test
    void testGetBackupsModifyList() {
        manager.addPlayerToDepthChart("QB", tomBrady, 0);
        manager.addPlayerToDepthChart("QB", blaineGabbert, 1);

        // get backups and clear list
        List<Player> backups = manager.getBackups("QB", tomBrady);
        backups.clear(); 

        List<Player> secureBackups = manager.getBackups("QB", tomBrady);
        assertFalse(secureBackups.isEmpty(), "Internal state leaked! Modifying the returned list destroyed internal data.");
        assertEquals(1, secureBackups.size());
    }

    @Test
    void testGetFullDepthChart() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        manager.addPlayerToDepthChart("QB", tomBrady, 0);
        manager.addPlayerToDepthChart("QB", blaineGabbert, 1);
        manager.addPlayerToDepthChart("LWR", mikeEvans, 0);

        // add player to TE, then remove it to check skip empty list logic
        manager.addPlayerToDepthChart("TE", kyleTrask, 0);
        manager.removePlayerFromDepthChart("TE", kyleTrask);

        manager.getFullDepthChart();

        String output = outContent.toString();

        assertTrue(output.contains("=== Tampa Bay Buccaneers (NFL) Depth Chart ==="),
        "Header was formatted incorrectly.");
        assertTrue(output.contains("QB - (#12, Tom Brady), (#11, Blaine Gabbert)"),
                    "QB output was formatted incorrectly.");
        assertTrue(output.contains("LWR - (#13, Mike Evans)"),
                    "LWR output was formatted incorrectly.");
        assertFalse(output.contains("TE -"),
                    "Empty positions should be skipped entirely.");

    }

    @Test
    void testGetBackupsWrongPosition() {
        // Mike Evans added to LWR
        manager.addPlayerToDepthChart("LWR", mikeEvans, 0);
        manager.addPlayerToDepthChart("QB", tomBrady, 0);

        // check backups for Mike Evans in QB
        List<Player> backups = manager.getBackups("QB", mikeEvans);

        // should be empty because he is not a QB
        assertTrue(backups.isEmpty(), "Should return empty list for player in wrong position");
    }

    @Test
    void testGetBackupsSequence() {
        
        manager.addPlayerToDepthChart("QB", tomBrady, 0);
        manager.addPlayerToDepthChart("QB", blaineGabbert, 1);
        manager.addPlayerToDepthChart("QB", kyleTrask, 2);
        manager.addPlayerToDepthChart("LWR", mikeEvans, 0);
        manager.addPlayerToDepthChart("LWR", jaelonDarden, 1);
        manager.addPlayerToDepthChart("LWR", scottMiller, 2);

        // check Tom Brady (QB) backups: [Gabbert, Trask]
        List<Player> bradyBackups = manager.getBackups("QB", tomBrady);
        assertEquals(2, bradyBackups.size());
        assertEquals(blaineGabbert, bradyBackups.get(0));
        assertEquals(kyleTrask, bradyBackups.get(1));

        // check Jaelon Darden (LWR) backups: [Scott Miller]
        List<Player> dardenBackups = manager.getBackups("LWR", jaelonDarden);
        assertEquals(1, dardenBackups.size());
        assertEquals(scottMiller, dardenBackups.get(0));

        // check Kyle Trask (QB) backups: Empty
        assertTrue(manager.getBackups("QB", kyleTrask).isEmpty());
    }

    @Test
    void testAddPlayerDuplicateEntry() {
        manager.addPlayerToDepthChart("QB", tomBrady, 0);
        manager.addPlayerToDepthChart("QB", blaineGabbert, 1);

        // move Tom Brady to depth 1 
        manager.addPlayerToDepthChart("QB", tomBrady, 1);

        // Brady will be at 1, and list size will still be 2
        List<Player> backupsForGabbert = manager.getBackups("QB", blaineGabbert);
        assertEquals(1, backupsForGabbert.size());
        assertEquals(tomBrady, backupsForGabbert.get(0));
        
        // verify Brady is not duplicated
        List<Player> backupsForNoOne = manager.getBackups("QB", tomBrady);
        assertTrue(backupsForNoOne.isEmpty(), "Player should have been moved, not duplicated");
    }

    @Test
    void testRemovalFlow() {
        manager.addPlayerToDepthChart("LWR", mikeEvans, 0);
        manager.addPlayerToDepthChart("LWR", jaelonDarden, 1);
        manager.addPlayerToDepthChart("LWR", scottMiller, 2);

        // remove Mike Evans
        List<Player> removed = manager.removePlayerFromDepthChart("LWR", mikeEvans);
        assertEquals(mikeEvans, removed.get(0));

        // check Jaelon Darden is in starting position, Scott Miller will be his backup
        List<Player> dardenBackups = manager.getBackups("LWR", jaelonDarden);
        assertEquals(1, dardenBackups.size());
        assertEquals(scottMiller, dardenBackups.get(0));
    }

    @Test
    void testConstructorNullSport() {
        Assertions.assertThrows(DepthChartException.class, () -> 
            new DepthChartManager(null, "TB", "Tampa Bay Buccaneers")
        );
    }

    @Test
    void testConstructorInvalidShortName() {

        Assertions.assertThrows(DepthChartException.class, () -> 
            new DepthChartManager(Sport.NFL, "T", "Tampa Bay Buccaneers")
        );

        Assertions.assertThrows(DepthChartException.class, () -> 
            new DepthChartManager(Sport.NFL, "TBBB", "Tampa Bay Buccaneers")
        );

        Assertions.assertThrows(DepthChartException.class, () -> 
            new DepthChartManager(Sport.NFL, null, "Tampa Bay Buccaneers")
        );
    }

    @Test
    void testConstructorEmptyLongName() {

        Assertions.assertThrows(DepthChartException.class, () -> 
            new DepthChartManager(Sport.NFL, "TB", "")
        );

        Assertions.assertThrows(DepthChartException.class, () -> 
            new DepthChartManager(Sport.NFL, "TB", "   ")
        );
    }
}
