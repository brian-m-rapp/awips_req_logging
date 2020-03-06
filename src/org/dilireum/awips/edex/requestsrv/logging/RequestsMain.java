package org.dilireum.awips.edex.requestsrv.logging;

import java.util.Map;
import javax.xml.bind.JAXBException;

import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.edex.utility.EDEXLocalizationAdapter;

public class RequestsMain {
	
	public static void main(String[] args)  throws JAXBException {
        System.setProperty("edex.home", "/awips2/edex");
        System.setProperty("aw.site.identifier", "OAX");
        PathManagerFactory.setAdapter(new EDEXLocalizationAdapter());
        RequestLogger logger = RequestLogger.getInstance();

		Map<String, RequestFilter> reqMap = logger.getFilterMap();

		for (String cls : reqMap.keySet()) {
			if (reqMap.get(cls).isEnabled()) {
				System.out.println(cls);
			} else {
				System.out.format("(%s - disabled)\n", cls);
				continue;
			}

			Map <String, ClassAttribute> attrs = reqMap.get(cls).getAttributeMap();
			for (String attr : attrs.keySet()) {
				System.out.format("\tAttribute name: %s\n", attrs.get(attr).getName());
				if (attrs.get(attr).getMaxLength() > 0)
					System.out.format("\t\tMaximum length: %s\n", attrs.get(attr).getMaxLength());
				System.out.format("\t\tEnabled: %s\n", attrs.get(attr).isEnabled());
			}
		}
	}
}
