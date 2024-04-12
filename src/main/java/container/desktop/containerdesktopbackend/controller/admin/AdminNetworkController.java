package container.desktop.containerdesktopbackend.controller.admin;

import container.desktop.api.entity.Network;
import container.desktop.api.service.NetworkService;
import container.desktop.containerdesktopbackend.DTO.NetworkUpdatingDTO;
import container.desktop.containerdesktopbackend.Result;
import container.desktop.containerdesktopbackend.entity.BackendNetwork;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/networks")
public class AdminNetworkController {

    @Resource(name = "network_service")
    private NetworkService<BackendNetwork> networkService;

    @PutMapping("/{networkId}")
    public ResponseEntity<Result> update(@PathVariable String networkId,
                                         @RequestBody NetworkUpdatingDTO networkUpdatingDTO) {
        Network network = networkService.findById(networkId);
        if (network == null) {
            return new ResponseEntity<>(Result.notFound().setMessage("网络" + networkId + "不存在"),
                    HttpStatus.NOT_FOUND);
        } else {
            if (networkUpdatingDTO.available() != null) {
                network.setAvailable(networkUpdatingDTO.available());
            }
            if (networkUpdatingDTO.shown() != null) {
                network.setShown(networkUpdatingDTO.shown());
            }
            networkService.update((BackendNetwork) network);
            return new ResponseEntity<>(Result.ok(), HttpStatus.OK);
        }
    }
}
