const current = [];

function generate_random_list() {
    fetch('/api/v1/random').then(
        async (response) => {
            const data = await response.json();
            current.length = 0;
            current.push(...data);
            const table = document.getElementById("random_list");
            table.replaceChildren();
            for (let song of current) {
                const li = document.createElement("li");
                appendText(li, "[");
                li.appendChild(get_link("index.html?id="+song.folder, "+"));
                appendText(li, "] · ");
                li.appendChild(get_link('api/v1/songplaylist/'+song.uuid, song.name));
                table.appendChild(li);
            }
        }
    );
}

function setup_randompage_js() {
    document.getElementById("random_tryagain_id").addEventListener('click', () => {
        generate_random_list();
    });

    document.getElementById("play_random_id").addEventListener('click', () => {
        let ids = current.map((x) => x.uuid);
        fetch('/api/v1/songplaylist/', {
            method: 'POST',
            body: JSON.stringify(ids),
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            }
        }).then(
            res => res.blob()
        ).then(blob => download_with_name(blob, `multi.m3u8`));
        console.log(ids);
    });
}