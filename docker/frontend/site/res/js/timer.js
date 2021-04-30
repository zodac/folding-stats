// Stolen code, again:
// https://stackoverflow.com/questions/37179899/countdown-timer-every-hour-but-on-30-minute-marks
function updateTime() {
    const zeroPad = (num, places) => String(num).padStart(places, '0')
    var time = new Date(),
    secsRemaining = 3600 - (time.getUTCMinutes()-15)%60 * 60 - time.getUTCSeconds();
    minutes = Math.floor(secsRemaining / 60) % 60;
    seconds = secsRemaining % 60;
    $("#min-part").text(minutes);
    $("#sec-part").text(zeroPad(seconds, 2));

    setTimeout( updateTime, 1000 - (new Date()).getUTCMilliseconds() );
}