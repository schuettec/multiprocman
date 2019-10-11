package com.github.schuettec.multiprocman.git;

import java.awt.Component;

import org.eclipse.jgit.lib.ProgressMonitor;

public interface ProgressMonitorView extends ProgressMonitor {

    /**
     * @return Returns the root {@link Component} used to position other GUI elements relatively to
     */
    public Component getRootComponent();
}
