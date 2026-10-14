package mz.co.amaterasu.gestao.base.service.impl;

import lombok.RequiredArgsConstructor;
import mz.co.amaterasu.gestao.base.entity.DocumentSequence;
import mz.co.amaterasu.gestao.base.repository.DocumentSequenceRepository;
import mz.co.amaterasu.gestao.base.service.DocumentSequenceService;
import mz.co.amaterasu.gestao.exceptions.ApiRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class DocumentSequenceServiceImpl implements DocumentSequenceService {

    private static final ZoneId ZONE = ZoneId.of("Africa/Maputo");

    private final DocumentSequenceRepository documentSequenceRepository;

    @Override
    @Transactional
    public String nextNumber(String documentType) {
        if (documentType == null || documentType.isBlank()) {
            throw new ApiRequestException("O tipo de documento é obrigatório");
        }
        String type = documentType.trim().toUpperCase();
        int year = Year.now(ZONE).getValue();
        DocumentSequence sequence = lockOrCreate(type, year);
        int next = sequence.getLastNumber() + 1;
        sequence.setLastNumber(next);
        documentSequenceRepository.save(sequence);

        int padding = sequence.getPadding() != null && sequence.getPadding() > 0
                ? sequence.getPadding()
                : 4;
        return String.format("%s-%d-%0" + padding + "d", sequence.getPrefix(), year, next);
    }

    private DocumentSequence lockOrCreate(String documentType, int year) {
        return documentSequenceRepository.findByDocumentTypeAndYear(documentType, year)
                .orElseGet(() -> createForYear(documentType, year));
    }

    private DocumentSequence createForYear(String documentType, int year) {
        DocumentSequence template = documentSequenceRepository
                .findFirstByDocumentTypeOrderByYearDesc(documentType)
                .orElseThrow(() -> new ApiRequestException(
                        "Sequência de documento não configurada: " + documentType));
        try {
            DocumentSequence created = DocumentSequence.builder()
                    .documentType(documentType)
                    .prefix(template.getPrefix())
                    .year(year)
                    .lastNumber(0)
                    .padding(template.getPadding() != null ? template.getPadding() : 4)
                    .build();
            return documentSequenceRepository.saveAndFlush(created);
        } catch (DataIntegrityViolationException ex) {
            return documentSequenceRepository.findByDocumentTypeAndYear(documentType, year)
                    .orElseThrow(() -> new ApiRequestException(
                            "Não foi possível atribuir o número do documento"));
        }
    }
}
