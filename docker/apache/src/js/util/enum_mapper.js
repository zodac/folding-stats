function getCategoryFrontend(value) {
    if (value.toLowerCase() === "amd_gpu") {
        return "AMD GPU";
    }

    if (value.toLowerCase() === "nvidia_gpu") {
        return "nVidia GPU";
    }

    if (value.toLowerCase() === "wildcard") {
        return "Wildcard";
    }

    return value;
}

function getCategoryBackend(value) {
    if (value.toLowerCase() === "amd gpu") {
        return "amd_gpu";
    }

    if (value.toLowerCase() === "nvidia gpu") {
        return "nvidia_gpu";
    }

    if (value.toLowerCase() === "wildcard") {
        return "wildcard";
    }

    return value;
}

function getHardwareMakeFrontend(value) {
    if (value.toLowerCase() === "amd") {
        return "AMD";
    }

    if (value.toLowerCase() === "nvidia") {
        return "nVidia";
    }

    if (value.toLowerCase() === "intel") {
        return "Intel";
    }

    return value;
}

function getHardwareMakeBackend(value) {
    if (value.toLowerCase() === "amd gpu") {
        return "AMD";
    }

    if (value.toLowerCase() === "nvidia") {
        return "NVIDIA";
    }

    if (value.toLowerCase() === "intel") {
        return "INTEL";
    }

    return value;
}

function getHardwareTypeFrontend(value) {
    if (value.toLowerCase() === "gpu") {
        return "GPU";
    }

    if (value.toLowerCase() === "cpu") {
        return "CPU";
    }

    return value;
}

function getHardwareTypeBackend(value) {
    if (value.toLowerCase() === "gpu") {
        return "GPU";
    }

    if (value.toLowerCase() === "cpu") {
        return "CPU";
    }

    return value;
}