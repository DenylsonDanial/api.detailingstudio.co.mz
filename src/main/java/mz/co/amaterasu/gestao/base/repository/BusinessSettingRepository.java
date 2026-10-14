
package mz.co.amaterasu.gestao.base.repository;

import mz.co.amaterasu.gestao.base.entity.BusinessSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessSettingRepository extends JpaRepository<BusinessSetting, Long> {

    Optional<BusinessSetting> findByIsActive(boolean isActive);

}
