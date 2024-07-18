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
function show(elementId) {
    showElement(document.getElementById(elementId))
}

function showElement(element) {
    element.style.display = "block"
}

function hide(elementId) {
    hideElement(document.getElementById(elementId))
}

function hideElement(element) {
    element.style.display = "none"
}

// The 'toggle' functions below simply change the colour of the buttons. There must be a smarter way to do this...
function toggleMainButtonStyle(id, classList){
    var button = document.getElementById(id)

    if(classList.contains("collapsed")){
        button.classList.add("btn-primary")
        button.classList.remove("btn-success")
    } else {
        button.classList.add("btn-success")
        button.classList.remove("btn-primary")
    }
}
