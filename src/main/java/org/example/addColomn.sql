ALTER TABLE dish ADD COLUMN price NUMERIC;

update dish set price = 2000 where name ilike 'Salade fraîche';
update dish set price = 6000 where name ilike 'Poulet grillé';
