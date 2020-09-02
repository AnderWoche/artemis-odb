package com.artemis;

public interface ResizableSystem {

	/**
	 *
	 * Resize the System depending on how big the screen is.
	 *
	 * @param width the Screen width
	 * @param height the Screen height
	 */
	public void resize(int width, int height);
}
