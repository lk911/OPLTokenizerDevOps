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

  private BufferedReader buffer; // handle to the input stream
  private int line = 1;          // current line number
  private int column = 0;        // current column number

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
    // Skip whitespace
    char ch = read();
    while (Character.isWhitespace(ch)) {
        if (isEOL(ch)) {
            line++;
            column = 0;
        }
        ch = read();
    }

    // Handle EOF
    if (isEOF(ch)) {
        return new Token(TokenType.EOS, "end-of-stream", line, column);
    }

    // Handle single-character tokens and comparators
    switch (ch) {
        case '.':
            return new Token(TokenType.DOT, ".", line, column);
        case ':':
            return new Token(TokenType.COLON, ":", line, column);
        case ',':
            return new Token(TokenType.COMMA, ",", line, column);
        case '(':
            return new Token(TokenType.LPAREN, "(", line, column);
        case ')':
            return new Token(TokenType.RPAREN, ")", line, column);
        case '[':
            return new Token(TokenType.LBRACKET, "[", line, column);
        case ']':
            return new Token(TokenType.RBRACKET, "]", line, column);
        case '{':
            return new Token(TokenType.LBRACE, "{", line, column);
        case '}':
            return new Token(TokenType.RBRACE, "}", line, column);
        case '+':
            return new Token(TokenType.PLUS, "+", line, column);
        case '-':
            return new Token(TokenType.MINUS, "-", line, column);
        case '*':
            return new Token(TokenType.TIMES, "*", line, column);
        case '/':
            return new Token(TokenType.DIVIDE, "/", line, column);
        case '=':
            if (peek() == '=') {
                read();
                return new Token(TokenType.EQUAL, "==", line, column);
            }
            return new Token(TokenType.ASSIGN, "=", line, column);
        case '<':
            if (peek() == '=') {
                read();
                return new Token(TokenType.LESS_EQ, "<=", line, column);
            }
            return new Token(TokenType.LESS, "<", line, column);
        case '>':
            if (peek() == '=') {
                read();
                return new Token(TokenType.GREATER_EQ, ">=", line, column);
            }
            return new Token(TokenType.GREATER, ">", line, column);
        case '!':
            if (peek() == '=') {
                read();
                return new Token(TokenType.NOT_EQUAL, "!=", line, column);
            }
            return new Token(TokenType.NOT, "!", line, column);
        case '#':
            StringBuilder commentStr = new StringBuilder();
            while (!isEOL(peek()) && !isEOF(peek())) {
                ch = read();
                commentStr.append(ch);
            }
            if (isEOL(peek())) {
                read(); // Consume the EOL character
                line++;
                column = 0;
            }
            return new Token(TokenType.COMMENT, commentStr.toString(), line - 1, column - commentStr.length());
        default:
            break;
    }

    // Handle numbers
    if (Character.isDigit(ch)) {
        StringBuilder numStr = new StringBuilder();
        boolean isDouble = false;
        while (Character.isDigit(peek()) || peek() == '.') {
            numStr.append(ch);
            if (ch == '.') {
                if (isDouble) {
                    error("invalid number format", line, column);
                }
                isDouble = true;
            }
            ch = read();
        }
        numStr.append(ch);
        if (isDouble) {
            return new Token(TokenType.DOUBLE_VAL, numStr.toString(), line, column - numStr.length() + 1);
        } else {
            return new Token(TokenType.INT_VAL, numStr.toString(), line, column - numStr.length() + 1);
        }
    }

    // Handle strings
    if (ch == '"') {
        StringBuilder str = new StringBuilder();
        while (true) {
            ch = read();
            if (isEOL(ch) || isEOF(ch)) {
                error("non-terminated string", line, column);
            }
            if (ch == '"') {
                break;
            }
            str.append(ch);
        }
        return new Token(TokenType.STRING_VAL, str.toString(), line, column - str.length());
    }

    // Handle identifiers and reserved words
    if (Character.isLetter(ch)) {
        StringBuilder idStr = new StringBuilder();
        while (Character.isLetterOrDigit(peek()) || peek() == '_') {
            idStr.append(ch);
            ch = read();
        }
        idStr.append(ch);
        String id = idStr.toString();
        switch (id) {
            case "true":
            case "false":
                return new Token(TokenType.BOOL_VAL, id, line, column - id.length() + 1);
            case "null":
                return new Token(TokenType.NULL_VAL, id, line, column - id.length() + 1);
            case "int":
                return new Token(TokenType.INT_TYPE, id, line, column - id.length() + 1);
            case "double":
                return new Token(TokenType.DOUBLE_TYPE, id, line, column - id.length() + 1);
            case "string":
                return new Token(TokenType.STRING_TYPE, id, line, column - id.length() + 1);
            case "bool":
                return new Token(TokenType.BOOL_TYPE, id, line, column - id.length() + 1);
            case "void":
                return new Token(TokenType.VOID_TYPE, id, line, column - id.length() + 1);
            case "struct":
                return new Token(TokenType.STRUCT, id, line, column - id.length() + 1);
            case "var":
                return new Token(TokenType.VAR, id, line, column - id.length() + 1);
            case "while":
                return new Token(TokenType.WHILE, id, line, column - id.length() + 1);
            case "for":
                return new Token(TokenType.FOR, id, line, column - id.length() + 1);
            case "from":
                return new Token(TokenType.FROM, id, line, column - id.length() + 1);
            case "to":
                return new Token(TokenType.TO, id, line, column - id.length() + 1);
            case "if":
                return new Token(TokenType.IF, id, line, column - id.length() + 1);
            case "else":
                return new Token(TokenType.ELSE, id, line, column - id.length() + 1);
            case "new":
                return new Token(TokenType.NEW, id, line, column - id.length() + 1);
            case "return":
                return new Token(TokenType.RETURN, id, line, column - id.length() + 1);
            case "and":
                return new Token(TokenType.AND, id, line, column - id.length() + 1);
            case "or":
                return new Token(TokenType.OR, id, line, column - id.length() + 1);
            case "not":
                return new Token(TokenType.NOT, id, line, column - id.length() + 1);
            default:
                return new Token(TokenType.ID, id, line, column - id.length() + 1);
        }
    }

    // Handle unrecognized symbols
    error("unrecognized symbol '" + ch + "'", line, column);
    return null;
  }
}