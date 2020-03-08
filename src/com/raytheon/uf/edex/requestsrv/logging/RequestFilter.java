package com.raytheon.uf.edex.requestsrv.logging;

import java.util.Map;
import java.util.HashMap;

/**
 * 
 * @author Brian Rapp
 * @version 1.0
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#    Engineer    Description
 * ------------- ---------- ----------- --------------------------------------------
 * Mar 8, 2020   DCS 21885  brapp       Initial creation
 * </pre>
 */
public class RequestFilter {
	private String className;

	private boolean enabled = true;	// Logging for each request type defaults to true

	private Map<String, ClassAttribute> attributeMap = new HashMap<>();

	public RequestFilter(RawRequestFilter req) {
		className = req.getClassName();
		enabled = req.isEnabled();
		for (ClassAttribute attr : req.getAttributes()) {
			addAttribute(attr);
		}
	}

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
}
