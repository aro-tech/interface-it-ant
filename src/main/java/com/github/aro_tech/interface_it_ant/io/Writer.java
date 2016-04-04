/**
 * 
 */
package com.github.aro_tech.interface_it_ant.io;

/**
 * Interface to handle text output
 * @author aro_tech
 *
 */
public interface Writer {
	/**
	 * Write text to output
	 * @param text Text to emit
	 */
	void emitText(String text);
	
	/**
	 * Write a stack trace to output
	 * @param t
	 */
	void emitThrowable(Throwable t);
}
