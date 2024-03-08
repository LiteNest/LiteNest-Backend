package container.desktop.containerdesktopbackend.entity;

import container.desktop.api.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class BackendUser implements User {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "username")
    private String username;
    @Column(name = "password")
    private String password;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles")
    @Enumerated(EnumType.STRING)
    private List<Role> roles;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_containers")
    private List<String> containerIds;

    private transient final Map<String, Object> attributes = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value")
    @CollectionTable(name = "user_metadata")
    private Map<String, String> metadata;



    @Override
    public void addContainer(String containerId) {
        getContainerIds().add(containerId);
    }

    @Override
    public void addRole(Role role) {
        getRoles().add(role);
    }

    @Override
    public void addMetaData(String key, String value) {
        metadata.put(key, value);
    }

    @Override
    public String getMetaData(String key) {
        return metadata.get(key);
    }

    @Override
    public void deleteMetaData(String key) {
        metadata.remove(key);
    }

    @Override
    public void clearMetaData() {
        metadata.clear();
    }

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
}
