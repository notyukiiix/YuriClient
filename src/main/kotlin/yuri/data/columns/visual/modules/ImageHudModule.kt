package yuri.data.columns.visual.modules

import com.mojang.blaze3d.platform.NativeImage
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.ResourceLocation
import yuri.YuriData
import yuri.YuriHudEditLayout
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.UUID
import java.util.function.Supplier
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object ImageHudModule {
    private const val TITLE = "Image HUD"
    private const val STORED_IMAGE_BASE = "image_hud_custom"
    private const val MAX_IMPORT_SCREEN_FRACTION = 0.45f
    private const val REMOVE_SIZE = 12

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    private fun imageHudConfigDir(): Path =
        FabricLoader.getInstance().configDir.resolve("yuri")

    @JvmField
    var imagePath: String = ""

    @JvmField
    var hudX: Int = 12

    @JvmField
    var hudY: Int = 12

    /** Visual scale applied to native image dimensions. */
    @JvmField
    var displayScale: Float = 1f

    private data class HudImage(
        var imagePath: String,
        var hudX: Int,
        var hudY: Int,
        var displayScale: Float,
        var textureLocation: ResourceLocation?,
        var dynamicTexture: DynamicTexture?,
        var nativeWidth: Int,
        var nativeHeight: Int
    )

    private val images: MutableList<HudImage> = mutableListOf()
    private var lastHoverIndex: Int = -1
    private var activeIndex: Int = -1
    private var pendingGpuReload: Boolean = false

    private var editorDragging: Boolean = false
    private var dragGrabOffsetX: Int = 0
    private var dragGrabOffsetY: Int = 0

    @JvmStatic
    fun getImagePath(): String = imagePath

    @JvmStatic
    fun setImagePathFromUi(path: String, reload: Boolean) {
        imagePath = path.trim()
        if (reload) {
            clearAllImages()
            if (imagePath.isNotEmpty()) {
                addImageFromPath(imagePath, 12, 12, displayScale, false)
            }
        }
    }

    /**
     * Copies the user's chosen file into `config/yuri/` and points the HUD at that copy.
     * Enables the Image HUD module when the image loads so it shows in-game and in the HUD editor.
     *
     * @param selectedAbsolutePath absolute path to an existing image file from the file picker
     * @return true if a texture was loaded successfully
     */
    @JvmStatic
    fun importImageFromUserSelection(selectedAbsolutePath: String): Boolean {
        val source = Path.of(selectedAbsolutePath.trim()).normalize()
        if (!Files.isRegularFile(source)) {
            return false
        }
        val dir = imageHudConfigDir()
        try {
            Files.createDirectories(dir)
        } catch (_: IOException) {
            return false
        }
        val originalName = source.fileName.toString()
        val dot = originalName.lastIndexOf('.')
        val ext = (if (dot >= 0) originalName.substring(dot) else ".png").lowercase()
        val safeExt = when (ext) {
            ".png", ".jpg", ".jpeg", ".gif", ".webp", ".bmp", ".tga", ".tif", ".tiff" -> ext
            else -> ".png"
        }
        val target = dir.resolve("${STORED_IMAGE_BASE}_${UUID.randomUUID()}$safeExt")
        val sourceAbs = source.toAbsolutePath()
        val targetAbs = target.toAbsolutePath()
        try {
            if (sourceAbs != targetAbs) {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (_: IOException) {
            return false
        }
        imagePath = targetAbs.toString()
        val count = images.size
        val startX = 12 + (count % 4) * 18
        val startY = 12 + (count % 4) * 18
        if (!addImageFromPath(imagePath, startX, startY, 1f, true)) {
            return false
        }
        activeIndex = images.lastIndex
        module.enabled = true
        return true
    }

    private fun applyDefaultImportScaleIfNeeded(image: HudImage) {
        val mc = Minecraft.getInstance() ?: return
        if (image.nativeWidth <= 0 || image.nativeHeight <= 0) {
            return
        }
        val screenW = max(1, mc.window.guiScaledWidth)
        val screenH = max(1, mc.window.guiScaledHeight)
        val maxW = screenW * MAX_IMPORT_SCREEN_FRACTION
        val maxH = screenH * MAX_IMPORT_SCREEN_FRACTION
        val targetScale = min(1f, min(maxW / image.nativeWidth.toFloat(), maxH / image.nativeHeight.toFloat()))
        image.displayScale = targetScale.coerceIn(0.12f, 8f)
        image.hudX = min(max(0, image.hudX), max(0, screenW - drawWidth(image)))
        image.hudY = min(max(0, image.hudY), max(0, screenH - drawHeight(image)))
    }

    @JvmStatic
    fun reloadTexture() {
        val copy = images.toList()
        clearAllImages()
        for (img in copy) {
            addImageFromPath(img.imagePath, img.hudX, img.hudY, img.displayScale, false)
        }
        pendingGpuReload = images.any { it.textureLocation == null }
    }

    private fun releaseTexture(image: HudImage) {
        val loc = image.textureLocation
        if (loc != null) {
            Minecraft.getInstance()?.textureManager?.release(loc)
        }
        image.dynamicTexture?.close()
        image.dynamicTexture = null
        image.textureLocation = null
    }

    @JvmStatic
    fun hasRenderableImage(): Boolean =
        images.any { it.textureLocation != null && it.nativeWidth > 0 && it.nativeHeight > 0 }

    @JvmStatic
    fun drawWidth(): Int =
        drawWidth(images.getOrNull(activeIndex))

    @JvmStatic
    fun drawHeight(): Int =
        drawHeight(images.getOrNull(activeIndex))

    private fun drawWidth(image: HudImage?): Int =
        max(1, ((image?.nativeWidth ?: 1) * (image?.displayScale ?: 1f)).roundToInt())

    private fun drawHeight(image: HudImage?): Int =
        max(1, ((image?.nativeHeight ?: 1) * (image?.displayScale ?: 1f)).roundToInt())

    @JvmStatic
    fun containsHudPoint(mx: Double, my: Double): Boolean {
        lastHoverIndex = findImageIndexAt(mx, my)
        if (lastHoverIndex >= 0) {
            activeIndex = lastHoverIndex
        }
        return lastHoverIndex >= 0
    }

    @JvmStatic
    fun renderImage(graphics: GuiGraphics) {
        for (img in images) {
            val loc = img.textureLocation ?: continue
            val dw = drawWidth(img)
            val dh = drawHeight(img)
            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                loc,
                img.hudX,
                img.hudY,
                0f,
                0f,
                dw,
                dh,
                img.nativeWidth,
                img.nativeHeight,
                img.nativeWidth,
                img.nativeHeight,
                -1
            )
        }
    }

    @JvmStatic
    fun shouldDrawInGameHud(): Boolean =
        module.enabled && run {
            if (pendingGpuReload) {
                reloadTexture()
            }
            hasRenderableImage()
        }

    @JvmStatic
    fun clearImageFromHud() {
        imagePath = ""
        clearAllImages()
        editorDragging = false
        module.enabled = false
    }

    @JvmStatic
    fun shouldShowHudEditorPlaceholder(): Boolean =
        !hasRenderableImage()

    @JvmStatic
    fun editorBeginDrag(mx: Double, my: Double) {
        val idx = findImageIndexAt(mx, my)
        if (idx < 0) {
            return
        }
        activeIndex = idx
        val img = images[idx]
        editorDragging = true
        dragGrabOffsetX = (mx - img.hudX).toInt()
        dragGrabOffsetY = (my - img.hudY).toInt()
    }

    @JvmStatic
    fun editorDragging(): Boolean = editorDragging

    @JvmStatic
    fun editorEndDrag() {
        editorDragging = false
        val img = images.getOrNull(activeIndex) ?: return
        if (YuriHudEditLayout.isGridSnapEnabled()) {
            img.hudX = YuriHudEditLayout.snapToGrid(img.hudX)
            img.hudY = YuriHudEditLayout.snapToGrid(img.hudY)
        }
    }

    @JvmStatic
    fun editorDragTo(mx: Double, my: Double, screenWidth: Int, screenHeight: Int) {
        if (!editorDragging) {
            return
        }
        val img = images.getOrNull(activeIndex) ?: return
        var nx = (mx - dragGrabOffsetX).toInt()
        var ny = (my - dragGrabOffsetY).toInt()
        val w = drawWidth(img)
        val h = drawHeight(img)
        nx = min(max(0, nx), max(0, screenWidth - w))
        ny = min(max(0, ny), max(0, screenHeight - h))
        if (YuriHudEditLayout.isGridSnapEnabled()) {
            nx = YuriHudEditLayout.snapToGrid(nx)
            ny = YuriHudEditLayout.snapToGrid(ny)
        }
        img.hudX = nx
        img.hudY = ny
    }

    @JvmStatic
    fun editorApplyScroll(delta: Double) {
        if (delta == 0.0) {
            return
        }
        val img = images.getOrNull(activeIndex) ?: return
        val factor = 1f + (delta * 0.08f).toFloat().coerceIn(-0.35f, 0.35f)
        img.displayScale = (img.displayScale * factor).coerceIn(0.12f, 8f)
    }

    @JvmStatic
    fun editorRemoveButtonBounds(mouseX: Double, mouseY: Double): IntArray? {
        val idx = findImageIndexAt(mouseX, mouseY).takeIf { it >= 0 } ?: activeIndex.takeIf { it >= 0 } ?: return null
        val img = images.getOrNull(idx) ?: return null
        val left = img.hudX + drawWidth(img) - REMOVE_SIZE
        val top = img.hudY
        return intArrayOf(left, top, left + REMOVE_SIZE, top + REMOVE_SIZE)
    }

    @JvmStatic
    fun editorRemoveImageAt(mouseX: Double, mouseY: Double): Boolean {
        val idx = findImageIndexAt(mouseX, mouseY)
        if (idx < 0) {
            return false
        }
        val img = images.getOrNull(idx) ?: return false
        val left = img.hudX + drawWidth(img) - REMOVE_SIZE
        val top = img.hudY
        val right = left + REMOVE_SIZE
        val bottom = top + REMOVE_SIZE
        if (!(mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom)) {
            return false
        }
        val removed = images.removeAt(idx)
        releaseTexture(removed)
        if (activeIndex >= images.size) {
            activeIndex = images.lastIndex
        }
        if (images.isEmpty()) {
            module.enabled = false
            imagePath = ""
        }
        return true
    }

    @JvmStatic
    fun imageCount(): Int = images.size

    @JvmStatic
    fun imagePathAt(index: Int): String = images[index].imagePath

    @JvmStatic
    fun imageXAt(index: Int): Int = images[index].hudX

    @JvmStatic
    fun imageYAt(index: Int): Int = images[index].hudY

    @JvmStatic
    fun imageScaleAt(index: Int): Float = images[index].displayScale

    @JvmStatic
    fun addImageFromConfig(path: String, x: Int, y: Int, scale: Float): Boolean =
        addImagePlaceholder(path, x, y, scale)

    @JvmStatic
    fun clearAllImages() {
        for (img in images) {
            releaseTexture(img)
        }
        images.clear()
        activeIndex = -1
        lastHoverIndex = -1
        pendingGpuReload = false
    }

    private fun addImagePlaceholder(pathString: String, x: Int, y: Int, scale: Float): Boolean {
        val path = Path.of(pathString.trim())
        if (!Files.isRegularFile(path)) {
            return false
        }
        val hudImage = HudImage(
            path.toAbsolutePath().toString(),
            max(0, x),
            max(0, y),
            scale.coerceIn(0.12f, 8f),
            null,
            null,
            1,
            1
        )
        images.add(hudImage)
        imagePath = hudImage.imagePath
        activeIndex = images.lastIndex
        pendingGpuReload = true
        return true
    }

    private fun findImageIndexAt(mx: Double, my: Double): Int {
        for (i in images.lastIndex downTo 0) {
            val img = images[i]
            val w = drawWidth(img)
            val h = drawHeight(img)
            if (mx >= img.hudX && mx < img.hudX + w && my >= img.hudY && my < img.hudY + h) {
                return i
            }
        }
        return -1
    }

    private fun addImageFromPath(pathString: String, x: Int, y: Int, scale: Float, applyDefaultScale: Boolean): Boolean {
        val path = Path.of(pathString.trim())
        if (!Files.isRegularFile(path)) {
            return false
        }
        val mc = Minecraft.getInstance()
        if (mc == null) {
            val hudImage = HudImage(
                path.toAbsolutePath().toString(),
                max(0, x),
                max(0, y),
                scale.coerceIn(0.12f, 8f),
                null,
                null,
                0,
                0
            )
            images.add(hudImage)
            imagePath = hudImage.imagePath
            activeIndex = images.lastIndex
            pendingGpuReload = true
            return true
        }
        return try {
            Files.newInputStream(path).use { stream ->
                val image = NativeImage.read(stream)
                val imageWidth = image.width
                val imageHeight = image.height
                try {
                    val id = ResourceLocation.fromNamespaceAndPath("yuri", "image_hud/${UUID.randomUUID()}")
                    val dyn = DynamicTexture(Supplier { "yuri_image_hud" }, image)
                    mc.textureManager.register(id, dyn)
                    val hudImage = HudImage(
                        path.toAbsolutePath().toString(),
                        max(0, x),
                        max(0, y),
                        scale.coerceIn(0.12f, 8f),
                        id,
                        dyn,
                        imageWidth,
                        imageHeight
                    )
                    if (applyDefaultScale) {
                        applyDefaultImportScaleIfNeeded(hudImage)
                    }
                    images.add(hudImage)
                    imagePath = hudImage.imagePath
                    activeIndex = images.lastIndex
                    pendingGpuReload = false
                    true
                } catch (_: IllegalStateException) {
                    // Can happen during client bootstrap before RenderSystem/texture manager device init.
                    image.close()
                    val hudImage = HudImage(
                        path.toAbsolutePath().toString(),
                        max(0, x),
                        max(0, y),
                        scale.coerceIn(0.12f, 8f),
                        null,
                        null,
                        imageWidth,
                        imageHeight
                    )
                    images.add(hudImage)
                    imagePath = hudImage.imagePath
                    activeIndex = images.lastIndex
                    pendingGpuReload = true
                    true
                }
            }
        } catch (_: IOException) {
            false
        } catch (_: RuntimeException) {
            false
        }
    }
}
