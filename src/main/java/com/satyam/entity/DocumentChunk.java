package com.satyam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class DocumentChunk {
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(length = 1000000, nullable = false )
	    private String content;
	    
	    @Column(length = 100, nullable = false )
	    private String name;
	    
	    @Column(nullable = false ,columnDefinition = "longblob")
	    private byte[] document;
	    @ManyToOne
	    private MyUser user;
}
