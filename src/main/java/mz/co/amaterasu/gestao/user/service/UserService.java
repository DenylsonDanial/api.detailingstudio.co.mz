package mz.co.amaterasu.gestao.user.service;


import mz.co.amaterasu.gestao.base.repository.TenantRepository;
import mz.co.amaterasu.gestao.base.service.EmailService;
import mz.co.amaterasu.gestao.base.repository.UserTenantRepository;
import mz.co.amaterasu.gestao.user.annotaion.MasterDB;
import mz.co.amaterasu.gestao.base.entity.UserTenant;
import org.apache.commons.lang3.StringUtils;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.user.dto.PasswordSetDTO;
import mz.co.amaterasu.gestao.user.dto.RequestSuccessResponse;
import mz.co.amaterasu.gestao.user.dto.UserDTO;
import mz.co.amaterasu.gestao.user.dto.projection.PUserDTO;
import mz.co.amaterasu.gestao.user.dto.projection.PUserFindAllDTO;
import mz.co.amaterasu.gestao.user.entity.Role;
import mz.co.amaterasu.gestao.user.entity.User;
import mz.co.amaterasu.gestao.user.enums.UserStatus;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import mz.co.amaterasu.gestao.exceptions.ValidationException;
import mz.co.amaterasu.gestao.user.repository.RoleRepository;
import mz.co.amaterasu.gestao.user.repository.UserRepository;
import mz.co.amaterasu.gestao.util.Utils;
import mz.co.amaterasu.gestao.util.GenericMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    public static final String NOTFOUND_USER = "notfound.user";
    private final UserRepository repository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final EmailService emailService;

    private final UserTenantRepository userTenantRepository;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @MasterDB
    public Page<PUserFindAllDTO> findAll(Pageable pageable, String status, String name) {
        Page<PUserFindAllDTO> page;
        if (Objects.isNull(status)) {
            page = repository.findAllPaginated(null, name, pageable);
        } else {
            page = repository
                    .findAllPaginated(UserStatus.valueOf(status), name, pageable);
        }

        List<PUserFindAllDTO> users =
                page.getContent().stream().peek(user -> user.setBusinessLocations(repository.findAllTenantsByUsername(user.getUsername()))).collect(Collectors.toList());

        return new PageImpl<>(users, pageable, page.getTotalElements());

    }

    @MasterDB
    public PUserDTO findById(Long id) throws ApiRequestException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        User obj = repository.findById(id)
                .orElseThrow(() -> new ApiRequestException(environment.getProperty(NOTFOUND_USER)));

        PUserDTO pUserDTO = new PUserDTO(obj);

        pUserDTO.setBusinessLocations(repository.findAllTenantsByUsername(pUserDTO.getUsername()));
        return pUserDTO;
    }

    @MasterDB
    public PUserDTO save(UserDTO userPojo) throws ApiRequestException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        if (StringUtils.isNotBlank(userPojo.getCurrentPassword()) && !StringUtils.equals(userPojo.getPassword(), userPojo.getPasswordConfirm())) {
            throw new ApiRequestException("As senhas devem ser iguais.");
        }
        if (!validations(userPojo).isEmpty()) {
            throw new ValidationException(validations(userPojo));
        }

        if (Objects.isNull(userPojo.getRolesIds()) && Objects.isNull(userPojo.getCreationSource())) {
            throw new ApiRequestException(environment.getProperty("notnull.roles"));
        }

        if (StringUtils.isBlank(userPojo.getUsername())) {
            userPojo.setUsername(generateUsername(userPojo.getFirstName(), userPojo.getSurname()));
        }

        List<Long> roleIds = new ArrayList<>();

        if (Objects.nonNull(userPojo.getRolesIds())) {
            roleIds = userPojo.getRolesIds();
        }

        if (StringUtils.isNotBlank(userPojo.getCreationSource())) {
            Optional<Role> role = roleRepository.findByName(userPojo.getCreationSource());
            if (role.isPresent()) {
                roleIds.add(role.get().getId());
            }
        }

        userPojo.setUserStatus(UserStatus.ACTIVE);
        userPojo.setPassword(passwordEncoder.encode(userPojo.getPassword()));
        userPojo.setName(userPojo.getFirstName(), userPojo.getSurname());

        User user = repository.save(GenericMapper.map(userPojo, User.class));
        for (Long id : roleIds) {
            Optional<Role> r = roleRepository.findById(id);
            if (r.isEmpty()) {
                throw new ApiRequestException(environment.getProperty("notfound.role"));
            }
            addRoleToUser(user.getUsername(), r.get().getName());
        }

        if (userPojo.getLocationsIds() != null) {
            for (long location : userPojo.getLocationsIds()) {
                tenantRepository.findById(location)
                        .ifPresent(item -> userTenantRepository.save(new UserTenant(null, userPojo.getUsername(), item.getSlug())));

            }
        }

        return GenericMapper.map(user, PUserDTO.class);
    }

    @MasterDB
    public PUserDTO update(Long id, UserDTO userDTO, boolean isAccessUpdate) throws ApiRequestException {

        User u = repository
                .findById(id).orElseThrow(() -> new ApiRequestException(environment.getProperty(NOTFOUND_USER)));

        if (userDTO.getRolesIds() != null) {
            u.getRoles().clear();

            for (Long roleId : userDTO.getRolesIds()) {
                roleRepository.findById(roleId).ifPresent(x -> u.getRoles().add(x));
            }
        }

        if (!isAccessUpdate) {
            if (StringUtils.isNotBlank(userDTO.getCurrentPassword()) && !StringUtils.equals(userDTO.getPassword(), userDTO.getPasswordConfirm())) {
                throw new ApiRequestException("As novas senhas devem ser iguais.");
            }
            if (!StringUtils.isEmpty(userDTO.getPassword())) {
                userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                userDTO.setPasswordChangedAt(Instant.now());
            }

            userDTO.setName(userDTO.getFirstName(), userDTO.getSurname());

            GenericMapper.synchronize(userDTO, u);
        }

        User persistedUser = repository.save(u);

        userTenantRepository.deleteUserTenantsByUsername(u.getUsername());

        for (long location : userDTO.getLocationsIds()) {
            tenantRepository.findById(location)
                    .ifPresent(item -> userTenantRepository.save(new UserTenant(null, userDTO.getUsername(), item.getSlug())));

        }

        return GenericMapper.map(persistedUser, PUserDTO.class);
    }

    @MasterDB
    public void addRoleToUser(String username, String roleName) throws ApiRequestException {
        Optional<User> user = repository.findByUsername(username);
        Optional<Role> role = roleRepository.findByName(roleName);
        if (user.isEmpty() || role.isEmpty()) {
            throw new ApiRequestException(environment.getProperty("notvalid.user"));
        }
        user.get().getRoles().add(role.get());
    }

    @MasterDB
    public List<UserTenant> findTenantsByUsername(String username) {
        return repository.findAllUserTenantsByUsername(username);
    }

    @MasterDB
    public RequestSuccessResponse delete(Long id) throws ApiRequestException {
        Optional<User> role = repository.findById(id);
        if (role.isEmpty()) {
            throw new ApiRequestException(environment.getProperty(NOTFOUND_USER));
        }
        try {
            RequestSuccessResponse resp = new RequestSuccessResponse(
                    String.format("%s ", environment.getProperty("msg.deleted")),
                    HttpStatus.OK);
            repository.deleteById(id);
            return resp;
        } catch (ConstraintViolationException e) {
            throw new ApiRequestException(environment.getProperty("msg.cantdelete"));
        }
    }

    @MasterDB
    public PUserDTO findByUsername(String username) throws ApiRequestException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        User obj = repository
                .findByUsername(username)
                .orElseThrow(() -> new ApiRequestException(environment.getProperty(NOTFOUND_USER)));

        PUserDTO userDTO = GenericMapper.map(obj, PUserDTO.class);

        userDTO.setBusinessLocations(repository.findAllTenantsByUsername(userDTO.getUsername()));

        return userDTO;
    }

    /**
     * Reposicao iniciada por um administrador: gera uma palavra-passe temporaria,
     * limpa passwordChangedAt para forcar a definicao de uma nova no primeiro
     * acesso, e envia-a por email.
     */
    @MasterDB
    public void resetPassword(Long id) {
        User user = repository
                .findById(id)
                .orElseThrow(() -> new ApiRequestException(environment.getProperty(NOTFOUND_USER)));

        String temporaryPassword = Utils.generatePassword(9);
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setPasswordChangedAt(null);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        repository.save(user);

        emailService.send(user.getEmail(), "Palavra-passe temporaria", "temporary-password", Map.of(
                "name", StringUtils.defaultIfBlank(user.getFirstName(), user.getUsername()),
                "companyName", "Detailing Studio",
                "username", user.getUsername(),
                "temporaryPassword", temporaryPassword,
                "loginUrl", frontendBaseUrl + "/auth/login"
        ));
    }

    private List<String> validations(UserDTO dto) {
        List<String> errors = new ArrayList<>();
        if (repository.existsByUsername(dto.getUsername())) errors.add("O utilizador já encontra-se cadastrado.");
        if (repository.existsByEmail(dto.getEmail())) errors.add("O email já encontra-se cadastrado.");
        if (dto.getNuit() != null && repository.existsByNuit(dto.getNuit())) errors.add("O NUIT inserido já existe.");

        return errors;
    }

    @MasterDB
    public String setPassword(PasswordSetDTO creds, Long id, String username) throws ApiRequestException {

        if (!StringUtils.equals(creds.getPassword(), creds.getPasswordConfirm())) {
            throw new ApiRequestException("As senhas devem ser iguais.");
        }

        User obj = repository
                .findById(id)
                .orElseThrow(() -> new ApiRequestException(environment.getProperty(NOTFOUND_USER)));

        if (!StringUtils.equals(obj.getUsername(), username)) {
            throw new ApiRequestException("Nao pode alterar a palavra-passe de outro utilizador.");
        }

        obj.setPassword(passwordEncoder.encode(creds.getPassword()));
        obj.setPasswordChangedAt(Instant.now());
        repository.save(obj);

        return "Senha alterada com sucesso";
    }

    public String generateUsername(String firstName, String surname) {

        String firstInitial = firstName.substring(0, 1).toLowerCase();
        boolean isUnique = false;
        int number = 0;

        String cleanedLastName = surname.replaceAll("\\s", "").toLowerCase();
        String username = firstInitial + cleanedLastName;

        while (!isUnique) {
            Optional<User> u = repository.findByUsername(username);
            if (u.isPresent()) {
                username = username + number;
                number++;
            } else {
                isUnique = true;
            }
        }
        return username;
    }

    public String generateUsername(String name) {

        boolean isUnique = false;
        int number = 0;

        String username = name.replaceAll("\\s", "").toLowerCase();

        while (!isUnique) {
            Optional<User> u = repository.findByUsername(username);
            if (u.isPresent()) {
                username = username + number;
                number++;
            } else {
                isUnique = true;
            }
        }
        return username;
    }

}
