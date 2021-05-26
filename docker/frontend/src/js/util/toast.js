function showToast(id, autohide) {
    new bootstrap
        .Toast(document.getElementById(id),
            {
                animation: true,
                autohide: autohide,
                delay: 3000 // 3 seconds
            }
        )
        .show();
}

function closeToast(id) {
    new bootstrap
        .Toast(document.getElementById(id))
        .dispose();
}

function successToast(text){
    document.getElementById("toast-success-text").innerHTML = text;
    showToast("toast-success", true);
}

function failureToast(text){
    document.getElementById("toast-failure-text").innerHTML = text;
    showToast("toast-failure", true);
}