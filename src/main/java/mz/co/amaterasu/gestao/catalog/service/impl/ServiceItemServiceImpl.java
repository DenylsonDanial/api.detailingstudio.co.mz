package mz.co.amaterasu.gestao.catalog.service.impl;

import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.enums.RecordStatus;
import mz.co.amaterasu.gestao.base.service.ChangeHistoryService;
import mz.co.amaterasu.gestao.catalog.dto.ServiceItemDTOs.CategoryPrice;
import mz.co.amaterasu.gestao.catalog.dto.ServiceItemDTOs.ServiceItemRequest;
import mz.co.amaterasu.gestao.catalog.dto.ServiceItemDTOs.ServiceItemResponse;
import mz.co.amaterasu.gestao.catalog.entity.ServiceItem;
import mz.co.amaterasu.gestao.catalog.entity.ServicePrice;
import mz.co.amaterasu.gestao.catalog.enums.PricingType;
import mz.co.amaterasu.gestao.catalog.repository.ServiceItemRepository;
import mz.co.amaterasu.gestao.catalog.service.ServiceItemService;
import mz.co.amaterasu.gestao.client.enums.VehicleCategory;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import mz.co.amaterasu.gestao.util.Money;
import mz.co.amaterasu.gestao.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ServiceItemServiceImpl implements ServiceItemService {

    private final ServiceItemRepository serviceItemRepository;
    private final ChangeHistoryService changeHistoryService;

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceItemResponse> findAll(Pageable pageable, RecordStatus status, String term) {
        Set<RecordStatus> statuses = status == null ? EnumSet.allOf(RecordStatus.class) : EnumSet.of(status);
        String like = term == null || term.isBlank() ? "%" : "%" + term.trim().toLowerCase() + "%";
        return serviceItemRepository.search(statuses, like, pageable).map(ServiceItemResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceItemResponse findById(Long id) {
        return ServiceItemResponse.from(get(id));
    }

    @Override
    @Transactional
    public ServiceItemResponse create(ServiceItemRequest request) {
        ServiceItem service = new ServiceItem();
        apply(service, request);
        ServiceItem saved = serviceItemRepository.save(service);
        ServiceItemResponse response = ServiceItemResponse.from(saved);
        changeHistoryService.record(ChangeHistoryService.SERVICE, saved.getId(), "CREATE", request.getReason(),
                null, response.priceSummary());
        return response;
    }

    /**
     * Alterar o catalogo nao modifica entradas anteriores: cada linha de entrada
     * guarda o nome e o preco aplicados no momento.
     */
    @Override
    @Transactional
    public ServiceItemResponse update(Long id, ServiceItemRequest request) {
        String reason = SecurityUtils.requireReason(request.getReason(),
                "Indique o motivo da alteração ao catálogo.");
        ServiceItem service = get(id);
        String before = ServiceItemResponse.from(service).priceSummary();
        apply(service, request);
        ServiceItemResponse response = ServiceItemResponse.from(serviceItemRepository.save(service));
        changeHistoryService.record(ChangeHistoryService.SERVICE, id, "UPDATE", reason, before, response.priceSummary());
        return response;
    }

    @Override
    @Transactional
    public ServiceItemResponse setStatus(Long id, RecordStatus status, String reason) {
        ServiceItem service = get(id);
        RecordStatus before = service.getStatus();
        service.setStatus(status);
        changeHistoryService.record(ChangeHistoryService.SERVICE, id,
                status == RecordStatus.ARCHIVED ? "ARCHIVE" : "UNARCHIVE", reason, before, status);
        return ServiceItemResponse.from(serviceItemRepository.save(service));
    }

    private ServiceItem get(Long id) {
        return serviceItemRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Serviço não encontrado."));
    }

    private void apply(ServiceItem service, ServiceItemRequest request) {
        PricingType type = request.getPricingType();
        service.setCode(request.getCode() == null || request.getCode().isBlank() ? null : request.getCode().trim());
        service.setName(request.getName().trim());
        service.setPricingType(type);
        service.setCategory(request.getCategory());
        service.setSortOrder(request.getSortOrder() == null ? service.getSortOrder() : request.getSortOrder());

        if (type == PricingType.FIXED || type == PricingType.HOURLY) {
            if (request.getBasePrice() == null) {
                throw new ApiRequestException(type == PricingType.HOURLY
                        ? "Indique o preço por hora."
                        : "Indique o preço do serviço.");
            }
            service.setBasePrice(Money.of(request.getBasePrice()));
        } else {
            service.setBasePrice(null);
        }

        List<CategoryPrice> prices = type == PricingType.BY_CATEGORY && request.getPrices() != null
                ? request.getPrices()
                : List.of();
        if (type == PricingType.BY_CATEGORY && prices.isEmpty()) {
            throw new ApiRequestException("Indique o preço de pelo menos uma categoria.");
        }
        Map<VehicleCategory, BigDecimal> requested = new EnumMap<>(VehicleCategory.class);
        for (CategoryPrice price : prices) {
            if (requested.put(price.getCategory(), Money.of(price.getPrice())) != null) {
                throw new ApiRequestException("Categoria repetida: " + price.getCategory().getLabel());
            }
        }

        // Actualiza as linhas existentes em vez de as recriar: o Hibernate insere
        // antes de apagar, o que violaria a chave unica (servico, categoria).
        service.getPrices().removeIf(existing -> !requested.containsKey(existing.getCategory()));
        service.getPrices().forEach(existing -> existing.setPrice(requested.remove(existing.getCategory())));
        requested.forEach((category, value) -> {
            ServicePrice entity = new ServicePrice();
            entity.setService(service);
            entity.setCategory(category);
            entity.setPrice(value);
            service.getPrices().add(entity);
        });
    }
}
