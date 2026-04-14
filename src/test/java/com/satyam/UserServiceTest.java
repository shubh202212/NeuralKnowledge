package com.satyam;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.satyam.service.UserService;

@SpringBootTest // This annotation run the project then test what you want to test 
// if we already run our project then no need of that annotation
public class UserServiceTest {

	@Autowired
	UserService userservice;
	@Test
	void testName() {
		
		assertNotNull(userservice.getUserByUsername("satyamsingh81140@gmail.com"));
	}
	
}
