grammar C;

// Parser Rules

program
    : (declaration | functionDef)* EOF
    ;

declaration
    : typeSpecifier declaratorList ';'
    ;

declaratorList
    : declarator (',' declarator)*
    ;

declarator
    : Identifier ('[' IntLiteral ']')?
    | '*' Identifier
    | Identifier '(' parameterList? ')'
    ;

functionDef
    : typeSpecifier Identifier '(' parameterList? ')' compoundStatement
    ;

parameterList
    : parameter (',' parameter)*
    ;

parameter
    : typeSpecifier ('*')? Identifier
    ;

typeSpecifier
    : 'int'
    | 'char'
    | 'void'
    | 'long'
    | 'short'
    | 'unsigned'
    | 'float'
    | 'double'
    ;

compoundStatement
    : '{' blockItem* '}'
    ;

blockItem
    : declaration
    | statement
    ;

statement
    : compoundStatement
    | ifStatement
    | whileStatement
    | forStatement
    | returnStatement
    | breakStatement
    | continueStatement
    | expressionStatement
    ;

ifStatement
    : 'if' '(' expression ')' statement ('else' statement)?
    ;

whileStatement
    : 'while' '(' expression ')' statement
    ;

forStatement
    : 'for' '(' forInit? ';' expression? ';' expression? ')' statement
    ;

forInit
    : declaration
    | expressionList
    ;

returnStatement
    : 'return' expression? ';'
    ;

breakStatement
    : 'break' ';'
    ;

continueStatement
    : 'continue' ';'
    ;

expressionStatement
    : expressionList? ';'
    ;

expressionList
    : expression (',' expression)*
    ;

expression
    : assignmentExpression
    ;

assignmentExpression
    : conditionalExpression (assignOp assignmentExpression)?
    ;

assignOp
    : '=' | '+=' | '-=' | '*=' | '/=' | '%='
    ;

conditionalExpression
    : logicalOrExpression ('?' expression ':' conditionalExpression)?
    ;

logicalOrExpression
    : logicalAndExpression ('||' logicalAndExpression)*
    ;

logicalAndExpression
    : equalityExpression ('&&' equalityExpression)*
    ;

equalityExpression
    : relationalExpression (('==' | '!=') relationalExpression)*
    ;

relationalExpression
    : additiveExpression (('<' | '>' | '<=' | '>=') additiveExpression)*
    ;

additiveExpression
    : multiplicativeExpression (('+' | '-') multiplicativeExpression)*
    ;

multiplicativeExpression
    : unaryExpression (('*' | '/' | '%') unaryExpression)*
    ;

unaryExpression
    : postfixExpression
    | '++' unaryExpression
    | '--' unaryExpression
    | '-' unaryExpression
    | '!' unaryExpression
    | '~' unaryExpression
    | '&' unaryExpression
    | '*' unaryExpression
    | '(' typeSpecifier ('*')? ')' unaryExpression
    ;

postfixExpression
    : primaryExpression postfixOp*
    ;

postfixOp
    : '[' expression ']'
    | '(' argumentList? ')'
    | '.' Identifier
    | '->' Identifier
    | '++'
    | '--'
    ;

primaryExpression
    : Identifier
    | IntLiteral
    | CharLiteral
    | StringLiteral
    | '(' expression ')'
    ;

argumentList
    : expression (',' expression)*
    ;

// Lexer Rules

IntLiteral
    : [0-9]+
    | '0x' [0-9a-fA-F]+
    ;

CharLiteral
    : '\'' (~['\\] | '\\' .) '\''
    ;

StringLiteral
    : '"' (~["\\\r\n] | '\\' .)* '"'
    ;

Identifier
    : [a-zA-Z_] [a-zA-Z_0-9]*
    ;

Whitespace
    : [ \t\r\n]+ -> skip
    ;

BlockComment
    : '/*' .*? '*/' -> skip
    ;

LineComment
    : '//' ~[\r\n]* -> skip
    ;
