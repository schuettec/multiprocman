package de.schuette.procman.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;

import com.thoughtworks.xstream.XStream;

import de.schuette.procman.ExceptionDialog;

public class Categories extends DefaultListModel<Category> {

	private static final String CATEGORIES = "categories";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Categories() {
		super();
		loadFromPreferences();
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
				ExceptionDialog.showException(e, "Error while loading user settings from user home directory.");
			}
		} else {
			this.addElement(Category.defaultCategory());
			saveToPreferences();
		}
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
			ExceptionDialog.showException(e, "Error while persisting user settings in user home directory.");
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
