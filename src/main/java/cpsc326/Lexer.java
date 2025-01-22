/**
 * CPSC 326, Spring 2025
 * MyPL Lexer Implementation.
 *
 * PUT YOUR NAME HERE IN PLACE OF THIS TEXT
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
    // read the initial character
    char ch = read();

    // TODO: Finish this method


    return null;
  }

}
