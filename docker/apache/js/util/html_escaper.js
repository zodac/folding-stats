// https://stackoverflow.com/a/6234804
function escape(unsafe) {
    return unsafe
         .replaceAll('&', "&amp;")
         .replaceAll('<', "&lt;")
         .replaceAll('>', "&gt;")
         .replaceAll('"', "&quot;")
         .replaceAll("'", "&apos;");
}