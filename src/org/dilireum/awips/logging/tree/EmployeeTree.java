package org.dilireum.awips.logging.tree;

import java.util.List;

public class EmployeeTree extends InMemoryTree<Employee> {

	public EmployeeTree(List<Employee> originalList) {
		super(originalList);
	}

	@Override
	public boolean loadChildrenComparison(Employee employee, Node<Employee> parentKey) {
		if (employee.bossId != null)
			return employee.bossId.equals(parentKey.getData().id);
		else
			return employee.bossId == parentKey.getData().id;
	}

}