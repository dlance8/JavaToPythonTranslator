package processes;
import constants.Terminal;
import constants.TextConstants;
import constants.TokenType;
import datastructures.Token;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
public final class Lexer extends MyProcess {
	private boolean lexing;
	private char currentChar;
	private int currentIndex, line, column;
	private ArrayList<Token> tokens;
	private String in;
	private StringBuilder currentString;

	public Lexer() {

	}
	public Lexer(Thread processManagementThread) {
		super(processManagementThread);
	}

	public ArrayList<Token> lexFromFile(String fileName) {
		step0(fileName);
		return lex(in);
	}

	public ArrayList<Token> lex(String input) {
		in = input;
		step1();
		step2();
		step3();
		return tokens;
	}

	/** Reads the input file as a string of byte values, converts each byte value into a char, and puts each char in a
	 * StringBuilder. */
	private void step0(String fileName) {
		StringBuilder newIn = new StringBuilder();
		try (
			FileReader fileReader = new FileReader(fileName);
		    BufferedReader reader = new BufferedReader(fileReader)
		) {
			int val;
			while ((val = reader.read()) != -1) {
				newIn.append((char) val);
			}
		} catch (FileNotFoundException e) {
			error("File " + fileName + " was not found");
		} catch (IOException e) {
			error("Unexpected IO Error occurred while reading " + fileName);
		}
		in = newIn.toString();
	}

	/** Finds all unicode escapes in the input and replaces each with a single char of the unicode value represented by
	 * the escape. Example: \u0040 --> @ */
	private void step1() {
		StringBuilder newIn = new StringBuilder(in.length());
		start();
		boolean backslash = false;
		while (lexing) {
			if (!backslash && earlyUnicodeEscape()) {
				// currentString is unicode escape backslash
				newIn.append((char) Integer.parseInt(currentString.substring(2), 16));
				currentString.setLength(0);
			} else {
				if (currentChar == '\\') {
					backslash = !backslash;
				}
				newIn.append(currentChar);
				advance();
			}
		}
		in = newIn.toString();
	}

	/** Finds all line breaks (as defined in the lexical grammar) appearing in the input and replaces each with a single
	 *  char of value U+000A. */
	private void step2() {
		StringBuilder newIn = new StringBuilder();
		start();
		while (lexing) {
			if (earlyLineTerminator()) {
				newIn.append('\n');
			} else {
				newIn.append(currentChar);
				advance();
			}
		}
		in = newIn.toString();
	}

	/** Executes the main lexing process. */
	private void step3() {
		start();
		input();
	}


	private boolean lexError(String message) {
		super.error("ERROR AT LINE " + line + " COLUMN " + column + "\n" + message);
		return false;
	}

	private void start() {
		line = column = 1;
		tokens = new ArrayList<>();
		currentIndex = -1;
		currentString = new StringBuilder();
		advance();
	}
	private void advance() {
		if (lexing = currentIndex < in.length() - 1) {
			column++;
			currentChar = in.charAt(++currentIndex);
		} else currentChar = 0;
	}
	private boolean acceptAppendAdvance(boolean accepted) {
		if (accepted &= lexing) {
			currentString.append(currentChar);
			advance();
		}
		return accepted;
	}
	private void newToken(TokenType type, Terminal value) {
		tokens.add(new Token(type, value, currentString.toString(), line, column));
	}

	private interface Acceptor {
		boolean accept();
	}
	private boolean accept(Acceptor acceptor) {
		final boolean wasLexing = lexing;
		final char oldCurrentChar = currentChar;
		final int oldCurrentIndex = currentIndex, oldCurrentStringLength = currentString.length(), oldLine = line, oldColumn = column;

		final boolean accepted = acceptor.accept();
		if (!accepted) {
			lexing = wasLexing;
			currentChar = oldCurrentChar;
			currentIndex = oldCurrentIndex;
			line = oldLine;
			column = oldColumn;
			currentString.setLength(oldCurrentStringLength);
		}
		return accepted;
	}
	private boolean accept(char value) {
		return acceptAppendAdvance(currentChar == value);
	}
	private void acceptRepeating(Acceptor acceptor) {
		while (true) if (!accept(acceptor)) break;
	}
	private void acceptRepeating(char value) {
		while (true) if (!accept(value)) break;
	}
	private boolean excepting(Acceptor acceptor, char... exceptions) {
		return accept(() -> {
			for (char exception : exceptions)
				if (accept(exception)) return false;
			return acceptor.accept();
		});
	}
	private boolean acceptAny(char... values) {
		for (char value : values)
			if (accept(value)) return true;
		return false;
	}
	private boolean acceptAnyInRange(char min, char max) {
		return acceptAppendAdvance(min <= currentChar && currentChar <= max);
	}
	private boolean acceptNonIdentifierTerminal(TokenType tokenType, HashMap<String, Terminal> hashMap, boolean allowSymbols) {
		final boolean wasLexing = lexing;
		final char oldCurrentChar = currentChar;
		final int oldCurrentIndex = currentIndex;
		final int oldLine = line, oldColumn = column;
		final StringBuilder oldCurrentString = currentString;

		boolean isLexingAtBest = lexing;
		char currentCharAtBest = currentChar;
		int currentIndexAtBest = currentIndex;

		int lineAtBest = line, columnAtBest = column;

		currentString = new StringBuilder();

		String best = null;
		for (String key : hashMap.keySet()) {
			if (best == null || best.length() < key.length()) {
				boolean accepted = true;
				for (int i = 0; i < key.length(); ++i) {
					if (!accept(key.charAt(i))) {
						accepted = false;
						break;
					}
				}
				if (accepted) {
					best = key;
					isLexingAtBest = lexing;
					currentCharAtBest = currentChar;
					currentIndexAtBest = currentIndex;
					lineAtBest = line;
					columnAtBest = column;
				}
			}
			lexing = wasLexing;
			currentChar = oldCurrentChar;
			currentIndex = oldCurrentIndex;
			line = oldLine;
			column = oldColumn;
			currentString.setLength(0);
		}

		final boolean accepted = best != null && (allowSymbols || !isLexingAtBest || !Character.isJavaIdentifierStart(currentCharAtBest));
		if (accepted) {
			lexing = isLexingAtBest;
			currentChar = currentCharAtBest;
			currentIndex = currentIndexAtBest;
			line = lineAtBest;
			column = columnAtBest;
			currentString = oldCurrentString.append(best);
			newToken(tokenType, hashMap.get(currentString.toString()));
		}
		return accepted;
	}

	private boolean earlyUnicodeEscape() {
		/* unicode escape = "\" , unicode marker , hex digit , hex digit , hex digit , hex digit ; */
		return accept(() -> {
			boolean accepted = accept('\\') && unicodeMarker();
			if (accepted) {
				accepted = hexDigit() && hexDigit() && hexDigit() && hexDigit();
				if (!accepted) {
					lexError("illegal unicode escape");
				}
			}
			return accepted;
		});
	}
	private boolean unicodeMarker() {
		/* unicode marker = "u" , { "u" } ; */
		final boolean accepted = accept('u');
		if (accepted) {
			acceptRepeating('u');
		}
		return accepted;
	}
	private boolean hexDigit() {
		/* hex digit = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
		 *           | "a" | "b" | "c" | "d" | "e" | "f"
		 *           | "A" | "B" | "C" | "D" | "E" | "F" ; */
		return acceptAnyInRange('0', '9') || acceptAnyInRange('a', 'f') || acceptAnyInRange('A', 'F');
	}
	private boolean rawInputCharacter() {
		/* raw input character = ? any Unicode character ? ; */
		return acceptAppendAdvance(true);
	}


	private boolean earlyLineTerminator() {
		/* line terminator = LF
		 *                 | CR
		 *                 | CR , LF ; */
		return accept(TextConstants.CR) | accept(TextConstants.LF);
	}
	private boolean lineTerminator() {
		final boolean accepted = accept('\n');
		if (accepted) {
			line++;
			column = 1;
		}
		return accepted;
	}
	private boolean inputCharacter() {
		/* input character = unicode input character - ( CR | LF ) ; */
		/* unicode input character = unicode escape | raw input character ; */
		return excepting(this::rawInputCharacter, TextConstants.CR, TextConstants.LF);
	}



	private void input() {
		/* input = { input element } , [ sub ] ;
		 * sub = SUB ; */
		acceptRepeating(this::inputElement);
		accept(TextConstants.SUB);
		if (lexing)
			lexError("unexpected symbol");
	}
	private boolean inputElement() {
		/* input element = white space | comment | token ; */
		currentString.setLength(0);
		return whiteSpace() || comment() || token();
	}
	private boolean token() {
		/* token = identifier | keyword | literal | separator | operator ; */

		/* literal = integer literal | floating point literal | boolean literal
		 *         | character literal | string literal | null literal ; */

		/* token = identifier
		 *       | keyword
		 *       | boolean literal
		 *       | null literal
		 *       | separator
		 *       | operator ; */

		return keyword() || booleanLiteral() || nullLiteral() || identifier() || floatingPointLiteral()
				|| integerLiteral() || characterLiteral() || stringLiteral() || separator() || operator();
	}



	private boolean whiteSpace() {
		/* white space = SP | HT | FF | line terminator ; */
		return accept(TextConstants.SP) || accept(TextConstants.HT) || accept(TextConstants.FF) || lineTerminator();
	}



	private boolean comment() {
		/* comment = traditional comment | end of line comment ;
		 * traditional comment = "/" , "*" , comment tail ;
		 * end of line comment = "/" , "/" , { input character } ;
		 *
		 * comment = "/" , ( ( "*" , comment tail ) | ( "/" , { input character } ) ; */

		return accept(() -> {
			boolean accepted = accept('/');
			if (accepted) {
				if (accept('*')) {
					accepted = commentTail();
				} else if (accept('/')) {
					// accepted = true; (redundant)
					acceptRepeating(this::inputCharacter);
				} else {
					accepted = false;
				}
			}
			return accepted;
		});
	}
	private boolean commentTail() {
		/* comment tail = "*" , comment tail star
		 *              | not star , comment tail ;
		 * not star = input character - "*"
		 *          | line terminator ;
		 *
		 * comment tail = "*" , comment tail star
		 *              | ( input character - "*" ) , comment tail
		 *              | line terminator , comment tail ; */


		/* unicode input character = unicode escape | raw input character ; */

		return accept('*') ? commentTailStar() : (lineTerminator() || rawInputCharacter()) && commentTail();
	}
	private boolean commentTailStar() {
		/* comment tail star = "/"
		 *                   | "*" , comment tail star
		 *                   | not star not slash , comment tail ;
		 * not star not slash = input character - ( "*" | "/" )
		 *                    | line terminator ;
		 *
		 * comment tail star = "/"
		 *                   | "*" , comment tail star
		 *                   | ( input character - ( "*" | "/" ) ) , comment tail
		 *                   | line terminator , comment tail ; */
		return accept('/') || (accept('*') ? commentTailStar() : (lineTerminator() || rawInputCharacter()) && commentTail());
	}



	private boolean identifier() {
		/* identifier = identifier chars - ( keyword | boolean literal | null literal ) ;
		 * identifier chars = java letter , { java letter or digit } ;
		 * java letter = ? any Unicode character for which Character.isJavaIdentifierStart(int) returns true ? ;
		 * java letter or digit = ? any Unicode character for which Character.isJavaIdentifierPart(int) returns true ? ; */
		final boolean accepted = acceptAppendAdvance(Character.isJavaIdentifierStart(currentChar));
		if (accepted) {
			acceptRepeating(() -> acceptAppendAdvance(Character.isJavaIdentifierPart(currentChar)));
			newToken(TokenType.IDENTIFIER, Terminal.IDENTIFIER);
		}
		return accepted;
	}

	private boolean keyword() {
		return acceptNonIdentifierTerminal(TokenType.KEYWORD, TextConstants.KEYWORDS, false);
	}
	private boolean booleanLiteral() {
		return acceptNonIdentifierTerminal(TokenType.LITERAL, TextConstants.BOOLEAN_LITERALS, false);
	}
	private boolean nullLiteral() {
		return acceptNonIdentifierTerminal(TokenType.LITERAL, TextConstants.NULL_LITERAL, false);
	}
	private boolean separator() {
		return acceptNonIdentifierTerminal(TokenType.SEPARATOR, TextConstants.SEPARATORS, true);
	}
	private boolean operator() {
		return acceptNonIdentifierTerminal(TokenType.OPERATOR, TextConstants.OPERATORS, true);
	}


	private boolean integerLiteral() {
		/* integer literal = decimal integer literal | hex integer literal
		 *                 | octal integer literal | binary integer literal ;
		 * decimal integer literal = decimal numeral , [ integer type suffix ] ;
		 * hex integer literal = hex numeral , [ integer type suffix ] ;
		 * octal integer literal = octal numeral , [ integer type suffix ] ;
		 * binary integer literal = binary numeral , [ integer type suffix ] ;
		 * integer type suffix = "l" | "L" ; */
		/* integer literal = decimal numeral , [ integer type suffix ]
		 *                 | hex numeral , [ integer type suffix ]
		 *                 | octal numeral , [ integer type suffix ]
		 *                 | binary numeral , [ integer type suffix ] ; */
		/* integer literal = ( decimal numeral | hex numeral | octal numeral | binary numeral ) , [ "l" | "L" ] ; */

		if ((hexNumeral() || octalNumeral() || binaryNumeral() || decimalNumeral())) {
			acceptAny('l', 'L');
			newToken(TokenType.LITERAL, Terminal.INTEGER_LITERAL);
			return true;
		} else {
			return false;
		}
	}

	private boolean decimalNumeral() {
		/* decimal numeral = "0"
		 *                 | non zero digit , [ digits ]
		 *                 | non zero digit , underscores , digits ;
		 * non zero digit = "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ; */
		return accept(() -> {
			if (accept('0')) {
				return true;
			} else if (acceptAnyInRange('1', '9')) {
				if (digits()) {
					return true;
				} else if (underscores()) {
					return digits();
				} else {
					return true;
				}
			} else {
				return false;
			}
		});
	}
	private boolean digits() {
		/* digits = digit
		 *        | digit , [ digits and underscores ] , digit ;
		 * digits and underscores = digit or underscore , { digit or underscore } ;
		 * digit or underscore = digit | "_" ;
		 *
		 * digit = "0" | non zero digit ;
		 * non zero digit = "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ; */


		/* digits = digit
		 *        | digit , digit
		 *        | digit , digit, { digit | "_" } , digit
		 *        | digit , "_" , { digit | "_" } , digit
		 * */

		return digitsOfAnyBase(() -> acceptAnyInRange('0', '9'));
	}
	private boolean underscores() {
		/* underscores = "_" , { "_" } ; */
		final boolean accepted = accept('_');
		if (accepted)
			acceptRepeating('_');
		return accepted;
	}

	private boolean hexNumeral() {
		/* hex numeral = "0" , "x" , hex digits
		 *             | "0" , "X" , hex digits ; */

		return accept(() -> accept('0') && acceptAny('x', 'X') && hexDigits());
	}
	private boolean hexDigits() {
		/* hex digits = hex digit
		 *            | hex digit , [ hex digits and underscores ] , hex digit ;
		 * hex digits and underscores = hex digit or underscore , { hex digit or underscore } ;
		 * hex digit or underscore = hex digit | "_" ; */

		/* hex digits = hex digit
		 *            | hex digit , hex digit
		 *            | hex digit , hex digit , { hex digit | "_" } , hex digit
		 *            | hex digit , "_" , { hex digit | "_" } , hex digit ; */

		return digitsOfAnyBase(this::hexDigit);
	}

	private boolean octalNumeral() {
		/* octal numeral = "0" , octal digits
		 *               | "0" , underscores , octal digits ; */
		return accept(() -> {
			if (accept('0')) {
				underscores();
				return octalDigits();
			} else {
				return false;
			}
		});
	}
	private boolean octalDigits() {
		/* octal digits = octal digit
		 *              | octal digit , [ octal digits and underscores ] , octal digit ;
		 * octal digits and underscores = octal digit or underscore , { octal digit or underscore } ;
		 * octal digit or underscore = octal digit | "_" ; */

		/* octal digits = octal digit
		 *              | octal digit , octal digit ;
		 *              | octal digit , octal digit , { octal digit | "_" } , octal digit ;
		 *              | octal digit , "_" , { octal digit | "_" } , octal digit ; */
		return digitsOfAnyBase(this::octalDigit);
	}
	private boolean octalDigit() {
		/* octal digit = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" ; */
		return acceptAnyInRange('0', '7');
	}

	private boolean binaryNumeral() {
		/* binary numeral = "0" , "b" , binary digits
		 *                | "0" , "B" , binary digits ;
		 * binary digits = binary digit
		 *               | binary digit , [ binary digits and underscores ] , binary digit ;
		 * binary digit = "0" | "1" ; */
		return accept(() -> accept('0') && acceptAny('b', 'B') && digitsOfAnyBase(() -> acceptAny('0', '1')));
	}


	private boolean floatingPointLiteral() {
		/* floating point literal = decimal floating point literal | hexadecimal floating point literal ; */
		final boolean accepted = decimalFloatingPointLiteral() || hexadecimalFloatingPointLiteral();
		if (accepted)
			newToken(TokenType.LITERAL, Terminal.FLOATING_POINT_LITERAL);
		return accepted;
	}
	private boolean decimalFloatingPointLiteral() {
		/* decimal floating point literal = digits , "." , [ digits ] , [ exponent part ] , [ float type suffix ]
		 *                                | "." , digits , [ exponent part ] , [ float type suffix ]
		 *                                | digits , exponent part , [ float type suffix ]
		 *                                | digits , [ exponent part ] , float type suffix ; */

		/* decimal floating point literal = digits , "."
		 *                                | digits , "." , float type suffix
		 *                                | digits , "." , exponent part
		 *                                | digits , "." , exponent part , float type suffix
		 *                                | digits , "." , digits
		 *                                | digits , "." , digits , float type suffix
		 *                                | digits , "." , digits , exponent part
		 *                                | digits , "." , digits , exponent part , float type suffix
		 *                                | digits , exponent part
		 *                                | digits , exponent part , float type suffix
		 *                                | digits , float type suffix
		 *                                | "." , digits
		 *                                | "." , digits , float type suffix
		 *                                | "." , digits , exponent part
		 *                                | "." , digits , exponent part , float type suffix ; */

		return accept(() -> {
			if (digits()) {
				if (accept('.')) {
					digits();
					exponentPart();
					floatTypeSuffix();
					return true;
				} else {
					return exponentPart() | floatTypeSuffix();
				}
			} else if (accept('.')) {
				if (digits()) {
					exponentPart();
					floatTypeSuffix();
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		});
	}
	private boolean exponentPart() {
		/* exponent part = exponent indicator , signed integer ;
		 * exponent indicator = "e" | "E" ;
		 *
		 * exponent part = ( "e" | "E" ) , signed integer ; */
		return accept(() -> acceptAny('e', 'E') && signedInteger());
	}
	private boolean signedInteger() {
		/* signed integer = [ sign ] , digits ;
		 * sign = "+" | "-" ;
		 *
		 * signed integer = [ "+" | "-" ] , digits ; */
		return accept(() -> {
			acceptAny('+', '-');
			return digits();
		});
	}
	private boolean floatTypeSuffix() {
		/* float type suffix = "f" | "F" | "d" | "D" ; */
		return acceptAny('f', 'F', 'd', 'D');
	}

	private boolean hexadecimalFloatingPointLiteral() {
		/* hexadecimal floating point literal = hex significand , binary exponent , [ float type suffix ] ;
		 * hex significand = hex numeral , [ "." ]
		 *                 | "0" , ( "x" | "X" ) , [ hex digits ] , "." , hex digits ;
		 * binary exponent = binary exponent indicator , signed integer ;
		 * binary exponent indicator = "p" | "P" ;
		 * hex numeral = "0" , "x" , hex digits
		 *             | "0" , "X" , hex digits ; */


		/* hexadecimal floating point literal = "0" , ( "x" | "X" ) , ( ( hex digits , [ "." ] )
		                                                              | ( [ hex digits ] , "." , hex digits ) )
		                                      , ( "p" | "P" ) , signed integer
		                                      , [ float type suffix ] ; */


		/* A =  ;
		 * hexadecimal floating point literal = "0" , ( "x" | "X" ) , ( hex digits
		 *                                                            | hex digits , "."
		 *                                                            | hex digits , "." , hex digits )
		 *                                                            | "." , hex digits
		 *                                          , ( "p" | "P" ) , signed integer , [ float type suffix ] ; */

		return accept(() -> {
			if (!(accept('0') && acceptAny('x', 'X') && hexDigits()))
				return false;
			if (accept('.'))
				hexDigits();
			if (!(acceptAny('p', 'P') && signedInteger()))
				return false;
			floatTypeSuffix();
			return true;
		});
	}

	private boolean characterLiteral() {
		/* character literal = "'" , single character , "'"
		 *                   | "'" , escape sequence , "'" ;
		 * (* It is a compile-time error for the character following the single character or escape sequence to be other than a '. *)
		 * (* It is a compile-time error for a line terminator to appear after the opening ' and before the closing matching '. *)
		 *
		 * single character = input character - ( "'" | "\" ) ; */

		final boolean accepted = accept(() -> {
			if (!accept('\''))
				return false;
			else if (!lexing || lineTerminator())
				return lexError("unclosed character literal");
			else if (accept('\''))
				return lexError("empty character literal");
			else if (!(escapeSequence() || rawInputCharacter()))
				return false; // This cannot happen.
			else if (!lexing || lineTerminator())
				return lexError("unclosed character literal");
			else if (!accept('\''))
				return lexError("too many characters in character literal");
			else return true;
		});
		if (accepted)
			newToken(TokenType.LITERAL, Terminal.CHARACTER_LITERAL);
		return accepted;
	}

	private boolean stringLiteral() {
		/* string literal = '"' , { string character } , '"' ;
		 * (* It is a compile-time error for a line terminator to appear after the opening " and before the closing matching ". *)
		 *
		 * string character = input character - ( '"' | '\' )
		 *                  | escape sequence ; */

		final boolean accepted = accept(() -> {
			if (!accept('"'))
				return false;
			while (true)
				if (!lexing || lineTerminator())
					return lexError("unclosed string literal");
				else if (accept('"'))
					break;
				else if (!(escapeSequence() || rawInputCharacter()))
					return false; // This cannot happen.
			return true;
		});
		if (accepted)
			newToken(TokenType.LITERAL, Terminal.STRING_LITERAL);
		return accepted;
	}

	private boolean escapeSequence() {
		/* escape sequence = "\" , "b" (* backspace BS, \u0008 *)
		 *                 | "\" , "t" (* horizontal tab HT, \u0009 *)
		 *                 | "\" , "n" (* linefeed LF, \u000a *)
		 *                 | "\" , "f" (* form feed FF, \u000C *)
		 *                 | "\" , "r" (* carriage return CR, \u000D *)
		 *                 | "\" , '"' (* double quote ", \u0022 *)
		 *                 | "\" , "'" (* single quote ', \u0027 *)
		 *                 | "\" , "\" (* backslash \, \u005C *)
		 *                 | octal escape ;
		 * octal escape = "\" , octal digit
		 *              | "\" , octal digit , octal digit
		 *              | "\" , zero to three , octal digit , octal digit ;
		 * (* It is a compile-time error if the character following a backslash in an escape sequence is not an ASCII
		 *    b, t, n, f, r, ", ', \, 0, 1, 2, 3, 4, 5, 6, or 7. The Unicode escape u is processed earlier. *) */

		/* octal escape = "\" , octal digit
		 *              | "\" , octal digit , octal digit
		 *              | "\" , zero to three , octal digit , octal digit ; */

		return accept(() -> {
			if (!accept('\\'))
				return false;
			if (acceptAny('b', 't', 'n', 'f', 'r', '"', '\'', '\\')) {
				return true;
			} else if (acceptAnyInRange('0', '3')) {
				if (octalDigit())
					octalDigit();
				return true;
			} else if (acceptAnyInRange('4', '7')) {
				octalDigit();
				return true;
			} else {
				return lexError("illegal escape character");
			}
		});
	}
	private boolean digitsOfAnyBase(Acceptor digitAcceptor) {
		/* digits of base n = digit of base n
		 *                  | digit of base n , digit of base n ;
		 *                  | digit of base n , digit of base n , { digit of base n | "_" } , digit of base n ;
		 *                  | digit of base n , "_" , { digit of base n | "_" } , digit of base n ; */
		return accept(() -> {
			if (!digitAcceptor.accept())
				return false;
			boolean acceptedMore, endsWithDigit;
			if (accept('_')) {
				acceptedMore = true;
				endsWithDigit = false;
			} else if (digitAcceptor.accept()) {
				acceptedMore = true;
				endsWithDigit = true;
			} else {
				acceptedMore = false;
				endsWithDigit = true;
			}
			while (acceptedMore) {
				if (accept('_')) {
					endsWithDigit = false;
				} else if (digitAcceptor.accept()) {
					endsWithDigit = true;
				} else {
					acceptedMore = false;
				}
			}
			if (!endsWithDigit)
				lexError("illegal underscore");
			return endsWithDigit;
		});
	}
}