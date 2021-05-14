// 'Borrowed' code:
// https://stackoverflow.com/questions/37179899/countdown-timer-every-hour-but-on-30-minute-marks
function updateTimer() {
    const zeroPad = (num, places) => String(num).padStart(places, '0')
    const timeOfHourForUpdate = 55;
    var time = new Date(),
    secsRemaining = 3600 - (time.getUTCMinutes()-timeOfHourForUpdate)%60 * 60 - time.getUTCSeconds();
    minutes = Math.floor(secsRemaining / 60) % 60;
    seconds = secsRemaining % 60;
    document.getElementById("min-part").innerHTML = minutes;
    document.getElementById("sec-part").innerHTML = zeroPad(seconds, 2);

    setTimeout(updateTimer, 1000 - (new Date()).getUTCMilliseconds() );
}