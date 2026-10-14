package mz.co.amaterasu.gestao.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mz.co.amaterasu.gestao.user.entity.Permission;

import java.io.Serial;
import java.io.Serializable;

@Data @AllArgsConstructor @NoArgsConstructor
public class PermissionDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;
	private Long id;
	@NotNull(message = "{email.notempty}")
	private String name;
	private String displayName;
	@NotNull(message = "Contexto é obrigatório")
	private String contextPath;
	private String bundler;

	public PermissionDTO(Permission permission) {
		id = permission.getId();
		name = permission.getName();
		displayName = permission.getDisplayName();
		contextPath = permission.getContextPath();
		bundler = permission.getBundler();
	}

}
