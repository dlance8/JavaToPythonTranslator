package constants;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
public final class TextConstants {
	/** Don't let anyone instantiate this class */
	private TextConstants() {}
	public static final char CR = 0x000A, FF = 0x000C, HT = 0x0009, LF = 0x000D, SP = 0x0020, SUB = 0x001A;
	public static final EnumSet<Terminal> ASSIGNMENT_OPERATORS;
	public static final HashMap<String, Terminal> BOOLEAN_LITERALS, NULL_LITERAL, KEYWORDS, SEPARATORS, OPERATORS;
	public static final LinkedList<HashMap<String, Terminal>> ALL_MAPS;
	static {
		ASSIGNMENT_OPERATORS = EnumSet.of(Terminal.ASSIGN, Terminal.ASSIGN_MULTIPLY, Terminal.ASSIGN_DIVIDE,
			Terminal.ASSIGN_MOD, Terminal.ASSIGN_ADD, Terminal.ASSIGN_SUBTRACT, Terminal.ASSIGN_LEFT_SHIFT,
			Terminal.ASSIGN_RIGHT_SHIFT, Terminal.ASSIGN_UNSIGNED_RIGHT_SHIFT, Terminal.ASSIGN_AND, Terminal.ASSIGN_XOR,
			Terminal.ASSIGN_OR);

		BOOLEAN_LITERALS = new HashMap<>();
		BOOLEAN_LITERALS.put("true",    Terminal.BOOLEAN_LITERAL    );
		BOOLEAN_LITERALS.put("false",   Terminal.BOOLEAN_LITERAL    );

		NULL_LITERAL = new HashMap<>();
		NULL_LITERAL.put("null",    Terminal.NULL_LITERAL   );

		KEYWORDS = new HashMap<>();
		KEYWORDS.put("abstract",        Terminal.ABSTRACT       );
		KEYWORDS.put("assert",          Terminal.ASSERT         );
		KEYWORDS.put("boolean",         Terminal.BOOLEAN        );
		KEYWORDS.put("break",           Terminal.BREAK          );
		KEYWORDS.put("byte",            Terminal.BYTE           );
		KEYWORDS.put("case",            Terminal.CASE           );
		KEYWORDS.put("catch",           Terminal.CATCH          );
		KEYWORDS.put("char",            Terminal.CHAR           );
		KEYWORDS.put("class",           Terminal.CLASS          );
		KEYWORDS.put("const",           Terminal.CONST          );
		KEYWORDS.put("continue",        Terminal.CONTINUE       );
		KEYWORDS.put("default",         Terminal.DEFAULT        );
		KEYWORDS.put("do",              Terminal.DO             );
		KEYWORDS.put("double",          Terminal.DOUBLE         );
		KEYWORDS.put("else",            Terminal.ELSE           );
		KEYWORDS.put("enum",            Terminal.ENUM           );
		KEYWORDS.put("extends",         Terminal.EXTENDS        );
		KEYWORDS.put("final",           Terminal.FINAL          );
		KEYWORDS.put("finally",         Terminal.FINALLY        );
		KEYWORDS.put("float",           Terminal.FLOAT          );
		KEYWORDS.put("for",             Terminal.FOR            );
		KEYWORDS.put("goto",            Terminal.GOTO           );
		KEYWORDS.put("if",              Terminal.IF             );
		KEYWORDS.put("implements",      Terminal.IMPLEMENTS     );
		KEYWORDS.put("import",          Terminal.IMPORT         );
		KEYWORDS.put("instanceof",      Terminal.INSTANCEOF     );
		KEYWORDS.put("int",             Terminal.INT            );
		KEYWORDS.put("interface",       Terminal.INTERFACE      );
		KEYWORDS.put("long",            Terminal.LONG           );
		KEYWORDS.put("native",          Terminal.NATIVE         );
		KEYWORDS.put("new",             Terminal.NEW            );
		KEYWORDS.put("package",         Terminal.PACKAGE        );
		KEYWORDS.put("private",         Terminal.PRIVATE        );
		KEYWORDS.put("protected",       Terminal.PROTECTED      );
		KEYWORDS.put("public",          Terminal.PUBLIC         );
		KEYWORDS.put("return",          Terminal.RETURN         );
		KEYWORDS.put("short",           Terminal.SHORT          );
		KEYWORDS.put("static",          Terminal.STATIC         );
		KEYWORDS.put("strictfp",        Terminal.STRICTFP       );
		KEYWORDS.put("super",           Terminal.SUPER          );
		KEYWORDS.put("switch",          Terminal.SWITCH         );
		KEYWORDS.put("synchronized",    Terminal.SYNCHRONIZED   );
		KEYWORDS.put("this",            Terminal.THIS           );
		KEYWORDS.put("throw",           Terminal.THROW          );
		KEYWORDS.put("throws",          Terminal.THROWS         );
		KEYWORDS.put("transient",       Terminal.TRANSIENT      );
		KEYWORDS.put("try",             Terminal.TRY            );
		KEYWORDS.put("void",            Terminal.VOID           );
		KEYWORDS.put("volatile",        Terminal.VOLATILE       );
		KEYWORDS.put("while",           Terminal.WHILE          );

		SEPARATORS = new HashMap<>();
		SEPARATORS.put("(",     Terminal.OPEN_PARENTHESIS   );
		SEPARATORS.put(")",     Terminal.CLOSE_PARENTHESIS  );
		SEPARATORS.put("{",     Terminal.OPEN_BRACE         );
		SEPARATORS.put("}",     Terminal.CLOSE_BRACE        );
		SEPARATORS.put("[",     Terminal.OPEN_BRACKET       );
		SEPARATORS.put("]",     Terminal.CLOSE_BRACKET      );
		SEPARATORS.put(";",     Terminal.SEMICOLON          );
		SEPARATORS.put(",",     Terminal.COMMA              );
		SEPARATORS.put(".",     Terminal.DOT                );
		SEPARATORS.put("...",   Terminal.ELLIPSES           );
		SEPARATORS.put("@",     Terminal.AT_SYMBOL          );
		SEPARATORS.put("::",    Terminal.DOUBLE_COLON       );

		OPERATORS = new HashMap<>();
		OPERATORS.put("=",      Terminal.ASSIGN                         );
		OPERATORS.put(">",      Terminal.GREATER_THAN                   );
		OPERATORS.put("<",      Terminal.LESS_THAN                      );
		OPERATORS.put("!",      Terminal.NOT                            );
		OPERATORS.put("~",      Terminal.BITWISE_COMPLEMENT             );
		OPERATORS.put("?",      Terminal.TERNARY                        );
		OPERATORS.put(":",      Terminal.COLON                          );
		OPERATORS.put("->",     Terminal.LAMBDA                         );
		OPERATORS.put("==",     Terminal.EQUAL_TO                       );
		OPERATORS.put(">=",     Terminal.GREATER_THAN_OR_EQUAL_TO       );
		OPERATORS.put("<=",     Terminal.LESS_THAN_OR_EQUAL_TO          );
		OPERATORS.put("!=",     Terminal.NOT_EQUAL_TO                   );
		OPERATORS.put("&&",     Terminal.AND_GATE                       );
		OPERATORS.put("||",     Terminal.OR_GATE                        );
		OPERATORS.put("++",     Terminal.INCREMENT                      );
		OPERATORS.put("--",     Terminal.DECREMENT                      );
		OPERATORS.put("+",      Terminal.ADD                            );
		OPERATORS.put("-",      Terminal.SUBTRACT                       );
		OPERATORS.put("*",      Terminal.MULTIPLY                       );
		OPERATORS.put("/",      Terminal.DIVIDE                         );
		OPERATORS.put("&",      Terminal.AND                            );
		OPERATORS.put("|",      Terminal.OR                             );
		OPERATORS.put("^",      Terminal.XOR                            );
		OPERATORS.put("%",      Terminal.MODULO                         );
		OPERATORS.put("<<",     Terminal.LEFT_SHIFT                     );
		OPERATORS.put(">>",     Terminal.RIGHT_SHIFT                    );
		OPERATORS.put(">>>",    Terminal.UNSIGNED_RIGHT_SHIFT           );
		OPERATORS.put("+=",     Terminal.ASSIGN_ADD                     );
		OPERATORS.put("-=",     Terminal.ASSIGN_SUBTRACT                );
		OPERATORS.put("*=",     Terminal.ASSIGN_MULTIPLY                );
		OPERATORS.put("/=",     Terminal.ASSIGN_DIVIDE                  );
		OPERATORS.put("&=",     Terminal.ASSIGN_AND                     );
		OPERATORS.put("|=",     Terminal.ASSIGN_OR                      );
		OPERATORS.put("^=",     Terminal.ASSIGN_XOR                     );
		OPERATORS.put("%=",     Terminal.ASSIGN_MOD                     );
		OPERATORS.put("<<=",    Terminal.ASSIGN_LEFT_SHIFT              );
		OPERATORS.put(">>=",    Terminal.ASSIGN_RIGHT_SHIFT             );
		OPERATORS.put(">>>=",   Terminal.ASSIGN_UNSIGNED_RIGHT_SHIFT    );

		ALL_MAPS = new LinkedList<>(Arrays.asList(BOOLEAN_LITERALS, NULL_LITERAL, KEYWORDS, SEPARATORS, OPERATORS));
	}
}