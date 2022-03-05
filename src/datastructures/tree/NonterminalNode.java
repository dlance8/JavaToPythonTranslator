package datastructures.tree;
import constants.Nonterminal;
import java.util.ArrayList;
import java.util.ListIterator;
public class NonterminalNode extends ArrayList<TreeNode> implements TreeNode {
	private Nonterminal value;
	public NonterminalNode(Nonterminal value) {
		this.value = value;
	}

	/**
	 * Provides a shorthand for the cast expression ((NonterminalNode) NonterminalNode.get(index)) where it is assumed
	 * that the child exists and is nonnull and is of type NonterminalNode.
	 */
	public NonterminalNode getNonterminalChild(int index) {
		return (NonterminalNode) get(index);
	}

	/**
	 * Provides a shorthand for the cast expression ((TerminalNode) NonterminalNode.get(index)) where it is assumed that
	 * that the child exists and is nonnull and is of type TerminalNode.
	 */
	public TerminalNode getTerminalChild(int index) {
		return (TerminalNode) get(index);
	}

	public void tighten() {
		ListIterator<TreeNode> itr = super.listIterator();
		while (itr.hasNext()) {
			TreeNode child = itr.next();
			if (child instanceof NonterminalNode) {
				NonterminalNode nonterminalChild = (NonterminalNode) child;
				nonterminalChild.tighten();
				if (nonterminalChild.size() == 1) {
					itr.set(nonterminalChild.get(0));
				}
			}
		}
	}

	@Override
	public String valueString() {
		final String original = value.toString();
		StringBuilder stringBuilder = new StringBuilder();
		boolean capitalize = false;
		for (int i = 0; i < original.length(); ++i) {
			final char c = original.charAt(i);
			if (c == '_') {
				capitalize = true;
			} else {
				stringBuilder.append(capitalize || i == 0 ? c : Character.toLowerCase(c));
				capitalize = false;
			}
		}
		return stringBuilder.toString();
	}

	@Override
	public void buildString(StringBuilder full, StringBuilder indent, boolean isLastChild) {
		// Recursive method to build strings from tree nodes
		// Mimics the format of the "tree" command in Windows command prompt
		TreeNode.super.buildString(full, indent, isLastChild);
		if (size() == 0) return;
		indent.append(isLastChild ? ' ' : '\u2502').append("   ");
		TreeNode lastChild = get(size() - 1);
		for (TreeNode child : this) {
			child.buildString(full, indent, child == lastChild);
		}
		indent.setLength(indent.length() - 4);
	}

	@Override
	public String toString() {
		StringBuilder full = new StringBuilder().append(valueString()).append('\n');
		if (size() == 0)
			return full.toString();
		StringBuilder indent = new StringBuilder();
		TreeNode lastChild = get(size() - 1);
		for (TreeNode child : this)
			child.buildString(full, indent, child == lastChild);
		return full.toString();
	}

	public Nonterminal getValue() {
		return value;
	}
	public void setValue(Nonterminal value) {
		this.value = value;
	}
}
