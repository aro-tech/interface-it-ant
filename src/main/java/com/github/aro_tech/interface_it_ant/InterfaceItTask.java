/**
 * 
 */
package com.github.aro_tech.interface_it_ant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.interfaceit.ClassCodeGenerator;
import org.interfaceit.meta.arguments.ArgumentNameSource;
import org.interfaceit.meta.arguments.LookupArgumentNameSource;
import org.interfaceit.meta.arguments.SourceLineReadingArgumentNameLoader;
import org.interfaceit.util.FileUtils;

/**
 * Custom Ant task to generate a wrapper interface delegating to static methods
 * 
 * @author aro_tech
 *
 */
public class InterfaceItTask extends Task {
	public static final int DEFAULT_INDENTATION_SPACES = 4;

	private String sourceArchivePath;
	private String delegateClass;
	private String sourceTextFilePath;
	private String outputSourceRootDirectory;
	private String targetInterfaceName;
	private String targetPackageName;
	private int indentationSpaces = DEFAULT_INDENTATION_SPACES;

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
			generator.generateClassToFile(getOutputDirectory(), getTargetInterfaceName(), getDelegateClassObject(),
					getTargetPackageName(), makeArgumentNameSource(), getIndentationSpaces());
		} catch (IOException e) {
			throw new BuildException(e);
		} catch (ClassNotFoundException e) {
			throw new BuildException(makeMessageForDelegateClasssNotFound(e.getMessage()), e);
		}
	}

	private String makeMessageForDelegateClasssNotFound(String msg) {
		return "Execution of interface-it was interrupted by a ClassNotFoundException. " + "Class not found: " + msg
				+ System.lineSeparator() + "Please verify the "
				+ "classpath provided to this ant task and the value of its 'delegateClass' attribute."
				+ makePackageReminderIfNecessary(msg);
	}

	/**
	 * @param msg
	 * @return
	 */
	private String makePackageReminderIfNecessary(String msg) {
		String packageReminder = "";
		if (msg.indexOf('.') < 0) {
			packageReminder = "\nReminder, the 'delegateClass' requires a fully qualified class name, including the package name (for example 'com.example.ExampleDelegateClass').";
		}
		return packageReminder;
	}

	/**
	 * @return the indentationSpaces
	 */
	public int getIndentationSpaces() {
		return indentationSpaces;
	}

	/**
	 * @param indentationSpaces
	 *            the indentationSpaces to set
	 */
	public void setIndentationSpaces(int indentationSpaces) {
		this.indentationSpaces = indentationSpaces;
	}

	protected ArgumentNameSource makeArgumentNameSource() throws IOException, ClassNotFoundException {
		LookupArgumentNameSource nameSource = new LookupArgumentNameSource();
		File archiveFile = getArchiveFile();
		List<String> sourceLines = new ArrayList<>();
		if (null != archiveFile && archiveFile.exists()) {
			sourceLines = new FileUtils().readFilesInZipArchive(archiveFile,
					this.getDelegateClassObject().getName().replace('.', '/') + ".java");

		} else {
			File textFile = getTextFile();
			if (null != textFile && textFile.exists()) {
				sourceLines = new FileUtils().readTrimmedLinesFromFilePath(textFile.toPath());
				new SourceLineReadingArgumentNameLoader().parseAndLoad(sourceLines, nameSource);
			}
		}
		if (!sourceLines.isEmpty()) {
			new SourceLineReadingArgumentNameLoader().parseAndLoad(sourceLines, nameSource);
		}
		return nameSource;
	}

	private File getTextFile() {
		return getFile(this.getSourceTextFilePath());
	}

	private File getArchiveFile() {
		return getFile(this.getSourceArchivePath());
	}

	private File getFile(String pathStr) {
		if (null == pathStr) {
			return null;
		}
		return new File(pathStr);
	}

	private Class<?> getDelegateClassObject() throws ClassNotFoundException {
		return Class.forName(this.getDelegateClass());
	}

	/**
	 * @return the targetInterfaceName
	 */
	public String getTargetInterfaceName() {
		return targetInterfaceName;
	}

	/**
	 * @param targetInterfaceName
	 *            the targetInterfaceName to set
	 */
	public void setTargetInterfaceName(String targetInterfaceName) {
		this.targetInterfaceName = targetInterfaceName;
	}

	private File getOutputDirectory() {
		return new File(
				this.getOutputSourceRootDirectory() + "/" + this.getDelegateClass().replace('.', '/') + ".java");
	}

	private void validateAttributes() throws BuildException {
		if (null == this.delegateClass || delegateClass.trim().length() < 1) {
			throw new BuildException("A value is required for the attribute 'delegateClass'");
		}
		
		if(null == this.outputSourceRootDirectory) {
			throw new BuildException("A value is required for the attribute 'outputSourceRootDirectory'");
		}
	}

	protected ClassCodeGenerator makeInterfaceItGenerator() {
		return null;
	}

	/**
	 * @return the sourceArchivePath
	 */
	public String getSourceArchivePath() {
		return sourceArchivePath;
	}

	/**
	 * @param sourceArchivePath
	 *            the sourceArchivePath to set
	 */
	public void setSourceArchivePath(String sourceArchivePath) {
		this.sourceArchivePath = sourceArchivePath;
	}

	/**
	 * @param delegateClass
	 *            the delegateClass to set
	 */
	public void setDelegateClass(String delegateClass) {
		this.delegateClass = delegateClass;
	}

	/**
	 * @return the delegateClass
	 */
	public String getDelegateClass() {
		return delegateClass;
	}

	/**
	 * @return the sourceTextFilePath
	 */
	public String getSourceTextFilePath() {
		return sourceTextFilePath;
	}

	/**
	 * @param sourceTextFilePath
	 *            the sourceTextFilePath to set
	 */
	public void setSourceTextFilePath(String sourceTextFilePath) {
		this.sourceTextFilePath = sourceTextFilePath;
	}

	/**
	 * @return the outputSourceRootDirectory
	 */
	public String getOutputSourceRootDirectory() {
		return outputSourceRootDirectory;
	}

	/**
	 * @param outputSourceRootDirectory
	 *            the outputSourceRootDirectory to set
	 */
	public void setOutputSourceRootDirectory(String outputSourceRootDirectory) {
		this.outputSourceRootDirectory = outputSourceRootDirectory;
	}

	/**
	 * @return the targetPackageName
	 */
	public String getTargetPackageName() {
		return targetPackageName;
	}

	/**
	 * @param targetPackageName
	 *            the targetPackageName to set
	 */
	public void setTargetPackageName(String targetPackageName) {
		this.targetPackageName = targetPackageName;
	}

}
