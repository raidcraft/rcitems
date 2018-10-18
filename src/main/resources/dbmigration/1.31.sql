-- apply changes
create table rcitems_consumeables (
  id                            bigint auto_increment not null,
  item_id                       integer not null,
  type                          varchar(9) not null,
  resource_name                 varchar(255),
  resource_gain                 double not null,
  percentage                    tinyint(1) default 0 not null,
  instant                       tinyint(1) default 0 not null,
  interval                      bigint not null,
  duration                      varchar(255),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_modified                 datetime(6) not null,
  constraint ck_rcitems_consumeables_type check ( type in ('HEALTH','RESOURCE','ATTRIBUTE')),
  constraint uq_rcitems_consumeables_item_id unique (item_id),
  constraint pk_rcitems_consumeables primary key (id)
);

alter table rcitems_consumeables add constraint fk_rcitems_consumeables_item_id foreign key (item_id) references rcitems_items (id) on delete restrict on update restrict;

