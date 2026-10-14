package mz.co.amaterasu.gestao.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity @NoArgsConstructor @AllArgsConstructor
@Table(name="tb_roles")
public class Role {

	@Setter
	@Getter
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Setter
	@Getter
    @Column(unique=true, name = "name")
	private String name;
	@Setter
	@Getter
	@Column(unique=true, name = "display_name")
	private String displayName;

	public Role(Long id, String name, String displayName) {
		this.id = id;
		this.name = name;
		this.displayName = displayName;
	}
	@Getter
	@Setter
	@ManyToMany(
			cascade= {
					CascadeType.DETACH,
					CascadeType.MERGE,
					CascadeType.PERSIST,
					CascadeType.REFRESH
					},
			fetch = FetchType.EAGER)
	@JoinTable(
			name="tb_permissions_roles",
			joinColumns = @JoinColumn(name="role_id"),
			inverseJoinColumns = @JoinColumn(name="permission_id"))
	private final Set<Permission> permissions = new HashSet<>();

	@Setter
    @JsonIgnore
	@ManyToMany(mappedBy = "roles")
	private Set<User> users = new HashSet<>();

	public void setPermissions(Set<Permission> permissions) {
		this.permissions.addAll(permissions);
	}

    @Override
	public String toString() {
		return "Role [id=" + id + ", name=" + name + ", displayName=" + displayName + ", permissions=" + permissions
				+ ", users=" + users + "]";
	}

}
