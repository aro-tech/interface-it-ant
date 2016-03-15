package com.github.aro_tech.interface_it_ant;

import java.io.File;

/**
 * Parameter object for setting up InterfaceIt to generate a wrapper interface
 * 
 * @author aro_tech
 *
 */
public class InterfaceItArguments {
	private final File outputRootDir;
	private final String outputPackage;
	private final String delegateClassName;
	private final String targetInterfaceName;
	private final int indentationSpaces;

	public InterfaceItArguments(File outputRootDir, String outputPackage, String delegateClassName,
			String targetInterfaceName, int indentationSpaces) {
		this.outputRootDir = outputRootDir;
		this.outputPackage = outputPackage;
		this.delegateClassName = delegateClassName;
		this.targetInterfaceName = targetInterfaceName;
		this.indentationSpaces = indentationSpaces;
	}

	/**
	 * @return the outputRootDir
	 */
	public File getOutputRootDir() {
		return outputRootDir;
	}

	/**
	 * @return the outputPackage
	 */
	public String getOutputPackage() {
		return outputPackage;
	}

	/**
	 * @return the delegateClassName
	 */
	public String getDelegateClassName() {
		return delegateClassName;
	}

	/**
	 * @return the targetInterfaceName
	 */
	public String getTargetInterfaceName() {
		return targetInterfaceName;
	}

	/**
	 * @return the indentationSpaces
	 */
	public int getIndentationSpaces() {
		return indentationSpaces;
	}

}