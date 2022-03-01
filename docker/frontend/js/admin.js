const REST_ENDPOINT_URL="%REST_ENDPOINT_URL%";

function adminLogin(){
    var userName = document.getElementById("login_username").value;
    var password = document.getElementById("login_password").value;
    var authorizationPayload = "Basic " + encode(userName, password);

    var requestData = JSON.stringify(
        {
            "encodedUserNameAndPassword": authorizationPayload
        }
    );

    show("loader");

    fetch(REST_ENDPOINT_URL+"/login/admin", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: requestData
    })
    .then(response => {
        hide("loader");
        document.getElementById("login_username").value = "";
        document.getElementById("login_password").value = "";

        if(response.status != 200){
            failureToast("Invalid admin credentials!");
            return;
        }

        successToast("Logged in successfully!");
        hide("login_form");
        show("admin_functions");
        sessionSet("Authorization", authorizationPayload);
    })
    .catch((error) => {
        hide("loader");
        console.error("Unexpected error logging in: ", error);
        return false;
    });
}

function manualUpdate() {
    show("loader");
    fetch(REST_ENDPOINT_URL+"/stats/manual/update", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        }
    })
    .then(response => {
        hide("loader");

        if(response.status != 200){
            failureToast("Manual update failed with code: " + response.status);
            response.json()
            .then(response => {
                console.error(JSON.stringify(response, null, 2));
            });
            return;
        }
        successToast("Stats manually updated");
    })
    .catch((error) => {
        hide("loader");
        console.error("Unexpected error updating stats: ", error);
        return false;
    });
}

function manualLars() {
    show("loader");
    fetch(REST_ENDPOINT_URL+"/debug/lars", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        }
    })
    .then(response => {
        hide("loader");

        if(response.status != 200){
            failureToast("Manual LARS update failed with code: " + response.status);
            response.json()
            .then(response => {
                console.error(JSON.stringify(response, null, 2));
            });
            return;
        }

        successToast("LARS data manually updated");
        loadHardware();
    })
    .catch((error) => {
        hide("loader");
        console.error("Unexpected error updating LARS: ", error);
        return false;
    });
}

function manualResultSave() {
    show("loader");
    fetch(REST_ENDPOINT_URL+"/results/manual/save", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        }
    })
    .then(response => {
        hide("loader");

        if(response.status != 200){
            failureToast("Manual result save failed with code: " + response.status);
            response.json()
            .then(response => {
                console.error(JSON.stringify(response, null, 2));
            });
            return;
        }
        successToast("Result manually saved");
    })
    .catch((error) => {
        hide("loader");
        console.error("Unexpected error saving result: ", error);
        return false;
    });
}

function printCaches() {
    show("loader");
    fetch(REST_ENDPOINT_URL+"/debug/caches", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": sessionGet("Authorization")
        }
    })
    .then(response => {
        hide("loader");

        if(response.status != 200){
            failureToast("Printing caches failed with code: " + response.status);
            response.json()
            .then(response => {
                console.error(JSON.stringify(response, null, 2));
            });
            return;
        }
        successToast("Caches printed");
    })
    .catch((error) => {
        hide("loader");
        console.error("Unexpected error printing caches: ", error);
        return false;
    });
}

document.addEventListener("DOMContentLoaded", function(event) {
    if(sessionContains("Authorization")) {
        hide("login_form");
        show("admin_functions");
    }

    updateTimer();
    hide("loader");
});
