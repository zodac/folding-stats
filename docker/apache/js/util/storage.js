function localGet(propertyName) {
    return localStorage.getItem(propertyName);
}

function localSet(propertyName, propertyValue) {
    localStorage.setItem(propertyName, propertyValue);
}

function localContains(propertyName) {
    return localGet(propertyName) !== null;
}

function sessionGet(propertyName) {
    return sessionStorage.getItem(propertyName);
}

function sessionSet(propertyName, propertyValue) {
    sessionStorage.setItem(propertyName, propertyValue);
}

function sessionContains(propertyName) {
    return sessionGet(propertyName) !== null;
}