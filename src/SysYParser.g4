parser grammar SysYParser;


options {
    tokenVocab = SysYLexer;//注意使用该语句指定词法分析器；请不要修改词法分析器或语法分析器的文件名，否则Makefile可能无法正常工作，影响评测结果
}

program
   : compUnit
   ;


compUnit
   : (funcDef | decl)+ EOF
   ;
// 下面是其他的语法单元定义

decl
    : constdecl | vardecl;


constdecl
    : 'const' btype constdef (',' constdef)* ';';

btype : 'int';

constdef : IDENT ('[' constExp ']')* '=' constInitVal;

constInitVal :
                constExp
                | '{' (constInitVal (',' constInitVal)*)? '}' ;


vardecl : btype vardef (',' vardef)* ';';

vardef : IDENT ('[' constExp ']')*
        | IDENT ('[' constExp ']')* '=' initVal;


initVal : exp
        | '{' (initVal (',' initVal)*)? '}';

funcDef : functype IDENT '(' (funcFParams)? ')' block;

functype : 'void' | 'int';

funcFParams : funcFParam (',' funcFParam);

funcFParam : btype IDENT ('[' ']' ('[' exp ']')*)?;

block : '{' (blockItem)* '}';

blockItem : decl | stmt;

stmt : lVal '=' exp ';'
        | (exp)? ';'
        | block
        | 'if' '(' cond ')' stmt ('else' stmt)?
        | 'while' '(' cond ')' stmt
        | 'break' ';'
        | 'continue' ';'
        | 'return' (exp)? ';';

exp
   : L_PAREN exp R_PAREN
   | lVal
   | number
   | IDENT L_PAREN funcRParams? R_PAREN
   | unaryOp exp
   | exp (MUL | DIV | MOD) exp
   | exp (PLUS | MINUS) exp
   ;




cond
   : exp
   | cond (LT | GT | LE | GE) cond
   | cond (EQ | NEQ) cond
   | cond AND cond
   | cond OR cond
   ;

lVal
   : IDENT (L_BRACKT exp R_BRACKT)*
   ;

number
   : INTEGER_CONST
   ;

unaryOp
   : PLUS
   | MINUS
   | NOT
   ;

funcRParams
   : param (COMMA param)*
   ;

param
   : exp
   ;

constExp
   : exp
   ;
