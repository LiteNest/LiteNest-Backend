package container.desktop.containerdesktopbackend.controller;

import container.desktop.api.entity.Network;
import container.desktop.api.service.NetworkService;
import container.desktop.containerdesktopbackend.Result;
import container.desktop.containerdesktopbackend.entity.BackendNetwork;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/network")
@RequiredArgsConstructor
public class NetworkController {

    private final NetworkService<BackendNetwork> networkService;

    @GetMapping("/")
    public ResponseEntity<Result> list() {
        List<? extends Network> networks = networkService.list();
        Result result = Result.builder()
                .code(200)
                .details(networks)
                .build();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


}
