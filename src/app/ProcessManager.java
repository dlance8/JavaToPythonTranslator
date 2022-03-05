package app;
import processes.Lexer;
import processes.Parser;
import datastructures.Token;
import processes.Translator;
import datastructures.tree.NonterminalNode;
import java.util.ArrayList;
public final class ProcessManager {
	public static void main(String[] args) {
		System.out.println(new ProcessManager().go("\\u"));
	}

	private boolean failed, inProgress, running;
	private ArrayList<Token> lexerResult;
	private NonterminalNode parserResult;
	private String finalResult, translatorResult;
	private Thread processManagementThread;

	public String go(String input) {
		failed = false;
		running = true;
		(processManagementThread = new Thread(() -> {
			finalResult = runAllProcesses(input);
			running = false;
			synchronized (ProcessManager.this) {
				ProcessManager.this.notifyAll();
			}
		})).start();
		synchronized (ProcessManager.this) {
			while (running) {
				try {
					ProcessManager.this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		return finalResult;
	}
	private String runAllProcesses(String input) {

		final Lexer lexer = new Lexer(processManagementThread);
		inProgress = true;
		new Thread(() -> {
			lexerResult = lexer.lex(input);
			inProgress = false;
			synchronized (lexer) {
				lexer.notifyAll();
			}
		}).start();
		synchronized (lexer) {
			while (inProgress) {
				try {
					lexer.wait();
				} catch (InterruptedException e) {
					failed = true;
					return lexer.getErrorMessage();
				}
			}
		}

		final Parser parser = new Parser(processManagementThread);
		inProgress = true;
		new Thread(() -> {
			parserResult = parser.parse(lexerResult);
			inProgress = false;
			synchronized (parser) {
				parser.notifyAll();
			}
		}).start();
		synchronized (parser) {
			while (inProgress) {
				try {
					parser.wait();
				} catch (InterruptedException e) {
					failed = true;
					return parser.getErrorMessage();
				}
			}
		}

		final Translator translator = new Translator(processManagementThread);
		inProgress = true;
		new Thread(() -> {
			translatorResult = translator.translate(parserResult);
			inProgress = false;
			synchronized (translator) {
				translator.notifyAll();
			}
		}).start();
		synchronized (translator) {
			while (inProgress) {
				try {
					translator.wait();
				} catch (InterruptedException e) {
					failed = true;
					return translator.getErrorMessage();
				}
			}
		}

		return translatorResult;
	}
}