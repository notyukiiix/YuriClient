package yuri

import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.loader.api.FabricLoader
import yuri.data.columns.general.modules.ChatModule
import yuri.data.columns.dev.modules.CustomScoreboardModule
import yuri.data.columns.dev.modules.OpsecModule
import yuri.client.YuriDungeonMapHud
import yuri.client.YuriDungeonSummaryHud
import yuri.data.columns.cheats.modules.DoorEspModule
import yuri.data.columns.dungeons.map.compat.DungeonMapConfig
import yuri.data.columns.cheats.modules.MobEspModule
import yuri.data.columns.cheats.modules.TranslucentDoorModule
import yuri.data.columns.visual.modules.RenderOptimiserModule
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object YuriConfig {
    private const val FILE_NAME = "yuri-client.properties"
    /**
     * Bumped when persisted module/render-optimiser indices change so old keys are not mis-applied.
     * Format 1: legacy three General modules (indices 0–2) and eleven Render Optimiser rows (0–10).
     */
    private const val CONFIG_FORMAT_KEY = "config.format"
    private const val CURRENT_CONFIG_FORMAT = 5

    @JvmStatic
    fun load() {
        val path = getConfigPath()
        if (!Files.exists(path)) {
            return
        }

        val properties = Properties()
        try {
            Files.newInputStream(path).use { inputStream -> properties.load(inputStream) }
        } catch (_: IOException) {
            return
        }

        YuriTheme.trySetAccent(properties.getProperty("accent", "#FFFFFF"))
        val keyType = properties.getProperty("keybind.open_gui.type", "KEYSYM")
        val keyCode = getInt(
            properties,
            "keybind.open_gui.code",
            getInt(properties, "keybind.open_gui", YuriClientMod.getOpenGuiKeyCode())
        )
        YuriClientMod.setOpenGuiKey(parseKey(keyType, keyCode))

        val settings = YuriData.ANIMATION_SETTINGS
        settings.size = clamp(getInt(properties, "anim.size", settings.size), 1, 20)
        if (properties.getProperty("anim.size") == null) {
            val sx = getFloat(properties, "anim.size_x", -1.0F)
            val sy = getFloat(properties, "anim.size_y", -1.0F)
            val sz = getFloat(properties, "anim.size_z", -1.0F)
            if (sx > 0.0F && sy > 0.0F && sz > 0.0F) {
                val avg = (sx + sy + sz) / 3.0F
                val ratio = max(0.0F, min(1.0F, (avg - 0.20F) / (3.00F - 0.20F)))
                settings.size = clamp(1 + (ratio * 19.0F).roundToInt(), 1, 20)
            }
        }
        settings.posX = getFloat(properties, "anim.pos_x", settings.posX)
        settings.posY = getFloat(properties, "anim.pos_y", settings.posY)
        settings.posZ = getFloat(properties, "anim.pos_z", settings.posZ)
        settings.swingSpeed = clamp(getInt(properties, "anim.swing_speed", settings.swingSpeed), 0, 20)
        settings.ignoreHaste =
            properties.getProperty("anim.ignore_haste", settings.ignoreHaste.toString()).toBoolean()
        settings.ignoreEquip =
            properties.getProperty("anim.ignore_equip", settings.ignoreEquip.toString()).toBoolean()

        val playerSize = YuriData.PLAYER_SIZE_SETTINGS
        playerSize.scaleX = clampFloat(getFloat(properties, "cos.player_size_x", playerSize.scaleX), 0.20F, 3.00F)
        playerSize.scaleY = clampFloat(getFloat(properties, "cos.player_size_y", playerSize.scaleY), 0.20F, 3.00F)
        playerSize.scaleZ = clampFloat(getFloat(properties, "cos.player_size_z", playerSize.scaleZ), 0.20F, 3.00F)

        val storedConfigFormat = getInt(properties, CONFIG_FORMAT_KEY, 1)
        val needsLegacyLayoutMigration = storedConfigFormat < CURRENT_CONFIG_FORMAT
        val generalColumn = YuriData.COLUMNS.getOrNull(0)
        val legacyGeneralThreeSlots = needsLegacyLayoutMigration &&
            properties.containsKey("module.0.2.enabled") &&
            generalColumn?.title == "General" &&
            generalColumn.modules.size == 2
        val legacyRenderOptimiserElevenRows = needsLegacyLayoutMigration &&
            properties.containsKey("render_optimiser.10")

        for (index in YuriData.COLUMNS.indices) {
            val column = YuriData.COLUMNS[index]
            column.x = getInt(properties, "column.$index.x", column.x)
            column.y = getInt(properties, "column.$index.y", column.y)
            column.collapsed =
                properties.getProperty("column.$index.collapsed", column.collapsed.toString()).toBoolean()

            for (moduleIndex in column.modules.indices) {
                val module = column.modules[moduleIndex]
                val key = if (legacyGeneralThreeSlots && index == 0) {
                    when (moduleIndex) {
                        0 -> "module.0.0.enabled"
                        1 -> "module.0.2.enabled"
                        else -> "module.$index.$moduleIndex.enabled"
                    }
                } else {
                    "module.$index.$moduleIndex.enabled"
                }
                module.enabled = properties.getProperty(key, module.enabled.toString()).toBoolean()
            }
        }

        if (legacyRenderOptimiserElevenRows) {
            for (i in 0..6) {
                val raw = properties.getProperty("render_optimiser.$i") ?: continue
                RenderOptimiserModule.setEnabled(i, raw.trim().toBoolean())
            }
            properties.getProperty("render_optimiser.8")?.let {
                RenderOptimiserModule.setEnabled(7, it.trim().toBoolean())
            }
            properties.getProperty("render_optimiser.9")?.let {
                RenderOptimiserModule.setEnabled(8, it.trim().toBoolean())
            }
            properties.getProperty("render_optimiser.10")?.let {
                RenderOptimiserModule.setEnabled(9, it.trim().toBoolean())
            }
        } else {
            if (storedConfigFormat == 3) {
                // Format 3: indices 0–9 unchanged; 10 was armour (dropped); 11 health/hunger; 12 effects.
                for (i in 0..9) {
                    val raw = properties.getProperty("render_optimiser.$i") ?: continue
                    RenderOptimiserModule.setEnabled(i, raw.trim().toBoolean())
                }
                properties.getProperty("render_optimiser.11")?.let {
                    RenderOptimiserModule.setEnabled(RenderOptimiserModule.IDX_HIDE_ACTIONBAR_HUD, it.trim().toBoolean())
                }
                properties.getProperty("render_optimiser.12")?.let {
                    RenderOptimiserModule.setEnabled(RenderOptimiserModule.IDX_HIDE_EFFECTS_HUD, it.trim().toBoolean())
                }
            } else if (storedConfigFormat == 4) {
                // Format 4: indices 0–9 unchanged; 10 actionbar; 11 effects.
                for (i in 0..9) {
                    val raw = properties.getProperty("render_optimiser.$i") ?: continue
                    RenderOptimiserModule.setEnabled(i, raw.trim().toBoolean())
                }
                properties.getProperty("render_optimiser.10")?.let {
                    RenderOptimiserModule.setEnabled(RenderOptimiserModule.IDX_HIDE_ACTIONBAR_HUD, it.trim().toBoolean())
                }
                properties.getProperty("render_optimiser.11")?.let {
                    RenderOptimiserModule.setEnabled(RenderOptimiserModule.IDX_HIDE_EFFECTS_HUD, it.trim().toBoolean())
                }
            } else {
                for (optionIndex in 0 until RenderOptimiserModule.optionCount()) {
                    val key = "render_optimiser.$optionIndex"
                    val raw = properties.getProperty(key) ?: continue
                    RenderOptimiserModule.setEnabled(optionIndex, raw.trim().toBoolean())
                }
                if (storedConfigFormat < 3) {
                    val effectsRaw = properties.getProperty("render_optimiser.13")
                    if (effectsRaw != null) {
                        RenderOptimiserModule.setEnabled(RenderOptimiserModule.IDX_HIDE_EFFECTS_HUD, effectsRaw.trim().toBoolean())
                    }
                }
            }
        }

        for (optionIndex in 0 until ChatModule.optionCount()) {
            val key = "chat.$optionIndex"
            val raw = properties.getProperty(key) ?: continue
            ChatModule.setEnabled(optionIndex, raw.trim().toBoolean())
        }

        for (optionIndex in 0 until MobEspModule.optionCount()) {
            val key = "mob_esp.$optionIndex"
            val raw = properties.getProperty(key) ?: continue
            MobEspModule.setEnabled(optionIndex, raw.trim().toBoolean())
        }
        MobEspModule.trySetGlowColor(properties.getProperty("mob_esp.color", MobEspModule.glowColorHex()))
        DoorEspModule.trySetOutlineColor(properties.getProperty("door_esp.outline", DoorEspModule.outlineColorHex()))
        DoorEspModule.trySetOpenableColor(properties.getProperty("door_esp.openable", DoorEspModule.openableColorHex()))
        TranslucentDoorModule.setDoorsAlpha(
            getInt(properties, "translucent_door.alpha", TranslucentDoorModule.defaultDoorAlpha())
        )
        OpsecModule.nameChange = properties.getProperty("opsec.name_change", "").trim()
        CustomScoreboardModule.hudX = getInt(properties, "custom_scoreboard.x", CustomScoreboardModule.hudX)
        CustomScoreboardModule.hudY = getInt(properties, "custom_scoreboard.y", CustomScoreboardModule.hudY)
        for (optionIndex in 0 until CustomScoreboardModule.optionCount()) {
            val key = "custom_scoreboard.$optionIndex"
            val raw = properties.getProperty(key) ?: continue
            CustomScoreboardModule.setEnabled(optionIndex, raw.trim().toBoolean())
        }

        YuriDungeonMapHud.hudX = getInt(properties, "dungeon_map.hud_x", YuriDungeonMapHud.hudX)
        YuriDungeonMapHud.hudY = getInt(properties, "dungeon_map.hud_y", YuriDungeonMapHud.hudY)
        DungeonMapConfig.mapExtraInfo =
            properties.getProperty("dungeon_map.extra_info", DungeonMapConfig.mapExtraInfo.toString()).toBoolean()
        DungeonMapConfig.mapHideInBoss =
            properties.getProperty("dungeon_map.hide_in_boss", DungeonMapConfig.mapHideInBoss.toString()).toBoolean()
        DungeonMapConfig.dungeonMapCheater =
            properties.getProperty("dungeon_map.cheater", DungeonMapConfig.dungeonMapCheater.toString()).toBoolean()
        DungeonMapConfig.boxWitherDoors =
            properties.getProperty("dungeon_map.box_wither", DungeonMapConfig.boxWitherDoors.toString()).toBoolean()
        DungeonMapConfig.highlightMimicRoom =
            properties.getProperty("dungeon_map.highlight_mimic", DungeonMapConfig.highlightMimicRoom.toString()).toBoolean()
        YuriDungeonSummaryHud.hudX = getInt(properties, "dungeon_summary.hud_x", YuriDungeonSummaryHud.hudX)
        YuriDungeonSummaryHud.hudY = getInt(properties, "dungeon_summary.hud_y", YuriDungeonSummaryHud.hudY)
        YuriDungeonSummaryHud.enabled =
            properties.getProperty("dungeon_summary.enabled", YuriDungeonSummaryHud.enabled.toString()).toBoolean()

        if (needsLegacyLayoutMigration) {
            save()
        }
    }

    @JvmStatic
    fun save() {
        val path = getConfigPath()
        try {
            Files.createDirectories(path.parent)
        } catch (_: IOException) {
            return
        }

        val properties = Properties()
        properties.setProperty(CONFIG_FORMAT_KEY, CURRENT_CONFIG_FORMAT.toString())
        properties.setProperty("accent", YuriTheme.accentHex())
        properties.setProperty("keybind.open_gui.type", YuriClientMod.getOpenGuiKeyType())
        properties.setProperty("keybind.open_gui.code", YuriClientMod.getOpenGuiKeyCode().toString())

        val settings = YuriData.ANIMATION_SETTINGS
        properties.setProperty("anim.size", clamp(settings.size, 1, 20).toString())
        properties.setProperty("anim.pos_x", settings.posX.toString())
        properties.setProperty("anim.pos_y", settings.posY.toString())
        properties.setProperty("anim.pos_z", settings.posZ.toString())
        properties.setProperty("anim.swing_speed", clamp(settings.swingSpeed, 0, 20).toString())
        properties.setProperty("anim.ignore_haste", settings.ignoreHaste.toString())
        properties.setProperty("anim.ignore_equip", settings.ignoreEquip.toString())

        val playerSize = YuriData.PLAYER_SIZE_SETTINGS
        properties.setProperty("cos.player_size_x", clampFloat(playerSize.scaleX, 0.20F, 3.00F).toString())
        properties.setProperty("cos.player_size_y", clampFloat(playerSize.scaleY, 0.20F, 3.00F).toString())
        properties.setProperty("cos.player_size_z", clampFloat(playerSize.scaleZ, 0.20F, 3.00F).toString())

        for (index in YuriData.COLUMNS.indices) {
            val column = YuriData.COLUMNS[index]
            properties.setProperty("column.$index.x", column.x.toString())
            properties.setProperty("column.$index.y", column.y.toString())
            properties.setProperty("column.$index.collapsed", column.collapsed.toString())

            for (moduleIndex in column.modules.indices) {
                val module = column.modules[moduleIndex]
                properties.setProperty("module.$index.$moduleIndex.enabled", module.enabled.toString())
            }
        }

        for (optionIndex in 0 until RenderOptimiserModule.optionCount()) {
            properties.setProperty("render_optimiser.$optionIndex", RenderOptimiserModule.isEnabled(optionIndex).toString())
        }

        for (optionIndex in 0 until ChatModule.optionCount()) {
            properties.setProperty("chat.$optionIndex", ChatModule.isEnabled(optionIndex).toString())
        }

        for (optionIndex in 0 until MobEspModule.optionCount()) {
            properties.setProperty("mob_esp.$optionIndex", MobEspModule.isEnabled(optionIndex).toString())
        }
        properties.setProperty("mob_esp.color", MobEspModule.glowColorHex())
        properties.setProperty("door_esp.outline", DoorEspModule.outlineColorHex())
        properties.setProperty("door_esp.openable", DoorEspModule.openableColorHex())
        properties.setProperty("translucent_door.alpha", TranslucentDoorModule.doorsAlpha().toString())
        properties.setProperty("opsec.name_change", OpsecModule.nameChange.trim())
        properties.setProperty("custom_scoreboard.x", CustomScoreboardModule.hudX.toString())
        properties.setProperty("custom_scoreboard.y", CustomScoreboardModule.hudY.toString())
        for (optionIndex in 0 until CustomScoreboardModule.optionCount()) {
            properties.setProperty("custom_scoreboard.$optionIndex", CustomScoreboardModule.isEnabled(optionIndex).toString())
        }

        properties.setProperty("dungeon_map.hud_x", YuriDungeonMapHud.hudX.toString())
        properties.setProperty("dungeon_map.hud_y", YuriDungeonMapHud.hudY.toString())
        properties.setProperty("dungeon_map.extra_info", DungeonMapConfig.mapExtraInfo.toString())
        properties.setProperty("dungeon_map.hide_in_boss", DungeonMapConfig.mapHideInBoss.toString())
        properties.setProperty("dungeon_map.cheater", DungeonMapConfig.dungeonMapCheater.toString())
        properties.setProperty("dungeon_map.box_wither", DungeonMapConfig.boxWitherDoors.toString())
        properties.setProperty("dungeon_map.highlight_mimic", DungeonMapConfig.highlightMimicRoom.toString())
        properties.setProperty("dungeon_summary.hud_x", YuriDungeonSummaryHud.hudX.toString())
        properties.setProperty("dungeon_summary.hud_y", YuriDungeonSummaryHud.hudY.toString())
        properties.setProperty("dungeon_summary.enabled", YuriDungeonSummaryHud.enabled.toString())

        try {
            Files.newOutputStream(path).use { outputStream ->
                properties.store(outputStream, "Yuri Client Config")
            }
        } catch (_: IOException) {
        }
    }

    private fun getConfigPath(): Path = FabricLoader.getInstance().configDir.resolve(FILE_NAME)

    private fun getFloat(properties: Properties, key: String, fallback: Float): Float {
        val value = properties.getProperty(key) ?: return fallback
        return value.trim().toFloatOrNull() ?: fallback
    }

    private fun getInt(properties: Properties, key: String, fallback: Int): Int {
        val value = properties.getProperty(key) ?: return fallback
        return value.trim().toIntOrNull() ?: fallback
    }

    private fun clamp(value: Int, minValue: Int, maxValue: Int): Int = max(minValue, min(maxValue, value))

    private fun clampFloat(value: Float, minValue: Float, maxValue: Float): Float =
        max(minValue, min(maxValue, value))

    private fun parseKey(typeName: String?, code: Int): InputConstants.Key {
        val type = try {
            InputConstants.Type.valueOf(typeName?.uppercase()?.trim() ?: "KEYSYM")
        } catch (_: IllegalArgumentException) {
            InputConstants.Type.KEYSYM
        }
        return type.getOrCreate(code)
    }
}
