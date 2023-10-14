function group_by_artist() {
    fetch('/api/v1/grouped/artist').then(
        async (response) => {
            let data = response.json();
        }
    )
}