package com.satyam.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.satyam.entity.MyUser;

@Repository
public interface UserRepo extends JpaRepository<MyUser, String> {

}
