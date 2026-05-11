package com.tlu.hrm.controller;

import com.tlu.hrm.model.UserExt;
import com.tlu.hrm.repository.UserExtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserExtController {

    @Autowired
    private UserExtRepository userRepository;

    @GetMapping
    public List<UserExt> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public UserExt createUser(@RequestBody UserExt user) {
        return userRepository.save(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserExt> getUserById(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserExt> updateUser(@PathVariable UUID id, @RequestBody UserExt userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(userDetails.getUsername());
            if(userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(userDetails.getPassword());
            }
            user.setEmail(userDetails.getEmail());
            user.setActive(userDetails.getActive());
            user.setRoles(userDetails.getRoles());
            user.setStaff(userDetails.getStaff());
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
