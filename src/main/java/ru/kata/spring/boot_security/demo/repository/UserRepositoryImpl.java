package ru.kata.spring.boot_security.demo.repository;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final EntityManager entityManager;
//    private final PasswordEncoder passwordEncoder;

    public UserRepositoryImpl(EntityManager entityManager
//            , PasswordEncoder passwordEncoder
                              ) {
        this.entityManager = entityManager;
//        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User findByUsername(String username) {
        return entityManager.createQuery("select distinct u from User u left join fetch u.roles where u.username = :username", User.class)
                .setParameter("username", username).getSingleResult();
    }

    @Override
    public List<User> showAllUsers() {
        return entityManager.createQuery("from User", User.class).getResultList();
    }   //"SELECT username, email FROM User WHERE id IN (SELECT user_id FROM users_roles)", User.class

    @Override
    public void update(int id, User user) {
        entityManager.merge(user);
    }

/*  ДРУГОЙ ВАРИАНТ UPDATE
    public void updateUser(String username, User newUser) {
        User user = userRepository.findByUsername(username);
        user.setPassword(passwordEncoder.encode(newUser.getPassword()));
        user.setFirstname(newUser.getFirstname());
        user.setLastname(newUser.getLastname());
        user.setEmail(newUser.getEmail());
        user.setRoles(newUser.getRoles());
        entityManager.merge(user);
    }
 */

    @Override
    public void save(User user) throws Exception {
            if (userExists(user.getUsername())) {
                throw new Exception("User with these details already exists");
            }
//        User newUser = new User();
//        newUser.setUsername(user.getUsername());
//        newUser.setPassword(user.getPassword());
//        newUser.setEmail(user.getEmail());
//         newUser.setRoles(Arrays.asList((Role) roleRepository.findRoleByName("ROLE_USER")));
//        if (newUser.getRoles().equals("USER")) {
//            Role roleUser = (Role) roleRepository.findRoleByName("ROLE_USER");
//            newUser.setRoles((List<Role>) roleUser);
//        }
        entityManager.persist(user);
    }

    private boolean userExists(String username) {
        return findByUsername(username) != null;
    }

    @Override
    public void delete(String username) {
        User user = findByUsername(username);
        entityManager.remove(entityManager.find(User.class, user.getId()));
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username);    // создаем пользователя, которого ищем в базе по юзернейму
        if (user == null) {
            throw new UsernameNotFoundException("Not found");
        }
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (Role role: user.getRoles()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getName())); // находим все роли пользователя и отправляем в разрешения
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), grantedAuthorities);
    }

}
