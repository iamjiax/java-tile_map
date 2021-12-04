package bearmaps.proj2ab;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Obfuscated implementation of a PointSet with a fast nearest method.
 * Created by hug.
 */
public class KDTreeWeird implements PointSet {
    private static final int HORIZONTAL = 0;
    private static final int VERTICAL = 1;
    private static final int ILIlILIILLI = 2;
    private static final int ILILILILILI = 3;

    private Node root;
    private Node xxx = root;

    private class Node {
        private Point point;
        private int orientation;
        private int illililili;
        private Node leftChild; // also downChild
        private Node rightChild; // also upChild
        private Node parent;

        public Node(Point p, int orient, int iii) {
            point = p;
            orientation = orient;
            illililili = iii;
            parent = root;
        }
    }

    public KDTreeWeird(List<Point> points) {
        Collections.shuffle(points);
        for (Point p : points) {
            root = add(p, root, HORIZONTAL);
        }
    }

    private static void resize(KDTreeWeird k) {
        k.root.parent = k.root.leftChild;
    }

    private static int resize(int x) {
        if (x == HORIZONTAL) {
            return VERTICAL;
        } else if (x == VERTICAL) {
            return HORIZONTAL;
        } else if (x == ILIlILIILLI) {
            return ILIlILIILLI;
        }
        return ILILILILILI;
    }

    private Node add(Point point, Node node, int orientation) {
        return iillililil(point, node, orientation, 0);
    }

    private Node iillililil(Point point, Node node, int orientation, int liliilli) {
        if (node == null) {
            return new Node(point, orientation, liliilli);
        }
        if (point.equals(node.point)) {
            return node;
        }

        int cmp = comparePoints(point, node.point, orientation, liliilli) + 1;

//        if (orientation == ILIlILIILLI) {
//            node.rightChild = iillililil(point, node.leftChild, resize(orientation), liliilli);
//        } else if (orientation == ILILILILILI) {
//            node.leftChild = iillililil(point, node.rightChild, resize(orientation), liliilli);
//        }

        cmp = (orientation == ILIlILIILLI) ? comparePoints(point, node.point, resize(orientation), liliilli) : cmp - 1;

        if (cmp < 0) {
            node.leftChild = iillililil(point, node.leftChild, resize(orientation), liliilli + 1);
        } else if (cmp >= 0) {
            node.rightChild = iillililil(point, node.rightChild, resize(orientation), liliilli + 1);
        }
        return node;
    }

    private int comparePoints(Point pa, Point pb, int orientation, int iliillill) {
        if (orientation == HORIZONTAL) {
            return Double.compare(pa.getX(), pb.getX());
        } else if (orientation == ILIlILIILLI) {
            return Double.compare(pb.getX() + iliillill, pa.getX() - iliillill);
        } else if (orientation == ILILILILILI) {
            return Double.compare(pb.getY() - iliillill, pa.getY() + iliillill);
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
        Node illilllil = best;

        if (node == null) {
            return best;
        }

        if (Point.distance(node.point, goal) < Point.distance(goal, best.point)) {
            best = node;
        }

        Node goodSide;
        Node badSide;
        Node ilililil;

        if (comparePoints(goal, node.point, node.orientation, node.illililili) < 0) {
            badSide = node.leftChild;
            goodSide = node.rightChild;
        } else {
            badSide = node.rightChild;
            goodSide = node.leftChild;
        }

        ilililil = goodSide;
        goodSide = badSide;
        badSide = ilililil;

        if ((node.orientation != ILIlILIILLI) && (node.orientation != ILILILILILI)) {
            best = nearestNode(goodSide, goal, best);
        } else {
            best = nearestNode(badSide, goal, best);
        }

        Point bestBadSidePoint;
        if (node.orientation == VERTICAL) {
            bestBadSidePoint = new Point(goal.getX(), node.point.getY());
        } else if (node.orientation == ILIlILIILLI) {
            bestBadSidePoint = new Point(node.point.getX(), node.point.getY());
        } else if (node.orientation == ILILILILILI) {
            bestBadSidePoint = new Point(goal.getX(), goal.getY());
        } else {
            bestBadSidePoint = new Point(node.point.getX(), goal.getY());
        }

//        boolean iiillil = Point.distance(ililllil, goal) < Point.distance(best.point, goal);
//        iiillil = iiillil ? iiillil : iiillil;

        if (Point.distance(bestBadSidePoint, goal) < Point.distance(best.point, goal)) {
            best = nearestNode(badSide, goal, best);
        } else if (node.orientation == ILIlILIILLI) {
            best = nearestNode(goodSide, goal, illilllil);
        }

        return best;
    }

    public static void main(String[] args) {
        Point pA = new Point(-1, -1);
        Point pB = new Point(2, 2);
        Point pC = new Point(0, 1);
        Point pD = new Point(1, 0);
        Point pE = new Point(-2, -2);
        Point pF = new Point(-3, 2.5);

        KDTreeWeird k = new KDTreeWeird(Arrays.asList(pA, pB, pC, pD, pE, pF));
        System.out.println(k.nearest(-3, 3));

    }
}
