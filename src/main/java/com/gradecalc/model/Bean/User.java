package com.gradecalc.model.Bean;

public class User {
    private String id;
    private String username;
    private String password;
    private String fullname;
    private String student_code;

    public User(String id, String username, String password, String fullname, String student_code) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.student_code = student_code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getStudent_code() {
        return student_code;
    }

    public void setStudent_code(String student_code) {
        this.student_code = student_code;
    }
}
