// https://stackoverflow.com/a/6234804
function escape(unsafe) {
    return unsafe.toString()
         .replaceAll('&', "&amp;")
         .replaceAll('<', "&lt;")
         .replaceAll('>', "&gt;")
         .replaceAll('"', "&quot;")
         .replaceAll("'", "&apos;");
}