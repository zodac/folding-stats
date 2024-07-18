/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
const NUMBER_OF_UPDATES_PROPERTY_NAME = "numberOfUpdates"

const UPDATE_ENABLED = "%UPDATE_ENABLED%"
const FIRST_DAY_OF_MONTH = 3
const UPDATE_MINUTE = 55

function startTimer() {
    calculateNumberOfUpdates()
    updateTimer()
}

function calculateNumberOfUpdates() {
    const now = new Date()

    const startOfMonth = Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), FIRST_DAY_OF_MONTH, 0, UPDATE_MINUTE, 0, 0)
    const seconds = Math.floor((now - (startOfMonth)) / MILLISECONDS_IN_SECOND)
    const minutes = seconds / SECONDS_IN_MINUTE
    const hours = Math.floor(minutes / MINUTES_IN_HOUR)

    if (localContains(NUMBER_OF_UPDATES_PROPERTY_NAME)) {
        let previousNumber = localGet(NUMBER_OF_UPDATES_PROPERTY_NAME)

        if (previousNumber < hours) {
            let diff = hours - previousNumber
            let updateCountToast = document.getElementById("toast-update-count-text")

            if (diff == 1) {
                updateCountToast.innerHTML = diff.toLocaleString() + " update"
            } else {
                updateCountToast.innerHTML = diff.toLocaleString() + " updates"
            }

            showToast("toast-update", true)
        }
    }

    localSet(NUMBER_OF_UPDATES_PROPERTY_NAME, hours)
}

// https://stackoverflow.com/questions/37179899/countdown-timer-every-hour-but-on-30-minute-marks
function updateTimer() {
    if (UPDATE_ENABLED == "false") {
        // Do not set up a timer if updating is disabled, and hide updates from the webpage
        hide("update_timer")
        return
    }

    const now = new Date()
    const currentDayOfMonth = now.getUTCDate()
    const zeroPad = (num, places) => String(num).padStart(places, '0')

    // If within the update period, set the update value to the next UPDATE_MINUTE
    if (currentDayOfMonth >= parseInt(FIRST_DAY_OF_MONTH)) {
        let secsRemaining = SECONDS_IN_HOUR - (now.getUTCMinutes() - UPDATE_MINUTE) % SECONDS_IN_HOUR - time.getUTCSeconds()
        let minutes = Math.floor(secsRemaining / SECONDS_IN_MINUTE) % SECONDS_IN_MINUTE
        let seconds = secsRemaining % SECONDS_IN_MINUTE

        document.getElementById("min-part").innerHTML = minutes
        document.getElementById("sec-part").innerHTML = zeroPad(seconds, 2)

        if (minutes === 0 && seconds === 0) {
            showToast("toast-refresh", false)
        }
    } else { // If not within update period, set the update value to the FIRST_DAY_OF_MONTH
        let msToStart = Math.abs(now - Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), FIRST_DAY_OF_MONTH))
        let minutes = millisAsMinutes(msToStart)
        let seconds = Math.floor((msToStart % MILLISECONDS_IN_MINUTE) / MILLISECONDS_IN_SECOND)

        document.getElementById("min-part").innerHTML = minutes
        document.getElementById("sec-part").innerHTML = zeroPad(seconds, 2)

        if (minutes === 0 && seconds === 0) {
            showToast("toast-refresh", false)
        }
    }

    // We want to check every 1s regardless of whether we're in the update period or not
    setTimeout(updateTimer, MILLISECONDS_IN_SECOND - now.getUTCMilliseconds())
}
