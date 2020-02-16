package org.dilireum.awips.logging.tree;

import java.util.List;

public abstract class TreeBean<T> {
	public TreeBean() {
	}
	
	public abstract List<T> getSubList();
}
