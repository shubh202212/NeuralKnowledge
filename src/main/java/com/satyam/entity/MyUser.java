package com.satyam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

//Equivalent to @Getter @Setter @RequiredArgsConstructor @ToString @EqualsAndHashCode. 
@Data 

@Entity
public class MyUser {
	
  @Id
  private String username;
  private String name;
  private String password;
  private boolean enabled=true;
  private String role="ROLE_USER";
  private String phone;
  @Column(nullable = true ,columnDefinition = "longblob")
  private byte[] photo;
  
}
