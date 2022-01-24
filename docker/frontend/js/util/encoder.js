function encode(userName, password) {
    return btoa(userName + ":" + password);
}
