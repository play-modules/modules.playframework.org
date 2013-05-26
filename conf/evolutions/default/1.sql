# --- !Ups

create table binary_content (
  id                        bigint not null,
  content                   bytea not null,
  content_length            integer not null,
  constraint pk_binary_content primary key (id))
;

create table category (
  id                        bigint not null,
  name                      varchar(255) not null,
  constraint uq_category_name unique (name),
  constraint pk_category primary key (id))
;

create table comment (
  id                        bigint not null,
  module_id                 bigint not null,
  author_id                 bigint not null,
  post_date                 timestamp not null,
  in_reply_to_id            bigint,
  comment_text              varchar(1000) not null,
  visible                   boolean not null,
  constraint pk_comment primary key (id))
;

create table external_account (
  id                        bigint not null,
  user_id                   bigint not null,
  external_id               varchar(255) not null,
  provider                  varchar(255) not null,
  first_name                varchar(255),
  last_name                 varchar(255),
  full_name                 varchar(255),
  email                     varchar(255),
  avatar_url                varchar(255),
  o_auth1info_id            bigint,
  o_auth2info_id            bigint,
  password_info_id          bigint,
  authentication_method     varchar(25),
  constraint pk_external_account primary key (id))
;

create table external_token (
  id                        bigint not null,
  uuid                      varchar(255),
  email                     varchar(255),
  creation_time             timestamp not null,
  expiration_time           timestamp not null,
  is_sign_up                boolean not null,
  constraint pk_external_token primary key (id))
;

create table featured_module (
  id                        bigint not null,
  play_module_id            bigint not null,
  description               varchar(1000),
  creation_date             timestamp not null,
  sticky                    boolean not null,
  constraint pk_featured_module primary key (id))
;

create table historical_event (
  id                        bigint not null,
  creation_date             timestamp not null,
  category                  varchar(255) not null,
  message                   varchar(1000) not null,
  module_key                varchar(255) not null,
  constraint pk_historical_event primary key (id))
;

create table mpooauth1info (
  id                        bigint not null,
  token                     varchar(255),
  secret                    varchar(255),
  constraint pk_mpooauth1info primary key (id))
;

create table mpooauth2info (
  id                        bigint not null,
  access_token              varchar(255),
  token_type                varchar(255),
  expires_in                integer,
  refresh_token             varchar(255),
  constraint pk_mpooauth2info primary key (id))
;

create table mpopassword_info(
  id                        bigint not null,
  password                  varchar(255),
  salt                      varchar(255),
  constraint pk_mpopasswordinfo primary key (id))
;

create table MPO_MODULE (
  id                        bigint not null,
  owner_id                  bigint not null,
  module_key                varchar(255) not null,
  name                      varchar(255) not null,
  summary                   varchar(500) not null,
  description               varchar(4000) not null,
  organisation              varchar(255) not null,
  category_id               bigint,
  project_url               varchar(2500),
  demo_url                  varchar(2500),
  avatar_url                varchar(2500),
  license_type              varchar(255) not null,
  contributors              varchar(255),
  license_url               varchar(255),
  download_count            bigint not null,
  up_vote_count             bigint not null,
  down_vote_count           bigint not null,
  created_on                timestamp not null,
  updated_on                timestamp not null,
  rating_id                 bigint,
  constraint uq_MPO_MODULE_module_key unique (module_key),
  constraint pk_MPO_MODULE primary key (id))
;

create table module_version (
  id                        bigint not null,
  play_module_id            bigint,
  version_code              varchar(255) not null,
  release_notes             varchar(255) not null,
  release_date              timestamp not null,
  binary_file_id            bigint not null,
  source_file_id            bigint,
  document_file_id          bigint,
  constraint pk_module_version primary key (id))
;

create table play_version (
  id                        bigint not null,
  name                      varchar(255) not null,
  major_version             varchar(3) not null,
  documentation_url         varchar(255) not null,
  constraint ck_play_version_major_version check (major_version in ('ONE','TWO')),
  constraint uq_play_version_name unique (name),
  constraint pk_play_version primary key (id))
;

create table rate (
  id                        bigint not null,
  user_id                   bigint not null,
  play_module_id            bigint not null,
  rating                    integer not null,
  last_change_date          timestamp not null,
  constraint pk_rate primary key (id))
;

create table rating (
  id                        bigint not null,
  one_star                  integer not null,
  two_stars                 integer not null,
  three_stars               integer not null,
  four_stars                integer not null,
  five_stars                integer not null,
  average_rating            float not null,
  constraint pk_rating primary key (id))
;

create table tag (
  id                        bigint not null,
  name                      varchar(255) not null,
  constraint pk_tag primary key (id))
;

create table MPO_USER (
  id                        bigint not null,
  user_name                 varchar(40) not null,
  display_name              varchar(100) not null,
  avatar_url                varchar(2500),
  account_active            boolean not null,
  constraint uq_MPO_USER_user_name unique (user_name),
  constraint pk_MPO_USER primary key (id))
;

create table user_role (
  id                        bigint not null,
  role_name                 varchar(255) not null,
  description               varchar(1000) not null,
  constraint uq_user_role_role_name unique (role_name),
  constraint pk_user_role primary key (id))
;

create table vote (
  id                        bigint not null,
  user_id                   bigint not null,
  play_module_id            bigint not null,
  vote_type                 varchar(4) not null,
  last_change_date          timestamp not null,
  constraint ck_vote_vote_type check (vote_type in ('UP','DOWN')),
  constraint pk_vote primary key (id))
;


create table MPO_MODULE_tag (
  MPO_MODULE_id                  bigint not null,
  tag_id                         bigint not null,
  constraint pk_MPO_MODULE_tag primary key (MPO_MODULE_id, tag_id))
;

create table module_version_play_version (
  module_version_id              bigint not null,
  play_version_id                bigint not null,
  constraint pk_module_version_play_version primary key (module_version_id, play_version_id))
;

create table MPO_USER_user_role (
  MPO_USER_id                    bigint not null,
  user_role_id                   bigint not null,
  constraint pk_MPO_USER_user_role primary key (MPO_USER_id, user_role_id))
;
create sequence binary_content_seq;

create sequence category_seq;

create sequence comment_seq;

create sequence external_account_seq;

create sequence external_token_seq;

create sequence featured_module_seq;

create sequence historical_event_seq;

create sequence mpooauth1info_seq;

create sequence mpooauth2info_seq;

create sequence mpopassword_info_seq;

create sequence MPO_MODULE_seq;

create sequence module_version_seq;

create sequence play_version_seq;

create sequence rate_seq;

create sequence rating_seq;

create sequence tag_seq;

create sequence MPO_USER_seq;

create sequence user_role_seq;

create sequence vote_seq;

alter table comment add constraint fk_comment_MPO_MODULE_1 foreign key (module_id) references MPO_MODULE (id) on delete restrict on update restrict;
create index ix_comment_MPO_MODULE_1 on comment (module_id);
alter table comment add constraint fk_comment_author_2 foreign key (author_id) references MPO_USER (id) on delete restrict on update restrict;
create index ix_comment_author_2 on comment (author_id);
alter table comment add constraint fk_comment_inReplyTo_3 foreign key (in_reply_to_id) references comment (id) on delete restrict on update restrict;
create index ix_comment_inReplyTo_3 on comment (in_reply_to_id);
alter table external_account add constraint fk_external_account_user_4 foreign key (user_id) references MPO_USER (id) on delete restrict on update restrict;
create index ix_external_account_user_4 on external_account (user_id);
alter table external_account add constraint fk_external_account_oAuth1Info_5 foreign key (o_auth1info_id) references mpooauth1info (id) on delete restrict on update restrict;
create index ix_external_account_oAuth1Info_5 on external_account (o_auth1info_id);
alter table external_account add constraint fk_external_account_oAuth2Info_6 foreign key (o_auth2info_id) references mpooauth2info (id) on delete restrict on update restrict;
create index ix_external_account_oAuth2Info_6 on external_account (o_auth2info_id);
alter table featured_module add constraint fk_featured_module_playModule_7 foreign key (play_module_id) references MPO_MODULE (id) on delete restrict on update restrict;
create index ix_featured_module_playModule_7 on featured_module (play_module_id);
alter table MPO_MODULE add constraint fk_MPO_MODULE_owner_8 foreign key (owner_id) references MPO_USER (id) on delete restrict on update restrict;
create index ix_MPO_MODULE_owner_8 on MPO_MODULE (owner_id);
alter table MPO_MODULE add constraint fk_MPO_MODULE_category_9 foreign key (category_id) references category (id) on delete restrict on update restrict;
create index ix_MPO_MODULE_category_9 on MPO_MODULE (category_id);
alter table MPO_MODULE add constraint fk_MPO_MODULE_rating_10 foreign key (rating_id) references rating (id) on delete restrict on update restrict;
create index ix_MPO_MODULE_rating_10 on MPO_MODULE (rating_id);
alter table module_version add constraint fk_module_version_playModule_11 foreign key (play_module_id) references MPO_MODULE (id) on delete restrict on update restrict;
create index ix_module_version_playModule_11 on module_version (play_module_id);
alter table module_version add constraint fk_module_version_binaryFile_12 foreign key (binary_file_id) references binary_content (id) on delete restrict on update restrict;
create index ix_module_version_binaryFile_12 on module_version (binary_file_id);
alter table module_version add constraint fk_module_version_sourceFile_13 foreign key (source_file_id) references binary_content (id) on delete restrict on update restrict;
create index ix_module_version_sourceFile_13 on module_version (source_file_id);
alter table module_version add constraint fk_module_version_documentFil_14 foreign key (document_file_id) references binary_content (id) on delete restrict on update restrict;
create index ix_module_version_documentFil_14 on module_version (document_file_id);
alter table rate add constraint fk_rate_MPO_USER_15 foreign key (user_id) references MPO_USER (id) on delete restrict on update restrict;
create index ix_rate_MPO_USER_15 on rate (user_id);
alter table rate add constraint fk_rate_playModule_16 foreign key (play_module_id) references MPO_MODULE (id) on delete restrict on update restrict;
create index ix_rate_playModule_16 on rate (play_module_id);
alter table vote add constraint fk_vote_MPO_USER_17 foreign key (user_id) references MPO_USER (id) on delete restrict on update restrict;
create index ix_vote_MPO_USER_17 on vote (user_id);
alter table vote add constraint fk_vote_playModule_18 foreign key (play_module_id) references MPO_MODULE (id) on delete restrict on update restrict;
create index ix_vote_playModule_18 on vote (play_module_id);



alter table MPO_MODULE_tag add constraint fk_MPO_MODULE_tag_MPO_MODULE_01 foreign key (MPO_MODULE_id) references MPO_MODULE (id) on delete restrict on update restrict;

alter table MPO_MODULE_tag add constraint fk_MPO_MODULE_tag_tag_02 foreign key (tag_id) references tag (id) on delete restrict on update restrict;

alter table module_version_play_version add constraint fk_module_version_play_versio_01 foreign key (module_version_id) references module_version (id) on delete restrict on update restrict;

alter table module_version_play_version add constraint fk_module_version_play_versio_02 foreign key (play_version_id) references play_version (id) on delete restrict on update restrict;

alter table MPO_USER_user_role add constraint fk_MPO_USER_user_role_MPO_USE_01 foreign key (MPO_USER_id) references MPO_USER (id) on delete restrict on update restrict;

alter table MPO_USER_user_role add constraint fk_MPO_USER_user_role_user_ro_02 foreign key (user_role_id) references user_role (id) on delete restrict on update restrict;

alter sequence binary_content_seq restart with 10000;

alter sequence category_seq restart with 10000;

alter sequence comment_seq restart with 10000;

alter sequence featured_module_seq restart with 10000;

alter sequence historical_event_seq restart with 10000;

alter sequence MPO_MODULE_seq restart with 10000;

alter sequence module_version_seq restart with 10000;

alter sequence play_version_seq restart with 10000;

alter sequence rate_seq restart with 10000;

alter sequence rating_seq restart with 10000;

alter sequence tag_seq restart with 10000;

alter sequence MPO_USER_seq restart with 10000;

alter sequence user_role_seq restart with 10000;

alter sequence vote_seq restart with 10000;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists binary_content;

drop table if exists category;

drop table if exists comment;

drop table if exists external_account;

drop table if exists external_account;

drop table if exists featured_module;

drop table if exists historical_event;

drop table if exists mpooauth1info;

drop table if exists mpooauth2info;

drop table if exists mpopassword_info

drop table if exists MPO_MODULE;

drop table if exists MPO_MODULE_tag;

drop table if exists module_version;

drop table if exists module_version_play_version;

drop table if exists play_version;

drop table if exists rate;

drop table if exists rating;

drop table if exists tag;

drop table if exists MPO_USER;

drop table if exists MPO_USER_user_role;

drop table if exists user_role;

drop table if exists vote;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists binary_content_seq;

drop sequence if exists category_seq;

drop sequence if exists comment_seq;

drop sequence if exists external_account_seq;

drop sequence if exists featured_module_seq;

drop sequence if exists historical_event_seq;

drop sequence if exists mpooauth1info_seq;

drop sequence if exists mpooauth2info_seq;

drop sequence if exists mpopassword_info_seq;

drop sequence if exists MPO_MODULE_seq;

drop sequence if exists module_version_seq;

drop sequence if exists play_version_seq;

drop sequence if exists rate_seq;

drop sequence if exists rating_seq;

drop sequence if exists tag_seq;

drop sequence if exists MPO_USER_seq;

drop sequence if exists user_role_seq;

drop sequence if exists vote_seq;
