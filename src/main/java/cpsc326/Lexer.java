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
     if (ch == '\n') {
       return true;
     }
     if (ch == '\r' && peek() == '\n') {
       read();
       return true;
     } else if (ch == '\r') {
       return true;
     }
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
        char ch = readAndSkipWhitespace();
    
        if (isEOF(ch)) {
            return new Token(TokenType.EOS, "end-of-stream", line, column);
        }
    
        int tempLine = line;
        int tempColumn = column;
    
        Token twoCharToken = handleTwoCharToken(ch, tempLine, tempColumn);
        if (twoCharToken != null) {
            return twoCharToken;
        }
    
        Token singleCharToken = handleSingleCharToken(ch, tempLine, tempColumn);
        if (singleCharToken != null) {
            return singleCharToken;
        }
    
        if (ch == '#') {
            return handleComment(tempLine, tempColumn);
        }
    
        if (ch == '"') {
            return handleString(tempLine, tempColumn);
        }
    
        if (Character.isDigit(ch)) {
            return handleNumber(ch, tempLine, tempColumn);
        }
    
        if (Character.isLetter(ch)) {
            return handleIdentifierOrKeyword(ch, tempLine, tempColumn);
        }
    
        error("unrecognized symbol '" + ch + "'", tempLine, tempColumn);
        return new Token(TokenType.EOS, "end-of-stream", line, column); // Error
    }
    
    private char readAndSkipWhitespace() {
        char ch = read();
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
        return ch;
    }
    
    private Token handleTwoCharToken(char ch, int tempLine, int tempColumn) {
        if (ch == '=') {
            return handleTwoChar('=', TokenType.ASSIGN, TokenType.EQUAL, "==", tempLine, tempColumn);
        } else if (ch == '!') {
            if(peek() == '='){
                read();
                return new Token(TokenType.NOT_EQUAL, "!=", tempLine, tempColumn);
            } else {
                error("expecting !=", tempLine, tempColumn);
                return new Token(TokenType.EOS, "end-of-stream", line, column); //error
            }
        } else if (ch == '<') {
            return handleTwoChar('<', TokenType.LESS, TokenType.LESS_EQ, "<=", tempLine, tempColumn);
        } else if (ch == '>') {
            return handleTwoChar('>', TokenType.GREATER, TokenType.GREATER_EQ, ">=", tempLine, tempColumn);
        }
        return null;
    }
    
    private Token handleTwoChar(
        char firstChar,
        TokenType singleType,
        TokenType doubleType,
        String doubleLexeme,
        int tempLine,
        int tempColumn) {
        if (peek() == '=') {
            read(); // Consume the second character
            return new Token(doubleType, doubleLexeme, tempLine, tempColumn);
        } else {
            return new Token(singleType, String.valueOf(firstChar), tempLine, tempColumn);
        }
    }
    
    private Token handleSingleCharToken(char ch, int tempLine, int tempColumn) {
        switch (ch) {
            case '+': return new Token(TokenType.PLUS, "+", tempLine, tempColumn);
            case '-': return new Token(TokenType.MINUS, "-", tempLine, tempColumn);
            case '*': return new Token(TokenType.TIMES, "*", tempLine, tempColumn);
            case '/': return new Token(TokenType.DIVIDE, "/", tempLine, tempColumn);
            case '.': return new Token(TokenType.DOT, ".", tempLine, tempColumn);
            case ':': return new Token(TokenType.COLON, ":", tempLine, tempColumn);
            case ',': return new Token(TokenType.COMMA, ",", tempLine, tempColumn);
            case '(': return new Token(TokenType.LPAREN, "(", tempLine, tempColumn);
            case ')': return new Token(TokenType.RPAREN, ")", tempLine, tempColumn);
            case '[': return new Token(TokenType.LBRACKET, "[", tempLine, tempColumn);
            case ']': return new Token(TokenType.RBRACKET, "]", tempLine, tempColumn);
            case '{': return new Token(TokenType.LBRACE, "{", tempLine, tempColumn);
            case '}': return new Token(TokenType.RBRACE, "}", tempLine, tempColumn);
            default: return null;
        }
    }
    
    private Token handleComment(int tempLine, int tempColumn) {
        StringBuilder lexeme = new StringBuilder();
        while (true) {
            char next = peek();
            if (isEOL(next) || isEOF(next)) {
                break;
            }
            lexeme.append(read());
        }
        return new Token(TokenType.COMMENT, lexeme.toString(), tempLine, tempColumn);
    }
    private Token handleString(int tempLine, int tempColumn) {
        StringBuilder lexeme = new StringBuilder();
        boolean escape = false;
        while (true) {
            char next = read();
            if (isEOF(next)) {
                error("non-terminated string", tempLine, tempColumn);
                return new Token(TokenType.EOS, "end-of-stream", line, column); // Error
            }
            if (escape) {
                switch (next) {
                    case 'n': lexeme.append('\n'); break;
                    case 't': lexeme.append('\t'); break;
                    case 'r': lexeme.append('\r'); break;
                    case '"': lexeme.append('"'); break;
                    case '\\': lexeme.append('\\'); break;
                    default:
                        error("Invalid escape sequence: \\" + next, tempLine, tempColumn);
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
        return new Token(TokenType.STRING_VAL, lexeme.toString(), tempLine, tempColumn);
    }
    
    private Token handleNumber(char ch, int tempLine, int tempColumn) {
        StringBuilder lexeme = new StringBuilder();
        lexeme.append(ch);
        boolean isDouble = false;
    
        while (Character.isDigit(peek())) {
            lexeme.append(read());
        }
    
        // leading zeros
        if (lexeme.length() > 1 && lexeme.charAt(0) == '0' && !isDouble && peek() != '.') {
            error("leading zero in number", tempLine, tempColumn);
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
            return new Token(TokenType.DOUBLE_VAL, lexeme.toString(), tempLine, tempColumn);
        } else {
            return new Token(TokenType.INT_VAL, lexeme.toString(), tempLine, tempColumn);
        }
    }
    
    private Token handleIdentifierOrKeyword(char ch, int tempLine, int tempColumn) {
        StringBuilder lexeme = new StringBuilder();
        lexeme.append(ch);
    
        while (Character.isLetterOrDigit(peek()) || peek() == '_') {
            lexeme.append(read());
        }
    
        String lexemeStr = lexeme.toString();
        TokenType type = getKeywordTokenType(lexemeStr);
    
        return new Token(type, lexemeStr, tempLine, tempColumn);
    }
    
    private TokenType getKeywordTokenType(String lexeme) {
        switch (lexeme) {
            case "and":     return TokenType.AND;
            case "or":      return TokenType.OR;
            case "not":     return TokenType.NOT;
            case "struct":  return TokenType.STRUCT;
            case "var":     return TokenType.VAR;
            case "if":      return TokenType.IF;
            case "else":    return TokenType.ELSE;
            case "while":   return TokenType.WHILE;
            case "for":     return TokenType.FOR;
            case "from":    return TokenType.FROM;
            case "to":      return TokenType.TO;
            case "new":     return TokenType.NEW;
            case "true":    return TokenType.BOOL_VAL;
            case "false":   return TokenType.BOOL_VAL;
            case "null":    return TokenType.NULL_VAL;
            case "void":    return TokenType.VOID_TYPE;
            case "int":     return TokenType.INT_TYPE;
            case "double":  return TokenType.DOUBLE_TYPE;
            case "bool":    return TokenType.BOOL_TYPE;
            case "string":  return TokenType.STRING_TYPE;
            case "return": return TokenType.RETURN;
            default:        return TokenType.ID;
        }
    }
 }