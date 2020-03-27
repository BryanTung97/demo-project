package localhost.demoproject;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.validation.Valid;
import javax.websocket.server.PathParam;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
public class EmployeeController {
	private final EmployeeRepository repository;
	private final EmployeeModelAssembler assembler;
	
	EmployeeController(EmployeeRepository repository, EmployeeModelAssembler assembler){
		this.repository = repository;
		this.assembler = assembler;
	}
	
	// Aggregate root
	
	@GetMapping("/employees")
	/**
	 * Returns a list of all employees stored in repository.
	 * 
	 * @return a list of all employees stored in repository
	 */
	HttpEntity<CollectionModel<EmployeeModel>> all() {
		List<Employee> employees = repository.findAll();
		
		return new ResponseEntity<CollectionModel<EmployeeModel>>(assembler.toCollectionModel(employees), HttpStatus.OK);
	}
	
	/*
	@GetMapping("/employees/{salary}")
	HttpEntity<CollectionModel<EmployeeModel>> getBySalary(@PathParam(value = "salary") double salary){
		List<Employee> employees = repository.findAll();
		List<Employee> employeesAboveSalary = new LinkedList<Employee>();
		
		for (Employee e : employees) {
			if (e.getSalary() > salary) {
				employeesAboveSalary.add(e);
			}
		}
		
		return new ResponseEntity<CollectionModel<EmployeeModel>>(assembler.toCollectionModel(employeesAboveSalary), HttpStatus.OK);
		
	}
	*/
	
	@PostMapping("/employees")
	/**
	 * Given a newEmployee, insert it into repository
	 * 
	 * @param - newEmployee an instance of an Employee entity
	 * @return a employeeModel of the newEmployee inserted
	 */
	HttpEntity<?> newEmployee(@Valid @RequestBody Employee newEmployee){
		EmployeeModel employeeModel = assembler.toModel(repository.save(newEmployee));
		URI uri = MvcUriComponentsBuilder.fromController(getClass()).path("/employees/{id}").
				buildAndExpand(employeeModel.getId()).toUri();

		return ResponseEntity
				.created(uri)
				.body(employeeModel);
	}
	
	
	// Single item
	
	@GetMapping("/employees/{id}")
	/**
	 * Given id of an Employee, find an employee in the repository with that id
	 * and return it
	 * 
	 * @param id - Long value representing the id of an Employee 
	 * @return the Employee with the given id
	 */
	HttpEntity<EmployeeModel> one(@PathVariable Long id) {
		Employee employee = repository.findById(id)
		    		.orElseThrow(() -> new EmployeeNotFoundException(id));
	    
	    return new ResponseEntity<EmployeeModel>(assembler.toModel(employee), HttpStatus.OK);
	}
	
	@PutMapping("/employees/{id}")
	/**
	 * Given id of an employee, if an employee with the given id exists, replace 
	 * the employee with newEmployee, else insert newEmployee into the repository
	 * 
	 * @param newEmployee - a new Employee to add to the repository
	 * @param id - Long value representing the id of an Employee to replace
	 * @return the employeeModel of the newEmployee
	 */
	HttpEntity<?> replaceEmployee(@Valid @RequestBody Employee newEmployee, @PathVariable Long id){
		Employee updatedEmployee = repository.findById(id)
				.map(employee -> {
					employee.setName(newEmployee.getName());
					employee.setRole(newEmployee.getRole());
					return repository.save(employee);
				})
				.orElseGet(() -> {
					newEmployee.setId(id);
					return repository.save(newEmployee);
				});
		
		EmployeeModel employeeModel = assembler.toModel(updatedEmployee);
		URI uri = MvcUriComponentsBuilder.fromController(getClass()).path("/employees/{id}")
				.buildAndExpand(employeeModel.getId()).toUri();
		
		return ResponseEntity
				.created(uri)
				.body(employeeModel);
	}
	
	@DeleteMapping("/employees/{id}")
	/**
	 * Given id of an Employee, delete employee from the repository if it exists
	 * 
	 * @param id - Long value representing the id of an Employee to delete
	 * @return an empty body
	 */
	HttpEntity<?> deleteEmployee(@PathVariable Long id) { 
		if (repository.existsById(id)) {
			repository.deleteById(id);
		}
		
		return ResponseEntity.noContent().build();
	}
}
