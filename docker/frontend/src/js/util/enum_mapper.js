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
    if (value.toLowerCase() === "linux") {
        return "linux";
    }

    if (value.toLowerCase() === "windows") {
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