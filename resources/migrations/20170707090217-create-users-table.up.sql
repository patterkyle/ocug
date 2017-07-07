create table if not exists users
(id serial primary key,
email text not null,
password text not null,
role text default 'student' not null,
active boolean default true);
