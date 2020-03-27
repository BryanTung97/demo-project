package localhost.demoproject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.springframework.hateoas.RepresentationModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EmployeeModel extends RepresentationModel<EmployeeModel>{
	private Long id;
	private String firstName;
	private String lastName;
	private String role;
	//private double salary;
	
	public String getName() {
		return this.firstName + " " + this.lastName;
	}
	
	public void setName(String name) {
		String[] parts = name.split(" ");
		this.firstName = parts[0];
		this.lastName = parts[1];
	}
}
