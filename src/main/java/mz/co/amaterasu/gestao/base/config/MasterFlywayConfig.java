package mz.co.amaterasu.gestao.base.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Migra a base de dados master no arranque.
 *
 * O Flyway automatico do Spring Boot esta desligado (spring.flyway.enabled=false)
 * porque o DataSource primario e o TenantRoutingDataSource: deixar o Flyway
 * escolher sozinho faria as migracoes do master correr contra a base de dados
 * do tenant activo. As bases de dados de tenant sao migradas em
 * {@code CompanySetupServiceImpl} no momento do provisionamento.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MasterFlywayConfig {

    @Qualifier("masterDataSource")
    private final DataSource masterDataSource;

    @PostConstruct
    void migrateMaster() {
        log.info("A aplicar migracoes na base de dados master");
        Flyway.configure()
                .dataSource(masterDataSource)
                .locations("classpath:db/migration/master")
                .baselineOnMigrate(true)
                .load()
                .migrate();
    }
}
