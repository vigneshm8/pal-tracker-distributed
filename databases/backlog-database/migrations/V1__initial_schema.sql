create table stories (
  id         bigint(20) not null auto_increment,
  project_id bigint(20),
  name       VARCHAR(255),

  primary key (id)
)
engine = innodb
default charset = utf8;
