
/* A Bison parser, made by GNU Bison 2.4.1.  */

/* Skeleton interface for Bison's Yacc-like parsers in C
   
      Copyright (C) 1984, 1989, 1990, 2000, 2001, 2002, 2003, 2004, 2005, 2006
   Free Software Foundation, Inc.
   
   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

/* As a special exception, you may create a larger work that contains
   part or all of the Bison parser skeleton and distribute that work
   under terms of your choice, so long as that work isn't itself a
   parser generator using the skeleton or a modified version thereof
   as a parser skeleton.  Alternatively, if you modify or redistribute
   the parser skeleton itself, you may (at your option) remove this
   special exception, which will cause the skeleton and the resulting
   Bison output files to be licensed under the GNU General Public
   License without this special exception.
   
   This special exception was added by the Free Software Foundation in
   version 2.2 of Bison.  */


/* Tokens.  */
#ifndef YYTOKENTYPE
# define YYTOKENTYPE
   /* Put the tokens into the symbol table, so that GDB and other debuggers
      know about them.  */
   enum yytokentype {
     PROGRAM = 258,
     BEGIN_B = 259,
     END = 260,
     FUNCTION = 261,
     READ = 262,
     WRITE = 263,
     IF = 264,
     ELSIF = 265,
     ENDIF = 266,
     DO = 267,
     WHILE = 268,
     CONTINUE = 269,
     BREAK = 270,
     RETURN = 271,
     INT = 272,
     VOID = 273,
     STRING = 274,
     FLOAT = 275,
     TRUE = 276,
     FALSE = 277,
     EQ = 278,
     ADD = 279,
     SUB = 280,
     MUL = 281,
     DIV = 282,
     EQU = 283,
     NEQU = 284,
     LT = 285,
     GT = 286,
     LP = 287,
     RP = 288,
     SC = 289,
     CM = 290,
     LTEQ = 291,
     GTEQ = 292,
     IDENTIFIER = 293,
     STRINGLITERAL = 294,
     INTLITERAL = 295,
     FLOATLITERAL = 296
   };
#endif



#if ! defined YYSTYPE && ! defined YYSTYPE_IS_DECLARED
typedef union YYSTYPE
{

/* Line 1676 of yacc.c  */
#line 11 "src/sample.ypp"

	int Val_I;
	float Val_F;
	char* sym;



/* Line 1676 of yacc.c  */
#line 101 "sample.tab.hpp"
} YYSTYPE;
# define YYSTYPE_IS_TRIVIAL 1
# define yystype YYSTYPE /* obsolescent; will be withdrawn */
# define YYSTYPE_IS_DECLARED 1
#endif

extern YYSTYPE yylval;


