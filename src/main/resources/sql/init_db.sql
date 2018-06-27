create table rc_visit (user_id integer, page_id integer, doc_date timestamp);
create index rc_visit_doc_date_user_id on rc_visit (doc_date, user_id);
create index rc_visit_user_id_page_id on rc_visit (user_id, page_id);
create index rc_visit_page_id_user_id on rc_visit (page_id, user_id);
