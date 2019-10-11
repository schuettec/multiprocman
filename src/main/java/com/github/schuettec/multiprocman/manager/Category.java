package com.github.schuettec.multiprocman.manager;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;

import com.github.schuettec.multiprocman.ProcessDescriptor;
import com.github.schuettec.multiprocman.Resources;

public class Category implements Serializable {

    private static final long serialVersionUID = 1L;
    private ImageIcon icon;
    private String name;
    private String description;
    private DefaultListModel<ProcessDescriptor> processTemplates;

    private String url;
    private Date lastModified;

    public Category() {
        super();
        this.icon = defaultIcon();
        this.name = "unnamed";
        this.processTemplates = new DefaultListModel<>();
    }

    public Category(ImageIcon icon, String name, String description) {
        this(icon, name, description, null);
    }

    public Category(ImageIcon icon, String name, String description, Set<ProcessDescriptor> processTemplates) {
        super();
        this.icon = icon;
        this.name = name;
        this.description = description;
        this.processTemplates = new DefaultListModel<>();
        if (nonNull(processTemplates)) {
            processTemplates.addAll(processTemplates);
        }
    }

    public Date getLastModified() {
        if (isNull(lastModified)) {
            this.lastModified = new Date();
        }
        return lastModified;
    }

    /**
     * Sets the last modified field to now.
     */
    public void setLastModified() {
        this.lastModified = new Date();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public DefaultListModel<ProcessDescriptor> getProcessTemplates() {
        return processTemplates;
    }

    private static ImageIcon defaultIcon() {
        return new ImageIcon(Resources.getFolder());
    }

    public static Category defaultCategory() {
        return new Category(defaultIcon(), "Default", "The default category.", null);
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Category [icon=" + icon + ", name=" + name + ", description=" + description + ", processTemplates="
                + processTemplates + "]";
    }

}
