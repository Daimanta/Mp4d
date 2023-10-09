function set_fileinfo(uuid) {
    fetch('/api/v1/song/'+uuid).then(
        async (response) => {
            const data = await response.json();
            document.getElementById("name_id").textContent = data.name;
            document.getElementById("size_id").textContent = data.size;
            document.getElementById("length_id").textContent = data.length;
            document.getElementById("bitrate_id").textContent = data.bitrate;
            document.getElementById("directory_id").href = "index.html?id="+data.folderId;
            document.getElementById("play_id").href = "/api/v1/songplaylist/"+data.uuid;
        }
    )
}