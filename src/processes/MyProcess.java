package processes;
import app.ProcessManager;
public abstract class MyProcess {
	private final Thread processManagementThread;
	protected MyProcess(Thread processManagementThread) {
		this.processManagementThread = processManagementThread;
	}

	protected MyProcess() {
		processManagementThread = null;
	}

	private String errorMessage;
	protected void error(String errorMessage) {
		this.errorMessage = errorMessage;
		if (processManagementThread != null) {
			processManagementThread.interrupt();
		}
		Thread.currentThread().interrupt();
	}
	public String getErrorMessage() {
		return errorMessage;
	}
}