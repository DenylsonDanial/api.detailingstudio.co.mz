package mz.co.amaterasu.gestao.user.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mz.co.amaterasu.gestao.base.dto.TenantDTO;
import mz.co.amaterasu.gestao.user.entity.Role;
import mz.co.amaterasu.gestao.user.entity.User;
import mz.co.amaterasu.gestao.user.enums.Gender;
import mz.co.amaterasu.gestao.user.enums.UserStatus;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor @AllArgsConstructor
public class PUserDTO {

        private Long id;
        private String username;
        private String name;
        private String password;
        private String firstName;
        private String middleName;
        private String surname;
        private Long nuit;
        private Gender gender;
        private UserStatus userStatus;
        private String email;
        private String photo;
        private Instant emailVerified;
        private Instant createdAt;
        private Instant passwordChangedAt;
        private Set<Role> roles;
        private transient List<TenantDTO> businessLocations;


        public PUserDTO(User user) {
                this.id = user.getId();
                this.username = user.getUsername();
                this.name = user.getName();
                this.password = user.getPassword();
                this.firstName = user.getFirstName();
                this.middleName = user.getMiddleName();
                this.surname = user.getSurname();
                this.nuit = user.getNuit();
                this.gender = user.getGender();
                this.userStatus = user.getUserStatus();
                this.email = user.getEmail();
                this.photo = user.getPhoto();
                this.emailVerified = user.getEmailVerified();
                this.createdAt = user.getCreatedAt();
                this.passwordChangedAt = user.getPasswordChangedAt();
                this.roles = user.getRoles();
        }
}
