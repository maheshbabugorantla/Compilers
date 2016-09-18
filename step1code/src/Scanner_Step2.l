%{
	#include <iostream>
	#include <string>	
	using namespace std;
%}

%%
"\-\-".+$	{yyval.sym = "";}
PROGRAM		{return PROGRAM;}
BEGIN		{return BEGIN;}
END		{return END;}
FUNCTION	{return FUNCTION;}
READ		{return READ;}
WRITE		{return WRITE;}
IF		{return IF;}
ELSIF		{return ELSIF;}
ENDIF 		{return ENDIF;}
DO 		{return DO;}
WHILE		{return WHILE;}
CONTINUE	{return CONTINUE;}
BREAK		{return BREAK;}
RETURN		{return RETURN;}
INT		{return INT;}
VOID		{return VOID;}
STRING		{return STRING;}
FLOAT		{return FLOAT;}
TRUE		{return TRUE;}
FALSE		{return FALSE;}

":=" {yyval.sym = ":=";}
"+" {yyval.sym = "+"; return addop; }
"-" {yyval.sym = "-"; return addop; }
"*" {yyval.sym = "*"; return mulop; }
"/" {yyval.sym = "/"; return mulop; }
"=" {yyval.sym = "="; return compop; }
"!=" {yyval.sym = "!="; return compop; }
"<" {yyval.sym = "<"; return compop; }
">" {yyval.sym = ">"; return compop; }
"(" {yyval.sym = "("; return compop; }
")" {yyval.sym = ")";}
";" {yyval.sym = ";";}
"," {yyval.sym = ",";}
"<=" {yyval.sym = "<="; return compop; }
">=" {yyval.sym = ">="; return compop; }

"\""([^"\""]{0,80})"\""	{yyval.sym = yytext; return str;}
[a-zA-Z]{1}[a-zA-Z0-9]{0,30}+	{yyval.sym = yytext; return id;}
[0-9]+		{yyval.val_F = atoi(yytext); return primary;}
[0-9]{0,4}\.[0-9]{1,6}+	 {yyval.val_I = atof(yytext); return primary;}
[ \t\n] { }
. { cerr << "Unrecognized Token" << endl; exit(1);}

%%
