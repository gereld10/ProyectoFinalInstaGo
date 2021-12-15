package com.example.instago;

public class UsuariosLogin {

    private String password;
    private String email;
    private String id;


    public UsuariosLogin(String password, String email, String id) {
        this.password = password;
        this.email = email;
        this.id = id;
    }

    public UsuariosLogin() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getKey() {
        return id;
    }

    public void setKey(String key) {
        this.id = id;
    }
}
