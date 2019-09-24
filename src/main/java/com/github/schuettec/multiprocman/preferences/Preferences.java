package com.github.schuettec.multiprocman.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.Map;

import com.github.schuettec.multiprocman.ExceptionDialog;
import com.github.schuettec.multiprocman.manager.ProcessManager;
import com.thoughtworks.xstream.XStream;

/**
 * This class defines the global application preferences and provides features to load/save global preferences.
 */
public class Preferences {

	private Map<String, String> globalVariables = new Hashtable<>();

	public Map<String, String> getGlobalVariables() {
		return globalVariables;
	}

	public void setGlobalVariables(Map<String, String> globalVariables) {
		this.globalVariables = globalVariables;
	}

	public void save() {
		saveToPreferences(this);
	}

	public static void saveToPreferences(Preferences data) {
		try (FileOutputStream fout = new FileOutputStream(getUserFile())) {
			XStream xstream = new XStream();
			xstream.toXML(data, fout);
		} catch (Exception e) {
			ExceptionDialog.showException(ProcessManager.getInstance(), e,
			    "Error while persisting user settings in user home directory.");
		}
	}

	public static Preferences loadFromPreferences() {
		File userFile = getUserFile();
		if (userFile.exists()) {
			try (FileInputStream fin = new FileInputStream(userFile)) {
				XStream xstream = new XStream();
				Object readObject = xstream.fromXML(fin);
				if (readObject instanceof Preferences) {
					return (Preferences) readObject;
				}
			} catch (Exception e) {
				ExceptionDialog.showException(ProcessManager.getInstance(), e,
				    "Error while loading user settings from user home directory.");
			}
		}
		return new Preferences();
	}

	private static File getUserFile() {
		return new File(System.getProperty("user.home") + File.separator + ".procman.settings");
	}

}
