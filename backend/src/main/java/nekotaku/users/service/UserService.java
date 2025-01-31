package quiz.users.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import quiz.users.api.dto.UserCreateDTO;
import quiz.users.api.dto.UserGetDTO;
import quiz.users.entity.User;
import quiz.users.repository.IUserRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
@Service
public class UserService {
  @Autowired
  private IUserRepository userRepository;

  private UserGetDTO userToUserGetDTO(User user){
    UserGetDTO userGetDTO = new UserGetDTO(user);
    userGetDTO.setQuizzes(user.getQuizzes());
    return userGetDTO;
  }

  public User findById(@NotNull Long id){
    return this.userRepository.findById(id).orElseThrow(
        () -> new RuntimeException(String.format("Пользователь с ИД %d не найден", id))
    );
  }

  public UserGetDTO getById(@NotNull Long id){
    return userToUserGetDTO(findById(id));
  }

  public List<UserGetDTO> findAll(){
    return this.userRepository.findAll().stream().map(this::userToUserGetDTO).toList();
  }

  public Long add(@Valid @NotNull UserCreateDTO userData) {
    User user = userData.getId() == null ? new User() : findById(userData.getId());

    user.setUsername(userData.getUsername());
    user.setEmail(userData.getEmail());
    user.setPassword(userData.getPassword());
    user.setDeleted(userData.isDeleted());

    return this.userRepository.save(user).getId();
  }

  public Long update(@Valid @NotNull UserCreateDTO userData){
    return add(userData);
  }

  public Long delete(@Valid @NotNull UserCreateDTO userData){
    userData.setDeleted(true);
    return add(userData);
  }

}
