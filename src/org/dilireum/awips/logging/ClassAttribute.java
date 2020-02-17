package org.dilireum.awips.logging;

import javax.xml.bind.annotation.*;

@XmlRootElement(name="attribute")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"name", "enabled", "maxLength"})
public class ClassAttribute {
	@XmlElement 
	private String name;

	@XmlElement(required=false)
	private boolean enabled = true;

	@XmlElement(name="maxlength", required=false)
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
