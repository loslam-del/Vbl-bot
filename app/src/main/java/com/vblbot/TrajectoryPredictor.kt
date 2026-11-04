package com.vblbot

data class BallPoint(val x: Float, val y: Float, val timeMs: Long)
data class BallPrediction(val x: Float, val y: Float, val confidence: Float, val note: String)

enum class ShotProfile(val label: String, val horizonMs: Long, val adjustment: Float) {
    GENERAL("Unknown / general", 350L, 1.0f),
    FAST("Fast hit", 220L, 1.0f),
    FLOAT("Float / spin", 300L, 0.85f),
    SERVE("Serve", 350L, 0.9f),
    SPECIAL("Special ability", 180L, 0.7f)
}

/** Short-term 2D screen-coordinate estimate from three or more observations. */
object TrajectoryPredictor {
    fun predict(points: List<BallPoint>, profile: ShotProfile): BallPrediction? {
        if (points.size < 3) return null
        val recent = points.takeLast(6)
        val first = recent.first()
        val last = recent.last()
        val elapsed = last.timeMs - first.timeMs
        if (elapsed <= 0L) return null
        val vx = (last.x - first.x) / elapsed.toFloat()
        val vy = (last.y - first.y) / elapsed.toFloat()
        val horizon = profile.horizonMs * profile.adjustment
        val confidence = (recent.size / 6f * (elapsed / 180f)).coerceIn(0.2f, 0.85f)
        return BallPrediction(
            x = last.x + vx * horizon,
            y = last.y + vy * horizon,
            confidence = confidence,
            note = "Short-term screen-space estimate; update it with new frames."
        )
    }
}
