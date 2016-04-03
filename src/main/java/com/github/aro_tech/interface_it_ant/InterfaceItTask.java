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

import com.github.aro_tech.interface_it.api.StatisticProvidingMixinGenerator;
import com.github.aro_tech.interface_it.format.CodeFormatter;
import com.github.aro_tech.interface_it.meta.arguments.ArgumentNameSource;
import com.github.aro_tech.interface_it.meta.arguments.LookupArgumentNameSource;
import com.github.aro_tech.interface_it.meta.arguments.SourceLineReadingArgumentNameLoader;
import com.github.aro_tech.interface_it.policy.DeprecationPolicy;
import com.github.aro_tech.interface_it.util.FileUtils;
import com.github.aro_tech.interface_it.util.SourceFileReader;
import com.github.aro_tech.interface_it_ant.io.Writer;

/**
 * Custom Ant task to generate a wrapper interface delegating to static methods
 * 
 * @author aro_tech
 *
 */
public class InterfaceItTask extends Task {
	public static final int DEFAULT_INDENTATION_SPACES = 4;

	public String echo;
	private boolean debug;

	private String sourceArchivePath;
	private String delegateClass;
	private String sourceTextFilePath;
	private String outputSourceRootDirectory;
	private String targetInterfaceName;
	private String targetPackageName;
	private int indentationSpaces = DEFAULT_INDENTATION_SPACES;
	private Writer out = new Writer() {

		@Override
		public void emitThrowable(Throwable t) {
			out.emitThrowable(t);
		}

		@Override
		public void emitText(String text) {
			System.out.println(text);
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */
	@Override
	public void execute() throws BuildException {
		if (null != this.echo) {
			out.emitText(echo);
		}
		validateAttributes();

		StatisticProvidingMixinGenerator generator = makeInterfaceItGenerator();

		try {
			if (debug) {
				out.emitText(this.toString());
			}
			File wroteFile = generator.generateMixinJavaFile(getOutputDirectory(), getTargetInterfaceName(),
					getDelegateClassObject(), getTargetPackageName(), makeArgumentNameSource());

			out.emitText("Wrote file: " + wroteFile.getAbsolutePath());
		} catch (IOException e) {
			handleIOException(e);
		} catch (ClassNotFoundException e) {
			handleClassNotFound(e);
		} catch (Throwable t) {
			handleUnexpectedError(t);
		}
	}

	private void handleUnexpectedError(Throwable t) {
		out.emitThrowable(t);
		throw new BuildException("Unepected error.  Please verify the attributes provided for this task.");
	}

	private void handleClassNotFound(ClassNotFoundException e) {
		emitThrowableIfDebug(e);
		throw new BuildException(makeMessageForDelegateClasssNotFound(e.getMessage()), e);
	}

	private void handleIOException(IOException e) {
		emitThrowableIfDebug(e);
		throw new BuildException(e);
	}

	private void emitThrowableIfDebug(Throwable t) {
		if (debug) {
			out.emitThrowable(t);
		}
	}

	private String makeMessageForDelegateClasssNotFound(String msg) {
		return "Execution of interface-it was interrupted by a ClassNotFoundException. " + "Class not found: " + msg
				+ System.lineSeparator() + "Please verify the "
				+ "classpath provided to this ant task and the value of its 'delegateClass' attribute."
				+ makePackageReminderIfNecessary(msg);
	}

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
		SourceFileReader reader = FileUtils.getDefaultSourceFileReader();
		if (null != archiveFile && archiveFile.exists()) {
			sourceLines = reader.readFilesInZipArchive(archiveFile, makeDelegateSourcePaths());

		} else {
			File textFile = getTextFile();
			if (null != textFile && textFile.exists()) {
				sourceLines = reader.readTrimmedLinesFromFilePath(textFile.toPath());
				new SourceLineReadingArgumentNameLoader().parseAndLoad(sourceLines, nameSource);
			}
		}
		if (!sourceLines.isEmpty()) {
			new SourceLineReadingArgumentNameLoader().parseAndLoad(sourceLines, nameSource);
		}
		return nameSource;
	}

	private String[] makeDelegateSourcePaths() throws ClassNotFoundException {
		Class<?> delegate = this.getDelegateClassObject();
		List<String> args = new ArrayList<>();
		while (null != delegate && delegate != Object.class) {
			String canonicalClassName = delegate.getName();
			args.add(canonicalClassNameToJavaFilePath(canonicalClassName));
			delegate = delegate.getSuperclass();
		}
		String[] sourcePaths = args.toArray(new String[0]);
		return sourcePaths;
	}

	private String canonicalClassNameToJavaFilePath(String canonicalClassName) {
		return canonicalClassName.replace('.', '/') + ".java";
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
		return new File(this.getOutputSourceRootDirectory() + "/" + this.getTargetPackageName().replace('.', '/'));
	}

	private void validateAttributes() throws BuildException {
		verifyDelegateClass();
		verifyOutputSourceRootDir();
		checkForAndHandleMissingTargetInterfaceName();
		warnIfMissingPackageName();
		warnIfNoSourceIsProvided();
	}

	private void warnIfNoSourceIsProvided() {
		if (noSourceFilesProvided()) {
			out.emitText("Warning: No source file or archive provided.  "
					+ "Using default argument names such as 'arg0', 'arg1', etc.");
		}
	}

	private void warnIfMissingPackageName() {
		if (this.getTargetPackageName().isEmpty()) {
			out.emitText("Warning: Using root package by default because of missing attribute targetPackageName.");
		}
	}

	private void checkForAndHandleMissingTargetInterfaceName() {
		if (isNullOrBlank(this.targetInterfaceName)) {
			try {
				this.targetInterfaceName = this.getDelegateClassObject().getSimpleName();
				out.emitText("Warning: Using '" + this.targetInterfaceName
						+ "' by default for missing attribute targetInterfaceName.");
			} catch (ClassNotFoundException | NullPointerException e) {
				throw new BuildException("Unable to load class from attribute delegateClass: " + this.delegateClass);
			}
		}
	}

	private void verifyOutputSourceRootDir() {
		if (isNullOrBlank(this.outputSourceRootDirectory)) {
			throw new BuildException("A value is required for the attribute 'outputSourceRootDirectory'");
		}
	}

	private void verifyDelegateClass() {
		if (isNullOrBlank(this.delegateClass)) {
			throw new BuildException("A value is required for the attribute 'delegateClass'");
		}
	}

	private boolean noSourceFilesProvided() {
		return isNullOrBlank(this.sourceArchivePath) && isNullOrBlank(this.sourceTextFilePath);
	}

	private boolean isNullOrBlank(String property) {
		return property == null || property.trim().length() < 1;
	}

	protected StatisticProvidingMixinGenerator makeInterfaceItGenerator() {
		return new StatisticProvidingMixinGenerator(FileUtils.getDefaultFileSystem(),
				DeprecationPolicy.PROPAGATE_DEPRECATION, new CodeFormatter(getIndentationSpaces()));
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
		return null == targetPackageName ? "" : targetPackageName;
	}

	/**
	 * @param targetPackageName
	 *            the targetPackageName to set
	 */
	public void setTargetPackageName(String targetPackageName) {
		this.targetPackageName = targetPackageName;
	}

	/**
	 * @return the echo
	 */
	public String getEcho() {
		return echo;
	}

	/**
	 * @param echo
	 *            the echo to set
	 */
	public void setEcho(final String echo) {
		this.echo = echo;
	}

	/**
	 * @return the debug
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * @param debug
	 *            the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String delegateClassObject = null;
		try {
			Class<?> delegateClazz = getDelegateClassObject();
			delegateClassObject = delegateClazz.toString();
		} catch (Throwable t) {
			delegateClassObject = t.toString();
		}
		return "InterfaceItTask [echo=" + echo + ", debug=" + debug + ", sourceArchivePath=" + sourceArchivePath
				+ ", delegateClass=" + delegateClass + ", sourceTextFilePath=" + sourceTextFilePath
				+ ", outputSourceRootDirectory=" + outputSourceRootDirectory + ", targetInterfaceName="
				+ targetInterfaceName + ", targetPackageName=" + targetPackageName + ", indentationSpaces="
				+ indentationSpaces + ", getTextFile()=" + toAbsolutePathString(getTextFile()) + ", getArchiveFile()="
				+ toAbsolutePathString(getArchiveFile()) + ", getDelegateClassObject()=" + delegateClassObject
				+ ", getOutputDirectory()=" + toAbsolutePathString(getOutputDirectory()) + "]";
	}

	private String toAbsolutePathString(File f) {
		if (null == f) {
			return "<null>";
		}
		return f.getAbsolutePath();
	}

	/**
	 * Direct text output
	 * 
	 * @param out
	 *            Writer to use for output
	 */
	void setWriter(Writer out) {
		this.out = out;
	}
}
