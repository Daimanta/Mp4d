function store_search() {
    const result = {
        "all": true,
        "search": null,
        "artist": null,
        "album": null,
        "song": null,
        "year": null,
        "start_folder": null,
    };
    sessionStorage.setItem("search", JSON.stringify(result));
    window.location = "search_results.html";
}

function do_search() {
    const element = document.getElementById("list_id");
    const songs = [{"uuid": "asdsdsd", "name": "asdsdds", "folder_id": "dfsdsds", "folder_name": "fdsfdfdfd"}];
    for (let song of songs) {
        const tr = document.createElement("tr");
        const song_td = document.createElement("td");
        song_td.align = "left";
        song_td.width = "50%";
        song_td.appendChild(get_link(song.uuid, song.name));
        tr.appendChild(song_td);
        

        const folder_td = document.createElement("td");
        folder_td.align = "left";
        folder_td.appendChild(get_link(song.folder_id, song.folder_name));
        tr.appendChild(folder_td);

        element.appendChild(tr);
    }

}