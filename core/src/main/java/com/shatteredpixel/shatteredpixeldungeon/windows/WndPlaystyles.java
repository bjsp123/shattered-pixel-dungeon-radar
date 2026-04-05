package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.PlaystyleConfig;
import com.shatteredpixel.shatteredpixeldungeon.Playstyles;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndPlaystyles extends Window {

	private static final int WIDTH      = 120;
	private static final int TTL_HEIGHT = 16;
	private static final int ROW_HEIGHT = 11;
	private static final int NAME_WIDTH = 38;
	private static final int INFO_WIDTH = 12;
	private static final int GAP        = 1;

	private final boolean editable;
	private final int[] currentLevels;
	private final RedButton[][] levelButtons;

	private RenderedTextBlock summaryBlock;

	public WndPlaystyles( int[] levels, boolean editable ) {

		super();

		this.editable = editable;
		this.currentLevels = levels.clone();
		this.levelButtons = new RedButton[Playstyles.COUNT][4];

		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get(this, "title"), 12 );
		title.hardlight( TITLE_COLOR );
		title.setPos( (WIDTH - title.width()) / 2, (TTL_HEIGHT - title.height()) / 2 );
		PixelScene.align(title);
		add( title );

		float pos = TTL_HEIGHT;

		for (int cat = 0; cat < Playstyles.COUNT; cat++) {

			if (cat > 0) pos += GAP;

			final int c = cat;
			final String id = Playstyles.NAME_IDS[cat];

			// Single row: name | 4 level buttons | info button
			RenderedTextBlock name = PixelScene.renderTextBlock(
					Messages.titleCase(Messages.get(Playstyles.class, id)), 6 );
			name.setPos( 0, pos + (ROW_HEIGHT - name.height()) / 2 );
			PixelScene.align(name);
			add( name );

			IconButton info = new IconButton(Icons.get(Icons.INFO)) {
				@Override
				protected void onClick() {
					super.onClick();
					String flavor = Messages.get(Playstyles.class, id + "_desc");
					StringBuilder full = new StringBuilder(flavor);
					full.append("\n\n");
					full.append(buildBuffDebuffLine(c));
					ShatteredPixelDungeon.scene().add(new WndMessage(full.toString()));
				}
			};
			info.setRect( WIDTH - INFO_WIDTH, pos, INFO_WIDTH, ROW_HEIGHT );
			add( info );

			// 4 level buttons between name and info
			String[] labels = {"0", "1", "2", "3"};
			float btnsStart = NAME_WIDTH + 1;
			float btnsEnd   = WIDTH - INFO_WIDTH - 1;
			float btnW      = (btnsEnd - btnsStart - 3) / 4f;
			for (int j = 0; j < 4; j++) {
				final int level = j;
				RedButton btn = new RedButton(labels[j]) {
					@Override
					protected void onClick() {
						if (!editable) return;
						currentLevels[c] = level;
						updateButtons(c);
						updateSummary();
					}
				};
				btn.setRect( btnsStart + j * (btnW + 1), pos, btnW, ROW_HEIGHT );
				btn.active = editable;
				add( btn );
				levelButtons[cat][j] = btn;
			}
			updateButtons(cat);

			pos += ROW_HEIGHT;
		}

		// --- Summary section ---
		pos += GAP * 2;

		RenderedTextBlock summaryLabel = PixelScene.renderTextBlock( "Current bonuses:", 7 );
		summaryLabel.hardlight( TITLE_COLOR );
		summaryLabel.setPos( 0, pos );
		PixelScene.align(summaryLabel);
		add( summaryLabel );
		pos = summaryLabel.bottom() + GAP;

		summaryBlock = PixelScene.renderTextBlock( 7 );
		summaryBlock.maxWidth( WIDTH );
		summaryBlock.setPos( 0, pos );
		add( summaryBlock );
		updateSummary();

		// Reserve space for summary (up to ~4 wrapped lines at 7pt)
		pos += 40;

		if (!editable && SPDSettings.hasItemRequirements()) {
			pos += GAP * 3;
			RenderedTextBlock reqTitle = PixelScene.renderTextBlock( Messages.get(this, "requirements"), 8 );
			reqTitle.hardlight( TITLE_COLOR );
			reqTitle.setPos( 0, pos );
			PixelScene.align(reqTitle);
			add( reqTitle );
			pos = reqTitle.bottom() + GAP;

			String[] reqLabels = {"Floor 1", "Floor 2", "Floor 5", "Floor 10"};
			String[] vals = {SPDSettings.seedfinderItemsLvl1(), SPDSettings.seedfinderItemsLvl2(), SPDSettings.seedfinderItemsLvl5(), SPDSettings.seedfinderItemsLvl10()};
			for (int i = 0; i < 3; i++) {
				if (vals[i].isEmpty()) continue;
				RenderedTextBlock line = PixelScene.renderTextBlock( reqLabels[i] + ": " + vals[i], 6 );
				line.maxWidth(WIDTH);
				line.setPos( 0, pos );
				PixelScene.align(line);
				add( line );
				pos = line.bottom() + GAP;
			}
		}

		resize( WIDTH, (int)pos );
	}

	private void updateButtons( int cat ) {
		for (int j = 0; j < 4; j++) {
			RedButton btn = levelButtons[cat][j];
			if (btn == null) continue;
			if (currentLevels[cat] == j) {
				btn.textColor(TITLE_COLOR);
			} else {
				btn.textColor(0xFFFFFF);
			}
		}
	}

	private void updateSummary() {
		if (summaryBlock == null) return;
		PlaystyleConfig cfg = PlaystyleConfig.compute( currentLevels );
		StringBuilder sb = new StringBuilder();
		appendMult(sb, "Melee",    cfg.getActualMultiplier(PlaystyleConfig.PLAYSTYLE_MELEE_DAMAGE));
		appendMult(sb, "Ranged",   cfg.getActualMultiplier(PlaystyleConfig.PLAYSTYLE_RANGED_DAMAGE));
		appendMult(sb, "Magic",    cfg.getActualMultiplier(PlaystyleConfig.PLAYSTYLE_MAGIC_DAMAGE));
		appendMult(sb, "Move Spd", cfg.getActualMultiplier(PlaystyleConfig.PLAYSTYLE_MOVEMENT_SPEED));
		appendMult(sb, "Atk Spd",  cfg.getActualMultiplier(PlaystyleConfig.PLAYSTYLE_ATTACK_SPEED));
		appendMult(sb, "Accuracy", cfg.getActualMultiplier(PlaystyleConfig.PLAYSTYLE_ACCURACY));
		appendMult(sb, "Evasion",  cfg.getActualMultiplier(PlaystyleConfig.PLAYSTYLE_EVASION));
		appendMult(sb, "HP",       cfg.getActualMultiplier(PlaystyleConfig.PLAYSTYLE_HITPOINTS));
		appendMult(sb, "Armor",    cfg.getActualMultiplier(PlaystyleConfig.PLAYSTYLE_ARMOR));
		appendMult(sb, "Luck",     cfg.getActualMultiplier(PlaystyleConfig.PLAYSTYLE_LUCK));
		appendFlat(sb, "Vision",   Math.round(cfg.getActualMultiplier(PlaystyleConfig.PLAYSTYLE_VISION)));
		appendFlat(sb, "X-Ray",    Math.round(cfg.getActualMultiplier(PlaystyleConfig.PLAYSTYLE_XRAY_VISION)));
		summaryBlock.text( sb.length() > 0 ? sb.toString() : "None." );
	}

	private static final String[] STAT_LABELS = {
		"melee damage", "ranged damage", "magic damage", "move speed", "attack speed",
		"accuracy", "evasion", "HP", "armor", "luck", "vision", "x-ray vision"
	};

	private static String buildBuffDebuffLine( int cat ) {
		StringBuilder buffs   = new StringBuilder();
		StringBuilder debuffs = new StringBuilder();
		for (int stat = 0; stat < Playstyles.STAT_COUNT; stat++) {
			int pts = Playstyles.POINTS[cat][stat];
			if (pts == 0) continue;
			StringBuilder target = pts > 0 ? buffs : debuffs;
			if (target.length() > 0) target.append(", ");
			target.append(STAT_LABELS[stat]);
		}
		StringBuilder result = new StringBuilder();
		if (buffs.length()   > 0) result.append("Buffs: ").append(buffs);
		if (debuffs.length() > 0) {
			if (result.length() > 0) result.append("\n");
			result.append("Debuffs: ").append(debuffs);
		}
		return result.toString();
	}

	private static void appendMult(StringBuilder sb, String name, float mult) {
		int pct = Math.round((mult - 1f) * 100);
		if (pct == 0) return;
		if (sb.length() > 0) sb.append(", ");
		sb.append(name).append(" ");
		if (pct > 0) sb.append("+");
		sb.append(pct).append("%");
	}

	private static void appendFlat( StringBuilder sb, String name, int bonus ) {
		if (bonus == 0) return;
		if (sb.length() > 0) sb.append(", ");
		sb.append(name).append(" ");
		if (bonus > 0) sb.append("+");
		sb.append(bonus);
	}

	@Override
	public void onBackPressed() {
		if (editable) {
			for (int i = 0; i < Playstyles.COUNT; i++) {
				SPDSettings.playstyleLvl(i, currentLevels[i]);
			}
		}
		super.onBackPressed();
	}
}
