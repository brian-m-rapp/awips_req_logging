package org.dilireum.awips.edex.requestsrv.logging;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class RequestMarshall {

	public static void main(String[] args) throws JAXBException {
		Map<String, RequestFilter> map = new HashMap<>();

		RequestFilter req1 = new RequestFilter();
		req1.setClassName("com.raytheon.uf.common.dataquery.requests.QlServerRequest");
		ClassAttribute attr = new ClassAttribute();
		attr.setName("query");
		attr.setMaxLength(80);
		attr.setEnabled(true);
		req1.addAttribute(attr);
		
		attr = new ClassAttribute();
		attr.setName("lang");
		attr.setEnabled(false);
		req1.addAttribute(attr);

		map.put(req1.getClassName(), req1);

		RawRequestFilters requestMap = new RawRequestFilters();
		requestMap.setRequestFiltersMap(map);

		JAXBContext jaxbContext = JAXBContext.newInstance(RawRequestFilters.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(requestMap, System.out);
	}

}
