-- apply changes
create table rcitems_crafting_recipes (
  id                            integer auto_increment not null,
  name                          varchar(255) not null,
  permission                    varchar(255),
  result                        varchar(255) not null,
  amount                        integer not null,
  shape                         varchar(255),
  type                          varchar(9) not null,
  constraint ck_rcitems_crafting_recipes_type check ( type in ('SHAPED','SHAPELESS','FURNACE','ANVIL')),
  constraint uq_rcitems_crafting_recipes_name unique (name),
  constraint pk_rcitems_crafting_recipes primary key (id)
);

create table rcitems_crafting_recipe_slots (
  id                            integer auto_increment not null,
  recipe_id                     integer not null,
  slot                          varchar(255) not null,
  item                          varchar(255) not null,
  amount                        integer not null,
  constraint pk_rcitems_crafting_recipe_slots primary key (id)
);

create table rcitems_armor (
  id                            integer auto_increment not null,
  equipment_id                  integer,
  armor_type                    varchar(7) not null,
  armor_value                   integer not null,
  constraint ck_rcitems_armor_armor_type check ( armor_type in ('CLOTH','LEATHER','MAIL','PLATE','SHIELD')),
  constraint uq_rcitems_armor_equipment_id unique (equipment_id),
  constraint pk_rcitems_armor primary key (id)
);

create table rcitems_equipment (
  id                            integer auto_increment not null,
  item_id                       integer not null,
  equipment_slot                varchar(11) not null,
  durability                    integer not null,
  constraint ck_rcitems_equipment_equipment_slot check ( equipment_slot in ('ONE_HANDED','SHIELD_HAND','TWO_HANDED','HEAD','CHEST','LEGS','FEET','HANDS','INVENTORY','UNDEFINED')),
  constraint uq_rcitems_equipment_item_id unique (item_id),
  constraint pk_rcitems_equipment primary key (id)
);

create table rcitems_items (
  id                            integer auto_increment not null,
  name                          varchar(255) not null,
  lore                          varchar(255),
  minecraft_item                varchar(255) not null,
  minecraft_data_value          integer not null,
  item_level                    integer not null,
  quality                       varchar(9) not null,
  max_stack_size                integer not null,
  sell_price                    double not null,
  bind_type                     varchar(5) not null,
  item_type                     varchar(11) not null,
  block_usage                   tinyint(1) default 0 not null,
  lootable                      tinyint(1) default 0 not null,
  enchantment_effect            tinyint(1) default 0 not null,
  info                          varchar(255),
  constraint ck_rcitems_items_quality check ( quality in ('POOR','COMMON','UNCOMMON','RARE','EPIC','LEGENDARY')),
  constraint ck_rcitems_items_bind_type check ( bind_type in ('BOE','BOP','QUEST','NONE')),
  constraint ck_rcitems_items_item_type check ( item_type in ('WEAPON','ARMOR','USEABLE','EQUIPMENT','QUEST','ENCHANTMENT','GEM','ENHANCEMENT','CRAFTING','CONSUMEABLE','TRASH','SPECIAL','PROFESSION','CLASS','UNDEFINED')),
  constraint uq_rcitems_items_name unique (name),
  constraint pk_rcitems_items primary key (id)
);

create table rcitems_item_categories (
  item_id                       integer not null,
  category_id                   integer not null,
  constraint pk_rcitems_item_categories primary key (item_id,category_id)
);

create table rcitems_attachments (
  id                            integer auto_increment not null,
  item_id                       integer not null,
  attachment_name               varchar(255) not null,
  provider_name                 varchar(255) not null,
  description                   varchar(255),
  color                         varchar(255),
  constraint pk_rcitems_attachments primary key (id)
);

create table rcitems_weapons (
  id                            integer auto_increment not null,
  equipment_id                  integer not null,
  weapon_type                   varchar(14) not null,
  min_damage                    integer not null,
  max_damage                    integer not null,
  swing_time                    double not null,
  constraint ck_rcitems_weapons_weapon_type check ( weapon_type in ('SWORD','TWO_HAND_SWORD','DAGGER','AXE','TWO_HAND_AXE','POLEARM','MACE','TWO_HAND_MACE','STAFF','BOW','MAGIC_WAND')),
  constraint uq_rcitems_weapons_equipment_id unique (equipment_id),
  constraint pk_rcitems_weapons primary key (id)
);

create table rcitems_equipment_attributes (
  id                            integer auto_increment not null,
  equipment_id                  integer not null,
  attribute                     varchar(29) not null,
  attribute_value               integer not null,
  constraint ck_rcitems_equipment_attributes_attribute check ( attribute in ('STRENGTH','AGILITY','STAMINA','INTELLECT','SPIRIT','CRITICAL_STRIKE_RATING','HIT_RATING','MAGICAL_HIT','ATTACK_POWER','SPELL_POWER','DODGE_RATING','PARRY_RATING','SHIELD_BLOCK_RATING','DEFENSE_RATING','HEAL','HASTE_RATING','ARMOR_PENETRATION','WEAPON_SKILL_RATING','RANGED_CRITICAL_STRIKE_RATING','EXPERTISE_RATING_2','EXPERTISE_RATING','RESILIENCE_RATING','RANGED_ATTACK_POWER','MANA_REGENERATION','ARMOR_PENETRATION_RATING','HEALTH_REGEN','SPELL_PENETRATION','BLOCK_VALUE','MASTERY_RATING','FIRE_RESISTANCE','FROST_RESISTANCE','HOLY_RESISTANCE','SHADOW_RESISTANCE','NATURE_RESISTANCE','ARCANE_RESISTANCE')),
  constraint pk_rcitems_equipment_attributes primary key (id)
);

create table rcitems_attachment_data (
  id                            integer auto_increment not null,
  attachment_id                 integer,
  data_key                      varchar(255),
  data_value                    varchar(255),
  constraint pk_rcitems_attachment_data primary key (id)
);

create table rcitems_categories (
  id                            integer auto_increment not null,
  name                          varchar(255),
  description                   varchar(255),
  constraint pk_rcitems_categories primary key (id)
);

create index ix_rcitems_crafting_recipe_slots_recipe_id on rcitems_crafting_recipe_slots (recipe_id);
alter table rcitems_crafting_recipe_slots add constraint fk_rcitems_crafting_recipe_slots_recipe_id foreign key (recipe_id) references rcitems_crafting_recipes (id) on delete restrict on update restrict;

alter table rcitems_armor add constraint fk_rcitems_armor_equipment_id foreign key (equipment_id) references rcitems_equipment (id) on delete restrict on update restrict;

alter table rcitems_equipment add constraint fk_rcitems_equipment_item_id foreign key (item_id) references rcitems_items (id) on delete restrict on update restrict;

create index ix_rcitems_item_categories_rcitems_items on rcitems_item_categories (item_id);
alter table rcitems_item_categories add constraint fk_rcitems_item_categories_rcitems_items foreign key (item_id) references rcitems_items (id) on delete restrict on update restrict;

create index ix_rcitems_item_categories_rcitems_categories on rcitems_item_categories (category_id);
alter table rcitems_item_categories add constraint fk_rcitems_item_categories_rcitems_categories foreign key (category_id) references rcitems_categories (id) on delete restrict on update restrict;

create index ix_rcitems_attachments_item_id on rcitems_attachments (item_id);
alter table rcitems_attachments add constraint fk_rcitems_attachments_item_id foreign key (item_id) references rcitems_items (id) on delete restrict on update restrict;

alter table rcitems_weapons add constraint fk_rcitems_weapons_equipment_id foreign key (equipment_id) references rcitems_equipment (id) on delete restrict on update restrict;

create index ix_rcitems_equipment_attributes_equipment_id on rcitems_equipment_attributes (equipment_id);
alter table rcitems_equipment_attributes add constraint fk_rcitems_equipment_attributes_equipment_id foreign key (equipment_id) references rcitems_equipment (id) on delete restrict on update restrict;

create index ix_rcitems_attachment_data_attachment_id on rcitems_attachment_data (attachment_id);
alter table rcitems_attachment_data add constraint fk_rcitems_attachment_data_attachment_id foreign key (attachment_id) references rcitems_attachments (id) on delete restrict on update restrict;

