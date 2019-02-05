create table APP_USER (
  id char(36) primary key,
  created_dt datetime not null,
  username varchar(100) not null unique,
  password varchar(100),
  email varchar(200) not null unique,
  is_verif boolean not null,
  email_verif_cd varchar(36)
);

create table USER_ROLE (
  user_id char(36) not null,
  role char(36) not null
);
