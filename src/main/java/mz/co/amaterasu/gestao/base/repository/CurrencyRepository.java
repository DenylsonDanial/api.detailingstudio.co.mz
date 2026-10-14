package mz.co.amaterasu.gestao.base.repository;

import mz.co.amaterasu.gestao.base.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
}
