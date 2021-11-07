function sortJsonByKey(key) {
    return function(first, second) {
        if (first[key].toString().toLowerCase() > second[key].toString().toLowerCase()) {
            return 1;
        }
        if (first[key].toString().toLowerCase() < second[key].toString().toLowerCase()) {
            return -1;
        }
        return 0;
    }
}