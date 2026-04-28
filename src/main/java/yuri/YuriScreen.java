package yuri;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import java.awt.Color;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import yuri.data.columns.general.modules.ChatModule;
import yuri.data.columns.cheats.modules.MobEspModule;
import yuri.data.columns.cheats.modules.TranslucentDoorModule;
import yuri.data.columns.dev.modules.OpsecModule;
import yuri.data.columns.visual.modules.ImageHudModule;
import yuri.data.columns.visual.modules.RenderOptimiserModule;

public final class YuriScreen extends Screen {
    private static final int TEXT_COLOR = 0xFFF6F7FB;
    private static final int MUTED_TEXT_COLOR = 0xB6C0CE;
    private static final Component TITLE_COMPONENT = Component.literal("Yuri Client").withStyle(ChatFormatting.BOLD);
    private static final Component SUBTITLE_COMPONENT = Component.literal("Right-Click Click GUI or Animations for Settings.");
    private static final int COLUMN_WIDTH = 128;
    private static final int HEADER_HEIGHT = 24;
    private static final int BODY_HEIGHT = 126;
    private static final int MODULE_HEIGHT = 18;
    private static final int MODULE_GAP = 6;
    private static final int HEADER_TEXT_Y = 8;
    private static final int SCREEN_PADDING = 10;
    private static final int POPUP_HEADER_HEIGHT = 18;
    private static final int CLICK_POPUP_WIDTH = 206;
    private static final int CLICK_POPUP_HEIGHT = 218;
    /** Padding from popup top to colour pickers. */
    private static final int CLICK_PICKER_TOP_OFFSET = 48;
    private static final int CLICK_PICKER_SV = 64;
    private static final int CLICK_PICKER_GAP = 6;
    private static final int CLICK_HUE_STRIP = 8;
    /** Space between SV+hue block and full-width hex row. */
    private static final int CLICK_GAP_BELOW_PICKER = 8;
    private static final int CLICK_HEX_ROW_HEIGHT = 22;
    /** Space between hex row and Open GUI Key row. */
    private static final int CLICK_GAP_HEX_TO_KEY = 8;
    private static final int CLICK_HEX_FIELD_INSET = 16;
    private static final int ANIM_POPUP_WIDTH = 236;
    private static final int ANIM_POPUP_HEIGHT = 244;
    private static final int PLAYER_SIZE_POPUP_WIDTH = 236;
    private static final int PLAYER_SIZE_POPUP_HEIGHT = 112;
    private static final int RENDER_OPTIMISER_POPUP_WIDTH = 248;
    private static final int RENDER_OPTIMISER_POPUP_HEIGHT = 232;
    private static final int IMAGE_HUD_POPUP_WIDTH = 260;
    private static final int IMAGE_HUD_POPUP_HEIGHT_COLLAPSED = 88;
    private static final int IMAGE_HUD_POPUP_HEIGHT_EXPANDED = 124;
    /** Vertical layout inside Image HUD popup (offsets from popup top). */
    private static final int IMAGE_HUD_LABEL_TOP = 20;
    private static final int IMAGE_HUD_PATH_TOP = 32;
    private static final int IMAGE_HUD_BROWSE_TOP = 52;
    private static final int IMAGE_HUD_BROWSE_HEIGHT = 16;
    private static final int IMAGE_HUD_PENDING_PROMPT_TOP = 70;
    private static final int IMAGE_HUD_PENDING_NAME_TOP = 81;
    private static final int IMAGE_HUD_PENDING_BUTTON_TOP = 94;
    private static final int IMAGE_HUD_PENDING_BUTTON_HEIGHT = 16;
    private static final int RENDER_OPTIMISER_ROW_HEIGHT = 18;
    private static final int CHAT_POPUP_WIDTH = 248;
    private static final int CHAT_POPUP_HEIGHT = 96;
    private static final int MOB_ESP_POPUP_WIDTH = 248;
    private static final int MOB_ESP_POPUP_HEIGHT = 262;
    private static final int MOB_ESP_PICKER_SV = 64;
    private static final int MOB_ESP_PICKER_GAP = 6;
    private static final int MOB_ESP_HUE_STRIP = 8;
    private static final int MOB_ESP_GAP_BELOW_PICKER = 8;
    private static final int MOB_ESP_HEX_ROW_HEIGHT = 22;
    private static final int MOB_ESP_HEX_FIELD_INSET = 16;
    private static final int TRANSLUCENT_DOOR_POPUP_WIDTH = 236;
    private static final int TRANSLUCENT_DOOR_POPUP_HEIGHT = 96;
    private static final int OPSEC_POPUP_WIDTH = 248;
    private static final int OPSEC_POPUP_HEIGHT = 78;
    private static final int CHAT_ROW_HEIGHT = 18;
    private static final int SLIDER_ROW_STEP = 22;
    private static final int SLIDER_BAR_HEIGHT = 4;
    private static final int POPUP_CLOSE_SIZE = 10;

    private int draggingIndex = -1;
    private int dragOffsetX;
    private int dragOffsetY;

    private PopupType activePopup = PopupType.NONE;
    private final List<PopupType> popupStack = new ArrayList<>();
    private boolean popupPositioned;
    private boolean draggingPopup;
    private int popupX;
    private int popupY;
    private int popupDragOffsetX;
    private int popupDragOffsetY;
    private int draggingSliderIndex = -1;
    private Integer previousGuiScale;
    private final int[] popupStoredX = new int[PopupType.values().length];
    private final int[] popupStoredY = new int[PopupType.values().length];
    private final boolean[] popupHasStoredPosition = new boolean[PopupType.values().length];

    private boolean syncingHexInput;
    private EditBox hexInput;
    private EditBox imageHudPathInput;
    private boolean syncingImageHudPathInput;
    private EditBox opsecNameInput;
    private boolean syncingOpsecNameInput;
    private boolean syncingMobEspHexInput;
    private EditBox mobEspHexInput;
    /** Absolute path chosen via file dialog; import runs only after user confirms. */
    private String imageHudPendingImportPath;
    private boolean waitingForOpenKeyBind;
    private float clickHue;
    private float clickSat;
    private float clickVal;
    private float mobEspHue;
    private float mobEspSat;
    private float mobEspVal;
    private boolean draggingClickSV;
    private boolean draggingClickHue;
    private boolean draggingMobEspSV;
    private boolean draggingMobEspHue;
    private int clickPopupWidth = CLICK_POPUP_WIDTH;
    private int clickPopupHeight = CLICK_POPUP_HEIGHT;
    private int animPopupWidth = ANIM_POPUP_WIDTH;
    private int animPopupHeight = ANIM_POPUP_HEIGHT;
    private int playerPopupWidth = PLAYER_SIZE_POPUP_WIDTH;
    private int playerPopupHeight = PLAYER_SIZE_POPUP_HEIGHT;
    private int renderOptimiserPopupWidth = RENDER_OPTIMISER_POPUP_WIDTH;
    private int renderOptimiserPopupHeight = RENDER_OPTIMISER_POPUP_HEIGHT;
    private int chatPopupWidth = CHAT_POPUP_WIDTH;
    private int chatPopupHeight = CHAT_POPUP_HEIGHT;
    private int mobEspPopupWidth = MOB_ESP_POPUP_WIDTH;
    private int mobEspPopupHeight = MOB_ESP_POPUP_HEIGHT;
    private int translucentDoorPopupWidth = TRANSLUCENT_DOOR_POPUP_WIDTH;
    private int translucentDoorPopupHeight = TRANSLUCENT_DOOR_POPUP_HEIGHT;
    private int opsecPopupWidth = OPSEC_POPUP_WIDTH;
    private int opsecPopupHeight = OPSEC_POPUP_HEIGHT;
    private int imageHudPopupWidth = IMAGE_HUD_POPUP_WIDTH;
    private int imageHudPopupHeight = IMAGE_HUD_POPUP_HEIGHT_COLLAPSED;
    private int renderOptimiserScroll;
    private boolean columnsInitialized;

    public YuriScreen() {
        super(Component.literal("Yuri"));
    }

    @Override
    protected void init() {
        applyConfiguredGuiScale();
        centerColumns(!columnsInitialized);
        columnsInitialized = true;
        if (activePopup == PopupType.NONE) {
            popupPositioned = false;
        } else if (!popupPositioned) {
            centerPopup();
            popupPositioned = true;
        } else {
            popupX = clamp(popupX, SCREEN_PADDING, Math.max(SCREEN_PADDING, width - getPopupWidth() - SCREEN_PADDING));
            popupY = clamp(popupY, SCREEN_PADDING, Math.max(SCREEN_PADDING, height - getPopupHeight() - SCREEN_PADDING));
        }

        hexInput = new EditBox(font, 0, 0, CLICK_POPUP_WIDTH - CLICK_HEX_FIELD_INSET * 2, 18, Component.literal("HEX"));
        hexInput.setMaxLength(7);
        hexInput.setBordered(false);
        hexInput.setTextColor(TEXT_COLOR);
        hexInput.setTextColorUneditable(MUTED_TEXT_COLOR);
        hexInput.setResponder(this::onHexChanged);
        syncHexInput();
        syncClickPickerFromTheme();
        hexInput.setVisible(activePopup == PopupType.CLICK_GUI);
        addRenderableWidget(hexInput);

        imageHudPathInput = new EditBox(font, 0, 0, IMAGE_HUD_POPUP_WIDTH - 20, 18, Component.literal("Image path"));
        imageHudPathInput.setMaxLength(4096);
        imageHudPathInput.setBordered(false);
        imageHudPathInput.setTextColor(TEXT_COLOR);
        imageHudPathInput.setTextColorUneditable(MUTED_TEXT_COLOR);
        imageHudPathInput.setResponder(this::onImageHudPathChanged);
        imageHudPathInput.setVisible(activePopup == PopupType.IMAGE_HUD);
        addRenderableWidget(imageHudPathInput);

        opsecNameInput = new EditBox(font, 0, 0, OPSEC_POPUP_WIDTH - 20, 18, Component.literal("Display name"));
        opsecNameInput.setMaxLength(256);
        opsecNameInput.setBordered(false);
        opsecNameInput.setTextColor(TEXT_COLOR);
        opsecNameInput.setTextColorUneditable(MUTED_TEXT_COLOR);
        opsecNameInput.setResponder(this::onOpsecNameChanged);
        opsecNameInput.setVisible(activePopup == PopupType.OPSEC);
        addRenderableWidget(opsecNameInput);

        mobEspHexInput = new EditBox(font, 0, 0, MOB_ESP_POPUP_WIDTH - MOB_ESP_HEX_FIELD_INSET * 2, 18, Component.literal("MOB HEX"));
        mobEspHexInput.setMaxLength(7);
        mobEspHexInput.setBordered(false);
        mobEspHexInput.setTextColor(TEXT_COLOR);
        mobEspHexInput.setTextColorUneditable(MUTED_TEXT_COLOR);
        mobEspHexInput.setResponder(this::onMobEspHexChanged);
        syncMobEspHexInput();
        syncMobEspPickerFromModule();
        mobEspHexInput.setVisible(activePopup == PopupType.MOB_ESP);
        addRenderableWidget(mobEspHexInput);

        layoutPopupWidgets();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        restorePreviousGuiScale();
        YuriConfig.save();
        super.removed();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (activePopup != PopupType.NONE) {
            PopupType clickedPopup = findPopupAt(event.x(), event.y());
            if (clickedPopup != PopupType.NONE) {
                focusPopup(clickedPopup, true);
            }

            if (event.button() == 0 && isInsidePopupClose(event.x(), event.y())) {
                closePopup();
                return true;
            }

            if (activePopup == PopupType.ANIMATIONS && event.button() == 0 && isInsideAnimationResetButton(event.x(), event.y())) {
                YuriData.ANIMATION_SETTINGS.resetToDefaults();
                YuriConfig.save();
                return true;
            }
            if (activePopup == PopupType.PLAYER_SIZE && event.button() == 0 && isInsidePlayerSizeResetButton(event.x(), event.y())) {
                YuriData.PLAYER_SIZE_SETTINGS.resetToDefaults();
                YuriConfig.save();
                return true;
            }
            if (activePopup == PopupType.TRANSLUCENT_DOOR && event.button() == 0 && isInsideTranslucentDoorResetButton(event.x(), event.y())) {
                TranslucentDoorModule.resetDoorAlphaToDefault();
                YuriConfig.save();
                return true;
            }

            if (activePopup == PopupType.CLICK_GUI && waitingForOpenKeyBind) {
                if (event.button() == 0 && isInsideRebindButton(event.x(), event.y())) {
                    return true;
                }

                YuriClientMod.setOpenGuiKey(InputConstants.Type.MOUSE.getOrCreate(event.button()));
                waitingForOpenKeyBind = false;
                return true;
            }

            if (activePopup == PopupType.CLICK_GUI && event.button() == 0) {
                if (isInsideClickGuiSVPicker(event.x(), event.y())) {
                    draggingClickSV = true;
                    updateClickGuiSVPicker(event.x(), event.y());
                    return true;
                }
                if (isInsideClickGuiHuePicker(event.x(), event.y())) {
                    draggingClickHue = true;
                    updateClickGuiHuePicker(event.y());
                    return true;
                }
            }

            if (isInsidePopupHeader(event.x(), event.y()) && event.button() == 0) {
                draggingPopup = true;
                popupDragOffsetX = (int) event.x() - popupX;
                popupDragOffsetY = (int) event.y() - popupY;
                return true;
            }

            if (activePopup == PopupType.ANIMATIONS && event.button() == 0) {
                int sliderIndex = getHoveredAnimationSlider(event.x(), event.y());
                if (sliderIndex >= 0) {
                    draggingSliderIndex = sliderIndex;
                    setAnimationSliderValue(sliderIndex, event.x());
                    return true;
                }

                if (isInsideIgnoreHasteToggle(event.x(), event.y())) {
                    YuriData.ANIMATION_SETTINGS.ignoreHaste = !YuriData.ANIMATION_SETTINGS.ignoreHaste;
                    return true;
                }
                if (isInsideIgnoreEquipToggle(event.x(), event.y())) {
                    YuriData.ANIMATION_SETTINGS.ignoreEquip = !YuriData.ANIMATION_SETTINGS.ignoreEquip;
                    return true;
                }
            }

            if (activePopup == PopupType.PLAYER_SIZE && event.button() == 0) {
                int sliderIndex = getHoveredPlayerSizeSlider(event.x(), event.y());
                if (sliderIndex >= 0) {
                    draggingSliderIndex = sliderIndex;
                    setPlayerSizeSliderValue(sliderIndex, event.x());
                    return true;
                }
            }
            if (activePopup == PopupType.TRANSLUCENT_DOOR && event.button() == 0) {
                if (isInsideTranslucentDoorSlider(event.x(), event.y())) {
                    draggingSliderIndex = 0;
                    setTranslucentDoorSliderValue(event.x());
                    return true;
                }
            }
            if (activePopup == PopupType.RENDER_OPTIMISER && event.button() == 0) {
                int optionIndex = getHoveredRenderOptimiserOption(event.x(), event.y());
                if (optionIndex >= 0) {
                    RenderOptimiserModule.toggle(optionIndex);
                    return true;
                }
            }
            if (activePopup == PopupType.CHAT && event.button() == 0) {
                int optionIndex = getHoveredChatOption(event.x(), event.y());
                if (optionIndex >= 0) {
                    ChatModule.toggle(optionIndex);
                    return true;
                }
            }
            if (activePopup == PopupType.MOB_ESP && event.button() == 0) {
                if (isInsideMobEspSVPicker(event.x(), event.y())) {
                    draggingMobEspSV = true;
                    updateMobEspSVPicker(event.x(), event.y());
                    return true;
                }
                if (isInsideMobEspHuePicker(event.x(), event.y())) {
                    draggingMobEspHue = true;
                    updateMobEspHuePicker(event.y());
                    return true;
                }
                int optionIndex = getHoveredMobEspOption(event.x(), event.y());
                if (optionIndex >= 0) {
                    MobEspModule.toggle(optionIndex);
                    return true;
                }
            }
            if (activePopup == PopupType.IMAGE_HUD && event.button() == 0) {
                if (imageHudPendingImportPath != null) {
                    if (isInsideImageHudConfirmAddButton(event.x(), event.y())) {
                        confirmImageHudPendingImport();
                        return true;
                    }
                    if (isInsideImageHudDiscardPendingButton(event.x(), event.y())) {
                        discardImageHudPendingSelection();
                        return true;
                    }
                }
                if (isInsideImageHudBrowseButton(event.x(), event.y())) {
                    openImageHudFilePicker();
                    return true;
                }
            }

            if (isInsidePopup(event.x(), event.y())) {
                if (activePopup == PopupType.CLICK_GUI && event.button() == 0 && isInsideRebindButton(event.x(), event.y())) {
                    waitingForOpenKeyBind = true;
                    return true;
                }
                if (event.button() == 0) {
                    return super.mouseClicked(event, doubleClick);
                }
                return true;
            }
        }

        if (activePopup == PopupType.NONE) {
            int[] editHudSlot = YuriHudEditLayout.slotLeftTopWidthHeight();
            if (editHudSlot != null && event.button() == 0) {
                if (event.x() >= editHudSlot[0] && event.x() < editHudSlot[0] + editHudSlot[2]
                    && event.y() >= editHudSlot[1] && event.y() < editHudSlot[1] + editHudSlot[3]) {
                    if (minecraft != null) {
                        minecraft.setScreen(new YuriHudEditScreen(this));
                    }
                    return true;
                }
            }
        }

        int hoveredIndex = getHoveredHeaderIndex(event.x(), event.y());
        if (hoveredIndex >= 0) {
            YuriData.Column column = YuriData.COLUMNS[hoveredIndex];
            if (event.button() == 0) {
                draggingIndex = hoveredIndex;
                dragOffsetX = (int) event.x() - column.x;
                dragOffsetY = (int) event.y() - column.y;
                return true;
            }

            if (event.button() == 1) {
                column.collapsed = !column.collapsed;
                return true;
            }
        }

        ModuleHit moduleHit = getHoveredModule(event.x(), event.y());
        if (moduleHit != null) {
            if (event.button() == 0) {
                if (isClickGuiModule(moduleHit.module)) {
                    resetGuiLayout();
                } else {
                    moduleHit.module.enabled = !moduleHit.module.enabled;
                }
                return true;
            }

            if (event.button() == 1) {
                if (isClickGuiModule(moduleHit.module)) {
                    openPopup(PopupType.CLICK_GUI);
                    return true;
                }
                if (isAnimationsModule(moduleHit.module)) {
                    openPopup(PopupType.ANIMATIONS);
                    return true;
                }
                if (isPlayerSizeModule(moduleHit.module)) {
                    openPopup(PopupType.PLAYER_SIZE);
                    return true;
                }
                if (isRenderOptimiserModule(moduleHit.module)) {
                    openPopup(PopupType.RENDER_OPTIMISER);
                    return true;
                }
                if (isChatModule(moduleHit.module)) {
                    openPopup(PopupType.CHAT);
                    return true;
                }
                if (isMobEspModule(moduleHit.module)) {
                    openPopup(PopupType.MOB_ESP);
                    return true;
                }
                if (isTranslucentDoorModule(moduleHit.module)) {
                    openPopup(PopupType.TRANSLUCENT_DOOR);
                    return true;
                }
                if (isOpsecModule(moduleHit.module)) {
                    openPopup(PopupType.OPSEC);
                    return true;
                }
                if (isImageHudModule(moduleHit.module)) {
                    openPopup(PopupType.IMAGE_HUD);
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (activePopup == PopupType.RENDER_OPTIMISER && isInsidePopup(mouseX, mouseY)) {
            int maxScroll = getRenderOptimiserMaxScroll();
            if (maxScroll > 0) {
                int direction = scrollY > 0 ? -1 : scrollY < 0 ? 1 : 0;
                if (direction != 0) {
                    renderOptimiserScroll = clamp(renderOptimiserScroll + direction * RENDER_OPTIMISER_ROW_HEIGHT, 0, maxScroll);
                    return true;
                }
            }
        }
        if (activePopup == PopupType.CHAT && isInsidePopup(mouseX, mouseY)) {
            return true;
        }
        if (activePopup == PopupType.MOB_ESP && isInsidePopup(mouseX, mouseY)) {
            return true;
        }
        if (activePopup == PopupType.OPSEC && isInsidePopup(mouseX, mouseY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (activePopup == PopupType.CLICK_GUI && waitingForOpenKeyBind) {
            int keyCode = event.key();
            if (keyCode == 256) {
                waitingForOpenKeyBind = false;
                return true;
            }

            YuriClientMod.setOpenGuiKey(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
            waitingForOpenKeyBind = false;
            return true;
        }
        if (event.key() == 256 && activePopup != PopupType.NONE) {
            closePopup();
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (activePopup == PopupType.CLICK_GUI && event.button() == 0) {
            if (draggingClickSV) {
                updateClickGuiSVPicker(event.x(), event.y());
                return true;
            }
            if (draggingClickHue) {
                updateClickGuiHuePicker(event.y());
                return true;
            }
        }
        if (activePopup == PopupType.MOB_ESP && event.button() == 0) {
            if (draggingMobEspSV) {
                updateMobEspSVPicker(event.x(), event.y());
                return true;
            }
            if (draggingMobEspHue) {
                updateMobEspHuePicker(event.y());
                return true;
            }
        }

        if (activePopup == PopupType.ANIMATIONS && draggingSliderIndex >= 0 && event.button() == 0) {
            setAnimationSliderValue(draggingSliderIndex, event.x());
            return true;
        }
        if (activePopup == PopupType.PLAYER_SIZE && draggingSliderIndex >= 0 && event.button() == 0) {
            setPlayerSizeSliderValue(draggingSliderIndex, event.x());
            return true;
        }
        if (activePopup == PopupType.TRANSLUCENT_DOOR && draggingSliderIndex >= 0 && event.button() == 0) {
            setTranslucentDoorSliderValue(event.x());
            return true;
        }
        if (draggingPopup && event.button() == 0) {
            int maxX = Math.max(SCREEN_PADDING, width - getPopupWidth() - SCREEN_PADDING);
            int maxY = Math.max(SCREEN_PADDING, height - getPopupHeight() - SCREEN_PADDING);
            popupX = clamp((int) event.x() - popupDragOffsetX, SCREEN_PADDING, maxX);
            popupY = clamp((int) event.y() - popupDragOffsetY, SCREEN_PADDING, maxY);
            layoutPopupWidgets();
            return true;
        }
        if (draggingIndex < 0 || event.button() != 0) {
            return super.mouseDragged(event, dragX, dragY);
        }

        YuriData.Column column = YuriData.COLUMNS[draggingIndex];
        int maxX = Math.max(SCREEN_PADDING, width - COLUMN_WIDTH - SCREEN_PADDING);
        int maxY = Math.max(SCREEN_PADDING, height - HEADER_HEIGHT - SCREEN_PADDING);
        column.x = clamp((int) event.x() - dragOffsetX, SCREEN_PADDING, maxX);
        column.y = clamp((int) event.y() - dragOffsetY, SCREEN_PADDING, maxY);
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && draggingSliderIndex >= 0) {
            draggingSliderIndex = -1;
            return true;
        }
        if (event.button() == 0 && (draggingClickSV || draggingClickHue)) {
            draggingClickSV = false;
            draggingClickHue = false;
            return true;
        }
        if (event.button() == 0 && (draggingMobEspSV || draggingMobEspHue)) {
            draggingMobEspSV = false;
            draggingMobEspHue = false;
            return true;
        }

        if (event.button() == 0 && draggingPopup) {
            draggingPopup = false;
            return true;
        }
        if (event.button() == 0 && draggingIndex >= 0) {
            draggingIndex = -1;
            return true;
        }

        return super.mouseReleased(event);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, width, height, YuriTheme.backgroundTop(), YuriTheme.backgroundBottom());
        graphics.drawCenteredString(font, TITLE_COMPONENT, width / 2, 14, TEXT_COLOR);
        graphics.drawCenteredString(font, SUBTITLE_COMPONENT, width / 2, 27, MUTED_TEXT_COLOR);

        for (int index = 0; index < YuriData.COLUMNS.length; index++) {
            renderColumn(graphics, YuriData.COLUMNS[index], mouseX, mouseY, index == draggingIndex);
        }

        if (activePopup == PopupType.NONE) {
            int[] editHudSlot = YuriHudEditLayout.slotLeftTopWidthHeight();
            if (editHudSlot != null) {
                boolean hovered = mouseX >= editHudSlot[0] && mouseX < editHudSlot[0] + editHudSlot[2]
                    && mouseY >= editHudSlot[1] && mouseY < editHudSlot[1] + editHudSlot[3];
                YuriHudEditScreen.renderToolbarSlot(
                    graphics,
                    font,
                    editHudSlot[0],
                    editHudSlot[1],
                    editHudSlot[2],
                    editHudSlot[3],
                    mouseX,
                    mouseY,
                    "Edit Hud",
                    false,
                    hovered
                );
            }
        }

        renderPopups(graphics, mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderColumn(GuiGraphics graphics, YuriData.Column column, int mouseX, int mouseY, boolean dragging) {
        int left = column.x;
        int top = column.y;
        int right = left + COLUMN_WIDTH;
        int headerBottom = top + HEADER_HEIGHT;
        boolean hasModules = column.modules.length > 0;
        int bodyHeight = getColumnBodyHeight(column);
        int bodyBottom = headerBottom + bodyHeight;
        boolean hovered = isInsideHeader(column, mouseX, mouseY);

        int shadowBottom = headerBottom + (column.collapsed || !hasModules ? 2 : bodyHeight + 4);
        graphics.fill(left + 2, top + 3, right + 2, shadowBottom, YuriTheme.shadow());
        graphics.fill(left, top, right, headerBottom, YuriTheme.headerBase());
        graphics.fill(left, top, right, top + 2, dragging || hovered ? YuriTheme.accent() : YuriTheme.accentSoft());
        graphics.fill(left, headerBottom - 1, right, headerBottom, YuriTheme.accentMuted());
        graphics.fill(left, top, left + 1, headerBottom, YuriTheme.columnBorder());
        graphics.fill(right - 1, top, right, headerBottom, YuriTheme.columnBorder());

        if (!column.collapsed && hasModules) {
            graphics.fill(left, headerBottom, right, bodyBottom, YuriTheme.columnBody());
            graphics.fill(left, headerBottom, right, headerBottom + 1, YuriTheme.columnBorder());
            graphics.fill(left, bodyBottom - 1, right, bodyBottom, YuriTheme.columnBorder());
            graphics.fill(left, headerBottom, left + 1, bodyBottom, YuriTheme.columnBorder());
            graphics.fill(right - 1, headerBottom, right, bodyBottom, YuriTheme.columnBorder());
        }

        graphics.drawCenteredString(font, column.title, left + COLUMN_WIDTH / 2, top + HEADER_TEXT_Y, YuriTheme.accent());
        graphics.drawString(font, column.collapsed ? "+" : "-", right - 10, top + 8, hovered || dragging ? TEXT_COLOR : MUTED_TEXT_COLOR, false);

        if (!column.collapsed && hasModules) {
            int moduleY = headerBottom + 8;
            for (YuriData.Module module : column.modules) {
                renderModule(graphics, left + 6, right - 6, moduleY, mouseX, mouseY, module);
                moduleY += MODULE_HEIGHT + MODULE_GAP;
            }
        }
    }

    private int getColumnBodyHeight(YuriData.Column column) {
        if (column.modules.length == 0) {
            return 0;
        }
        int moduleAreaHeight = column.modules.length * MODULE_HEIGHT + (column.modules.length - 1) * MODULE_GAP;
        return 8 + moduleAreaHeight + 8;
    }

    private void renderModule(GuiGraphics graphics, int left, int right, int top, int mouseX, int mouseY, YuriData.Module module) {
        int bottom = top + MODULE_HEIGHT;
        boolean hovered = mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
        boolean clickGuiModule = isClickGuiModule(module);
        int fillColor = clickGuiModule || module.enabled ? YuriTheme.accentSoft() : 0x660C0E13;
        int borderColor = clickGuiModule || module.enabled ? YuriTheme.accent() : hovered ? YuriTheme.accentSoft() : YuriTheme.columnBorder();
        int textColor = module.enabled ? 0xFF000000 : TEXT_COLOR;
        String stateText = clickGuiModule ? "RST" : "";
        int stateColor = clickGuiModule ? TEXT_COLOR : (module.enabled ? 0xFF000000 : TEXT_COLOR);

        graphics.fill(left, top, right, bottom, fillColor);
        graphics.fill(left, top, right, top + 1, borderColor);
        graphics.fill(left, bottom - 1, right, bottom, borderColor);
        graphics.fill(left, top, left + 1, bottom, borderColor);
        graphics.fill(right - 1, top, right, bottom, borderColor);
        graphics.drawString(font, module.title, left + 6, top + 5, textColor, false);
        if (!stateText.isEmpty()) {
            graphics.drawString(font, stateText, right - 22, top + 5, stateColor, false);
        }
    }

    private void renderPopups(GuiGraphics graphics, int mouseX, int mouseY) {
        if (popupStack.isEmpty() || activePopup == PopupType.NONE) {
            if (hexInput != null) {
                hexInput.setVisible(false);
            }
            if (imageHudPathInput != null) {
                imageHudPathInput.setVisible(false);
            }
            if (opsecNameInput != null) {
                opsecNameInput.setVisible(false);
            }
            if (mobEspHexInput != null) {
                mobEspHexInput.setVisible(false);
            }
            return;
        }
        PopupType focused = activePopup;
        int focusedX = popupX;
        int focusedY = popupY;

        for (PopupType popupType : popupStack) {
            if (popupType == PopupType.NONE) {
                continue;
            }
            if (popupType != focused) {
                applyPopupPosition(popupType);
            } else {
                activePopup = focused;
                popupX = focusedX;
                popupY = focusedY;
            }
            layoutPopupWidgets();
            int left = popupX;
            int top = popupY;
            int right = left + getPopupWidth();
            int bottom = top + getPopupHeight();
            boolean interactive = popupType == focused;
            boolean headerHovered = interactive && isInsidePopupHeader(mouseX, mouseY);

            graphics.fill(left + 3, top + 4, right + 3, bottom + 4, YuriTheme.shadow());
            graphics.fill(left, top, right, bottom, YuriTheme.subGuiBase());
            graphics.fill(left, top, right, top + POPUP_HEADER_HEIGHT, 0xF0131820);
            graphics.fill(left, top, right, top + 2, YuriTheme.accent());
            graphics.fill(left, top, left + 1, bottom, YuriTheme.columnBorder());
            graphics.fill(right - 1, top, right, bottom, YuriTheme.columnBorder());
            graphics.fill(left, bottom - 1, right, bottom, YuriTheme.columnBorder());
            graphics.drawString(font, headerHovered || (interactive && draggingPopup) ? "Drag" : "Move", right - 44, top + 5, MUTED_TEXT_COLOR, false);
            if (popupType == PopupType.ANIMATIONS || popupType == PopupType.PLAYER_SIZE || popupType == PopupType.TRANSLUCENT_DOOR) {
                boolean resetHovered = interactive && (popupType == PopupType.ANIMATIONS
                    ? isInsideAnimationResetButton(mouseX, mouseY)
                    : popupType == PopupType.PLAYER_SIZE
                        ? isInsidePlayerSizeResetButton(mouseX, mouseY)
                        : isInsideTranslucentDoorResetButton(mouseX, mouseY));
                renderPopupResetButton(graphics, left, top, right, resetHovered);
            }
            graphics.drawString(font, "x", right - 12, top + 5, interactive && isInsidePopupClose(mouseX, mouseY) ? YuriTheme.accent() : TEXT_COLOR, false);

            if (popupType == PopupType.CLICK_GUI) {
                renderClickGuiPopup(graphics, left, top);
            } else if (popupType == PopupType.ANIMATIONS) {
                renderAnimationsPopup(graphics, left, top);
            } else if (popupType == PopupType.PLAYER_SIZE) {
                renderPlayerSizePopup(graphics, left, top);
            } else if (popupType == PopupType.RENDER_OPTIMISER) {
                renderRenderOptimiserPopup(graphics, left, top);
            } else if (popupType == PopupType.CHAT) {
                renderChatPopup(graphics, left, top);
            } else if (popupType == PopupType.MOB_ESP) {
                renderMobEspPopup(graphics, left, top);
            } else if (popupType == PopupType.TRANSLUCENT_DOOR) {
                renderTranslucentDoorPopup(graphics, left, top);
            } else if (popupType == PopupType.OPSEC) {
                renderOpsecPopup(graphics, left, top);
            } else if (popupType == PopupType.IMAGE_HUD) {
                renderImageHudPopup(graphics, left, top, mouseX, mouseY);
            }
        }

        activePopup = focused;
        popupX = focusedX;
        popupY = focusedY;
        layoutPopupWidgets();
    }

    private void renderPlayerSizePopup(GuiGraphics graphics, int left, int top) {
        var settings = YuriData.PLAYER_SIZE_SETTINGS;
        graphics.drawString(font, "Player Size", left + 8, top + 5, YuriTheme.accent(), false);
        int popupWidth = getPopupWidth();
        graphics.fill(left + 10, top + 24, left + popupWidth - 10, top + getPopupHeight() - 12, 0x660A0D12);

        renderPlayerSizeSlider(graphics, left, top, 0, "X", settings.scaleX);
        renderPlayerSizeSlider(graphics, left, top, 1, "Y", settings.scaleY);
        renderPlayerSizeSlider(graphics, left, top, 2, "Z", settings.scaleZ);
    }

    private void renderPlayerSizeSlider(GuiGraphics graphics, int popupLeft, int popupTop, int index, String axis, float value) {
        int rowTop = popupTop + 34 + index * SLIDER_ROW_STEP;
        int sliderLeft = popupLeft + 30;
        int sliderWidth = getPopupWidth() - 60;
        int sliderRight = sliderLeft + sliderWidth;
        int barTop = rowTop + 11;
        int barBottom = barTop + SLIDER_BAR_HEIGHT;
        float ratio = normalize(value, 0.20F, 3.00F);
        int fillRight = sliderLeft + Math.round(sliderWidth * ratio);
        fillRight = clamp(fillRight, sliderLeft, sliderRight);
        int textY = rowTop + 2;
        String valueText = formatFloat(value);
        int valueX = sliderRight - font.width(valueText);
        int axisX = sliderLeft;
        int axisY = barTop - font.lineHeight - 1;

        graphics.drawString(font, axis, axisX, axisY, TEXT_COLOR, false);
        graphics.drawString(font, valueText, valueX, textY, YuriTheme.accent(), false);
        graphics.fill(sliderLeft, barTop, sliderRight, barBottom, 0xAA0A0D12);
        graphics.fill(sliderLeft, barTop, fillRight, barBottom, YuriTheme.accentSoft());
        graphics.fill(sliderLeft, barTop, sliderRight, barTop + 1, YuriTheme.columnBorder());
        graphics.fill(sliderLeft, barBottom - 1, sliderRight, barBottom, YuriTheme.columnBorder());
        graphics.fill(fillRight - 1, barTop - 2, fillRight + 1, barBottom + 2, YuriTheme.accent());
    }

    private void renderClickGuiPopup(GuiGraphics graphics, int left, int top) {
        int right = left + getPopupWidth();
        graphics.drawString(font, "Click GUI", left + 8, top + 5, YuriTheme.accent(), false);
        int innerLeft = left + 10;
        int innerRight = right - 10;
        int rowHeight = 18;

        // Row 1: Accent color
        int row1Top = top + 24;
        int row1Bottom = row1Top + rowHeight;
        graphics.fill(innerLeft, row1Top, innerRight, row1Bottom, 0x660A0D12);
        graphics.drawString(font, "Accent Color", left + 14, row1Top + 5, TEXT_COLOR, false);
        graphics.fill(innerRight - 18, row1Top + 5, innerRight - 8, row1Top + 15, 0xFF0A0C10);
        graphics.fill(innerRight - 17, row1Top + 6, innerRight - 9, row1Top + 14, YuriTheme.accent());
        renderClickGuiColorPicker(graphics, left, top);

        int hexRowTop = clickGuiHexRowTop(top);
        int hexRowBottom = hexRowTop + CLICK_HEX_ROW_HEIGHT;
        graphics.fill(innerLeft, hexRowTop, innerRight, hexRowBottom, 0x660A0D12);
        graphics.drawString(font, "Hex", left + 14, hexRowTop + 5, TEXT_COLOR, false);
        int hexFieldLeft = innerLeft + 6;
        int hexFieldRight = innerRight - 6;
        int hexFieldTop = hexRowTop + 3;
        int hexFieldBottom = hexRowBottom - 3;
        graphics.fill(hexFieldLeft, hexFieldTop, hexFieldRight, hexFieldBottom, YuriTheme.subGuiInput());

        // Row 2: Open key + compact rebind button
        int row2Top = clickGuiKeyRowTopInt(top);
        int row2Bottom = row2Top + rowHeight;
        graphics.fill(innerLeft, row2Top, innerRight, row2Bottom, 0x660A0D12);
        graphics.drawString(font, "Open GUI Key", left + 14, row2Top + 5, TEXT_COLOR, false);
        String keyName = YuriClientMod.getOpenGuiKeyName();
        int keyX = innerRight - 8 - font.width(keyName);
        graphics.drawString(font, keyName, keyX, row2Top + 5, YuriTheme.accent(), false);

        int buttonRight = innerRight - 8;
        int buttonTop = clickGuiRebindButtonTopInt(top);
        int buttonLeft = buttonRight - 62;
        int buttonBottom = buttonTop + 14;
        int buttonFill = waitingForOpenKeyBind ? YuriTheme.accentSoft() : 0xAA0A0D12;
        int buttonBorder = waitingForOpenKeyBind ? YuriTheme.accent() : YuriTheme.columnBorder();
        graphics.fill(buttonLeft, buttonTop, buttonRight, buttonBottom, buttonFill);
        graphics.fill(buttonLeft, buttonTop, buttonRight, buttonTop + 1, buttonBorder);
        graphics.fill(buttonLeft, buttonBottom - 1, buttonRight, buttonBottom, buttonBorder);
        graphics.fill(buttonLeft, buttonTop, buttonLeft + 1, buttonBottom, buttonBorder);
        graphics.fill(buttonRight - 1, buttonTop, buttonRight, buttonBottom, buttonBorder);
        graphics.drawString(font, waitingForOpenKeyBind ? "Listening..." : "Rebind", buttonLeft + 6, buttonTop + 3, TEXT_COLOR, false);
        graphics.drawString(font, waitingForOpenKeyBind ? "Press Any Key Or Mouse" : "Esc Cancels Key Capture", left + 14, buttonBottom + 4, MUTED_TEXT_COLOR, false);

    }

    private void renderClickGuiColorPicker(GuiGraphics graphics, int popupLeft, int anchorTop) {
        int svLeft = popupLeft + 12;
        int svTop = anchorTop + CLICK_PICKER_TOP_OFFSET;
        int svSize = CLICK_PICKER_SV;
        int hueLeft = svLeft + svSize + CLICK_PICKER_GAP;
        int hueWidth = CLICK_HUE_STRIP;
        int pickerBottom = svTop + svSize;
        int svRight = svLeft + svSize;

        int hueColor = Color.HSBtoRGB(clickHue, 1.0F, 1.0F);
        graphics.fill(svLeft, svTop, svRight, pickerBottom, 0xFF000000 | (hueColor & 0xFFFFFF));
        for (int x = 0; x < svSize; x++) {
            int alpha = 255 - Math.round((x / (float) (svSize - 1)) * 255.0F);
            graphics.fill(svLeft + x, svTop, svLeft + x + 1, pickerBottom, (alpha << 24) | 0xFFFFFF);
        }
        for (int y = 0; y < svSize; y++) {
            int alpha = Math.round((y / (float) (svSize - 1)) * 255.0F);
            graphics.fill(svLeft, svTop + y, svRight, svTop + y + 1, (alpha << 24));
        }
        graphics.fill(svLeft, svTop, svRight, svTop + 1, YuriTheme.columnBorder());
        graphics.fill(svLeft, pickerBottom - 1, svRight, pickerBottom, YuriTheme.columnBorder());
        graphics.fill(svLeft, svTop, svLeft + 1, pickerBottom, YuriTheme.columnBorder());
        graphics.fill(svRight - 1, svTop, svRight, pickerBottom, YuriTheme.columnBorder());

        int markerX = svLeft + Math.round(clickSat * (svSize - 1));
        int markerY = svTop + Math.round((1.0F - clickVal) * (svSize - 1));
        graphics.fill(markerX - 2, markerY - 2, markerX + 2, markerY + 2, 0xFFFFFFFF);
        graphics.fill(markerX - 1, markerY - 1, markerX + 1, markerY + 1, 0xFF000000);

        for (int y = 0; y < svSize; y++) {
            float hue = y / (float) (svSize - 1);
            int rgb = Color.HSBtoRGB(hue, 1.0F, 1.0F);
            graphics.fill(hueLeft, svTop + y, hueLeft + hueWidth, svTop + y + 1, 0xFF000000 | (rgb & 0xFFFFFF));
        }
        int hueMarkerY = svTop + Math.round(clickHue * (svSize - 1));
        graphics.fill(hueLeft - 1, hueMarkerY - 1, hueLeft + hueWidth + 1, hueMarkerY + 1, 0xFFFFFFFF);
        graphics.fill(hueLeft, svTop, hueLeft + hueWidth, svTop + 1, YuriTheme.columnBorder());
        graphics.fill(hueLeft, pickerBottom - 1, hueLeft + hueWidth, pickerBottom, YuriTheme.columnBorder());
        graphics.fill(hueLeft, svTop, hueLeft + 1, pickerBottom, YuriTheme.columnBorder());
        graphics.fill(hueLeft + hueWidth - 1, svTop, hueLeft + hueWidth, pickerBottom, YuriTheme.columnBorder());
    }

    private void renderMobEspColorPicker(GuiGraphics graphics, int popupLeft, int svTop) {
        int svLeft = popupLeft + 12;
        int svSize = MOB_ESP_PICKER_SV;
        int hueLeft = svLeft + svSize + MOB_ESP_PICKER_GAP;
        int hueWidth = MOB_ESP_HUE_STRIP;
        int pickerBottom = svTop + svSize;
        int svRight = svLeft + svSize;

        int hueColor = Color.HSBtoRGB(mobEspHue, 1.0F, 1.0F);
        graphics.fill(svLeft, svTop, svRight, pickerBottom, 0xFF000000 | (hueColor & 0xFFFFFF));
        for (int x = 0; x < svSize; x++) {
            int alpha = 255 - Math.round((x / (float) (svSize - 1)) * 255.0F);
            graphics.fill(svLeft + x, svTop, svLeft + x + 1, pickerBottom, (alpha << 24) | 0xFFFFFF);
        }
        for (int y = 0; y < svSize; y++) {
            int alpha = Math.round((y / (float) (svSize - 1)) * 255.0F);
            graphics.fill(svLeft, svTop + y, svRight, svTop + y + 1, (alpha << 24));
        }
        graphics.fill(svLeft, svTop, svRight, svTop + 1, YuriTheme.columnBorder());
        graphics.fill(svLeft, pickerBottom - 1, svRight, pickerBottom, YuriTheme.columnBorder());
        graphics.fill(svLeft, svTop, svLeft + 1, pickerBottom, YuriTheme.columnBorder());
        graphics.fill(svRight - 1, svTop, svRight, pickerBottom, YuriTheme.columnBorder());

        int markerX = svLeft + Math.round(mobEspSat * (svSize - 1));
        int markerY = svTop + Math.round((1.0F - mobEspVal) * (svSize - 1));
        graphics.fill(markerX - 2, markerY - 2, markerX + 2, markerY + 2, 0xFFFFFFFF);
        graphics.fill(markerX - 1, markerY - 1, markerX + 1, markerY + 1, 0xFF000000);

        for (int y = 0; y < svSize; y++) {
            float hue = y / (float) (svSize - 1);
            int rgb = Color.HSBtoRGB(hue, 1.0F, 1.0F);
            graphics.fill(hueLeft, svTop + y, hueLeft + hueWidth, svTop + y + 1, 0xFF000000 | (rgb & 0xFFFFFF));
        }
        int hueMarkerY = svTop + Math.round(mobEspHue * (svSize - 1));
        graphics.fill(hueLeft - 1, hueMarkerY - 1, hueLeft + hueWidth + 1, hueMarkerY + 1, 0xFFFFFFFF);
        graphics.fill(hueLeft, svTop, hueLeft + hueWidth, svTop + 1, YuriTheme.columnBorder());
        graphics.fill(hueLeft, pickerBottom - 1, hueLeft + hueWidth, pickerBottom, YuriTheme.columnBorder());
        graphics.fill(hueLeft, svTop, hueLeft + 1, pickerBottom, YuriTheme.columnBorder());
        graphics.fill(hueLeft + hueWidth - 1, svTop, hueLeft + hueWidth, pickerBottom, YuriTheme.columnBorder());
    }

    private void renderAnimationsPopup(GuiGraphics graphics, int left, int top) {
        var settings = YuriData.ANIMATION_SETTINGS;
        graphics.drawString(font, "Animations", left + 8, top + 5, YuriTheme.accent(), false);
        int popupWidth = getPopupWidth();
        graphics.fill(left + 10, top + 24, left + popupWidth - 10, top + 72, 0x660A0D12);
        graphics.fill(left + 10, top + 72, left + popupWidth - 10, top + 160, 0x660A0D12);
        graphics.fill(left + 10, top + 160, left + popupWidth - 10, top + 234, 0x660A0D12);

        graphics.drawString(font, "Size: " + clamp(settings.size, 1, 20), left + 14, top + 28, TEXT_COLOR, false);
        renderSizeSlider(graphics, left, top, 0, settings.size);

        graphics.drawString(font, "Pos:", left + 14, top + 76, TEXT_COLOR, false);
        renderAnimationSlider(graphics, left, top, 1, "X", settings.posX, -2.00F, 2.00F);
        renderAnimationSlider(graphics, left, top, 2, "Y", settings.posY, -2.00F, 2.00F);
        renderAnimationSlider(graphics, left, top, 3, "Z", settings.posZ, -2.00F, 2.00F);

        graphics.drawString(font, "Swing Speed: " + clamp(settings.swingSpeed, 0, 20), left + 14, top + 164, TEXT_COLOR, false);
        renderSwingSlider(graphics, left, top, 4, settings.swingSpeed);
        renderIgnoreHasteToggle(graphics, left, top, settings.ignoreHaste);
        renderIgnoreEquipToggle(graphics, left, top, settings.ignoreEquip);
    }

    private void renderRenderOptimiserPopup(GuiGraphics graphics, int left, int top) {
        graphics.drawString(font, "Render Optimiser", left + 8, top + 5, YuriTheme.accent(), false);
        int innerLeft = left + 10;
        int innerRight = left + getPopupWidth() - 10;
        int rowHeight = RENDER_OPTIMISER_ROW_HEIGHT;
        int rowStart = top + 24;
        int viewportTop = rowStart;
        int viewportBottom = top + getPopupHeight() - 10;
        int maxScroll = getRenderOptimiserMaxScroll();
        renderOptimiserScroll = clamp(renderOptimiserScroll, 0, maxScroll);
        int optionCount = RenderOptimiserModule.optionCount();
        int contentRight = innerRight - 8;
        graphics.enableScissor(innerLeft, viewportTop, contentRight, viewportBottom);

        try {
            for (int index = 0; index < optionCount; index++) {
                int rowTop = rowStart + index * rowHeight - renderOptimiserScroll;
                int rowBottom = rowTop + rowHeight;
                if (rowBottom <= viewportTop || rowTop >= viewportBottom) {
                    continue;
                }
                boolean enabled = RenderOptimiserModule.isEnabled(index);
                int fill = enabled ? YuriTheme.accentSoft() : 0x660A0D12;
                int border = enabled ? YuriTheme.accent() : YuriTheme.columnBorder();
                int textColor = enabled ? 0xFF000000 : TEXT_COLOR;

                graphics.fill(innerLeft, rowTop, contentRight, rowBottom, fill);
                graphics.fill(innerLeft, rowTop, contentRight, rowTop + 1, border);
                graphics.fill(innerLeft, rowBottom - 1, contentRight, rowBottom, border);
                graphics.fill(innerLeft, rowTop, innerLeft + 1, rowBottom, border);
                graphics.fill(contentRight - 1, rowTop, contentRight, rowBottom, border);
                graphics.drawString(font, RenderOptimiserModule.optionLabel(index), innerLeft + 6, rowTop + 5, textColor, false);
            }
        } finally {
            graphics.disableScissor();
        }

        if (maxScroll > 0) {
            int trackLeft = innerRight - 4;
            int trackRight = innerRight - 2;
            int trackTop = viewportTop;
            int trackBottom = viewportBottom;
            int trackHeight = trackBottom - trackTop;
            int contentHeight = optionCount * rowHeight;
            int thumbHeight = Math.max(12, Math.round((trackHeight * trackHeight) / (float) contentHeight));
            int thumbRange = Math.max(1, trackHeight - thumbHeight);
            int thumbTop = trackTop + Math.round((renderOptimiserScroll / (float) maxScroll) * thumbRange);
            int thumbBottom = thumbTop + thumbHeight;

            graphics.fill(trackLeft, trackTop, trackRight, trackBottom, 0x660A0D12);
            graphics.fill(trackLeft, thumbTop, trackRight, thumbBottom, YuriTheme.accentSoft());
        }
    }

    private void renderChatPopup(GuiGraphics graphics, int left, int top) {
        graphics.drawString(font, "Chat", left + 8, top + 5, YuriTheme.accent(), false);
        int innerLeft = left + 10;
        int innerRight = left + getPopupWidth() - 10;
        int rowTop = top + 24;
        int rowHeight = CHAT_ROW_HEIGHT;
        int optionCount = ChatModule.optionCount();
        for (int index = 0; index < optionCount; index++) {
            int topY = rowTop + index * rowHeight;
            int bottomY = topY + rowHeight;
            boolean enabled = ChatModule.isEnabled(index);
            int fill = enabled ? YuriTheme.accentSoft() : 0x660A0D12;
            int border = enabled ? YuriTheme.accent() : YuriTheme.columnBorder();
            int textColor = enabled ? 0xFF000000 : TEXT_COLOR;
            graphics.fill(innerLeft, topY, innerRight, bottomY, fill);
            graphics.fill(innerLeft, topY, innerRight, topY + 1, border);
            graphics.fill(innerLeft, bottomY - 1, innerRight, bottomY, border);
            graphics.fill(innerLeft, topY, innerLeft + 1, bottomY, border);
            graphics.fill(innerRight - 1, topY, innerRight, bottomY, border);
            graphics.drawString(font, ChatModule.optionLabel(index), innerLeft + 6, topY + 5, textColor, false);
        }
    }

    private void renderMobEspPopup(GuiGraphics graphics, int left, int top) {
        graphics.drawString(font, "Mob ESP", left + 8, top + 5, YuriTheme.accent(), false);
        int innerLeft = left + 10;
        int innerRight = left + getPopupWidth() - 10;
        int rowTop = mobEspOptionsTop(top);
        int rowHeight = CHAT_ROW_HEIGHT;
        int optionCount = MobEspModule.optionCount();
        for (int index = 0; index < optionCount; index++) {
            int topY = rowTop + index * rowHeight;
            int bottomY = topY + rowHeight;
            boolean enabled = MobEspModule.isEnabled(index);
            int fill = enabled ? YuriTheme.accentSoft() : 0x660A0D12;
            int border = enabled ? YuriTheme.accent() : YuriTheme.columnBorder();
            int textColor = enabled ? 0xFF000000 : TEXT_COLOR;
            graphics.fill(innerLeft, topY, innerRight, bottomY, fill);
            graphics.fill(innerLeft, topY, innerRight, topY + 1, border);
            graphics.fill(innerLeft, bottomY - 1, innerRight, bottomY, border);
            graphics.fill(innerLeft, topY, innerLeft + 1, bottomY, border);
            graphics.fill(innerRight - 1, topY, innerRight, bottomY, border);
            graphics.drawString(font, MobEspModule.optionLabel(index), innerLeft + 6, topY + 5, textColor, false);
        }

        int previewRowTop = mobEspColorPreviewTop(top);
        int previewRowBottom = previewRowTop + 18;
        graphics.fill(innerLeft, previewRowTop, innerRight, previewRowBottom, 0x660A0D12);
        graphics.drawString(font, "Hitbox outline", left + 14, previewRowTop + 5, TEXT_COLOR, false);
        graphics.fill(innerRight - 18, previewRowTop + 5, innerRight - 8, previewRowTop + 15, 0xFF0A0C10);
        graphics.fill(innerRight - 17, previewRowTop + 6, innerRight - 9, previewRowTop + 14, MobEspModule.glowColor());

        int pickerTop = getMobEspPickerTop(top);
        renderMobEspColorPicker(graphics, left, pickerTop);

        int hexRowTop = mobEspHexRowTop(top);
        int hexRowBottom = hexRowTop + MOB_ESP_HEX_ROW_HEIGHT;
        graphics.fill(innerLeft, hexRowTop, innerRight, hexRowBottom, 0x660A0D12);
        graphics.drawString(font, "Hex", left + 14, hexRowTop + 5, TEXT_COLOR, false);
        int hexFieldLeft = innerLeft + 6;
        int hexFieldRight = innerRight - 6;
        int hexFieldTop = hexRowTop + 3;
        int hexFieldBottom = hexRowBottom - 3;
        graphics.fill(hexFieldLeft, hexFieldTop, hexFieldRight, hexFieldBottom, YuriTheme.subGuiInput());
    }

    private void renderTranslucentDoorPopup(GuiGraphics graphics, int left, int top) {
        graphics.drawString(font, "Translucent Door", left + 8, top + 5, YuriTheme.accent(), false);
        int popupWidth = getPopupWidth();
        graphics.fill(left + 10, top + 24, left + popupWidth - 10, top + getPopupHeight() - 12, 0x660A0D12);
        renderTranslucentDoorSlider(graphics, left, top, TranslucentDoorModule.doorsAlpha());
    }

    private void renderTranslucentDoorSlider(GuiGraphics graphics, int popupLeft, int popupTop, int value) {
        int rowTop = popupTop + 34;
        int sliderLeft = popupLeft + 30;
        int sliderWidth = getPopupWidth() - 60;
        int sliderRight = sliderLeft + sliderWidth;
        int barTop = rowTop + 11;
        int barBottom = barTop + SLIDER_BAR_HEIGHT;
        float ratio = normalize(value, 0.0F, 255.0F);
        int fillRight = sliderLeft + Math.round(sliderWidth * ratio);
        fillRight = clamp(fillRight, sliderLeft, sliderRight);
        int textY = rowTop + 2;
        String valueText = Integer.toString(value);
        int valueX = sliderRight - font.width(valueText);
        int labelX = sliderLeft;
        int labelY = barTop - font.lineHeight - 1;

        graphics.drawString(font, "Alpha", labelX, labelY, TEXT_COLOR, false);
        graphics.drawString(font, valueText, valueX, textY, YuriTheme.accent(), false);
        graphics.fill(sliderLeft, barTop, sliderRight, barBottom, 0xAA0A0D12);
        graphics.fill(sliderLeft, barTop, fillRight, barBottom, YuriTheme.accentSoft());
        graphics.fill(sliderLeft, barTop, sliderRight, barTop + 1, YuriTheme.columnBorder());
        graphics.fill(sliderLeft, barBottom - 1, sliderRight, barBottom, YuriTheme.columnBorder());
        graphics.fill(fillRight - 1, barTop - 2, fillRight + 1, barBottom + 2, YuriTheme.accent());
    }

    private void renderOpsecPopup(GuiGraphics graphics, int left, int top) {
        graphics.drawString(font, "Name change", left + 8, top + 5, YuriTheme.accent(), false);
        int innerLeft = left + 10;
        int innerRight = left + getPopupWidth() - 10;
        int nameRowTop = top + 24;
        int nameRowBottom = nameRowTop + 20;
        graphics.fill(innerLeft, nameRowTop, innerRight, nameRowBottom, 0x660A0D12);
        graphics.drawString(font, "Client-side only", innerLeft + 4, nameRowBottom + 6, MUTED_TEXT_COLOR, false);
    }

    private void renderImageHudPopup(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        graphics.drawString(font, "Image HUD", left + 8, top + 5, YuriTheme.accent(), false);
        int innerLeft = left + 10;
        int innerRight = left + getPopupWidth() - 10;
        graphics.drawString(font, "Image file (PNG, JPG, GIF, WEBP, BMP, TGA, TIFF)", innerLeft, top + IMAGE_HUD_LABEL_TOP, TEXT_COLOR, false);
        int browseLeft = innerLeft;
        int browseTop = top + IMAGE_HUD_BROWSE_TOP;
        int browseRight = innerRight;
        int browseBottom = browseTop + IMAGE_HUD_BROWSE_HEIGHT;
        boolean browseHovered = activePopup == PopupType.IMAGE_HUD && isInsideImageHudBrowseButton(mouseX, mouseY);
        int bFill = browseHovered ? YuriTheme.accentSoft() : 0xAA0A0D12;
        int bBorder = browseHovered ? YuriTheme.accent() : YuriTheme.columnBorder();
        graphics.fill(browseLeft, browseTop, browseRight, browseBottom, bFill);
        graphics.fill(browseLeft, browseTop, browseRight, browseTop + 1, bBorder);
        graphics.fill(browseLeft, browseBottom - 1, browseRight, browseBottom, bBorder);
        graphics.fill(browseLeft, browseTop, browseLeft + 1, browseBottom, bBorder);
        graphics.fill(browseRight - 1, browseTop, browseRight, browseBottom, bBorder);
        String openLabel = "Open image…";
        int openX = browseLeft + (browseRight - browseLeft - font.width(openLabel)) / 2;
        graphics.drawString(font, openLabel, openX, browseTop + 4, TEXT_COLOR, false);
        if (imageHudPendingImportPath != null) {
            graphics.drawString(font, "Add this image to your HUD?", innerLeft, top + IMAGE_HUD_PENDING_PROMPT_TOP, TEXT_COLOR, false);
            graphics.drawString(font, imageHudPendingDisplayName(), innerLeft, top + IMAGE_HUD_PENDING_NAME_TOP, MUTED_TEXT_COLOR, false);
            int[] addBounds = imageHudPendingAddButtonBounds();
            int[] discardBounds = imageHudPendingDiscardButtonBounds();
            boolean addHovered = activePopup == PopupType.IMAGE_HUD && isInsideImageHudConfirmAddButton(mouseX, mouseY);
            boolean discardHovered = activePopup == PopupType.IMAGE_HUD && isInsideImageHudDiscardPendingButton(mouseX, mouseY);
            drawImageHudTextButton(graphics, addBounds, addHovered, "Add to HUD");
            drawImageHudTextButton(graphics, discardBounds, discardHovered, "Discard");
        }
    }

    private void drawImageHudTextButton(GuiGraphics graphics, int[] bounds, boolean hovered, String label) {
        int left = bounds[0];
        int top = bounds[1];
        int right = bounds[2];
        int bottom = bounds[3];
        int fill = hovered ? YuriTheme.accentSoft() : 0xAA0A0D12;
        int border = hovered ? YuriTheme.accent() : YuriTheme.columnBorder();
        graphics.fill(left, top, right, bottom, fill);
        graphics.fill(left, top, right, top + 1, border);
        graphics.fill(left, bottom - 1, right, bottom, border);
        graphics.fill(left, top, left + 1, bottom, border);
        graphics.fill(right - 1, top, right, bottom, border);
        int tx = left + (right - left - font.width(label)) / 2;
        int ty = top + (bottom - top - font.lineHeight) / 2 + 1;
        graphics.drawString(font, label, tx, ty, TEXT_COLOR, false);
    }

    private int[] imageHudPendingAddButtonBounds() {
        int innerLeft = popupX + 10;
        int innerRight = popupX + getPopupWidth() - 10;
        int gap = 6;
        int top = popupY + IMAGE_HUD_PENDING_BUTTON_TOP;
        int bottom = top + IMAGE_HUD_PENDING_BUTTON_HEIGHT;
        int total = innerRight - innerLeft;
        int addW = (total - gap) / 2;
        return new int[] { innerLeft, top, innerLeft + addW, bottom };
    }

    private int[] imageHudPendingDiscardButtonBounds() {
        int innerLeft = popupX + 10;
        int innerRight = popupX + getPopupWidth() - 10;
        int gap = 6;
        int top = popupY + IMAGE_HUD_PENDING_BUTTON_TOP;
        int bottom = top + IMAGE_HUD_PENDING_BUTTON_HEIGHT;
        int total = innerRight - innerLeft;
        int addW = (total - gap) / 2;
        return new int[] { innerLeft + addW + gap, top, innerRight, bottom };
    }

    private boolean isInsideImageHudConfirmAddButton(double mouseX, double mouseY) {
        if (activePopup != PopupType.IMAGE_HUD || imageHudPendingImportPath == null) {
            return false;
        }
        int[] b = imageHudPendingAddButtonBounds();
        return mouseX >= b[0] && mouseX < b[2] && mouseY >= b[1] && mouseY < b[3];
    }

    private boolean isInsideImageHudDiscardPendingButton(double mouseX, double mouseY) {
        if (activePopup != PopupType.IMAGE_HUD || imageHudPendingImportPath == null) {
            return false;
        }
        int[] b = imageHudPendingDiscardButtonBounds();
        return mouseX >= b[0] && mouseX < b[2] && mouseY >= b[1] && mouseY < b[3];
    }

    private String imageHudPendingDisplayName() {
        if (imageHudPendingImportPath == null || imageHudPendingImportPath.isEmpty()) {
            return "";
        }
        String name = Paths.get(imageHudPendingImportPath).getFileName().toString();
        int maxLen = 40;
        if (name.length() > maxLen) {
            return name.substring(0, maxLen - 3) + "...";
        }
        return name;
    }

    private void confirmImageHudPendingImport() {
        if (imageHudPendingImportPath == null) {
            return;
        }
        if (!ImageHudModule.importImageFromUserSelection(imageHudPendingImportPath)) {
            imageHudPendingImportPath = null;
            syncImageHudPopupHeight();
            return;
        }
        imageHudPendingImportPath = null;
        syncImageHudPopupHeight();
        if (imageHudPathInput != null) {
            syncingImageHudPathInput = true;
            imageHudPathInput.setValue(ImageHudModule.getImagePath());
            syncingImageHudPathInput = false;
        }
        YuriConfig.save();
        if (minecraft != null) {
            minecraft.setScreen(new YuriHudEditScreen(this));
        }
    }

    private void discardImageHudPendingSelection() {
        imageHudPendingImportPath = null;
        syncImageHudPopupHeight();
    }

    private boolean isInsideImageHudBrowseButton(double mouseX, double mouseY) {
        if (activePopup != PopupType.IMAGE_HUD) {
            return false;
        }
        int innerLeft = popupX + 10;
        int innerRight = popupX + getPopupWidth() - 10;
        int browseLeft = innerLeft;
        int browseTop = popupY + IMAGE_HUD_BROWSE_TOP;
        int browseRight = innerRight;
        int browseBottom = browseTop + IMAGE_HUD_BROWSE_HEIGHT;
        return mouseX >= browseLeft && mouseX < browseRight && mouseY >= browseTop && mouseY < browseBottom;
    }

    private void openImageHudFilePicker() {
        // LWJGL tinyfd uses the OS file dialog on the same thread as GLFW (AWT/Swing dialogs often never show with Minecraft).
        String startDir = imageHudInitialDirectory();
        String defaultPath = "";
        if (startDir != null && !startDir.isEmpty()) {
            defaultPath = startDir;
            if (!defaultPath.endsWith(File.separator)) {
                defaultPath = defaultPath + File.separator;
            }
        }
        String selected;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(9);
            filters.put(stack.UTF8("*.png"));
            filters.put(stack.UTF8("*.jpg"));
            filters.put(stack.UTF8("*.jpeg"));
            filters.put(stack.UTF8("*.gif"));
            filters.put(stack.UTF8("*.webp"));
            filters.put(stack.UTF8("*.bmp"));
            filters.put(stack.UTF8("*.tga"));
            filters.put(stack.UTF8("*.tif"));
            filters.put(stack.UTF8("*.tiff"));
            filters.flip();
            selected = TinyFileDialogs.tinyfd_openFileDialog(
                "Choose image",
                defaultPath,
                filters,
                "Images (*.png, *.jpg, *.jpeg, *.gif, *.webp, *.bmp, *.tga, *.tif, *.tiff)",
                false
            );
        }
        if (selected == null || selected.isEmpty()) {
            return;
        }
        Path full = Paths.get(selected).normalize();
        if (!Files.isRegularFile(full)) {
            return;
        }
        imageHudPendingImportPath = full.toAbsolutePath().toString();
        syncImageHudPopupHeight();
    }

    /**
     * Start folder for the image picker: parent of current file, else Pictures, Desktop, or user home.
     */
    private static String imageHudInitialDirectory() {
        String current = ImageHudModule.getImagePath();
        if (current != null && !current.isBlank()) {
            Path p = Paths.get(current.trim());
            Path parent = p.getParent();
            if (parent != null && Files.isDirectory(parent)) {
                return parent.toAbsolutePath().toString();
            }
        }
        Path pictures = Paths.get(System.getProperty("user.home"), "Pictures");
        if (Files.isDirectory(pictures)) {
            return pictures.toAbsolutePath().toString();
        }
        Path desktop = Paths.get(System.getProperty("user.home"), "Desktop");
        if (Files.isDirectory(desktop)) {
            return desktop.toAbsolutePath().toString();
        }
        return Paths.get(System.getProperty("user.home")).toAbsolutePath().toString();
    }

    private void onImageHudPathChanged(String value) {
        if (syncingImageHudPathInput) {
            return;
        }
        ImageHudModule.setImagePathFromUi(value, true);
        YuriConfig.save();
    }

    private void onOpsecNameChanged(String value) {
        if (syncingOpsecNameInput) {
            return;
        }
        OpsecModule.nameChange = value == null ? "" : value;
        if (minecraft != null) {
            minecraft.gui.getChat().rescaleChat();
        }
    }

    private void onMobEspHexChanged(String value) {
        if (syncingMobEspHexInput || !MobEspModule.trySetGlowColor(value)) {
            return;
        }
        syncMobEspPickerFromModule();
        syncMobEspHexInput();
        YuriConfig.save();
    }

    private void renderSizeSlider(GuiGraphics graphics, int popupLeft, int popupTop, int index, int value) {
        int rowTop = popupTop + getSliderRowY(index);
        int sliderLeft = popupLeft + 30;
        int sliderWidth = getPopupWidth() - 60;
        int sliderRight = sliderLeft + sliderWidth;
        int barTop = rowTop + 11;
        int barBottom = barTop + SLIDER_BAR_HEIGHT;
        int clamped = clamp(value, 1, 20);
        float ratio = normalize(clamped, 1.0F, 20.0F);
        int fillRight = sliderLeft + Math.round(sliderWidth * ratio);
        fillRight = clamp(fillRight, sliderLeft, sliderRight);
        int textY = rowTop + 2;

        graphics.drawString(font, "1", sliderLeft, textY, TEXT_COLOR, false);
        graphics.drawString(font, "20", sliderRight - font.width("20"), textY, MUTED_TEXT_COLOR, false);
        graphics.fill(sliderLeft, barTop, sliderRight, barBottom, 0xAA0A0D12);
        graphics.fill(sliderLeft, barTop, fillRight, barBottom, YuriTheme.accentSoft());
        graphics.fill(sliderLeft, barTop, sliderRight, barTop + 1, YuriTheme.columnBorder());
        graphics.fill(sliderLeft, barBottom - 1, sliderRight, barBottom, YuriTheme.columnBorder());
        graphics.fill(fillRight - 1, barTop - 2, fillRight + 1, barBottom + 2, YuriTheme.accent());
    }

    private void renderAnimationSlider(GuiGraphics graphics, int popupLeft, int popupTop, int index, String axis, float value, float min, float max) {
        int rowTop = popupTop + getSliderRowY(index);
        int sliderLeft = popupLeft + 30;
        int sliderWidth = getPopupWidth() - 60;
        int sliderRight = sliderLeft + sliderWidth;
        int barTop = rowTop + 11;
        int barBottom = barTop + SLIDER_BAR_HEIGHT;
        float ratio = normalize(value, min, max);
        int fillRight = sliderLeft + Math.round(sliderWidth * ratio);
        fillRight = clamp(fillRight, sliderLeft, sliderRight);
        int textY = rowTop + 2;
        String valueText = formatFloat(value);
        int valueX = sliderRight - font.width(valueText);
        int axisX = sliderLeft;
        int axisY = barTop - font.lineHeight - 1;

        graphics.drawString(font, axis, axisX, axisY, TEXT_COLOR, false);
        graphics.drawString(font, valueText, valueX, textY, YuriTheme.accent(), false);
        graphics.fill(sliderLeft, barTop, sliderRight, barBottom, 0xAA0A0D12);
        graphics.fill(sliderLeft, barTop, fillRight, barBottom, YuriTheme.accentSoft());
        graphics.fill(sliderLeft, barTop, sliderRight, barTop + 1, YuriTheme.columnBorder());
        graphics.fill(sliderLeft, barBottom - 1, sliderRight, barBottom, YuriTheme.columnBorder());
        graphics.fill(fillRight - 1, barTop - 2, fillRight + 1, barBottom + 2, YuriTheme.accent());
    }

    private void renderPopupResetButton(GuiGraphics graphics, int left, int top, int right, boolean hovered) {
        int buttonWidth = 18;
        int buttonHeight = 10;
        int buttonTop = top + 4;
        int closeLeft = right - POPUP_CLOSE_SIZE - 4;
        int resetLeft = closeLeft - 6 - buttonWidth;
        int resetRight = resetLeft + buttonWidth;
        int resetBottom = buttonTop + buttonHeight;
        int resetBorder = hovered ? YuriTheme.accent() : YuriTheme.columnBorder();
        int resetFill = hovered ? YuriTheme.accentSoft() : 0x660A0D12;

        graphics.fill(resetLeft, buttonTop, resetRight, resetBottom, resetFill);
        graphics.fill(resetLeft, buttonTop, resetRight, buttonTop + 1, resetBorder);
        graphics.fill(resetLeft, resetBottom - 1, resetRight, resetBottom, resetBorder);
        graphics.fill(resetLeft, buttonTop, resetLeft + 1, resetBottom, resetBorder);
        graphics.fill(resetRight - 1, buttonTop, resetRight, resetBottom, resetBorder);

        String text = "RST";
        int textX = resetLeft + (buttonWidth - font.width(text)) / 2;
        int textY = buttonTop + (buttonHeight - font.lineHeight) / 2;
        graphics.drawString(font, text, textX, textY, hovered ? YuriTheme.accent() : TEXT_COLOR, false);
    }

    private void renderSwingSlider(GuiGraphics graphics, int popupLeft, int popupTop, int index, int value) {
        int rowTop = popupTop + getSliderRowY(index);
        int sliderLeft = popupLeft + 30;
        int sliderWidth = getPopupWidth() - 60;
        int sliderRight = sliderLeft + sliderWidth;
        int barTop = rowTop + 11;
        int barBottom = barTop + SLIDER_BAR_HEIGHT;
        int clamped = clamp(value, 0, 20);
        float ratio = normalize(clamped, 0.0F, 20.0F);
        int fillRight = sliderLeft + Math.round(sliderWidth * ratio);
        fillRight = clamp(fillRight, sliderLeft, sliderRight);
        int textY = rowTop + 2;
        String maxText = "20";
        int maxTextX = sliderRight - font.width(maxText);

        graphics.drawString(font, "0", sliderLeft, textY, TEXT_COLOR, false);
        graphics.drawString(font, maxText, maxTextX, textY, TEXT_COLOR, false);
        graphics.fill(sliderLeft, barTop, sliderRight, barBottom, 0xAA0A0D12);
        graphics.fill(sliderLeft, barTop, fillRight, barBottom, YuriTheme.accentSoft());
        graphics.fill(sliderLeft, barTop, sliderRight, barTop + 1, YuriTheme.columnBorder());
        graphics.fill(sliderLeft, barBottom - 1, sliderRight, barBottom, YuriTheme.columnBorder());
        graphics.fill(fillRight - 1, barTop - 2, fillRight + 1, barBottom + 2, YuriTheme.accent());
    }

    private void renderIgnoreHasteToggle(GuiGraphics graphics, int popupLeft, int popupTop, boolean enabled) {
        int rowTop = popupTop + 202;
        int boxLeft = popupLeft + 12;
        int boxRight = boxLeft + 10;
        int boxTop = rowTop + 2;
        int boxBottom = boxTop + 10;
        int border = enabled ? YuriTheme.accent() : YuriTheme.columnBorder();
        int fill = enabled ? YuriTheme.accentSoft() : 0x660A0D12;

        graphics.fill(boxLeft, boxTop, boxRight, boxBottom, fill);
        graphics.fill(boxLeft, boxTop, boxRight, boxTop + 1, border);
        graphics.fill(boxLeft, boxBottom - 1, boxRight, boxBottom, border);
        graphics.fill(boxLeft, boxTop, boxLeft + 1, boxBottom, border);
        graphics.fill(boxRight - 1, boxTop, boxRight, boxBottom, border);

        if (enabled) {
            graphics.drawString(font, "x", boxLeft + 3, boxTop + 1, YuriTheme.accent(), false);
        }

        graphics.drawString(font, "Ignore Haste", popupLeft + 28, rowTop + 3, TEXT_COLOR, false);
    }

    private void renderIgnoreEquipToggle(GuiGraphics graphics, int popupLeft, int popupTop, boolean enabled) {
        int rowTop = popupTop + 216;
        int boxLeft = popupLeft + 12;
        int boxRight = boxLeft + 10;
        int boxTop = rowTop + 2;
        int boxBottom = boxTop + 10;
        int border = enabled ? YuriTheme.accent() : YuriTheme.columnBorder();
        int fill = enabled ? YuriTheme.accentSoft() : 0x660A0D12;

        graphics.fill(boxLeft, boxTop, boxRight, boxBottom, fill);
        graphics.fill(boxLeft, boxTop, boxRight, boxTop + 1, border);
        graphics.fill(boxLeft, boxBottom - 1, boxRight, boxBottom, border);
        graphics.fill(boxLeft, boxTop, boxLeft + 1, boxBottom, border);
        graphics.fill(boxRight - 1, boxTop, boxRight, boxBottom, border);

        if (enabled) {
            graphics.drawString(font, "x", boxLeft + 3, boxTop + 1, YuriTheme.accent(), false);
        }

        graphics.drawString(font, "Ignore Equip", popupLeft + 28, rowTop + 3, TEXT_COLOR, false);
    }

    private int getHoveredHeaderIndex(double mouseX, double mouseY) {
        for (int index = YuriData.COLUMNS.length - 1; index >= 0; index--) {
            if (isInsideHeader(YuriData.COLUMNS[index], mouseX, mouseY)) {
                return index;
            }
        }
        return -1;
    }

    private ModuleHit getHoveredModule(double mouseX, double mouseY) {
        for (int columnIndex = YuriData.COLUMNS.length - 1; columnIndex >= 0; columnIndex--) {
            YuriData.Column column = YuriData.COLUMNS[columnIndex];
            if (column.collapsed || column.modules.length == 0) {
                continue;
            }

            int moduleY = column.y + HEADER_HEIGHT + 8;
            int left = column.x + 6;
            int right = column.x + COLUMN_WIDTH - 6;
            for (YuriData.Module module : column.modules) {
                int bottom = moduleY + MODULE_HEIGHT;
                if (mouseX >= left && mouseX < right && mouseY >= moduleY && mouseY < bottom) {
                    return new ModuleHit(column, module);
                }
                moduleY += MODULE_HEIGHT + MODULE_GAP;
            }
        }
        return null;
    }

    private int getHoveredAnimationSlider(double mouseX, double mouseY) {
        for (int index = 0; index < 5; index++) {
            int rowTop = popupY + getSliderRowY(index);
            int sliderLeft = popupX + 30;
            int sliderRight = sliderLeft + getPopupWidth() - 60;
            int barTop = rowTop + 9;
            int barBottom = barTop + 8;
            if (mouseX >= sliderLeft && mouseX < sliderRight && mouseY >= barTop - 2 && mouseY < barBottom + 2) {
                return index;
            }
        }
        return -1;
    }

    private int getHoveredPlayerSizeSlider(double mouseX, double mouseY) {
        for (int index = 0; index < 3; index++) {
            int rowTop = popupY + 30 + index * SLIDER_ROW_STEP;
            int sliderLeft = popupX + 30;
            int sliderRight = sliderLeft + getPopupWidth() - 60;
            int barTop = rowTop + 9;
            int barBottom = barTop + 8;
            if (mouseX >= sliderLeft && mouseX < sliderRight && mouseY >= barTop - 2 && mouseY < barBottom + 2) {
                return index;
            }
        }
        return -1;
    }

    private boolean isInsideTranslucentDoorSlider(double mouseX, double mouseY) {
        int rowTop = popupY + 34;
        int sliderLeft = popupX + 30;
        int sliderRight = sliderLeft + getPopupWidth() - 60;
        int barTop = rowTop + 9;
        int barBottom = barTop + 8;
        return mouseX >= sliderLeft && mouseX < sliderRight && mouseY >= barTop - 2 && mouseY < barBottom + 2;
    }

    private int getHoveredRenderOptimiserOption(double mouseX, double mouseY) {
        int left = popupX + 10;
        int right = popupX + getPopupWidth() - 10;
        int rowHeight = RENDER_OPTIMISER_ROW_HEIGHT;
        int rowStart = popupY + 24;
        int viewportBottom = popupY + getPopupHeight() - 10;
        if (mouseY >= viewportBottom) {
            return -1;
        }
        int optionCount = RenderOptimiserModule.optionCount();
        for (int index = 0; index < optionCount; index++) {
            int rowTop = rowStart + index * rowHeight - renderOptimiserScroll;
            int rowBottom = rowTop + rowHeight;
            if (mouseX >= left && mouseX < right && mouseY >= rowTop && mouseY < rowBottom) {
                return index;
            }
        }
        return -1;
    }

    private int getHoveredChatOption(double mouseX, double mouseY) {
        int left = popupX + 10;
        int right = popupX + getPopupWidth() - 10;
        int rowStart = popupY + 24;
        int optionCount = ChatModule.optionCount();
        for (int index = 0; index < optionCount; index++) {
            int rowTop = rowStart + index * CHAT_ROW_HEIGHT;
            int rowBottom = rowTop + CHAT_ROW_HEIGHT;
            if (mouseX >= left && mouseX < right && mouseY >= rowTop && mouseY < rowBottom) {
                return index;
            }
        }
        return -1;
    }

    private int getHoveredMobEspOption(double mouseX, double mouseY) {
        int left = popupX + 10;
        int right = popupX + getPopupWidth() - 10;
        int rowStart = popupY + 24;
        int optionCount = MobEspModule.optionCount();
        for (int index = 0; index < optionCount; index++) {
            int rowTop = rowStart + index * CHAT_ROW_HEIGHT;
            int rowBottom = rowTop + CHAT_ROW_HEIGHT;
            if (mouseX >= left && mouseX < right && mouseY >= rowTop && mouseY < rowBottom) {
                return index;
            }
        }
        return -1;
    }

    private int getHoveredOpsecOption(double mouseX, double mouseY) {
        int left = popupX + 10;
        int right = popupX + getPopupWidth() - 10;
        int rowTop = popupY + 62;
        int rowBottom = rowTop + CHAT_ROW_HEIGHT;
        if (mouseX >= left && mouseX < right && mouseY >= rowTop && mouseY < rowBottom) {
            return 0;
        }
        return -1;
    }

    private boolean isInsideClickGuiSVPicker(double mouseX, double mouseY) {
        int left = popupX + 12;
        int top = getClickGuiPickerTop();
        int size = 64;
        return mouseX >= left && mouseX < left + size && mouseY >= top && mouseY < top + size;
    }

    private boolean isInsideClickGuiHuePicker(double mouseX, double mouseY) {
        int left = popupX + 12 + 64 + 6;
        int top = getClickGuiPickerTop();
        int width = 8;
        int height = 64;
        return mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + height;
    }

    private boolean isInsideMobEspSVPicker(double mouseX, double mouseY) {
        int left = popupX + 12;
        int top = getMobEspPickerTop();
        int size = MOB_ESP_PICKER_SV;
        return mouseX >= left && mouseX < left + size && mouseY >= top && mouseY < top + size;
    }

    private boolean isInsideMobEspHuePicker(double mouseX, double mouseY) {
        int left = popupX + 12 + MOB_ESP_PICKER_SV + MOB_ESP_PICKER_GAP;
        int top = getMobEspPickerTop();
        int width = MOB_ESP_HUE_STRIP;
        int height = MOB_ESP_PICKER_SV;
        return mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + height;
    }

    private void updateClickGuiSVPicker(double mouseX, double mouseY) {
        int left = popupX + 12;
        int top = getClickGuiPickerTop();
        int size = 64;
        clickSat = (float) ((mouseX - left) / (double) size);
        clickVal = 1.0F - (float) ((mouseY - top) / (double) size);
        clickSat = Math.max(0.0F, Math.min(1.0F, clickSat));
        clickVal = Math.max(0.0F, Math.min(1.0F, clickVal));
        updateAccentFromPicker();
    }

    private void updateClickGuiHuePicker(double mouseY) {
        int top = getClickGuiPickerTop();
        int size = 64;
        clickHue = (float) ((mouseY - top) / (double) size);
        clickHue = Math.max(0.0F, Math.min(1.0F, clickHue));
        updateAccentFromPicker();
    }

    private void updateMobEspSVPicker(double mouseX, double mouseY) {
        int left = popupX + 12;
        int top = getMobEspPickerTop();
        int size = MOB_ESP_PICKER_SV;
        mobEspSat = (float) ((mouseX - left) / (double) size);
        mobEspVal = 1.0F - (float) ((mouseY - top) / (double) size);
        mobEspSat = Math.max(0.0F, Math.min(1.0F, mobEspSat));
        mobEspVal = Math.max(0.0F, Math.min(1.0F, mobEspVal));
        updateMobEspColorFromPicker();
    }

    private void updateMobEspHuePicker(double mouseY) {
        int top = getMobEspPickerTop();
        int size = MOB_ESP_PICKER_SV;
        mobEspHue = (float) ((mouseY - top) / (double) size);
        mobEspHue = Math.max(0.0F, Math.min(1.0F, mobEspHue));
        updateMobEspColorFromPicker();
    }

    private void updateAccentFromPicker() {
        int rgb = Color.HSBtoRGB(clickHue, clickSat, clickVal) & 0xFFFFFF;
        String hex = String.format(java.util.Locale.ROOT, "%06X", rgb);
        YuriTheme.trySetAccent(hex);
        syncHexInput();
    }

    private void syncClickPickerFromTheme() {
        int accent = YuriTheme.accent();
        int r = accent >> 16 & 0xFF;
        int g = accent >> 8 & 0xFF;
        int b = accent & 0xFF;
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        clickHue = hsb[0];
        clickSat = hsb[1];
        clickVal = hsb[2];
    }

    private void updateMobEspColorFromPicker() {
        int rgb = Color.HSBtoRGB(mobEspHue, mobEspSat, mobEspVal) & 0xFFFFFF;
        String hex = String.format(java.util.Locale.ROOT, "%06X", rgb);
        MobEspModule.trySetGlowColor(hex);
        syncMobEspHexInput();
        YuriConfig.save();
    }

    private void syncMobEspPickerFromModule() {
        int color = MobEspModule.glowColor();
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        mobEspHue = hsb[0];
        mobEspSat = hsb[1];
        mobEspVal = hsb[2];
    }

    private int getRenderOptimiserMaxScroll() {
        int contentHeight = RenderOptimiserModule.optionCount() * RENDER_OPTIMISER_ROW_HEIGHT;
        int viewportHeight = getPopupHeight() - 34;
        return Math.max(0, contentHeight - viewportHeight);
    }

    private boolean isInsideIgnoreHasteToggle(double mouseX, double mouseY) {
        int rowTop = popupY + 202;
        int left = popupX + 12;
        int right = popupX + 156;
        int top = rowTop + 2;
        int bottom = rowTop + 14;
        return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
    }

    private boolean isInsideIgnoreEquipToggle(double mouseX, double mouseY) {
        int rowTop = popupY + 216;
        int left = popupX + 12;
        int right = popupX + 156;
        int top = rowTop + 2;
        int bottom = rowTop + 14;
        return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
    }

    private int getSliderRowY(int index) {
        if (index == 0) {
            return 36;
        }
        if (index <= 3) {
            return 84 + (index - 1) * SLIDER_ROW_STEP;
        }
        return 172;
    }

    private void setAnimationSliderValue(int sliderIndex, double mouseX) {
        var settings = YuriData.ANIMATION_SETTINGS;
        int sliderLeft = popupX + 30;
        int sliderWidth = getPopupWidth() - 60;
        float ratio = (float) ((mouseX - sliderLeft) / (double) sliderWidth);
        ratio = Math.max(0.0F, Math.min(1.0F, ratio));

        switch (sliderIndex) {
            case 0 -> settings.size = clamp(Math.round(denormalize(ratio, 1.00F, 20.00F)), 1, 20);
            case 1 -> settings.posX = denormalize(ratio, -2.00F, 2.00F);
            case 2 -> settings.posY = denormalize(ratio, -2.00F, 2.00F);
            case 3 -> settings.posZ = denormalize(ratio, -2.00F, 2.00F);
            case 4 -> settings.swingSpeed = clamp(Math.round(denormalize(ratio, 0.00F, 20.00F)), 0, 20);
            default -> {
                return;
            }
        }
    }

    private void setPlayerSizeSliderValue(int sliderIndex, double mouseX) {
        var settings = YuriData.PLAYER_SIZE_SETTINGS;
        int sliderLeft = popupX + 30;
        int sliderWidth = getPopupWidth() - 60;
        float ratio = (float) ((mouseX - sliderLeft) / (double) sliderWidth);
        ratio = Math.max(0.0F, Math.min(1.0F, ratio));
        float value = denormalize(ratio, 0.20F, 3.00F);

        switch (sliderIndex) {
            case 0 -> settings.scaleX = value;
            case 1 -> settings.scaleY = value;
            case 2 -> settings.scaleZ = value;
            default -> {
                return;
            }
        }
    }

    private void setTranslucentDoorSliderValue(double mouseX) {
        int sliderLeft = popupX + 30;
        int sliderWidth = getPopupWidth() - 60;
        float ratio = (float) ((mouseX - sliderLeft) / (double) sliderWidth);
        ratio = Math.max(0.0F, Math.min(1.0F, ratio));
        TranslucentDoorModule.setDoorsAlpha(clamp(Math.round(denormalize(ratio, 0.0F, 255.0F)), 0, 255));
    }

    private boolean isInsideHeader(YuriData.Column column, double mouseX, double mouseY) {
        return mouseX >= column.x && mouseX < column.x + COLUMN_WIDTH && mouseY >= column.y && mouseY < column.y + HEADER_HEIGHT;
    }

    private boolean isInsidePopup(double mouseX, double mouseY) {
        return mouseX >= popupX && mouseX < popupX + getPopupWidth() && mouseY >= popupY && mouseY < popupY + getPopupHeight();
    }

    private boolean isInsidePopupHeader(double mouseX, double mouseY) {
        return mouseX >= popupX && mouseX < popupX + getPopupWidth() && mouseY >= popupY && mouseY < popupY + POPUP_HEADER_HEIGHT;
    }

    private boolean isInsidePopupClose(double mouseX, double mouseY) {
        int left = popupX + getPopupWidth() - POPUP_CLOSE_SIZE - 4;
        int top = popupY + 4;
        int right = left + POPUP_CLOSE_SIZE;
        int bottom = top + POPUP_CLOSE_SIZE;
        return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
    }

    private boolean isInsideAnimationResetButton(double mouseX, double mouseY) {
        if (activePopup != PopupType.ANIMATIONS) {
            return false;
        }
        int right = popupX + getPopupWidth();
        int buttonWidth = 18;
        int buttonHeight = 10;
        int buttonTop = popupY + 4;
        int closeLeft = right - POPUP_CLOSE_SIZE - 4;
        int resetLeft = closeLeft - 6 - buttonWidth;
        int resetRight = resetLeft + buttonWidth;
        int resetBottom = buttonTop + buttonHeight;
        return mouseX >= resetLeft && mouseX < resetRight && mouseY >= buttonTop && mouseY < resetBottom;
    }

    private boolean isInsidePlayerSizeResetButton(double mouseX, double mouseY) {
        if (activePopup != PopupType.PLAYER_SIZE) {
            return false;
        }
        int right = popupX + getPopupWidth();
        int buttonWidth = 18;
        int buttonHeight = 10;
        int buttonTop = popupY + 4;
        int closeLeft = right - POPUP_CLOSE_SIZE - 4;
        int resetLeft = closeLeft - 6 - buttonWidth;
        int resetRight = resetLeft + buttonWidth;
        int resetBottom = buttonTop + buttonHeight;
        return mouseX >= resetLeft && mouseX < resetRight && mouseY >= buttonTop && mouseY < resetBottom;
    }

    private boolean isInsideTranslucentDoorResetButton(double mouseX, double mouseY) {
        if (activePopup != PopupType.TRANSLUCENT_DOOR) {
            return false;
        }
        int right = popupX + getPopupWidth();
        int buttonWidth = 18;
        int buttonHeight = 10;
        int buttonTop = popupY + 4;
        int closeLeft = right - POPUP_CLOSE_SIZE - 4;
        int resetLeft = closeLeft - 6 - buttonWidth;
        int resetRight = resetLeft + buttonWidth;
        int resetBottom = buttonTop + buttonHeight;
        return mouseX >= resetLeft && mouseX < resetRight && mouseY >= buttonTop && mouseY < resetBottom;
    }

    private boolean isClickGuiModule(YuriData.Module module) {
        return "Click GUI".equals(module.title);
    }

    private boolean isAnimationsModule(YuriData.Module module) {
        return "Animations".equals(module.title);
    }

    private boolean isPlayerSizeModule(YuriData.Module module) {
        return "Player Size".equals(module.title);
    }

    private boolean isRenderOptimiserModule(YuriData.Module module) {
        return "Render Optimiser".equals(module.title);
    }

    private boolean isChatModule(YuriData.Module module) {
        return "Chat".equals(module.title);
    }

    private boolean isMobEspModule(YuriData.Module module) {
        return "Mob ESP".equals(module.title);
    }

    private boolean isTranslucentDoorModule(YuriData.Module module) {
        return "Translucent Door".equals(module.title);
    }

    private boolean isOpsecModule(YuriData.Module module) {
        return "Name change".equals(module.title) || "Opsec".equals(module.title);
    }

    private boolean isImageHudModule(YuriData.Module module) {
        return false;
    }

    private void openPopup(PopupType popupType) {
        if (popupType == PopupType.NONE) {
            return;
        }
        focusPopup(popupType, true);
        layoutPopupWidgets();
        boolean showHex = popupType == PopupType.CLICK_GUI;
        boolean showImageHud = popupType == PopupType.IMAGE_HUD;
        boolean showOpsec = popupType == PopupType.OPSEC;
        boolean showMobEsp = popupType == PopupType.MOB_ESP;
        if (hexInput != null) {
            if (showHex) {
                syncHexInput();
                hexInput.setVisible(true);
                hexInput.setFocused(true);
            } else {
                hexInput.setVisible(false);
                hexInput.setFocused(false);
            }
        }
        if (imageHudPathInput != null) {
            if (showImageHud) {
                imageHudPendingImportPath = null;
                syncImageHudPopupHeight();
                syncingImageHudPathInput = true;
                imageHudPathInput.setValue(ImageHudModule.getImagePath());
                syncingImageHudPathInput = false;
                imageHudPathInput.setVisible(true);
                imageHudPathInput.setFocused(true);
            } else {
                imageHudPathInput.setVisible(false);
                imageHudPathInput.setFocused(false);
            }
        }
        if (opsecNameInput != null) {
            if (showOpsec) {
                syncingOpsecNameInput = true;
                opsecNameInput.setValue(OpsecModule.nameChange);
                syncingOpsecNameInput = false;
                opsecNameInput.setVisible(true);
                opsecNameInput.setFocused(true);
            } else {
                opsecNameInput.setVisible(false);
                opsecNameInput.setFocused(false);
            }
        }
        if (mobEspHexInput != null) {
            if (showMobEsp) {
                syncMobEspHexInput();
                syncMobEspPickerFromModule();
                mobEspHexInput.setVisible(true);
                mobEspHexInput.setFocused(true);
            } else {
                mobEspHexInput.setVisible(false);
                mobEspHexInput.setFocused(false);
            }
        }
    }

    private void closePopup() {
        if (activePopup == PopupType.NONE) {
            return;
        }
        PopupType closing = activePopup;
        storeActivePopupPosition();
        popupStack.remove(activePopup);
        if (popupStack.isEmpty()) {
            activePopup = PopupType.NONE;
        } else {
            activePopup = popupStack.get(popupStack.size() - 1);
            applyPopupPosition(activePopup);
        }
        waitingForOpenKeyBind = false;
        draggingPopup = false;
        draggingSliderIndex = -1;
        if (hexInput != null) {
            boolean show = activePopup == PopupType.CLICK_GUI;
            hexInput.setVisible(show);
            hexInput.setFocused(show);
        }
        if (imageHudPathInput != null) {
            boolean showImg = activePopup == PopupType.IMAGE_HUD;
            imageHudPathInput.setVisible(showImg);
            imageHudPathInput.setFocused(showImg);
        }
        if (opsecNameInput != null) {
            boolean showOpsec = activePopup == PopupType.OPSEC;
            opsecNameInput.setVisible(showOpsec);
            opsecNameInput.setFocused(showOpsec);
        }
        if (mobEspHexInput != null) {
            boolean showMobEsp = activePopup == PopupType.MOB_ESP;
            mobEspHexInput.setVisible(showMobEsp);
            mobEspHexInput.setFocused(showMobEsp);
        }
        if (closing == PopupType.IMAGE_HUD) {
            imageHudPendingImportPath = null;
        }
    }

    private void layoutPopupWidgets() {
        if (hexInput != null) {
            hexInput.setX(popupX + CLICK_HEX_FIELD_INSET);
            hexInput.setY(getClickGuiHexFieldTopY());
            hexInput.setWidth(getPopupWidth() - CLICK_HEX_FIELD_INSET * 2);
            hexInput.setVisible(activePopup == PopupType.CLICK_GUI);
        }
        if (imageHudPathInput != null) {
            imageHudPathInput.setX(popupX + 10);
            imageHudPathInput.setY(popupY + IMAGE_HUD_PATH_TOP);
            imageHudPathInput.setWidth(getPopupWidth() - 20);
            imageHudPathInput.setVisible(activePopup == PopupType.IMAGE_HUD);
        }
        if (opsecNameInput != null) {
            opsecNameInput.setX(popupX + 14);
            opsecNameInput.setY(popupY + 26);
            opsecNameInput.setWidth(getPopupWidth() - 28);
            opsecNameInput.setVisible(activePopup == PopupType.OPSEC);
        }
        if (mobEspHexInput != null) {
            mobEspHexInput.setX(popupX + MOB_ESP_HEX_FIELD_INSET);
            mobEspHexInput.setY(mobEspHexRowTop(popupY) + 3);
            mobEspHexInput.setWidth(getPopupWidth() - MOB_ESP_HEX_FIELD_INSET * 2);
            mobEspHexInput.setVisible(activePopup == PopupType.MOB_ESP);
        }
    }

    private boolean isInsideRebindButton(double mouseX, double mouseY) {
        int right = popupX + getPopupWidth() - 18;
        int top = getClickGuiRebindButtonTop();
        int left = right - 62;
        int bottom = top + 14;
        return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
    }

    private int getClickGuiPickerTop() {
        return popupY + CLICK_PICKER_TOP_OFFSET;
    }

    private int getMobEspPickerTop() {
        return getMobEspPickerTop(popupY);
    }

    private int getMobEspPickerTop(int anchorTop) {
        return mobEspColorPreviewTop(anchorTop) + 24;
    }

    private int getClickGuiHexFieldTopY() {
        return clickGuiHexRowTop(popupY) + 3;
    }

    private int clickGuiHexRowTop(int anchorTop) {
        return anchorTop + CLICK_PICKER_TOP_OFFSET + CLICK_PICKER_SV + CLICK_GAP_BELOW_PICKER;
    }

    private int mobEspHexRowTop(int anchorTop) {
        return getMobEspPickerTop(anchorTop) + MOB_ESP_PICKER_SV + MOB_ESP_GAP_BELOW_PICKER;
    }

    private int mobEspOptionsTop(int anchorTop) {
        return anchorTop + 24;
    }

    private int mobEspOptionsBottom(int anchorTop) {
        return mobEspOptionsTop(anchorTop) + MobEspModule.optionCount() * CHAT_ROW_HEIGHT;
    }

    private int mobEspColorPreviewTop(int anchorTop) {
        return mobEspOptionsBottom(anchorTop) + 8;
    }

    private int clickGuiKeyRowTopInt(int anchorTop) {
        return clickGuiHexRowTop(anchorTop) + CLICK_HEX_ROW_HEIGHT + CLICK_GAP_HEX_TO_KEY;
    }

    private int clickGuiRebindButtonTopInt(int anchorTop) {
        return clickGuiKeyRowTopInt(anchorTop) + 18 + 4;
    }

    private int getClickGuiRebindButtonTop() {
        return clickGuiRebindButtonTopInt(popupY);
    }

    private void onHexChanged(String value) {
        if (syncingHexInput || !YuriTheme.trySetAccent(value)) {
            return;
        }
        syncClickPickerFromTheme();
        syncHexInput();
    }

    private void syncHexInput() {
        if (hexInput == null) {
            return;
        }
        syncingHexInput = true;
        hexInput.setValue(YuriTheme.accentHex());
        syncingHexInput = false;
    }

    private void syncMobEspHexInput() {
        if (mobEspHexInput == null) {
            return;
        }
        syncingMobEspHexInput = true;
        mobEspHexInput.setValue(MobEspModule.glowColorHex());
        syncingMobEspHexInput = false;
    }

    private void resetGuiLayout() {
        for (YuriData.Column column : YuriData.COLUMNS) {
            column.resetPosition();
        }
        centerColumns(true);
        centerPopup();
        layoutPopupWidgets();
        draggingIndex = -1;
        draggingPopup = false;
        draggingSliderIndex = -1;
    }

    private int getPopupWidth() {
        return getPopupWidth(activePopup);
    }

    private int getPopupHeight() {
        return getPopupHeight(activePopup);
    }

    private int getPopupWidth(PopupType type) {
        return type == PopupType.ANIMATIONS ? animPopupWidth
            : type == PopupType.PLAYER_SIZE ? playerPopupWidth
            : type == PopupType.RENDER_OPTIMISER ? renderOptimiserPopupWidth
            : type == PopupType.CHAT ? chatPopupWidth
            : type == PopupType.MOB_ESP ? mobEspPopupWidth
            : type == PopupType.TRANSLUCENT_DOOR ? translucentDoorPopupWidth
            : type == PopupType.OPSEC ? opsecPopupWidth
            : type == PopupType.IMAGE_HUD ? imageHudPopupWidth
            : clickPopupWidth;
    }

    private int getPopupHeight(PopupType type) {
        return type == PopupType.ANIMATIONS ? animPopupHeight
            : type == PopupType.PLAYER_SIZE ? playerPopupHeight
            : type == PopupType.RENDER_OPTIMISER ? renderOptimiserPopupHeight
            : type == PopupType.CHAT ? chatPopupHeight
            : type == PopupType.MOB_ESP ? mobEspPopupHeight
            : type == PopupType.TRANSLUCENT_DOOR ? translucentDoorPopupHeight
            : type == PopupType.OPSEC ? opsecPopupHeight
            : type == PopupType.IMAGE_HUD ? imageHudPopupHeight
            : clickPopupHeight;
    }

    private void centerPopup() {
        popupX = (width - getPopupWidth()) / 2;
        popupY = (height - getPopupHeight()) / 2;
    }

    private void setPopupSize(PopupType type, int widthValue, int heightValue) {
        int w = clamp(widthValue, 170, Math.max(170, width - SCREEN_PADDING * 2));
        int h = clamp(heightValue, 120, Math.max(120, height - SCREEN_PADDING * 2));
        if (type == PopupType.ANIMATIONS) {
            animPopupWidth = w;
            animPopupHeight = h;
        } else if (type == PopupType.PLAYER_SIZE) {
            playerPopupWidth = w;
            playerPopupHeight = h;
        } else if (type == PopupType.RENDER_OPTIMISER) {
            renderOptimiserPopupWidth = w;
            renderOptimiserPopupHeight = h;
        } else if (type == PopupType.CHAT) {
            chatPopupWidth = w;
            chatPopupHeight = h;
        } else if (type == PopupType.MOB_ESP) {
            mobEspPopupWidth = w;
            mobEspPopupHeight = h;
        } else if (type == PopupType.TRANSLUCENT_DOOR) {
            translucentDoorPopupWidth = w;
            translucentDoorPopupHeight = h;
        } else if (type == PopupType.OPSEC) {
            opsecPopupWidth = w;
            opsecPopupHeight = h;
        } else if (type == PopupType.CLICK_GUI) {
            clickPopupWidth = w;
            clickPopupHeight = h;
        } else if (type == PopupType.IMAGE_HUD) {
            imageHudPopupWidth = w;
            imageHudPopupHeight = h;
        }
    }

    private void storePopupPosition(PopupType type, int x, int y) {
        int index = type.ordinal();
        popupStoredX[index] = x;
        popupStoredY[index] = y;
        popupHasStoredPosition[index] = true;
    }

    private void storeActivePopupPosition() {
        if (activePopup == PopupType.NONE) {
            return;
        }
        storePopupPosition(activePopup, popupX, popupY);
    }

    private void applyPopupPosition(PopupType type) {
        if (type == PopupType.NONE) {
            return;
        }
        activePopup = type;
        int index = type.ordinal();
        if (popupHasStoredPosition[index]) {
            popupX = popupStoredX[index];
            popupY = popupStoredY[index];
        } else {
            centerPopup();
            storePopupPosition(type, popupX, popupY);
        }
    }

    private void focusPopup(PopupType popupType, boolean bringToFront) {
        if (popupType == PopupType.NONE) {
            return;
        }
        storeActivePopupPosition();
        if (bringToFront) {
            popupStack.remove(popupType);
            popupStack.add(popupType);
        } else if (!popupStack.contains(popupType)) {
            popupStack.add(popupType);
        }
        activePopup = popupType;
        int index = popupType.ordinal();
        if (popupHasStoredPosition[index]) {
            popupX = popupStoredX[index];
            popupY = popupStoredY[index];
        } else {
            popupX = (width - getPopupWidth(popupType)) / 2;
            popupY = (height - getPopupHeight(popupType)) / 2;
            storePopupPosition(popupType, popupX, popupY);
        }
        popupX = clamp(popupX, SCREEN_PADDING, Math.max(SCREEN_PADDING, width - getPopupWidth() - SCREEN_PADDING));
        popupY = clamp(popupY, SCREEN_PADDING, Math.max(SCREEN_PADDING, height - getPopupHeight() - SCREEN_PADDING));
        waitingForOpenKeyBind = false;
        draggingPopup = false;
        draggingSliderIndex = -1;
        if (popupType == PopupType.RENDER_OPTIMISER) {
            syncRenderOptimiserPopupHeight();
        }
    }

    private PopupType findPopupAt(double mouseX, double mouseY) {
        for (int index = popupStack.size() - 1; index >= 0; index--) {
            PopupType popupType = popupStack.get(index);
            if (popupType == PopupType.NONE) {
                continue;
            }
            int left = popupType == activePopup ? popupX : popupStoredX[popupType.ordinal()];
            int top = popupType == activePopup ? popupY : popupStoredY[popupType.ordinal()];
            if (!popupHasStoredPosition[popupType.ordinal()] && popupType != activePopup) {
                left = (width - getPopupWidth(popupType)) / 2;
                top = (height - getPopupHeight(popupType)) / 2;
            }
            int right = left + getPopupWidth(popupType);
            int bottom = top + getPopupHeight(popupType);
            if (mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom) {
                return popupType;
            }
        }
        return PopupType.NONE;
    }

    private void centerColumns(boolean snapToTarget) {
        int count = YuriData.COLUMNS.length;
        int availableWidth = Math.max(0, width - SCREEN_PADDING * 2);
        int minGap = 6;
        int maxGap = 18;
        int preferredGap = 12;
        int gap = preferredGap;
        if (count > 1) {
            int naturalGap = (availableWidth - count * COLUMN_WIDTH) / (count - 1);
            gap = clamp(naturalGap, minGap, maxGap);
        }
        int totalWidth = count * COLUMN_WIDTH + (count - 1) * gap;
        int startX = (width - totalWidth) / 2;
        int y = 42;
        for (int i = 0; i < count; i++) {
            YuriData.Column column = YuriData.COLUMNS[i];
            int targetX = startX + i * (COLUMN_WIDTH + gap);
            if (snapToTarget || i == draggingIndex) {
                column.x = targetX;
            } else {
                column.x = smoothToTarget(column.x, targetX);
            }
            column.y = y;
        }
    }

    private int smoothToTarget(int current, int target) {
        int delta = target - current;
        if (Math.abs(delta) <= 1) {
            return target;
        }
        return current + Math.round(delta * 0.22F);
    }

    private void applyConfiguredGuiScale() {
        if (minecraft == null || minecraft.options == null) {
            return;
        }
        Options options = minecraft.options;
        int targetScale = 2;
        if (previousGuiScale == null) {
            previousGuiScale = options.guiScale().get();
        }
        if (options.guiScale().get() != targetScale) {
            options.guiScale().set(targetScale);
            minecraft.resizeDisplay();
        }
    }

    private void restorePreviousGuiScale() {
        if (minecraft == null || minecraft.options == null || previousGuiScale == null) {
            return;
        }
        Options options = minecraft.options;
        if (options.guiScale().get() != previousGuiScale) {
            options.guiScale().set(previousGuiScale);
            minecraft.resizeDisplay();
        }
        previousGuiScale = null;
    }

    private String formatFloat(float value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private float normalize(float value, float min, float max) {
        if (max <= min) {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, (value - min) / (max - min)));
    }

    private float denormalize(float ratio, float min, float max) {
        return min + (max - min) * ratio;
    }

    private record ModuleHit(YuriData.Column column, YuriData.Module module) {
    }

    private enum PopupType {
        NONE,
        CLICK_GUI,
        ANIMATIONS,
        PLAYER_SIZE,
        RENDER_OPTIMISER,
        CHAT,
        MOB_ESP,
        TRANSLUCENT_DOOR,
        OPSEC,
        IMAGE_HUD
    }

    private void syncRenderOptimiserPopupHeight() {
        int rows = RenderOptimiserModule.optionCount() * RENDER_OPTIMISER_ROW_HEIGHT;
        int target = 24 + rows + 10;
        renderOptimiserPopupHeight = clamp(target, 80, Math.max(80, height - SCREEN_PADDING * 2));
    }

    private void syncImageHudPopupHeight() {
        int target = imageHudPendingImportPath == null
            ? IMAGE_HUD_POPUP_HEIGHT_COLLAPSED
            : IMAGE_HUD_POPUP_HEIGHT_EXPANDED;
        imageHudPopupHeight = clamp(target, 80, Math.max(80, height - SCREEN_PADDING * 2));
        if (activePopup == PopupType.IMAGE_HUD) {
            popupY = clamp(popupY, SCREEN_PADDING, Math.max(SCREEN_PADDING, height - getPopupHeight() - SCREEN_PADDING));
            layoutPopupWidgets();
        }
    }

}
