package com.github.schuettec.multiprocman.manager;

import java.util.Arrays;
import java.util.Iterator;

public class PromptVariable {

    private String name;
    private String message;
    private String defaultValue;
    private String lastValue;
    private boolean prompt;

    private boolean selection;
    private String[] selectionValues;

    public PromptVariable() {
        super();
    }

    public PromptVariable(String name, String message, String defaultValue, boolean prompt, boolean selection,
            String[] selectionValues) {
        super();
        this.name = name;
        this.message = message;
        this.defaultValue = defaultValue;
        this.lastValue = defaultValue;
        this.prompt = prompt;
        this.selection = selection;
        this.selectionValues = selectionValues;
    }

    public static String[] deniedStrings() {
        return new String[] { "!", "{", "}", " " };
    }

    public static boolean isValidVariableName(String variableName) {
        for (String denied : deniedStrings()) {
            if (variableName.contains(denied)) {
                return false;
            }
        }
        return true;
    }

    public static String escape(String text) {
        for (String denied : deniedStrings()) {
            text = text.replace(denied, "");
        }
        return text;
    }

    public static String[] parseSelectionValues(String commaSeparated) {
        String[] split = commaSeparated.split(",");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
        }
        return split;
    }

    public String getLastValue() {
        return lastValue;
    }

    public void setLastValue(String lastValue) {
        this.lastValue = lastValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isPrompt() {
        return prompt;
    }

    public void setPrompt(boolean prompt) {
        this.prompt = prompt;
    }

    public boolean isSelection() {
        return selection;
    }

    public void setSelection(boolean selection) {
        this.selection = selection;
    }

    public String[] getSelectionValues() {
        return selectionValues;
    }

    public void setSelectionValues(String[] selectionValues) {
        this.selectionValues = selectionValues;
    }

    public String getSelectionValuesAsCSV() {
        StringBuilder b = new StringBuilder();
        Iterator<String> it = Arrays.asList(selectionValues).iterator();
        while (it.hasNext()) {
            b.append(it.next());
            if (it.hasNext()) {
                b.append(",");
            }
        }
        return b.toString();
    }

}
