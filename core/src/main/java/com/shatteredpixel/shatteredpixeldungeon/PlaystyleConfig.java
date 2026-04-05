package com.shatteredpixel.shatteredpixeldungeon;

import java.util.Arrays;

/**
 * Stores the combined playstyle stat values for the current run.
 * Computed once at game start via {@link #compute(int[])} and stored in {@link Dungeon}.
 *
 * Use {@link #getActualMultiplier(int)} for the computed effect of a stat (multiplier or flat bonus).
 * Use {@link #getTotalScores(int)} for the raw point total of a stat across all chosen playstyle levels.
 * Stat index constants are named PLAYSTYLE_*.
 */
public class PlaystyleConfig {

    // --- Stat index constants ---

    public static final int PLAYSTYLE_MELEE_DAMAGE   = Playstyles.STAT_MELEE_DAMAGE;
    public static final int PLAYSTYLE_RANGED_DAMAGE  = Playstyles.STAT_RANGED_DAMAGE;
    public static final int PLAYSTYLE_MAGIC_DAMAGE   = Playstyles.STAT_MAGIC_DAMAGE;
    public static final int PLAYSTYLE_MOVEMENT_SPEED = Playstyles.STAT_MOVEMENT_SPEED;
    public static final int PLAYSTYLE_ATTACK_SPEED   = Playstyles.STAT_ATTACK_SPEED;
    public static final int PLAYSTYLE_ACCURACY       = Playstyles.STAT_ACCURACY;
    public static final int PLAYSTYLE_EVASION        = Playstyles.STAT_EVASION;
    public static final int PLAYSTYLE_HITPOINTS      = Playstyles.STAT_HITPOINTS;
    public static final int PLAYSTYLE_ARMOR          = Playstyles.STAT_ARMOR;
    public static final int PLAYSTYLE_LUCK           = Playstyles.STAT_LUCK;
    public static final int PLAYSTYLE_VISION         = Playstyles.STAT_VISION;
    public static final int PLAYSTYLE_XRAY_VISION    = Playstyles.STAT_XRAY_VISION;

    private final int[]   rawLevels;
    private final float[] multipliers;  // computed effect per stat (multiplier: 1.0 neutral; flat: 0 neutral)
    private final int[]   totalPoints;  // raw point totals per stat (0 = neutral)

    /** Neutral config — all multipliers 1.0, all flat stats 0. Used as default before a run starts. */
    public PlaystyleConfig() {
        this.rawLevels   = new int[Playstyles.COUNT];
        this.multipliers = new float[Playstyles.STAT_COUNT];
        Arrays.fill(this.multipliers, 1f);
        // flat stats (stealth, vision, xray) should be 0; fill(1f) is wrong for those,
        // but we correct by computing from zero levels which gives 0 for flat stats.
        float[] neutral = Playstyles.computePlaystyleValues(this.rawLevels);
        System.arraycopy(neutral, 0, this.multipliers, 0, Playstyles.STAT_COUNT);
        this.totalPoints = new int[Playstyles.STAT_COUNT];
    }

    private PlaystyleConfig(int[] rawLevels, float[] multipliers, int[] totalPoints) {
        this.rawLevels   = rawLevels;
        this.multipliers = multipliers;
        this.totalPoints = totalPoints;
    }

    /**
     * Returns the computed effect for the given stat.
     * Multiplier stats (damage, speed, hp, armor, luck, accuracy, evasion): 1.0 = neutral.
     * Flat stats (stealth, vision, xray): 0 = neutral.
     */
    public float getActualMultiplier(int stat) {
        return multipliers[stat];
    }

    /**
     * Returns the total raw point score for the given stat,
     * summed across all chosen playstyle levels.
     */
    public int getTotalScores(int stat) {
        return totalPoints[stat];
    }

    /** Returns the raw per-playstyle level array (length {@link Playstyles#COUNT}). */
    public int[] getRawLevels() {
        return rawLevels;
    }

    /**
     * Computes the combined stat values from all playstyle levels
     * and returns a new {@link PlaystyleConfig} ready to be stored in game state.
     *
     * @param levels array of length {@link Playstyles#COUNT}, each entry 0–3.
     */
    public static PlaystyleConfig compute(int[] levels) {
        int[] pts = new int[Playstyles.STAT_COUNT];
        for (int cat = 0; cat < Playstyles.COUNT; cat++) {
            int lvl = levels[cat];
            if (lvl == 0) continue;
            for (int stat = 0; stat < Playstyles.STAT_COUNT; stat++) {
                pts[stat] += lvl * Playstyles.POINTS[cat][stat];
            }
        }
        float[] m = Playstyles.computePlaystyleValues(levels);
        return new PlaystyleConfig(levels.clone(), m, pts);
    }
}
