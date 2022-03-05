package processes;
import constants.Nonterminal;
import constants.Terminal;
import constants.TokenType;
import datastructures.Token;
import javafx.scene.control.TreeItem;
import datastructures.tree.NonterminalNode;
import datastructures.tree.TerminalNode;
import java.util.ArrayList;
public final class Parser extends MyProcess {
	private static final boolean PRINT_PROGRESS = false;

	private boolean parsing;
	private int currentIndex;
	private ArrayList<Token> tokens;
	private NonterminalNode currentParent, root;
	private Token currentToken;

	public TreeItem<String> debugRoot;
	private TreeItem<String> debugCurrent;
	private TreeItem<String> addMessage(String message) {
		return addMessage(debugCurrent, message);
	}
	private TreeItem<String> addMessage(TreeItem<String> parent, String message) {
		TreeItem<String> newItem = new TreeItem<>(message);
		parent.getChildren().add(newItem);
		return newItem;
	}
	private TreeItem<String> newCurrent(String message) {
		return debugCurrent = debugCurrent == null ? debugRoot = new TreeItem<>(message) : addMessage(message);
	}
	private void $(TreeItem<String> node, boolean accepted) {
		$(node, accepted, null);
	}
	private void $(TreeItem<String> node, boolean accepted, String text) {
		node.setValue((accepted ? '\u2713' : '\u2717') + node.getValue() + (text == null ? "" : " | " + text));
	}
	private void printProgress(String message) {
		if (PRINT_PROGRESS) System.out.println(message);
	}

	public Parser() {

	}
	public Parser(Thread processManagementThread) {
		super(processManagementThread);
	}

	private interface NonterminalAcceptor { boolean accept(); }

	public NonterminalNode parse(ArrayList<Token> tokens) {
		this.tokens = tokens;
		start();
		try {
			accept(this::compilationUnit);
		} catch (NullPointerException|StackOverflowError e) {
			error("Failed in Parser");
		}
		return root.size() == 0 ? root : root.getNonterminalChild(0);
	}
	private boolean identifier() {
//		return accept(Nonterminal.IDENTIFIER, () -> acceptAppendAdvance(parsing && currentToken.getType() == TokenType.IDENTIFIER));
		return acceptAppendAdvance(parsing && currentToken.getType() == TokenType.IDENTIFIER);
	}
	private boolean literal() {
//		return accept(Nonterminal.LITERAL, () -> acceptAppendAdvance(currentToken.getType() == TokenType.LITERAL));
		return acceptAppendAdvance(currentToken.getType() == TokenType.LITERAL);
	}
	private boolean check(TokenType type) {
		return parsing && currentToken.getType() == type;
	}

	private boolean lookAhead(int k, Terminal value) {
		return parsing && currentIndex + k < tokens.size() && tokens.get(currentIndex + k).getValue() == value;
	}
	private boolean lookAhead(Terminal... values) {
		if (!(parsing && currentIndex + values.length < tokens.size())) return false;
		for (int i = 0; i < values.length; ++i)
			if (tokens.get(currentIndex + i).getValue() != values[i]) return false;
		return true;
	}

	private boolean check(Terminal value) {
		return parsing && currentToken.getValue() == value;
	}
	private boolean accept(Nonterminal value, Terminal... terminals) {
		final NonterminalNode currentParentAtStart = currentParent;
		currentParent = new NonterminalNode(value);
		boolean acceptedAny = false;
		for (Terminal terminal : terminals) {
			if (accept(terminal)) {
				acceptedAny = true;
				break;
			}
		}
		if (acceptedAny)
			currentParentAtStart.add(currentParent);
		currentParent = currentParentAtStart;
		return acceptedAny;
	}
	private boolean accept(Nonterminal value, NonterminalAcceptor Nonterminals) {
		printProgress("Nonterminal\n\tvalue = " + value + "\n\tcurrentToken = " + currentToken.getText());
		final TreeItem<String> oldCurrent = debugCurrent, thisRoot = newCurrent(value.toString());

		final NonterminalNode parentAtStart = currentParent;

		currentParent = new NonterminalNode(value);

		TreeItem<String> x = debugCurrent = addMessage(thisRoot, "option 1 of 1 | currentToken = \"" + currentToken.getText() + "\"");

		final boolean accepted = accept(Nonterminals);

		$(x, accepted);

		if (accepted)
			parentAtStart.add(currentParent);
		currentParent = parentAtStart;

		$(thisRoot, accepted);
		debugCurrent = oldCurrent;

		return accepted;
	}
	private boolean acceptAny(NonterminalAcceptor... acceptors) {
		for (NonterminalAcceptor acceptor : acceptors)
			if (accept(acceptor)) return true;
		return false;
	}
	private boolean acceptAll(NonterminalAcceptor... acceptors) {
		final int childrenAtStart = currentParent.size();
		final boolean accepted = accept(() -> {
			for (NonterminalAcceptor acceptor : acceptors)
				if (!accept(acceptor)) return false;
			return true;
		});
		if (!accepted) {
			while (currentParent.size() > childrenAtStart) {
				currentParent.remove(currentParent.size() - 1);
			}
		}
		return accepted;
	}
	private boolean accept(Terminal terminal) {
		printProgress("TERMINAL\n\tvalue = " + terminal + "\n\tcurrentToken = " + currentToken.getText());
		final String text = currentToken.getText();
		final boolean accepted = acceptAppendAdvance(currentToken.getValue() == terminal);
		TreeItem<String> newTreeItem = addMessage("TERMINAL: " + terminal.toString());
		$(newTreeItem, accepted, text);
		return accepted;
	}
	private boolean accept(NonterminalAcceptor Nonterminal) {
		final TreeItem<String> oldCurrent = debugCurrent, newCurrent = newCurrent("ACCEPT");
		final boolean parsingAtStart = parsing;
		final int indexAtStart = currentIndex;
		final NonterminalNode parentAtStart = currentParent;
		final Token tokenAtStart = currentToken;
		final boolean accepted = Nonterminal.accept();
		if (!accepted) {
			parsing = parsingAtStart;
			currentIndex = indexAtStart;
			currentParent = parentAtStart;
			currentToken = tokenAtStart;
		}
		if (newCurrent.getChildren().size() == 1) {
			if (oldCurrent != null) {
				oldCurrent.getChildren().remove(oldCurrent.getChildren().size() - 1);
				oldCurrent.getChildren().add(newCurrent.getChildren().get(0));
			}
		} else {
			$(newCurrent, accepted);
		}
		debugCurrent = oldCurrent;
		return accepted;
	}
	private boolean acceptRepeating(NonterminalAcceptor acceptor) {
		final TreeItem<String> oldCurrent = debugCurrent, newCurrent = newCurrent("ACCEPT REPEATING");

		int n = 0;
		while (true)
			if (accept(acceptor)) n++; else break;

		$(newCurrent, n > 0);
		debugCurrent = oldCurrent;

		return true;
	}
	private boolean acceptRepeating(Terminal value) {
		final TreeItem<String> oldCurrent = debugCurrent, newCurrent = newCurrent("ACCEPT REPEATING");

		int n = 0;
		while (true)
			if (accept(value)) n++; else break;

		$(newCurrent, n > 0);
		debugCurrent = oldCurrent;

		return true;
	}
	private boolean acceptOptional(NonterminalAcceptor Nonterminal) {
		final TreeItem<String> oldCurrent = debugCurrent, newCurrent = newCurrent("ACCEPT OPTIONAL");
		final boolean accepted = accept(Nonterminal);
		if (newCurrent.getChildren().size() == 1) {
			final TreeItem<String> child = newCurrent.getChildren().get(0);
			child.setValue(new StringBuilder(child.getValue()).insert(1, '[').append(']').toString());
			oldCurrent.getChildren().set(oldCurrent.getChildren().size() - 1, child);
		} else $(newCurrent, accepted);
		debugCurrent = oldCurrent;
		return true;
	}
	private boolean acceptOptional(Terminal value) {
		final TreeItem<String> oldCurrent = debugCurrent, newCurrent = newCurrent("ACCEPT OPTIONAL");
		final boolean accepted = accept(value);
		if (newCurrent.getChildren().size() == 1) {
			final TreeItem<String> child = newCurrent.getChildren().get(0);
			child.setValue(new StringBuilder(child.getValue()).insert(2, '[').append(']').toString());
			oldCurrent.getChildren().set(oldCurrent.getChildren().size() - 1, child);
		} else $(newCurrent, accepted);
		debugCurrent = oldCurrent;
		return true;
	}
	private void start() {
		currentParent = root = new NonterminalNode(Nonterminal.MY_ROOT);
		currentIndex = -1;
		advance();
	}
	private void advance() {
		currentToken = (parsing = ++currentIndex < tokens.size()) ? tokens.get(currentIndex) : new Token();
	}
	private boolean acceptAppendAdvance(boolean accepted) {
		if (accepted &= parsing) {
			currentParent.add(new TerminalNode(currentToken));
			advance();
		}
		return accepted;
	}

	private boolean type() {
		return accept(Nonterminal.TYPE, () -> acceptAny(this::primitiveType, this::referenceType));
	}
	private boolean primitiveType() {
		return accept(Nonterminal.PRIMITIVE_TYPE, () -> acceptAny(() -> acceptAll(() -> acceptRepeating(this::annotation), this::numericType), () -> acceptAll(() -> acceptRepeating(this::annotation), () -> accept(Terminal.BOOLEAN))));
	}
	private boolean numericType() {
		return accept(Nonterminal.NUMERIC_TYPE, () -> acceptAny(this::integralType, this::floatingPointType));
	}
	private boolean integralType() {
		return accept(Nonterminal.INTEGRAL_TYPE, () -> acceptAny(() -> accept(Terminal.BYTE), () -> accept(Terminal.SHORT), () -> accept(Terminal.INT), () -> accept(Terminal.LONG), () -> accept(Terminal.CHAR)));
	}
	private boolean floatingPointType() {
		return accept(Nonterminal.FLOATING_POINT_TYPE, () -> acceptAny(() -> accept(Terminal.FLOAT), () -> accept(Terminal.DOUBLE)));
	}
	private boolean referenceType() {
		return accept(Nonterminal.REFERENCE_TYPE, () -> acceptAny(this::arrayType, this::classOrInterfaceType, this::typeVariable));
	}
	private boolean classOrInterfaceType() {
		return accept(Nonterminal.CLASS_OR_INTERFACE_TYPE, () -> acceptAny(this::classType, this::interfaceType));
	}
	private boolean classType() {
		return accept(Nonterminal.CLASS_TYPE, () -> acceptAny(() -> acceptAll(() -> acceptRepeating(this::annotation), this::identifier, () -> acceptOptional(this::typeArguments)), () -> acceptAll(this::classOrInterfaceType, () -> accept(Terminal.DOT), () -> acceptRepeating(this::annotation), this::identifier, () -> acceptOptional(this::typeArguments))));
	}
	private boolean interfaceType() {
		return accept(Nonterminal.INTERFACE_TYPE, this::classType);
	}
	private boolean typeVariable() {
		return accept(Nonterminal.TYPE_VARIABLE, () -> acceptAll(() -> acceptRepeating(this::annotation), this::identifier));
	}
	private boolean arrayType() {
		return accept(Nonterminal.ARRAY_TYPE, () -> acceptAny(() -> acceptAll(this::primitiveType, this::dims), () -> acceptAll(this::classType, this::dims), () -> acceptAll(this::typeVariable, this::dims)));
	}
	private boolean dims() {
		return accept(Nonterminal.DIMS, () -> acceptAll(() -> acceptRepeating(this::annotation), () -> accept(Terminal.OPEN_BRACKET), () -> accept(Terminal.CLOSE_BRACKET), () -> acceptRepeating(() -> acceptAll(() -> acceptRepeating(this::annotation), () -> accept(Terminal.OPEN_BRACKET), () -> accept(Terminal.CLOSE_BRACKET)))));
	}
	private boolean typeParameter() {
		return accept(Nonterminal.TYPE_PARAMETER, () -> acceptAll(() -> acceptRepeating(this::typeParameterModifier), this::identifier, () -> acceptOptional(this::typeBound)));
	}
	private boolean typeParameterModifier() {
		return accept(Nonterminal.TYPE_PARAMETER_MODIFIER, this::annotation);
	}
	private boolean typeBound() {
		return accept(Nonterminal.TYPE_BOUND, () -> acceptAny(() -> acceptAll(() -> accept(Terminal.EXTENDS), this::typeVariable), () -> acceptAll(() -> accept(Terminal.EXTENDS), this::classOrInterfaceType, () -> acceptRepeating(this::additionalBound))));
	}
	private boolean additionalBound() {
		return accept(Nonterminal.ADDITIONAL_BOUND, () -> acceptAll(() -> accept(Terminal.AND), this::interfaceType));
	}
	private boolean typeArguments() {
		return accept(Nonterminal.TYPE_ARGUMENTS, () -> acceptAll(() -> accept(Terminal.LESS_THAN), this::typeArgumentList, () -> accept(Terminal.GREATER_THAN)));
	}
	private boolean typeArgumentList() {
		return accept(Nonterminal.TYPE_ARGUMENT_LIST, () -> acceptAll(this::typeArgument, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.COMMA), this::typeArgument))));
	}
	private boolean typeArgument() {
		return accept(Nonterminal.TYPE_ARGUMENT, () -> acceptAny(this::referenceType, this::wildcard));
	}
	private boolean wildcard() {
		return accept(Nonterminal.WILDCARD, () -> acceptAll(() -> acceptRepeating(this::annotation), () -> accept(Terminal.TERNARY), () -> acceptOptional(this::wildcardBounds)));
	}
	private boolean wildcardBounds() {
		return accept(Nonterminal.WILDCARD_BOUNDS, () -> acceptAny(() -> acceptAll(() -> accept(Terminal.EXTENDS), this::referenceType), () -> acceptAll(() -> accept(Terminal.SUPER), this::referenceType)));
	}
	private boolean packageName() {
		return accept(Nonterminal.PACKAGE_NAME, () -> acceptAll(this::identifier, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.DOT), this::identifier))));
	}
	private boolean typeName() {
		return accept(Nonterminal.TYPE_NAME, () -> acceptAll(this::identifier, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.DOT), this::identifier))));
	}
	private boolean packageOrTypeName() {
		return accept(Nonterminal.PACKAGE_OR_TYPE_NAME, () -> acceptAll(this::identifier, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.DOT), this::identifier))));
	}
	private boolean expressionName() {
		return accept(Nonterminal.EXPRESSION_NAME, () -> acceptAll(this::identifier, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.DOT), this::identifier))));
	}
	private boolean methodName() {
		return accept(Nonterminal.METHOD_NAME, this::identifier);
	}
	private boolean ambiguousName() {
		return accept(Nonterminal.AMBIGUOUS_NAME, () -> acceptAll(this::identifier, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.DOT), this::identifier))));
	}
	private boolean compilationUnit() {
		return accept(Nonterminal.COMPILATION_UNIT, () -> acceptAll(() -> acceptOptional(this::packageDeclaration), () -> acceptRepeating(this::importDeclaration), () -> acceptRepeating(this::typeDeclaration)));
	}
	private boolean packageDeclaration() {
		return accept(Nonterminal.PACKAGE_DECLARATION, () -> acceptAll(() -> acceptRepeating(this::packageModifier), () -> accept(Terminal.PACKAGE), this::identifier, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.DOT), this::identifier)), () -> accept(Terminal.SEMICOLON)));
	}
	private boolean packageModifier() {
		return accept(Nonterminal.PACKAGE_MODIFIER, this::annotation);
	}
	private boolean importDeclaration() {
		return accept(Nonterminal.IMPORT_DECLARATION, () -> acceptAny(this::singleTypeImportDeclaration, this::typeImportOnDemandDeclaration, this::singleStaticImportDeclaration, this::staticImportOnDemandDeclaration));
	}
	private boolean singleTypeImportDeclaration() {
		return accept(Nonterminal.SINGLE_TYPE_IMPORT_DECLARATION, () -> acceptAll(() -> accept(Terminal.IMPORT), this::typeName, () -> accept(Terminal.SEMICOLON)));
	}
	private boolean typeImportOnDemandDeclaration() {
		return accept(Nonterminal.TYPE_IMPORT_ON_DEMAND_DECLARATION, () -> acceptAll(() -> accept(Terminal.IMPORT), this::packageOrTypeName, () -> accept(Terminal.DOT), () -> accept(Terminal.MULTIPLY), () -> accept(Terminal.SEMICOLON)));
	}
	private boolean singleStaticImportDeclaration() {
		return accept(Nonterminal.SINGLE_STATIC_IMPORT_DECLARATION, () -> acceptAll(() -> accept(Terminal.IMPORT), () -> accept(Terminal.STATIC), this::typeName, () -> accept(Terminal.DOT), this::identifier, () -> accept(Terminal.SEMICOLON)));
	}
	private boolean staticImportOnDemandDeclaration() {
		return accept(Nonterminal.STATIC_IMPORT_ON_DEMAND_DECLARATION, () -> acceptAll(() -> accept(Terminal.IMPORT), () -> accept(Terminal.STATIC), this::typeName, () -> accept(Terminal.DOT), () -> accept(Terminal.MULTIPLY), () -> accept(Terminal.SEMICOLON)));
	}
	private boolean typeDeclaration() {
		return accept(Nonterminal.TYPE_DECLARATION, () -> acceptAny(this::classDeclaration, this::interfaceDeclaration, () -> accept(Terminal.SEMICOLON)));
	}
	private boolean classDeclaration() {
		return accept(Nonterminal.CLASS_DECLARATION, () -> acceptAny(this::normalClassDeclaration, this::enumDeclaration));
	}
	private boolean normalClassDeclaration() {
		return accept(Nonterminal.NORMAL_CLASS_DECLARATION, () -> acceptAll(() -> acceptRepeating(this::classModifier), () -> accept(Terminal.CLASS), this::identifier, () -> acceptOptional(this::typeParameters), () -> acceptOptional(this::superclass), () -> acceptOptional(this::superinterfaces), this::classBody));
	}
	private boolean classModifier() {
		return accept(Nonterminal.CLASS_MODIFIER, () -> acceptAny(this::annotation, () -> accept(Terminal.PUBLIC), () -> accept(Terminal.PROTECTED), () -> accept(Terminal.PRIVATE), () -> accept(Terminal.ABSTRACT), () -> accept(Terminal.STATIC), () -> accept(Terminal.FINAL), () -> accept(Terminal.STRICTFP)));
	}
	private boolean typeParameters() {
		return accept(Nonterminal.TYPE_PARAMETERS, () -> acceptAll(() -> accept(Terminal.LESS_THAN), this::typeParameterList, () -> accept(Terminal.GREATER_THAN)));
	}
	private boolean typeParameterList() {
		return accept(Nonterminal.TYPE_PARAMETER_LIST, () -> acceptAll(this::typeParameter, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.COMMA), this::typeParameter))));
	}
	private boolean superclass() {
		return accept(Nonterminal.SUPERCLASS, () -> acceptAll(() -> accept(Terminal.EXTENDS), this::classType));
	}
	private boolean superinterfaces() {
		return accept(Nonterminal.SUPERINTERFACES, () -> acceptAll(() -> accept(Terminal.IMPLEMENTS), this::interfaceTypeList));
	}
	private boolean interfaceTypeList() {
		return accept(Nonterminal.INTERFACE_TYPE_LIST, () -> acceptAll(this::interfaceType, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.COMMA), this::interfaceType))));
	}
	private boolean classBody() {
		return accept(Nonterminal.CLASS_BODY, () -> acceptAll(() -> accept(Terminal.OPEN_BRACE), () -> acceptRepeating(this::classBodyDeclaration), () -> accept(Terminal.CLOSE_BRACE)));
	}
	private boolean classBodyDeclaration() {
		return accept(Nonterminal.CLASS_BODY_DECLARATION, () -> acceptAny(this::classMemberDeclaration, this::instanceInitializer, this::staticInitializer, this::constructorDeclaration));
	}
	private boolean classMemberDeclaration() {
		return accept(Nonterminal.CLASS_MEMBER_DECLARATION, () -> acceptAny(this::fieldDeclaration, this::methodDeclaration, this::classDeclaration, this::interfaceDeclaration, () -> accept(Terminal.SEMICOLON)));
	}
	private boolean fieldDeclaration() {
		return accept(Nonterminal.FIELD_DECLARATION, () -> acceptAll(() -> acceptRepeating(this::fieldModifier), this::unannType, this::variableDeclaratorList, () -> accept(Terminal.SEMICOLON)));
	}
	private boolean fieldModifier() {
		return accept(Nonterminal.FIELD_MODIFIER, () -> acceptAny(this::annotation, () -> accept(Terminal.PUBLIC), () -> accept(Terminal.PROTECTED), () -> accept(Terminal.PRIVATE), () -> accept(Terminal.STATIC), () -> accept(Terminal.FINAL), () -> accept(Terminal.TRANSIENT), () -> accept(Terminal.VOLATILE)));
	}
	private boolean variableDeclaratorList() {
		return accept(Nonterminal.VARIABLE_DECLARATOR_LIST, () -> acceptAll(this::variableDeclarator, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.COMMA), this::variableDeclarator))));
	}
	private boolean variableDeclarator() {
		return accept(Nonterminal.VARIABLE_DECLARATOR, () -> acceptAll(this::variableDeclaratorId, () -> acceptOptional(() -> acceptAll(() -> accept(Terminal.ASSIGN), this::variableInitializer))));
	}
	private boolean variableDeclaratorId() {
		return accept(Nonterminal.VARIABLE_DECLARATOR_ID, () -> acceptAll(this::identifier, () -> acceptOptional(this::dims)));
	}
	private boolean variableInitializer() {
		return accept(Nonterminal.VARIABLE_INITIALIZER, () -> acceptAny(this::expression, this::arrayInitializer));
	}
	private boolean unannType() {
		return accept(Nonterminal.UNANN_TYPE, () -> acceptAny(this::unannReferenceType, this::unannPrimitiveType));
	}
	private boolean unannPrimitiveType() {
		return accept(Nonterminal.UNANN_PRIMITIVE_TYPE, () -> acceptAny(this::numericType, () -> accept(Terminal.BOOLEAN)));
	}
	private boolean unannReferenceType() {
		return accept(Nonterminal.UNANN_REFERENCE_TYPE, () -> acceptAny(this::unannArrayType, this::unannClassOrInterfaceType, this::unannTypeVariable));
	}
	private boolean unannClassOrInterfaceType() {
		if (!check(Terminal.IDENTIFIER)) {
			return false;
		}
		return accept(Nonterminal.UNANN_CLASS_OR_INTERFACE_TYPE, () -> acceptAny(this::unannClassType, this::unannInterfaceType));
	}
	private boolean unannClassType() {
		return accept(Nonterminal.UNANN_CLASS_TYPE, () -> acceptAny(() -> acceptAll(this::identifier, () -> acceptOptional(this::typeArguments)), () -> acceptAll(this::unannClassOrInterfaceType, () -> accept(Terminal.DOT), () -> acceptRepeating(this::annotation), this::identifier, () -> acceptOptional(this::typeArguments))));
	}
	private boolean unannInterfaceType() {
		return accept(Nonterminal.UNANN_INTERFACE_TYPE, this::unannClassType);
	}
	private boolean unannTypeVariable() {
		return accept(Nonterminal.UNANN_TYPE_VARIABLE, this::identifier);
	}
	private boolean unannArrayType() {
		return accept(Nonterminal.UNANN_ARRAY_TYPE, () -> acceptAny(() -> acceptAll(this::unannPrimitiveType, this::dims), () -> acceptAll(this::unannClassOrInterfaceType, this::dims), () -> acceptAll(this::unannTypeVariable, this::dims)));
	}
	private boolean methodDeclaration() {
		return accept(Nonterminal.METHOD_DECLARATION, () -> acceptAll(() -> acceptRepeating(this::methodModifier), this::methodHeader, this::methodBody));
	}
	private boolean methodModifier() {
		return accept(Nonterminal.METHOD_MODIFIER, () -> acceptAny(this::annotation, () -> accept(Terminal.PUBLIC), () -> accept(Terminal.PROTECTED), () -> accept(Terminal.PRIVATE), () -> accept(Terminal.ABSTRACT), () -> accept(Terminal.STATIC), () -> accept(Terminal.FINAL), () -> accept(Terminal.SYNCHRONIZED), () -> accept(Terminal.NATIVE), () -> accept(Terminal.STRICTFP)));
	}
	private boolean methodHeader() {
		return accept(Nonterminal.METHOD_HEADER, () -> acceptAny(() -> acceptAll(this::result, this::methodDeclarator, () -> acceptOptional(this::throws_)), () -> acceptAll(this::typeParameters, () -> acceptRepeating(this::annotation), this::result, this::methodDeclarator, () -> acceptOptional(this::throws_))));
	}
	private boolean result() {
		return accept(Nonterminal.RESULT, () -> acceptAny(this::unannType, () -> accept(Terminal.VOID)));
	}
	private boolean methodDeclarator() {
		return accept(Nonterminal.METHOD_DECLARATOR, () -> acceptAll(this::identifier, () -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptOptional(this::formalParameterList), () -> accept(Terminal.CLOSE_PARENTHESIS), () -> acceptOptional(this::dims)));
	}
	private boolean formalParameterList() {
		return accept(Nonterminal.FORMAL_PARAMETER_LIST, () -> acceptAll(this::formalParameters, () -> acceptOptional(() -> acceptAll(() -> accept(Terminal.COMMA), this::lastFormalParameter))));
	}
	private boolean formalParameters() {
		return accept(Nonterminal.FORMAL_PARAMETERS, () -> acceptAny(() -> acceptAll(this::formalParameter, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.COMMA), this::formalParameter))), () -> acceptAll(this::receiverParameter, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.COMMA), this::formalParameter)))));
	}
	private boolean formalParameter() {
		return accept(Nonterminal.FORMAL_PARAMETER, () -> acceptAll(() -> acceptRepeating(this::variableModifier), this::unannType, this::variableDeclaratorId));
	}
	private boolean variableModifier() {
		return accept(Nonterminal.VARIABLE_MODIFIER, () -> acceptAny(this::annotation, () -> accept(Terminal.FINAL)));
	}
	private boolean lastFormalParameter() {
		return accept(Nonterminal.LAST_FORMAL_PARAMETER, () -> acceptAny(() -> acceptAll(() -> acceptRepeating(this::variableModifier), this::unannType, () -> acceptRepeating(this::annotation), () -> accept(Terminal.ELLIPSES), this::variableDeclaratorId), this::formalParameter));
	}
	private boolean receiverParameter() {
		return accept(Nonterminal.RECEIVER_PARAMETER, () -> acceptAll(() -> acceptRepeating(this::annotation), this::unannType, () -> acceptOptional(() -> acceptAll(this::identifier, () -> accept(Terminal.DOT))), () -> accept(Terminal.THIS)));
	}
	private boolean throws_() {
		return accept(Nonterminal.THROWS, () -> acceptAll(() -> accept(Terminal.THROWS), this::exceptionTypeList));
	}
	private boolean exceptionTypeList() {
		return accept(Nonterminal.EXCEPTION_TYPE_LIST, () -> acceptAll(this::exceptionType, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.COMMA), this::exceptionType))));
	}
	private boolean exceptionType() {
		return accept(Nonterminal.EXCEPTION_TYPE, () -> acceptAny(this::classType, this::typeVariable));
	}
	private boolean methodBody() {
		return accept(Nonterminal.METHOD_BODY, () -> acceptAny(this::block, () -> accept(Terminal.SEMICOLON)));
	}
	private boolean instanceInitializer() {
		return accept(Nonterminal.INSTANCE_INITIALIZER, this::block);
	}
	private boolean staticInitializer() {
		return accept(Nonterminal.STATIC_INITIALIZER, () -> acceptAll(() -> accept(Terminal.STATIC), this::block));
	}
	private boolean constructorDeclaration() {
		return accept(Nonterminal.CONSTRUCTOR_DECLARATION, () -> acceptAll(() -> acceptRepeating(this::constructorModifier), this::constructorDeclarator, () -> acceptOptional(this::throws_), this::constructorBody));
	}
	private boolean constructorModifier() {
		return accept(Nonterminal.CONSTRUCTOR_MODIFIER, () -> acceptAny(this::annotation, () -> accept(Terminal.PUBLIC), () -> accept(Terminal.PROTECTED), () -> accept(Terminal.PRIVATE)));
	}
	private boolean constructorDeclarator() {
		return accept(Nonterminal.CONSTRUCTOR_DECLARATOR, () -> acceptAll(() -> acceptOptional(this::typeParameters), this::simpleTypeName, () -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptOptional(this::formalParameterList), () -> accept(Terminal.CLOSE_PARENTHESIS)));
	}
	private boolean simpleTypeName() {
		return accept(Nonterminal.SIMPLE_TYPE_NAME, this::identifier);
	}
	private boolean constructorBody() {
		return accept(Nonterminal.CONSTRUCTOR_BODY, () -> acceptAll(() -> accept(Terminal.OPEN_BRACE), () -> acceptOptional(this::explicitConstructorInvocation), () -> acceptOptional(this::blockStatements), () -> accept(Terminal.CLOSE_BRACE)));
	}
	private boolean explicitConstructorInvocation() {
		return accept(Nonterminal.EXPLICIT_CONSTRUCTOR_INVOCATION, () -> acceptAny(() -> acceptAll(() -> acceptOptional(this::typeArguments), () -> accept(Terminal.THIS), () -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptOptional(this::argumentList), () -> accept(Terminal.CLOSE_PARENTHESIS), () -> accept(Terminal.SEMICOLON)), () -> acceptAll(() -> acceptOptional(this::typeArguments), () -> accept(Terminal.SUPER), () -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptOptional(this::argumentList), () -> accept(Terminal.CLOSE_PARENTHESIS), () -> accept(Terminal.SEMICOLON)), () -> acceptAll(this::expressionName, () -> accept(Terminal.DOT), () -> acceptOptional(this::typeArguments), () -> accept(Terminal.SUPER), () -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptOptional(this::argumentList), () -> accept(Terminal.CLOSE_PARENTHESIS), () -> accept(Terminal.SEMICOLON)), () -> acceptAll(this::primary, () -> accept(Terminal.DOT), () -> acceptOptional(this::typeArguments), () -> accept(Terminal.SUPER), () -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptOptional(this::argumentList), () -> accept(Terminal.CLOSE_PARENTHESIS), () -> accept(Terminal.SEMICOLON))));
	}
	private boolean enumDeclaration() {
		return accept(Nonterminal.ENUM_DECLARATION, () -> acceptAll(() -> acceptRepeating(this::classModifier), () -> accept(Terminal.ENUM), this::identifier, () -> acceptOptional(this::superinterfaces), this::enumBody));
	}
	private boolean enumBody() {
		return accept(Nonterminal.ENUM_BODY, () -> acceptAll(() -> accept(Terminal.OPEN_BRACE), () -> acceptOptional(this::enumConstantList), () -> acceptOptional(() -> accept(Terminal.COMMA)), () -> acceptOptional(this::enumBodyDeclarations), () -> accept(Terminal.CLOSE_BRACE)));
	}
	private boolean enumConstantList() {
		return accept(Nonterminal.ENUM_CONSTANT_LIST, () -> acceptAll(this::enumConstant, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.COMMA), this::enumConstant))));
	}
	private boolean enumConstant() {
		return accept(Nonterminal.ENUM_CONSTANT, () -> acceptAll(() -> acceptRepeating(this::enumConstantModifier), this::identifier, () -> acceptOptional(() -> acceptAll(() -> accept(Terminal.OPEN_PARENTHESIS), this::argumentList, () -> accept(Terminal.CLOSE_PARENTHESIS))), () -> acceptOptional(this::classBody)));
	}
	private boolean enumConstantModifier() {
		return accept(Nonterminal.ENUM_CONSTANT_MODIFIER, this::annotation);
	}
	private boolean enumBodyDeclarations() {
		return accept(Nonterminal.ENUM_BODY_DECLARATIONS, () -> acceptAll(() -> accept(Terminal.SEMICOLON), () -> acceptRepeating(this::classBodyDeclaration)));
	}
	private boolean interfaceDeclaration() {
		return accept(Nonterminal.INTERFACE_DECLARATION, () -> acceptAny(this::normalInterfaceDeclaration, this::annotationTypeDeclaration));
	}
	private boolean normalInterfaceDeclaration() {
		return accept(Nonterminal.NORMAL_INTERFACE_DECLARATION, () -> acceptAll(() -> acceptRepeating(this::interfaceModifier), () -> accept(Terminal.INTERFACE), this::identifier, () -> acceptOptional(this::typeParameters), () -> acceptOptional(this::extendsInterfaces), this::interfaceBody));
	}
	private boolean interfaceModifier() {
		return accept(Nonterminal.INTERFACE_MODIFIER, () -> acceptAny(this::annotation, () -> accept(Terminal.PUBLIC), () -> accept(Terminal.PROTECTED), () -> accept(Terminal.PRIVATE), () -> accept(Terminal.ABSTRACT), () -> accept(Terminal.STATIC), () -> accept(Terminal.STRICTFP)));
	}
	private boolean extendsInterfaces() {
		return accept(Nonterminal.EXTENDS_INTERFACES, () -> acceptAll(() -> accept(Terminal.EXTENDS), this::interfaceTypeList));
	}
	private boolean interfaceBody() {
		return accept(Nonterminal.INTERFACE_BODY, () -> acceptAll(() -> accept(Terminal.OPEN_BRACE), () -> acceptRepeating(this::interfaceMemberDeclaration), () -> accept(Terminal.CLOSE_BRACE)));
	}
	private boolean interfaceMemberDeclaration() {
		return accept(Nonterminal.INTERFACE_MEMBER_DECLARATION, () -> acceptAny(this::constantDeclaration, this::interfaceMethodDeclaration, this::classDeclaration, this::interfaceDeclaration, () -> accept(Terminal.SEMICOLON)));
	}
	private boolean constantDeclaration() {
		return accept(Nonterminal.CONSTANT_DECLARATION, () -> acceptAll(() -> acceptRepeating(this::constantModifier), this::unannType, this::variableDeclaratorList, () -> accept(Terminal.SEMICOLON)));
	}
	private boolean constantModifier() {
		return accept(Nonterminal.CONSTANT_MODIFIER, () -> acceptAny(this::annotation, () -> accept(Terminal.PUBLIC), () -> accept(Terminal.STATIC), () -> accept(Terminal.FINAL)));
	}
	private boolean interfaceMethodDeclaration() {
		return accept(Nonterminal.INTERFACE_METHOD_DECLARATION, () -> acceptAll(() -> acceptRepeating(this::interfaceMethodModifier), this::methodHeader, this::methodBody));
	}
	private boolean interfaceMethodModifier() {
		return accept(Nonterminal.INTERFACE_METHOD_MODIFIER, () -> acceptAny(this::annotation, () -> accept(Terminal.PUBLIC), () -> accept(Terminal.ABSTRACT), () -> accept(Terminal.DEFAULT), () -> accept(Terminal.STATIC), () -> accept(Terminal.STRICTFP)));
	}
	private boolean annotationTypeDeclaration() {
		return accept(Nonterminal.ANNOTATION_TYPE_DECLARATION, () -> acceptAll(() -> acceptRepeating(this::interfaceModifier), () -> accept(Terminal.AT_SYMBOL), () -> accept(Terminal.INTERFACE), this::identifier, this::annotationTypeBody));
	}
	private boolean annotationTypeBody() {
		return accept(Nonterminal.ANNOTATION_TYPE_BODY, () -> acceptAll(() -> accept(Terminal.OPEN_BRACE), () -> acceptRepeating(this::annotationTypeMemberDeclaration), () -> accept(Terminal.CLOSE_BRACE)));
	}
	private boolean annotationTypeMemberDeclaration() {
		return accept(Nonterminal.ANNOTATION_TYPE_MEMBER_DECLARATION, () -> acceptAny(this::annotationTypeElementDeclaration, this::constantDeclaration, this::classDeclaration, this::interfaceDeclaration, () -> accept(Terminal.SEMICOLON)));
	}
	private boolean annotationTypeElementDeclaration() {
		return accept(Nonterminal.ANNOTATION_TYPE_ELEMENT_DECLARATION, () -> acceptAll(() -> acceptRepeating(this::annotationTypeElementModifier), this::unannType, this::identifier, () -> accept(Terminal.OPEN_PARENTHESIS), () -> accept(Terminal.CLOSE_PARENTHESIS), () -> acceptOptional(this::dims), () -> acceptOptional(this::defaultValue), () -> accept(Terminal.SEMICOLON)));
	}
	private boolean annotationTypeElementModifier() {
		return accept(Nonterminal.ANNOTATION_TYPE_ELEMENT_MODIFIER, () -> acceptAny(this::annotation, () -> accept(Terminal.PUBLIC), () -> accept(Terminal.ABSTRACT)));
	}
	private boolean defaultValue() {
		return accept(Nonterminal.DEFAULT_VALUE, () -> acceptAll(() -> accept(Terminal.DEFAULT), this::elementValue));
	}
	private boolean annotation() {
		return accept(Nonterminal.ANNOTATION, () -> acceptAny(this::normalAnnotation, this::markerAnnotation, this::singleElementAnnotation));
	}
	private boolean normalAnnotation() {
		return accept(Nonterminal.NORMAL_ANNOTATION, () -> acceptAll(() -> accept(Terminal.AT_SYMBOL), this::typeName, () -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptOptional(this::elementValuePairList), () -> accept(Terminal.CLOSE_PARENTHESIS)));
	}
	private boolean elementValuePairList() {
		return accept(Nonterminal.ELEMENT_VALUE_PAIR_LIST, () -> acceptAll(this::elementValuePair, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.COMMA), this::elementValuePair))));
	}
	private boolean elementValuePair() {
		return accept(Nonterminal.ELEMENT_VALUE_PAIR, () -> acceptAll(this::identifier, () -> accept(Terminal.ASSIGN), this::elementValue));
	}
	private boolean elementValue() {
		return accept(Nonterminal.ELEMENT_VALUE, () -> acceptAny(this::conditionalExpression, this::elementValueArrayInitializer, this::annotation));
	}
	private boolean elementValueArrayInitializer() {
		return accept(Nonterminal.ELEMENT_VALUE_ARRAY_INITIALIZER, () -> acceptAll(() -> accept(Terminal.OPEN_BRACE), () -> acceptOptional(this::elementValueList), () -> acceptOptional(() -> accept(Terminal.COMMA)), () -> accept(Terminal.CLOSE_BRACE)));
	}
	private boolean elementValueList() {
		return accept(Nonterminal.ELEMENT_VALUE_LIST, () -> acceptAll(this::elementValue, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.COMMA), this::elementValue))));
	}
	private boolean markerAnnotation() {
		return accept(Nonterminal.MARKER_ANNOTATION, () -> acceptAll(() -> accept(Terminal.AT_SYMBOL), this::typeName));
	}
	private boolean singleElementAnnotation() {
		return accept(Nonterminal.SINGLE_ELEMENT_ANNOTATION, () -> acceptAll(() -> accept(Terminal.AT_SYMBOL), this::typeName, () -> accept(Terminal.OPEN_PARENTHESIS), this::elementValue, () -> accept(Terminal.CLOSE_PARENTHESIS)));
	}
	private boolean arrayInitializer() {
		return accept(Nonterminal.ARRAY_INITIALIZER, () -> acceptAll(() -> accept(Terminal.OPEN_BRACE), () -> acceptOptional(this::variableInitializerList), () -> acceptOptional(() -> accept(Terminal.COMMA)), () -> accept(Terminal.CLOSE_BRACE)));
	}
	private boolean variableInitializerList() {
		return accept(Nonterminal.VARIABLE_INITIALIZER_LIST, () -> acceptAll(this::variableInitializer, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.COMMA), this::variableInitializer))));
	}
	private boolean block() {
		return accept(Nonterminal.BLOCK, () -> acceptAll(() -> accept(Terminal.OPEN_BRACE), () -> acceptOptional(this::blockStatements), () -> accept(Terminal.CLOSE_BRACE)));
	}
	private boolean blockStatements() {
		return accept(Nonterminal.BLOCK_STATEMENTS, () -> acceptAll(this::blockStatement, () -> acceptRepeating(this::blockStatement)));
	}
	private boolean blockStatement() {
		return accept(Nonterminal.BLOCK_STATEMENT, () -> acceptAny(this::localVariableDeclarationStatement, this::classDeclaration, this::statement));
	}
	private boolean localVariableDeclarationStatement() {
		return accept(Nonterminal.LOCAL_VARIABLE_DECLARATION_STATEMENT, () -> acceptAll(this::localVariableDeclaration, () -> accept(Terminal.SEMICOLON)));
	}
	private boolean localVariableDeclaration() {
		return accept(Nonterminal.LOCAL_VARIABLE_DECLARATION, () -> acceptAll(() -> acceptRepeating(this::variableModifier), this::unannType, this::variableDeclaratorList));
	}
	private boolean statement() {
		return accept(Nonterminal.STATEMENT, () -> acceptAny(this::statementWithoutTrailingSubstatement, this::labeledStatement, this::ifThenElseStatement, this::ifThenStatement, this::whileStatement, this::forStatement));
	}
	private boolean statementNoShortIf() {
		return accept(Nonterminal.STATEMENT_NO_SHORT_IF, () -> acceptAny(this::statementWithoutTrailingSubstatement, this::labeledStatementNoShortIf, this::ifThenElseStatementNoShortIf, this::whileStatementNoShortIf, this::forStatementNoShortIf));
	}
	private boolean statementWithoutTrailingSubstatement() {
		return accept(Nonterminal.STATEMENT_WITHOUT_TRAILING_SUBSTATEMENT, () -> acceptAny(this::block, this::emptyStatement, this::expressionStatement, this::assertStatement, this::switchStatement, this::doStatement, this::breakStatement, this::continueStatement, this::returnStatement, this::synchronizedStatement, this::throwStatement, this::tryStatement));
	}
	private boolean emptyStatement() {
		return accept(Nonterminal.EMPTY_STATEMENT, () -> accept(Terminal.SEMICOLON));
	}
	private boolean labeledStatement() {
		return accept(Nonterminal.LABELED_STATEMENT, () -> acceptAll(this::identifier, () -> accept(Terminal.COLON), this::statement));
	}
	private boolean labeledStatementNoShortIf() {
		return accept(Nonterminal.LABELED_STATEMENT_NO_SHORT_IF, () -> acceptAll(this::identifier, () -> accept(Terminal.COLON), this::statementNoShortIf));
	}
	private boolean expressionStatement() {
		return accept(Nonterminal.EXPRESSION_STATEMENT, () -> acceptAll(this::statementExpression, () -> accept(Terminal.SEMICOLON)));
	}
	private boolean statementExpression() {
		return accept(Nonterminal.STATEMENT_EXPRESSION, () -> acceptAny(this::assignment, this::preIncrementExpression, this::preDecrementExpression, this::postIncrementExpression, this::postDecrementExpression, this::methodInvocation, this::classInstanceCreationExpression));
	}
	private boolean ifThenStatement() {
		return accept(Nonterminal.IF_THEN_STATEMENT, () -> acceptAll(() -> accept(Terminal.IF), () -> accept(Terminal.OPEN_PARENTHESIS), this::expression, () -> accept(Terminal.CLOSE_PARENTHESIS), this::statement));
	}
	private boolean ifThenElseStatement() {
		return accept(Nonterminal.IF_THEN_ELSE_STATEMENT, () -> acceptAll(() -> accept(Terminal.IF), () -> accept(Terminal.OPEN_PARENTHESIS), this::expression, () -> accept(Terminal.CLOSE_PARENTHESIS), this::statementNoShortIf, () -> accept(Terminal.ELSE), this::statement));
	}
	private boolean ifThenElseStatementNoShortIf() {
		return accept(Nonterminal.IF_THEN_ELSE_STATEMENT_NO_SHORT_IF, () -> acceptAll(() -> accept(Terminal.IF), () -> accept(Terminal.OPEN_PARENTHESIS), this::expression, () -> accept(Terminal.CLOSE_PARENTHESIS), this::statementNoShortIf, () -> accept(Terminal.ELSE), this::statementNoShortIf));
	}
	private boolean assertStatement() {
		return accept(Nonterminal.ASSERT_STATEMENT, () -> acceptAny(() -> acceptAll(() -> accept(Terminal.ASSERT), this::expression, () -> accept(Terminal.SEMICOLON)), () -> acceptAll(() -> accept(Terminal.ASSERT), this::expression, () -> accept(Terminal.COLON), this::expression)));
	}
	private boolean switchStatement() {
		return accept(Nonterminal.SWITCH_STATEMENT, () -> acceptAll(() -> accept(Terminal.SWITCH), () -> accept(Terminal.OPEN_PARENTHESIS), this::expression, () -> accept(Terminal.CLOSE_PARENTHESIS), this::switchBlock));
	}
	private boolean switchBlock() {
		return accept(Nonterminal.SWITCH_BLOCK, () -> acceptAll(() -> accept(Terminal.OPEN_BRACE), () -> acceptRepeating(this::switchBlockStatementGroup), () -> acceptRepeating(this::switchLabel), () -> accept(Terminal.CLOSE_BRACE)));
	}
	private boolean switchBlockStatementGroup() {
		return accept(Nonterminal.SWITCH_BLOCK_STATEMENT_GROUP, () -> acceptAll(this::switchLabels, this::blockStatements));
	}
	private boolean switchLabels() {
		return accept(Nonterminal.SWITCH_LABELS, () -> acceptAll(this::switchLabel, () -> acceptRepeating(this::switchLabel)));
	}
	private boolean switchLabel() {
		return accept(Nonterminal.SWITCH_LABEL, () -> acceptAny(() -> acceptAll(() -> accept(Terminal.CASE), this::constantExpression, () -> accept(Terminal.COLON)), () -> acceptAll(() -> accept(Terminal.CASE), this::enumConstantName, () -> accept(Terminal.COLON)), () -> acceptAll(() -> accept(Terminal.DEFAULT), () -> accept(Terminal.COLON))));
	}
	private boolean enumConstantName() {
		return accept(Nonterminal.ENUM_CONSTANT_NAME, this::identifier);
	}
	private boolean whileStatement() {
		return accept(Nonterminal.WHILE_STATEMENT, () -> acceptAll(() -> accept(Terminal.WHILE), () -> accept(Terminal.OPEN_PARENTHESIS), this::expression, () -> accept(Terminal.CLOSE_PARENTHESIS), this::statement));
	}
	private boolean whileStatementNoShortIf() {
		return accept(Nonterminal.WHILE_STATEMENT_NO_SHORT_IF, () -> acceptAll(() -> accept(Terminal.WHILE), () -> accept(Terminal.OPEN_PARENTHESIS), this::expression, () -> accept(Terminal.CLOSE_PARENTHESIS), this::statementNoShortIf));
	}
	private boolean doStatement() {
		return accept(Nonterminal.DO_STATEMENT, () -> acceptAll(() -> accept(Terminal.DO), this::statement, () -> accept(Terminal.WHILE), () -> accept(Terminal.OPEN_PARENTHESIS), this::expression, () -> accept(Terminal.CLOSE_PARENTHESIS), () -> accept(Terminal.SEMICOLON)));
	}
	private boolean forStatement() {
		return accept(Nonterminal.FOR_STATEMENT, () -> acceptAny(this::basicForStatement, this::enhancedForStatement));
	}
	private boolean forStatementNoShortIf() {
		return accept(Nonterminal.FOR_STATEMENT_NO_SHORT_IF, () -> acceptAny(this::basicForStatementNoShortIf, this::enhancedForStatementNoShortIf));
	}
	private boolean basicForStatement() {
		return accept(Nonterminal.BASIC_FOR_STATEMENT, () -> acceptAll(() -> accept(Terminal.FOR), () -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptOptional(this::forInit), () -> accept(Terminal.SEMICOLON), () -> acceptOptional(this::expression), () -> accept(Terminal.SEMICOLON), () -> acceptOptional(this::forUpdate), () -> accept(Terminal.CLOSE_PARENTHESIS), this::statement));
	}
	private boolean basicForStatementNoShortIf() {
		return accept(Nonterminal.BASIC_FOR_STATEMENT_NO_SHORT_IF, () -> acceptAll(() -> accept(Terminal.FOR), () -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptOptional(this::forInit), () -> accept(Terminal.SEMICOLON), () -> acceptOptional(this::expression), () -> accept(Terminal.SEMICOLON), () -> acceptOptional(this::forUpdate), () -> accept(Terminal.CLOSE_PARENTHESIS), this::statementNoShortIf));
	}
	private boolean forInit() {
		return accept(Nonterminal.FOR_INIT, () -> acceptAny(this::statementExpressionList, this::localVariableDeclaration));
	}
	private boolean forUpdate() {
		return accept(Nonterminal.FOR_UPDATE, this::statementExpressionList);
	}
	private boolean statementExpressionList() {
		return accept(Nonterminal.STATEMENT_EXPRESSION_LIST, () -> acceptAll(this::statementExpression, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.COMMA), this::statementExpression))));
	}
	private boolean enhancedForStatement() {
		return accept(Nonterminal.ENHANCED_FOR_STATEMENT, () -> acceptAll(() -> accept(Terminal.FOR), () -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptRepeating(this::variableModifier), this::unannType, this::variableDeclaratorId, () -> accept(Terminal.COLON), this::expression, () -> accept(Terminal.CLOSE_PARENTHESIS), this::statement));
	}
	private boolean enhancedForStatementNoShortIf() {
		return accept(Nonterminal.ENHANCED_FOR_STATEMENT_NO_SHORT_IF, () -> acceptAll(() -> accept(Terminal.FOR), () -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptRepeating(this::variableModifier), this::unannType, this::variableDeclaratorId, () -> accept(Terminal.COLON), this::expression, () -> accept(Terminal.CLOSE_PARENTHESIS), this::statementNoShortIf));
	}
	private boolean breakStatement() {
		return accept(Nonterminal.BREAK_STATEMENT, () -> acceptAll(() -> accept(Terminal.BREAK), () -> acceptOptional(this::identifier), () -> accept(Terminal.SEMICOLON)));
	}
	private boolean continueStatement() {
		return accept(Nonterminal.CONTINUE_STATEMENT, () -> acceptAll(() -> accept(Terminal.CONTINUE), () -> acceptOptional(this::identifier), () -> accept(Terminal.SEMICOLON)));
	}
	private boolean returnStatement() {
		return accept(Nonterminal.RETURN_STATEMENT, () -> acceptAll(() -> accept(Terminal.RETURN), () -> acceptOptional(this::expression), () -> accept(Terminal.SEMICOLON)));
	}
	private boolean throwStatement() {
		return accept(Nonterminal.THROW_STATEMENT, () -> acceptAll(() -> accept(Terminal.THROW), this::expression, () -> accept(Terminal.SEMICOLON)));
	}
	private boolean synchronizedStatement() {
		return accept(Nonterminal.SYNCHRONIZED_STATEMENT, () -> acceptAll(() -> accept(Terminal.SYNCHRONIZED), () -> accept(Terminal.OPEN_PARENTHESIS), this::expression, () -> accept(Terminal.CLOSE_PARENTHESIS), this::block));
	}
	private boolean tryStatement() {
		return accept(Nonterminal.TRY_STATEMENT, () -> acceptAny(() -> acceptAll(() -> accept(Terminal.TRY), this::block, () -> acceptOptional(this::catches), this::finally_), () -> acceptAll(() -> accept(Terminal.TRY), this::block, this::catches), this::tryWithResourcesStatement));
	}
	private boolean catches() {
		return accept(Nonterminal.CATCHES, () -> acceptAll(this::catchClause, () -> acceptRepeating(this::catchClause)));
	}
	private boolean catchClause() {
		return accept(Nonterminal.CATCH_CLAUSE, () -> acceptAll(() -> accept(Terminal.CATCH), () -> accept(Terminal.OPEN_PARENTHESIS), this::catchFormalParameter, () -> accept(Terminal.CLOSE_PARENTHESIS), this::block));
	}
	private boolean catchFormalParameter() {
		return accept(Nonterminal.CATCH_FORMAL_PARAMETER, () -> acceptAll(() -> acceptRepeating(this::variableModifier), this::catchType, this::variableDeclaratorId));
	}
	private boolean catchType() {
		return accept(Nonterminal.CATCH_TYPE, () -> acceptAll(this::unannClassType, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.OR), this::classType))));
	}
	private boolean finally_() {
		return accept(Nonterminal.FINALLY, () -> acceptAll(() -> accept(Terminal.FINALLY), this::block));
	}
	private boolean tryWithResourcesStatement() {
		return accept(Nonterminal.TRY_WITH_RESOURCES_STATEMENT, () -> acceptAll(() -> accept(Terminal.TRY), this::resourceSpecification, this::block, () -> acceptOptional(this::catches), () -> acceptOptional(this::finally_)));
	}
	private boolean resourceSpecification() {
		return accept(Nonterminal.RESOURCE_SPECIFICATION, () -> acceptAll(() -> accept(Terminal.OPEN_PARENTHESIS), this::resourceList, () -> acceptOptional(() -> accept(Terminal.SEMICOLON)), () -> accept(Terminal.CLOSE_PARENTHESIS)));
	}
	private boolean resourceList() {
		return accept(Nonterminal.RESOURCE_LIST, () -> acceptAll(this::resource, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.SEMICOLON), this::resource))));
	}
	private boolean resource() {
		return accept(Nonterminal.RESOURCE, () -> acceptAll(() -> acceptRepeating(this::variableModifier), this::unannType, this::variableDeclaratorId, () -> accept(Terminal.ASSIGN), this::expression));
	}
	private boolean primary() {
		return accept(Nonterminal.PRIMARY, () -> acceptAny(this::primaryNoNewArray, this::arrayCreationExpression));
	}
	private boolean primaryNoNewArray() {
		if (!(
			   check(TokenType.LITERAL)
			|| check(Terminal.IDENTIFIER)
			|| lookAhead(Terminal.BYTE, Terminal.DOT)
			|| lookAhead(Terminal.SHORT, Terminal.DOT)
			|| lookAhead(Terminal.INT, Terminal.DOT)
			|| lookAhead(Terminal.LONG, Terminal.DOT)
			|| lookAhead(Terminal.CHAR, Terminal.DOT)
			|| lookAhead(Terminal.FLOAT, Terminal.DOT)
			|| lookAhead(Terminal.DOUBLE, Terminal.DOT)
			|| lookAhead(Terminal.BOOLEAN, Terminal.DOT)
			|| check(Terminal.VOID)
			|| check(Terminal.THIS)
			|| check(Terminal.OPEN_PARENTHESIS)
			|| check(Terminal.NEW)
			|| check(Terminal.SUPER)
			|| check(Terminal.AT_SYMBOL)
		)) return false;

		return accept(Nonterminal.PRIMARY_NO_NEW_ARRAY,
			() -> acceptAny(
				this::literal,
				this::classLiteral,
				() -> accept(Terminal.THIS),
				() -> acceptAll(
					this::typeName,
					() -> accept(Terminal.DOT),
					() -> accept(Terminal.THIS)
				),
				() -> acceptAll(
					() -> accept(Terminal.OPEN_PARENTHESIS),
					this::expression,
					() -> accept(Terminal.CLOSE_PARENTHESIS)
				),
				this::classInstanceCreationExpression,
				this::fieldAccess,
				this::arrayAccess,
				this::methodInvocation,
				this::methodReference
			)
		);
	}
	private boolean classLiteral() {
		return accept(Nonterminal.CLASS_LITERAL,
			() -> acceptAny(
				() -> acceptAll(
					this::typeName,
					() -> acceptRepeating(
						() -> acceptAll(
							() -> accept(Terminal.OPEN_BRACKET),
							() -> accept(Terminal.CLOSE_BRACKET)
						)
					),
					() -> accept(Terminal.DOT),
					() -> accept(Terminal.CLASS)
				),
				() -> acceptAll(
					this::numericType,
					() -> acceptRepeating(
						() -> acceptAll(() -> accept(Terminal.OPEN_BRACKET), () -> accept(Terminal.CLOSE_BRACKET))), () -> accept(Terminal.DOT), () -> accept(Terminal.CLASS)), () -> acceptAll(() -> accept(Terminal.BOOLEAN), () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.OPEN_BRACKET), () -> accept(Terminal.CLOSE_BRACKET))), () -> accept(Terminal.DOT), () -> accept(Terminal.CLASS)), () -> acceptAll(() -> accept(Terminal.VOID), () -> accept(Terminal.DOT), () -> accept(Terminal.CLASS))));
	}
	private boolean classInstanceCreationExpression() {
		return accept(Nonterminal.CLASS_INSTANCE_CREATION_EXPRESSION, () -> acceptAny(this::unqualifiedClassInstanceCreationExpression, () -> acceptAll(this::expressionName, () -> accept(Terminal.DOT), this::unqualifiedClassInstanceCreationExpression), () -> acceptAll(this::primary, () -> accept(Terminal.DOT), this::unqualifiedClassInstanceCreationExpression)));
	}
	private boolean unqualifiedClassInstanceCreationExpression() {
		return accept(Nonterminal.UNQUALIFIED_CLASS_INSTANCE_CREATION_EXPRESSION, () -> acceptAll(() -> accept(Terminal.NEW), () -> acceptOptional(this::typeArguments), this::classOrInterfaceTypeToInstantiate, () -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptOptional(this::argumentList), () -> accept(Terminal.CLOSE_PARENTHESIS), () -> acceptOptional(this::classBody)));
	}
	private boolean classOrInterfaceTypeToInstantiate() {
		return accept(Nonterminal.CLASS_OR_INTERFACE_TYPE_TO_INSTANTIATE, () -> acceptAll(() -> acceptRepeating(this::annotation), this::identifier, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.DOT), () -> acceptRepeating(this::annotation), this::identifier)), () -> acceptOptional(this::typeArgumentsOrDiamond)));
	}
	private boolean typeArgumentsOrDiamond() {
		return accept(Nonterminal.TYPE_ARGUMENTS_OR_DIAMOND, () -> acceptAny(this::typeArguments, () -> acceptAll(() -> accept(Terminal.LESS_THAN), () -> accept(Terminal.GREATER_THAN))));
	}
	private boolean fieldAccess() {
		return accept(Nonterminal.FIELD_ACCESS,
			() -> acceptAny(
				() -> acceptAll(
					this::primary,
					() -> accept(Terminal.DOT),
					this::identifier
				),
				() -> acceptAll(
					() -> accept(Terminal.SUPER),
					() -> accept(Terminal.DOT),
					this::identifier
				),
				() -> acceptAll(
					this::typeName,
					() -> accept(Terminal.DOT),
					() -> accept(Terminal.SUPER),
					() -> accept(Terminal.DOT),
					this::identifier
				)
			)
		);
	}
	private boolean arrayAccess() {
		return accept(Nonterminal.ARRAY_ACCESS,
			() -> acceptAny(
				() -> acceptAll(
					this::expressionName,
					() -> accept(Terminal.OPEN_BRACKET),
					this::expression,
					() -> accept(Terminal.CLOSE_BRACKET)
				),
				() -> acceptAll(
					this::primaryNoNewArray,
					() -> accept(Terminal.OPEN_BRACKET),
					this::expression,
					() -> accept(Terminal.CLOSE_BRACKET)
				)
			)
		);
	}
	private boolean methodInvocation() {
		return accept(Nonterminal.METHOD_INVOCATION, () -> acceptAny(
			() -> acceptAll(this::methodName, () -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptOptional(this::argumentList), () -> accept(Terminal.CLOSE_PARENTHESIS)),
			() -> acceptAll(this::typeName, () -> accept(Terminal.DOT), () -> acceptOptional(this::typeArguments), this::identifier, () -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptOptional(this::argumentList), () -> accept(Terminal.CLOSE_PARENTHESIS)),
			() -> {
				final boolean acceptedExpressionName = accept(Nonterminal.EXPRESSION_NAME, () -> {
					boolean any = false;
					while (acceptAll(this::identifier, () -> accept(Terminal.DOT)))
						if (!any) any = true;
					return any;
				});
				if (!acceptedExpressionName) {
					return false;
				}
				NonterminalNode expressionName = currentParent.getNonterminalChild(currentParent.size() - 1);
				TerminalNode dot = expressionName.getTerminalChild(expressionName.size() - 1);
				expressionName.remove(expressionName.size() - 1);
				currentParent.add(dot);
				return acceptAll(() -> acceptOptional(this::typeArguments), this::identifier, () -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptOptional(this::argumentList), () -> accept(Terminal.CLOSE_PARENTHESIS));
			},
			() -> acceptAll(this::primary, () -> accept(Terminal.DOT), () -> acceptOptional(this::typeArguments), this::identifier, () -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptOptional(this::argumentList), () -> accept(Terminal.CLOSE_PARENTHESIS)),
			() -> acceptAll(() -> accept(Terminal.SUPER), () -> accept(Terminal.DOT), () -> acceptOptional(this::typeArguments), this::identifier, () -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptOptional(this::argumentList), () -> accept(Terminal.CLOSE_PARENTHESIS)),
			() -> acceptAll(this::typeName, () -> accept(Terminal.DOT), () -> accept(Terminal.SUPER), () -> accept(Terminal.DOT), () -> acceptOptional(this::typeArguments), this::identifier, () -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptOptional(this::argumentList), () -> accept(Terminal.CLOSE_PARENTHESIS))));
	}
	private boolean argumentList() {
		return accept(Nonterminal.ARGUMENT_LIST, () -> acceptAll(this::expression, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.COMMA), this::expression))));
	}
	private boolean methodReference() {
		return accept(Nonterminal.METHOD_REFERENCE, () -> acceptAny(() -> acceptAll(this::expressionName, () -> accept(Terminal.DOUBLE_COLON), () -> acceptOptional(this::typeArguments), this::identifier), () -> acceptAll(this::referenceType, () -> accept(Terminal.DOUBLE_COLON), () -> acceptOptional(this::typeArguments), this::identifier), () -> acceptAll(this::primary, () -> accept(Terminal.DOUBLE_COLON), () -> acceptOptional(this::typeArguments), this::identifier), () -> acceptAll(() -> accept(Terminal.SUPER), () -> accept(Terminal.DOUBLE_COLON), () -> acceptOptional(this::typeArguments), this::identifier), () -> acceptAll(this::typeName, () -> accept(Terminal.DOT), () -> accept(Terminal.SUPER), () -> accept(Terminal.DOUBLE_COLON), () -> acceptOptional(this::typeArguments), this::identifier), () -> acceptAll(this::classType, () -> accept(Terminal.DOUBLE_COLON), () -> acceptOptional(this::typeArguments), () -> accept(Terminal.NEW)), () -> acceptAll(this::arrayType, () -> accept(Terminal.DOUBLE_COLON), () -> accept(Terminal.NEW))));
	}
	private boolean arrayCreationExpression() {
		return accept(Nonterminal.ARRAY_CREATION_EXPRESSION, () -> acceptAny(() -> acceptAll(() -> accept(Terminal.NEW), this::primitiveType, this::dimExprs, () -> acceptOptional(this::dims)), () -> acceptAll(() -> accept(Terminal.NEW), this::classOrInterfaceType, this::dimExprs, () -> acceptOptional(this::dims)), () -> acceptAll(() -> accept(Terminal.NEW), this::primitiveType, this::dims, this::arrayInitializer), () -> acceptAll(() -> accept(Terminal.NEW), this::classOrInterfaceType, this::dims, this::arrayInitializer)));
	}
	private boolean dimExprs() {
		return accept(Nonterminal.DIM_EXPRS, () -> acceptAll(this::dimExpr, () -> acceptRepeating(this::dimExpr)));
	}
	private boolean dimExpr() {
		return accept(Nonterminal.DIM_EXPR, () -> acceptAll(() -> acceptRepeating(this::annotation), () -> accept(Terminal.OPEN_BRACKET), this::expression, () -> accept(Terminal.CLOSE_BRACKET)));
	}
	private boolean expression() {
		return accept(Nonterminal.EXPRESSION, () -> acceptAny(this::lambdaExpression, this::assignmentExpression));
	}
	private boolean lambdaExpression() {
		return accept(Nonterminal.LAMBDA_EXPRESSION, () -> acceptAll(this::lambdaParameters, () -> accept(Terminal.LAMBDA), this::lambdaBody));
	}
	private boolean lambdaParameters() {
		return accept(Nonterminal.LAMBDA_PARAMETERS, () -> acceptAny(this::identifier, () -> acceptAll(() -> accept(Terminal.OPEN_PARENTHESIS), () -> acceptOptional(this::formalParameterList), () -> accept(Terminal.CLOSE_PARENTHESIS)), () -> acceptAll(() -> accept(Terminal.OPEN_PARENTHESIS), this::inferredFormalParameterList, () -> accept(Terminal.CLOSE_PARENTHESIS))));
	}
	private boolean inferredFormalParameterList() {
		return accept(Nonterminal.INFERRED_FORMAL_PARAMETER_LIST, () -> acceptAll(this::identifier, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.COMMA), this::identifier))));
	}
	private boolean lambdaBody() {
		return accept(Nonterminal.LAMBDA_BODY, () -> acceptAny(this::expression, this::block));
	}
	private boolean assignmentExpression() {
		return accept(Nonterminal.ASSIGNMENT_EXPRESSION, () -> acceptAny(this::conditionalOrExpression, this::assignment));
	}
	private boolean assignment() {
		return accept(Nonterminal.ASSIGNMENT, () -> acceptAll(this::leftHandSide, this::assignmentOperator, this::expression));
	}
	private boolean leftHandSide() {
		return accept(Nonterminal.LEFT_HAND_SIDE, () -> acceptAny(this::expressionName, this::fieldAccess, this::arrayAccess));
	}
	private boolean assignmentOperator() {
		return accept(Nonterminal.ASSIGNMENT_OPERATOR, () -> acceptAny(() -> accept(Terminal.ASSIGN), () -> accept(Terminal.ASSIGN_MULTIPLY), () -> accept(Terminal.ASSIGN_DIVIDE), () -> accept(Terminal.ASSIGN_MOD), () -> accept(Terminal.ASSIGN_ADD), () -> accept(Terminal.ASSIGN_SUBTRACT), () -> accept(Terminal.ASSIGN_LEFT_SHIFT), () -> accept(Terminal.ASSIGN_RIGHT_SHIFT), () -> accept(Terminal.ASSIGN_UNSIGNED_RIGHT_SHIFT), () -> accept(Terminal.ASSIGN_AND), () -> accept(Terminal.ASSIGN_XOR), () -> accept(Terminal.ASSIGN_OR)));
	}
	private boolean conditionalExpression() {
		return accept(Nonterminal.CONDITIONAL_EXPRESSION, () -> acceptAny(() -> acceptAll(this::conditionalOrExpression, () -> accept(Terminal.TERNARY), this::expression, () -> accept(Terminal.COLON), this::conditionalExpression), () -> acceptAll(this::conditionalOrExpression, () -> accept(Terminal.TERNARY), this::expression, () -> accept(Terminal.COLON), this::lambdaExpression), this::conditionalOrExpression));
	}
	private boolean conditionalOrExpression() {
		return accept(Nonterminal.CONDITIONAL_OR_EXPRESSION, () -> acceptAll(this::conditionalAndExpression, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.OR_GATE), this::conditionalAndExpression))));
	}
	private boolean conditionalAndExpression() {
		return accept(Nonterminal.CONDITIONAL_AND_EXPRESSION, () -> acceptAll(this::inclusiveOrExpression, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.AND_GATE), this::inclusiveOrExpression))));
	}
	private boolean inclusiveOrExpression() {
		return accept(Nonterminal.INCLUSIVE_OR_EXPRESSION, () -> acceptAll(this::exclusiveOrExpression, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.OR), this::exclusiveOrExpression))));
	}
	private boolean exclusiveOrExpression() {
		return accept(Nonterminal.EXCLUSIVE_OR_EXPRESSION, () -> acceptAll(this::andExpression, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.XOR), this::andExpression))));
	}
	private boolean andExpression() {
		return accept(Nonterminal.AND_EXPRESSION, () -> acceptAll(this::equalityExpression, () -> acceptRepeating(() -> acceptAll(() -> accept(Terminal.AND), this::equalityExpression))));
	}
	private boolean equalityExpression() {
		return accept(Nonterminal.EQUALITY_EXPRESSION, () -> acceptAll(this::relationalExpression, () -> acceptRepeating(() -> acceptAll(() -> acceptAny(() -> accept(Terminal.EQUAL_TO), () -> accept(Terminal.NOT_EQUAL_TO)), this::relationalExpression))));
	}
	private boolean relationalExpression() {
		return accept(Nonterminal.RELATIONAL_EXPRESSION,
			() -> acceptAny(
				() -> acceptAll(
					this::shiftExpression,
					() -> accept(Terminal.INSTANCEOF),
					this::referenceType
				),
				() -> acceptAll(
					this::shiftExpression,
					() -> acceptRepeating(() ->
						acceptAll(
							() -> acceptAny(
								() -> accept(Terminal.LESS_THAN),
								() -> accept(Terminal.GREATER_THAN),
								() -> accept(Terminal.LESS_THAN_OR_EQUAL_TO),
								() -> accept(Terminal.GREATER_THAN_OR_EQUAL_TO)
							),
							this::shiftExpression
						)
					)
				)
			)
		);
	}
	private boolean shiftExpression() {
		return accept(Nonterminal.SHIFT_EXPRESSION, () -> acceptAll(this::additiveExpression, () -> acceptRepeating(() -> acceptAll(() -> acceptAny(() -> accept(Terminal.LEFT_SHIFT), () -> accept(Terminal.RIGHT_SHIFT), () -> accept(Terminal.UNSIGNED_RIGHT_SHIFT)), this::additiveExpression))));
	}
	private boolean additiveExpression() {
		return accept(Nonterminal.ADDITIVE_EXPRESSION, () -> acceptAll(this::multiplicativeExpression, () -> acceptRepeating(() -> acceptAll(() -> acceptAny(() -> accept(Terminal.ADD), () -> accept(Terminal.SUBTRACT)), this::multiplicativeExpression))));
	}
	private boolean multiplicativeExpression() {
		return accept(Nonterminal.MULTIPLICATIVE_EXPRESSION,
			() -> acceptAll(this::unaryExpression,
				() -> acceptRepeating(
					() -> acceptAll(
						() -> acceptAny(
							() -> accept(Terminal.MULTIPLY),
							() -> accept(Terminal.DIVIDE),
							() -> accept(Terminal.MODULO)
						),
						this::unaryExpression
					)
				)
			)
		);
	}
	private boolean unaryExpression() {
		return accept(Nonterminal.UNARY_EXPRESSION, () -> acceptAny(this::preIncrementExpression, this::preDecrementExpression, () -> acceptAll(() -> accept(Terminal.ADD), this::unaryExpression), () -> acceptAll(() -> accept(Terminal.SUBTRACT), this::unaryExpression), this::unaryExpressionNotPlusMinus));
	}
	private boolean preIncrementExpression() {
		return accept(Nonterminal.PRE_INCREMENT_EXPRESSION, () -> acceptAll(() -> accept(Terminal.INCREMENT), this::unaryExpression));
	}
	private boolean preDecrementExpression() {
		return accept(Nonterminal.PRE_DECREMENT_EXPRESSION, () -> acceptAll(() -> accept(Terminal.DECREMENT), this::unaryExpression));
	}
	private boolean unaryExpressionNotPlusMinus() {
		return accept(Nonterminal.UNARY_EXPRESSION_NOT_PLUS_MINUS, () -> acceptAny(this::postfixExpression, () -> acceptAll(() -> accept(Terminal.BITWISE_COMPLEMENT), this::unaryExpression), () -> acceptAll(() -> accept(Terminal.NOT), this::unaryExpression), this::castExpression));
	}
	private boolean postfixExpression() {
		if (!(
			   check(TokenType.LITERAL)
			|| check(Terminal.IDENTIFIER)
			|| check(Terminal.BYTE)
			|| check(Terminal.SHORT)
			|| check(Terminal.INT)
			|| check(Terminal.LONG)
			|| check(Terminal.CHAR)
			|| check(Terminal.FLOAT)
			|| check(Terminal.DOUBLE)
			|| check(Terminal.BOOLEAN)
			|| check(Terminal.VOID)
			|| check(Terminal.THIS)
			|| check(Terminal.OPEN_PARENTHESIS)
			|| check(Terminal.NEW)
			|| check(Terminal.SUPER)
			|| check(Terminal.AT_SYMBOL)
		)) { return false; }
		return accept(Nonterminal.POSTFIX_EXPRESSION, () -> acceptAny(this::expressionName, this::postIncrementExpression, this::postDecrementExpression, this::primary));
	}
	private boolean postIncrementExpression() {
		return lookAhead(1, Terminal.INCREMENT) && accept(Nonterminal.POST_INCREMENT_EXPRESSION, () -> acceptAll(this::postfixExpression, () -> accept(Terminal.INCREMENT)));
	}
	private boolean postDecrementExpression() {
		return lookAhead(1, Terminal.DECREMENT) && accept(Nonterminal.POST_DECREMENT_EXPRESSION, () -> acceptAll(this::postfixExpression, () -> accept(Terminal.DECREMENT)));
	}
	private boolean castExpression() {
		return accept(Nonterminal.CAST_EXPRESSION, () -> acceptAny(() -> acceptAll(() -> accept(Terminal.OPEN_PARENTHESIS), this::primitiveType, () -> accept(Terminal.CLOSE_PARENTHESIS), this::unaryExpression), () -> acceptAll(() -> accept(Terminal.OPEN_PARENTHESIS), this::referenceType, () -> acceptRepeating(this::additionalBound), () -> accept(Terminal.CLOSE_PARENTHESIS), this::unaryExpressionNotPlusMinus), () -> acceptAll(() -> accept(Terminal.OPEN_PARENTHESIS), this::referenceType, () -> acceptRepeating(this::additionalBound), () -> accept(Terminal.CLOSE_PARENTHESIS), this::lambdaExpression)));
	}
	private boolean constantExpression() {
		return accept(Nonterminal.CONSTANT_EXPRESSION, this::expression);
	}
}