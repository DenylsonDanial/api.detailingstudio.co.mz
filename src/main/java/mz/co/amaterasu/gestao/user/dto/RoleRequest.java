package mz.co.amaterasu.gestao.user.dto;

import java.util.Set;


public record RoleRequest (
		Long id,
		String name,
		String displayName,
		Set<Long> permissions
){}
