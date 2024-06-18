package com.chargev.emsp.repository.authentication;

import java.util.Date;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.chargev.emsp.entity.authenticationentity.AuthSubject;

public interface AuthSubjectRepository extends CrudRepository<AuthSubject, String>, PagingAndSortingRepository<AuthSubject, String> {

}
