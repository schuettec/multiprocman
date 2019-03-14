package livefilereader;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.font.TextLayout;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
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

					String projectDir = new File(".").getCanonicalPath();
					String jre = System.getProperty("java.home");

					String[] commandRandom = new String[] {
					    jre + "/bin/java.exe", "-jar", projectDir + "/randomOutput.jar"
					};

					String[] commandProgress = new String[] {
					    jre + "/bin/java.exe", "-jar", projectDir + "/ConsoleTest.jar"
					};

					String[] commandService = new String[] {
					    jre + "/bin/java.exe", "-Dorg.gradle.appname=user-service", "-classpath",
					    "C:\\Users\\schuettec\\git\\user-service\\gradle\\wrapper\\gradle-wrapper.jar",
					    "org.gradle.wrapper.GradleWrapperMain", "bootrun"
					};

					String[] command = commandService;

					ProcessBuilder builder = new ProcessBuilder(command);
					builder.directory(new File(System.getProperty("user.home") + "/git/user-service"));
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

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.controller = controller;

		JScrollBar lineScroller = controller.getLineScroller();
		JTextPane textView = controller.getTextView();
		JScrollPane horizontalScroller = new JScrollPane(textView, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
		    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		horizontalScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		contentPane.add(horizontalScroller, BorderLayout.CENTER);
		contentPane.add(lineScroller, BorderLayout.EAST);
	}

}
