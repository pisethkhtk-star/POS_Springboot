package com.dinsaren.springbootjwtapi.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "users")
@Data
@DynamicUpdate()
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String verifyEmail;
    private String phoneNumber;
    @JsonIgnore
    private String password;
    private String status;
    private String profile;
    private String changePassword;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public User() {
    }

    public User(String username, String email, String password, String phoneNumber) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }



    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }


    public String getChangePassword() {
        return changePassword;
    }

    public void setChangePassword(String changePassword) {
        this.changePassword = changePassword;
    }


    public String getVerifyEmail() {
        return verifyEmail;
    }

    public void setVerifyEmail(String verifyEmail) {
        this.verifyEmail = verifyEmail;
    }

    public String getName() {
        if (firstName != null || lastName != null) {
            String full = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
            if (!full.isEmpty()) return full;
        }
        if (username != null && !username.isEmpty()) return username;
        return email != null ? email : "";
    }

    public void setName(String name) {
        if (name != null) {
            String[] parts = name.trim().split("\\s+", 2);
            this.firstName = parts[0];
            this.lastName = parts.length > 1 ? parts[1] : "";
            if (this.username == null || this.username.isEmpty()) {
                this.username = this.email != null ? this.email : parts[0];
            }
        }
    }

    public String getRole() {
        if (roles != null && !roles.isEmpty()) {
            for (Role r : roles) {
                if (r.getName() != null) {
                    return r.getName().name().replace("ROLE_", "");
                }
            }
        }
        return "CASHIER";
    }

    public java.util.Date getCreatedAt() {
        return getCreateAt();
    }
}
