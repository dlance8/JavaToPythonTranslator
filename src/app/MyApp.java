package app;
import com.sun.webkit.WebPage;
import javafx.application.Application;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import javafx.*;

import javax.swing.*;

public class MyApp extends Application {

	// Class attributes
	private boolean javaIsSaved = true, pythonIsSaved = true;
	private File javaFile, pythonFile;
	private Stage primaryStage;

	private final FileChooser javaFileChooser = new FileChooser(), pythonFileChooser = new FileChooser();
	private final TextArea javaArea = new TextArea(), pythonArea = new TextArea();
	private final Alert alert = new Alert(null, "Unsaved Java and Python code will be lost.", ButtonType.OK, ButtonType.CANCEL);


	/**
	 * Code for user interface
	 * @param
	 */
	@Override
	public void start(Stage primaryStage) {

		this.primaryStage = primaryStage;
		primaryStage.setTitle("Java To Python Translator ");
		primaryStage.getIcons().add(new Image("examples/img_1.png"));

		final FileChooser.ExtensionFilter
			allFiles = new FileChooser.ExtensionFilter("All Files", "*.*"),
			javaFiles = new FileChooser.ExtensionFilter("Java Files", "*.java"),
			pythonFiles = new FileChooser.ExtensionFilter("Python Files", "*.py");

		javaFileChooser.setTitle("Choose a Java file:");
		javaFileChooser.getExtensionFilters().addAll(javaFiles, allFiles);

		pythonFileChooser.setTitle("Choose a Python file:");
		pythonFileChooser.getExtensionFilters().addAll(pythonFiles, allFiles);



		// Vertical container for application
		VBox vBox = new VBox();

		// Menu bar to hold all of our menus(buttons)
		MenuBar menuBar = new MenuBar();

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

		// Load example files menu
		Menu menu4 = new Menu("Examples");
//Lesly
		MenuItem ex1 = new MenuItem("Class Methods");
		File ex1file = new File("src/examples/classMethods.java");

		MenuItem ex2 = new MenuItem("Variable Declaration");
		File ex2file = new File("src/examples/variableDeclaration.java");

		MenuItem ex3 = new MenuItem("If-Else");
		File ex3file = new File("src/examples/ifElse.java");

		MenuItem ex4 = new MenuItem("While Loops");
		File ex4file = new File("src/examples/whileLoops.java");

		MenuItem ex5 = new MenuItem("Testing");
		File ex5file = new File("src/examples/testing.java");

		ex1.setOnAction(e -> openExample(ex1file));
		ex2.setOnAction(e -> openExample(ex2file));
		ex3.setOnAction(e -> openExample(ex3file));
		ex4.setOnAction(e -> openExample(ex4file));
		ex5.setOnAction(e -> openExample(ex5file));

		menu4.getItems().addAll(ex1, ex2, ex3, ex4, ex5);
//Lesly
		Menu menu5 = new Menu("Settings");
		MenuItem zoomIn = new MenuItem("Zoom In");
		MenuItem zoomOut = new MenuItem("Zoom Out");
		Menu changeBackgroundmenu = new Menu("Change Background");
		MenuItem lightBackground = new MenuItem("Light");

		MenuItem darkBackground = new MenuItem("Dark");
		menu5.getItems().addAll(zoomIn,zoomOut,changeBackgroundmenu);


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





		Menu menu6 = new Menu("Help");
		MenuItem documentation = new MenuItem("Documentation");
		File documentationfile = new File("src/examples/README.md");

		MenuItem about = new MenuItem("About");
		File aboutfile = new File("src/examples/classMethods.java");

		MenuItem reportBug = new MenuItem("Report Bug");

		//File reportBugfile = new File("https://github.com/Lesly412/IntegerArray_withTarget/issues");;

		documentation.setOnAction(e -> openExample(documentationfile));
		about.setOnAction(e -> openExample(aboutfile));
		//ERROR @here :
		// reportBug.setOnAction(e -> 	new URL("https://github.com/Lesly412/IntegerArray_withTarget/issues"));

		String lightStyle = javaArea.getStyle();
		darkBackground.setOnAction(e ->javaArea.setStyle("-fx-control-inner-background:#000000; -fx-font-family: Consolas; -fx-highlight-fill: #00ff00; -fx-highlight-text-fill: #000000; -fx-text-fill: #00ff00; "));
		lightBackground.setOnAction(e ->javaArea.setStyle(lightStyle));

		changeBackgroundmenu.getItems().addAll(lightBackground,darkBackground);

		menu6.getItems().addAll(documentation,about,reportBug);


		MenuBar menuBar2 = new MenuBar();
		menuBar2.getMenus().addAll(menu5, menu6);
		menuBar2.setPrefWidth(600);
		//menuBar2.setPrefHeight(600);


		// ADD ALL MENUS TO THE MENU BAR

		menuBar.getMenus().addAll(menu2, menu3, menu4);
		HBox hBoxMenu = new HBox();
		Button translateButton = new Button("Translate");

		translateButton.setOnAction(e -> translate());

		hBoxMenu.getChildren().addAll(menuBar, translateButton, menuBar2);


		// Horizontal container to hold text areas
		HBox hBox = new HBox();
		hBox.getChildren().addAll(javaArea, pythonArea);

		// Add the menu bar and text areas to the vertical container
		vBox.getChildren().addAll(hBoxMenu, hBox);


		pythonArea.setEditable(false);

		// Set text area dimensions
		DoubleBinding textAreaWidth = primaryStage.widthProperty().divide(2);
		DoubleBinding textAreaHeight = primaryStage.heightProperty().subtract(menuBar.heightProperty());
		javaArea.prefWidthProperty().bind(textAreaWidth);
		javaArea.prefHeightProperty().bind(textAreaHeight);
		pythonArea.prefWidthProperty().bind(textAreaWidth);
		pythonArea.prefHeightProperty().bind(textAreaHeight);

		// Set fonts
		Font font = Font.font("Monospaced", null, null, 20);
		javaArea.setFont(font);
		pythonArea.setFont(font);

		// Set up scene
		Scene scene = new Scene(vBox, 1280, 720);

		scene.setOnKeyPressed(e -> {
			if (e.isControlDown() && e.getCode() == KeyCode.S) {
				saveJava();
			}
		});

		primaryStage.setScene(scene);

		primaryStage.show();

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

	/**
	 * Code for event handlers.
	 * Defines read/write and button functionalities
	 */


	// Updates title of application when a java or python file is saved.
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

	// Writes the translated python code to the python text area
	private void translate() {
		pythonArea.setText(new ProcessManager().go(javaArea.getText()));
		pythonIsSaved = false;
		updateTitle();
	}

	// Functionality for 'New Java File' button
	private void newJava() {
		if (confirmFails()) return; // Make sure the work is saved

		javaFile = pythonFile = null;
		javaArea.clear();
		pythonArea.clear();
		javaIsSaved = pythonIsSaved = true;
		updateTitle();
	}

	// Functionality for 'Open Java File' button
	private void openJava() {
		if (confirmFails()) return; // Make sure the work is saved

		File file = javaFileChooser.showOpenDialog(primaryStage);
		if (file != null) {
			javaFile = file;
			javaArea.setText(readFile(file));
			javaIsSaved = true;
			updateTitle();
		}
	}

	// Functionality for 'Save Java File' button
	private void saveJava() {
		if (javaFile == null) {
			saveJavaAs();
		} else {
			writeFile(javaFile, javaArea.getText());
			javaIsSaved = true;
			updateTitle();
		}
	}

	// Functionality for 'Save as Java File' button
	private void saveJavaAs() {
		final File selectedFile = javaFileChooser.showSaveDialog(primaryStage);
		if (selectedFile != null) {
			javaFile = selectedFile;
			saveJava();
		}
	}

	// Functionality for 'Save Python File' button
	private void savePython() {
		if (pythonFile == null) {
			savePythonAs();
		} else {
			writeFile(pythonFile, pythonArea.getText());
			pythonIsSaved = true;
			updateTitle();
		}
	}

	// Functionality for 'Save Python File' button
	private void savePythonAs() {
		final File selectedFile = pythonFileChooser.showSaveDialog(primaryStage);
		if (selectedFile != null) {
			pythonFile = selectedFile;
			savePython();
		}
	}

	/**
	 * Reads a java file
	 *
	 * @param file
	 * @return Stringbuilder object as String
	 */
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

	// Functionality for 'Examples' button
	private void openExample(File example_file) {

		javaArea.setText(readFile(example_file));

	}
}