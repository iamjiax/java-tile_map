package bearmaps.proj2ab;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Obfuscated implementation of a PointSet with a fast nearest method.
 * Created by hug.
 */
public class KDTree implements PointSet {
    private static final boolean HORIZONTAL = true;
    private static final boolean VERTICAL = false;

    private Node root;

    private class Node {
        private Point point;
        private boolean orientation;
        private Node leftChild; // also downChild
        private Node rightChild; // also upChild

        public Node(Point p, boolean orient) {
            point = p;
            orientation = orient;
        }
    }

    public KDTree(List<Point> points) {
        // Collections.shuffle(points);
        for (Point p : points) {
            root = add(p, root, HORIZONTAL);
        }
    }

    private boolean rotate(boolean orientation) {
        return !orientation;
    }

    private Node add(Point point, Node node, boolean orientation) {
        if (node == null) {
            return new Node(point, orientation);
        }
        if (point.equals(node.point)) {
            return node;
        }

        int cmp = comparePoints(point, node.point, orientation);

        if (cmp < 0) {
            node.leftChild = add(point, node.leftChild, rotate(orientation));
        } else if (cmp >= 0) {
            node.rightChild = add(point, node.rightChild, rotate(orientation));
        }
        return node;
    }

    private int comparePoints(Point pa, Point pb, boolean orientation) {
        if (orientation == HORIZONTAL) {
            return Double.compare(pa.getX(), pb.getX());
        } else {
            return Double.compare(pa.getY(), pb.getY());
        }
    }

    @Override
    public Point nearest(double x, double y) {
        Point goal = new Point(x, y);
        Node nearestNode = nearestNode(root, goal, root);
        return nearestNode.point;
    }

    private Node nearestNode(Node node, Point goal, Node best) {
        if (node == null) {
            return best;
        }

        if (Point.distance(node.point, goal) < Point.distance(goal, best.point)) {
            best = node;
        }

        Node goodSide;
        Node badSide;

        if (comparePoints(goal, node.point, node.orientation) < 0) {
            goodSide = node.leftChild;
            badSide = node.rightChild;
        } else {
            goodSide = node.rightChild;
            badSide = node.leftChild;
        }

        best = nearestNode(goodSide, goal, best);

        Point bestBadSidePoint;
        if (node.orientation == VERTICAL) {
            bestBadSidePoint = new Point(goal.getX(), node.point.getY());
        } else {
            bestBadSidePoint = new Point(node.point.getX(), goal.getY());
        }

        if (Point.distance(bestBadSidePoint, goal) < Point.distance(best.point, goal)) {
            best = nearestNode(badSide, goal, best);
        }

        return best;
    }

    public static void main(String[] args) {
        Point pA = new Point(2, 3);
        Point pB = new Point(4, 2);
        Point pC = new Point(4, 5);
        Point pD = new Point(3, 3);
        Point pE = new Point(1, 5);
        Point pF = new Point(4, 4);

        KDTree k = new KDTree(Arrays.asList(pA, pB, pC, pD, pE, pF));
        System.out.println(k.nearest(-3, 3));

    }
}
