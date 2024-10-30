package com.example.examplewithreactjs.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {

    User findTopByOrderByIdDesc();

    @Query("select u from User u where u.email = ?1 and u.pass = ?2")
    User findByEmailAndPassword(String email, String password);

    @Query("SELECT" +
            " case when count(u)>0" +
            " then true else false end " +
            "FROM User u " +
            "where u.email = ?1")
    boolean findUserByEmail(String mail);

    @Query("select u from User u where u.email = ?1")
    User findByEmail(String email);

    @Query("select count(u.page) from User u group by u.page")
    List<Integer> findByPages();
}
