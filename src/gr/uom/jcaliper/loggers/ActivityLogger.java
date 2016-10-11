package gr.uom.jcaliper.loggers;

import java.awt.Font;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author Panagiotis Kouros
 */
public class ActivityLogger extends Handler {

	private static final int FWIDTH = 600;
	private static final int FHEIGHT = 400;
	private LogFrame window = null;
	private Logger logger = null;

	// the singleton instance
	private static ActivityLogger handler = new ActivityLogger();

	private ActivityLogger() {
		super();
		setFormatter(new MyTinyFormatter());
		if (window == null)
			window = new LogFrame("Activity log", FWIDTH, FHEIGHT);
		window.setVisible(false);
		logger = Logger.getLogger("Activity_logger");
		for (Handler iHandler : logger.getParent().getHandlers())
			logger.getParent().removeHandler(iHandler);
		logger.addHandler(this);
	}

	public static synchronized ActivityLogger getInstance() {

		if (handler == null) {
			handler = new ActivityLogger();
		}
		return handler;
	}

	public Logger getLogger() {
		return logger;
	}

	public static synchronized void info(String message) {
		getInstance().logger.log(Level.INFO, message);
	}

	@Override
	public synchronized void publish(LogRecord record) {
		if (window == null)
			window = new LogFrame("Logging window", FWIDTH, FHEIGHT);
		window.setVisible(true);
		String message = null;
		// check if the record is loggable
		if (!isLoggable(record))
			return;
		try {
			message = getFormatter().format(record);
		} catch (Exception e) {
			reportError(null, e, ErrorManager.FORMAT_FAILURE);
		}

		try {
			window.showInfo(message);
		} catch (Exception ex) {
			reportError(null, ex, ErrorManager.WRITE_FAILURE);
		}

	}

	@Override
	public void close() {
	}

	@Override
	public void flush() {
	}

	private class MyTinyFormatter extends Formatter {
		@Override
		public String format(final LogRecord r) {
			return r.getMessage();
		}
	}

	private class LogFrame extends JFrame {

		private JTextArea textArea = null;
		private JScrollPane pane = null;

		LogFrame(String title, int width, int height) {
			super(title);
			setSize(width, height);
			textArea = new JTextArea();
			textArea.setEditable(false);
			textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
			pane = new JScrollPane(textArea);
			getContentPane().add(pane);
		}

		public void showInfo(String data) {
			textArea.append(data);
			JScrollBar vertical = pane.getVerticalScrollBar();
			vertical.setValue(vertical.getMaximum());
			getContentPane().validate();
		}

		private static final long serialVersionUID = 1L;

	}
}
