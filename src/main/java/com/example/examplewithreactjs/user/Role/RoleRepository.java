package com.example.examplewithreactjs.user.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepository extends JpaRepository<Role,Integer> {

    @Query("select r from Role r where r.roleName =?1")
    Role findRoleByName(String role);
}
