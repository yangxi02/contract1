package com.example.contract.service.impl;

import com.example.contract.entity.Contract;
import com.example.contract.repository.ContractRepository;
import com.example.contract.service.ContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 合同服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;

    @Override
    @Transactional
    public Contract createContract(Contract contract) {
        if (contractRepository.existsByContractNo(contract.getContractNo())) {
            throw new RuntimeException("合同编号已存在: " + contract.getContractNo());
        }
        contract.setIsDeleted(false);
        if (contract.getStatus() == null) {
            contract.setStatus(Contract.ContractStatus.DRAFT);
        }
        Contract savedContract = contractRepository.save(contract);
        log.info("创建合同成功, 合同编号: {}", savedContract.getContractNo());
        return savedContract;
    }

    @Override
    @Transactional
    public Contract updateContract(Long id, Contract contract) {
        Contract existingContract = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("合同不存在, ID: " + id));
        
        if (!existingContract.getContractNo().equals(contract.getContractNo()) 
                && contractRepository.existsByContractNo(contract.getContractNo())) {
            throw new RuntimeException("合同编号已存在: " + contract.getContractNo());
        }

        contract.setId(id);
        contract.setCreatedAt(existingContract.getCreatedAt());
        contract.setIsDeleted(existingContract.getIsDeleted());
        
        Contract updatedContract = contractRepository.save(contract);
        log.info("更新合同成功, 合同编号: {}", updatedContract.getContractNo());
        return updatedContract;
    }

    @Override
    @Transactional
    public void deleteContract(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("合同不存在, ID: " + id));
        contract.setIsDeleted(true);
        contractRepository.save(contract);
        log.info("删除合同成功, 合同编号: {}", contract.getContractNo());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Contract> getContractById(Long id) {
        return contractRepository.findById(id)
                .filter(contract -> !Boolean.TRUE.equals(contract.getIsDeleted()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Contract> getContractByNo(String contractNo) {
        return contractRepository.findByContractNo(contractNo)
                .filter(contract -> !Boolean.TRUE.equals(contract.getIsDeleted()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getAllContracts() {
        return contractRepository.findAll().stream()
                .filter(contract -> !Boolean.TRUE.equals(contract.getIsDeleted()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Contract> getContractsByPage(Pageable pageable) {
        return contractRepository.findByIsDeletedFalse(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getContractsByProjectNo(String projectNo) {
        return contractRepository.findByProjectNo(projectNo).stream()
                .filter(contract -> !Boolean.TRUE.equals(contract.getIsDeleted()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getContractsByLesseeName(String lesseeName) {
        return contractRepository.findByLesseeNameContaining(lesseeName).stream()
                .filter(contract -> !Boolean.TRUE.equals(contract.getIsDeleted()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getContractsByStatus(Contract.ContractStatus status) {
        return contractRepository.findByStatus(status).stream()
                .filter(contract -> !Boolean.TRUE.equals(contract.getIsDeleted()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getContractsBySignDateRange(LocalDate startDate, LocalDate endDate) {
        return contractRepository.findBySignDateBetween(startDate, endDate);
    }

    @Override
    @Transactional
    public Contract updateContractStatus(Long id, Contract.ContractStatus status) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("合同不存在, ID: " + id));
        contract.setStatus(status);
        Contract updatedContract = contractRepository.save(contract);
        log.info("更新合同状态成功, 合同编号: {}, 新状态: {}", updatedContract.getContractNo(), status);
        return updatedContract;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByContractNo(String contractNo) {
        return contractRepository.existsByContractNo(contractNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getContractsByAmountGreaterThan(BigDecimal amount) {
        return contractRepository.findByContractAmountGreaterThanAndIsDeletedFalse(amount);
    }
}
