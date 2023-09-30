CREATE TABLE folder (
    id integer not null primary key autoincrement,
    path varchar(255) not null
);

CREATE TABLE song (
    uuid varchar(36) not null primary key,
    folder_id integer not null references folder(id),
    name varchar(255) not null
);

