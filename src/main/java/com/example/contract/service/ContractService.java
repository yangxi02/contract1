package com.example.contract.service;

import com.example.contract.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 合同服务接口
 */
public interface ContractService {

    /**
     * 创建合同
     */
    Contract createContract(Contract contract);

    /**
     * 更新合同
     */
    Contract updateContract(Long id, Contract contract);

    /**
     * 删除合同(逻辑删除)
     */
    void deleteContract(Long id);

    /**
     * 根据ID查询合同
     */
    Optional<Contract> getContractById(Long id);

    /**
     * 根据合同编号查询合同
     */
    Optional<Contract> getContractByNo(String contractNo);

    /**
     * 获取所有合同
     */
    List<Contract> getAllContracts();

    /**
     * 分页查询合同
     */
    Page<Contract> getContractsByPage(Pageable pageable);

    /**
     * 根据项目编号查询合同
     */
    List<Contract> getContractsByProjectNo(String projectNo);

    /**
     * 根据承租人名称查询合同
     */
    List<Contract> getContractsByLesseeName(String lesseeName);

    /**
     * 根据合同状态查询
     */
    List<Contract> getContractsByStatus(Contract.ContractStatus status);

    /**
     * 根据签订日期范围查询
     */
    List<Contract> getContractsBySignDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 更新合同状态
     */
    Contract updateContractStatus(Long id, Contract.ContractStatus status);

    /**
     * 检查合同编号是否存在
     */
    boolean existsByContractNo(String contractNo);

    /**
     * 查询金额大于指定值的合同
     */
    List<Contract> getContractsByAmountGreaterThan(BigDecimal amount);
}
