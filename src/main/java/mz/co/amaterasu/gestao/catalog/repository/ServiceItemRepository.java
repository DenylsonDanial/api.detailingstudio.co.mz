package mz.co.amaterasu.gestao.catalog.repository;

import mz.co.amaterasu.gestao.base.enums.RecordStatus;
import mz.co.amaterasu.gestao.catalog.entity.ServiceItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

    @Query("select s from ServiceItem s where s.status in :statuses " +
            "and (lower(s.name) like :term or lower(coalesce(s.code, '')) like :term)")
    Page<ServiceItem> search(@Param("statuses") Collection<RecordStatus> statuses,
                             @Param("term") String term,
                             Pageable pageable);
}
