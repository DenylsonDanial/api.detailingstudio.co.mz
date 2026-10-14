package mz.co.amaterasu.gestao.base.resource;

import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.entity.Currency;
import mz.co.amaterasu.gestao.base.service.BusinessSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Listas de referencia usadas pelos formularios do frontend. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/common")
public class CommonController {

    private final BusinessSettingService businessSettingService;

    @GetMapping("/listCurrencies")
    public ResponseEntity<List<Currency>> listCurrencies() {
        return ResponseEntity.ok(businessSettingService.listCurrencies());
    }
}
