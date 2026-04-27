package yuri.data.columns.dungeons.map.handlers

/**
 * Dungeon score state shell. Scoreboard parsing / external sync not wired yet.
 */
object ScoreCalculation {
    @JvmField
    var mimicKilled: Boolean = false

    @JvmField
    var princeKilled: Boolean = false

    @JvmField
    var deathCount: Int = 0

    @JvmField
    var foundSecrets: Int = 0

    @JvmField
    var cryptsCount: Int = 0

    @JvmField
    var secretPercentage: Double = 0.0

    @JvmField
    var clearedPercentage: Int = 0

    @JvmField
    var completedRooms: Int = 0

    @JvmField
    var secondsElapsed: Int = 0

    @JvmField
    var score: Int = 0

    @JvmStatic
    fun registerEvents() {
    }
}
