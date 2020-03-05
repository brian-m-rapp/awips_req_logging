package org.dilireum.awips.edex.requestsrv.logging;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class RequestsMain {
	
	public static void main(String[] args)  throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(RawRequestFilters.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		RawRequestFilters requestFilters = (RawRequestFilters) jaxbUnmarshaller.unmarshal(new File("requests.xml"));
		Map<String, RequestFilter> reqMap = new HashMap<>();
		requestFilters.requestFiltersToMap(reqMap);

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
