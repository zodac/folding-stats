function getOperatingSystem(value){
    if (value.toLowerCase() === "linux") {
        return "Linux";
    }

    if (value.toLowerCase() === "windows") {
        return "Windows";
    }

    return value;
}

function getCategory(value){
    if (value.toLowerCase() === "amd_gpu" || value.toLowerCase() === "amd gpu") {
        return "AMD GPU";
    }

    if (value.toLowerCase() === "nvidia_gpu" || value.toLowerCase() === "nvidia gpu") {
        return "nVidia GPU";
    }

    if (value.toLowerCase() === "wildcard") {
        return "Wildcard";
    }

    return value;
}