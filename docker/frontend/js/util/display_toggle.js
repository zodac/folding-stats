function show(elementId) {
    document.getElementById(elementId).style.display = "block";
}

function showElement(element) {
    element.style.display = "block";
}

function hide(elementId) {
    document.getElementById(elementId).style.display = "none";
}

function hideElement(element) {
    element.style.display = "none";
}

// The 'toggle' functions below simply change the colour of the buttons. There must be a smarter way to do this...
function toggleMainButtonStyle(id, classList){
    var button = document.getElementById(id);

    if(classList.contains("collapsed")){
        button.classList.add("btn-primary");
        button.classList.remove("btn-success");
    } else {
        button.classList.add("btn-success");
        button.classList.remove("btn-primary");
    }
}
