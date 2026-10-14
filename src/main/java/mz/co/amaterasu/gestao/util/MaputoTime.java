package mz.co.amaterasu.gestao.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Datas de negocio sao sempre locais de Maputo, independentemente do fuso do servidor. */
public final class MaputoTime {

    public static final ZoneId ZONE = ZoneId.of("Africa/Maputo");

    private MaputoTime() {
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    public static Instant toInstant(LocalDateTime local) {
        return local.atZone(ZONE).toInstant();
    }

    public static LocalDate toLocalDate(Instant instant) {
        return instant.atZone(ZONE).toLocalDate();
    }

    /**
     * Datas de movimento nao podem ser futuras; datas anteriores a hoje sao
     * lancamentos retroactivos, reservados ao gestor (backdate:movements).
     */
    public static void validateMovementDate(LocalDate date) {
        LocalDate today = today();
        if (date.isAfter(today)) {
            throw new mz.co.amaterasu.gestao.exceptions.ApiRequestException("A data do movimento não pode ser futura.");
        }
        if (date.isBefore(today)) {
            SecurityUtils.require("backdate:movements",
                    "Lançamentos com data retroactiva estão reservados ao gestor.");
        }
    }
}
