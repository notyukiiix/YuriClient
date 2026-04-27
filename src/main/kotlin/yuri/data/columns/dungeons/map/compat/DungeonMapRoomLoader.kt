package yuri.data.columns.dungeons.map.compat

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import yuri.data.columns.dungeons.map.core.RoomData
import yuri.data.columns.dungeons.map.core.RoomType

/**
 * Loads [RoomData] from [DATA_PATH]. Drop a compatible `rooms.json` here for full room recognition.
 */
object DungeonMapRoomLoader {
    private const val DATA_PATH = "/data/yuri/dungeon_map/rooms.json"

    @JvmStatic
    fun load(): List<RoomData> {
        val stream = DungeonMapRoomLoader::class.java.getResourceAsStream(DATA_PATH) ?: return emptyList()
        return try {
            stream.bufferedReader().use { reader ->
                parseRooms(JsonParser.parseReader(reader))
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseRooms(root: com.google.gson.JsonElement): List<RoomData> {
        if (!root.isJsonArray) {
            return emptyList()
        }
        val arr = root.asJsonArray
        val out = ArrayList<RoomData>(arr.size())
        for (el in arr) {
            if (!el.isJsonObject) {
                continue
            }
            val o = el.asJsonObject
            val name = o.get("name")?.asString ?: continue
            val type = o.get("type")?.asString?.let { runCatching { RoomType.valueOf(it) }.getOrNull() } ?: RoomType.NORMAL
            val shape = o.get("shape")?.asString ?: ""
            val cores = parseIntArray(o.getAsJsonArray("cores"))
            val trapped = o.get("trappedChests")?.asInt ?: 0
            val revive = o.get("reviveStones")?.asInt ?: 0
            val secrets = o.get("secrets")?.asInt ?: 0
            val crypts = o.get("crypts")?.asInt ?: 0
            out.add(
                RoomData(
                    name = name,
                    type = type,
                    shape = shape,
                    cores = cores,
                    secretDetails = RoomData.SecretDetails(),
                    secretCoords = RoomData.SecretCoords(),
                    trappedChests = trapped,
                    reviveStones = revive,
                    secrets = secrets,
                    crypts = crypts
                )
            )
        }
        return out
    }

    private fun parseIntArray(arr: JsonArray?): List<Int> {
        if (arr == null) {
            return emptyList()
        }
        val out = ArrayList<Int>(arr.size())
        for (el in arr) {
            if (el.isJsonPrimitive && el.asJsonPrimitive.isNumber) {
                out.add(el.asInt)
            }
        }
        return out
    }
}
