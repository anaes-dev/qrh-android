package dev.anaes.qrh.data

/** Whether the disclaimers need to be shown on launch, and in which form. */
enum class DisclaimerStatus {
    /** Accepted for the current version; go straight to the list. */
    ACCEPTED,

    /** Never accepted: show the first-launch disclaimers. */
    FIRST_RUN,

    /** Accepted for an older version: show the disclaimers with the "updated" notice. */
    UPDATED,
}

/** Pure decision logic for [UserPreferences.disclaimerStatus], kept separate for unit testing. */
internal fun disclaimerStatusFor(
    seenWarning: Boolean,
    legacySeenWarning: Boolean,
    savedVersion: Int,
    currentVersion: Int,
): DisclaimerStatus = when {
    seenWarning && savedVersion >= currentVersion -> DisclaimerStatus.ACCEPTED
    seenWarning || legacySeenWarning -> DisclaimerStatus.UPDATED
    else -> DisclaimerStatus.FIRST_RUN
}
