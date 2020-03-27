package localhost.demoproject;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Entity
public class Employee{
	
	@Id @GeneratedValue
	private Long id;
	@NotNull @Size(min=1, message="firstName should have atleast 2 characters") 
	private String firstName;
	@NotNull @Size(min=1, message="lastName should have atleast 2 characters") 
	private String lastName;
	@NotNull @Size(min=1, message="role should have atleast 2 characters")
	private  String role;
	//private double salary;
	
	Employee(){}
	
	/*
	Employee(String firstName, String lastName, String role,  double salary){
		this.firstName = firstName;
		this.lastName = lastName;
		this.role = role;
		this.salary = salary;
	}
	*/
	
	Employee(String firstName, String lastName, String role){
		this.firstName = firstName;
		this.lastName = lastName;
		this.role = role;
	}
	
	public String getName() {
		return this.firstName + " " + this.lastName;
	}
	
	public void setName(String name) {
		String[] parts = name.split(" ");
		this.firstName = parts[0];
		this.lastName = parts[1];
	}
}
