%{
	#include "lex.yy.c"
	#include<stdio.h>
	#include<string.h>

	typedef struct
	{
		char* sym;
	} yylex_val;
	
	int yylex( yylex_val yylval; return 1;);
	int yyerror(const char *p) {  printf("Not Accepted\n"); }

%}

%token <sym> PROGRAM BEGIN_B END FUNCTION READ WRITE IF ELSIF ENDIF DO WHILE CONTINUE BREAK RETURN INT VOID STRING FLOAT TRUE FALSE EQ ADD SUB MUL DIV EQU NEQU LT GT LP RP SC CM LTEQ GTEQ SPACE IDENTIFIER STRINGLITERAL

%token <sym> INTLITERAL
%token <sym> FLOATLITERAL

%type <sym> program pgm_body decl string_decl str var_decl var_type any_type id_list id_tail param_decl_list param_decl param_decl_tail func_declarations func_decl func_body stmt_list stmt base_stmt assign_stmt assign_expr read_stmt write_stmt return_stmt factor_prefix addop mulop if_stmt else_part cond compop do_while_stmt expr_prefix factor expr_list_tail expr_list call_expr postfix_expr expr id primary


%% /* Grammar rules and actions follow */

/* Program */
program: PROGRAM id BEGIN_B pgm_body END;
id: IDENTIFIER;
pgm_body: decl func_declarations;
decl: | string_decl decl | var_decl decl;

/* Global String Declaration */
string_decl: STRING id EQ str;
str: STRINGLITERAL;

/* Variable Declarations */
var_decl: var_type id_list;
var_type: FLOAT|INT;
any_type: var_type | VOID;
id_list: id id_tail;
id_tail: | CM id id_tail;

/* Function Parameter List */
param_decl_list: | param_decl param_decl_tail;
param_decl: var_type id;
param_decl_tail: | CM param_decl param_decl_tail;

/* Function Declarations */
func_declarations: | func_decl func_declarations;
func_decl: FUNCTION any_type id LP param_decl_list RP BEGIN_B func_body END;
func_body: decl stmt_list;

/* Statement List */
stmt_list: | stmt stmt_list;
stmt: base_stmt | if_stmt | do_while_stmt;
base_stmt: assign_stmt | read_stmt | write_stmt | return_stmt;

/* Basic Statements */
assign_stmt: assign_expr;
assign_expr: id EQ expr;
read_stmt: READ LP id_list RP;
write_stmt: WRITE LP id_list RP;
return_stmt: RETURN expr;

/* Expressions */
expr: expr_prefix factor;
expr_prefix: | expr_prefix factor addop;
factor: factor_prefix postfix_expr;
factor_prefix: | factor_prefix postfix_expr mulop;
postfix_expr: primary | call_expr;
call_expr: id LP expr_list RP;
expr_list: | expr expr_list_tail;
expr_list_tail: | CM expr expr_list_tail;
primary: LP expr RP | id | INTLITERAL | FLOATLITERAL;
addop: ADD | SUB;
mulop: MUL | DIV;

/* Complex Statements and Condition */
if_stmt: IF LP cond RP decl stmt_list else_part ENDIF;
else_part: | ELSIF LP cond RP decl stmt_list else_part;
cond: expr compop expr | TRUE | FALSE;
compop: LT | GT | EQU | NEQU | LTEQ | GTEQ;

do_while_stmt: DO decl stmt_list WHILE LP cond RP; 

%%

int main()
{
	yyparse();
	return 0;
}
