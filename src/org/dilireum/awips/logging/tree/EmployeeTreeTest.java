package org.dilireum.awips.logging.tree;

import java.util.ArrayList;
import java.util.List;

public class EmployeeTreeTest {
	
	public static List<Employee> employees = new ArrayList<Employee>() {
		private static final long serialVersionUID = 1L;
		{
			add(new Employee(1, "Zahir Hamid", 0.0, "CEO", null));
			add(new Employee(2, "Rachdi AbdelKader", 0.0, "VP", 1));
			add(new Employee(3, "Ferkous Rachid", 0.0, "VP", 1));
			add(new Employee(4, "El Jem Mohamed", 0.0, "Administrator", 2));
			add(new Employee(5, "El Idrissi Mahmoud", 0.0, "General Manager", 4));
			add(new Employee(6, "Naciri Said", 0.0, "Manager", 5));
			add(new Employee(7, "Rafiq Rachid", 0.0, "Manager", 5));
			add(new Employee(8, "El Ouali Rachid", 0.0, "Manager", 7));
			add(new Employee(9, "Khiari Mohamed", 0.0, "Manager", 5));
			add(new Employee(10, "Miftah Mohamed", 0.0, "VP", 1));
			add(new Employee(11, "El Fad Hassan", 0.0, "Technician", 8));
			add(new Employee(12, "Belkhayat Abdelhadi", 0.0, "Manager", 15));
			add(new Employee(13, "Amer Abdessalam", 0.0, "Technician", 12));
			add(new Employee(14, "Lahlou Nouaman", 0.0, "General Manager", 4));
			add(new Employee(15, "Yassar", 0.0, "Administrator", 3));
		}
	};
	
	public static void main(String[] args) {
		Node<Employee> d = new EmployeeTree(employees).loadTree(new Employee());
		Tree.traverse(d);
	}
}
