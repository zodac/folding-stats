/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
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
function localGet(propertyName) {
    return localStorage.getItem(propertyName)
}

function localSet(propertyName, propertyValue) {
    localStorage.setItem(propertyName, propertyValue)
}

function localContains(propertyName) {
    return localGet(propertyName) !== null
}

function sessionGet(propertyName) {
    return sessionStorage.getItem(propertyName)
}

function sessionSet(propertyName, propertyValue) {
    sessionStorage.setItem(propertyName, propertyValue)
}

function sessionContains(propertyName) {
    return sessionGet(propertyName) !== null
}
