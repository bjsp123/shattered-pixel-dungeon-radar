package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ItemRequirementsSearch {

    public final AtomicInteger attemptCount = new AtomicInteger(0);
    private Runnable progressCallback;

    public void setProgressCallback(Runnable cb) { progressCallback = cb; }

    public static class Result {
        public final String code;
        public final String summary;
        Result(String code, String summary) { this.code = code; this.summary = summary; }
    }

    /**
     * Searches up to maxRolls random seeds and returns the first one where:
     *  - all fragments in items1Raw  appear in items at depth <= 1
     *  - all fragments in items2Raw  appear in items at depth <= 2
     *  - all fragments in items5Raw  appear in items at depth <= 5
     *  - all fragments in items10Raw appear in items at depth <= 10
     * Returns a Result with the seed code and full item summary, or null if no match found.
     */
    public Result findSeedWithRequirements(String items1Raw, String items2Raw,
                                           String items5Raw, String items10Raw, int maxRolls) {
        ArrayList<String> frags1  = parseFragments(items1Raw);
        ArrayList<String> frags2  = parseFragments(items2Raw);
        ArrayList<String> frags5  = parseFragments(items5Raw);
        ArrayList<String> frags10 = parseFragments(items10Raw);

        int maxDepth = 0;
        if (!frags1.isEmpty())  maxDepth = 1;
        if (!frags2.isEmpty())  maxDepth = 2;
        if (!frags5.isEmpty())  maxDepth = 5;
        if (!frags10.isEmpty()) maxDepth = 10;
        if (maxDepth == 0) return null;

        DungeonItemSummary generator = new DungeonItemSummary();

        try {
            for (int attempt = 0; attempt < maxRolls; attempt++) {
                if (Thread.currentThread().isInterrupted())
                    throw new InterruptedException();

                int checked = attemptCount.incrementAndGet();
                if (progressCallback != null && checked % 10 == 0)
                    progressCallback.run();

                long candidateSeed = Random.Long(DungeonSeed.TOTAL_SEEDS);
                String code = DungeonSeed.convertToCode(candidateSeed);
                String summary = generator.getItemsSummary(code, maxDepth);

                if (!frags1.isEmpty()  && !allMatch(summary, frags1,  1))  continue;
                if (!frags2.isEmpty()  && !allMatch(summary, frags2,  2))  continue;
                if (!frags5.isEmpty()  && !allMatch(summary, frags5,  5))  continue;
                if (!frags10.isEmpty() && !allMatch(summary, frags10, 10)) continue;

                return new Result(code, summary);
            }
        } catch (InterruptedException e) {
            return null;
        }
        return null;
    }

    /**
     * Returns true if every fragment in frags appears in at least one line of summary
     * where the depth prefix is <= maxDepth.
     */
    private boolean allMatch(String summary, ArrayList<String> frags, int maxDepth) {
        StringBuilder filtered = new StringBuilder();
        for (String line : summary.split("\n")) {
            int colon = line.indexOf(':');
            if (colon < 1) continue;
            try {
                int depth = Integer.parseInt(line.substring(0, colon).trim());
                if (depth <= maxDepth) filtered.append(line).append("\n");
            } catch (NumberFormatException ignored) { }
        }
        String block = filtered.toString();
        for (String frag : frags) {
            if (!block.contains(frag)) return false;
        }
        return true;
    }

    /** Splits newline-separated text into a list of non-empty trimmed lowercase fragments. */
    private ArrayList<String> parseFragments(String text) {
        if (text == null || text.isEmpty()) return new ArrayList<>();
        ArrayList<String> list = new ArrayList<>(Arrays.asList(
                text.toLowerCase().split("[\\r\\n]+")));
        list.replaceAll(String::trim);
        list.removeIf(String::isEmpty);
        return list;
    }
}
