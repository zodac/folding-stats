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