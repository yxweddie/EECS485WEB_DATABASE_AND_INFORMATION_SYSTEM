use group126pa3;
create trigger album_add_update 
after insert on contain
for each row
update album set lastupdated=now() where albumid=new.albumid;

create trigger album_delete_update
after delete on contain
for each row
update album set lastupdated=now() where albumid=old.albumid;

create trigger album_update_trigger
before update on album 
for each row set new.lastupdated = now();

create trigger photo_update_trigger
after update on contain
for each row
update album set lastupdated=now() where albumid=new.albumid;