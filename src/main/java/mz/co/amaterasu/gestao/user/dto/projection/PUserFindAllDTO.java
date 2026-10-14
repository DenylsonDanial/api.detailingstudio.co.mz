package mz.co.amaterasu.gestao.user.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mz.co.amaterasu.gestao.base.dto.TenantDTO;
import mz.co.amaterasu.gestao.user.entity.User;
import mz.co.amaterasu.gestao.user.enums.Gender;
import mz.co.amaterasu.gestao.user.enums.UserStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
public class PUserFindAllDTO {

    @Getter
    @Setter
    private Long id;
    @Getter @Setter
    private String username;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String firstName;
    @Getter @Setter
    private String middleName;
    @Getter @Setter
    private String surname;
    @Getter @Setter
    private Long nuit;
    @Getter @Setter
    private Gender gender;
    @Getter @Setter
    private UserStatus userStatus;
    @Getter @Setter
    private String email;
    @Getter @Setter
    private String photo;
    @Getter @Setter
    private Set<PRoleDTO> roles = new HashSet<>();
    @Getter @Setter
    private transient List<TenantDTO> businessLocations = new ArrayList<>();

    public PUserFindAllDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.firstName = user.getFirstName();
        this.middleName = user.getMiddleName();
        this.surname = user.getSurname();
        this.nuit = user.getNuit();
        this.gender = user.getGender();
        this.userStatus = user.getUserStatus();
        this.email = user.getEmail();
        this.photo = user.getPhoto();

        user.getRoles().forEach(r -> this.roles.add(new PRoleDTO(r.getId(), r.getName(),r.getDisplayName(), null)));
    }
}
