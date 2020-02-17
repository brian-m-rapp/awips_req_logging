package org.dilireum.awips.logging;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="request")
@XmlType(propOrder = {"className", "attributes"})
public class Request {
	@XmlElement(name="class")
	private String className;
	@XmlElementWrapper(name = "attributes")
	@XmlElement(name="attribute")
	private List<ClassAttribute> attributes = new ArrayList<>();

	private Map<String, ClassAttribute> attributeMap = new HashMap<>();

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Map<String, ClassAttribute> getAttributeMap() {
		return attributeMap;
	}

	public void setAttributeMap(Map<String, ClassAttribute> attributeMap) {
		this.attributeMap = attributeMap;
	}

	public void addAttribute(ClassAttribute attribute) {
		attributeMap.put(attribute.getName(), attribute);
	}

	public ClassAttribute getAttribute(String attrName) {
		return attributeMap.get(attrName);
	}

	public void attributesToMap() {
		attributeMap = new HashMap<String, ClassAttribute>();
		for (ClassAttribute attr : attributes) {
			attributeMap.put(attr.getName(), attr);
		}
	}

	public void attributeMapToList() {
		attributes = new ArrayList<ClassAttribute>();
		for (String name : attributeMap.keySet()) {
			attributes.add(attributeMap.get(name));
		}
	}
}
