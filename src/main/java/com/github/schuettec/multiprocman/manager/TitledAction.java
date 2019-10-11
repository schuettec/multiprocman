package com.github.schuettec.multiprocman.manager;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

public class TitledAction extends AbstractAction {

    private Action delegate;

    public TitledAction(String title, Action delegate) {
        super(title, (Icon) delegate.getValue(Action.SMALL_ICON));
        this.delegate = delegate;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        delegate.actionPerformed(e);
    }

}
