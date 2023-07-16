package quiz.users.api;

import org.springframework.web.bind.annotation.*;
import quiz.users.api.dto.UserCreateDTO;
import quiz.users.api.dto.UserGetDTO;
import quiz.users.service.UserService;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping()
  public List<UserGetDTO> getAll(){
    return this.userService.findAll();
  }

  @GetMapping("{id}")
  public UserGetDTO getById(@PathVariable @NotNull Long id){
    return this.userService.getById(id);
  }

  @PostMapping("/registration")
  public Long create(@RequestBody @NotNull UserCreateDTO userDTO){
    return this.userService.add(userDTO);
  }

  @PutMapping("{id}")
  public Long update(@PathVariable @NotNull Long id, @RequestBody @NotNull UserCreateDTO userDTO){
    userDTO.setId(id);
    return this.userService.update(userDTO);
  }

  @DeleteMapping("{id}")
  public Long delete(@PathVariable @NotNull Long id, @RequestBody @NotNull UserCreateDTO userDTO){
    userDTO.setId(id);
    return this.userService.delete(userDTO);
  }

}
