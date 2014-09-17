select timestamp, clusters_serialized from clusters 
where timestamp > 1410877677 - 60000;

insert into clusters values (1410877640, "test");