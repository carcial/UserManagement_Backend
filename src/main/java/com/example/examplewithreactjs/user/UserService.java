package com.example.examplewithreactjs.user;

import com.example.examplewithreactjs.user.Role.Role;
import com.example.examplewithreactjs.user.Role.RoleRepository;
import lombok.AllArgsConstructor;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Service
@AllArgsConstructor
public class UserService {


    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private AuthenticationManager authenticationManager;

    private PasswordEncoder bCryptPass;


    public String register(User user){

        boolean existingUser = userRepository.findUserByEmail(user.getEmail());

        if(existingUser){
            throw new RuntimeException("user with the email ' "+user.getEmail()+" ' exist already !!!");
        }

        String encodedPassword = bCryptPass.encode(user.getPass());


        List<Integer> allSetsPages = userRepository.findByPages();
        List<Role> listOfRoles = roleRepository.findAll();
        String manager = "boos@email.com";

        if(allSetsPages.isEmpty()){
            user.setPage(1);
        }
        else {
            List<User> allUserInDB = userRepository.findAll();
            int page = allUserInDB.get(allUserInDB.size()-1).getPage();
            if(allSetsPages.get(allSetsPages.size()-1) < 4){
                user.setPage(page);
            }
            else {
                page++;
                user.setPage(page);
            }
        }
        if (listOfRoles.size() == 0){
            Role role1 = new Role("USER");
            Role role2 = new Role("ADMIN");
            Role role3 = new Role("MANAGER");

            roleRepository.saveAll(List.of(role1,role2,role3));
            if (user.getEmail().equals(manager)){
                for (Role role: listOfRoles) {
                    user.addRole(role);
                }
            }
            else {
                Role role = roleRepository.findRoleByName("USER");
                user.addRole(role);
            }
        }
        else {
            if (user.getEmail().equals(manager)){
                for (Role role: listOfRoles) {
                    user.addRole(role);
                }
            }
            else {
                Role role = roleRepository.findRoleByName("USER");
                user.addRole(role);
            }
        }

        user.setPass(encodedPassword);
        userRepository.save(user);

        return Integer.toString(user.getId());
    }

    public UserLogin userLogin(UserLogin user){
        Authentication authentication;

        try {
           authentication = authenticationManager
                            .authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(),user.getPass()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            user.setMessage("success");
            User user1 = userRepository.findByEmail(user.getEmail());
            user.setId(user1.getId());
            user.setName(user1.getName());
        }catch (Exception e){
            user.setMessage("failure");
            throw new BadCredentialsException("invalid credential");
        }
        return user;
    }

    @Transactional
    public User saveChanges(int id, String name, String surName, String pass, String email){

        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("user not found"));

        boolean isAdmin = user.getUserRoles().stream().anyMatch(role -> role.getRoleName().equals("ADMIN"));

        if(!isAdmin){
            throw new BadCredentialsException("access denied for non 'ADMIN'");
        }

        if(name != null){
            user.setName(name);
        }
        if(surName != null){
            user.setSurName(surName);
        }
        if(pass != null){
            String encodedPass = bCryptPass.encode(pass);
            user.setPass(encodedPass);
        }
        if(email != null){
            user.setEmail(email);
        }
        return user;
    }


    public User getUser(int id){
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("user not found"));
    }


    public List<User> getAllUsers(int authorisedUserId){
        User authorisedUser = userRepository.findById(authorisedUserId)
                .orElseThrow(()->new RuntimeException("User not found"));

        boolean isAuthorised = authorisedUser.getUserRoles()
                .stream().anyMatch(role -> role.getRoleName().equals("ADMIN"));
        boolean isAuthorised2 = authorisedUser.getUserRoles()
                .stream().anyMatch(role -> role.getRoleName().equals("MANAGER"));

        if(!isAuthorised && !isAuthorised2){
            throw new BadCredentialsException("You are not authorise to get all user's information");
        }
        return userRepository.findAll();
    }

    public List<Role> getUserRoles(int id){
        User user = userRepository.findById(id)
                .orElseThrow(()->new RuntimeException("User not found"));
        return user.allRolesOfUser();
    }

    @Transactional
    public String addNewRoleToUser(int managerId,int userId,String newRole){
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("There is no Manager with this ID"));
        boolean isAuthorised = manager.getUserRoles()
                .stream().anyMatch(role -> role.getRoleName().equals("MANAGER"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("There is no User with this ID"));

        if(!isAuthorised){
            throw new BadCredentialsException("Authorised only for Manager/s");
        }

        boolean isAdmin = user.getUserRoles()
                .stream().anyMatch(role -> role.getRoleName().equals("ADMIN"));
        boolean isManager = user.getUserRoles()
                .stream().anyMatch(role -> role.getRoleName().equals("MANAGER"));

        if(user.allRolesOfUser().size() < 3){
            if(!newRole.equals("USER")){
                Role role = roleRepository.findRoleByName(newRole);
                if(isAdmin || isManager){
                    throw new RuntimeException("The user is already "+ newRole);
                }

                user.addRole(role);

            }

        }

        return "Successful";
    }
    public String  deleteUser(String emailOfUserTobeDeleted,int authorisedUserId){
        User authorisedUser = userRepository.findById(authorisedUserId)
                .orElseThrow(()->new RuntimeException("User not found"));

        boolean isAuthorised = authorisedUser.getUserRoles()
                .stream().anyMatch(role -> role.getRoleName().equals("MANAGER"));

        if(!isAuthorised){
            throw new BadCredentialsException("You are not authorise to get all user's information");
        }
        boolean exist  = userRepository.findUserByEmail(emailOfUserTobeDeleted);

        if(exist){
            User user = userRepository.findByEmail(emailOfUserTobeDeleted);
            userRepository.deleteById(user.getId());
            return  "success";
        }
        else {
            throw new RuntimeException("The user to be deleted was not found");
        }

    }

    @Transactional
    public void reorganisePagesAfterDeletingUsers(){
        List<User> allUserInDB = userRepository.findAll();
        int page = 1;
        int countPages = 0;
        for(User user : allUserInDB){
            if(countPages < 4){
                user.setPage(page);
                countPages++;
            }
            else {
                countPages = 1;//because the user's page that did not satisfy the condition up is already
                              // been set here, that is why only 3 more users will be needed
                page++;
                user.setPage(page);
            }

        }


    }

    /*==========================================================================*/
    public  byte[] compressImage(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setLevel(Deflater.BEST_COMPRESSION);
        deflater.setInput(data);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] tmp = new byte[4*1024];
        while (!deflater.finished()) {
            int size = deflater.deflate(tmp);
            outputStream.write(tmp, 0, size);
        }
        try {
            outputStream.close();
        } catch (Exception ignored) {
        }
        return outputStream.toByteArray();
    }



    public byte[] decompressImage(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] tmp = new byte[4*1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(tmp);
                outputStream.write(tmp, 0, count);
            }
            outputStream.close();
        } catch (Exception ignored) {
        }
        return outputStream.toByteArray();
    }
    /*==========================================================================*/



    @Transactional
    public String uploadImage(MultipartFile file, int id) throws IOException {
        User user = userRepository.findById(id).orElseThrow(() ->  new RuntimeException("user not found"));
        if(user != null){
            user.setProfilePic(compressImage(file.getBytes()));
            user.setImageContentType(file.getContentType());
            System.out.println("image stored");
            return  "image stored successfully";
        }
        return  null;
    }

    public byte[] getUploadedImage(int id){
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("user not found"));
        if(user != null){
            if(user.getProfilePic() != null){
                return decompressImage(user.getProfilePic());
            }
        }
        return null;
    }

    public String  getConTyp(int id){
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("user not found"));
        if(user != null){
            return user.getImageContentType();
        }
        return null;
    }


}
