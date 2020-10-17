drop table if exists TB_ALUNOS;

create table TB_ALUNOS(
id long auto_increment not null primary key ,
nome varchar(100) not null,
nregistro varchar(11) not null)
