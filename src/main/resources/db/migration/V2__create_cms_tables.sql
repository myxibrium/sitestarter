create table PAGE (
  id char(36) primary key,
  created_dt datetime not null,
  owner_id char(36) not null,
  name varchar(100) not null unique,
  title varchar(200) not null,
  content clob not null
);

create table MEDIA (
  id char(36) primary key,
  created_dt datetime not null,
  owner_id char(36) not null,
  filename varchar(200) not null,
  mimetype varchar(200) not null,
  content blob not null
);
