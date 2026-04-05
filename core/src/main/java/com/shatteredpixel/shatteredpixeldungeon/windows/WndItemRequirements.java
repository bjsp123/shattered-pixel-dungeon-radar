package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Game;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.TextInput;
import com.watabou.utils.DeviceCompat;

public class WndItemRequirements extends Window {

	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 16;
	private static final int INPUT_HEIGHT  = 36;

	// Text stored per slot; committed to settings on Save.
	private final String[] stored = new String[4];
	private int activeSlot = 0;

	// Background patches and text displays for all four slots.
	private final NinePatch[]        slotBg   = new NinePatch[4];
	private final RenderedTextBlock[] slotDisp = new RenderedTextBlock[4];
	private final float[] slotY = new float[4];
	private float slotW;

	// Single TextInput — repositioned to whichever slot is active.
	private TextInput input;

	public WndItemRequirements() {
		super();
		int width = PixelScene.landscape() ? 220 : 135;
		slotW = width - 2 * MARGIN;

		stored[0] = SPDSettings.seedfinderItemsLvl1();
		stored[1] = SPDSettings.seedfinderItemsLvl2();
		stored[2] = SPDSettings.seedfinderItemsLvl5();
		stored[3] = SPDSettings.seedfinderItemsLvl10();

		IconTitle titleBar = new IconTitle(Icons.get(Icons.MAGNIFY),
				Messages.get(this, "title"));
		titleBar.setRect(0, 0, width, 0);
		add(titleBar);
		float pos = titleBar.bottom() + MARGIN;

		RenderedTextBlock desc = PixelScene.renderTextBlock(Messages.get(this, "desc"), 6);
		desc.maxWidth(width);
		desc.setPos(0, pos);
		add(desc);
		pos = desc.bottom() + MARGIN * 2;

		int textSize = (int)(PixelScene.uiCamera.zoom * 6);

		String[] labelKeys = {"by_lvl1", "by_lvl2", "by_lvl5", "by_lvl10"};
		for (int i = 0; i < 4; i++) {
			RenderedTextBlock lbl = PixelScene.renderTextBlock(Messages.get(this, labelKeys[i]), 6);
			lbl.setPos(MARGIN, pos); add(lbl); pos = lbl.bottom() + 1;
			slotY[i] = pos;

			// Background — always visible so inactive slots still look like input fields.
			slotBg[i] = Chrome.get(Chrome.Type.TOAST_WHITE);
			slotBg[i].x = MARGIN;
			slotBg[i].y = slotY[i];
			slotBg[i].size(slotW, INPUT_HEIGHT);
			add(slotBg[i]);

			// Static text display shown only when this slot is NOT active.
			slotDisp[i] = PixelScene.renderTextBlock(stored[i], 6);
			slotDisp[i].maxWidth((int)(slotW - slotBg[i].marginHor()));
			slotDisp[i].setPos(MARGIN + slotBg[i].marginLeft(),
					slotY[i] + slotBg[i].marginTop());
			slotDisp[i].visible = (i != 0);   // slot 0 starts active
			add(slotDisp[i]);

			// Click area — switches focus to this slot.
			final int slotIdx = i;
			PointerArea area = new PointerArea(MARGIN, slotY[i], slotW, INPUT_HEIGHT) {
				@Override
				protected void onClick(PointerEvent event) {
					switchToSlot(slotIdx);
				}
			};
			add(area);

			pos += INPUT_HEIGHT + MARGIN;
		}

		// Single TextInput, starts on slot 0.
		input = new TextInput(Chrome.get(Chrome.Type.TOAST_WHITE), true, textSize);
		input.setText(stored[0]);
		input.setRect(MARGIN, slotY[0], slotW, INPUT_HEIGHT);
		add(input);

		// --- Save / Clear ---
		float btnW = (width - 3f * MARGIN) / 2f;
		RedButton btnSave = new RedButton(Messages.get(this, "save")) {
			@Override protected void onClick() {
				commitActive();
				SPDSettings.seedfinderItemsLvl1(stored[0]);
				SPDSettings.seedfinderItemsLvl2(stored[1]);
				SPDSettings.seedfinderItemsLvl5(stored[2]);
				SPDSettings.seedfinderItemsLvl10(stored[3]);
				onSaveOrClear();
				hide();
			}
		};
		btnSave.setRect(MARGIN, pos, btnW, BUTTON_HEIGHT); add(btnSave);

		RedButton btnClear = new RedButton(Messages.get(this, "clear")) {
			@Override protected void onClick() {
				SPDSettings.seedfinderItemsLvl1("");
				SPDSettings.seedfinderItemsLvl2("");
				SPDSettings.seedfinderItemsLvl5("");
				SPDSettings.seedfinderItemsLvl10("");
				onSaveOrClear();
				hide();
			}
		};
		btnClear.setRect(btnSave.right() + MARGIN, pos, btnW, BUTTON_HEIGHT); add(btnClear);
		pos += BUTTON_HEIGHT;

		resize(width, (int) pos);

		// Shift up for soft keyboard (mirrors WndTextInput lines 211-213)
		if (!DeviceCompat.hasHardKeyboard()) {
			offset(0, -(int)(Game.height / (4 * camera.zoom)));
			boundOffsetWithMargin(0);
		}

		// Re-layout TextInput after resize() establishes camera zoom (mirrors WndTextInput line 216)
		input.setRect(input.left(), input.top(), input.width(), INPUT_HEIGHT);
	}

	/** Save the current TextInput text back to the active slot's stored string. */
	private void commitActive() {
		stored[activeSlot] = input.getText();
	}

	/** Move the TextInput to a different slot, saving the current one first. */
	private void switchToSlot(int slot) {
		if (slot == activeSlot) return;
		commitActive();
		slotDisp[activeSlot].text(stored[activeSlot]);
		slotDisp[activeSlot].visible = true;

		activeSlot = slot;
		slotDisp[slot].visible = false;
		input.setText(stored[slot]);
		input.setRect(MARGIN, slotY[slot], slotW, INPUT_HEIGHT);
	}

	/** Override in caller to react to Save/Clear (e.g. refresh icon colors). */
	public void onSaveOrClear() { }

	@Override
	public void onBackPressed() { /* prevent accidental dismiss while typing */ }
}
