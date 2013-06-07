# --- !Ups

-- trim column from db
update mpo_module set module_key = btrim(module_key) where module_key != btrim(module_key);

# --- !Downs

-- trim column from db
update mpo_module set module_key = btrim(module_key) where module_key != btrim(module_key);

