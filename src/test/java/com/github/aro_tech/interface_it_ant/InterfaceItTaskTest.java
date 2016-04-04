package com.github.aro_tech.interface_it_ant;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.tools.ant.BuildException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import com.github.aro_tech.interface_it.api.StatisticProvidingMixinGenerator;
import com.github.aro_tech.interface_it.meta.arguments.ArgumentNameSource;
import com.github.aro_tech.interface_it.policy.DeprecationPolicy;
import com.github.aro_tech.interface_it.statistics.GenerationStatistics;
import com.github.aro_tech.interface_it_ant.io.Writer;
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
	StatisticProvidingMixinGenerator mockGenerator = mock(StatisticProvidingMixinGenerator.class);
	ArgumentNameSource mockNameSource = mock(ArgumentNameSource.class);
	InterfaceItTask underTestWithMocks;
	Writer mockWriter = mock(Writer.class);
	private static final File WROTE_FILE = new File("./Whatever.java");

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
			protected StatisticProvidingMixinGenerator makeInterfaceItGenerator() {
				return mockGenerator;
			}

		};
		underTestWithMocks.setWriter(mockWriter);
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
		setGeneratorMockToReturnFile();

		underTestWithMocks.execute();

		verifyNormalMockExecution(iiArguments);
	}

	@Test
	public void should_write_file_path_on_success() throws IOException {
		InterfaceItArguments iiArguments = setUpIIArguments();
		setArguments(iiArguments, underTestWithMocks);
		setGeneratorMockToReturnFile();
		underTestWithMocks.execute();
		verify(mockWriter).emitText("Wrote file: " + WROTE_FILE.getAbsolutePath());
		verifyNormalMockExecution(iiArguments);
	}

	@Test
	public void should_write_statistics_on_success() throws IOException {
		InterfaceItArguments iiArguments = setUpIIArguments();
		setArguments(iiArguments, underTestWithMocks);
		setGeneratorMockToReturnFile();
		GenerationStatistics stats = new GenerationStatistics();
		stats.incrementMethodCount();
		stats.incrementMethodCount();
		stats.incrementConstantCount();
		when(mockGenerator.getStatistics()).thenReturn(stats);
		underTestWithMocks.execute();
		verify(mockWriter, atLeastOnce()).emitText(contains("1", "2", "method", "constant"));
		verifyNormalMockExecution(iiArguments);
	}

	private String contains(final String...expectedParts) {
		return argThat(
				new ArgumentMatcher<String>() {

					@Override
					public boolean matches(Object argument) {

						return contains(argument.toString().toLowerCase(), expectedParts);
					}

					private boolean contains(String toCheck, String... args) {
						for (String arg : args) {
							if (!toCheck.contains(arg)) {
								return false;
							}
						}
						return true;
					}
				});
	}

	/**
	 * @throws IOException
	 */
	private void setGeneratorMockToReturnFile() throws IOException {
		when(mockGenerator.generateMixinJavaFile(any(), anyString(), any(), anyString(), any())).thenReturn(WROTE_FILE);
	}

	@Test
	public void should_call_generator_using_default_indentation_when_not_specified() throws IOException {
		InterfaceItArguments iiArguments = setUpArguments(null);
		setArguments(iiArguments, underTestWithMocks);
		setGeneratorMockToReturnFile();

		underTestWithMocks.execute();

		File saveDir = makeSaveDirectoryFile(iiArguments.getOutputRootDir().getAbsolutePath(),
				iiArguments.getOutputPackage());
		verify(mockGenerator).generateMixinJavaFile(argThat(new FileMatcher(saveDir.getAbsolutePath())),
				eq(iiArguments.getTargetInterfaceName()), eq(Math.class), eq(iiArguments.getOutputPackage()),
				eq(mockNameSource));

		/*
		 * eq(mockNameSource), eq(InterfaceItTask.DEFAULT_INDENTATION_SPACES));
		 */
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
	public void should_emit_debug_message() throws IOException {
		InterfaceItArguments iiArguments = setUpIIArguments();
		setArguments(iiArguments, underTestWithMocks);
		underTestWithMocks.setDebug(true);
		String path = iiArguments.getOutputRootDir().getAbsolutePath();
		setGeneratorMockToReturnFile();

		underTestWithMocks.execute();
		verify(mockWriter).emitText(
				"InterfaceItTask [echo=null, " + "debug=true, sourceArchivePath=null, delegateClass=java.lang.Math,"
						+ " sourceTextFilePath=null, outputSourceRootDirectory=" + path
						+ ", targetInterfaceName=MyMath, targetPackageName=org.example.test, indentationSpaces=6, getTextFile()=<null>, getArchiveFile()=<null>, "
						+ "getDelegateClassObject()=class java.lang.Math, " + "getOutputDirectory()=" + path
						+ "\\org\\example\\test]");
	}

	@Test
	public void should_emit_echo_message_before_validation() {
		underTestWithMocks.setEcho("echo test");
		try {
			underTestWithMocks.execute();
		} catch (Throwable t) {
			// ignore error - we just need to verify the echo
		}
		verify(mockWriter).emitText("echo test");
	}

	@Test
	public void should_throw_build_exception_if_output_directory_not_provided() {
		InterfaceItArguments iiArguments = groupIIArguments(null, TEST_OUTPUT_PACKAGE, NON_EXISTENT_CLASS_NAME,
				TEST_TARGET_INTERFACE_NAME, null);
		setArguments(iiArguments, underTestWithMocks);
		underTestWithMocks.setOutputSourceRootDirectory(" ");
		verifyBuildException(executeReturningExpectedBuildException(), "'outputSourceRootDirectory'",
				"value is required");
	}

	@Test
	public void should_throw_build_exception_if_output_directory_is_blank() {
		InterfaceItArguments iiArguments = groupIIArguments(null, TEST_OUTPUT_PACKAGE, NON_EXISTENT_CLASS_NAME,
				TEST_TARGET_INTERFACE_NAME, null);
		setArguments(iiArguments, underTestWithMocks);
		verifyBuildException(executeReturningExpectedBuildException(), "'outputSourceRootDirectory'",
				"value is required");
	}

	@Test
	public void should_warn_and_use_delegate_class_name_if_no_target_interface_name() throws IOException {
		InterfaceItArguments iiArguments = setUpIIArguments();
		setArguments(iiArguments, underTestWithMocks);
		underTestWithMocks.setTargetInterfaceName(null);
		setGeneratorMockToReturnFile();

		underTestWithMocks.execute();

		File saveDir = makeSaveDirectoryFile(iiArguments.getOutputRootDir().getAbsolutePath(),
				iiArguments.getOutputPackage());
		verify(mockWriter).emitText("Warning: Using 'Math' by default for missing attribute targetInterfaceName.");
		verify(mockGenerator).generateMixinJavaFile(argThat(new FileMatcher(saveDir.getAbsolutePath())), eq("Math"),
				eq(Math.class), eq(iiArguments.getOutputPackage()), eq(mockNameSource));
	}

	@Test
	public void should_warn_and_use_delegate_class_name_if_blank_target_interface_name() throws IOException {
		InterfaceItArguments iiArguments = setUpIIArguments();
		setArguments(iiArguments, underTestWithMocks);
		underTestWithMocks.setTargetInterfaceName(" ");
		setGeneratorMockToReturnFile();

		underTestWithMocks.execute();

		File saveDir = makeSaveDirectoryFile(iiArguments.getOutputRootDir().getAbsolutePath(),
				iiArguments.getOutputPackage());
		verify(mockWriter).emitText("Warning: Using 'Math' by default for missing attribute targetInterfaceName.");
		verify(mockGenerator).generateMixinJavaFile(argThat(new FileMatcher(saveDir.getAbsolutePath())), eq("Math"),
				eq(Math.class), eq(iiArguments.getOutputPackage()), eq(mockNameSource));
	}

	@Test
	public void should_warn_if_no_target_package_name() throws IOException {
		InterfaceItArguments iiArguments = setUpIIArguments();
		setArguments(iiArguments, underTestWithMocks);
		underTestWithMocks.setTargetPackageName(null);
		setGeneratorMockToReturnFile();

		underTestWithMocks.execute();

		verify(mockWriter)
				.emitText("Warning: Using root package by default because of missing attribute targetPackageName.");
		verify(mockGenerator).generateMixinJavaFile(
				argThat(new FileMatcher(iiArguments.getOutputRootDir().getAbsolutePath())),
				eq(iiArguments.getTargetInterfaceName()), eq(Math.class), eq(""), eq(mockNameSource));
	}

	@Test
	public void should_warn_if_source_paths_are_null() throws IOException {
		InterfaceItArguments iiArguments = setUpIIArguments();
		setArguments(iiArguments, underTestWithMocks);
		underTestWithMocks.setSourceArchivePath(null);
		underTestWithMocks.setSourceArchivePath(null);
		setGeneratorMockToReturnFile();

		underTestWithMocks.execute();

		verify(mockWriter).emitText(
				"Warning: No source file or archive provided.  Using default argument names such as 'arg0', 'arg1', etc.");
		verifyNormalMockExecution(iiArguments);
	}

	@Test
	public void should_warn_if_source_paths_are_blank() throws IOException {
		InterfaceItArguments iiArguments = setUpIIArguments();
		setArguments(iiArguments, underTestWithMocks);
		underTestWithMocks.setSourceArchivePath(" ");
		underTestWithMocks.setSourceArchivePath(" ");
		setGeneratorMockToReturnFile();

		underTestWithMocks.execute();

		verify(mockWriter).emitText(
				"Warning: No source file or archive provided.  Using default argument names such as 'arg0', 'arg1', etc.");
		verifyNormalMockExecution(iiArguments);
	}

	private void verifyNormalMockExecution(InterfaceItArguments iiArguments) throws IOException {
		File saveDir = makeSaveDirectoryFile(iiArguments.getOutputRootDir().getAbsolutePath(),
				iiArguments.getOutputPackage());
		verify(mockGenerator).generateMixinJavaFile(argThat(new FileMatcher(saveDir.getAbsolutePath())),
				eq(iiArguments.getTargetInterfaceName()), eq(Math.class), eq(iiArguments.getOutputPackage()),
				eq(mockNameSource));
	}

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
		if (null != outputRootDir) {
			underTestToUse.setOutputSourceRootDirectory(outputRootDir.getAbsolutePath());
		}
	}

	private File makeSaveDirectoryFile(String rootPath, String delegateClassPackage) {
		File saveDir = new File(rootPath + "/" + delegateClassPackage.replace('.', '/'));
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
	public void should_construct_argument_name_source_from_zip_with_superclass_bug1_INTEGRATION()
			throws NoSuchMethodException, SecurityException, ClassNotFoundException, IOException {
		underTest.setDelegateClass(org.mockito.Mockito.class.getName());

		String path = this.getClass().getClassLoader().getResource("examples.zip").toExternalForm().substring(6);
		underTest.setSourceArchivePath(path);

		ArgumentNameSource result = underTest.makeArgumentNameSource();
		Method method = org.mockito.Matchers.class.getMethod("argThat", ArgumentMatcher.class);
		assertThat(result.getArgumentNameFor(method, 0)).isEqualTo("matcher");
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
	
	@Test
	public void should_use_propagation_policy_by_default() {
		assertThat(underTest.getDeprecationPolicy()).isEqualTo(DeprecationPolicy.PROPAGATE_DEPRECATION);
	}

	@Test
	public void should_use_propagation_policy_if_ignore_set_to_false() {
		InterfaceItTask task = new InterfaceItTask();
		task.setIgnoreDeprecated(false);
		assertThat(task.getDeprecationPolicy()).isEqualTo(DeprecationPolicy.PROPAGATE_DEPRECATION);
	}
	
	@Test
	public void should_use_ignore_policy_if_ignore_set_to_true() {
		InterfaceItTask task = new InterfaceItTask();
		task.setIgnoreDeprecated(true);
		assertThat(task.getDeprecationPolicy()).isEqualTo(DeprecationPolicy.IGNORE_DEPRECATED_METHODS);
	}

}
