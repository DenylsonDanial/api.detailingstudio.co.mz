package mz.co.amaterasu.gestao.base.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.amaterasu.gestao.user.entity.Role;
import mz.co.amaterasu.gestao.user.entity.User;
import mz.co.amaterasu.gestao.user.enums.UserStatus;
import mz.co.amaterasu.gestao.user.repository.RoleRepository;
import mz.co.amaterasu.gestao.user.repository.UserRepository;
import mz.co.amaterasu.gestao.util.TenantContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/**
 * Cria o primeiro super administrador a partir de variaveis de ambiente, para
 * que nenhuma credencial fique em controlo de versoes.
 *
 * Definir ADMIN_BOOTSTRAP_USERNAME, ADMIN_BOOTSTRAP_EMAIL e
 * ADMIN_BOOTSTRAP_PASSWORD apenas no primeiro arranque; depois de o utilizador
 * existir, este runner nao faz nada.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String username = System.getenv("ADMIN_BOOTSTRAP_USERNAME");
        String email = System.getenv("ADMIN_BOOTSTRAP_EMAIL");
        String password = System.getenv("ADMIN_BOOTSTRAP_PASSWORD");

        if (StringUtils.isAnyBlank(username, email, password)) {
            return;
        }

        TenantContext.setMasterTenant();
        try {
            if (userRepository.existsByUsername(username)) {
                log.info("Utilizador administrador '{}' ja existe, bootstrap ignorado", username);
                return;
            }

            Optional<Role> superAdmin = roleRepository.findByName("SA");
            if (superAdmin.isEmpty()) {
                log.error("Perfil SA nao encontrado. As migracoes do master correram?");
                return;
            }

            // Referencia gerida: CascadeType.PERSIST no User.roles falha com Role detached.
            Role saRole = roleRepository.getReferenceById(superAdmin.get().getId());

            User admin = new User();
            admin.setUsername(username);
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setName("Administrador do Sistema");
            admin.setFirstName("Administrador");
            admin.setSurname("Sistema");
            admin.setUserStatus(UserStatus.ACTIVE);
            admin.setCreatedAt(Instant.now());
            admin.setRoles(Set.of(saRole));
            userRepository.save(admin);

            log.warn("Super administrador '{}' criado. Altere a palavra-passe no primeiro acesso "
                    + "e remova as variaveis ADMIN_BOOTSTRAP_* do ambiente.", username);
        } finally {
            TenantContext.clear();
        }
    }
}
