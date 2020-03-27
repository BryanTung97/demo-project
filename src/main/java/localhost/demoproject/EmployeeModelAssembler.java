package localhost.demoproject;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class EmployeeModelAssembler extends RepresentationModelAssemblerSupport<Employee, EmployeeModel>{
	public EmployeeModelAssembler() {
		super(EmployeeController.class, EmployeeModel.class);
	}
	
	@Override
	public EmployeeModel toModel(Employee employee) {
		EmployeeModel employeeModel = instantiateModel(employee);
		
		employeeModel.add(linkTo(methodOn(EmployeeController.class).one(employee.getId())).withSelfRel(),
					linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
		
		employeeModel.setId(employee.getId());
		employeeModel.setName(employee.getName());
		employeeModel.setRole(employee.getRole());
		//employeeModel.setSalary(employee.getSalary());
		
		return employeeModel;
	}
	
	@Override
	public CollectionModel<EmployeeModel> toCollectionModel(Iterable<? extends Employee> entities){
		CollectionModel<EmployeeModel> employeeModels = super.toCollectionModel(entities);
		
		employeeModels.add(linkTo(methodOn(EmployeeController.class).all()).withSelfRel());
		
		return employeeModels;
	}
	
}
