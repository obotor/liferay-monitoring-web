package com.liferay.sales.monitoring.filter;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import javax.portlet.Portlet;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(immediate = true, service = MonitoringActivator.class)
public class MonitoringActivator {

	private PortletServiceListener portletServiceListener;
	public static final Log log = LogFactoryUtil.getLog(MonitoringActivator.class);

	@Activate
	public void start(BundleContext bundleContext) throws Exception {
		portletServiceListener = new PortletServiceListener(bundleContext);
		String filter = "(objectClass="+Portlet.class.getName()+")";
		int count = 0;
		try {
			// get notified of all new portlet services added in future, to add filter
			bundleContext.addServiceListener(portletServiceListener, filter);
			
			// initialize filters for all portlets that have already been registered
			@SuppressWarnings("rawtypes")
			ServiceReference[] srl = bundleContext.getServiceReferences(Portlet.class.getName(), filter);
			for (int i = 0; srl != null && i < srl.length; i++) {
				portletServiceListener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, srl[i]));
				count++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
		log.info("Initialized " + count + " PortletFilters");
	}

	@Deactivate
	public void stop(BundleContext bundleContext) throws Exception {
		String filter = "(objectClass="+Portlet.class.getName()+")";
		try {
			@SuppressWarnings("rawtypes")
			ServiceReference[] srl = bundleContext.getServiceReferences(Portlet.class.getName(), filter);
			for (int i = 0; srl != null && i < srl.length; i++) {
				portletServiceListener.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, srl[i]));
			}
			bundleContext.removeServiceListener(portletServiceListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
