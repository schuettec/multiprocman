package livefilereader;

import java.io.File;

public class Starter {

	public static void main(String[] args) throws Exception {

		ProcessBuilder builder = new ProcessBuilder("C:\\Program Files\\Java\\jdk1.8.0_191\\bin\\java.exe", "-jar",
		    "C:\\Users\\schuettec\\git\\multiprocman\\randomOutput.jar");
		builder.redirectErrorStream(true);

		ProcessObserver observer = new ProcessObserverImpl(builder, new File("output.txt"), new ProcessCallback() {

			@Override
			public void output(long lines) {
				System.out.println("Lines read: " + lines);
			}

			@Override
			public void cannotWriteOutput(File outputFile, Exception cause) {
				System.out.println("Cannot write output file: " + outputFile.getAbsolutePath());
				cause.printStackTrace();
			}

			@Override
			public void cannotStartProcess(Exception cause) {
				System.out.println("Start process:");
				cause.printStackTrace();
			}
		});
		observer.startProcess();

		Runtime.getRuntime()
		    .addShutdownHook(new Thread(new Runnable() {

			    @Override
			    public void run() {
				    observer.stopProcess();
			    }
		    }));

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
