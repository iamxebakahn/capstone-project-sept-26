package dev.zeba.userauthservice.Service;

import dev.zeba.userauthservice.model.Role;
import dev.zeba.userauthservice.model.User;
import dev.zeba.userauthservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User signup(String email, String password){
            if(userRepository.existsByEmail(email)){
                return null;
            }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password)); //can't store pass directly, we need to use Bencrypt for it
        //its hashing not encoding, but still .encode is the term
        //different salt given to each password due to Bcrypt
        //3 parts: $ separated : 2a -> version of BCrypt algo; 10 -> cost (2^10/1024 iterations to break password: actually making it slow)
        //salt(random value added to generate hash)+password : 22 chars + 31 chars
        user.setRole(Role.USER);
        return userRepository.save(user);
    }
}

