package yuri.data.columns.general.modules

import yuri.YuriData
import kotlin.random.Random

object SillySpeakModule {
    private const val TITLE = "Silly Speak"

    @JvmField
    val module: YuriData.Module = YuriData.Module(TITLE)

    @JvmStatic
    fun transformOutgoingMessage(message: String?): String {
        val base = message?.trim().orEmpty()
        if (!module.enabled || base.isEmpty() || base.startsWith("/")) {
            return message.orEmpty()
        }

        val words = base.split(Regex("\\s+")).toMutableList()
        val maxInsertions = minOf(4, maxOf(2, words.size / 2))
        val insertionCount = Random.nextInt(2, maxInsertions + 1)
        repeat(insertionCount) {
            val token = TOKENS[Random.nextInt(TOKENS.size)]
            val index = Random.nextInt(words.size + 1)
            words.add(index, token)
        }

        val transformed = words.joinToString(" ")
        return if (transformed.length <= MAX_CHAT_LENGTH) transformed else transformed.substring(0, MAX_CHAT_LENGTH)
    }

    private val TOKENS: List<String> = listOf(
        "uwu",
        "owo",
        "meow",
        "nyaa",
        "purrr",
        ">///<",
        ">.<",
        "O.o",
        "rawr",
        ":3",
        "x3",
        "mrrp"
    )
    private const val MAX_CHAT_LENGTH: Int = 256
}
