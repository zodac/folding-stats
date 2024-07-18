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
    now = new Date()

    startOfMonth = Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), FIRST_DAY_OF_MONTH, 0, UPDATE_MINUTE, 0, 0)
    seconds = Math.floor((now - (startOfMonth))/1000)
    minutes = seconds/60
    hours = Math.floor(minutes/60)

    if (localContains(NUMBER_OF_UPDATES_PROPERTY_NAME)){
        previousNumber = localGet(NUMBER_OF_UPDATES_PROPERTY_NAME)

        if(previousNumber < hours){
            diff = hours - previousNumber
            updateCountToast = document.getElementById("toast-update-count-text")

            if(diff == 1){
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
    if(UPDATE_ENABLED == "false") {
        // Do not set up a timer if updating is disabled, and hide updates from the webpage
        hide("update_timer")
        return
    }

    currentDayOfMonth = new Date().getUTCDate()
    const zeroPad = (num, places) => String(num).padStart(places, '0')

    // If within the update period, set the update value to the next UPDATE_MINUTE
    if(currentDayOfMonth >= parseInt(FIRST_DAY_OF_MONTH)){
        time = new Date()
        secsRemaining = 3600 - (time.getUTCMinutes()-UPDATE_MINUTE)%60 * 60 - time.getUTCSeconds()
        minutes = Math.floor(secsRemaining / 60) % 60
        seconds = secsRemaining % 60
        document.getElementById("min-part").innerHTML = minutes
        document.getElementById("sec-part").innerHTML = zeroPad(seconds, 2)

        if(minutes === 0 && seconds === 0){
            showToast("toast-refresh", false)
        }
    } else { // If not within update period, set the update value to the FIRST_DAY_OF_MONTH
        time = new Date()
        var msToStart = Math.abs(time - Date.UTC(time.getUTCFullYear(), time.getUTCMonth(), FIRST_DAY_OF_MONTH))
        var minutes = Math.floor(msToStart / 60000)
        var seconds = Math.floor((msToStart % 60000) / 1000)

        document.getElementById("min-part").innerHTML = minutes
        document.getElementById("sec-part").innerHTML = zeroPad(seconds, 2)

        if(minutes === 0 && seconds === 0) {
            showToast("toast-refresh", false)
        }
    }

    // We want to check every 1s regardless of whether we're in the update period or not
    setTimeout(updateTimer, 1000 - (new Date()).getUTCMilliseconds())
}