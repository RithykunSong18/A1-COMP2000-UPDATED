import java.util.*;

public class PathFind {
    public static Cell nextStepBFS(Grid g, Actor a, Cell start, Cell goal) {
        if (start == goal) return start;

        Map<Cell, Cell> parent = new HashMap<>();
        ArrayDeque<Cell> q = new ArrayDeque<>();
        q.add(start); parent.put(start, null);

        while (!q.isEmpty()) {
            Cell cur = q.removeFirst();
            for (Cell nb : g.neighbors(cur)) {
                if (parent.containsKey(nb)) continue;
                if (g.isBlockedFor(a, nb)) continue;
                parent.put(nb, cur);
                if (nb == goal) {
                    Cell step = nb, prev = parent.get(step);
                    while (prev != null && prev != start) { step = prev; prev = parent.get(step); }
                    return step;
                }
                q.addLast(nb);
            }
        }
        return start;
    }
}