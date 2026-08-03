package com.studysprint.app.data.model

/**
 * A short break activity suggested between focus sessions. Tagged as indoor
 * or outdoor so the picker can prefer outdoor ones when weather is nice.
 */
data class BreakActivity(
    val title: String,
    val description: String,
    val indoor: Boolean,
)

/**
 * Curated library of break activities. Kept on-device — no network needed —
 * so breaks still work offline. This also serves as the fallback when the
 * weather fetch fails.
 */
object BreakActivityLibrary {

    val activities: List<BreakActivity> = listOf(
        BreakActivity("Stretch it out", "Stand and do 2 minutes of gentle stretching.", indoor = true),
        BreakActivity("Eye reset", "Look at something 6m away for 20 seconds. Repeat 3x.", indoor = true),
        BreakActivity("Water break", "Walk to the kitchen and refill your bottle.", indoor = true),
        BreakActivity("Desk tidy", "Clear your desk for 2 minutes. Reset the space.", indoor = true),
        BreakActivity("Deep breaths", "Box breathing: in 4s, hold 4s, out 4s, hold 4s.", indoor = true),
        BreakActivity("Quick walk", "Step outside for a 5-minute walk around the block.", indoor = false),
        BreakActivity("Fresh air", "Open a window or step outside for some sunlight.", indoor = false),
        BreakActivity("Sky glance", "Look up at the sky for a minute — change your focal depth.", indoor = false),
        BreakActivity("Snack run", "Grab a healthy snack and eat it away from your desk.", indoor = true),
        BreakActivity("Posture check", "Roll your shoulders, sit up, reset your spine.", indoor = true),
        BreakActivity("Stair climb", "Walk a flight of stairs to get the blood moving.", indoor = false),
        BreakActivity("Mindful minute", "Close your eyes and notice three sounds around you.", indoor = true),
    )

    /**
     * Pick a sensible activity. When [preferOutdoor] is true (nice weather),
     * we bias toward outdoor activities; otherwise indoor.
     */
    fun pick(preferOutdoor: Boolean, random: kotlin.random.Random = kotlin.random.Random): BreakActivity {
        val pool = if (preferOutdoor) activities.filter { !it.indoor } + activities
        else activities.filter { it.indoor }.ifEmpty { activities }
        return pool.random(random)
    }
}
