package localhost.demoproject;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.gson.Gson;

import static org.mockito.Mockito.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class EmployeeControllerTest {
	// Class to be tested
	private static EmployeeController employeeController;
	
	// Dependencies
	private static EmployeeRepository repository;
	private static EmployeeModelAssembler assembler;
	
	private static MockMvc mockMvc;

	@BeforeAll
	public static void setup() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		
		repository = mock(EmployeeRepository.class);
		assembler = mock(EmployeeModelAssembler.class);
		employeeController = new EmployeeController(repository, assembler);
		mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
	}
	/**
	 * Helper function to get the id EmployeeModel from result
	 * 
	 * @param result - response entity containing a collection model of EmployeeModels 
	 * @param id - the integer value of the EmployeeModel we wish to grab from result
	 * @return the EmployeeModel in result given id
	 */
	public EmployeeModel getEmployeeModelFromResponse (ResponseEntity<CollectionModel<EmployeeModel>> result, int id) {
		return (EmployeeModel) result.getBody().getContent().toArray()[id];
	}
	
	/**
	 * Helper function that returns an EmployeeModel with the same attributes as employee and id
	 * 
	 * @param employee - Employee to create an EmployeeModel for
	 * @param id - Long value of the id to give a EmployeeModel
	 * @return an EmployeeModel with the same attributes as employee with id
	 */
	public EmployeeModel fromEmployeeToEmployeeModel(Employee employee, Long id) {
		return new EmployeeModel(id, employee.getFirstName(), employee.getLastName(), employee.getRole());
	}
	
	
	/**
	 * Helper function that asserts employee contains the same attributes as the
	 * EmployeeModel found in the body of result and with the same statusCode
	 * 
	 * @param employee - Employee to compare with result's EmployeeModel 
	 * @param result - ResponseEntity with EmployeeModel and status code to compare
	 * @param statusCode - integer representing the HTTP status code to compare with result
	 */
	void helperAssertEqualsEmployee(Employee employee, ResponseEntity<EmployeeModel> result, int statusCode) {
		assertAll("Employee Information",
				() -> assertEquals(employee.getFirstName(), result.getBody().getFirstName()),
				() -> assertEquals(employee.getLastName(), result.getBody().getLastName()),
				() -> assertEquals(employee.getName(), result.getBody().getName()),
				() -> assertEquals(employee.getRole(), result.getBody().getRole()),
				() -> assertEquals(statusCode, result.getStatusCodeValue())
			);
	}
		
	/**
	 * Helper function used to check that links in result body match expectations
	 * 
	 * @param result - ResponseEntity containing links to other parts of api
	 * @param ref - String value of reference to link
	 * @param ending - String value of what the href ends with
	 */
	void helperTestLinks(ResponseEntity<EmployeeModel> result, String ref, String ending) {
		assertTrue(result.getBody().hasLink(ref));
		assertTrue(result.getBody().getLinks(ref).get(0).getHref().endsWith(ending));
	}
	
	@Test
	/**
	 * Tests EmployeeController's all method to make sure the EmployeeModel's returned
	 * match the attributes of the original two employees in repository
	 */
	void all_ExistingEmployees_ShouldReturnList() {
		Employee employee1 = new Employee("Bilbo", "Baggins", "burglar");
		Employee employee2 = new Employee("Jaime", "Lannister", "king's guard");
		EmployeeModel employeeModel1 = new EmployeeModel(1L, "Bilbo", "Baggins", "burglar");
		EmployeeModel employeeModel2 = new EmployeeModel(2L, "Jaime", "Lannister", "king's guard");
		
		employeeModel1.add(linkTo(methodOn(EmployeeController.class).one(employeeModel1.getId())).withSelfRel(),
				linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));		
		employeeModel2.add(linkTo(methodOn(EmployeeController.class).one(employeeModel2.getId())).withSelfRel(),
				linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));		
		
		List<Employee> employees = Arrays.asList(employee1, employee2);
		List<EmployeeModel> employeeModels = Arrays.asList(employeeModel1, employeeModel2);
		
		CollectionModel<EmployeeModel> employeeCollectionModel = new CollectionModel<>(employeeModels);
		employeeCollectionModel.add(linkTo(methodOn(EmployeeController.class).all()).withSelfRel());

		when(repository.findAll()).thenReturn(employees);
		when(assembler.toCollectionModel(employees)).thenReturn(employeeCollectionModel);

		ResponseEntity<CollectionModel<EmployeeModel>> result 
			= (ResponseEntity<CollectionModel<EmployeeModel>>) employeeController.all();

		assertAll(
				() -> assertEquals(2, result.getBody().getContent().size()),
				() -> assertEquals(employeeModels.get(0), getEmployeeModelFromResponse(result, 0)),
				() -> assertEquals(employeeModels.get(1), getEmployeeModelFromResponse(result, 1)),
				() -> assertEquals(200, result.getStatusCodeValue()),
				() -> assertTrue(result.getBody().hasLink("self")),
				() -> assertTrue(result.getBody().getLinks("self").get(0).getHref().endsWith("/employees")),
				() -> assertTrue(getEmployeeModelFromResponse(result, 0).hasLink("self")),
				() -> assertTrue(getEmployeeModelFromResponse(result, 0).getLinks("self")
						.get(0).getHref().endsWith("/1")),
				() -> assertTrue(getEmployeeModelFromResponse(result, 0).hasLink("employees")),
				() -> assertTrue(getEmployeeModelFromResponse(result, 0).getLinks("employees")
						.get(0).getHref().endsWith("/employees")),
				() -> assertTrue(getEmployeeModelFromResponse(result, 1).hasLink("self")),
				() -> assertTrue(getEmployeeModelFromResponse(result, 1).getLinks("self")
						.get(0).getHref().endsWith("/2")),
				() -> assertTrue(getEmployeeModelFromResponse(result, 1).hasLink("employees")),
				() -> assertTrue(getEmployeeModelFromResponse(result, 1).getLinks("employees")
						.get(0).getHref().endsWith("/employees"))
		);
	}
	
	@Test
	/**
	 * Tests EmployeeController's all method to ensure that it still returns an empty list
	 * with a HTTP OK response when there are not Employees inside the repository
	 */
	void all_NoExistingEmployees_ShouldReturnList() {
		List<Employee> employees = Arrays.asList();
		List<EmployeeModel> employeeModels = Arrays.asList();
		
		CollectionModel<EmployeeModel> employeeCollectionModel = new CollectionModel<EmployeeModel>(employeeModels);
		employeeCollectionModel.add(linkTo(methodOn(EmployeeController.class).all()).withSelfRel());

		when(repository.findAll()).thenReturn(employees);
		when(assembler.toCollectionModel(employees)).thenReturn(employeeCollectionModel);

		ResponseEntity<CollectionModel<EmployeeModel>> result 
			= (ResponseEntity<CollectionModel<EmployeeModel>>) employeeController.all();
				
		assertAll(
				() -> assertEquals(0, result.getBody().getContent().size()),
				() -> assertEquals(200, result.getStatusCodeValue()),
				() -> assertTrue(result.getBody().hasLink("self")),
				() -> assertTrue(result.getBody().getLinks("self").get(0).getHref().endsWith("/employees"))
		);
	}
	
	@Test
	/**
	 * Tests EmployeeController's one method to make sure that it returns the correct employee
	 * from the repository given a valid id
	 */
	void one_ExistingEmployeeGiven_ShouldReturnEmployee() {
		Employee employee = new Employee("Bilbo", "Baggins", "burglar");
		EmployeeModel employeeModel = fromEmployeeToEmployeeModel(employee, 1L);
		int expectedStatusCode = 200;
		
		employeeModel.add(linkTo(methodOn(EmployeeController.class).one(employeeModel.getId())).withSelfRel(),
					linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
				
		when(repository.findById(1L)).thenReturn(Optional.of(employee));
		when(assembler.toModel(employee)).thenReturn(employeeModel);
		
		ResponseEntity<EmployeeModel> result = (ResponseEntity<EmployeeModel>) employeeController.one(1L);
		
		helperTestLinks(result, "self", "/1");
		helperTestLinks(result, "employees", "/employees");
		helperAssertEqualsEmployee(employee, result, expectedStatusCode);
	}
	
	@Test
	/**
	 * Tests EmployeeController's one method to make sure it throws an EmployeeNotFoundException
	 * when given an id that does not correspond to an Employee in repository
	 */
	void one_NonexistentExmployeeGiven_ShouldReturnError() {
		when(repository.findById(1L)).thenReturn(Optional.empty());
				
		EmployeeNotFoundException exception = assertThrows(EmployeeNotFoundException.class, 
				() -> employeeController.one(1L));
		
		assertEquals("Could not find employee 1", exception.getMessage());
	}
	
	@Test
	/**
	 * Test's EmployeeController's newEmployee method to make sure it returns a
	 * ResponseEntity with the given employee and HTTP created status
	 */
	void newEmployee_ValidEmployee_ShouldReturnEmployee(){
		Employee employee = new Employee("Bilbo", "Baggins", "burglar");
		EmployeeModel employeeModel = fromEmployeeToEmployeeModel(employee, 1L);
		int expectedStatusCode = 201;
		
		employeeModel.add(linkTo(methodOn(EmployeeController.class).one(employeeModel.getId())).withSelfRel(),
					linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
		
		when(repository.save(employee)).thenReturn(employee);
		when(assembler.toModel(employee)).thenReturn(employeeModel);	
		
		@SuppressWarnings("unchecked")
		ResponseEntity<EmployeeModel> result = (ResponseEntity<EmployeeModel>) employeeController.newEmployee(employee);
		
		helperTestLinks(result, "self", "/1");
		helperTestLinks(result, "employees", "/employees");
		helperAssertEqualsEmployee(employee, result, expectedStatusCode);
	}
	
	@Test
	/**
	 * Test's EmployeeController's replaceEmployee method to make sure that
	 * given a newEmployee, it is able to return the newEmployee with a
	 * HTTP created status
	 */
	void replaceEmployee_ExistingEmployee_ShouldReturnNewEmployee() {
		Employee ogEmployee = new Employee("Robert", "Baratheon", "king");
		Employee newEmployee = new Employee("Jon", "Snow", "night's watch");
		EmployeeModel newEmployeeModel = fromEmployeeToEmployeeModel(newEmployee, 1L);
		int expectedStatusCode = 201;
		
		newEmployeeModel.add(linkTo(methodOn(EmployeeController.class).one(newEmployeeModel.getId())).withSelfRel(),
				linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));		
		
		when(repository.findById(1L)).thenReturn(Optional.of(ogEmployee));
		when(repository.save(newEmployee)).thenReturn(newEmployee);
		when(assembler.toModel(newEmployee)).thenReturn(newEmployeeModel);
		
		@SuppressWarnings("unchecked")
		ResponseEntity<EmployeeModel> result = (ResponseEntity<EmployeeModel>) 
				employeeController.replaceEmployee(newEmployee, 1L);
		
		helperTestLinks(result, "self", "/1");
		helperTestLinks(result, "employees", "/employees");
		helperAssertEqualsEmployee(newEmployee, result, expectedStatusCode);
	}
	
	@Test
	/**
	 * Test's EmployeeController's replaceEmployee method to make sure that 
	 * when given an id that does not correspond to an existing Employee, it
	 * will still return the newEmployee with HTTP created status
	 */
	void replaceEmployee_NonExistingEmployee_ShouldReturnNewEmployee() {
		Employee newEmployee = new Employee("Jon", "Snow", "night's watch");
		EmployeeModel newEmployeeModel = fromEmployeeToEmployeeModel(newEmployee, 1L);
		int expectedStatusCode = 201;
		
		newEmployeeModel.add(linkTo(methodOn(EmployeeController.class).one(newEmployeeModel.getId())).withSelfRel(),
				linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));		
		
		when(repository.findById(1L)).thenReturn(Optional.empty());
		when(repository.save(newEmployee)).thenReturn(newEmployee);
		when(assembler.toModel(newEmployee)).thenReturn(newEmployeeModel);
		
		@SuppressWarnings("unchecked")
		ResponseEntity<EmployeeModel> result 
				= (ResponseEntity<EmployeeModel>) employeeController.replaceEmployee(newEmployee, 3L);
		
		helperTestLinks(result, "self", "/1");
		helperTestLinks(result, "employees", "/employees");
		helperAssertEqualsEmployee(newEmployee, result, expectedStatusCode);
	}
	
	@Test
	/**
	 * Test's EmployeeController's deleteEmployee method to ensure that given an id
	 * that corresponds to a valid Employee in repository, it will return a ResponseEntity
	 * with HTTP no content status
	 */
	void deleteEmployee_ExistingEmployee_ShouldReturnEmptyBody() {
		when(repository.existsById(1L)).thenReturn(true);
		
		ResponseEntity<?> result = (ResponseEntity<?>) employeeController.deleteEmployee(1L);
		
		assertEquals(204, result.getStatusCodeValue());
	}
	
	@Test
	/**
	 * Test's EmployeeController's deleteEmployee method to ensure that given even
	 * when given an id that does not correspond to a valid Employee in repository,
	 * it will still return a ResponseEntity with HTTP no content status
	 */
	void deleteEmployee_NonExistentEmployee_ShouldReturnEmptyBody() {
		when(repository.existsById(1L)).thenReturn(false);
		
		ResponseEntity<?> result = (ResponseEntity<?>) employeeController.deleteEmployee(1L);
		
		assertEquals(204, result.getStatusCodeValue());
	}
	
	// Test exceptions
	
	@Test
	/**
	 * Tests HTTP get request to invalid endpoints /employees/{id} where
	 * id is a String instead of a long value 
	 * 
	 * @throws Exception - expects a HTTP 400 bad request to be thrown
	 */
	void one_IncorrectArgumentType_ShouldThrowError() throws Exception {
		String exception = mockMvc.perform(get("/employees/second"))
				.andExpect(status().isBadRequest())
				.andReturn()
				.getResolvedException()
				.getMessage();
	
		assertTrue(StringUtils.contains(exception, "Failed to convert value of type \'java.lang.String\'"));
	}
	
	@Test
	/**
	 * Tests HTTP post request to invalid endpoints /employees/{id} when
	 * posts should be to a collection of resources 
	 * 
	 * @throws Exception - expects HTTP 405 method not allowed to be thrown
	 */
	void post_InvalidEndPoint_ShouldThrowError() throws Exception{
		Employee employee = new Employee("Jason", "Lake", "team owner");
		Gson gson = new Gson();

		String exception = mockMvc.perform(post("/employees/3")
				.content(gson.toJson(employee))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isMethodNotAllowed())
				.andReturn()
				.getResolvedException()
				.getMessage();
		
		assertEquals("Request method \'POST\' not supported", exception);
	}

	@Test
	/**
	 * Tests HTTP put request to invalid endpoint /employee when
	 * put should be to a single resource like /employee/{id}
	 * 
	 * @throws Exception - expects HTTP 405 method not allowed to be thrown
	 */
	void put_InvalidEndPoint_ShouldThrowError() throws Exception{
		Employee employee = new Employee("Jason", "Lake", "team owner");
		Gson gson = new Gson();

		String exception = mockMvc.perform(put("/employees")
				.content(gson.toJson(employee))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isMethodNotAllowed())
				.andReturn()
				.getResolvedException()
				.getMessage();
		
		assertEquals("Request method \'PUT\' not supported", exception);
	}
	
	@Test
	/**
	 * Test HTTP delete request on invalid endpoint /employee when
	 * delete should be to a single resource
	 * 
	 * @throws Exception - expects HTTP 405 method not allowed to be thrown
	 */
	void delete_InvalidEndPoint_ShouldThrowError() throws Exception{
		String exception = mockMvc.perform(delete("/employees"))
				.andExpect(status().isMethodNotAllowed())
				.andReturn()
				.getResolvedException()
				.getMessage();
		
		assertEquals("Request method \'DELETE\' not supported", exception);
	}
	
	@Test
	/** 
	 * Test HTTP post request without any content to create. 
	 * 
	 * @throws Exception - expects HTTP 400 bad request to be thrown
	 */
	void put_EmptyBody_ShouldThrowError() throws Exception {
		String exception = mockMvc.perform(post("/employees")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn()
				.getResolvedException()
				.getMessage();
		
		assertTrue(StringUtils.contains(exception, "Required request body is missing"));
	}
	
	// Test incorrect content
	
	@Test
	/**
	 * Tests HTTP post request with null value on the role field. Role is required to
	 * be not null, so an exception is expected
	 * 
	 * @throws Exception - expects HTTP 400 bad request to be thrown
	 */
	void post_InvalidBodyWithNullFields_ShouldThrowError() throws Exception {
		Employee employee = new Employee();
		employee.setFirstName("Richard");
		employee.setLastName("Dawkins");
		
		Gson gson = new Gson();
		
		String exception = mockMvc.perform(post("/employees")
				.content(gson.toJson(employee))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn()
				.getResolvedException()
				.getMessage();

		assertTrue(StringUtils.contains(exception, "Field error in object \'employee\' on field \'role\'"));
	}
	
	@Test
	/**
	 * Tests HTTP put request with null value for the lastName field. lastName is required
	 * to be not null so an exception is expected
	 * 
	 * @throws Exception - expects HTTP 400 bad request to be thrown
	 */
	void put_InvalidBodyWithNullFields_ShouldThrowError() throws Exception {
		Employee employee = new Employee();
		employee.setFirstName("Richard");
		employee.setRole("ethologist");

		Gson gson = new Gson();
		
		String exception = mockMvc.perform(put("/employees/1")
				.content(gson.toJson(employee))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn()
				.getResolvedException()
				.getMessage();
	
		assertTrue(StringUtils.contains(exception, "Field error in object \'employee\' on field \'lastName\'"));
	}
	
	// Incorrect content type
	@Test
	/**
	 * Tests HTTP post request with incorrect content type. Content type of application/json
	 * is expected, so an HTTP 415 error is expected
	 * 
	 * @throws Exception - expects HTTP 415 unsupported media type to be thrown
	 */
	void post_InvalidContentType_ShouldThrowError() throws Exception {
		Employee employee = new Employee();
		employee.setFirstName("Richard");
		employee.setLastName("Dawkins");
		
		Gson gson = new Gson();
		
		String exception = mockMvc.perform(post("/employees")
				.content(gson.toJson(employee))
				.contentType(MediaType.TEXT_HTML))
				.andExpect(status().isUnsupportedMediaType())
				.andReturn()
				.getResolvedException()
				.getMessage();

		assertEquals("Content type \'text/html\' not supported",exception);
	}
	
	//Additional fields
	
	@Test
	/**
	 * Tests HTTP post request with additional fields in body. Expect additional fields
	 * to be filtered out and should return HTTP 201 created status
	 * 
	 * @throws Exception - expects HTTP 201 create status
	 */
	void post_BodyWithAdditionalFields_ShouldReturnCreateStatus() throws Exception {
		// Json of employee
		String employeeJson = "{\"random\":\"text\",\"firstName\":\"Richard\",\"lastName\":\"Dawkins\",\"role\":\"ethologist\"}";
		
		Employee employee  = new Employee("Richard", "Dawkins", "ethologist");
		EmployeeModel employeeModel = fromEmployeeToEmployeeModel(employee, 1L);

		when(repository.save(employee)).thenReturn(employee);
		when(assembler.toModel(employee)).thenReturn(employeeModel);
		
		mockMvc.perform(post("/employees")
				.content(employeeJson)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}
	
	@Test
	/**
	 * Tests HTTP put request with additional fields in body. Expect additional fields
	 * to be filtered out and should return HTTP 201 created status
	 * 
	 * @throws Exception - expects HTTP 201 create status
	 */
	void put_BodyWithAdditionalFields_ShouldReturnCreateStatus() throws Exception {
		// Json of employee
		String employeeJson = "{\"random\":\"text\",\"firstName\":\"Richard\",\"lastName\":\"Dawkins\",\"role\":\"ethologist\"}";
		
		Employee employee  = new Employee("Richard", "Dawkins", "ethologist");
		EmployeeModel employeeModel = fromEmployeeToEmployeeModel(employee, 1L);
		
		employee.setId(1L);

		when(repository.findById(1L)).thenReturn(Optional.empty());
		when(repository.save(employee)).thenReturn(employee);
		when(assembler.toModel(employee)).thenReturn(employeeModel);
		
		mockMvc.perform(put("/employees/1")
				.content(employeeJson)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}
	
	@Test
	/**
	 * Tests HTTP get request with request body. Expect body to be
	 * filtered out and should return HTTP 200 created status
	 * 
	 * @throws Exception - expects HTTP 200 ok status
	 */
	void get_HasBody_ShouldReturnOKStatus() throws Exception {
		Employee employee  = new Employee("Richard", "Dawkins", "ethologist");
		EmployeeModel employeeModel = fromEmployeeToEmployeeModel(employee, 1L);
		
		employee.setId(1L);

		when(repository.findById(1L)).thenReturn(Optional.of(employee));
		when(assembler.toModel(employee)).thenReturn(employeeModel);
		
		mockMvc.perform(get("/employees/1")
				.content("anything")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
}
