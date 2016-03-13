/**
 * 
 */
package com.github.aro_tech.interface_it_ant;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.interfaceit.ClassCodeGenerator;
import org.interfaceit.meta.arguments.ArgumentNameSource;

/**
 * Custom Ant task to generate a wrapper interface delegating to static methods
 * 
 * @author aro_tech
 *
 */
public class InterfaceItTask extends Task {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */
	@Override
	public void execute() throws BuildException {
		validateAttributes();
		ClassCodeGenerator generator = makeInterfaceItGenerator();
		try {
			generator.generateClassToFile(getOutputDirectory(), getTargetInterfaceName(), getDelegateClass(),
					getTargetPackageName(), makeArgumentNameSource(), getIndentationSpaces());
		} catch (IOException e) {
			throw new BuildException(e);
		}
	}

	private int getIndentationSpaces() {
		return 0;
	}

	protected ArgumentNameSource makeArgumentNameSource() {
		return null;
	}

	private String getTargetPackageName() {
		return null;
	}

	private Class<?> getDelegateClass() {
		return null;
	}

	private String getTargetInterfaceName() {
		return null;
	}

	private File getOutputDirectory() {
		return null;
	}

	private void validateAttributes() throws BuildException {
	}

	protected ClassCodeGenerator makeInterfaceItGenerator() {
		return null;
	}

}
