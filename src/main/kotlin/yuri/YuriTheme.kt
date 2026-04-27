package yuri

object YuriTheme {
    private const val DEFAULT_RGB = 0xFFFFFF

    private var accentArgb: Int = 0

    init {
        setAccentRgb(DEFAULT_RGB)
    }

    @JvmStatic
    fun accent(): Int = accentArgb

    @JvmStatic
    fun backgroundTop(): Int = 0x7A06070A

    @JvmStatic
    fun backgroundBottom(): Int = 0x94030405.toInt()

    @JvmStatic
    fun columnBody(): Int = 0x7A090B0F

    @JvmStatic
    fun columnBorder(): Int = 0x92272D36.toInt()

    @JvmStatic
    fun headerBase(): Int = 0xE0121419.toInt()

    @JvmStatic
    fun subGuiBase(): Int = 0xD0101217.toInt()

    @JvmStatic
    fun subGuiInput(): Int = 0xD008090C.toInt()

    @JvmStatic
    fun shadow(): Int = 0x7810141A

    @JvmStatic
    fun accentSoft(): Int = withAlpha(accentArgb, 0x9A)

    @JvmStatic
    fun accentMuted(): Int = withAlpha(accentArgb, 0x66)

    @JvmStatic
    fun accentHex(): String = String.format("#%06X", accentArgb and 0xFFFFFF)

    @JvmStatic
    fun reset() {
        setAccentRgb(DEFAULT_RGB)
    }

    @JvmStatic
    fun trySetAccent(value: String?): Boolean {
        if (value == null) {
            return false
        }

        var normalized = value.trim()
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1)
        }

        if (normalized.length != 6) {
            return false
        }

        for (ch in normalized) {
            if (Character.digit(ch, 16) < 0) {
                return false
            }
        }

        setAccentRgb(normalized.toInt(16))
        return true
    }

    private fun setAccentRgb(rgb: Int) {
        accentArgb = -0x1000000 or rgb
    }

    private fun withAlpha(color: Int, alpha: Int): Int {
        return (alpha and 0xFF shl 24) or (color and 0xFFFFFF)
    }
}
