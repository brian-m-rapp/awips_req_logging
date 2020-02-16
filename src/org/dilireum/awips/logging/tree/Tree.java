package org.dilireum.awips.logging.tree;

import org.dilireum.awips.logging.tree.Node;
import java.util.List;

public abstract class Tree<T> {

	public Tree() {
	}

	public abstract Node<T> loadTree(T element);
	protected abstract List<T> loadChildren(Node<T> parent, List<T> list);
	
    public static void traverse(Node<?> obj) {
    	traverse(obj, 0);
    }

    private static void traverse(Node<?> obj, int indentLevel) {
		if (obj != null) {
			for (int i = 0; i < obj.getChildren().size(); i++) {
				Tree.toIndent(indentLevel);
				System.out.println((obj.getChildren().get(i).getData()));
				traverse(obj.getChildren().get(i), indentLevel + 1);
			}
		}
	}

    public static void toIndent(int indentLevel) {
    	for (int j = 0; j < indentLevel; j++) {
			System.out.print("\t");
		}
    }
    
}