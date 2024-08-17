package sit.int221.kanbanapi.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {
    @NotNull
    @NotBlank
    private String username;

    @NotNull
    @NotBlank
    private String password;
}
