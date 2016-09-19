%{
	#include"sample.tab.hpp"
%}

%option noyywrap

%%
"\-\-".+$	{ }
PROGRAM		{yylval.sym = yytext; return PROGRAM; }
BEGIN		{yylval.sym = yytext; return BEGIN_B; }
END		{yylval.sym = yytext; return END; }
FUNCTION	{yylval.sym = yytext; return FUNCTION; }
READ		{yylval.sym = yytext; return READ; }
WRITE		{yylval.sym = yytext; return WRITE; }
IF		{yylval.sym = yytext; return IF; }
ELSIF		{yylval.sym = yytext; return ELSIF; }
ENDIF 		{yylval.sym = yytext; return ENDIF; }
DO 		{yylval.sym = yytext; return DO; }
WHILE		{yylval.sym = yytext; return WHILE; }
CONTINUE	{yylval.sym = yytext; return CONTINUE; }
BREAK		{yylval.sym = yytext; return BREAK; }
RETURN		{yylval.sym = yytext; return RETURN; }
INT		{yylval.sym = yytext; return INT; }
VOID		{yylval.sym = yytext; return VOID; }
STRING		{yylval.sym = yytext; return STRING; }
FLOAT		{yylval.sym = yytext; return FLOAT; }
TRUE		{yylval.sym = yytext; return TRUE; }
FALSE		{yylval.sym = yytext; return FALSE; }

":=" {yylval.sym = yytext; return EQ; }
"+" {yylval.sym = yytext; return ADD; }
"-" {yylval.sym = yytext; return SUB; }
"*" {yylval.sym = yytext; return MUL; }
"/" {yylval.sym = yytext; return DIV; }
"=" {yylval.sym = yytext; return EQU; }
"!=" {yylval.sym = yytext; return NEQU; }
"<" {yylval.sym = yytext; return LT; }
">" {yylval.sym = yytext; return GT; }
"(" {yylval.sym = yytext; return LP; }
")" {yylval.sym = yytext; return RP; }
";" {yylval.sym = yytext; return SC; }
"," {yylval.sym = yytext; return CM; }
"<=" {yylval.sym = yytext; return LTEQ; }
">=" {yylval.sym = yytext; return GTEQ; }

"\""([^"\""]{0,80})"\""	{yylval.sym = yytext; return STRINGLITERAL; }
[a-zA-Z]{1}[a-zA-Z0-9]{0,30}+	{yylval.sym = yytext; return IDENTIFIER; }
[0-9]+		{yylval.sym = yytext; return INTLITERAL; }
[0-9]{0,4}\.[0-9]{1,6}+	 {yylval.sym = yytext; return FLOATLITERAL; }
[ \t\n] { yylval.sym = yytext; }
. { printf("Unrecognized Token\n"); exit(1); }

%%
