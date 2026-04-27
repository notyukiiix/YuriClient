package yuri;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import yuri.data.columns.dev.modules.CustomScoreboardModule;
import yuri.data.columns.visual.modules.ImageHudModule;

/**
 * HUD layout screen: grid snap, Image HUD move (drag) and scale (scroll wheel).
 */
public final class YuriHudEditScreen extends Screen {
    private static final int TEXT_COLOR = 0xFFF6F7FB;
    private static final int MUTED_TEXT_COLOR = 0xB6C0CE;
    private static final Component HEADER = Component.literal("HUD Editor").withStyle(ChatFormatting.BOLD);
    private static final Component PLACEHOLDER = Component.literal("Enable Image HUD or Custom Scoreboard to edit HUD elements.");
    private static final Component HINT = Component.literal("Drag HUD elements to move · Scroll over image to scale");

    private final Screen returnScreen;

    public YuriHudEditScreen(Screen returnScreen) {
        super(Component.literal("HUD Editor"));
        this.returnScreen = returnScreen;
    }

    @Override
    protected void init() {
        super.init();
        // Do not always reload here: reloadTexture() releases the GPU texture first; a failed second load
        // would leave nothing on screen even when import just succeeded. Only reload if the path is set but not loaded.
        if (!ImageHudModule.hasRenderableImage() && !ImageHudModule.getImagePath().isEmpty()) {
            ImageHudModule.reloadTexture();
        }
    }

    @Override
    public void removed() {
        ImageHudModule.editorEndDrag();
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
        if (ImageHudModule.shouldShowHudEditorPlaceholder() && !CustomScoreboardModule.shouldRender()) {
            graphics.drawCenteredString(font, PLACEHOLDER, width / 2, height / 2 - 6, MUTED_TEXT_COLOR);
        }
        graphics.drawCenteredString(font, HINT, width / 2, height - 36, MUTED_TEXT_COLOR);
        graphics.drawString(font, "Esc — back", 12, height - 20, MUTED_TEXT_COLOR, false);

        super.render(graphics, mouseX, mouseY, partialTick);

        // Draw last so nothing in the screen pass sits above the draggable image.
        if (ImageHudModule.hasRenderableImage()) {
            ImageHudModule.renderImage(graphics);
            renderRemoveButton(graphics, mouseX, mouseY);
        }
        if (CustomScoreboardModule.shouldRender()) {
            CustomScoreboardModule.renderHudEditor(graphics);
        }
    }

    private void renderRemoveButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int[] bounds = ImageHudModule.editorRemoveButtonBounds(mouseX, mouseY);
        if (bounds == null) {
            return;
        }
        int left = bounds[0];
        int top = bounds[1];
        int right = bounds[2];
        int bottom = bounds[3];
        boolean hovered = isInsideRemoveButton(mouseX, mouseY);
        int fill = hovered ? 0xCCEA4D5A : 0xAA7A1A24;
        int border = hovered ? 0xFFFF8591 : 0xFFCF5D6A;
        graphics.fill(left, top, right, bottom, fill);
        graphics.fill(left, top, right, top + 1, border);
        graphics.fill(left, bottom - 1, right, bottom, border);
        graphics.fill(left, top, left + 1, bottom, border);
        graphics.fill(right - 1, top, right, bottom, border);
        graphics.drawString(font, "x", left + 4, top + 2, 0xFFFFFFFF, false);
    }

    private boolean isInsideRemoveButton(double mouseX, double mouseY) {
        int[] bounds = ImageHudModule.editorRemoveButtonBounds(mouseX, mouseY);
        if (bounds == null) {
            return false;
        }
        int left = bounds[0];
        int top = bounds[1];
        int right = bounds[2];
        int bottom = bounds[3];
        return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
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
        if (event.button() == 0 && ImageHudModule.editorRemoveImageAt(event.x(), event.y())) {
            YuriConfig.save();
            return true;
        }
        if (event.button() == 0 && ImageHudModule.hasRenderableImage() && ImageHudModule.containsHudPoint(event.x(), event.y())) {
            ImageHudModule.editorBeginDrag(event.x(), event.y());
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
        if (event.button() == 0 && ImageHudModule.editorDragging()) {
            ImageHudModule.editorEndDrag();
            YuriConfig.save();
            return true;
        }
        if (event.button() == 0 && CustomScoreboardModule.editorDragging()) {
            CustomScoreboardModule.editorEndDrag();
            YuriConfig.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (ImageHudModule.editorDragging() && event.button() == 0) {
            ImageHudModule.editorDragTo(event.x(), event.y(), width, height);
            return true;
        }
        if (CustomScoreboardModule.editorDragging() && event.button() == 0) {
            CustomScoreboardModule.editorDragTo(event.x(), event.y(), width, height);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (ImageHudModule.hasRenderableImage() && ImageHudModule.containsHudPoint(mouseX, mouseY)) {
            ImageHudModule.editorApplyScroll(scrollY);
            YuriConfig.save();
            return true;
        }
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
