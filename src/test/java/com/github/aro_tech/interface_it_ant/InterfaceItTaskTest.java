package com.github.aro_tech.interface_it_ant;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

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

	private InterfaceItArguments setUpIIArguments() {
		String rootPath = "./dummySourceRoot";
		File outputRootDir = new File(rootPath);
		String outputPackage = "org.example.test";
		String delegateClassName = "java.lang.Math";
		String targetInterfaceName = "MyMath";
		int indentationSpaces = 6;
		InterfaceItArguments iiArguments = groupIIArguments(outputRootDir, outputPackage, delegateClassName,
				targetInterfaceName, indentationSpaces);
		return iiArguments;
	}

	private InterfaceItArguments groupIIArguments(File outputRootDir, String outputPackage, String delegateClassName,
			String targetInterfaceName, int indentationSpaces) {
		InterfaceItArguments iiArguments = new InterfaceItArguments(outputRootDir, outputPackage, delegateClassName,
				targetInterfaceName, indentationSpaces);
		return iiArguments;
	}

	private void setArguments(InterfaceItArguments parameterObject, InterfaceItTask underTestToUse) {
		underTestToUse.setOutputSourceRootDirectory(parameterObject.getOutputRootDir().getAbsolutePath());
		underTestToUse.setDelegateClass(parameterObject.getDelegateClassName());
		underTestToUse.setTargetInterfaceName(parameterObject.getTargetInterfaceName());
		underTestToUse.setIndentationSpaces(parameterObject.getIndentationSpaces());
		underTestToUse.setTargetPackageName(parameterObject.getOutputPackage());
	}

	private File makeSaveDirectoryFile(String rootPath, String delegateClassName) {
		File saveDir = new File(rootPath + "/" + delegateClassName.replace('.', '/') + ".java");
		return saveDir;
	}

	@Test
	public void should_construct_default_argument_name_source_if_no_source_provided()
			throws NoSuchMethodException, SecurityException, ClassNotFoundException, IOException {
		underTest.setDelegateClass("java.lang.Math");
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
