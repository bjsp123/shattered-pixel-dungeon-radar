package com.shatteredpixel.shatteredpixeldungeon;

public class Playstyles {

	// --- Playstyle indices ---

	public static final int GLASS_CANNON    = 0;
	public static final int WALKING_TANK    = 1;
	public static final int FLAILING_MANIAC = 2;
	public static final int FENCER          = 3;
	public static final int HEAVY_HITTER    = 4;
	public static final int ADVENTURER      = 5;
	public static final int FAERIE_BLOOD    = 6;
	public static final int SNIPER          = 7;
	public static final int PHYSICALLY_FIT  = 8;
	public static final int FAST_FEET       = 9;
	public static final int STEELSKIN       = 10;
	public static final int SAGE            = 11;

	public static final int COUNT = 12;

	public static final String[] NAME_IDS = {
			"glass_cannon",
			"walking_tank",
			"flailing_maniac",
			"fencer",
			"heavy_hitter",
			"adventurer",
			"faerie_blood",
			"sniper",
			"physically_fit",
			"fast_feet",
			"steelskin",
			"sage"
	};

	// --- Stat indices (match the multiplier field order in PlaystyleConfig) ---

	public static final int STAT_MELEE_DAMAGE   = 0;
	public static final int STAT_RANGED_DAMAGE  = 1;
	public static final int STAT_MAGIC_DAMAGE   = 2;
	public static final int STAT_MOVEMENT_SPEED = 3;
	public static final int STAT_ATTACK_SPEED   = 4;
	public static final int STAT_ACCURACY       = 5;
	public static final int STAT_EVASION        = 6;
	public static final int STAT_HITPOINTS      = 7;
	public static final int STAT_ARMOR          = 8;
	public static final int STAT_LUCK           = 9;
	public static final int STAT_VISION         = 10;
	public static final int STAT_XRAY_VISION    = 11;

	public static final int STAT_COUNT = 12;

	/**
	 * How much one point contributes to a multiplier.
	 * A stat with +1 point at level 1 gives a ×1.25 multiplier.
	 */
	public static final float POINT_VALUE = 0.25f;

	/**
	 * Points per level per stat for each playstyle.
	 * Indexed [playstyle][stat]. Multiply by the player's chosen level to get total points.
	 * One point = POINT_VALUE (25%) bonus or penalty to the multiplier.
	 *
	 *                          mel  rng  mag  spd  asp  acc  eva   hp  arm  lck  vis  xry
	 * Glass Cannon:          {  0,   1,   1,   0,   0,   0,   0,  -1,  -1,   0,   0,   0 }
	 * Walking Tank:          {  0,   0,   0,  -1,   0,   0,  -1,   1,   1,   0,   0,   0 }
	 * Flailing Maniac:       {  1,   0,   0,   0,   1,  -1,  -1,   0,   0,   0,   0,   0 }
	 * Fencer:                {  0,  -1,  -1,   0,   1,   1,   0,   0,  -1,   0,   0,   0 }
	 * Heavy Hitter:          {  1,   1,   0,   0,  -1,   0,  -1,   0,   0,   0,   0,   0 }
	 * Adventurer:            { -1,   0,  -1,   0,   0,   0,   0,   0,   0,   1,   0,   1 }
	 * Faerie Blood:          { -1,   0,   1,   0,   0,   0,   0,  -1,   0,   1,   0,   0 }
	 * Sniper:                { -1,   1,   0,   0,  -1,   0,   0,   0,   0,   0,   1,   0 }
	 * Physically Fit:        {  1,   0,   0,   0,   0,   0,  -1,   1,  -1,   0,   0,   0 }
	 * Fast Feet:             {  0,  -1,  -1,   1,   0,   0,   0,  -1,   0,   0,   0,   0 }
	 * Steelskin:             {  1,   0,   0,  -1,   0,   0,   0,   0,   1,   0,   0,   0 }
	 * Sage:                  { -1,  -1,  -1,   0,   0,   0,   1,   0,   0,   1,   1,   1 }
	 */
	public static final int[][] POINTS = {
		//                        mel  rng  mag  spd  asp  acc  eva   hp  arm  lck  vis  xry
		/* GLASS_CANNON    */ {   0,   1,   1,   0,   0,   0,   0,  -1,  -1,   0,   0,   0 },
		/* WALKING_TANK    */ {   0,   0,   0,  -1,   0,   0,  -1,   1,   1,   0,   0,   0 },
		/* FLAILING_MANIAC */ {   1,   0,   0,   0,   1,  -1,  -1,   0,   0,   0,   0,   0 },
		/* FENCER          */ {   0,  -1,  -1,   0,   1,   1,   0,   0,  -1,   0,   0,   0 },
		/* HEAVY_HITTER    */ {   1,   1,   0,   0,  -1,   0,  -1,   0,   0,   0,   0,   0 },
		/* ADVENTURER      */ {  -1,   0,  -1,   0,   0,   0,   1,   0,   0,   1,   0,   1 },
		/* FAERIE_BLOOD    */ {  -1,   0,   1,   0,   0,   0,   0,  -1,   0,   1,   0,   0 },
		/* SNIPER          */ {  -1,   1,   0,   0,  -1,   0,   0,   0,   0,   0,   1,   0 },
		/* PHYSICALLY_FIT  */ {   1,   0,   0,   0,   0,   0,  -1,   1,  -1,   0,   0,   0 },
		/* FAST_FEET       */ {   0,  -1,  -1,   1,   0,   0,   0,  -1,   0,   0,   0,   0 },
		/* STEELSKIN       */ {   1,   0,   0,  -1,   0,   0,   0,   0,   1,   0,   0,   0 },
		/* SAGE            */ {  -1,  -1,  -1,   0,   0,   0,   1,   0,   0,   1,   1,   1 },
	};

	/**
	 * Computes the final stat array from the player's chosen levels.
	 * Sums points across all playstyles (weighted by level), then converts per-stat:
	 *
	 *   Damage (melee/ranged/magic): 1.24  ^ points  (multiplier, 1.0 = neutral)
	 *   Movement speed:              1.075 ^ points  (multiplier, 1.0 = neutral)
	 *   Attack speed:                1.15  ^ points  (multiplier, 1.0 = neutral)
	 *   Accuracy:                    1.30  ^ points  (multiplier, 1.0 = neutral; matches RingOfAccuracy)
	 *   Evasion:                     1.125 ^ points  (multiplier, 1.0 = neutral; matches RingOfEvasion)
	 *   Hitpoints:                   1.33  ^ points  (multiplier, 1.0 = neutral)
	 *   Armor:                       1.25  ^ points  (multiplier, 1.0 = neutral)
	 *   Luck:                        1.20  ^ (0.5 * points)  (multiplier, 1.0 = neutral;
	 *                                         equivalent to half a Ring of Wealth per point)
	 *   Vision:                      points  (flat, 0 = neutral)
	 *   X-ray vision:                points  (flat, 0 = neutral)
	 *
	 * NOTE: STAT_VISION, STAT_XRAY_VISION are flat values (0 = neutral).
	 * All other slots are multipliers (1.0 = neutral).
	 *
	 * @param levels array of length COUNT, each entry 0–3.
	 * @return float array of length STAT_COUNT.
	 */
	public static float[] computePlaystyleValues( int[] levels ) {
		int[] totalPoints = new int[STAT_COUNT];
		for (int cat = 0; cat < COUNT; cat++) {
			int lvl = levels[cat];
			if (lvl == 0) continue;
			for (int stat = 0; stat < STAT_COUNT; stat++) {
				totalPoints[stat] += lvl * POINTS[cat][stat];
			}
		}
		float[] out = new float[STAT_COUNT];
		out[STAT_MELEE_DAMAGE]   = (float) Math.pow(1.24, totalPoints[STAT_MELEE_DAMAGE]);
		out[STAT_RANGED_DAMAGE]  = (float) Math.pow(1.24, totalPoints[STAT_RANGED_DAMAGE]);
		out[STAT_MAGIC_DAMAGE]   = (float) Math.pow(1.24, totalPoints[STAT_MAGIC_DAMAGE]);
		out[STAT_MOVEMENT_SPEED] = (float) Math.pow(1.075, totalPoints[STAT_MOVEMENT_SPEED]);
		out[STAT_ATTACK_SPEED]   = (float) Math.pow(1.15, totalPoints[STAT_ATTACK_SPEED]);
		out[STAT_ACCURACY]       = (float) Math.pow(1.3, totalPoints[STAT_ACCURACY]);
		out[STAT_EVASION]        = (float) Math.pow(1.125, totalPoints[STAT_EVASION]);
		out[STAT_HITPOINTS]      = (float) Math.pow(1.33, totalPoints[STAT_HITPOINTS]);
		out[STAT_ARMOR]          = (float) Math.pow(1.25, totalPoints[STAT_ARMOR]);
		out[STAT_LUCK]           = (float) Math.pow(1.20, 0.5 * totalPoints[STAT_LUCK]);
		out[STAT_VISION]         = totalPoints[STAT_VISION];
		out[STAT_XRAY_VISION]    = totalPoints[STAT_XRAY_VISION];
		return out;
	}

}
