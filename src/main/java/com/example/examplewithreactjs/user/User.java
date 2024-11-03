package com.example.examplewithreactjs.user;



import com.example.examplewithreactjs.user.role.Role;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String surName;

    private String email;

    private String pass;
    @Lob
    @Column(length = 490000000)
    private byte[] profilePic;

    private String imageContentType;

    private int page;

    @ManyToMany
    @JoinTable(name = "user_role",
               joinColumns = @JoinColumn(name = "user_Id"),
               inverseJoinColumns = @JoinColumn(name = "role_Id"))
    private Set<Role> userRoles = new HashSet<>();

    public User(String name,String surName,String email, String pass){
        this.name = name;
        this.surName = surName;
        this.email = email;
        this.pass = pass;
    }


    public void  addRole(Role role){
        this.userRoles.add(role);
    }
    public void  removeRole(Role role){
        this.userRoles.remove(role);
    }
    public List<Role> allRolesOfUser(){
        return new ArrayList<>(userRoles);
    }



}
