package app;
import javafx.application.Application;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
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
	private String pythonSaveLocation;

	@Override
	public void start(Stage primaryStage) {

		this.primaryStage = primaryStage;
		primaryStage.setTitle("Java to Python Translator");
		primaryStage.getIcons().add(new Image("examples/img_1.png"));


		//updateTitle();

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
		ButtonBar buttonbar = new ButtonBar();

		/**
		 * Java Menu
		 */
		Menu jMenu = new Menu("Java File");
		MenuItem jMenu_New = new MenuItem("New");
		jMenu_New.setOnAction(e -> newJava());

		MenuItem jMenu_Open = new MenuItem("Open");
		jMenu_Open.setOnAction(e -> openJava());

		MenuItem jMenu_Save = new MenuItem("Save");
		jMenu_Save.setOnAction(e -> saveJava());

		MenuItem jMenu_SaveAs = new MenuItem("Save As");
		jMenu_SaveAs.setOnAction(e -> saveJavaAs());

		jMenu.getItems().addAll(jMenu_New, jMenu_Open, jMenu_Save, jMenu_SaveAs);

		/**
		 * Translate Menu
		 */
//		Menu menu1 = new Menu("Translate");
//		MenuItem menu1a = new MenuItem("Translate");
//		menu1a.setOnAction(e -> translate());
//
//
//		menu1.getItems().addAll(menu1a);

		/**
		 * Buttons for cmdline + idle
		 */
		Button cmd = new Button("Run");
		cmd.setOnAction(e -> run());
		Button idle = new Button("Idle");
		idle.setOnAction(e -> runidle());


		// Java file options menu
//		Menu menu2 = new Menu("Java File");
//
//		MenuItem menu2a = new MenuItem("New");
//		menu2a.setOnAction(e -> newJava());
//
//
//		MenuItem menu2c = new MenuItem("Open");
//		menu2c.setOnAction(e -> openJava());
//
//		MenuItem menu2d = new MenuItem("Save");
//		menu2d.setOnAction(e -> saveJava());
//
//		MenuItem menu2e = new MenuItem("Save As");
//		menu2e.setOnAction(e -> saveJavaAs());
//
//		menu2.getItems().addAll(menu2a, menu2c, menu2d, menu2e);

		/**
		 * Python file options menu
		 */
		Menu pyMenu = new Menu("Python File");

		MenuItem pMenu_Save = new MenuItem("Save");
		pMenu_Save.setOnAction(e -> savePython());

		MenuItem pMenu_SaveAs = new MenuItem("Save As");
		pMenu_SaveAs.setOnAction(e -> savePythonAs());

		pyMenu.getItems().addAll(pMenu_Save, pMenu_SaveAs);

		/**
		 * Examples Menu
		 */

		Menu exMenu = new Menu("Examples");

		//Menu subMenu = new Menu("Fundamentals");

		MenuItem ex1 = new MenuItem("Class Methods");
		File ex1file = new File("src/examples/classMethods.java");
		ex1.setOnAction(e -> openExample(ex1file));

		MenuItem ex2 = new MenuItem("Variable Declaration");
		File ex2file = new File("src/examples/variableDeclaration.java");
		ex2.setOnAction(e -> openExample(ex2file));

		MenuItem ex3 = new MenuItem("If/else Statements");
		File ex3file = new File("src/examples/ifElse.java");
		ex3.setOnAction(e -> openExample(ex3file));

		MenuItem ex4 = new MenuItem("While Loops");
		File ex4file = new File("src/examples/whileLoops.java");
		ex4.setOnAction(e -> openExample(ex4file));

		MenuItem ex5 = new MenuItem("For Loops");
		File ex5file = new File("src/examples/forLoops.java");
		ex5.setOnAction(e -> openExample(ex5file));

//		MenuItem ex6 = new MenuItem("Testing");
//		File ex6file = new File("src/examples/testing.java");
//		ex6.setOnAction(e -> openExample(ex6file));

		MenuItem ex7 = new MenuItem("Objects");
		File ex7file = new File("src/examples/objects.java");
		ex7.setOnAction(e -> openExample(ex7file));

		MenuItem ex8 = new MenuItem("Binary Search");
		File ex8file = new File("src/examples/BinarySearch.java");
		ex8.setOnAction(e -> openExample(ex8file));

//		MenuItem program1 = new MenuItem("EvenOdd");
//		File evenOdd = new File("src/examples/EvenOdd.java");
//		program1.setOnAction(e -> openExample(evenOdd));

//		subMenu.getItems().addAll(ex1, ex2, ex3, ex4, ex5, ex7);

		exMenu.getItems().addAll(ex1, ex2, ex3, ex4, ex5, ex7, ex8);

		/**
		 * Settings Menu
		 */

		Menu settingsMenu = new Menu("Settings");
		MenuItem zoomIn = new MenuItem("Zoom In");
		MenuItem zoomOut = new MenuItem("Zoom Out");
		Menu changeBackgroundmenu = new Menu("Change Background");
		MenuItem lightBackground = new MenuItem("Light");
		MenuItem darkBackground = new MenuItem("Dark");
		settingsMenu.getItems().addAll(zoomIn,zoomOut,changeBackgroundmenu);

		StringBuffer st = new StringBuffer("-fx-font-size:20");
		zoomIn.setOnAction(event -> {
			String st3 = st.substring(14,16);
			Integer x = Integer.parseInt(st3) + 5;
			st.replace(14,16,x.toString());

			javaArea.setStyle(st.toString());
		});

		zoomOut.setOnAction(event -> {
			String st3 = st.substring(14,16);
			Integer x = Integer.parseInt(st3) - 5;
			st.replace(14,16,x.toString());

			javaArea.setStyle(st.toString());
		});

		/**
		 * Help Menu
		 */
		Menu helpMenu = new Menu("Help");

		MenuItem documentation = new MenuItem("Documentation");
		File documentationfile = new File("src/examples/README.md");
		documentation.setOnAction(e -> openExample(documentationfile));

		MenuItem about = new MenuItem("About");
		File aboutfile = new File("examples/j2p_html.html");
		//about.setOnAction(e -> getHostServices().showDocument("D:\\Documents\\GitHub\\JavaToPythonTranslator\\src\\examples\\j2p_html.html"));
		about.setOnAction(e -> getHostServices().showDocument(new File("src/examples/j2p_html.html").getAbsolutePath()));
		MenuItem reportBug = new MenuItem("Report Issue");
		reportBug.setOnAction(e -> getHostServices().showDocument("https://github.com/dlance8/JavaToPythonTranslator/issues"));

		/**
		 * Change background colors
		 */
		String lightStyle = javaArea.getStyle();
		darkBackground.setOnAction(e ->javaArea.setStyle("-fx-control-inner-background:#000000; -fx-font-family: Consolas; -fx-highlight-fill: #00ff00; -fx-highlight-text-fill: #000000; -fx-text-fill: #00ff00; "));
		lightBackground.setOnAction(e ->javaArea.setStyle(lightStyle));

		changeBackgroundmenu.getItems().addAll(lightBackground,darkBackground);

		helpMenu.getItems().addAll(documentation,about,reportBug);

		// ADD ALL MENUS TO THE MENU BAR
		menuBar.getMenus().addAll(jMenu, pyMenu, exMenu);
		HBox hBoxMenu = new HBox();

		MenuBar menuBar2 = new MenuBar();
		menuBar2.getMenus().addAll(settingsMenu, helpMenu);
		menuBar2.setPrefWidth(600);

		buttonbar.getButtons().addAll(cmd, idle);
		Button translateButton = new Button("Translate");
		translateButton.setOnAction(e -> translate());

		hBoxMenu.getChildren().addAll(menuBar, translateButton, menuBar2);

		HBox hBox = new HBox();
		hBox.getChildren().addAll(javaArea, pythonArea);

		vBox.getChildren().addAll(hBoxMenu, hBox, buttonbar);
		//vBox.getChildren().addAll(menuBar, hBox, buttonbar);

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

	private void run() {
		if(!pythonIsSaved){
			savePython();
		}
		else if(pythonFile == null){
			displayTextWindow("Kinda weird you're trying to run an empty file, but lets see what happens.\n\nAttempting to run an empty python file...\nnothing interesting happens");
		}
		else{
			try {
				StringBuilder sb = new StringBuilder();
				ProcessBuilder builder = new ProcessBuilder(
						"cmd.exe", "/c", "python " + pythonSaveLocation);
				builder.redirectErrorStream(true);
				Process p = builder.start();
				BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while (true) {
					line = r.readLine();
					if (line == null) { break; }
					sb.append(line).append("\n");
					//System.out.println(line);
				}
				//System.out.println(sb.toString());
				displayTextWindow(sb.toString());

			}
			catch (Exception e)
			{
				System.out.println("An error has occurred");
				e.printStackTrace();
			}
		}
	}

	private void runidle(){
		try {
			ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "python -m idlelib");
			Process process = pb.start();
		}
		catch (Exception e)
		{
			System.out.println("An error has occurred");
			e.printStackTrace();
		}
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
		pythonFileChooser.setInitialDirectory(new File("src/pyFiles"));
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
			pythonSaveLocation = pythonFile.getPath();
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
	private void displayTextWindow(String text) {
		Stage stage = new Stage();
		stage.setTitle("Python Output");
		TextArea textArea = new TextArea(text);
		textArea.setFont(Font.font("Monospaced", null, null, 20));

		textArea.setEditable(false);
		textArea.prefWidthProperty().bind(stage.widthProperty());
		textArea.prefHeightProperty().bind(stage.heightProperty());

		stage.setScene(new Scene(new Pane(textArea), 500, 500));
		stage.show();
	}

}