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
    if (isEOL(peek())) {
      line++;
      column=0;
    }
    // read the initial character
    char ch = read();
    String tokenStr="";
    Token tkn;
    while (Character.isWhitespace(ch)) {
      ch = read();
    }
    if (isEOF(ch)) {
      tkn= new Token(TokenType.EOS, "end-of-stream", line, column);
      return tkn;
    }
    
    //single char and other comparators
    switch (ch) {
      case '.':
        tkn = new Token(TokenType.DOT, ".", line, column);
        return tkn;
      case ':':
        tkn = new Token(TokenType.COLON, ":", line, column);
        return tkn;
      case ',':
        tkn = new Token(TokenType.COMMA, ",", line, column);
        return tkn;
      case '(':
        tkn = new Token(TokenType.LPAREN, "(", line, column);
        return tkn;
      case ')':
        tkn = new Token(TokenType.RPAREN, ")", line, column);
        return tkn;
      case '[':
        tkn = new Token(TokenType.LBRACKET, "[", line, column);
        return tkn;
      case ']':
        tkn = new Token(TokenType.RBRACKET, "]", line, column);
        return tkn;
      case '{':
        tkn = new Token(TokenType.LBRACE, "{", line, column);
        return tkn;
      case '}':
        tkn = new Token(TokenType.RBRACE, "}", line, column);
        return tkn;
      case '+':
        tkn = new Token(TokenType.PLUS, "+", line, column);
        return tkn;
      case '-':
        tkn = new Token(TokenType.MINUS, "-", line, column);
        return tkn;
      case '*':
        tkn = new Token(TokenType.TIMES, "*", line, column); 
        return tkn;
      case '/':
        tkn = new Token(TokenType.DIVIDE, "/", line, column);
        return tkn;
      case '=':
        if (peek()=='=') {
          tkn = new Token(TokenType.EQUAL, "==", line, column);
          ch=read();
          return tkn;
        }
        tkn = new Token(TokenType.ASSIGN, "=", line, column);
        return tkn;
      case '<':
        if (peek()=='=') {
          ch=read();
          tkn = new Token(TokenType.LESS_EQ, "<=", line, column);
          return tkn;
        }
        tkn = new Token(TokenType.LESS, "<", line, column);
        return tkn;
      case '>':
        if (peek()=='=') {
          ch=read();
          tkn = new Token(TokenType.GREATER_EQ, ">=", line, column);
          return tkn;
        }
        tkn = new Token(TokenType.GREATER, ">", line, column);
        return tkn;
      case '!':
        if (peek()=='=') {
          ch=read();
          tkn = new Token(TokenType.NOT_EQUAL, "!=", line, column);
          return tkn;
        }
        tkn = new Token(TokenType.NOT, "!", line, column);
        return tkn;
      case '#':
        while (!isEOL(peek()) && !isEOF(peek())) {
          ch = read();
          tokenStr += ch;
        }
        if (isEOL(peek())) {
            //ch = read(); 
            line++; 
        }
        tkn = new Token(TokenType.COMMENT, tokenStr, line-1, column);
        if (isEOL(peek())) {
          column=0;
        }
        return tkn;
      default:
        break;
    }
    if (Character.isDigit(ch)) {
      boolean flt=false;
      while (Character.isDigit(peek())||peek()=='.') {
        tokenStr+=ch;
        if (ch=='.') {
          flt=true;
        }
        ch=read();
      }
      if (flt) {
        tkn= new Token(TokenType.DOUBLE_VAL, tokenStr, line, column);
        return tkn;
      }
      else{
        tkn= new Token(TokenType.INT_VAL, tokenStr, line, column);
        return tkn;
      }
    }
    if (ch=='"') {
      while (true) {
        tokenStr+=ch;
        if (peek()=='"') {
          tokenStr+=ch;
          break;
        }
        ch=read();
      }
      tkn= new Token(TokenType.STRING_VAL, tokenStr, line, column);
      return tkn;
    }
    if (Character.isLetter(ch)) {
      while (true) {
        if (isEOL(ch)) {
          line++;

        }
        tokenStr+=ch;
        switch (tokenStr) {
          case "true":
          case "false":
            tkn= new Token(TokenType.BOOL_VAL, tokenStr, line, column);
            return tkn;
          case "null":
            tkn = new Token(TokenType.NULL_VAL, tokenStr, line, column);
            return tkn;
          case "int":
            tkn= new Token(TokenType.INT_TYPE, tokenStr, line, column);
            return tkn;
          case "double":
            tkn= new Token(TokenType.DOUBLE_TYPE, tokenStr, line, column);
            return tkn;
          case "char":
            tkn= new Token(TokenType.CHAR_TYPE, tokenStr, line, column);
            return tkn;
            case "string":
            tkn = new Token(TokenType.STRING_TYPE, tokenStr, line, column);
            return tkn;
          case "bool":
              tkn = new Token(TokenType.BOOL_TYPE, tokenStr, line, column);
              return tkn;
          case "void":
              tkn = new Token(TokenType.VOID_TYPE, tokenStr, line, column);
              return tkn;
      
          // Reserved words
          case "struct":
              tkn = new Token(TokenType.STRUCT, tokenStr, line, column);
              return tkn;
          case "var":
              tkn = new Token(TokenType.VAR, tokenStr, line, column);
              return tkn;
          case "while":
              tkn = new Token(TokenType.WHILE, tokenStr, line, column);
              return tkn;
          case "for":
              tkn = new Token(TokenType.FOR, tokenStr, line, column);
              return tkn;
          case "from":
              tkn = new Token(TokenType.FROM, tokenStr, line, column);
              return tkn;
          case "to":
              tkn = new Token(TokenType.TO, tokenStr, line, column);
              return tkn;
          case "if":
              tkn = new Token(TokenType.IF, tokenStr, line, column);
              return tkn;
          case "else":
              tkn = new Token(TokenType.ELSE, tokenStr, line, column);
              return tkn;
          case "new":
              tkn = new Token(TokenType.NEW, tokenStr, line, column);
              return tkn;
          case "return":
              tkn = new Token(TokenType.RETURN, tokenStr, line, column);
              return tkn;
            
          default:
            if (peek()=='=') {
              tkn= new Token(TokenType.ID, tokenStr, line, column);
              return tkn;
            }
            break;
        }
        ch=read();
      }
    }
    return null;
  }

}
