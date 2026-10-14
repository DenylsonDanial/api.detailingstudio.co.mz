package mz.co.amaterasu.gestao.payment.service;

import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.AllocationPreview;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.PaymentFilter;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.PaymentRequest;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.PaymentResponse;
import mz.co.amaterasu.gestao.payment.dto.PaymentDTOs.ReversalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {

    Page<PaymentResponse> findAll(PaymentFilter filter, Pageable pageable);

    PaymentResponse findById(Long id);

    /** Proposta de distribuicao pelas entradas mais antigas, mostrada antes de guardar. */
    List<AllocationPreview> previewAllocation(Long clientId, BigDecimal amount);

    PaymentResponse create(PaymentRequest request);

    PaymentResponse reverse(Long id, ReversalRequest request);
}
