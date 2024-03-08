package container.desktop.containerdesktopbackend;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public enum Unit {
    SECOND(1),
    MINUTE(60),
    HOUR(60*60),
    DAY(60*60*24);
    private final Integer value;
}