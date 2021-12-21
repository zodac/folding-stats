function sortJsonByKey(key) {
    return function(first, second) {
        firstVal = first[key].toString().toLowerCase()
        secondVal = second[key].toString().toLowerCase()

        if (isNaN(firstVal) || isNaN(secondVal)){
            if (firstVal > secondVal) {
                return 1;
            }
            if (firstVal < secondVal) {
                return -1;
            }
            return 0;
        } else {
            if (parseInt(firstVal) > parseInt(secondVal)) {
                return 1;
            }
            if (parseInt(firstVal) < parseInt(secondVal)) {
                return -1;
            }
            return 0;
        }
    }
}
