package org.dilireum.awips.edex.requestsrv.logging;

import javax.xml.bind.annotation.*;

@XmlRootElement(name="attribute")
@XmlAccessorType(XmlAccessType.NONE)
public class ClassAttribute {
	@XmlAttribute 
	private String name;

	@XmlAttribute
	private boolean enabled = true;

	@XmlAttribute(name="maxlength")
	private int maxLength = -1;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
}
