package com.raytheon.uf.edex.requestsrv.logging;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="request")
public class RequestFilter {
	@XmlAttribute(name="class")
	private String className;

	@XmlAttribute
	private boolean enabled = true;	// Logging for each request type defaults to true

	/* 
	 * Attributes are loaded from the XML file into this ArrayList.  
	 * Call attributesToMap() to copy the attributes to attributeMap HashMap.
	 * The HashMap provides efficient access by attribute name.
	 */
	@XmlElementWrapper(name="attributes", required=false)
	@XmlElement(name="attribute", required=false)
	private List<ClassAttribute> attributes = new ArrayList<>();

	private Map<String, ClassAttribute> attributeMap = new HashMap<>();

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
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
			if (!attributeMap.containsKey(attr.getName())) {
				attributeMap.put(attr.getName(), attr);
			}
		}
	}

	public void attributeMapToList() {
		attributes = new ArrayList<ClassAttribute>();
		for (String name : attributeMap.keySet()) {
			attributes.add(attributeMap.get(name));
		}
	}
}
