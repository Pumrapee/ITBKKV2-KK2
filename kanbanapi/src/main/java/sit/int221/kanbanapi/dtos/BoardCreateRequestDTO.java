package sit.int221.kanbanapi.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardCreateRequestDTO {
    @Size(min = 1, max = 120)
    @NotNull
    @NotBlank
    private String name;

    public String getBoardName() {
        return name;
    }
}
