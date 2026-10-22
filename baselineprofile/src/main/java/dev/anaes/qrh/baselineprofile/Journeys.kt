package dev.anaes.qrh.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.SearchCondition
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import java.util.regex.Pattern

// The app's own text, as the user sees it (from strings.xml and data.json)
private const val AGREE = "Agree & Continue"
private const val GUIDELINE = "Cardiac arrest"
/** One that's not a match for [SEARCH_QUERY], and is on the list's first screen. */
private const val OTHER_GUIDELINE = "Key basic plan"
private const val SEARCH_HINT = "Search guidelines…"
private const val VIEW_CARDS = "View cards"
private const val SEARCH_QUERY = "adrenaline"
private const val CLEAR_SEARCH = "Clear search"

// Generous, for slow emulators: a step that never happens still fails, just later
private const val TIMEOUT = 10_000L
private const val AGREE_TIMEOUT = 2_000L
private const val MAX_AGREE_ATTEMPTS = 10

/** Share of an element's size kept clear around a gesture, so it starts clear of its edges. */
private const val GESTURE_MARGIN = 0.1f

/** The app under test, as passed by the Baseline Profile Gradle plugin. */
val targetPackage: String =
    InstrumentationRegistry.getArguments().getString("targetAppId") ?: "dev.anaes.qrh"

/** Agrees to the disclaimers if they're showing (the first launch after installing), to reach the list. */
fun MacrobenchmarkScope.acceptDisclaimers() {
    device.wait(Until.hasObject(By.text(Pattern.compile("$AGREE|$GUIDELINE"))), TIMEOUT)
    if (!device.hasObject(By.text(AGREE))) return
    // Agreeing needs the disclaimers read to the end first. UiAutomator can't reliably tell when a
    // Compose page is at its end, so this scrolls and tries to agree until the list opens.
    val disclaimers = device.findObject(By.scrollable(true))
    disclaimers.setGestureMarginPercentage(GESTURE_MARGIN)
    repeat(MAX_AGREE_ATTEMPTS) {
        repeat(3) { disclaimers.fling(Direction.DOWN) }
        device.findObject(By.text(AGREE))?.click()
        if (device.wait(Until.hasObject(By.text(GUIDELINE)), AGREE_TIMEOUT)) return
    }
    error("Couldn't agree to the disclaimers")
}

/** Scrolls down the guideline list and back. */
fun MacrobenchmarkScope.browseList() {
    val list = device.findObject(By.scrollable(true).hasDescendant(By.text(GUIDELINE)))
    list.setGestureMarginPercentage(GESTURE_MARGIN)
    list.fling(Direction.DOWN)
    waitFor(Until.gone(By.text(GUIDELINE)))
    checkNotNull(list.scrollUntil(Direction.UP, Until.findObject(By.text(GUIDELINE)))) { "Couldn't scroll back up the list" }
}

/** Opens a guideline, scrolls through it, and looks through its card view. */
fun MacrobenchmarkScope.readGuideline() {
    device.findObject(By.text(GUIDELINE)).click()
    waitFor(Until.hasObject(By.desc(VIEW_CARDS)))

    largestScrollable().run {
        setGestureMarginPercentage(GESTURE_MARGIN)
        fling(Direction.DOWN)
        device.waitForIdle()
        fling(Direction.UP)
        device.waitForIdle()
    }

    device.findObject(By.desc(VIEW_CARDS)).click()
    waitFor(Until.hasObject(By.text(Pattern.compile("""1 of \d+"""))))
    // One card at a time, each swipe once the last has settled
    val cards = largestScrollable().apply { setGestureMarginPercentage(GESTURE_MARGIN) }
    for (card in 2..4) {
        cards.swipe(Direction.LEFT, 0.8f)
        waitFor(Until.hasObject(By.text(Pattern.compile("""$card of \d+"""))))
    }

    device.pressBack()
    waitFor(Until.hasObject(By.desc(VIEW_CARDS)))
    device.pressBack()
    waitFor(Until.gone(By.desc(VIEW_CARDS)))
}

/** Searches the guidelines' content, opens a match at the place it's found, then clears the search. */
fun MacrobenchmarkScope.searchGuidelines() {
    device.findObject(By.text(SEARCH_HINT)).click()
    waitFor(Until.findObject(By.focused(true))).text = SEARCH_QUERY
    // The list narrowed to the matches
    waitFor(Until.gone(By.text(OTHER_GUIDELINE)))
    // Scrolling to it, as the keyboard may be leaving the list little room (on a tablet in landscape)
    findInList(GUIDELINE, Direction.DOWN).click()
    waitFor(Until.hasObject(By.desc(VIEW_CARDS)))
    device.waitForIdle()

    // On a tablet the keyboard can still be up beside the guideline, and Back closes it first
    val closed = (1..2).any {
        device.pressBack()
        device.wait(Until.gone(By.desc(VIEW_CARDS)), TIMEOUT)
    }
    check(closed) { "Couldn't close the guideline" }
    // With the button, as Back would close the keyboard (back with the search field's focus) first
    device.findObject(By.desc(CLEAR_SEARCH)).click()
    // The whole list, back at its top
    waitFor(Until.hasObject(By.text(OTHER_GUIDELINE)))
}

/** The guideline list's row with [text], scrolling [direction] to it if it's not in view. */
private fun MacrobenchmarkScope.findInList(text: String, direction: Direction): UiObject2 {
    device.wait(Until.hasObject(By.text(text)), TIMEOUT)
    device.findObject(By.text(text))?.let { return it }
    // The scrollable with guideline codes in it (with no guideline open)
    val list = device.findObject(By.scrollable(true).hasDescendant(By.text(Pattern.compile("""\d+-\d+"""))))
    list.setGestureMarginPercentage(GESTURE_MARGIN)
    return checkNotNull(list.scrollUntil(direction, Until.findObject(By.text(text)))) { "Couldn't find $text in the list" }
}

/** Waits for [condition], failing the run if it's not met, so a missed step can't go unnoticed. */
private fun <R> MacrobenchmarkScope.waitFor(condition: SearchCondition<R>): R =
    device.wait(condition, TIMEOUT).also { check(it != null && it != false) { "Timed out waiting for $condition" } }

/** The guideline when it's beside the list (on a tablet), as it's the wider of the two. */
private fun MacrobenchmarkScope.largestScrollable(): UiObject2 =
    device.findObjects(By.scrollable(true)).maxBy { it.visibleBounds.width() * it.visibleBounds.height() }
