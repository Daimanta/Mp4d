CREATE TABLE folder (
    id integer not null primary key autoincrement,
    path varchar(255) not null,
    parent_id integer references folder(id)
);

CREATE INDEX folder_path_idx ON folder(path);
CREATE INDEX folder_parent_idx ON folder(parent_id);

CREATE TABLE song (
    uuid varchar(36) not null primary key,
    folder_id integer not null references folder(id),
    name varchar(255) not null
);

CREATE INDEX song_folder_idx ON song(folder_id);
CREATE INDEX song_name_idx ON song(name);

CREATE UNIQUE INDEX song_name_folder_idx ON song(folder_id, name);
