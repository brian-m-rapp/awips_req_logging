package org.dilireum.awips.logging.tree;

import java.util.List;

public abstract class InMemoryTree<T extends InMemoryTreeBean<T>> extends Tree<T> {

	private List<T> originalList;
	
	public InMemoryTree(List<T> originalList) {
		this.originalList = originalList;
	}

	public Node<T> loadTree(T element) {
		Node<T> root = new Node<T>(element, null);

		this.loadChildren(root, originalList);

		return root;
	}
  
	protected List<T> loadChildren(Node<T> parent, List<T> list) {
		if (list != null && !list.isEmpty()) {
			for (T element : list) {
				if (loadChildrenComparison(element, parent)) {
					Node<T> nextParent = new Node<T>(element, parent);
					if (element.getOriginalList() == null) {
						element.setOriginalList(originalList);
					}
					loadChildren(nextParent, element.getSubList());
				}
			}
		}
		return list;
	}
	
	public abstract boolean loadChildrenComparison(T element, Node<T> parentKey);
}
