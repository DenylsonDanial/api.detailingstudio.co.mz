package mz.co.amaterasu.gestao.user.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mz.co.amaterasu.gestao.user.enums.Gender;
import mz.co.amaterasu.gestao.user.enums.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "tb_users")
public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	@Getter
	@Setter
	private Long id;
	@Column(name = "name")
	@Getter @Setter
	private String name;
	@Column(unique = true, nullable = false)
	@Getter @Setter
	private String username;
	@Column(nullable = false)
	@Getter @Setter
	private String password;
	@Column(name = "first_name")
	@Getter @Setter
	private String firstName;
	@Getter @Setter
	@Column(name = "middle_name")
	private String middleName;
	@Getter @Setter
	private String surname;
	@Column(name = "gender")
	@Getter @Setter
	private Gender gender;
	@Column(name = "user_status", nullable = false)
	@Getter @Setter
	private UserStatus userStatus;
	@Getter @Setter
	@Column(unique = true, nullable = false)
	private String email;
	@Column(unique = true)
	@Getter @Setter
	private Long nuit;
	@Column(name = "email_verified_at")
	@Getter @Setter
	private Instant emailVerified;
	@Column(name = "created_at", updatable = false)
	@Getter @Setter
	private Instant createdAt;
	@Column(name = "password_changed_at")
	@Getter @Setter
	private Instant passwordChangedAt;
	@Column(name = "last_login_at")
	@Getter @Setter
	private Instant lastLoginAt;
	@Column(name = "locked_until")
	@Getter @Setter
	private Instant lockedUntil;
	@Column(name = "failed_login_count", nullable = false)
	@Getter @Setter
	private int failedLoginCount;
	@Getter @Setter
	private String phone;
	@Getter @Setter
	private String photo;

	@Getter @Setter
	@ManyToMany(
			cascade= {
					CascadeType.DETACH,
					CascadeType.MERGE,
					CascadeType.PERSIST,
					CascadeType.REFRESH
					},
			fetch = FetchType.EAGER)
	@JoinTable(
			name="tb_roles_users",
			joinColumns = @JoinColumn(name="user_id"),
			inverseJoinColumns = @JoinColumn(name="role_id"))
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private Set<Role> roles = new HashSet<>();

	public String getName(Gender gender, String firstName, String lastname) {
		String prefix = gender.getCode().equals(Gender.M.toString()) ? "Sr." : "Sra.";
		return String.format("%s %s %s", prefix, firstName, lastname);
	}

	public User() {
	}

	public User(Long id, String name, String username, String password, String firstName, String middleName, String surname, Gender gender, UserStatus userStatus, String email, Long nuit, Instant emailVerified, Instant createdAt, Instant passwordChangedAt, Set<Role> roles) {
		this.id = id;
		this.name = name;
		this.username = username;
		this.password = password;
		this.firstName = firstName;
		this.middleName = middleName;
		this.surname = surname;
		this.gender = gender;
		this.userStatus = userStatus;
		this.email = email;
		this.nuit = nuit;
		this.emailVerified = emailVerified;
		this.createdAt = createdAt;
		this.passwordChangedAt = passwordChangedAt;
		this.roles = roles;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<Role> roles = getRoles();
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();

		for (Role role : roles) {
			for (Permission permission: role.getPermissions()) {
				authorities.add(new SimpleGrantedAuthority(permission.getName()));
			}
			authorities.add(new SimpleGrantedAuthority(role.getName()));
		}

		return authorities;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return lockedUntil == null || lockedUntil.isBefore(Instant.now());
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return UserStatus.ACTIVE.equals(userStatus);
	}
}
