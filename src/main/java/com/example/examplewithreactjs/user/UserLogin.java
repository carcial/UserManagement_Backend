package com.example.examplewithreactjs.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserLogin {

    private int id;
    private String name;
    private String email;
    private String pass;

    private String message;

}
