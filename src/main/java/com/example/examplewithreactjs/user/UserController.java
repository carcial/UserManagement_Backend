package com.example.examplewithreactjs.user;

import com.example.examplewithreactjs.user.role.Role;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "api/v1/user")
@CrossOrigin
@AllArgsConstructor
public class UserController {


    private UserService userService;



    @PostMapping(path = "/register")
    public String addUser(@RequestBody User user){
        if(user.getEmail().isEmpty()|| user.getName().isEmpty()||user.getSurName().isEmpty() ){
            throw new IllegalStateException("all must be field");
        }
        return userService.register(user);
        //return userService.addUser(user);
    }

    @GetMapping(path = "/see_users/{authorisedUserId}")
    public List<User> getAllUsers(@PathVariable("authorisedUserId") int authorisedUserId){
        return  userService.getAllUsers(authorisedUserId);
    }

    @GetMapping(path = "/getUser/{id}")
    public User getUser(@PathVariable("id") int id){
        return userService.getUser(id);
    }

    @GetMapping(path = "/getRoles/{id}")
    public List<Role> getUsersRoles(@PathVariable("id") int id){
        return userService.getUserRoles(id);
    }

    @PostMapping(path = "/login")
    public UserLogin userLogin(@RequestBody UserLogin user) {
        return userService.userLogin(user);
    }

    @PutMapping(path = "/add_new_role/{managerId}/{userId}")
    public String addNewRole(@PathVariable("managerId") int managerId,
                             @PathVariable("userId") int userId,
                             @RequestParam String newRole){
        return userService.addNewRoleToUser(managerId,userId,newRole);
    }

    @PutMapping(path = "/saveChanges/{id}")
    public User saveChanges(@PathVariable("id") int id,
                            @RequestParam(required = false) String name,
                            @RequestParam(required = false) String surName,
                            @RequestParam(required = false) String pass,
                            @RequestParam(required = false) String email){


        return  userService.saveChanges(id,name,surName,pass,email);
    }

    @PutMapping(path = "/upload/{id}")
    public String uploadImage(@PathVariable("id") int id, @RequestParam("image") MultipartFile file) throws IOException {
        return userService.uploadImage(file,id);
    }



    @GetMapping(path = "/getImage/{id}")
    public ResponseEntity<?> getUploadedImage(@PathVariable("id") int id){
        byte[] imageData =  userService.getUploadedImage(id);
        String imgContentTpy = userService.getConTyp(id);
        return  ResponseEntity.status(HttpStatus.OK)
                              .contentType(MediaType.valueOf(imgContentTpy))
                              .body(imageData);
    }

    @DeleteMapping(path = "/deleteUser/{email}/{authorisedUserId}")
    public String deleteUser(@PathVariable("email") String email,@PathVariable("authorisedUserId") int authorisedUserId){
        String msg = userService.deleteUser(email,authorisedUserId);
        userService.reorganisePagesAfterDeletingUsers();
        return msg ;
    }
}
