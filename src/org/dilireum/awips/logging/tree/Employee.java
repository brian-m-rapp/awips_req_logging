package org.dilireum.awips.logging.tree;

public class Employee extends InMemoryTreeBean<Employee> {
	Integer id;
	String name;
	double salary;
	String title;
	Integer bossId;
	
	public Employee() {
	}
	
	public Employee(Integer id, String name, double salary, String title, Integer bossId) {
		this.id = id;
		this.name = name;
		this.salary = salary;
		this.title = title;
		this.bossId = bossId;
	}
	
	@Override
	public String toString() {
		return id + " " + name + " " + salary + " " + title + " " + bossId;
	}

	@Override
	public boolean subListComparison(Employee employee) {
		return this.id.equals(employee.bossId);
	}

}
