-- apply changes
CALL usp_ebean_drop_column('rcitems_consumeables', 'version');

CALL usp_ebean_drop_column('rcitems_consumeables', 'when_created');

CALL usp_ebean_drop_column('rcitems_consumeables', 'when_modified');

