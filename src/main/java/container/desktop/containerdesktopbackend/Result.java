package container.desktop.containerdesktopbackend;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class Result {
    private Integer code;
    private String message;
    private Object details;

    public static Result notFound() {
        return Result.builder().code(404).build();
    }

    public Result setMessage(String message) {
        this.message = message;
        return this;
    }

    public Result setDetails(Object details) {
        this.details = details;
        return this;
    }

    public static Result ok() {
        return Result.builder().code(200).build();
    }

    public static Result forbidden() {
        return Result.builder().code(403).build();
    }
}