package com.example.contract.repository;

import com.example.contract.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 合同数据访问层
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {

    /**
     * 根据合同编号查询合同
     */
    Optional<Contract> findByContractNo(String contractNo);

    /**
     * 根据项目编号查询合同列表
     */
    List<Contract> findByProjectNo(String projectNo);

    /**
     * 根据承租人名称模糊查询
     */
    List<Contract> findByLesseeNameContaining(String lesseeName);

    /**
     * 根据合同状态查询
     */
    List<Contract> findByStatus(Contract.ContractStatus status);

    /**
     * 分页查询未删除的合同
     */
    Page<Contract> findByIsDeletedFalse(Pageable pageable);

    /**
     * 根据合同编号模糊查询
     */
    @Query("SELECT c FROM Contract c WHERE c.contractNo LIKE %:keyword% AND c.isDeleted = false")
    List<Contract> findByContractNoContaining(@Param("keyword") String keyword);

    /**
     * 查询指定日期范围内的合同
     */
    @Query("SELECT c FROM Contract c WHERE c.signDate BETWEEN :startDate AND :endDate AND c.isDeleted = false")
    List<Contract> findBySignDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查询金额大于指定值的合同
     */
    List<Contract> findByContractAmountGreaterThanAndIsDeletedFalse(BigDecimal amount);

    /**
     * 统计指定状态的合同数量
     */
    long countByStatusAndIsDeletedFalse(Contract.ContractStatus status);

    /**
     * 检查合同编号是否存在
     */
    boolean existsByContractNo(String contractNo);
}
