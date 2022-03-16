package datastructures.tree;
public interface TreeNode {
	String valueString();
	default void buildString(StringBuilder full, StringBuilder indent, boolean isLastChild) {
		full.append(indent).append(isLastChild ? '\u2514' : '\u251c').append("\u2500\u2500\u2500").append(valueString()).append('\n');
	}

	/**
	 * Returns the size of the tree with this instance of TreeNode being the root. The size of the tree is the number of
	 * children plus the number of grandchildren plus the number of great-grandchildren etc. etc.
	 */
	int fullSize();
}