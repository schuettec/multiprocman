package livefilereader;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.font.TextLayout;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

/**
 * Use {@link TextLayout} to render text.
 */
public class ReaderUI extends JFrame {

	private JPanel contentPane;
	private ReaderController controller;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {

					String[] commandRandom = new String[] {
					    "C:\\Program Files\\Java\\jdk1.8.0_191\\bin\\java.exe", "-jar",
					    "C:\\Users\\schuettec\\git\\multiprocman\\randomOutput.jar"
					};

					String[] commandProgress = new String[] {
					    "C:\\Program Files\\Java\\jdk1.8.0_191\\bin\\java.exe", "-jar",
					    "C:\\Users\\schuettec\\git\\multiprocman\\outputtest.jar"
					};

					String[] command = commandRandom;

					ProcessBuilder builder = new ProcessBuilder(command);
					builder.redirectErrorStream(true);

					ReaderController controller = new ReaderController();
					ProcessObserver observer = new ProcessObserverImpl(builder, new File("output.txt"), controller);
					observer.startProcess();

					ReaderUI frame = new ReaderUI(controller);
					frame.setVisible(true);

					Runtime.getRuntime()
					    .addShutdownHook(new Thread(new Runnable() {
						    @Override
						    public void run() {
							    observer.stopProcess();
						    }
					    }));

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ReaderUI(ReaderController controller) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		this.controller = controller;

		JScrollBar lineScroller = controller.getLineScroller();
		JTextPane textView = controller.getTextView();
		contentPane.add(textView, BorderLayout.CENTER);
		contentPane.add(lineScroller, BorderLayout.EAST);
	}

}
