package auth;

import database.User;
import database.UserRepository;
import java.util.Optional;

public class AuthService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public AuthService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public String register(String login, String password) {
        try {
            validateCredentials(login, password);
            boolean registered = userRepository.createNewUser(login, passwordHasher.hash(password));
            return registered ? "Пользователь зарегистрирован" : "Пользователь с таким логином уже существует";
        }
        catch (Exception e) {
            return e.getMessage();
        }
    }

    public Optional<User> authenticate(String login, String password) {
        try {
            validateCredentials(login, password);
            return userRepository.findByLoginAndPasswordHash(login, passwordHasher.hash(password));
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }

    private void validateCredentials(String login, String password) throws IllegalArgumentException {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
    }
}
