package kurs.service;

import kurs.model.ImportStatus;
import kurs.repository.ImportStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImportStatusFacade {

    private final ImportStatusRepository importStatusRepository;

    public ImportStatus saveAndFlush(ImportStatus importStatus) {
        return importStatusRepository.saveAndFlush(importStatus);
    }

    public Optional<ImportStatus> findById(int id) {
        return importStatusRepository.findById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateToProcessing(int id) {
        ImportStatus toUpdate = importStatusRepository.findById(id).orElseThrow();
        toUpdate.setStartDate(LocalDateTime.now());
        toUpdate.setStatus(ImportStatus.Status.PROCESSING);
        importStatusRepository.saveAndFlush(toUpdate);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateProgress(int id, int progress) {
        ImportStatus toUpdate = importStatusRepository.findById(id).orElseThrow();
        toUpdate.setProcessed(progress);
        importStatusRepository.saveAndFlush(toUpdate);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateToSuccess(int id) {
        ImportStatus toUpdate = importStatusRepository.findById(id).orElseThrow();
        toUpdate.setFinishDate(LocalDateTime.now());
        toUpdate.setStatus(ImportStatus.Status.SUCCESS);
        importStatusRepository.saveAndFlush(toUpdate);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateToFail(int id, String message) {
        ImportStatus toUpdate = importStatusRepository.findById(id).orElseThrow();
        toUpdate.setFinishDate(LocalDateTime.now());
        toUpdate.setStatus(ImportStatus.Status.FAILED);
        toUpdate.setFailedReason(message);
        importStatusRepository.saveAndFlush(toUpdate);
    }
}