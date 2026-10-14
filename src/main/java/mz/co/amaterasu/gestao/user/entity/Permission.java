package mz.co.amaterasu.gestao.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @NoArgsConstructor @AllArgsConstructor
@Data @Table(name="tb_permissions")
public class Permission {
// TODO adicionar campo isDefault, esse vai ser usado pra garantir que não são apagadas permissoes
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	@Column(unique=true, name = "name")
	private String name;
	@Column(unique=true, name = "display_name")
	private String displayName;
	@Column(unique=true, name = "context_path")
	private String contextPath;
	private String bundler;


}
