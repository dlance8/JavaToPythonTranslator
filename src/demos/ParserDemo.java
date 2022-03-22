package demos;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import processes.Lexer;
import processes.Parser;
import datastructures.tree.NonterminalNode;
public class ParserDemo {

	private static final boolean USE_DEBUG_UI = false;

	public static void main(String[] args) {
		Parser parser = new Parser();
		NonterminalNode root = parser.parse(new Lexer().lexFromFile("src/examples/testing.java"));
		//root.tighten();
		System.out.println(root);
		if (USE_DEBUG_UI) {
			//ParserDemoWithDebugUI.launch(args, parser.debugRoot);
		}
	}

	public static class ParserDemoWithDebugUI extends Application {
		private static TreeItem<String> treeRoot;
		private static void launch(String[] args, TreeItem<String> treeRoot) {
			ParserDemoWithDebugUI.treeRoot = treeRoot;
			Application.launch(args);
		}
		@Override
		public void start(Stage primaryStage) {
			primaryStage.setScene(new Scene(new Group(new TreeView<String>(treeRoot){{
				prefWidthProperty().bind(primaryStage.widthProperty());
				prefHeightProperty().bind(primaryStage.heightProperty());
			}}), 900, 900));
			primaryStage.show();
		}
	}
}

