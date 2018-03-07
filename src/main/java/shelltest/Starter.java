package shelltest;

import java.io.File;
import java.io.InputStream;

public class Starter {

  private static final int Y_INCREMENT = 1;
  private static final int X_INCREMENT = 1;
  private static final String APPHOME = "C:\\Users\\schuettec\\git\\user-service";

  // public static void main(String[] args) throws Exception {
  // SwingUtilities.invokeLater(new Runnable() {
  // @Override
  // public void run() {
  // JFrame frame = new JFrame();
  // JTextPane text = new JTextPane();
  // text.set
  // JScrollPane scroller = new JScrollPane(text);
  // frame.add(scroller, BorderLayout.CENTER);
  // frame.pack();
  // frame.setVisible(true);
  // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  // }
  // });
  // }

  public static void main(String[] args) throws Exception {

    String classpath = APPHOME + "\\gradle\\wrapper\\gradle-wrapper.jar";
    ProcessBuilder pb = new ProcessBuilder("java.exe", "-Dorg.gradle.appname=user-service", "-classpath", classpath,
        "org.gradle.wrapper.GradleWrapperMain", "bootrun");
    pb.directory(new File(APPHOME));
    pb.redirectErrorStream(true);
    final Process process = pb.start();
    final InputStream inputStream = process.getInputStream();
    final InputStream errorStream = process.getErrorStream();
    Runtime.getRuntime()
        .addShutdownHook(new Thread(new Runnable() {

          @Override
          public void run() {
            process.destroy();
            try {
              System.out.println("Process destroyed. Exited with " + process.waitFor());
            } catch (InterruptedException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }
        }));

    // final InputStream inputStream = new ByteArrayInputStream(
    // "2018-02-12 13:12:30.115 [35m INFO[0;39m [renew-user-service] [,] 18244 --- [ost-startStop-1]
    // [34mb.a.s.AuthenticationManagerConfiguration[0;39m : [32m"
    // .getBytes());
    // final InputStream errorStream = new ByteArrayInputStream(
    // "2018-02-12 13:12:30.115 [35m INFO[0;39m [renew-user-service] [,] 18244 --- [ost-startStop-1]
    // [34mb.a.s.AuthenticationManagerConfiguration[0;39m : [32m"
    // .getBytes());

    // SwingUtilities.invokeLater(new Runnable() {
    // @Override
    // public void run() {
    // DragonConsoleFrame dcf = new DragonConsoleFrame();
    // final DragonConsole console = dcf.getConsole();
    // console.setUseANSIColorCodes(true);
    // console.setIgnoreInput(true);
    // dcf.setVisible(true);
    //
    // Thread t = new Thread(new Runnable() {
    //
    // @Override
    // public void run() {
    // Scanner input1 = new Scanner(inputStream);
    // Scanner input2 = new Scanner(errorStream);
    // while (!Thread.currentThread()
    // .isInterrupted()) {
    //
    // if (input1.hasNextLine()) {
    // String nextLine = input1.nextLine();
    // try {
    // console.append(nextLine + "\n");
    // } catch (Exception e) {
    // System.out.println("EIGENTLICH WOLLT ICK DETTE HIER AUSGEBEN: '" + nextLine + "'");
    // e.printStackTrace();
    // }
    // }
    //
    // if (input2.hasNextLine()) {
    // String nextLine = input2.nextLine();
    // try {
    // console.append(nextLine + "\n");
    // } catch (Exception e) {
    // e.printStackTrace();
    // System.out.println("EIGENTLICH WOLLT ICK DETTE HIER AUSGEBEN: '" + nextLine + "'");
    // }
    // }
    // }
    // }
    // });
    // t.start();
    //
    // final StyledDocument styledDocument = console.getStyledDocument();
    //
    // final Canvas canvas = new Canvas() {
    //
    // /**
    // *
    // */
    // private static final long serialVersionUID = 1L;
    //
    // @Override
    // public void paint(Graphics g) {
    // Graphics2D g2d = (Graphics2D) g;
    // g2d.setBackground(Color.BLACK);
    // super.paint(g);
    //
    // Position startPosition = styledDocument.getStartPosition();
    // Position endPosition = styledDocument.getEndPosition();
    // int start = startPosition.getOffset();
    // int end = endPosition.getOffset();
    //
    // int y = 1;
    // int x = 1;
    // for (int i = start; i < end; i++) {
    // Element charElem = styledDocument.getCharacterElement(i);
    // String text = null;
    // try {
    // text = styledDocument.getText(i, 1);
    // } catch (BadLocationException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // if (text.equals("\n") || text.equals("\r\n")) {
    // y += Y_INCREMENT;
    // x = 1;
    // } else {
    // AttributeSet attributes = charElem.getAttributes();
    // Color foreground = StyleConstants.getForeground(attributes);
    // Color backGround = StyleConstants.getBackground(attributes);
    // g2d.setBackground(backGround);
    // g2d.setColor(foreground);
    // g2d.drawLine(x, y, x, y);
    // }
    // x += X_INCREMENT;
    // }
    // }
    // };
    //
    // JDialog dialog = new JDialog();
    // dialog.setPreferredSize(new Dimension(100, 100));
    // dialog.add(canvas, BorderLayout.CENTER);
    // dialog.pack();
    // dialog.setVisible(true);
    //
    // styledDocument.addDocumentListener(new DocumentListener() {
    //
    // @Override
    // public void insertUpdate(DocumentEvent e) {
    // canvas.repaint();
    // }
    //
    // @Override
    // public void removeUpdate(DocumentEvent e) {
    // canvas.repaint();
    // }
    //
    // @Override
    // public void changedUpdate(DocumentEvent e) {
    // canvas.repaint();
    // }
    //
    // });
    //
    // }
    // });

  }
}
