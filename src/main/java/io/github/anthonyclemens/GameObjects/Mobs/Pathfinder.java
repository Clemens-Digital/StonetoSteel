package io.github.anthonyclemens.GameObjects.Mobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import io.github.anthonyclemens.GameObjects.GameObject;
import io.github.anthonyclemens.WorldGen.Chunk;
import io.github.anthonyclemens.WorldGen.World;

public class Pathfinder {

    private Pathfinder() {}

    static class Node {
        int x;
        int y;
        Node parent;
        double g;
        double h;

        Node(int x, int y, Node parent, double g, double h) {
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.g = g;
            this.h = h;
        }

        double f() {
            return g + h;
        }
    }

    public static List<int[]> findPath(World world, int startX, int startY, int goalX, int goalY) {
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(Node::f));
        Map<String, Node> visited = new HashMap<>();

        Node start = new Node(startX, startY, null, 0, heuristic(startX, startY, goalX, goalY));
        open.add(start);

        while (!open.isEmpty()) {
            Node current = open.poll();
            String key = current.x + "," + current.y;

            if (current.x == goalX && current.y == goalY) {
                return smoothPath(world, reconstruct(current));
            }

            visited.put(key, current);

            for (int[] dir : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];
                String nKey = nx + "," + ny;

                if (visited.containsKey(nKey)) continue;
                if (!isWalkable(world, nx, ny)) continue;

                double g = current.g + 1;
                double h = heuristic(nx, ny, goalX, goalY);
                Node neighbor = new Node(nx, ny, current, g, h);

                open.add(neighbor);
            }
        }
        return Collections.emptyList();
    }

    private static double heuristic(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private static List<int[]> reconstruct(Node node) {
        LinkedList<int[]> path = new LinkedList<>();
        while (node != null) {
            path.addFirst(new int[]{node.x, node.y});
            node = node.parent;
        }
        return path;
    }

    private static boolean isWalkable(World world, int absX, int absY) {
        int[] bc = world.getBlockAndChunk(absX, absY);
        Chunk chunk = world.getChunk(bc[2], bc[3]);
        for (GameObject obj : chunk.getGameObjects()) {
            if (obj.getX() == bc[0] && obj.getY() == bc[1] && obj.isSolid()) {
                return false;
            }
        }
        return true;
    }

    // --- Path smoothing ---
    private static List<int[]> smoothPath(World world, List<int[]> rawPath) {
        if (rawPath.size() <= 2) return rawPath;

        List<int[]> smooth = new ArrayList<>();
        int[] last = rawPath.get(0);
        smooth.add(last);

        int i = 0;
        while (i < rawPath.size() - 1) {
            int j = rawPath.size() - 1;
            for (; j > i + 1; j--) {
                if (hasLineOfSight(world, rawPath.get(i), rawPath.get(j))) {
                    break;
                }
            }
            smooth.add(rawPath.get(j));
            i = j;
        }
        return smooth;
    }

    private static boolean hasLineOfSight(World world, int[] a, int[] b) {
        // Bresenham’s line algorithm
        int x0 = a[0], y0 = a[1];
        int x1 = b[0], y1 = b[1];
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            if (!isWalkable(world, x0, y0)) return false;
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 < dx) { err += dx; y0 += sy; }
        }
        return true;
    }
}
