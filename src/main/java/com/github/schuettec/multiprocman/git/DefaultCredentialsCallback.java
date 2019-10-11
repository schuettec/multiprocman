package com.github.schuettec.multiprocman.git;

import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class DefaultCredentialsCallback implements CredentialsCallback {

    private static Map<String, String> passphrases = new HashMap<>();
    private static Map<String, String> passwords = new HashMap<>();

    private String passphrase;
    private String password;

    @Override
    public String getPassphrase() {
        String p = passphrase;
        this.passphrase = null;
        return p;
    }

    @Override
    public String getPassword() {
        String p = password;
        this.password = null;
        return p;
    }

    @Override
    public void showMessage(String message, Component rootComponent) {
        JOptionPane.showMessageDialog(rootComponent, message, "GIT message", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public boolean promptPassword(String message, Component rootComponent) {
        if (passwords.containsKey(message)) {
            this.password = passwords.get(message);
            return true;
        } else {
            JPanel panel = new JPanel();
            JLabel label = new JLabel(message + ":");
            JPasswordField pass = new JPasswordField();
            pass.setPreferredSize(new Dimension(100, 26));
            pass.requestFocus();
            panel.add(label);
            panel.add(pass);
            String[] options = new String[] { "OK", "Cancel" };

            JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_OPTION, null, options,
                    options[0]) {
                @Override
                public void selectInitialValue() {
                    pass.requestFocusInWindow();
                }
            };

            JDialog createDialog = pane.createDialog(panel, "GIT credentials");
            createDialog.setLocationRelativeTo(rootComponent);
            createDialog.setVisible(true);

            if (pane.getValue() == options[0]) {
                this.password = new String(pass.getPassword());
                passwords.put(message, password);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean promptPassphrase(String message, Component rootComponent) {
        if (passphrases.containsKey(message)) {
            this.passphrase = passphrases.get(message);
            return true;
        } else {
            JPanel panel = new JPanel();
            JLabel label = new JLabel(message + ":");
            JPasswordField pass = new JPasswordField();
            pass.setPreferredSize(new Dimension(100, 26));
            pass.requestFocus();
            panel.add(label);
            panel.add(pass);
            String[] options = new String[] { "OK", "Cancel" };

            JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_OPTION, null, options,
                    options[0]) {
                @Override
                public void selectInitialValue() {
                    pass.requestFocusInWindow();
                }
            };

            JDialog dialog = pane.createDialog(panel, "GIT credentials");
            dialog.setLocationRelativeTo(rootComponent);
            dialog.setVisible(true);

            if (pane.getValue() == options[0]) {
                this.passphrase = new String(pass.getPassword());
                passphrases.put(message, passphrase);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean promptYesNo(String message, Component rootComponent) {
        int option = JOptionPane.showConfirmDialog(rootComponent, message, "GIT question", JOptionPane.YES_NO_OPTION);
        if (option == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void clearCache() {
        passphrases.clear();
        passwords.clear();
    }

}
