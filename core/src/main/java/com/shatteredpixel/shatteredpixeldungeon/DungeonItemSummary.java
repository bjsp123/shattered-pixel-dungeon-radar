package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalMimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GoldenMimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrinketCatalyst;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import java.util.ArrayList;
import java.util.LinkedList;

public class DungeonItemSummary {

    /**
     * Returns a multi-line string of all items generated for the given seed over `floors` floors.
     * Each line: "{depth}: {item_title_lowercase}"
     * Trinkets are listed at depth 0.
     * Quest items appear at the floor where their quest is generated.
     */
    public String getItemsSummary(String seed, int floors) {
        String prevCustomSeed = SPDSettings.customSeed();
        SPDSettings.customSeed(seed);
        Dungeon.initSeed();
        SPDSettings.customSeed(prevCustomSeed);
        Dungeon.daily = false;
        GamesInProgress.selectedClass = HeroClass.WARRIOR;
        Dungeon.init();

        StringBuilder sb = new StringBuilder();

        // Trinkets — available from the start, listed at depth 0
        TrinketCatalyst cata = new TrinketCatalyst();
        int NUM_TRINKETS = TrinketCatalyst.WndTrinket.NUM_TRINKETS;
        while (cata.rolledTrinkets.size() < NUM_TRINKETS) {
            cata.rolledTrinkets.add((Trinket) Generator.random(Generator.Category.TRINKET));
        }
        for (Trinket t : cata.rolledTrinkets) {
            sb.append("0: ").append(t.identify().title().toLowerCase()).append("\n");
        }

        for (int i = 0; i < floors; i++) {
            Level l = Dungeon.newLevel();
            int depth = Dungeon.depth;

            // Heap items and mob drops
            ArrayList<Heap> heaps = new ArrayList<>(l.heaps.valueList());
            heaps.addAll(getMobDrops(l));
            for (Heap h : heaps) {
                if (h.type == Heap.Type.FOR_SALE) continue;
                for (Item item : h.items) {
                    item.identify();
                    sb.append(depth).append(": ").append(item.title().toLowerCase()).append("\n");
                }
            }

            // Sacrificial fire
            if (l.sacrificialFireItem != null) {
                sb.append(depth).append(": ")
                  .append(l.sacrificialFireItem.identify().title().toLowerCase())
                  .append(" (sacrificial fire)\n");
            }

            // Quest items at the floor where they are generated
            if (Ghost.Quest.armor != null) {
                sb.append(depth).append(": ")
                  .append(Ghost.Quest.armor.inscribe(Ghost.Quest.glyph).identify().title().toLowerCase())
                  .append("\n");
                sb.append(depth).append(": ")
                  .append(Ghost.Quest.weapon.enchant(Ghost.Quest.enchant).identify().title().toLowerCase())
                  .append("\n");
                Ghost.Quest.complete();
            }
            if (Wandmaker.Quest.wand1 != null) {
                sb.append(depth).append(": ")
                  .append(Wandmaker.Quest.wand1.identify().title().toLowerCase()).append("\n");
                sb.append(depth).append(": ")
                  .append(Wandmaker.Quest.wand2.identify().title().toLowerCase()).append("\n");
                Wandmaker.Quest.complete();
            }
            if (Blacksmith.Quest.type != 0) {
                Blacksmith.Quest.type = 0;
            }
            if (Imp.Quest.reward != null) {
                sb.append(depth).append(": ")
                  .append(Imp.Quest.reward.identify().title().toLowerCase()).append("\n");
                Imp.Quest.complete();
            }

            Dungeon.depth++;
        }

        return sb.toString();
    }

    private ArrayList<Heap> getMobDrops(Level l) {
        ArrayList<Heap> heaps = new ArrayList<>();
        for (Mob m : l.mobs) {
            if (m instanceof ArmoredStatue) {
                Heap h = new Heap();
                h.items = new LinkedList<>();
                h.items.add(((ArmoredStatue) m).armor.identify());
                h.items.add(((ArmoredStatue) m).weapon.identify());
                h.type = Heap.Type.STATUE;
                heaps.add(h);
            } else if (m instanceof Statue) {
                Heap h = new Heap();
                h.items = new LinkedList<>();
                h.items.add(((Statue) m).weapon.identify());
                h.type = Heap.Type.STATUE;
                heaps.add(h);
            } else if (m instanceof Mimic) {
                Heap h = new Heap();
                h.items = new LinkedList<>();
                for (Item item : ((Mimic) m).items) h.items.add(item.identify());
                if (m instanceof GoldenMimic)       h.type = Heap.Type.GOLDEN_MIMIC;
                else if (m instanceof CrystalMimic) h.type = Heap.Type.CRYSTAL_MIMIC;
                else                                 h.type = Heap.Type.MIMIC;
                heaps.add(h);
            }
        }
        return heaps;
    }
}
