package ru.kata.spring.boot_security.demo.services;

import ru.kata.spring.boot_security.demo.models.Role;

import java.util.List;

public interface RoleService {
    List<Role> getRolesList();
    Role findRoleByName(String name);
    Role findRole(Integer id);
}
