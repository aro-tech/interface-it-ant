package com.github.aro_tech.interface_it_ant;

import java.io.IOException;
import java.lang.reflect.Method;

import org.interfaceit.meta.arguments.ArgumentNameSource;
import org.junit.Before;
import org.junit.Test;

import com.github.aro_tech.interface_it_ant.wrappers.AssertJ;
import com.github.aro_tech.interface_it_ant.wrappers.Mockito;

/**
 * Unit and integration tests for InterfaceItTask
 * 
 * @author aro_tech
 *
 */
public class InterfaceItTaskTest implements AssertJ, Mockito {
	InterfaceItTask underTest = new InterfaceItTask();

	@Before
	public void setUp() throws Exception {
	}

	// @Test
	// public void testExecute() {
	// fail("Not yet implemented");
	// }

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

	// @Test
	// public void testMakeInterfaceItGenerator() {
	// fail("Not yet implemented");
	// }

}
