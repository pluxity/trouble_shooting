INSERT INTO parent (name) VALUES ('부모1');
INSERT INTO child (name, parent_id) VALUES ('자식1', (SELECT id FROM parent WHERE name = '부모1')); 