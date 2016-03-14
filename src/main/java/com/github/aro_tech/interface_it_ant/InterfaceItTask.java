/**
 * 
 */
package com.github.aro_tech.interface_it_ant;

import java.io.File;
import java.io.IOException;
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
	private String sourceArchivePath;
	private String delegateClass;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */
	@Override
	public void execute() throws BuildException {
		validateAttributes();

		// System.out.println("Hello, Ant");
		ClassCodeGenerator generator = makeInterfaceItGenerator();

		try {
			generator.generateClassToFile(getOutputDirectory(), getTargetInterfaceName(), getDelegateClassObject(),
					getTargetPackageName(), makeArgumentNameSource(), getIndentationSpaces());
		} catch (IOException | ClassNotFoundException e) {
			throw new BuildException(e);
		}
	}

	private int getIndentationSpaces() {
		return 0;
	}

	protected ArgumentNameSource makeArgumentNameSource() throws IOException, ClassNotFoundException {
		LookupArgumentNameSource nameSource = new LookupArgumentNameSource();
		File archiveFile = getArchiveFile();
		if (null != archiveFile && archiveFile.exists()) {
			List<String> sourceLines = new FileUtils().readFilesInZipArchive(archiveFile,
					this.getDelegateClassObject().getName().replace('.', '/') + ".java");
			new SourceLineReadingArgumentNameLoader().parseAndLoad(sourceLines, nameSource);
			return nameSource;
		}
		return new ArgumentNameSource() {
		};
	}


	private File getArchiveFile() {
		if(null == this.getSourceArchivePath()) {
			return null;
		}
		File archiveFile = new File(this.getSourceArchivePath());
		return archiveFile;
	}

	private String getTargetPackageName() {
		return null;
	}

	private Class<?> getDelegateClassObject() throws ClassNotFoundException {
		return Class.forName(this.getDelegateClass());
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

}
