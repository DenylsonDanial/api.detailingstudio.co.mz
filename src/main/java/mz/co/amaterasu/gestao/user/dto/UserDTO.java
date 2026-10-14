package mz.co.amaterasu.gestao.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mz.co.amaterasu.gestao.base.dto.TenantDTO;
import mz.co.amaterasu.gestao.user.enums.Gender;
import mz.co.amaterasu.gestao.user.enums.UserStatus;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor @AllArgsConstructor
public class UserDTO {

	@Getter @Setter
	private Long id;
	@Getter @Setter
	private String username;
	@Getter @Setter
	private String name;
	@Getter @Setter
	private String password;
	@Getter @Setter
	private String passwordConfirm;
	@Getter @Setter
	private String currentPassword;
	@Getter @Setter
	@NotNull(message = "{notnull.firstName}")
	private String firstName;
	@Getter @Setter
	private String middleName;
	@Getter @Setter
	@NotNull(message = "{notnull.lastname}")
	private String surname;
	@Getter @Setter
	private String phone;
	@Getter @Setter
	private Long nuit;
	@Getter @Setter
	private Gender gender;
	@Getter @Setter
	private UserStatus userStatus;
	@Email(message = "{notvalid.email}")
	@NotNull(message = "{notnull.email}")
	@Getter @Setter
	private String email;
	@Getter @Setter
	private Instant emailVerified;
	@Getter @Setter
	private Instant createdAt;
	@Getter @Setter
	private Instant passwordChangedAt;
	@Getter @Setter
	private List<Long> rolesIds;
	@Getter @Setter
	private List<TenantDTO> businessLocations;
	@Getter @Setter
	private List<Long> locationsIds;

	@Getter @Setter
	private String photo;

	@Getter @Setter
	private String creationSource;

	public void setName(String firstName, String surname) {
		name = String.format("%s %s", firstName, surname);
	}

}
