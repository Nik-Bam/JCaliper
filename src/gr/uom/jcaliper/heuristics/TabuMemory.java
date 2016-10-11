package gr.uom.jcaliper.heuristics;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Panagiotis Kouros
 */
public class TabuMemory {

	private int tabuTenure;
	private TreeMap<Integer, Integer> tabuList = new TreeMap<Integer, Integer>();

	public void setTenure(int tabuTenure) {
		this.tabuTenure = tabuTenure;
	}

	public void update(int moveId, IMove move) {
		tabuList.put(move.getTabuAttribute(), moveId);
		// System.out.println(getTabuList());
	}

	public boolean isPermitted(int moveId, IMove move) {
		if (!tabuList.containsKey(move.getTabuAttribute()))
			return true;
		return (moveId - tabuList.get(move.getTabuAttribute()) > tabuTenure);
	}

	public String getTabuList() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Integer, Integer> entry : tabuList.entrySet())
			sb.append(entry.getKey()).append(':').append(entry.getValue()).append('\n');
		return sb.toString();
	}

	public void reset() {
		tabuList.clear();
	}
}
