package com.elektrifi.sanctions.beans;

import java.sql.Date;

public class UserBean {

	private String 	userName;
	private String 	firstName;
	private String 	secondName;
	private String 	lastName;
	private	String	emailAddress;
	private int		user_id;
	private String 	password;
	private String	sessionId;
	private Date	session_created;	
	private Date	session_updated;	
	private	Date	date_created;
	private	Date	date_updated;
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getSecondName() {
		return secondName;
	}
	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public Date getSession_created() {
		return session_created;
	}
	public void setSession_created(Date session_created) {
		this.session_created = session_created;
	}
	public Date getSession_updated() {
		return session_updated;
	}
	public void setSession_updated(Date session_updated) {
		this.session_updated = session_updated;
	}
	public Date getDate_created() {
		return date_created;
	}
	public void setDate_created(Date date_created) {
		this.date_created = date_created;
	}
	public Date getDate_updated() {
		return date_updated;
	}
	public void setDate_updated(Date date_updated) {
		this.date_updated = date_updated;
	}
	
	@Override
	public String toString() {
		return "UserBean [userName=" + userName + ", firstName=" + firstName
				+ ", secondName=" + secondName + ", lastName=" + lastName
				+ ", emailAddress=" + emailAddress + ", user_id=" + user_id
				+ ", password=" + password + ", sessionId=" + sessionId
				+ ", session_created=" + session_created + ", session_updated="
				+ session_updated + ", date_created=" + date_created
				+ ", date_updated=" + date_updated + "]";
	}
}
