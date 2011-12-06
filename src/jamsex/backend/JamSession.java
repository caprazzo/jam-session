package jamsex.backend;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Unindexed;

@Unindexed
public class JamSession {

	@Id Long id;
	private String desc;
	
	JamSession() {}
	
	public JamSession(String desc) {
		this.desc = desc;
	}
	
	public String getDesc() {
		return desc;
	}
	
}
