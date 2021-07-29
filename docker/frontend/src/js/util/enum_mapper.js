function getOperatingSystemFrontend(value) {
    if (value.toLowerCase() === "linux") {
        return "Linux";
    }

    if (value.toLowerCase() === "windows") {
        return "Windows";
    }

    return value;
}

function getOperatingSystemBackend(value) {
    if (value.toLowerCase() === "Linux") {
        return "linux";
    }

    if (value.toLowerCase() === "Windows") {
        return "windows";
    }

    return value;
}

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
    if (value.toLowerCase() === "AMD GPU") {
        return "amd_gpu";
    }

    if (value.toLowerCase() === "nVidia GPU") {
        return "nvidia_gpu";
    }

    if (value.toLowerCase() === "Wildcard") {
        return "wildcard";
    }

    return value;
}