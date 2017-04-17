package org.vaadin.teemu.clara;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;

import static org.jsoup.helper.Validate.fail;

/**
 * @author davidbenninger
 */
public class MemoryUsageTest {

	private InputStream xml;
	private Controller controller;

	public static class Controller {

	}

	@Before
	public void setUp() {
		xml = getXml("demo-layout.xml");
		controller = new Controller();
	}

	@Test
	@Ignore
	public void testMemoryUsage() {

		final long freeBefore=Runtime.getRuntime().freeMemory();
		Clara.create(xml, controller);
		final long freeAfter=Runtime.getRuntime().freeMemory();

		fail("We used " + (freeBefore - freeAfter) + " bytes of memory");
	}

	private InputStream getXml(String fileName) {
		return getClass().getClassLoader().getResourceAsStream(fileName);
	}
}
