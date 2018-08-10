package com.github.schuettec.multiprocman.manager;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.github.schuettec.multiprocman.ExceptionDialog;
import com.github.schuettec.multiprocman.ProcessDescriptor;
import com.thoughtworks.xstream.XStream;

public class Categories extends DefaultListModel<Category> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public Categories() {
		super();
		loadFromPreferences();
	}

	public static void exportApplications(List<ProcessDescriptor> applications, File file) {
		try {
			exportApplications(applications, new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			ExceptionDialog.showException(ProcessManager.getInstance(), e,
			    "Error while exporting the selected launch configurations.");
		}
	}

	public static void exportApplications(List<ProcessDescriptor> applications, OutputStream output) {
		try (OutputStream o = output) {
			XStream xstream = new XStream();
			xstream.toXML(applications, o);
		} catch (Exception e) {
			ExceptionDialog.showException(ProcessManager.getInstance(), e,
			    "Error while exporting the selected launch configurations.");
		}
	}

	public static List<ProcessDescriptor> importApplications(File file) {
		try {
			return importApplications(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			ExceptionDialog.showException(ProcessManager.getInstance(), e,
			    "Error while importing the selected applications.");
			return Collections.emptyList();
		}
	}

	public static List<ProcessDescriptor> importApplications(InputStream input) {
		try (InputStream in = input) {
			XStream xstream = new XStream();
			Object fromXML = xstream.fromXML(input);
			if (fromXML instanceof List) {
				List list = (List) fromXML;
				if (list.isEmpty() || !(list.get(0) instanceof ProcessDescriptor)) {
					showNoApplicationsMessage();
				}
				return list;
			} else {
				showNoApplicationsMessage();
				return Collections.emptyList();
			}
		} catch (Exception e) {
			ExceptionDialog.showException(ProcessManager.getInstance(), e,
			    "Error while importing the selected applications.");
			return Collections.emptyList();
		}
	}

	public static void exportCategories(List<Category> categories, File file) {
		try {
			exportCategories(categories, new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			ExceptionDialog.showException(ProcessManager.getInstance(), e, "Error while exporting the selected categories.");
		}
	}

	public static void exportCategories(List<Category> categories, OutputStream output) {
		try (OutputStream o = output) {
			XStream xstream = new XStream();
			xstream.toXML(categories, o);
		} catch (Exception e) {
			ExceptionDialog.showException(ProcessManager.getInstance(), e, "Error while exporting the selected categories.");
		}
	}

	public static List<Category> importCategories(File file) {
		try {
			return importCategories(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			ExceptionDialog.showException(ProcessManager.getInstance(), e, "Error while importing the selected categories.");
			return Collections.emptyList();
		}
	}

	public static List<Category> importCategories(InputStream input) {
		try (InputStream in = input) {
			XStream xstream = new XStream();
			Object fromXML = xstream.fromXML(input);
			if (fromXML instanceof List) {
				List list = (List) fromXML;
				if (list.isEmpty() || !(list.get(0) instanceof Category)) {
					showNoCategoriesMessage();
				}
				return list;
			} else {
				showNoCategoriesMessage();
				return Collections.emptyList();
			}
		} catch (Exception e) {
			ExceptionDialog.showException(ProcessManager.getInstance(), e, "Error while importing the selected categories.");
			return Collections.emptyList();
		}
	}

	private static void showNoApplicationsMessage() {
		JOptionPane.showMessageDialog(ProcessManager.getInstance(), "The source to import does not contain applications.",
		    "Import failed", JOptionPane.ERROR_MESSAGE);
	}

	private static void showNoCategoriesMessage() {
		JOptionPane.showMessageDialog(ProcessManager.getInstance(), "The source to import does not contain categories.",
		    "Import failed", JOptionPane.ERROR_MESSAGE);
	}

	private void loadFromPreferences() {
		File userFile = getUserFile();
		if (userFile.exists()) {
			try (FileInputStream fin = new FileInputStream(userFile)) {
				XStream xstream = new XStream();
				Object readObject = xstream.fromXML(fin);
				if (readObject instanceof List) {
					List list = (List) readObject;
					for (Object o : list) {
						if (o instanceof Category) {
							Category category = (Category) o;
							super.addElement(category);
						}
					}
				}
			} catch (Exception e) {
				ExceptionDialog.showException(ProcessManager.getInstance(), e,
				    "Error while loading user settings from user home directory.");
			}
		} else {
			this.addElement(Category.defaultCategory());
			saveToPreferences();
		}
	}

	public List<ProcessDescriptor> findProcessDescriptor(String title) {
		requireNonNull(title, "Title may not be null.");
		List<ProcessDescriptor> result = new LinkedList<>();
		Enumeration<Category> enm = this.elements();
		while (enm.hasMoreElements()) {
			Category cat = enm.nextElement();
			Enumeration<ProcessDescriptor> pdEnm = cat.getProcessTemplates()
			    .elements();
			while (pdEnm.hasMoreElements()) {
				ProcessDescriptor pd = pdEnm.nextElement();
				if (pd.getTitle()
				    .equals(title)) {
					result.add(pd);
				}
			}
		}
		return result;
	}

	public void addCategory(ImageIcon icon, String name, String description) {
		this.addElement(new Category(icon, name, description, null));
		saveToPreferences();
	}

	public void saveToPreferences() {
		try (FileOutputStream fout = new FileOutputStream(getUserFile())) {
			Object[] anArray = new Object[this.size()];
			this.copyInto(anArray);
			List<Object> asList = Arrays.asList(anArray);
			XStream xstream = new XStream();
			xstream.toXML(asList, fout);
		} catch (Exception e) {
			ExceptionDialog.showException(ProcessManager.getInstance(), e,
			    "Error while persisting user settings in user home directory.");
		}
	}

	private File getUserFile() {
		return new File(System.getProperty("user.home") + File.separator + ".procman.store");
	}

	@Override
	public void setSize(int newSize) {
		super.setSize(newSize);
		saveToPreferences();
	}

	@Override
	public void setElementAt(Category element, int index) {
		super.setElementAt(element, index);
		saveToPreferences();
	}

	@Override
	public void removeElementAt(int index) {
		super.removeElementAt(index);
		saveToPreferences();
	}

	@Override
	public void insertElementAt(Category element, int index) {
		super.insertElementAt(element, index);
		saveToPreferences();
	}

	@Override
	public void addElement(Category element) {
		super.addElement(element);
		saveToPreferences();
	}

	@Override
	public boolean removeElement(Object obj) {
		boolean removeElement = super.removeElement(obj);
		saveToPreferences();
		return removeElement;
	}

	@Override
	public void removeAllElements() {
		super.removeAllElements();
		saveToPreferences();
	}

	@Override
	public Category set(int index, Category element) {
		Category set = super.set(index, element);
		saveToPreferences();
		return set;
	}

	@Override
	public void add(int index, Category element) {
		super.add(index, element);
		saveToPreferences();
	}

	@Override
	public Category remove(int index) {
		Category remove = super.remove(index);
		saveToPreferences();
		return remove;
	}

	@Override
	public void clear() {
		super.clear();
		saveToPreferences();
	}

	@Override
	public void removeRange(int fromIndex, int toIndex) {
		super.removeRange(fromIndex, toIndex);
		saveToPreferences();
	}

}
