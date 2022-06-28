package com.onlineexaminationapp.repository;



import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.onlineexaminationapp.domain.User;

@Repository
@Qualifier("userRepository")
public interface UserRepository extends CrudRepository<User,Long>{

	User findUserByUsername(String username);

	User findUserByEmail(String email);

}
