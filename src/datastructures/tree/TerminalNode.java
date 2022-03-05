package datastructures.tree;
import constants.Terminal;
import datastructures.Token;
public class TerminalNode extends Token implements TreeNode {
	public TerminalNode(Token token) {
		super(token);
	}
	@Override
	public String valueString() {
		final char quotes = getValue() == Terminal.STRING_LITERAL ? '\'' : '"';
		return "\u001B[32m" + getType() + " | " + getValue() + " | " + quotes + getText() + quotes + "\u001B[0m";
	}
	@Override
	public String toString() {
		return valueString();
	}
}