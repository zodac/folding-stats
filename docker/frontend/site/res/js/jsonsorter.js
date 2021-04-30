function sortJsonByKey(key) {
    return function(first, second) {
        if (first[key] > second[key]) {
            return 1;
        }
        if (first[key] < second[key]) {
            return -1;
        }
        return 0;
    }
}