package app;
import javafx.application.Application;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
public class MyApp extends Application {
	private boolean javaIsSaved = true, pythonIsSaved = true;
	private File javaFile, pythonFile;
	private File workingDirectory = new File(System.getProperty("user.dir"));
	private Stage primaryStage;
	private final FileChooser javaFileChooser = new FileChooser();
	private final FileChooser pythonFileChooser = new FileChooser();

	private final TextArea javaArea = new TextArea(), pythonArea = new TextArea();
	private final Alert alert = new Alert(null, "Unsaved Java and Python code will be lost.", ButtonType.OK, ButtonType.CANCEL);



	@Override
	public void start(Stage primaryStage) {

		this.primaryStage = primaryStage;

		updateTitle();

		final FileChooser.ExtensionFilter
			allFiles = new FileChooser.ExtensionFilter("All Files", "*.*"),
			javaFiles = new FileChooser.ExtensionFilter("Java Files", "*.java"),
			pythonFiles = new FileChooser.ExtensionFilter("Python Files", "*.py");


		javaFileChooser.setTitle("Choose a Java file:");
		javaFileChooser.getExtensionFilters().addAll(javaFiles, allFiles);

		pythonFileChooser.setTitle("Choose a Python file:");
		pythonFileChooser.getExtensionFilters().addAll(pythonFiles, allFiles);

		VBox vBox = new VBox();

		MenuBar menuBar = new MenuBar();

		// Translate menu
		Menu menu1 = new Menu("Translate");
		MenuItem menu1a = new MenuItem("Translate");
		menu1a.setOnAction(e -> translate());


		menu1.getItems().addAll(menu1a);

		// Java file options menu
		Menu menu2 = new Menu("Java File");

		MenuItem menu2a = new MenuItem("New");
		menu2a.setOnAction(e -> newJava());


		MenuItem menu2c = new MenuItem("Open");
		menu2c.setOnAction(e -> openJava());

		MenuItem menu2d = new MenuItem("Save");
		menu2d.setOnAction(e -> saveJava());

		MenuItem menu2e = new MenuItem("Save As");
		menu2e.setOnAction(e -> saveJavaAs());

		menu2.getItems().addAll(menu2a, menu2c, menu2d, menu2e);

		// Python file options menu
		Menu menu3 = new Menu("Python File");

		MenuItem menu3a = new MenuItem("Save");
		menu3a.setOnAction(e -> savePython());

		MenuItem menu3b = new MenuItem("Save As");
		menu3b.setOnAction(e -> savePythonAs());

		menu3.getItems().addAll(menu3a, menu3b);

		// MENU 4
		Menu menu4 = new Menu("Examples");

		MenuItem ex1 = new MenuItem("Class Methods");
		File ex1file = new File("src/examples/classMethods.java");

		MenuItem ex2 = new MenuItem("Variable Declaration");
		File ex2file = new File("src/examples/variableDeclaration.java");

		MenuItem ex3 = new MenuItem("If/else Statements");
		File ex3file = new File("src/examples/ifElse.java");

		MenuItem ex4 = new MenuItem("Loops");
		File ex4file = new File("src/examples/whileLoops.java");

		MenuItem ex5 = new MenuItem("Testing");
		File ex5file = new File("src/examples/testing.java");


		ex1.setOnAction(e -> openExample(ex1file));
		ex2.setOnAction(e -> openExample(ex2file));
		ex3.setOnAction(e -> openExample(ex3file));
		ex4.setOnAction(e -> openExample(ex4file));
		ex5.setOnAction(e -> openExample(ex5file));

		menu4.getItems().addAll(ex1, ex2, ex3, ex4, ex5);

		// ADD ALL MENUS TO THE MENU BAR

		menuBar.getMenus().addAll(menu1, menu2, menu3, menu4);





		HBox hBox = new HBox();
		hBox.getChildren().addAll(javaArea, pythonArea);

		//pythonArea.setEditable(false);

		vBox.getChildren().addAll(menuBar, hBox);

		pythonArea.setEditable(false);

		DoubleBinding textAreaWidth = primaryStage.widthProperty().divide(2);
		DoubleBinding textAreaHeight = primaryStage.heightProperty().subtract(menuBar.heightProperty());

		javaArea.prefWidthProperty().bind(textAreaWidth);
		javaArea.prefHeightProperty().bind(textAreaHeight);
		pythonArea.prefWidthProperty().bind(textAreaWidth);
		pythonArea.prefHeightProperty().bind(textAreaHeight);

		Font font = Font.font("Monospaced", null, null, 20);
		javaArea.setFont(font);
		pythonArea.setFont(font);


		Scene scene = new Scene(vBox, 1280, 720);

		scene.setOnKeyPressed(e -> {
			if (e.isControlDown() && e.getCode() == KeyCode.S) {
				saveJava();
			}
		});

		primaryStage.setScene(scene);

		primaryStage.show();

//		javaFile = new File("C:/DistributedProject/examples.MyClass.java");
//		javaArea.setText(readFile(javaFile));
//		translate();

		javaArea.textProperty().addListener((o, ov, nv)-> {
			if (javaIsSaved) {
				javaIsSaved = false;
				updateTitle();
			}
		});

		primaryStage.setOnCloseRequest(e -> {
			if (confirmFails()) {
				e.consume();
			}
		});
	}
	private void updateTitle() {
		StringBuilder stringBuilder = new StringBuilder();
		if (!javaIsSaved) {
			stringBuilder.append("*");
		}
		if (javaFile == null) {
			stringBuilder.append("New Java File");
		} else {
			stringBuilder.append(javaFile.getName());
		}
		stringBuilder.append(" | ");
		if (!pythonIsSaved) {
			stringBuilder.append("*");
		}
		if (pythonFile == null) {
			stringBuilder.append("New Python File");
		} else {
			stringBuilder.append(pythonFile.getName());
		}
		primaryStage.setTitle(stringBuilder.toString());
	}
	private boolean confirmFails() {
		return !(javaIsSaved && pythonIsSaved) && alert.showAndWait().get() != ButtonType.OK;
	}
	private void translate() {
		pythonArea.setText(new ProcessManager().go(javaArea.getText()));
		pythonIsSaved = false;
		updateTitle();
	}
	private void newJava() {
		if (confirmFails()) return; // Make sure the work is saved

		javaFile = pythonFile = null;
		javaArea.clear();
		pythonArea.clear();
		javaIsSaved = pythonIsSaved = true;
		updateTitle();
	}
	private void openJava() {
		if (confirmFails()) return; // Make sure the work is saved

		javaFileChooser.setInitialDirectory(workingDirectory);
		File file = javaFileChooser.showOpenDialog(primaryStage);
		if (file != null) {
			javaFile = file;
			javaArea.setText(readFile(file));
			javaIsSaved = true;
			updateTitle();
		}
	}
	private void saveJava() {
		if (javaFile == null) {
			saveJavaAs();
		} else {
			writeFile(javaFile, javaArea.getText());
			javaIsSaved = true;
			updateTitle();
		}
	}
	private void saveJavaAs() {
		final File selectedFile = javaFileChooser.showSaveDialog(primaryStage);
		if (selectedFile != null) {
			javaFile = selectedFile;
			saveJava();
		}
	}
	private void savePython() {
		if (pythonFile == null) {
			savePythonAs();
		} else {
			writeFile(pythonFile, pythonArea.getText());
			pythonIsSaved = true;
			updateTitle();
		}
	}
	private void savePythonAs() {
		final File selectedFile = pythonFileChooser.showSaveDialog(primaryStage);
		if (selectedFile != null) {
			pythonFile = selectedFile;
			savePython();
		}
	}
	private String readFile(File file) {
		Scanner scanner;
		try {
			scanner = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
		StringBuilder stringBuilder = new StringBuilder();
		// For each line of the file, add that line's contents to the StringBuilder, then add a line break.
		while (scanner.hasNextLine()) {
			stringBuilder.append(scanner.nextLine()).append('\n');
		}
		scanner.close();
		// The above process will have one extra line-break at the very end (for non-empty files),
		// so this will remove that last line break to completely match the file contents
		if (stringBuilder.length() > 0) {
			stringBuilder.setLength(stringBuilder.length() - 1);
		}
		return stringBuilder.toString();
	}
	private void writeFile(File file, String text) {
		PrintWriter printWriter;
		try {
			printWriter = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
			return;
		}
		printWriter.print(text);
		printWriter.close();
	}



	private void openExample(File example_file) {

		javaArea.setText(readFile(example_file));

	}
}