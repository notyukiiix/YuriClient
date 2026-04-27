package yuri.client

import java.util.Locale

object ShortCommandHelper {
    private val aliases: Map<String, String> = linkedMapOf(
        "hub" to "h",
        "dh" to "warp dh",
        "dhub" to "warp dungeon_hub",
        "dungeons" to "warp dungeon_hub",
        "castle" to "warp castle",
        "barn" to "warp barn",
        "park" to "warp park",
        "museum" to "warp museum",
        "da" to "visit da",
        "ec" to "enderchest",
        "bz" to "bazaar",
        "ah" to "auctionhouse",
        "cp" to "chat p",
        "cg" to "chat g",
        "ca" to "chat a",
        "pd" to "p disband",
        "pw" to "p warp"
    )

    @JvmStatic
    fun rewriteShortCommand(rawCommand: String): String? {
        if (rawCommand.isBlank()) {
            return null
        }
        val normalized = rawCommand.trim().removePrefix("/")
        if (normalized.isBlank()) {
            return null
        }
        val parts = normalized.split(Regex("\\s+"), limit = 2)
        val command = parts[0].lowercase(Locale.ROOT)
        val args = if (parts.size > 1) parts[1].trim() else ""
        if (command == "pt") {
            if (args.isEmpty()) {
                return null
            }
            return "/p transfer $args"
        }
        if (args.isNotEmpty()) {
            return null
        }
        val mapped = aliases[command] ?: return null
        return "/$mapped"
    }

    @JvmStatic
    fun tabSuggestions(rawPrefix: String): List<String> {
        val prefix = rawPrefix.trim().removePrefix("/").lowercase(Locale.ROOT)
        if (prefix.contains(" ")) {
            return emptyList()
        }
        return (aliases.keys + "pt")
            .asSequence()
            .filter { it.startsWith(prefix) }
            .map { "/$it" }
            .toList()
    }
}
