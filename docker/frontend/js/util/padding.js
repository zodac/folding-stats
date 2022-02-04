function leftPad(input, width, paddingChar) {
    return (String(paddingChar).repeat(width) + String(input)).slice(String(input).length);
}
