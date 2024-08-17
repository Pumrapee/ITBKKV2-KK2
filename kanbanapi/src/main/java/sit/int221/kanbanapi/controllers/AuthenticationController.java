package sit.int221.kanbanapi.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.kanbanapi.databases.userdb.entities.User;
import sit.int221.kanbanapi.dtos.LoginDTO;
import sit.int221.kanbanapi.exceptions.AuthenticationFailed;
import sit.int221.kanbanapi.services.AuthenticationService;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = {"http://ip23kk2.sit.kmutt.ac.th","http://localhost:5173","http://intproj23.sit.kmutt.ac.th"})
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity login(@RequestBody @Valid LoginDTO login) {
        if (!authenticationService.authenticate(login.getUsername(), login.getPassword())) {
            throw new AuthenticationFailed("username or password incorrect");
        }
        return new ResponseEntity(HttpStatus.OK);
    }
}
