package shelltest;

import static java.util.Objects.requireNonNull;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import shelltest.console.AnsiColorTextPane;
import shelltest.console.AutoScrollToBottomListener;
import shelltest.console.ScrollableAnsiColorTextPaneContainer;
import shelltest.consolepreview.ConsolePreview;
import shelltest.themes.Theme;
import shelltest.themes.console.AnsiColorTextPaneTheme;

public class MainFrame extends JFrame implements Appendable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  AbstractAction scrollToBottomAction = new AbstractAction() {

    @Override
    public void actionPerformed(ActionEvent e) {
      JToggleButton source = (JToggleButton) e.getSource();
      consoleScroller.setAutoScrollToBottom(source.isSelected());
      consoleScroller.scrollToBottom();
    }
  };

  private static class ListeningToggleButtonModel extends JToggleButton.ToggleButtonModel
      implements AutoScrollToBottomListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void autoScrollToBottomProperty(Boolean value) {
      this.setSelected(value);
    }
  };

  private JPanel contentPane;
  private JPanel footerContainer;
  private JSeparator separator;
  private JPanel footer;
  private JToggleButton tglScrollToBottom;
  private ScrollableAnsiColorTextPaneContainer consoleScroller;

  private AnsiColorTextPane console;

  private ConsolePreview consolePreview;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          final MainFrame frame = new MainFrame();
          frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
          frame.setVisible(true);

          Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
              AtomicInteger index = new AtomicInteger();
              while (true) {
                EventQueue.invokeLater(new Runnable() {
                  @Override
                  public void run() {
                    Random r = new Random();
                    int i = index.incrementAndGet();
                    Color c = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
                    frame.append(c, i + " " + "Hallo\n");
                    c = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
                    frame.append(c, i + " " + "Duuuuuuuuuuuuuuuuuuu\nESEEEEEEEEEEEEEEEEEEEEEEEEEEL");
                    c = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
                    frame.append(c, i + " " + "MÖÖÖÖÖÖÖÖÖÖÖÖÖP\n");
                    c = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
                    frame.append(c, i + " " + "TEST\n");
                  }
                });
                try {
                  Thread.sleep(450);
                } catch (InterruptedException e) {
                }
                if (index.get() == 3) {
                  return;
                }
              }
            }

          });
          t.start();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

  }

  /**
   * Create the frame.
   */
  public MainFrame() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 450, 300);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    contentPane.setLayout(new BorderLayout(0, 0));
    setContentPane(contentPane);

    this.console = new AnsiColorTextPane();
    theme(console);

    this.consoleScroller = new ScrollableAnsiColorTextPaneContainer(console);
    contentPane.add(consoleScroller, BorderLayout.CENTER);

    this.consolePreview = new ConsolePreview();
    contentPane.add(consolePreview, BorderLayout.NORTH);
    this.console.addAppendListener(consolePreview);

    footerContainer = new JPanel();
    footerContainer.setBorder(null);
    contentPane.add(footerContainer, BorderLayout.SOUTH);
    footerContainer.setLayout(new BorderLayout(0, 0));

    separator = new JSeparator();
    separator.setPreferredSize(new Dimension(0, 3));
    separator.setOrientation(SwingConstants.VERTICAL);
    footerContainer.add(separator, BorderLayout.NORTH);

    footer = new JPanel();
    footerContainer.add(footer, BorderLayout.CENTER);
    footer.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));

    tglScrollToBottom = new JToggleButton(scrollToBottomAction);
    ListeningToggleButtonModel toggleModel = new ListeningToggleButtonModel();
    consoleScroller.addAutoScrollToBottomListener(toggleModel);
    tglScrollToBottom.setModel(toggleModel);
    footer.add(tglScrollToBottom);

  }

  private void theme(AnsiColorTextPane console) {
    theme(console, AnsiColorTextPaneTheme.class);
  }

  private <C extends JComponent> void theme(C component, Class<? extends Theme<C>> service) {
    requireNonNull(component, "The component to be themed may not be null.");
    requireNonNull(service, "The theme type may not be null.");
    ServiceLoader<? extends Theme<C>> themeLoader = ServiceLoader.load(service);
    Iterator<? extends Theme<C>> it = themeLoader.iterator();
    if (it.hasNext()) {
      Theme<C> theme = it.next();
      theme.apply(component);
    }
  }

  @Override
  public void append(Color c, String s) {
    this.consoleScroller.append(c, s);
  }

  @Override
  public void appendANSI(String s) {
    this.consoleScroller.appendANSI(s);
  }

}
