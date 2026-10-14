package mz.co.amaterasu.gestao.user.dto.projection;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.user.dto.PermissionDTO;
import mz.co.amaterasu.gestao.user.entity.Role;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor @AllArgsConstructor
public class PRoleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    @NotNull(message = "{notnull.roleName}")
    @NotEmpty(message = "{notnull.roleName}")
    private String name;

    private String displayName;

    @NotNull(message = "{notnull.permissions}")
    @NotEmpty(message = "{notnull.permissions}")
    private Set<PermissionDTO> permissions = new HashSet<>();

    public PRoleDTO(Role role) {
        this.id = role.getId();
        this.name = role.getName();
        this.displayName = role.getDisplayName();
        if (role.getPermissions().size() > 0) {
            role.getPermissions().stream()
                    .map(x -> this.permissions.add(new PermissionDTO(x))).collect(Collectors.toSet());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Set<PermissionDTO> getpermissions() {
        return permissions;
    }

    public void setpermissions(Set<PermissionDTO> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return "RoleRequest [id=" + id + ", name=" + name + ", displayName=" + displayName + ", permissions="
                + permissions + "]";
    }


}
