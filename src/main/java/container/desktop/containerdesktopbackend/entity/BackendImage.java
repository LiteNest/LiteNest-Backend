package container.desktop.containerdesktopbackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import container.desktop.api.entity.Image;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "image")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BackendImage implements Image {
    @Id
    @Column(name = "id")
    private String id;
    private String name;
    @Column(name = "min_vcpu")
    @JsonProperty("min_vcpus")
    private Integer minimumVcpus;
    @JsonProperty("min_ram")
    @Column(name = "min_ram")
    private Integer minimumRAM;
    @Column(name = "remote_desktop_port")
    @JsonProperty("remote_desktop_port")
    private Integer remoteDesktopPort;
    @Column(name = "available")
    private boolean available;
    @Column(name = "description")
    private String description;
    @Column(name = "min_root_disk")
    @JsonProperty("min_root_disk")
    private Integer minimumRootDisk;
    @Setter(AccessLevel.PRIVATE)
    @Column(name = "is_public")
    @JsonIgnore
    private boolean _public;

    private transient final Map<String, Object> attributes = new HashMap<>();

    @Override
    public void addAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public void deleteAttribute(String key) {
        attributes.remove(key);
    }

    @Override
    public void clearAttributes() {
        attributes.clear();
    }

    @Override
    public void setAvailable(@Nullable Boolean available) {
        if (available != null) {
            this.available = available;
        }
    }

    @Override
    public boolean isPublic() {
        return _public;
    }

    @Override
    public void setPublic(Boolean _public) {
        if (_public != null) {
            this._public = _public;
        }
    }
}
