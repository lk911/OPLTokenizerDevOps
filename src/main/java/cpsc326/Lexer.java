/**
 * CPSC 326, Spring 2025
 * MyPL Lexer Implementation.
 *
 * Liam Kordsmeier
 */

 package cpsc326;

 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.IOException;
 
 
 /**
  * The Lexer class takes an input stream containing mypl source code
  * and transforms (tokenizes) it into a stream of tokens.
  */
 public class Lexer {
 
   private BufferedReader buffer;
   private int line = 1;        
   private int column = 0;      
 
   /**
    * Creates a new Lexer object out of an input stream.
    */
   public Lexer(InputStream input) {
     buffer = new BufferedReader(new InputStreamReader(input));
   }
 
   /**
    * Helper function to read a single character from the input stream.
    * @return A single character
    */
   private char read() {
     try {
       ++column;
       return (char)buffer.read();
     } catch(IOException e) {
       error("read error", line, column + 1);
     }
     return (char)-1;
   }
 
   /**
    * Helper function to look ahead one character in the input stream.
    * @return A single character
    */
   private char peek() {
     int ch = -1;
     try {
       buffer.mark(1);
       ch = (char)buffer.read();
       buffer.reset();
       return (char)ch;
     } catch(IOException e) {
       error("read error", line, column + 1);
     }
     return (char)-1;
   }
 
   /**
    * Helper function to check if the given character is an end of line
    * symbol.
    * @return True if the character is an end of line character and
    * false otherwise.
    */
   private boolean isEOL(char ch) {
     if (ch == '\n')
       return true;
     if (ch == '\r' && peek() == '\n') {
       read();
       return true;
     }
     else if (ch == '\r')
       return true;
     return false;
   }
  
   /**
    * Helper function to check if the given character is an end of file
    * symbol.
    * @return True if the character is an end of file character and
    * false otherwise.
    */
   private boolean isEOF(char ch) {
     return ch == (char)-1;
   }
  
   /**
    * Print an error message and exit the program.
    */
   private void error(String msg, int line, int column) {
     String s = "[%d,%d] %s";
     MyPLException.lexerError(String.format(s, line, column, msg));
   }
 
   /**
    * Obtains and returns the next token in the stream.
    * @return The next token in the stream.
    */
 
   public Token nextToken() {
     char ch = read();
 // skip whitespace
     while (Character.isWhitespace(ch)) { 
         if (isEOL(ch)) {
             line++;
             column = 0;
         }
         if (isEOF(ch)) {
             break;
         }
         ch = read();
     }
 
     if (isEOF(ch)) { // check for EOF
         return new Token(TokenType.EOS, "end-of-stream", line, column);
     }
 
     int tempLine = line;
     int tempColum = column;
     //2 char tokens
     if (ch == '=') { 
 
         if (peek() == '=') {
             read();
             return new Token(TokenType.EQUAL, "==", tempLine, tempColum);
         } else {
             return new Token(TokenType.ASSIGN, "=", tempLine, tempColum);
         }
     } else if (ch == '!') {
         if (peek() == '=') {
             read();
             return new Token(TokenType.NOT_EQUAL, "!=", tempLine, tempColum);
         } else {
             error("expecting !=", tempLine, tempColum);
             return new Token(TokenType.EOS, "end-of-stream", line, column); // Error
         }
     } else if (ch == '<') {
         if (peek() == '=') {
             read();
             return new Token(TokenType.LESS_EQ, "<=", tempLine, tempColum);
         } else {
             return new Token(TokenType.LESS, "<", tempLine, tempColum);
         }
     } else if (ch == '>') {
         if (peek() == '=') {
             read();
             return new Token(TokenType.GREATER_EQ, ">=", tempLine, tempColum);
         } else {
             return new Token(TokenType.GREATER, ">", tempLine, tempColum);
         }
     }
 
     // single-character tokens
     switch (ch) {
         case '+':
             return new Token(TokenType.PLUS, "+", tempLine, tempColum);
         case '-':
             return new Token(TokenType.MINUS, "-", tempLine, tempColum);
         case '*':
             return new Token(TokenType.TIMES, "*", tempLine, tempColum);
         case '/':
             return new Token(TokenType.DIVIDE, "/", tempLine, tempColum);
         case '.':
             return new Token(TokenType.DOT, ".", tempLine, tempColum);
         case ':':
             return new Token(TokenType.COLON, ":", tempLine, tempColum);
         case ',':
             return new Token(TokenType.COMMA, ",", tempLine, tempColum);
         case '(':
             return new Token(TokenType.LPAREN, "(", tempLine, tempColum);
         case ')':
             return new Token(TokenType.RPAREN, ")", tempLine, tempColum);
         case '[':
             return new Token(TokenType.LBRACKET, "[", tempLine, tempColum);
         case ']':
             return new Token(TokenType.RBRACKET, "]", tempLine, tempColum);
         case '{':
             return new Token(TokenType.LBRACE, "{", tempLine, tempColum);
         case '}':
             return new Token(TokenType.RBRACE, "}", tempLine, tempColum);
     }
 
     if (ch == '#') { // check for comments
 
         StringBuilder lexeme = new StringBuilder();
         while (true) {
             char next = peek();
             if (isEOL(next) || isEOF(next)) {
                 break;
             }
             lexeme.append(read());
         }
         return new Token(TokenType.COMMENT, lexeme.toString(), tempLine, tempColum);
     }
 
     if (ch == '"') { // check for strings
         StringBuilder lexeme = new StringBuilder();
         boolean escape = false;
         while (true) {
             char next = read();
             if (isEOF(next)) {
                 error("non-terminated string", tempLine, tempColum);
                 return new Token(TokenType.EOS, "end-of-stream", line, column); // Error
             }
             if (escape) {
                 switch (next) {
                     case 'n':
                         lexeme.append('\n');
                         break;
                     case 't':
                         lexeme.append('\t');
                         break;
                     case 'r':
                         lexeme.append('\r');
                         break;
                     case '"':
                         lexeme.append('"');
                         break;
                     case '\\':
                         lexeme.append('\\');
                         break;
                     default:
                         error("Invalid escape sequence: \\" + next, tempLine, tempColum);
                         return new Token(TokenType.EOS, "end-of-stream", line, column); // Error
                 }
                 escape = false;
             } else {
                 if (next == '\\') {
                     escape = true;
                 } else if (next == '"') {
                     break;
                 } else if (isEOL(next)) {
                     error("non-terminated string", line, column);
                     return new Token(TokenType.EOS, "end-of-stream", line, column); // Error
                 } else {
                     lexeme.append(next);
                 }
             }
         }
         return new Token(TokenType.STRING_VAL, lexeme.toString(), tempLine, tempColum);
     }
 
     if (Character.isDigit(ch)) { // check for numbers
         StringBuilder lexeme = new StringBuilder();
         lexeme.append(ch);
         boolean isDouble = false;
 
         while (Character.isDigit(peek())) {
             lexeme.append(read());
         }
 
         // leading zeros
         if (lexeme.length() > 1 && lexeme.charAt(0) == '0' && !isDouble && peek() != '.') {
             error("leading zero in number", tempLine, tempColum);
             return new Token(TokenType.EOS, "end-of-stream", line, column); // Error
         }
 
         if (peek() == '.') {
             lexeme.append(read());
             isDouble = true;
 
             if (!Character.isDigit(peek())) {
                 error("missing digit after decimal", tempLine, column + 1);
                 return new Token(TokenType.EOS, "end-of-stream", line, column);
             }
 
             while (Character.isDigit(peek())) {
                 lexeme.append(read());
             }
         }
 
         if (isDouble) {
             return new Token(TokenType.DOUBLE_VAL, lexeme.toString(), tempLine, tempColum);
         } else {
             return new Token(TokenType.INT_VAL, lexeme.toString(), tempLine, tempColum);
         }
     }
 
     if (Character.isLetter(ch)) { // identifiers and reserved words
 
         StringBuilder lexeme = new StringBuilder();
         lexeme.append(ch);
 
         // next characters can be letters, numbers, or underscore
         while (Character.isLetterOrDigit(peek()) || peek() == '_') {
             lexeme.append(read());
         }
 
         String lexemeStr = lexeme.toString();
         TokenType type = TokenType.ID;
 
         switch (lexemeStr) {
             case "and":
                 type = TokenType.AND;
                 break;
             case "or":
                 type = TokenType.OR;
                 break;
             case "not":
                 type = TokenType.NOT;
                 break;
             case "struct":
                 type = TokenType.STRUCT;
                 break;
             case "var":
                 type = TokenType.VAR;
                 break;
             case "if":
                 type = TokenType.IF;
                 break;
             case "else":
                 type = TokenType.ELSE;
                 break;
             case "while":
                 type = TokenType.WHILE;
                 break;
             case "for":
                 type = TokenType.FOR;
                 break;
             case "from":
                 type = TokenType.FROM;
                 break;
             case "to":
                 type = TokenType.TO;
                 break;
             case "new":
                 type = TokenType.NEW;
                 break;
             case "true":
                 type = TokenType.BOOL_VAL;
                 break;
             case "false":
                 type = TokenType.BOOL_VAL;
                 break;
             case "null":
                 type = TokenType.NULL_VAL;
                 break;
             case "void":
                 type = TokenType.VOID_TYPE;
                 break;
             case "int":
                 type = TokenType.INT_TYPE;
                 break;
             case "double":
                 type = TokenType.DOUBLE_TYPE;
                 break;
             case "bool":
                 type = TokenType.BOOL_TYPE;
                 break;
             case "string":
                 type = TokenType.STRING_TYPE;
                 break;
             case "return":
                 type = TokenType.RETURN;
                 break;
             default:
                 type = TokenType.ID;
         }
         return new Token(type, lexemeStr, tempLine, tempColum);
     }
 
     error("unrecognized symbol '" + ch + "'", tempLine, tempColum);
     return new Token(TokenType.EOS, "end-of-stream", line, column); // Error
   }
 }