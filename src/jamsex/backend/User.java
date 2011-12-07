package jamsex.backend;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Unindexed;

@Unindexed
public class User {
	@Id Long id;
	User() {}	
}
