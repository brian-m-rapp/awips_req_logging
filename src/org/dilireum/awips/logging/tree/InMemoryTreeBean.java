package org.dilireum.awips.logging.tree;

import java.util.ArrayList;
import java.util.List;

public abstract class InMemoryTreeBean<T> extends TreeBean<T> {
	private List<T> originalList;

	public List<T> getOriginalList() {
		return originalList;
	}

	public void setOriginalList(List<T> originalList) {
		this.originalList = originalList;
	}

	@Override
	public List<T> getSubList() {
		List<T> list = new ArrayList<T>();
		
		for (T d : originalList) {
			if (subListComparison(d)) {
				list.add(d);
			}
		}
		
		return list;
	}
	
	public abstract boolean subListComparison(T element);
    
}
