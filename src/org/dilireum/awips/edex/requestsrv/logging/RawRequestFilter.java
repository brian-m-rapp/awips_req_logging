package org.dilireum.awips.edex.requestsrv.logging;

import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="request")
public class RawRequestFilter {
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

	public List<ClassAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<ClassAttribute> attributes) {
		this.attributes = attributes;
	}
}
