package com.github.aro_tech.interface_it_ant;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.tools.ant.BuildException;
import org.interfaceit.ClassCodeGenerator;
import org.interfaceit.meta.arguments.ArgumentNameSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import com.github.aro_tech.interface_it_ant.wrappers.AssertJ;
import com.github.aro_tech.interface_it_ant.wrappers.Mockito;

/**
 * Unit and integration tests for InterfaceItTask
 * 
 * @author aro_tech
 *
 */
public class InterfaceItTaskTest implements AssertJ, Mockito {
	private static final String NON_EXISTENT_CLASS_WITH_NO_PACKAGE_NAME = "NonExistentClassWithNoPackageName";
	private static final String NON_EXISTENT_CLASS_NAME = "org.bogus.DoesNotExist";
	private static final String TEST_TARGET_INTERFACE_NAME = "MyMath";
	private static final String TEST_DELEGATE_CLASS_NAME = "java.lang.Math";
	private static final String TEST_OUTPUT_PACKAGE = "org.example.test";
	private static final String DUMMY_SOURCE_ROOT = "./dummySourceRoot";
	InterfaceItTask underTest;
	ClassCodeGenerator mockGenerator = mock(ClassCodeGenerator.class);
	ArgumentNameSource mockNameSource = mock(ArgumentNameSource.class);
	InterfaceItTask underTestWithMocks;

	@Before
	public void setUp() throws Exception {
		underTest = new InterfaceItTask();
		underTestWithMocks = new InterfaceItTask() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.github.aro_tech.interface_it_ant.InterfaceItTask#
			 * makeArgumentNameSource()
			 */
			@Override
			protected ArgumentNameSource makeArgumentNameSource() throws IOException, ClassNotFoundException {
				return mockNameSource;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.github.aro_tech.interface_it_ant.InterfaceItTask#
			 * makeInterfaceItGenerator()
			 */
			@Override
			protected ClassCodeGenerator makeInterfaceItGenerator() {
				return mockGenerator;
			}

		};
	}

	// Matcher to compare File instances by the absolute path
	private static class FileMatcher implements ArgumentMatcher<File> {
		private final String expectedPath;

		public FileMatcher(String expectedPath) {
			super();
			this.expectedPath = expectedPath;
		}

		@Override
		public boolean matches(Object file) {
			return expectedPath.equals(((File) file).getAbsolutePath());
		}

		@Override
		public String toString() {
			return "Expecting file path: " + expectedPath;
		}
	}

	@Test
	public void should_call_generator_with_expected_arguments() throws IOException {
		InterfaceItArguments iiArguments = setUpIIArguments();
		setArguments(iiArguments, underTestWithMocks);

		underTestWithMocks.execute();

		File saveDir = makeSaveDirectoryFile(iiArguments.getOutputRootDir().getAbsolutePath(),
				iiArguments.getDelegateClassName());
		verify(mockGenerator).generateClassToFile(argThat(new FileMatcher(saveDir.getAbsolutePath())),
				eq(iiArguments.getTargetInterfaceName()), eq(Math.class), eq(iiArguments.getOutputPackage()),
				eq(mockNameSource), eq(iiArguments.getIndentationSpaces()));
	}

	@Test
	public void should_call_generator_using_default_indentation_when_not_specified() throws IOException {
		InterfaceItArguments iiArguments = setUpArguments(null);
		setArguments(iiArguments, underTestWithMocks);

		underTestWithMocks.execute();

		File saveDir = makeSaveDirectoryFile(iiArguments.getOutputRootDir().getAbsolutePath(),
				iiArguments.getDelegateClassName());
		verify(mockGenerator).generateClassToFile(argThat(new FileMatcher(saveDir.getAbsolutePath())),
				eq(iiArguments.getTargetInterfaceName()), eq(Math.class), eq(iiArguments.getOutputPackage()),
				eq(mockNameSource), eq(InterfaceItTask.DEFAULT_INDENTATION_SPACES));
	}

	@Test
	public void should_throw_build_exception_if_delegate_class_not_found() {
		InterfaceItArguments iiArguments = groupIIArguments(new File(DUMMY_SOURCE_ROOT), TEST_OUTPUT_PACKAGE,
				NON_EXISTENT_CLASS_NAME, TEST_TARGET_INTERFACE_NAME, null);
		setArguments(iiArguments, underTestWithMocks);
		verifyBuildException(executeReturningExpectedBuildException(), "not found", NON_EXISTENT_CLASS_NAME);
	}

	@Test
	public void should_remind_about_need_for_delegate_class_package() {
		InterfaceItArguments iiArguments = groupIIArguments(new File(DUMMY_SOURCE_ROOT), TEST_OUTPUT_PACKAGE,
				NON_EXISTENT_CLASS_WITH_NO_PACKAGE_NAME, TEST_TARGET_INTERFACE_NAME, null);
		setArguments(iiArguments, underTestWithMocks);
		verifyBuildException(executeReturningExpectedBuildException(), "not found",
				NON_EXISTENT_CLASS_WITH_NO_PACKAGE_NAME, "fully qualified", "package name");
	}

	@Test
	public void should_throw_build_exception_if_delegate_class_name_not_provided() {
		InterfaceItArguments iiArguments = groupIIArguments(new File(DUMMY_SOURCE_ROOT), TEST_OUTPUT_PACKAGE,
				null /* class name not provided */, TEST_TARGET_INTERFACE_NAME, null);
		setArguments(iiArguments, underTestWithMocks);
		verifyBuildException(executeReturningExpectedBuildException(),
				"A value is required for the attribute 'delegateClass'");
	}

	@Test
	public void should_throw_build_exception_if_delegate_class_name_is_blank() {
		InterfaceItArguments iiArguments = groupIIArguments(new File(DUMMY_SOURCE_ROOT), TEST_OUTPUT_PACKAGE,
				" " /* class name is a blank space */, TEST_TARGET_INTERFACE_NAME, null);
		setArguments(iiArguments, underTestWithMocks);
		verifyBuildException(executeReturningExpectedBuildException(),
				"A value is required for the attribute 'delegateClass'");
	}

	@Test
	public void should_throw_build_exception_if_output_directory_not_provided() {
		InterfaceItArguments iiArguments = groupIIArguments(null, TEST_OUTPUT_PACKAGE, NON_EXISTENT_CLASS_NAME,
				TEST_TARGET_INTERFACE_NAME, null);
		setArguments(iiArguments, underTestWithMocks);
		verifyBuildException(executeReturningExpectedBuildException(), "'outputSourceRootDirectory'",
				"value is required");
	}

	// TODO: verify message for blank outputSourceRootDirectory
	// TODO: warning for insufficient source info
	// TODO: verify missing/blank targetInterfaceName
	// TODO: warn for missing targetPackageName 
	
	
	private void verifyBuildException(BuildException thrown, String... expectedMessages) {
		assertThat(thrown).isNotNull();
		assertThat(thrown.getMessage()).contains(expectedMessages);
	}

	private BuildException executeReturningExpectedBuildException() {
		try {
			underTestWithMocks.execute();
		} catch (BuildException e) {
			return e;
		}
		return null;
	}

	private InterfaceItArguments setUpIIArguments() {
		Integer indentationSpaces = 6;
		return setUpArguments(indentationSpaces);
	}

	/**
	 * @param indentationSpaces
	 * @return
	 */
	private InterfaceItArguments setUpArguments(Integer indentationSpaces) {
		InterfaceItArguments iiArguments = groupIIArguments(new File(DUMMY_SOURCE_ROOT), TEST_OUTPUT_PACKAGE,
				TEST_DELEGATE_CLASS_NAME, TEST_TARGET_INTERFACE_NAME, indentationSpaces);
		return iiArguments;
	}

	private InterfaceItArguments groupIIArguments(File outputRootDir, String outputPackage, String delegateClassName,
			String targetInterfaceName, Integer indentationSpaces) {
		InterfaceItArguments iiArguments = new InterfaceItArguments(outputRootDir, outputPackage, delegateClassName,
				targetInterfaceName, indentationSpaces);
		return iiArguments;
	}

	private void setArguments(InterfaceItArguments parameterObject, InterfaceItTask underTestToUse) {
		setOutputRootDir(parameterObject, underTestToUse);
		underTestToUse.setDelegateClass(parameterObject.getDelegateClassName());
		underTestToUse.setTargetInterfaceName(parameterObject.getTargetInterfaceName());
		Integer indentationSpaces = parameterObject.getIndentationSpaces();
		if (null != indentationSpaces) {
			underTestToUse.setIndentationSpaces(indentationSpaces);
		}
		underTestToUse.setTargetPackageName(parameterObject.getOutputPackage());
	}

	private void setOutputRootDir(InterfaceItArguments parameterObject, InterfaceItTask underTestToUse) {
		File outputRootDir = parameterObject.getOutputRootDir();
		if(null != outputRootDir) {
			underTestToUse.setOutputSourceRootDirectory(outputRootDir.getAbsolutePath());			
		}
	}

	private File makeSaveDirectoryFile(String rootPath, String delegateClassName) {
		File saveDir = new File(rootPath + "/" + delegateClassName.replace('.', '/') + ".java");
		return saveDir;
	}

	@Test
	public void should_construct_default_argument_name_source_if_no_source_provided()
			throws NoSuchMethodException, SecurityException, ClassNotFoundException, IOException {
		underTest.setDelegateClass(TEST_DELEGATE_CLASS_NAME);
		ArgumentNameSource result = underTest.makeArgumentNameSource();
		Method method = org.mockito.Mockito.class.getMethod("mock", Class.class);
		assertThat(result.getArgumentNameFor(method, 0)).isEqualTo("arg0");
	}

	@Test
	public void should_construct_argument_name_source_from_zip_INTEGRATION()
			throws NoSuchMethodException, SecurityException, ClassNotFoundException, IOException {
		underTest.setDelegateClass(org.mockito.Mockito.class.getName());

		String path = this.getClass().getClassLoader().getResource("examples.zip").toExternalForm().substring(6);
		underTest.setSourceArchivePath(path);

		ArgumentNameSource result = underTest.makeArgumentNameSource();
		Method method = org.mockito.Mockito.class.getMethod("mock", Class.class);
		assertThat(result.getArgumentNameFor(method, 0)).isEqualTo("classToMock");
	}

	@Test
	public void should_construct_argument_name_source_from_text_file_INTEGRATION()
			throws NoSuchMethodException, SecurityException, ClassNotFoundException, IOException {
		underTest.setDelegateClass(java.net.URLEncoder.class.getName());

		String path = this.getClass().getClassLoader().getResource("exampleSrc.txt").toExternalForm().substring(6);
		underTest.setSourceTextFilePath(path);

		ArgumentNameSource result = underTest.makeArgumentNameSource();
		Method method = java.net.URLEncoder.class.getMethod("encode", String.class);
		assertThat(result.getArgumentNameFor(method, 0)).isEqualTo("stringToEncode");
	}

	// @Test
	// public void testMakeInterfaceItGenerator() {
	// fail("Not yet implemented");
	// }

}
