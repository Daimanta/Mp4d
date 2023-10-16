function get_link(link, text) {
    const result = document.createElement("a");
    result.href = link;
    result.textContent = text;
    return result;
}

function rest_get_with_name(rest_name, file_name) {
    fetch(rest_name)
        .then( response => response.blob() )
        .then( blob => download_with_name(blob, file_name) )
}

function download_with_name(blob, name) {
    const file = window.URL.createObjectURL(blob);
    let fileLink = document.createElement('a');
    fileLink.href = file;
    fileLink.download = name;
    fileLink.click()
}