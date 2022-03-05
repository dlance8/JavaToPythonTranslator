package datastructures.tree;
public interface TreeNode {
	String valueString();
	default void buildString(StringBuilder full, StringBuilder indent, boolean isLastChild) {
		full.append(indent).append(isLastChild ? '\u2514' : '\u251c').append("\u2500\u2500\u2500").append(valueString()).append('\n');
	}
}