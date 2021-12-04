package bearmaps.hw4;

import bearmaps.proj2ab.DoubleMapPQ;
import bearmaps.proj2ab.ExtrinsicMinPQ;

import java.util.*;

/**
 * Obfuscated implementation of a solver for a shortest paths problem.
 * Created by hug.
 */
public class AStarSolver<Vertex> implements ShortestPathsSolver<Vertex> {
    private AStarGraph<Vertex> graph;
    private List<Vertex> solution;
    // edge to vertex map
    private Map<Vertex, WeightedEdge<Vertex>> pathMap = new HashMap<>();
    // distance to source map
    private Map<Vertex, Double> distToSource = new HashMap<>();

    private Vertex target;
    private SolverOutcome outcome;
    private int numStates = 0;
    private double explorationTime;

    public AStarSolver(AStarGraph<Vertex> input, Vertex start, Vertex end, double timeout) {
        graph = input;
        this.target = end;
        ExtrinsicMinPQ<Vertex> pq = new DoubleMapPQ<>();

        pq.add(start, graph.estimatedDistanceToGoal(start, end));
        pathMap.put(start, null);
        distToSource.put(start, 0.0);

        Stopwatch timer = new Stopwatch();
        boolean pqIsEmpty = pq.size() == 0;

        while (!pqIsEmpty && !smallestIsGoal(pq, end)
                && notTimeout(timer, timeout)) {
            Vertex current = pq.removeSmallest();
            numStates += 1;
            for (WeightedEdge<Vertex> edge : graph.neighbors(current)) {
                Vertex next = edge.to();
                double oldDist = getDistToSource(next);
                double newDist = getDistToSource(current) + edge.weight();
                if (newDist < oldDist) {
                    pathMap.put(next, edge);
                    distToSource.put(next, getDistToSource(current) + edge.weight());
                    double priority = graph.estimatedDistanceToGoal(next, end) + getDistToSource(next);
                    if (pq.contains(next)) {
                        pq.changePriority(next, priority);
                    } else {
                        pq.add(next, priority);
                    }
                }
            }
            pqIsEmpty = pq.size() == 0;
        }
        explorationTime = timer.elapsedTime();

        if (pq.size() == 0) {
            this.outcome = SolverOutcome.UNSOLVABLE;
            solution = new ArrayList<>();
            return;
        }

        solution = constructPath(start, pq.getSmallest());

        if (pq.getSmallest().equals(end)) {
            this.outcome = SolverOutcome.SOLVED;
        } else {
            this.outcome = SolverOutcome.TIMEOUT;
        }
    }

    private boolean smallestIsGoal(ExtrinsicMinPQ<Vertex> pq, Vertex goal) {
        return pq.getSmallest().equals(goal);
    }


    private boolean notTimeout(Stopwatch timer, double timeout) {
        return timer.elapsedTime() < timeout;
    }

    @Override
    public SolverOutcome outcome() {
        return outcome;
    }

    private List<Vertex> constructPath(Vertex start, Vertex end) {
        List<Vertex> path = new ArrayList<>();
        path.add(end);
        while (pathMap.get(end) != null) {
            WeightedEdge<Vertex> e = pathMap.get(end);
            path.add(e.from());
            end = e.from();
        }
        Collections.reverse(path);
        return path;
    }


    private double getDistToSource(Vertex vertex) {
        return distToSource.getOrDefault(vertex, Double.POSITIVE_INFINITY);
    }

    @Override
    public double solutionWeight() {
        return getDistToSource(target);
    }

    @Override
    public List<Vertex> solution() {
        return solution;
    }

    @Override
    public int numStatesExplored() {
        return numStates;
    }

    @Override
    public double explorationTime() {
        return explorationTime;
    }

    public class Stopwatch {
        private final long start = System.currentTimeMillis();

        public Stopwatch() {
        }

        public double elapsedTime() {
            long now = System.currentTimeMillis();
            return (double)(now - this.start) / 1000.0D;
        }
    }
}
