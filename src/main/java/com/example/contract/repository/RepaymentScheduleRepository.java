package com.example.contract.repository;

import com.example.contract.entity.RepaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 还款计划表Repository
 */
@Repository
public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {

    List<RepaymentSchedule> findByContractNo(String contractNo);

    List<RepaymentSchedule> findByContractId(Long contractId);

    List<RepaymentSchedule> findByStatus(RepaymentSchedule.RepaymentStatus status);

    List<RepaymentSchedule> findByContractNoAndStatus(String contractNo, RepaymentSchedule.RepaymentStatus status);
}
