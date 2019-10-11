package com.github.schuettec.multiprocman;

import static java.util.Objects.requireNonNull;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class ExceptionDialog extends JDialog {

    private final JPanel contentPanel = new JPanel();
    private Action disposeAction = new AbstractAction("Close") {

        @Override
        public void actionPerformed(ActionEvent e) {
            ExceptionDialog.this.dispose();
        }
    };

    public static void showException(Component parent, Exception exception, String messageFormat, Object... param) {
        new ExceptionDialog(parent, String.format(messageFormat, param), exception);
    }

    public static void showException(Component parent, Exception exception, String message) {
        new ExceptionDialog(parent, message, exception);
    }

    /**
     * Create the dialog.
     */
    private ExceptionDialog(Component parent, String message, Exception exception) {
        requireNonNull(message, "Message must not be null.");
        requireNonNull(exception, "Exception must not be null.");
        setIconImage(Resources.getApplicationIcon());
        setModal(true);
        setTitle("Exception");
        setAlwaysOnTop(true);
        setAutoRequestFocus(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(480, 420));
        setSize(new Dimension(480, 420));
        setLocationRelativeTo(parent);

        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        JLabel lblIcon = new JLabel(UIManager.getIcon("OptionPane.errorIcon"));
        JLabel lblHeader = new JLabel("An unexpected exception occurred.");
        lblHeader.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        lblHeader.setVerticalAlignment(SwingConstants.TOP);

        JScrollPane scrollPane = new JScrollPane();

        JLabel lblMessage = new JLabel("<html>" + message + "</html>");
        lblMessage.setFont(UIManager.getFont("Label.font"));
        lblMessage.setVerticalTextPosition(SwingConstants.TOP);
        lblMessage.setVerticalAlignment(SwingConstants.TOP);

        GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
        gl_contentPanel
                .setHorizontalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_contentPanel.createSequentialGroup().addContainerGap().addComponent(lblIcon)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
                                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
                                        .addGroup(gl_contentPanel.createSequentialGroup()
                                                .addComponent(lblHeader, GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                                                .addGap(52))
                                        .addGroup(
                                                gl_contentPanel
                                                        .createSequentialGroup().addComponent(lblMessage,
                                                                GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
                                                        .addGap(10)))
                                .addContainerGap()));
        gl_contentPanel.setVerticalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_contentPanel.createSequentialGroup().addContainerGap()
                        .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING, false)
                                .addGroup(gl_contentPanel.createSequentialGroup().addComponent(lblHeader)
                                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(lblMessage))
                                .addComponent(lblIcon))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE).addContainerGap()));

        JTextArea txtException = new JTextArea(ExceptionUtils.getStackTrace(exception));
        txtException.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtException.setEditable(false);
        scrollPane.setViewportView(txtException);
        contentPanel.setLayout(gl_contentPanel);
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton cancelButton = new JButton(disposeAction);
                cancelButton.setPreferredSize(new Dimension(85, 23));
                cancelButton.addActionListener(e -> {
                    this.dispose();
                });
                getRootPane().setDefaultButton(cancelButton);
                buttonPane.add(cancelButton);

            }
        }
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                "Close");
        getRootPane().getActionMap().put("Close", disposeAction);
        setVisible(true);
    }

}
