package ads.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PathFinderTest {
    private GameMap map;

    @BeforeEach
    void createEmptyMap() {
        map = new GameMap();
        for (int row = 0; row < GameMap.SIZE; row++) {
            for (int column = 0; column < GameMap.SIZE; column++) {
                map.setCell(row, column, '.');
            }
        }
    }

    @Test
    void aStarAndBfsReturnShortestPathOnOpenGrid() {
        map.setCell(2, 3, 'T');
        PathFinder finder = new PathFinder(map);

        List<Position> aStar = finder.findPathAStar(new Position(0, 0));
        List<Position> bfs = finder.findPathBFS(new Position(0, 0));

        assertEquals(6, aStar.size());
        assertEquals(aStar.size(), bfs.size());
        assertEquals(new Position(2, 3), aStar.get(aStar.size() - 1));
        assertEquals(new Position(2, 3), bfs.get(bfs.size() - 1));
    }

    @Test
    void bothAlgorithmsReturnEmptyPathWhenGoalIsBlocked() {
        map.setCell(0, 0, 'T');
        map.setCell(0, 1, 'X');
        map.setCell(1, 0, 'X');
        PathFinder finder = new PathFinder(map);
        Position start = new Position(1, 1);

        // Make every exit from the start an obstacle, without relying on random state.
        map.setCell(1, 2, 'X');
        map.setCell(2, 1, 'X');

        assertTrue(finder.findPathAStar(start).isEmpty());
        assertTrue(finder.findPathBFS(start).isEmpty());
    }

    @Test
    void positionUsesChessStyleNotation() {
        assertEquals("a20", new Position(0, 0).toNotation());
        assertEquals("t1", new Position(19, 19).toNotation());
    }
}
