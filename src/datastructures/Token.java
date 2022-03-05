package datastructures;

import constants.Terminal;
import constants.TokenType;

/**
 * The Token class represents an individual lexeme and is capable of representing identifiers, keywords,
 * operators and reserved words. The token class is used by the lexer and called whenever the lexer
 * detects a new Token as each Token is separated by a space.
 */
public class Token {
	private final int column, line;
	private final String text;
	private final Terminal value;
	private final TokenType type;

	/**
	 * Initializes a Token
	 *
	 * @param type      the type of the token, such as INT or KEYWORD.
	 * @param value     specifies what keyword, operator, or data type the token represents as understood by the parser.
	 * @param text      the text of the token as it appears in the source code.
	 * @param line      the line number of the first character of the token, starting at 1.
	 * @param column    the column of the first character of the token, starting at 1.
	 */
	public Token(TokenType type, Terminal value, String text, int line, int column) {
		this.type = type;
		this.value = value;
		this.text = text;
		this.line = line;
		this.column = column;
	}

	public Token(Token that) {
		this.column = that.column;
		this.line = that.line;
		this.text = that.text;
		this.value = that.value;
		this.type = that.type;
	}

	public Token() {
		column = line = -1;
		value = null;
		type = null;
		text = null;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Token)) return false;
		final Token that = (Token) o;
		return this.column == that.column
				&& this.line == that.line
				&& this.value == that.value
				&& this.type == that.type
				&& this.text.equals(that.text);
	}

	@Override
	public String toString() {
		return type.toString() + "\n\t" + value.toString() + "\n\tLine " + line + ", Col " + column + "\n\t" + text;
	}

	public int getColumn() {
		return column;
	}
	public int getLine() {
		return line;
	}
	public String getText() {
		return text;
	}
	public Terminal getValue() {
		return value;
	}
	public TokenType getType() {
		return type;
	}
}