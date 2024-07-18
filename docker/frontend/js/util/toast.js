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
function showToast(id, autohide) {
    new bootstrap
        .Toast(document.getElementById(id),
            {
                animation: true,
                autohide: autohide,
                delay: 3000 // 3 seconds
            }
        )
        .show()
}

function closeToast(id) {
    new bootstrap
        .Toast(document.getElementById(id))
        .dispose()
}

function successToast(text) {
    document.getElementById("toast-success-text").innerHTML = html_escape(text)
    showToast("toast-success", true)
}

function failureToast(text) {
    document.getElementById("toast-failure-text").innerHTML = html_escape(text)
    showToast("toast-failure", true)
}

function failureToastPermanent(text) {
    document.getElementById("toast-failure-text").innerHTML = html_escape(text)
    showToast("toast-failure", false)
}
