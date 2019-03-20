package com.github.schuettec.multiprocman.process;

public interface ViewFrameListener {
	default void viewFrameChanged(int viewFrameLines) {
	}

	default void autoScrollStateChanged(boolean autoScrollNewValue) {
	}
}
