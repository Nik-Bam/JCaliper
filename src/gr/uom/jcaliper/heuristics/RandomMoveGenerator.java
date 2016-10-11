package gr.uom.jcaliper.heuristics;

import java.util.Collection;
import java.util.Random;
import java.util.TreeMap;

public class RandomMoveGenerator {

	private TreeMap<Integer, IMove> map = new TreeMap<Integer, IMove>();
	private static Random rand = new Random(0);

	public RandomMoveGenerator(Collection<? extends IMove> moves /* , MoveStatistics stats */) {
		int index = 0;
		for (IMove move : moves)
			map.put(index++, move);
	}

	public IMove getRandomMove() {
		if (map.size() > 0)
			return map.get(rand.nextInt(map.size()));
		return null;
	}

}
