package org.dilireum.awips.logging;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class RequestMarshall {

	public static void main(String[] args) throws JAXBException {
		Map<String, Request> map = new HashMap<>();

		Request req1 = new Request();
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

		Requests requestMap = new Requests();
		requestMap.setRequestMap(map);

		JAXBContext jaxbContext = JAXBContext.newInstance(Requests.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(requestMap, System.out);
	}

}
