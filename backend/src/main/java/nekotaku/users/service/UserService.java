package nekotaku.users.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import nekotaku.auth.api.dto.RegistrationUserDto;
import nekotaku.roles.services.RoleService;
import nekotaku.users.api.dto.UserCreateDTO;
import nekotaku.users.api.dto.UserGetDTO;
import nekotaku.users.api.dto.UserUpdateDTO;
import nekotaku.users.model.User;
import nekotaku.users.repository.IUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
    private final Logger logger = LoggerFactory.getLogger(UserDetailsService.class);
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    private UserGetDTO userToUserGetDTO(User user) {
        return new UserGetDTO(user);
    }

    public User findById(@NotNull Long id){
        return userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с ИД %d не найден", id))
        );
    }

    public User findByUserName(@NotNull String username) {
        return userRepository.findByUsername(username);
    }

    public UserGetDTO getById(@NotNull Long id) {
        return userToUserGetDTO(findById(id));
    }

    public UserGetDTO getByUserName(@NotNull String username) {
        return userToUserGetDTO(findByUserName(username));
    }

    public List<UserGetDTO> findAll() {
        return userRepository.findAll().stream().map(this::userToUserGetDTO).toList();
    }

    public Long add(@Valid @NotNull UserCreateDTO userData) {
        try {
            User user = userData.getId() != null ? findById(userData.getId()) : new User();
            user.setUsername(userData.getUsername());
            user.setEmail(userData.getEmail());
            user.setPassword(userData.getPassword());
            user.setActive(userData.isActive());
            return userRepository.save(user).getId();
        } catch (EntityExistsException e) {
            logger.error("Пользователь с именем '{}' уже существует.", userData.getUsername(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Произошла ошибка при добавлении пользователя: {}", userData.getUsername(), e);
            throw new IllegalArgumentException("Не удалось добавить пользователя", e);
        }
    }

    public Long update(@Valid @NotNull UserUpdateDTO userData) throws IOException {
        try {
            //
            User user = findById(userData.getId());
            if (userData.getAvatar() != null) {
                //todo
                //String avatarUrl = Utils.setPoster(userData.getAvatar(), user.getId(), user.getAvatarURL(), "/images/user/avatar/");
                //userRepository.updateAvatar(avatarUrl, user.getId());
            }
            return user.getId();
        } catch (EntityNotFoundException e) {
            logger.error("Пользователь с ИД {} не найден.", userData.getId(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Произошла ошибка при обновлении пользователя: {}", userData.getId(), e);
            throw new IllegalArgumentException("Не удалось обновить пользователя", e);
        }
    }

    public Long delete(@Valid @NotNull UserCreateDTO userData) {
        try {
            userData.setActive(false);
            return add(userData);
        } catch (Exception e) {
            logger.error("Произошла ошибка при удалении пользователя: {}", userData.getUsername(), e);
            throw new IllegalArgumentException("Не удалось удалить пользователя", e);
        }
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = Optional.ofNullable(findByUserName(username))
                    .orElseThrow(() -> new EntityNotFoundException(String.format("Пользователь '%s' не найден", username)));
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).toList()
            );
        } catch (EntityNotFoundException e) {
            logger.error("Пользователь '{}' не найден.", username);
            throw e;
        } catch (Exception e) {
            logger.error("Произошла ошибка при загрузке пользователя: {}", username, e);
            throw new IllegalArgumentException("Не удалось загрузить пользователя", e);
        }
    }

    public User createNewUser(RegistrationUserDto registrationUserDto) {
        try {
            User user = new User();
            user.setUsername(registrationUserDto.getUsername());
            user.setEmail(registrationUserDto.getEmail());
            user.setPassword(passwordEncoder.encode(registrationUserDto.getPassword()));
            user.setRoles(List.of(roleService.getUserRole()));
            return userRepository.save(user);
        } catch (EntityExistsException e) {
            logger.error("Пользователь с именем '{}' уже существует.", registrationUserDto.getUsername());
            throw e;
        } catch (Exception e) {
            logger.error("Произошла ошибка при создании нового пользователя: {}", registrationUserDto.getUsername(), e);
            throw new IllegalArgumentException("Не удалось создать нового пользователя", e);
        }
    }

}
