package yuri;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import yuri.data.columns.dev.modules.CustomScoreboardModule;

/**
 * HUD layout screen for movable HUD elements.
 */
public final class YuriHudEditScreen extends Screen {
    private static final int TEXT_COLOR = 0xFFF6F7FB;
    private static final int MUTED_TEXT_COLOR = 0xB6C0CE;
    private static final Component HEADER = Component.literal("HUD Editor").withStyle(ChatFormatting.BOLD);
    private static final Component PLACEHOLDER = Component.literal("Enable Custom Scoreboard to edit HUD elements.");
    private static final Component HINT = Component.literal("Drag HUD elements to move");

    private final Screen returnScreen;

    public YuriHudEditScreen(Screen returnScreen) {
        super(Component.literal("HUD Editor"));
        this.returnScreen = returnScreen;
    }

    @Override
    public void removed() {
        YuriConfig.save();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, width, height, YuriTheme.backgroundTop(), YuriTheme.backgroundBottom());

        if (YuriHudEditLayout.isGridSnapEnabled()) {
            renderAlignmentGrid(graphics, width, height);
        }

        int[] slot = YuriHudEditLayout.slotLeftTopWidthHeight();
        if (slot != null) {
            boolean hovered = isInsideSlot(mouseX, mouseY, slot);
            renderToolbarSlot(
                graphics,
                slot[0],
                slot[1],
                slot[2],
                slot[3],
                mouseX,
                mouseY,
                "Grid Toggle",
                YuriHudEditLayout.isGridSnapEnabled(),
                hovered
            );
        }

        graphics.drawCenteredString(font, HEADER, width / 2, 14, TEXT_COLOR);
        if (!CustomScoreboardModule.shouldRender()) {
            graphics.drawCenteredString(font, PLACEHOLDER, width / 2, height / 2 - 6, MUTED_TEXT_COLOR);
        }
        graphics.drawCenteredString(font, HINT, width / 2, height - 36, MUTED_TEXT_COLOR);
        graphics.drawString(font, "Esc — back", 12, height - 20, MUTED_TEXT_COLOR, false);

        super.render(graphics, mouseX, mouseY, partialTick);

        if (CustomScoreboardModule.shouldRender()) {
            CustomScoreboardModule.renderHudEditor(graphics);
        }
    }

    private static void renderAlignmentGrid(GuiGraphics graphics, int screenWidth, int screenHeight) {
        int cell = YuriHudEditLayout.GRID_CELL_PX;
        int line = (YuriTheme.columnBorder() & 0x00FFFFFF) | 0x48000000;
        for (int x = 0; x <= screenWidth; x += cell) {
            graphics.fill(x, 0, x + 1, screenHeight, line);
        }
        for (int y = 0; y <= screenHeight; y += cell) {
            graphics.fill(0, y, screenWidth, y + 1, line);
        }
    }

    static void renderToolbarSlot(
        GuiGraphics graphics,
        net.minecraft.client.gui.Font font,
        int left,
        int top,
        int slotWidth,
        int slotHeight,
        int mouseX,
        int mouseY,
        String label,
        boolean toggledOn,
        boolean hovered
    ) {
        int right = left + slotWidth;
        int bottom = top + slotHeight;
        int fillColor = toggledOn ? YuriTheme.accentSoft() : 0x660C0E13;
        int borderColor = toggledOn ? YuriTheme.accent() : hovered ? YuriTheme.accentSoft() : YuriTheme.columnBorder();
        int textColor = toggledOn ? 0xFF000000 : TEXT_COLOR;

        graphics.fill(left + 2, top + 2, right + 2, bottom + 2, YuriTheme.shadow());
        graphics.fill(left, top, right, bottom, fillColor);
        graphics.fill(left, top, right, top + 1, borderColor);
        graphics.fill(left, bottom - 1, right, bottom, borderColor);
        graphics.fill(left, top, left + 1, bottom, borderColor);
        graphics.fill(right - 1, top, right, bottom, borderColor);

        int textWidth = font.width(label);
        int tx = left + (slotWidth - textWidth) / 2;
        int ty = top + (slotHeight - font.lineHeight) / 2;
        graphics.drawString(font, label, tx, ty, textColor, false);
    }

    private void renderToolbarSlot(
        GuiGraphics graphics,
        int left,
        int top,
        int slotWidth,
        int slotHeight,
        int mouseX,
        int mouseY,
        String label,
        boolean toggledOn,
        boolean hovered
    ) {
        renderToolbarSlot(graphics, font, left, top, slotWidth, slotHeight, mouseX, mouseY, label, toggledOn, hovered);
    }

    private static boolean isInsideSlot(double mouseX, double mouseY, int[] slot) {
        return mouseX >= slot[0] && mouseX < slot[0] + slot[2] && mouseY >= slot[1] && mouseY < slot[1] + slot[3];
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int[] slot = YuriHudEditLayout.slotLeftTopWidthHeight();
        if (slot != null && event.button() == 0 && isInsideSlot(event.x(), event.y(), slot)) {
            YuriHudEditLayout.toggleGridSnap();
            return true;
        }
        if (event.button() == 0 && CustomScoreboardModule.shouldRender() && CustomScoreboardModule.containsHudPoint(event.x(), event.y())) {
            CustomScoreboardModule.editorBeginDrag(event.x(), event.y());
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && CustomScoreboardModule.editorDragging()) {
            CustomScoreboardModule.editorEndDrag();
            YuriConfig.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (CustomScoreboardModule.editorDragging() && event.button() == 0) {
            CustomScoreboardModule.editorDragTo(event.x(), event.y(), width, height);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE) {
            if (minecraft != null) {
                minecraft.setScreen(returnScreen != null ? returnScreen : new YuriScreen());
            }
            return true;
        }
        return super.keyPressed(event);
    }
}
