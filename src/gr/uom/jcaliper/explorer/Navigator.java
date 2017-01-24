package gr.uom.jcaliper.explorer;

import gr.uom.jcaliper.heuristics.RandomMoveGenerator;
import gr.uom.jcaliper.metrics.EvaluatedClass;

import java.util.*;

/**
 * @author Panagiotis Kouros
 */
public class Navigator {

    final ComparisonPolicy comparison;
    final boolean biggerValueIsBetter;
    private TreeMap<Long, EvaluatedClass> classes = new TreeMap<Long, EvaluatedClass>();
    private EvaluatedClass empty;
    protected TreeSet<CratMove> moves = new TreeSet<CratMove>(new MoveComparator());
    private double sumOfGain = 0.0;
    private double sumOfGain2 = 0.0;
    private double minDeterioration = 0.0;
    private double maxDeterioration = 0.0;
    private int improvingMoves = 0;
    private Random rand = new Random(0);

    public Navigator(boolean biggerValueIsBetter, EvaluatedClass empty) {
        this.biggerValueIsBetter = biggerValueIsBetter;
        if (biggerValueIsBetter) {
            comparison = new BiggerValueIsBetter();
            minDeterioration = -1e10;
            maxDeterioration = 1e10;
        } else {
            comparison = new SmallerValueIsBetter();
            minDeterioration = 1e10;
            maxDeterioration = -1e10;
        }

        this.empty = empty;
        classes.put(empty.getHash(), empty);
    }

    public void newCurrentState(CratState state) {
        classes.clear();
        moves.clear();
        sumOfGain = 0.0;
        sumOfGain2 = 0.0;
        improvingMoves = 0;
        classes.put(empty.getHash(), empty);
        for (EvaluatedClass cl : state.values())
            addClass(cl);
    }

    public void removeClass(EvaluatedClass removed) {
        if (removed.size() == 0)
            return; // moves to empty class should be always available
        Iterator<CratMove> it = moves.iterator();
        while (it.hasNext()) {
            CratMove move = it.next();
            if ((move.from == removed) || (move.to == removed)) {
                removeMove(move); // in fact: prepare to remove
                it.remove();
            }
        }
        classes.remove(removed.getHash());
    }

    public void addClass(EvaluatedClass newClass) {
        if (newClass.size() == 0)
            return; // moves to empty class are already updated
        boolean oneMemberClass = (newClass.size() == 1);
        for (int entId : newClass.candidatesForExit())
            for (EvaluatedClass target : classes.values()) {
                // Don't add moves from oneMemberClass to empty class
                boolean targetIsEmpty = (target.size() == 0);
                if (target.candidatesForEntry().contains(entId)
                        && (!oneMemberClass || !targetIsEmpty)) {
                    CratMove move = new CratMove(0, entId, newClass, target, biggerValueIsBetter);
                    addMove(move);
                }
            }
        for (int entId : newClass.candidatesForEntry())
            for (EvaluatedClass origin : classes.values())
                if (origin.candidatesForExit().contains(entId)) {
                    CratMove move = new CratMove(0, entId, origin, newClass, biggerValueIsBetter);
                    addMove(move);
                }
        classes.put(newClass.getHash(), newClass);
    }

    private void removeMove(CratMove move) {
        sumOfGain -= move.gain;
        sumOfGain2 -= move.gain * move.gain;
        if (comparison.isGood(move.gain))
            improvingMoves--;
        // move will be removed by the iterator
        // and not by: 'feasibleMoves.remove(move)'
    }

    private void addMove(CratMove move) {
        sumOfGain += move.gain;
        sumOfGain2 += move.gain * move.gain;
        if (comparison.isGood(move.gain))
            improvingMoves++;
        if (comparison.compare(0, move.gain)) {
            if (comparison.compare(move.gain, minDeterioration))
                minDeterioration = move.gain;
            if (comparison.compare(maxDeterioration, move.gain))
                maxDeterioration = move.gain;
        }
        moves.add(move);
    }

    public CratMove getBestMove() {
        if (moves.size() > 0)
            return moves.first();
        return null;
    }

    public CratMove getAMoveBetterThan(double threshold) {
        // System.out.println(getTopEvaluatedMoves(50));
        if (moves.size() == 0)
            return null;
        if (!((moves.first()).isBetterThan(threshold)))
            return null;
        // Create a fake move with gain=threshold
        CratMove fake = new CratMove(0, 0, empty, empty, true);
        fake.gain = threshold;
        // Get all moves better than fake
        CratMove move = moves.floor(fake);
        SortedSet<CratMove> betterMoves = moves.subSet(moves.first(), move);
        if (betterMoves.size() < 2)
            return moves.first();
        // Return random move from 'betterMoves'
        return getRandomFromCollection(betterMoves);
    }

    public CratMove getRandomMove() {
        // System.out.println(getTopEvaluatedMoves(50));
        return getRandomFromCollection(moves);
    }

    private CratMove getRandomFromCollection(Collection<CratMove> movePool) {
        if (movePool.size() == 0)
            return null;
        int position = rand.nextInt(movePool.size());
        Iterator<CratMove> it = movePool.iterator();
        while (position-- > 0)
            ;
        it.next();
        return it.next();
    }

    public RandomMoveGenerator getRandomMoveGenerator() {
        return new RandomMoveGenerator(moves);
    }

    public int getNumOfFeasibleMoves() {
        return moves.size();
    }

    public double getMoveGainAverage() {
        if (moves.size() > 0)
            return sumOfGain / moves.size();
        return 0.0;
    }

    public double getMoveGainStdDev() {
        if (moves.size() > 1)
            return Math.sqrt((sumOfGain2 - (moves.size() * sumOfGain * sumOfGain))
                    / (moves.size() - 1));
        return 0.0;
    }

    public double getGoodMovesMetric() {
        return (double) improvingMoves / moves.size();
    }

    /**
     * @return the minDeterioration
     */
    public double getMinDeterioration() {
        return minDeterioration;
    }

    /**
     * @return the maxDeterioration
     */
    public double getMaxDeterioration() {
        return maxDeterioration;
    }

    public void clear() {
        if (classes != null)
            classes.clear();
        if (moves != null)
            moves.clear();
    }

    @Override
    public void finalize() throws Throwable {
        clear();
        super.finalize();
    }

    private class MoveComparator implements Comparator<CratMove> {
        @Override
        public int compare(CratMove move1, CratMove move2) {
            if (comparison.compare(move1.gain, move2.gain))
                return -1;
            else if (comparison.compare(move2.gain, move1.gain))
                return 1;
            else if (move1.from.getHash() < move2.from.getHash())
                return -1;
            else if (move1.from.getHash() > move2.from.getHash())
                return 1;
            else if (move1.to.getHash() < move2.to.getHash())
                return -1;
            else if (move1.to.getHash() > move2.to.getHash())
                return 1;
            else if (move1.moving < move2.moving)
                return -1;
            else if (move1.moving > move2.moving)
                return 1;
            else
                return 0;
        }
    }

    private interface ComparisonPolicy {
        public boolean compare(double value1, double value2);

        public boolean isGood(double value);
    }

    private class BiggerValueIsBetter implements ComparisonPolicy {
        @Override
        public boolean compare(double value1, double value2) {
            return (value1 - value2) > 1e-10;
        }

        @Override
        public boolean isGood(double value) {
            return (value > 1e-10);
        }
    }

    private class SmallerValueIsBetter implements ComparisonPolicy {
        @Override
        public boolean compare(double value1, double value2) {
            return ((value2 - value1) > 1e-10);
        }

        @Override
        public boolean isGood(double value) {
            return (value < -1e-10);
        }
    }

    public String getTopEvaluatedMoves(int howMany) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Top %d of evaluated moves\n", howMany));
        for (CratMove move : moves) {
            sb.append(String.format("%d from %s to %s\t%10.8f\n", move.moving, move.from, move.to,
                    move.gain));
            if (--howMany <= 0)
                break;
        }
        return sb.toString();
    }

    public int getNumOfImprovingMoves() {
        return improvingMoves;
    }
}
