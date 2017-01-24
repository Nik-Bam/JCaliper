/**
 *
 */
package gr.uom.jcaliper.system;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculate the Dice's similarity coefficient between two strings
 * from: www.codeproject.com/Articles/147230/Simple-Fuzzy-String-Similarity-in-Java
 *
 * @author Panagiotis Kouros
 */
public class Dice {

    public static final double StringSimilarity(String s1, String s2) {
        return dice(bigram(s1), bigram(s2));
    }

    private static final List<char[]> bigram(String input) {
        ArrayList<char[]> bigram = new ArrayList<char[]>();
        for (int i = 0; i < input.length() - 1; i++) {
            char[] chars = new char[2];
            chars[0] = input.charAt(i);
            chars[1] = input.charAt(i + 1);
            bigram.add(chars);
        }
        return bigram;
    }

    private static final double dice(List<char[]> bigram1, List<char[]> bigram2) {
        if ((bigram1.size() + bigram2.size()) == 0)
            return 0.0;
        List<char[]> copy = new ArrayList<char[]>(bigram2);
        int matches = 0;
        for (int i = bigram1.size(); --i >= 0; ) {
            char[] bigram = bigram1.get(i);
            for (int j = copy.size(); --j >= 0; ) {
                char[] toMatch = copy.get(j);
                if (bigram[0] == toMatch[0] && bigram[1] == toMatch[1]) {
                    copy.remove(j);
                    matches += 2;
                    break;
                }
            }
        }
        return (double) matches / (bigram1.size() + bigram2.size());
    }
}
