grammar schema;

schema
:
	db '['
	(
		table
	)+ ']'
;

db
:
	FORMAT # normal
	| FORMAT '*' # prefix
	| '*' FORMAT # suffix
;

table
:
	tableName '(' tableC ')'
;

tableName
:
	FORMAT
;

tableC
:
	fieldName
	(
		',' fieldName
	)* # field
	| '*' # all
;

fieldName
:
	FORMAT
	(
		':' flag
	)?
;

flag
:
	(
		'true'
		| 'false'
	)
;

FORMAT
:
	[a-zA-Z0-9_]+
;

WS
:
	[ \t\r\n]+ -> skip
;

COMMENT
:
	'/*' .*? '*/' -> skip
;
