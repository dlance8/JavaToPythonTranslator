package processes;
import constants.Nonterminal;
import constants.Terminal;
import datastructures.tree.NonterminalNode;
import datastructures.tree.TerminalNode;
import datastructures.tree.TreeNode;

import java.util.Hashtable;

import static constants.Nonterminal.*;
import static constants.Terminal.*;

public class Translator extends MyProcess {
	public static void main(String[] args) {
		new Translator().translate(new Parser().parse(new Lexer().lexFromFile("src/MyTestClass.java")));
	}

	//test commit
	private final StringBuilder indent = new StringBuilder();
	private final StringBuilder out = new StringBuilder();

	Hashtable vars = new Hashtable();
	/**
	 * vars = {"myVar1" = "int", "myVar2 = "String"}
	 */
	private String declaredVarName;
	private String declaredVarType;
	private boolean needsToBeCasted;

	public Translator() {
	}
	
	public Translator(Thread processManagementThread) {
		super(processManagementThread);
	}

	public String translate(NonterminalNode root) {
		compilationUnit(root);
		return out.toString();
	}

	private void print(String text) {
		out.append(text);
	}

	private void println(String text) {
		out.append(text).append('\n').append(indent);
	}

	private void increaseIndent() {
		indent.append('\t');
	}

	private void decreaseIndent() {
		if (indent.length() < 1) return;
		indent.setLength(indent.length() - 1);
	}

	private void setIndent(int level) {
		indent.setLength(0);
		switch (level) {
			case 0:
				if (indent.length() < 1) {
					break;
				} else {
					indent.setLength(0);
				}
			case 1:
				indent.setLength(1);
				indent.append('\t');
				break;
			case 2:
				indent.setLength(2);
				indent.append('\t');
				indent.append('\t');
				break;
			case 3:
				indent.setLength(3);
				indent.append('\t');
				indent.append('\t');
				indent.append('\t');
				break;
		}
	}

	private void setIndentWithNewline(int level) {
		indent.setLength(0);
		switch (level) {
			case 0:
				if (indent.length() < 1) {
					break;
				} else {
					indent.setLength(0);
				}
			case 1:
				indent.setLength(1);
				indent.append('\t');
				break;
			case 2:
				indent.setLength(2);
				indent.append('\t');
				indent.append('\t');
				break;
			case 3:
				indent.setLength(3);
				indent.append('\t');
				indent.append('\t');
				indent.append('\t');
				break;
		}
		println("");
	}

	private void compilationUnit(NonterminalNode parent) {
		// CompilationUnit = [ PackageDeclaration ] , { ImportDeclaration } , { TypeDeclaration } ;

		if (parent.size() == 0)
			return;

		NonterminalNode child;

		int index = 0;
		child = parent.getNonterminalChild(index);
		if (child.getValue() == Nonterminal.PACKAGE_DECLARATION) {
			packageDeclaration(child);
			++index;
		}

		while (true) {
			child = parent.getNonterminalChild(index);
			if (child.getValue() == Nonterminal.IMPORT_DECLARATION) {
				importDeclaration(child);
				++index;
			} else {
				break;
			}
		}


		while (index < parent.size()) {
			child = parent.getNonterminalChild(index);
			if (child.getValue() == Nonterminal.TYPE_DECLARATION) {
				typeDeclaration(child);
				++index;
			} else {
				break;
			}
		}
	}


	private void importDeclaration(NonterminalNode parent) {
		// ImportDeclaration = SingleTypeImportDeclaration
		//                   | TypeImportOnDemandDeclaration
		//                   | SingleStaticImportDeclaration
		//                   | StaticImportOnDemandDeclaration ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void interfaceDeclaration(NonterminalNode parent) {
		// InterfaceDeclaration = NormalInterfaceDeclaration
		//                      | AnnotationTypeDeclaration ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void packageDeclaration(NonterminalNode parent) {
		// PackageDeclaration = { PackageModifier } , "package" , Identifier , { "." , Identifier }, ";" ;
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void typeDeclaration(NonterminalNode parent) {
		// TypeDeclaration = ClassDeclaration
		//                 | InterfaceDeclaration
		//                 | ";" ;

		if (parent.get(0) instanceof NonterminalNode) {
			NonterminalNode child = parent.getNonterminalChild(0);
			if (child.getValue() == Nonterminal.CLASS_DECLARATION) {
				classDeclaration(child);
			} else {
				interfaceDeclaration(child);
			}
		} else {
			// do nothing
		}
	}

	private void classDeclaration(NonterminalNode parent) {
		// ClassDeclaration = NormalClassDeclaration
		//                  | EnumDeclaration ;
		NonterminalNode child = parent.getNonterminalChild(0);
		if (child.getValue() == Nonterminal.NORMAL_CLASS_DECLARATION) {
			normalClassDeclaration(child);
		} else {
			enumDeclaration(child);
		}
	}

	private void enumDeclaration(NonterminalNode parent) {
		// EnumDeclaration = { ClassModifier } , "enum" , Identifier , [ Superinterfaces ] , EnumBody ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void normalClassDeclaration(NonterminalNode parent) {
		// NormalClassDeclaration = { ClassModifier } , "class" , Identifier , [ TypeParameters ] , [ SuperClass ] , [ SuperInterfaces ] , ClassBody ;

		setIndent(0);
		int index = 0;

		while (true) {
			TreeNode child = parent.get(index);
			if (child instanceof NonterminalNode) { // if child is Nonterminal
				classModifier((NonterminalNode) child);
			} else { // child is terminal
				break;
			}
			index++;
		}

		// we now KNOW index points to "class"

		print("class ");
		index++;

		print(parent.getTerminalChild(index).getText());
		String className = parent.getTerminalChild(index).getText();
		index++;


		NonterminalNode child = parent.getNonterminalChild(index);
		if (child.getValue() == Nonterminal.TYPE_PARAMETERS) {
			typeParameters(child);
			index++;
			child = parent.getNonterminalChild(index);
		}

		if (child.getValue() == Nonterminal.SUPER_CLASS) {
			superClass(child);
			index++;
			child = parent.getNonterminalChild(index);
		}

		if (child.getValue() == Nonterminal.SUPER_INTERFACES) {
			superInterfaces(child);
			index++;
			child = parent.getNonterminalChild(index);
		}

		//increaseIndent();
		print(": ");
		classBody(parent.getNonterminalChild(index), className);


		println("");
		setIndentWithNewline(0);
		//println("");
		print("#0 indent");


		setIndentWithNewline(1);
		//println("");
		print("#1 indent");


		setIndentWithNewline(2);
		//println("");
		print("#2 indent");

		setIndentWithNewline(3);
		//println("");
		print("#3 indent");


	}

	private void superClass(NonterminalNode parent) {
		// Superclass = "extends" , ClassType ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void superInterfaces(NonterminalNode parent) {
		// Superinterfaces = "implements" , InterfaceTypeList ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void typeParameters(NonterminalNode parent) {
		// TypeParameters = "<" , TypeParameterList , ">" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void classModifier(NonterminalNode parent) {
		// ClassModifier = Annotation | "public" | "protected" | "private" | "abstract" | "static" | "final" | "strictfp" ;

		// do nothing
	}

	private void classBody(NonterminalNode parent, String className) {
		// ClassBody = "{" , { ClassBodyDeclaration } , "}" ;

		int classBodyLength = parent.size();
		int index = 1;

		//reminder to check this while condition
		while (index < classBodyLength - 1) {
			setIndentWithNewline(1);
			classBodyDeclaration(parent.getNonterminalChild(index), className);
			index++;
		}
		setIndentWithNewline(0);
		println("");
		print("if __name__ == \"__main__\":");
		setIndentWithNewline(1);
		print(className + ".main()");


//		error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void classBodyDeclaration(NonterminalNode parent, String className) {
		// ClassBodyDeclaration = ClassMemberDeclaration
		//                      | InstanceInitializer
		//                      | StaticInitializer
		//                      | ConstructorDeclaration ;
		NonterminalNode child = parent.getNonterminalChild(0);
		if (child.getValue() == Nonterminal.CLASS_MEMBER_DECLARATION) {
			classMemberDeclaration(child, className);
		} else if (child.getValue() == Nonterminal.INSTANCE_INITIALIZER) {
			instanceInitializer(child);
		} else if (child.getValue() == Nonterminal.STATIC_INITIALIZER) {
			staticInitializer(child);
		} else {
			constructorDeclaration(child);
		}

		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void instanceInitializer(NonterminalNode parent) {
		// InstanceInitializer = Block ;
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void staticInitializer(NonterminalNode parent) {
		// StaticInitializer = "static" , Block ;
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void constructorDeclaration(NonterminalNode parent) {
		// ConstructorDeclaration = { ConstructorModifier } , ConstructorDeclarator , [ Throws ] , ConstructorBody ;
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void classMemberDeclaration(NonterminalNode parent, String className) {
		// ClassMemberDeclaration = FieldDeclaration
		//                        | MethodDeclaration
		//                        | ClassDeclaration
		//                        | InterfaceDeclaration
		//                        | ";" ;
		NonterminalNode child = parent.getNonterminalChild(0);
		if (child.getValue() == Nonterminal.FIELD_DECLARATION) {
			fieldDeclaration(child);
		} else if (child.getValue() == Nonterminal.METHOD_DECLARATION) {
			methodDeclaration(child, className);
		} else if (child.getValue() == Nonterminal.CLASS_DECLARATION) {
			classDeclaration(child);
		} else {
			interfaceDeclaration(child);
		}


		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void fieldDeclaration(NonterminalNode parent) {
		// FieldDeclaration = { FieldModifier } , UnannType , VariableDeclaratorList , ";" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void methodDeclaration(NonterminalNode parent, String className) {
		// MethodDeclaration = { MethodModifier } , MethodHeader , MethodBody ;

		int index = 0;

//		println(parent.getNonterminalChild(0).toString());
//		println(parent.getNonterminalChild(1).toString());
//		println(parent.getNonterminalChild(2).toString());
//		println(parent.getNonterminalChild(3).toString());

		// don't care about method modifiers
		NonterminalNode child = parent.getNonterminalChild(2);
		methodHeader(child);
		child = parent.getNonterminalChild(3);
		methodBody(child, className);


		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void methodModifier(NonterminalNode parent) {
		// MethodModifier = Annotation | "public" | "protected" | "private" | "abstract" | "static" | "final" | "synchronized" | "native" | "strictfp" ;

		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void methodName(NonterminalNode parent, String className) {
		// MethodName = Identifier ;
		String functionName = parent.getTerminalChild(0).getText();
		if (functionName.equals("print") || functionName.equals("println")) {
			print("print");
		} else {
			print(className + "." + parent.getTerminalChild(0).getText());
		}

		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void methodHeader(NonterminalNode parent) {
		// MethodHeader = Result , MethodDeclarator , [ Throws ]
		//              | TypeParameters , { Annotation } , Result , MethodDeclarator , [ Throws ] ;

		int index = 0;
		NonterminalNode child = parent.getNonterminalChild(index);
		if (child.getValue() == RESULT) {
			result(child);
		}
		index++;
		child = parent.getNonterminalChild(index);
		if (child.getValue() == METHOD_DECLARATOR) {
			methodDeclarator(child);
		}


		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void result(NonterminalNode parent) {
		// Result = UnannType
		//        | "void" ;

		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void methodDeclarator(NonterminalNode parent) {
		// MethodDeclarator = Identifier , "(" , [ FormalParameterList ] , ")", [ Dims ] ;
		String methodName = parent.getTerminalChild(0).getText();

		if (methodName.equals("main")) {
			print("def ");
			print(methodName);
			print("():");  // \n
		} else {
			print("def ");
			print(methodName);
			print("():");  // \n
		}
	}

	private void methodBody(NonterminalNode parent, String className) {
		// MethodBody = Block
		//            | ";" ;

		block(parent.getNonterminalChild(0), className);

		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void block(NonterminalNode parent, String className) {
		// Block = "{" , [ BlockStatements ] , "}" ;

		// keep current indentation
		if (indent.length() > 2){// continue
		}
		// default indentation for block statements
		else {
			setIndentWithNewline(2);
		}

		blockStatements(parent.getNonterminalChild(1), className);
	}

	private void blockStatements(NonterminalNode parent, String className) {
		// BlockStatements = BlockStatement , { BlockStatement } ;

		int index = 0;
		while (index < parent.size()) {
			blockStatement(parent.getNonterminalChild(index), className);
			index++;
		}

	}

	private void blockStatement(NonterminalNode parent, String className) {
		// BlockStatement = LocalVariableDeclarationStatement
		//                | ClassDeclaration
		//                | Statement ;

		// keep current indentation

		declaredVarName = "";
		declaredVarType = "";

		if (indent.length() > 2){ } else { setIndentWithNewline(2); }


		NonterminalNode child = parent.getNonterminalChild(0);
		if (child.getValue() == STATEMENT) {
			statement(child, className);
		} else if (child.getValue() == LOCAL_VARIABLE_DECLARATION_STATEMENT) {
			localVariableDeclarationStatement(child);
		}
	}

	private void localVariableDeclarationStatement(NonterminalNode parent) {
		// LocalVariableDeclarationStatement = LocalVariableDeclaration , ";" ;
		int index = 0;
		localVariableDeclaration(parent.getNonterminalChild(index));

		index++;

		if (parent.getTerminalChild(1).getText().equals(";")){
			println("");
		}

	}

	private void localVariableDeclaration(NonterminalNode parent) {
		// LocalVariableDeclaration = { VariableModifier } , UnannType , VariableDeclaratorList ;

		int index = 0;
		NonterminalNode child = parent.getNonterminalChild(index);
		if (child.getValue() == UNANN_TYPE) {
			unannType(child);
		}

		index++;
		child = parent.getNonterminalChild(index);
		if (child.getValue() == VARIABLE_DECLARATOR_LIST) {
			variableDeclaratorList(child);
		}
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void variableDeclaratorList(NonterminalNode parent) {
		// VariableDeclaratorList = VariableDeclarator , { "," , VariableDeclarator } ;

		NonterminalNode child = parent.getNonterminalChild(0);
		variableDeclarator(child);
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void variableDeclarator(NonterminalNode parent) {
		// VariableDeclarator = VariableDeclaratorId , [ "=" , VariableInitializer ] ;

		NonterminalNode child = parent.getNonterminalChild(0);
		variableDeclaratorId(child);

		print(" = ");

		child = parent.getNonterminalChild(2);
		variableInitializer(child);


		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void variableDeclaratorId(NonterminalNode parent) {
		// VariableDeclaratorId = Identifier , [ Dims ] ;
		declaredVarName = parent.getTerminalChild(0).getText();
		print(declaredVarName);  // variable identifier

		vars.put(declaredVarName, declaredVarType);


		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void variableInitializer(NonterminalNode parent) {
		// VariableInitializer = Expression
		//                     | ArrayInitializer ;

		NonterminalNode child = parent.getNonterminalChild(0);
		expression(child);
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void unannType(NonterminalNode parent) {
		// UnannType = UnannReferenceType
		//           | UnannPrimitiveType ;

		NonterminalNode child = parent.getNonterminalChild(0);
		if (child.getValue() == UNANN_REFERENCE_TYPE){
			unannReferenceType(child);
		}
		else{
			unannPrimitiveType(child);
		}
	}

	private void unannPrimitiveType(NonterminalNode parent) {
		// UnannPrimitiveType = NumericType
		//                    | "boolean" ;

		NonterminalNode child = parent.getNonterminalChild(0);
		if (child.getValue() == NUMERIC_TYPE){
			numericType(child);
		}
		else{
			// "boolean"?
		}
	}

	private void numericType(NonterminalNode parent) {
		// NumericType = IntegralType
		//             | FloatingPointType ;

		NonterminalNode child = parent.getNonterminalChild(0);
		if (child.getValue() == INTEGRAL_TYPE){
			integralType(child);
		}
		else{
			floatingPointType(child);
		}
	}

	private void integralType(NonterminalNode parent) {
		// IntegralType = "byte" | "short" | "int" | "long" | "char" ;

		declaredVarType = parent.getTerminalChild(0).getText();
	}

	private void floatingPointType(NonterminalNode parent) {
		// FloatingPointType = "float" | "double" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void unannReferenceType(NonterminalNode parent) {
		// UnannReferenceType = UnannArrayType
		//                    | UnannClassOrInterfaceType
		//                    | UnannTypeVariable ;

		NonterminalNode child = parent.getNonterminalChild(0);
		if(child.getValue() == UNANN_ARRAY_TYPE){
			unannArrayType(child);
		}
		if(child.getValue() == UNANN_CLASS_OR_INTERFACE_TYPE){
			unannClassOrInterfaceType(child);
		}
		if(child.getValue() == UNANN_TYPE_VARIABLE){
			unannTypeVariable(child);
		}
	}

	private void statement(NonterminalNode parent, String className) {
		// Statement = StatementWithoutTrailingSubstatement
		//           | LabeledStatement
		//           | IfThenStatement
		//           | IfThenElseStatement
		//           | WhileStatement
		//           | ForStatement ;

		NonterminalNode child = parent.getNonterminalChild(0);
		if (child.getValue() == STATEMENT_WITHOUT_TRAILING_SUBSTATEMENT) {
			statementWithoutTrailingSubstatement(child, className);
		}

		if (child.getValue() == LABELED_STATEMENT) {
			labeledStatement(child);
		}

		if (child.getValue() == IF_THEN_STATEMENT) {
			ifThenStatement(child);
		}

		if (child.getValue() == IF_THEN_ELSE_STATEMENT) {
			ifThenElseStatement(child);
		}

		if (child.getValue() == WHILE_STATEMENT) {
			whileStatement(child);
		}

		if (child.getValue() == FOR_STATEMENT) {
			forStatement(child);
		}

		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void whileStatement(NonterminalNode parent) {
		// WhileStatement = "while" , "(" , Expression , ")" , Statement ;
		String className = "";
		int index = 0;
		print("while");
		index++;

		print("(");
		index++;

		needsToBeCasted = false;
		expression(parent.getNonterminalChild(index));  // condition in while statement
		index++;

		print("):");
		index++;
		increaseIndent();
		println("");

		needsToBeCasted = true;
		statement(parent.getNonterminalChild(index), className);

	}

	private void forStatement(NonterminalNode parent) {
		// ForStatement = BasicForStatement
		//              | EnhancedForStatement ;

		NonterminalNode child = parent.getNonterminalChild(0);
		if(child.getValue() == BASIC_FOR_STATEMENT){
			basicForStatement(child);
		}
		else{
			enhancedForStatement(child);
		}

		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void basicForStatement(NonterminalNode parent) {
		// BasicForStatement = "for" , "(" , [ ForInit ] , ";" , [ Expression ] , ";" , [ ForUpdate ] , ")" , Statement ;

		String className = "";
		int index = 0;

		// define the iteration variable first
		if(parent.getNonterminalChild(2).getValue() == FOR_INIT){
			forInit(parent.getNonterminalChild(2));
			println("");
		}

		while(index < parent.size()){
			TreeNode child = parent.get(index);

			if(child instanceof NonterminalNode){
				if(((NonterminalNode) child).getValue() == FOR_INIT){
					//forInit(parent.getNonterminalChild(index));
					index++;
				}
				if(((NonterminalNode) child).getValue() == EXPRESSION){
					needsToBeCasted = false;
					expression(parent.getNonterminalChild(index));
					print("):");
					index++;
				}
				if(((NonterminalNode) child).getValue() == FOR_UPDATE){
					//forUpdate(parent.getNonterminalChild(index));
					index++;
				}
				if(((NonterminalNode) child).getValue() == STATEMENT){
//					statement(parent.getNonterminalChild(index), className);
					index++;
				}
			}
			else if(child instanceof TerminalNode){
				if(((TerminalNode) child).getValue() == FOR){
					print("while");
					index++;
				}
				if(((TerminalNode) child).getValue() == OPEN_PARENTHESIS){
					print("(");
					index++;
				}
				if(((TerminalNode) child).getValue() == SEMICOLON){
					index++;
				}
				if(((TerminalNode) child).getValue() == CLOSE_PARENTHESIS){
//					print(")");
					index++;
				}
			}
			else{
				index++;
			}

		}

		increaseIndent();
		println("");

		statement(parent.getNonterminalChild(8), className);
		needsToBeCasted = false;
		forUpdate(parent.getNonterminalChild(6));

		decreaseIndent();
		println("");


	}

	private void forUpdate(NonterminalNode parent) {
		// ForUpdate = StatementExpressionList ;
		statementExpressionList(parent.getNonterminalChild(0));

		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void statementExpressionList(NonterminalNode parent) {
		// StatementExpressionList = StatementExpression , { "," , StatementExpression } ;
		String className = "";
		statementExpression(parent.getNonterminalChild(0), className);

		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void forInit(NonterminalNode parent) {
		// ForInit = StatementExpressionList
		//         | LocalVariableDeclaration ;

		if(parent.getNonterminalChild(0).getValue() == STATEMENT_EXPRESSION_LIST){
			statementExpressionList(parent.getNonterminalChild(0));
		}
		else{
			localVariableDeclaration(parent.getNonterminalChild(0));
		}

		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void labeledStatement(NonterminalNode parent) {
		// LabeledStatement = Identifier , ":" , Statement ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void ifThenStatement(NonterminalNode parent) {
		// IfThenStatement = "if" , "(" , Expression , ")" , Statement ;
		String className = "";
		int index = 0;
		while (index < parent.size()) {
			TreeNode child = parent.get(index);

			if (child instanceof TerminalNode) {
				if (((TerminalNode) child).getValue() == IF) {
					print("if");
					index++;
				}
				if (((TerminalNode) child).getValue() == OPEN_PARENTHESIS) {
					print("(");
					index++;
				}

				if (((TerminalNode) child).getValue() == CLOSE_PARENTHESIS) {
					print("):");
					//print(String.valueOf(indent.length()));
					//increaseIndent();
					//increaseIndent();
					println("");
					//decreaseIndent();
					//decreaseIndent();

					index++;
				}

			} else if (child instanceof NonterminalNode) {
				if (((NonterminalNode) child).getValue() == EXPRESSION) {
					//print("x < 4");
					expression((NonterminalNode) child);
					index++;
				}
				if (((NonterminalNode) child).getValue() == STATEMENT) {
					//print("less than 4");
					statement((NonterminalNode) child, className);
					index++;
				}

			} else {
				index++;
			}

		}
	}

	private void ifThenElseStatement(NonterminalNode parent) {
		// IfThenElseStatement = "if" , "(" , Expression , ")" , StatementNoShortIf , "else" , Statement ;

		int startingIndent = indent.length();
		int index = 0;
		String className = ""; //ignore
		while (index < parent.size()) {
			TreeNode child = parent.get(index);
			if (child instanceof NonterminalNode) {
				if (((NonterminalNode) child).getValue() == EXPRESSION) {
					needsToBeCasted = false;
					expression((NonterminalNode) child);
					index++;
				}
				if (((NonterminalNode) child).getValue() == STATEMENT_NO_SHORT_IF) {
					increaseIndent();
					println("");
					statementNoShortIf((NonterminalNode) child);
					index++;
					decreaseIndent();
					println("");
				}

				if (((NonterminalNode) child).getValue() == STATEMENT) {
					increaseIndent();
					println("");
					statement((NonterminalNode) child, className);
					setIndentWithNewline(startingIndent);
					index++;
				}

			} else if (child instanceof TerminalNode) {
				if (((TerminalNode) child).getValue() == IF) {
					print("if");
					index++;
				}

				if (((TerminalNode) child).getValue() == OPEN_PARENTHESIS) {
					print("(");
					index++;
				}

				if (((TerminalNode) child).getValue() == CLOSE_PARENTHESIS) {
					print("):");
					index++;
				}

				if (((TerminalNode) child).getValue() == ELSE) {
					print("else:");
					index++;
				}

			} else {
				index++;
			}

		}
	}

	private void statementNoShortIf(NonterminalNode parent) {
		// StatementNoShortIf = StatementWithoutTrailingSubstatement
		//                    | LabeledStatementNoShortIf
		//                    | IfThenElseStatementNoShortIf
		//                    | WhileStatementNoShortIf
		//                    | ForStatementNoShortIf ;

		NonterminalNode child = parent.getNonterminalChild(0);
		String className = "";
		if (child.getValue() == STATEMENT_WITHOUT_TRAILING_SUBSTATEMENT) {
			statementWithoutTrailingSubstatement(child, className);
		}


		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void statementWithoutTrailingSubstatement(NonterminalNode parent, String className) {
		// StatementWithoutTrailingSubstatement = Block
		//                                      | EmptyStatement
		//                                      | ExpressionStatement
		//                                      | AssertStatement
		//                                      | SwitchStatement
		//                                      | DoStatement
		//                                      | BreakStatement
		//                                      | ContinueStatement
		//                                      | ReturnStatement
		//                                      | SynchronizedStatement
		//                                      | ThrowStatement
		//                                      | TryStatement ;

		NonterminalNode child = parent.getNonterminalChild(0);
		if (child.getValue() == EXPRESSION_STATEMENT) {
			expressionStatement(child, className);
		}

		if (child.getValue() == BLOCK) {
			block(child, className);
		}
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void expressionStatement(NonterminalNode parent, String className) {
		// ExpressionStatement = StatementExpression , ";" ;

		int index = 0;
		statementExpression(parent.getNonterminalChild(index), className);
		index++;
		if(parent.getTerminalChild(index).getText().equals(";")){
			println("");
		}
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void statementExpression(NonterminalNode parent, String className) {
		// StatementExpression = Assignment
		//                     | PreIncrementExpression
		//                     | PreDecrementExpression
		//                     | PostIncrementExpression
		//                     | PostDecrementExpression
		//                     | MethodInvocation
		//                     | ClassInstanceCreationExpression ;

		NonterminalNode child = parent.getNonterminalChild(0);
//		if (child.getValue() == METHOD_INVOCATION) {
//			methodInvocation(child, className);
//		}

		switch(child.getValue()){
			case METHOD_INVOCATION:
				methodInvocation(child, className);
				break;
			case PRE_INCREMENT_EXPRESSION:
				preIncrementExpression(child);
				break;
			case PRE_DECREMENT_EXPRESSION:
				preDecrementExpression(child);
				break;
			case POST_INCREMENT_EXPRESSION:
				postIncrementExpression(child);
				break;
			case POST_DECREMENT_EXPRESSION:
				postDecrementExpression(child);
				break;
			case CLASS_INSTANCE_CREATION_EXPRESSION:
				classInstanceCreationExpression(child);
				break;

		}

		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void methodInvocation(NonterminalNode parent, String className) {
		//                  | TypeName , "." , [ TypeArguments ] , Identifier , "(" , [ ArgumentList ] , ")"
		//                  | ExpressionName , "." , [ TypeArguments ] , Identifier , "(" , [ArgumentList ] , ")"
		//                  | Primary , "." , [ TypeArguments ] , Identifier , "(" , [ArgumentList ] , ")"
		//                  | "super" , "." , [ TypeArguments ] , Identifier , "(" , [ArgumentList ] , ")"
		//                  | TypeName , "." , "super" , "." , [ TypeArguments ] , Identifier , "(" , [ArgumentList ] , ")" ;

		int index = 0;
		while (index < parent.size()) {
			TreeNode child = parent.get(index);

			if (child instanceof NonterminalNode) {

				if ((((NonterminalNode) child).getValue() == EXPRESSION_NAME)) {
					expressionName((NonterminalNode) child);
					index++;
				}

				if ((((NonterminalNode) child).getValue() == METHOD_NAME)) {
					methodName((NonterminalNode) child, className);
					index++;
				}
				if (((NonterminalNode) child).getValue() == ARGUMENT_LIST) {
					argumentList((NonterminalNode) child);
					index++;
				}

				if (((NonterminalNode) child).getValue() == TYPE_ARGUMENTS) {
					index++;
				}
			} else if (child instanceof TerminalNode) {
				if (((TerminalNode) child).getValue() == Terminal.IDENTIFIER) {
					print("print"); // should equal "print" or "println"
					index++;
				}

				if (((TerminalNode) child).getValue() == Terminal.DOT) {
					index++;
				}

				if (((TerminalNode) child).getValue() == OPEN_PARENTHESIS) {
					print("(");
					index++;
				}
				if (((TerminalNode) child).getValue() == Terminal.CLOSE_PARENTHESIS) {
					print(")");
					index++;
				}
				if (((TerminalNode) child).getValue() == Terminal.IDENTIFIER){
				}
			}

			else {
				index++;
			}
		}
	}

	private void postIncrementExpression(NonterminalNode parent) {
		// PostIncrementExpression = PostfixExpression , "++" ;

		needsToBeCasted = false;

		NonterminalNode child = parent.getNonterminalChild(0);
		postfixExpression(child);

		print(" += 1");

		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void postDecrementExpression(NonterminalNode parent) {
		// PostDecrementExpression = PostfixExpression , "--" ;
		int index = 0;
		NonterminalNode child = parent.getNonterminalChild(index);
		if (child.getValue() == POSTFIX_EXPRESSION){
			postfixExpression(child);
		}
		index++;
		print(" -= 1");
	}

	private void preIncrementExpression(NonterminalNode parent) {
		// PreIncrementExpression = "++" , UnaryExpression ;
		NonterminalNode child = parent.getNonterminalChild(1);
		unaryExpression(child);

		print(" += 1");

		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void preDecrementExpression(NonterminalNode parent) {
		// PreDecrementExpression = "--" , UnaryExpression ;\
		//
		unaryExpression(parent.getNonterminalChild(1));
		print(" -= 1");
	}

	private void postfixExpression(NonterminalNode parent) {
		// PostfixExpression = Primary
		//                   | ExpressionName
		//                   | PostIncrementExpression
		//                   | PostDecrementExpression ;

		NonterminalNode child = parent.getNonterminalChild(0);
		if (child.getValue() == PRIMARY) {
			primary(child);
		}
		if (child.getValue() == EXPRESSION_NAME) {
			expressionName(child);
		}
		if (child.getValue() == POST_INCREMENT_EXPRESSION) {
			postIncrementExpression(child);
		}
		if (child.getValue() == POST_DECREMENT_EXPRESSION) {
			postDecrementExpression(child);
		}
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void expressionName(NonterminalNode parent) {
		// ExpressionName = Identifier , { "." , Identifier } ;

		String exprName = parent.getTerminalChild(0).getText();
		if (exprName.equals("System")) {
			// do nothing
		}

		// if exprName is not a conditional statement and not a String
		else if(needsToBeCasted == true && vars.get(exprName).equals("int")){
			print("str("+ exprName + ")");
		}
		else {
			print(parent.getTerminalChild(0).getText());
		}

		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void argumentList(NonterminalNode parent) {
		// ArgumentList = Expression , { "," , Expression } ;

		expression(parent.getNonterminalChild(0));
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void expression(NonterminalNode parent) {
		// Expression = LambdaExpression
		//            | AssignmentExpression ;

		assignmentExpression(parent.getNonterminalChild(0));

		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void assignmentExpression(NonterminalNode parent) {
		// AssignmentExpression = ConditionalOrExpression
		//                      | Assignment ;

		conditionalOrExpression(parent.getNonterminalChild(0));
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void conditionalOrExpression(NonterminalNode parent) {
		// ConditionalOrExpression = ConditionalAndExpression , { "||" , ConditionalAndExpression } ;

		conditionalAndExpression(parent.getNonterminalChild(0));
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void conditionalAndExpression(NonterminalNode parent) {
		// ConditionalAndExpression = InclusiveOrExpression , { "&&" , InclusiveOrExpression } ;

		inclusiveOrExpression(parent.getNonterminalChild(0));
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void inclusiveOrExpression(NonterminalNode parent) {
		// InclusiveOrExpression = ExclusiveOrExpression , { "|" , ExclusiveOrExpression } ;
		exclusiveOrExpression(parent.getNonterminalChild(0));
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void exclusiveOrExpression(NonterminalNode parent) {
		// ExclusiveOrExpression = AndExpression , { "^" , AndExpression } ;
		andExpression(parent.getNonterminalChild(0));
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void andExpression(NonterminalNode parent) {
		// AndExpression = EqualityExpression , { "&" , EqualityExpression } ;
		equalityExpression(parent.getNonterminalChild(0));
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void equalityExpression(NonterminalNode parent) {
		// EqualityExpression = RelationalExpression , { ( "==" | "!=" ) , RelationalExpression } ;

		relationalExpression(parent.getNonterminalChild(0));
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void relationalExpression(NonterminalNode parent) {
		// RelationalExpression = ShiftExpression , "instanceof" , ReferenceType
		//                      | ShiftExpression , { ( "<" | ">" | "<=" | ">=" ) , ShiftExpression } ;


		int index = 0;
		while (index < parent.size()) {
			TreeNode child = parent.get(index);

			if (child instanceof NonterminalNode) {
				shiftExpression((NonterminalNode) child);
				index++;
			} else if (child instanceof TerminalNode) {
				print(" " + ((TerminalNode) child).getText() + " "); // print "<" | ">" | "<=" | ">="
				index++;
			} else {
				index++;
			}
		}


		//shiftExpression(parent.getNonterminalChild(0));

		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void shiftExpression(NonterminalNode parent) {
		// ShiftExpression = AdditiveExpression , { ( "<<" | ">>" | ">>>" ) , AdditiveExpression } ;

		additiveExpression(parent.getNonterminalChild(0));
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void additiveExpression(NonterminalNode parent) {
		// AdditiveExpression = MultiplicativeExpression , { ( "+" | "-" ) , MultiplicativeExpression } ;

		multiplicativeExpression(parent.getNonterminalChild(0));
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void multiplicativeExpression(NonterminalNode parent) {
		// MultiplicativeExpression = UnaryExpression , { ( "*" | "/" | "%" ) , UnaryExpression } ;
		int index = 0;
		while(index < parent.size()){
			TreeNode child = parent.get(index);
			if(child instanceof NonterminalNode){
				unaryExpression((NonterminalNode) child);
				index++;
			}
			else if(child instanceof TerminalNode){
				print(((TerminalNode) child).getText());
				index++;
			}
			else{
				index++;
			}
		}
	}

	private void unaryExpression(NonterminalNode parent) {
		// UnaryExpression = PreIncrementExpression
		//                 | PreDecrementExpression
		//                 | "+" , UnaryExpression
		//                 | "-" , UnaryExpression
		//                 | UnaryExpressionNotPlusMinus ;

		unaryExpressionNotPlusMinus(parent.getNonterminalChild(0));
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void unaryExpressionNotPlusMinus(NonterminalNode parent) {
		// UnaryExpressionNotPlusMinus = PostfixExpression
		//                             | "~" , UnaryExpression
		//                             | "!" , UnaryExpression
		//                             | CastExpression ;

		postfixExpression(parent.getNonterminalChild(0));
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void primary(NonterminalNode parent) {
		// Primary = PrimaryNoNewArray
		//         | ArrayCreationExpression ;

		primaryNoNewArray(parent.getNonterminalChild(0));
		//error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void primaryNoNewArray(NonterminalNode parent) {
		// PrimaryNoNewArray = Literal
		//                   | ClassLiteral
		//                   | "this"
		//                   | TypeName , "." , "this"
		//                   | "(" , Expression , ")"
		//                   | ClassInstanceCreationExpression
		//                   | FieldAccess
		//                   | ArrayAccess
		//                   | MethodInvocation
		//                   | MethodReference ;
		String className = "";

		TreeNode child = parent.get(0);
		if(child instanceof TerminalNode){
			String literal = parent.getTerminalChild(0).getText();
			print(literal);
			}
		else if(child instanceof NonterminalNode){
			if(((NonterminalNode) child).getValue() == METHOD_INVOCATION){
				methodInvocation((NonterminalNode) child, className);
			}
		}
	}


	private void assignment(NonterminalNode parent) {
		// Assignment = LeftHandSide , AssignmentOperator , Expression ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void leftHandSide(NonterminalNode parent) {
		// LeftHandSide = ExpressionName
		//              | FieldAccess
		//              | ArrayAccess ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void assignmentOperator(NonterminalNode parent) {
		// AssignmentOperator = "=" | "*=" | "/=" | "%=" | "+=" | "-=" | "<<=" | ">>=" | ">>>=" | "&=" | "^=" | "|=" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void conditionalExpression(NonterminalNode parent) {
		// ConditionalExpression = ConditionalOrExpression , "?" , Expression , ":" , ConditionalExpression
		//                       | ConditionalOrExpression , "?" , Expression , ":" , LambdaExpression
		//                       | ConditionalOrExpression ;


		error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void castExpression(NonterminalNode parent) {
		// CastExpression = "(" , PrimitiveType , ")" , UnaryExpression
		//                | "(" , ReferenceType , { AdditionalBound } , ")" , UnaryExpressionNotPlusMinus
		//                | "(" , ReferenceType , { AdditionalBound } , ")" , LambdaExpression ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void constantExpression(NonterminalNode parent) {
		// ConstantExpression = Expression ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void type(NonterminalNode parent) {
		// Type = PrimitiveType
		//      | ReferenceType ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void primitiveType(NonterminalNode parent) {
		// PrimitiveType = { Annotation } , NumericType
		//               | { Annotation } , "boolean" ;

		// DO NOTHING
	}
	private void referenceType(NonterminalNode parent) {
		// ReferenceType = ArrayType
		//               | ClassOrInterfaceType
		//               | TypeVariable ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void classOrInterfaceType(NonterminalNode parent) {
		// ClassOrInterfaceType = ClassType
		//                      | InterfaceType ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void classType(NonterminalNode parent) {
		// ClassType = { Annotation } , Identifier , [ TypeArguments ]
		//           | ClassOrInterfaceType , "." , { Annotation } , Identifier , [ TypeArguments ] ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void interfaceType(NonterminalNode parent) {
		// InterfaceType = ClassType ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void typeVariable(NonterminalNode parent) {
		// TypeVariable = { Annotation } , Identifier ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void arrayType(NonterminalNode parent) {
		// ArrayType = PrimitiveType , Dims
		//           | ClassType , Dims
		//           | TypeVariable , Dims ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void dims(NonterminalNode parent) {
		// Dims = { Annotation } , "[" , "]" , { { Annotation } , "[" , "]" } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void typeParameter(NonterminalNode parent) {
		// TypeParameter = { TypeParameterModifier } , Identifier , [ TypeBound ] ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void typeParameterModifier(NonterminalNode parent) {
		// TypeParameterModifier = Annotation ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void typeBound(NonterminalNode parent) {
		// TypeBound = "extends" , TypeVariable
		//           | "extends" , ClassOrInterfaceType , { AdditionalBound } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void additionalBound(NonterminalNode parent) {
		// AdditionalBound = "&" , InterfaceType ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void typeArguments(NonterminalNode parent) {
		// TypeArguments = "<" , TypeArgumentList , ">" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void typeArgumentList(NonterminalNode parent) {
		// TypeArgumentList = TypeArgument , { "," , TypeArgument } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void typeArgument(NonterminalNode parent) {
		// TypeArgument = ReferenceType
		//              | Wildcard;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void wildcard(NonterminalNode parent) {
		// Wildcard = { Annotation } , "?" , [ WildcardBounds ] ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void wildcardBounds(NonterminalNode parent) {
		// WildcardBounds = "extends" , ReferenceType
		//                | "super" , ReferenceType ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void packageName(NonterminalNode parent) {
		// PackageName = Identifier , { "." , Identifier } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void typeName(NonterminalNode parent) {
		// TypeName = Identifier , { "." , Identifier } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void packageOrTypeName(NonterminalNode parent) {
		// PackageOrTypeName = Identifier , { "." , Identifier } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void ambiguousName(NonterminalNode parent) {
		// AmbiguousName = Identifier , { "." , Identifier } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void packageModifier(NonterminalNode parent) {
		// PackageModifier = Annotation ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void singleTypeImportDeclaration(NonterminalNode parent) {
		// SingleTypeImportDeclaration = "import" , TypeName , ";" ;

		// do nothing for now, may need to keep track of imports

	}
	private void typeImportOnDemandDeclaration(NonterminalNode parent) {
		// TypeImportOnDemandDeclaration = "import" , PackageOrTypeName , "." , "*" , ";" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void singleStaticImportDeclaration(NonterminalNode parent) {
		// SingleStaticImportDeclaration = "import" , "static" , TypeName , "." , Identifier , ";" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void staticImportOnDemandDeclaration(NonterminalNode parent) {
		// StaticImportOnDemandDeclaration = "import" , "static" , TypeName , "." , "*" , ";" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void typeParameterList(NonterminalNode parent) {
		// TypeParameterList = TypeParameter , { "," , TypeParameter } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void interfaceTypeList(NonterminalNode parent) {
		// InterfaceTypeList = InterfaceType , { "," , InterfaceType } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void fieldModifier(NonterminalNode parent) {
		// FieldModifier = Annotation | "public" | "protected" | "private" | "static" | "final" | "transient" | "volatile" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void unannClassOrInterfaceType(NonterminalNode parent) {
		// UnannClassOrInterfaceType = UnannClassType
		//                           | UnannInterfaceType ;

		NonterminalNode child = parent.getNonterminalChild(0);
		if(child.getValue() == UNANN_CLASS_TYPE){
			unannClassType(child);
		}
		if(child.getValue() == UNANN_INTERFACE_TYPE){
			unannInterfaceType(child);
		}
	}
	private void unannClassType(NonterminalNode parent) {
		// UnannClassType = Identifier , [ TypeArguments ]
		//                | UnannClassOrInterfaceType , "." , { Annotation } , Identifier , [ TypeArguments ] ;
		declaredVarType = parent.getTerminalChild(0).getText();

	}
	private void unannInterfaceType(NonterminalNode parent) {
		// UnannInterfaceType = UnannClassType ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void unannTypeVariable(NonterminalNode parent) {
		// UnannTypeVariable = Identifier ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void unannArrayType(NonterminalNode parent) {
		// UnannArrayType = UnannPrimitiveType , Dims
		//                | UnannClassOrInterfaceType , Dims
		//                | UnannTypeVariable , Dims ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void formalParameterList(NonterminalNode parent) {
		// FormalParameterList = FormalParameters , [ "," , LastFormalParameter ] ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void formalParameters(NonterminalNode parent) {
		// FormalParameters = FormalParameter , { "," , FormalParameter }
		//                  | ReceiverParameter , { "," , FormalParameter } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void formalParameter(NonterminalNode parent) {
		// FormalParameter = { VariableModifier } , UnannType , VariableDeclaratorId ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void variableModifier(NonterminalNode parent) {
		// VariableModifier = Annotation | "final" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void lastFormalParameter(NonterminalNode parent) {
		// LastFormalParameter = { VariableModifier } , UnannType , { Annotation } , "..." , VariableDeclaratorId
		//                     | FormalParameter ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void receiverParameter(NonterminalNode parent) {
		// ReceiverParameter = { Annotation } , UnannType , [ Identifier , "." ] , "this" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void throws_(NonterminalNode parent) {
		// Throws = "throws" , ExceptionTypeList ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void exceptionTypeList(NonterminalNode parent) {
		// ExceptionTypeList = ExceptionType , { "," , ExceptionType } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void exceptionType(NonterminalNode parent) {
		// ExceptionType = ClassType
		//               | TypeVariable ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void constructorModifier(NonterminalNode parent) {
		// ConstructorModifier = Annotation | "public" | "protected" | "private" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void constructorDeclarator(NonterminalNode parent) {
		// ConstructorDeclarator = [ TypeParameters ] , SimpleTypeName , "(" , [ FormalParameterList ] , ")" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void simpleTypeName(NonterminalNode parent) {
		// SimpleTypeName = Identifier ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void constructorBody(NonterminalNode parent) {
		// ConstructorBody = "{" , [ ExplicitConstructorInvocation ] , [ BlockStatements ] , "}" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void explicitConstructorInvocation(NonterminalNode parent) {
		// ExplicitConstructorInvocation = [ TypeArguments ] , "this" , "(" , [ ArgumentList ] , ")" , ";"
		//                               | [ TypeArguments ] , "super" , "(" , [ ArgumentList ] , ")" , ";"
		//                               | ExpressionName , "." , [ TypeArguments ] , "super" , "(" , [ ArgumentList ] , ")" , ";"
		//                               | Primary , "." , [ TypeArguments ] , "super" , "(" , [ ArgumentList ] , ")" , ";" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void enumBody(NonterminalNode parent) {
		// EnumBody = "{" , [ EnumConstantList ] , [ "," ] , [ EnumBodyDeclarations ] , "}" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void enumConstantList(NonterminalNode parent) {
		// EnumConstantList = EnumConstant , { "," , EnumConstant } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void enumConstant(NonterminalNode parent) {
		// EnumConstant = { EnumConstantModifier } , Identifier , [ "(" , ArgumentList , ")" ] , [ ClassBody ] ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void enumConstantModifier(NonterminalNode parent) {
		// EnumConstantModifier = Annotation ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void enumBodyDeclarations(NonterminalNode parent) {
		// EnumBodyDeclarations = ";" , { ClassBodyDeclaration } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void normalInterfaceDeclaration(NonterminalNode parent) {
		// NormalInterfaceDeclaration = { InterfaceModifier } , "interface" , Identifier , [ TypeParameters ] , [ ExtendsInterfaces ] , InterfaceBody ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void interfaceModifier(NonterminalNode parent) {
		// InterfaceModifier = Annotation | "public" | "protected" | "private" | "abstract" | "static" | "strictfp" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void extendsInterfaces(NonterminalNode parent) {
		// ExtendsInterfaces = "extends" , InterfaceTypeList ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void interfaceBody(NonterminalNode parent) {
		// InterfaceBody = "{" , { InterfaceMemberDeclaration } , "}" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void interfaceMemberDeclaration(NonterminalNode parent) {
		// InterfaceMemberDeclaration = ConstantDeclaration
		//                            | InterfaceMethodDeclaration
		//                            | ClassDeclaration
		//                            | InterfaceDeclaration
		//                            | ";" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void constantDeclaration(NonterminalNode parent) {
		// ConstantDeclaration = { ConstantModifier } , UnannType , VariableDeclaratorList , ";" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void constantModifier(NonterminalNode parent) {
		// ConstantModifier = Annotation | "public" | "static" | "final" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void interfaceMethodDeclaration(NonterminalNode parent) {
		// InterfaceMethodDeclaration = { InterfaceMethodModifier } , MethodHeader , MethodBody ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void interfaceMethodModifier(NonterminalNode parent) {
		// InterfaceMethodModifier = Annotation | "public" | "abstract" | "default" | "static" | "strictfp" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void annotationTypeDeclaration(NonterminalNode parent) {
		// AnnotationTypeDeclaration = { InterfaceModifier } , "@" , "interface" , Identifier , AnnotationTypeBody ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void annotationTypeBody(NonterminalNode parent) {
		// AnnotationTypeBody = "{" , { AnnotationTypeMemberDeclaration } , "}" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void annotationTypeMemberDeclaration(NonterminalNode parent) {
		// AnnotationTypeMemberDeclaration = AnnotationTypeElementDeclaration
		//                                 | ConstantDeclaration
		//                                 | ClassDeclaration
		//                                 | InterfaceDeclaration
		//                                 | ";" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void annotationTypeElementDeclaration(NonterminalNode parent) {
		// AnnotationTypeElementDeclaration = { AnnotationTypeElementModifier } , UnannType , Identifier , "(" , ")" , [ Dims ] , [ DefaultValue ] , ";" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void annotationTypeElementModifier(NonterminalNode parent) {
		// AnnotationTypeElementModifier = Annotation | "public" | "abstract" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void defaultValue(NonterminalNode parent) {
		// DefaultValue = "default" , ElementValue ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void annotation(NonterminalNode parent) {
		// Annotation = NormalAnnotation
		//            | MarkerAnnotation
		//            | SingleElementAnnotation ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void normalAnnotation(NonterminalNode parent) {
		// NormalAnnotation = "@" , TypeName , "(" , [ ElementValuePairList ] , ")" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void elementValuePairList(NonterminalNode parent) {
		// ElementValuePairList = ElementValuePair , { "," , ElementValuePair } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void elementValuePair(NonterminalNode parent) {
		// ElementValuePair = Identifier , "=" , ElementValue ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void elementValue(NonterminalNode parent) {
		// ElementValue = ConditionalExpression
		//              | ElementValueArrayInitializer
		//              | Annotation ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void elementValueArrayInitializer(NonterminalNode parent) {
		// ElementValueArrayInitializer = "{" , [ ElementValueList ] , [ "," ] , "}" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void elementValueList(NonterminalNode parent) {
		// ElementValueList = ElementValue , { "," , ElementValue } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void markerAnnotation(NonterminalNode parent) {
		// MarkerAnnotation = "@" , TypeName ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void singleElementAnnotation(NonterminalNode parent) {
		// SingleElementAnnotation = "@" , TypeName , "(" , ElementValue , ")" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void arrayInitializer(NonterminalNode parent) {
		// ArrayInitializer = "{" , [ VariableInitializerList ] , [ "," ] , "}" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void variableInitializerList(NonterminalNode parent) {
		// VariableInitializerList = VariableInitializer , { "," , VariableInitializer } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void emptyStatement(NonterminalNode parent) {
		// EmptyStatement = ";" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void labeledStatementNoShortIf(NonterminalNode parent) {
		// LabeledStatementNoShortIf = Identifier , ":" , StatementNoShortIf ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void ifThenElseStatementNoShortIf(NonterminalNode parent) {
		// IfThenElseStatementNoShortIf = "if" , "(" , Expression , ")" , StatementNoShortIf , "else" , StatementNoShortIf ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void assertStatement(NonterminalNode parent) {
		// AssertStatement = "assert" , Expression , ";"
		//                 | "assert" , Expression , ":" , Expression ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void switchStatement(NonterminalNode parent) {
		// SwitchStatement = "switch" , "(" , Expression , ")" , SwitchBlock ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void switchBlock(NonterminalNode parent) {
		// SwitchBlock = "{" , { SwitchBlockStatementGroup } , { SwitchLabel } , "}" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void switchBlockStatementGroup(NonterminalNode parent) {
		// SwitchBlockStatementGroup = SwitchLabels , BlockStatements ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void switchLabels(NonterminalNode parent) {
		// SwitchLabels = SwitchLabel , { SwitchLabel } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void switchLabel(NonterminalNode parent) {
		// SwitchLabel = "case" , ConstantExpression , ":"
		//             | "case" , EnumConstantName , ":"
		//             | "default" , ":" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void enumConstantName(NonterminalNode parent) {
		// EnumConstantName = Identifier ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void whileStatementNoShortIf(NonterminalNode parent) {
		// WhileStatementNoShortIf = "while" , "(" , Expression , ")" , StatementNoShortIf ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void doStatement(NonterminalNode parent) {
		// DoStatement = "do" , Statement , "while" , "(" , Expression , ")" , ";" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void forStatementNoShortIf(NonterminalNode parent) {
		// ForStatementNoShortIf = BasicForStatementNoShortIf
		//                       | EnhancedForStatementNoShortIf ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}

	private void basicForStatementNoShortIf(NonterminalNode parent) {
		// BasicForStatementNoShortIf = "for" , "(" , [ ForInit ] , ";" , [ Expression ] , ";" , [ ForUpdate ] , ")" , StatementNoShortIf ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}



	private void enhancedForStatement(NonterminalNode parent) {
		// EnhancedForStatement = "for" , "(" , { VariableModifier } , UnannType , VariableDeclaratorId , ":" , Expression , ")" , Statement ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void enhancedForStatementNoShortIf(NonterminalNode parent) {
		// EnhancedForStatementNoShortIf = "for" , "(" , { VariableModifier } , UnannType , VariableDeclaratorId , ":" , Expression , ")" , StatementNoShortIf ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void breakStatement(NonterminalNode parent) {
		// BreakStatement = "break" , [ Identifier ] , ";" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void continueStatement(NonterminalNode parent) {
		// ContinueStatement = "continue" , [ Identifier ] , ";" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void returnStatement(NonterminalNode parent) {
		// ReturnStatement = "return" , [ Expression ] , ";" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void throwStatement(NonterminalNode parent) {
		// ThrowStatement = "throw" , Expression , ";" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void synchronizedStatement(NonterminalNode parent) {
		// SynchronizedStatement = "synchronized" , "(" , Expression , ")" , Block ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void tryStatement(NonterminalNode parent) {
		// TryStatement = "try" , Block , [ Catches ] , Finally
		//              | "try" , Block , Catches
		//              | TryWithResourcesStatement ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void catches(NonterminalNode parent) {
		// Catches = CatchClause , { CatchClause } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void catchClause(NonterminalNode parent) {
		// CatchClause = "catch" , "(" , CatchFormalParameter , ")" , Block ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void catchFormalParameter(NonterminalNode parent) {
		// CatchFormalParameter = { VariableModifier } , CatchType , VariableDeclaratorId ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void catchType(NonterminalNode parent) {
		// CatchType = "UnannClassType" , { "|" , ClassType } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void finally_(NonterminalNode parent) {
		// Finally = "finally" , Block ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void tryWithResourcesStatement(NonterminalNode parent) {
		// TryWithResourcesStatement = "try" , ResourceSpecification , Block , [ Catches ] , [ Finally ] ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void resourceSpecification(NonterminalNode parent) {
		// ResourceSpecification = "(" , ResourceList , [ ";" ] , ")" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void resourceList(NonterminalNode parent) {
		// ResourceList = Resource , { ";" , Resource } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void resource(NonterminalNode parent) {
		// Resource = { VariableModifier } , UnannType , VariableDeclaratorId , "=" , Expression ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void classLiteral(NonterminalNode parent) {
		// ClassLiteral = TypeName , { "[" , "]" } , "." , "class"
		//              | NumericType , { "[" , "]" } , "." , "class"
		//              | "boolean" , { "[" , "]" } , "." , "class"
		//              | "void" , "." , "class" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void classInstanceCreationExpression(NonterminalNode parent) {
		// ClassInstanceCreationExpression = UnqualifiedClassInstanceCreationExpression
		//                                 | ExpressionName , "." , UnqualifiedClassInstanceCreationExpression
		//                                 | Primary , "." , UnqualifiedClassInstanceCreationExpression ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void unqualifiedClassInstanceCreationExpression(NonterminalNode parent) {
		// UnqualifiedClassInstanceCreationExpression = "new" , [ TypeArguments ] , ClassOrInterfaceTypeToInstantiate , "(" , [ ArgumentList ] , ")" , [ ClassBody ] ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void classOrInterfaceTypeToInstantiate(NonterminalNode parent) {
		// ClassOrInterfaceTypeToInstantiate = { Annotation } , Identifier , { "." , { Annotation } , Identifier } , [ TypeArgumentsOrDiamond ] ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void typeArgumentsOrDiamond(NonterminalNode parent) {
		// TypeArgumentsOrDiamond = TypeArguments
		//                        | "<" , ">" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void fieldAccess(NonterminalNode parent) {
		// FieldAccess = Primary , "." , Identifier
		//             | "super" , "." , Identifier
		//             | TypeName , "." , "super" , "." , Identifier ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void arrayAccess(NonterminalNode parent) {
		// ArrayAccess = ExpressionName , "[" , Expression , "]"
		//             | PrimaryNoNewArray , "[" , Expression , "]" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void methodReference(NonterminalNode parent) {
		// MethodReference = ExpressionName , "::" , [ TypeArguments ] , Identifier
		//                 | ReferenceType , "::" , [ TypeArguments ] , Identifier
		//                 | Primary , "::" , [ TypeArguments ] , Identifier
		//                 | "super" , "::" , [ TypeArguments ] , Identifier
		//                 | TypeName , "." , "super" , "::" , [ TypeArguments ] , Identifier
		//                 | ClassType , "::" , [ TypeArguments ] , "new"
		//                 | ArrayType , "::" , "new" ;


		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void arrayCreationExpression(NonterminalNode parent) {
		// ArrayCreationExpression = "new" , PrimitiveType , DimExprs , [ Dims ]
		//                         | "new" , ClassOrInterfaceType , DimExprs , [ Dims ]
		//                         | "new" , PrimitiveType , Dims , ArrayInitializer
		//                         | "new" , ClassOrInterfaceType , Dims , ArrayInitializer ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void dimExprs(NonterminalNode parent) {
		// DimExprs = DimExpr , { DimExpr } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void dimExpr(NonterminalNode parent) {
		// DimExpr = { Annotation } , "[" , Expression , "]" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void lambdaExpression(NonterminalNode parent) {
		// LambdaExpression = LambdaParameters , "->" , LambdaBody ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void lambdaParameters(NonterminalNode parent) {
		// LambdaParameters = Identifier
		//                  | "(" , [ FormalParameterList ] , ")"
		//                  | "(" , InferredFormalParameterList , ")" ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void inferredFormalParameterList(NonterminalNode parent) {
		// InferredFormalParameterList = Identifier , { "," , Identifier } ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}
	private void lambdaBody(NonterminalNode parent) {
		// LambdaBody = Expression
		//            | Block ;
		error("Nonterminal " + parent.getValue() + " is not supported.");
	}

}




// very important info below




























































// made you look