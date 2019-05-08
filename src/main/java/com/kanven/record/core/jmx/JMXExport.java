package com.kanven.record.core.jmx;

import java.lang.management.ManagementFactory;
import java.text.MessageFormat;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author kanven
 *
 */
public class JMXExport {

	private static final Logger log = LoggerFactory.getLogger(JMXExport.class);
	
	private static final String DEAFULT_FORMAT = "com.fcbox.record:type={0},name={1}";

	private MBeanServer server;

	private static class JMXExportHolder {

		private final static JMXExport instance = new JMXExport();

	}

	private JMXExport() {
		try {
			server = ManagementFactory.getPlatformMBeanServer();
		} catch (Error e) {
			server = MBeanServerFactory.createMBeanServer();
		}
	}

	public static JMXExport getInstance() {
		return JMXExportHolder.instance;
	}

	public void export(String name, Object bean) {
		try {
			ObjectName on = new ObjectName(MessageFormat.format(DEAFULT_FORMAT, bean.getClass(),name));
			server.registerMBean(bean, on);
		} catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException
				| NotCompliantMBeanException e) {
			log.error(name + " mxbean export failure", e);
		}
	}

}
