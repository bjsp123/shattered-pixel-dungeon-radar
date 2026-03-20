package com.shatteredpixel.shatteredpixeldungeon.windows;

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
	private static final int BTN_HEIGHT = 12;
	private static final int GAP        = 2;

	private final boolean editable;
	private final int[] currentLevels;
	private final RedButton[][] levelButtons;

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

			// Name row
			RenderedTextBlock name = PixelScene.renderTextBlock(
					Messages.titleCase(Messages.get(Playstyles.class, id)), 7 );
			name.setPos( 0, pos + 1 );
			PixelScene.align(name);
			add( name );

			IconButton info = new IconButton(Icons.get(Icons.INFO)) {
				@Override
				protected void onClick() {
					super.onClick();
					ShatteredPixelDungeon.scene().add(
							new WndMessage(Messages.get(Playstyles.class, id + "_desc"))
					);
				}
			};
			info.setRect( WIDTH - 16, pos, 16, 11 );
			add( info );

			pos += 11 + 1;

			// 4 level buttons: -1, 0, +1, +2
			String[] labels = {"0", "1", "2", "3"};
			float btnW = (WIDTH - 3f) / 4f;
			for (int j = 0; j < 4; j++) {
				final int level = j;
				RedButton btn = new RedButton(labels[j]) {
					@Override
					protected void onClick() {
						if (!editable) return;
						currentLevels[c] = level;
						updateButtons(c);
					}
				};
				btn.setRect( j * (btnW + 1), pos, btnW, BTN_HEIGHT );
				btn.active = editable;
				add( btn );
				levelButtons[cat][j] = btn;
			}
			updateButtons(cat);

			pos += BTN_HEIGHT;
		}

		if (!editable && SPDSettings.hasItemRequirements()) {
			pos += GAP * 3;
			RenderedTextBlock reqTitle = PixelScene.renderTextBlock( Messages.get(this, "requirements"), 8 );
			reqTitle.hardlight( TITLE_COLOR );
			reqTitle.setPos( 0, pos );
			PixelScene.align(reqTitle);
			add( reqTitle );
			pos = reqTitle.bottom() + GAP;

			String[] reqLabels = {"Floors 1-5", "Floors 6-10", "Floors 11-15"};
			String[] vals = {SPDSettings.seedfinderItemsLvl5(), SPDSettings.seedfinderItemsLvl10(), SPDSettings.seedfinderItemsLvl15()};
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

	private void updateButtons(int cat) {
		for (int j = 0; j < 4; j++) {
			RedButton btn = levelButtons[cat][j];
			if (btn == null) continue;
			int level = j;
			if (currentLevels[cat] == level) {
				btn.textColor(TITLE_COLOR);
			} else {
				btn.textColor(0xFFFFFF);
			}
		}
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
