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
function getRoleFrontend(value) {
    if (value.toLowerCase() === "captain") {
        return "Captain"
    }

    if (value.toLowerCase() === "member") {
        return "Member"
    }

    return value
}

function getCategoryFrontend(value) {
    if (value.toLowerCase() === "amd_gpu") {
        return "AMD GPU"
    }

    if (value.toLowerCase() === "nvidia_gpu") {
        return "nVidia GPU"
    }

    if (value.toLowerCase() === "wildcard") {
        return "Wildcard"
    }

    return value
}

function getCategoryBackend(value) {
    if (value.toLowerCase() === "amd gpu") {
        return "amd_gpu"
    }

    if (value.toLowerCase() === "nvidia gpu") {
        return "nvidia_gpu"
    }

    if (value.toLowerCase() === "wildcard") {
        return "wildcard"
    }

    return value
}

function getHardwareMakeFrontend(value) {
    if (value.toLowerCase() === "amd") {
        return "AMD"
    }

    if (value.toLowerCase() === "nvidia") {
        return "nVidia"
    }

    if (value.toLowerCase() === "intel") {
        return "Intel"
    }

    return value
}

function getHardwareMakeBackend(value) {
    if (value.toLowerCase() === "amd gpu") {
        return "AMD"
    }

    if (value.toLowerCase() === "nvidia") {
        return "NVIDIA"
    }

    if (value.toLowerCase() === "intel") {
        return "INTEL"
    }

    return value
}

function getHardwareTypeFrontend(value) {
    if (value.toLowerCase() === "gpu") {
        return "GPU"
    }

    if (value.toLowerCase() === "cpu") {
        return "CPU"
    }

    return value
}

function getHardwareTypeBackend(value) {
    return getHardwareTypeFrontend(value)
}

function getUserChangeStateFrontend(value) {
    if (value.toLowerCase() === "requested_now") {
        return "Requested (Immediate)"
    }

    if (value.toLowerCase() === "requested_next_month") {
        return "Requested (1st " + nextMonth() + ")"
    }

    if (value.toLowerCase() === "approved_now") {
        return "Approved (change in progress...)"
    }

    if (value.toLowerCase() === "approved_next_month") {
        return "Approved (will be applied 1st " + nextMonth() + ")"
    }

    if (value.toLowerCase() === "rejected") {
        return "Rejected"
    }

    if (value.toLowerCase() === "completed") {
        return "Completed"
    }

    return value
}

function nextMonth() {
    var now = new Date()
    if (now.getUTCMonth() == 11) {
        return new Date(now.getUTCFullYear() + 1, 0, 1).toLocaleString("default", { month: "long" })
    }

    return new Date(now.getUTCFullYear(), now.getUTCMonth() + 1, 1).toLocaleString("default", { month: "long" })
}
