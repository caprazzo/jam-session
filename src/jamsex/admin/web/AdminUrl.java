package jamsex.admin.web;

public enum AdminUrl {
		
	index(""), 
	login("/login"), 
	access_denied("/access_denied"), 
	logout("/logout"),
	create_session("/create_session");
	
	private final String url;
	private static String base;

	private AdminUrl(String url) {
		this.url = url;
	}
	
	public String url() {
		return base + url;
	}
	
	public static void setBase(String base) {
		AdminUrl.base = base;		
	}
	
}
