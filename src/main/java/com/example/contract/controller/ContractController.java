package com.example.contract.controller;

import com.example.contract.entity.Contract;
import com.example.contract.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 合同管理控制器
 */
@Tag(name = "合同管理", description = "融资租赁合同相关接口")
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    /**
     * 创建合同
     */
    @Operation(summary = "创建合同", description = "创建新的融资租赁合同")
    @PostMapping
    public ResponseEntity<Contract> createContract(@Valid @RequestBody Contract contract) {
        Contract createdContract = contractService.createContract(contract);
        return new ResponseEntity<>(createdContract, HttpStatus.CREATED);
    }

    /**
     * 更新合同
     */
    @Operation(summary = "更新合同", description = "根据ID更新合同信息")
    @PutMapping("/{id}")
    public ResponseEntity<Contract> updateContract(
            @Parameter(description = "合同ID") @PathVariable Long id,
            @Valid @RequestBody Contract contract) {
        Contract updatedContract = contractService.updateContract(id, contract);
        return ResponseEntity.ok(updatedContract);
    }

    /**
     * 删除合同
     */
    @Operation(summary = "删除合同", description = "根据ID删除合同(逻辑删除)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContract(
            @Parameter(description = "合同ID") @PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据ID查询合同
     */
    @Operation(summary = "查询合同详情", description = "根据ID查询合同详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<Contract> getContractById(
            @Parameter(description = "合同ID") @PathVariable Long id) {
        return contractService.getContractById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据合同编号查询
     */
    @Operation(summary = "根据合同编号查询", description = "根据合同编号查询合同")
    @GetMapping("/by-no/{contractNo}")
    public ResponseEntity<Contract> getContractByNo(
            @Parameter(description = "合同编号") @PathVariable String contractNo) {
        return contractService.getContractByNo(contractNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取所有合同
     */
    @Operation(summary = "获取所有合同", description = "获取所有未删除的合同列表")
    @GetMapping
    public ResponseEntity<List<Contract>> getAllContracts() {
        List<Contract> contracts = contractService.getAllContracts();
        return ResponseEntity.ok(contracts);
    }

    /**
     * 分页查询合同
     */
    @Operation(summary = "分页查询合同", description = "分页查询合同列表")
    @GetMapping("/page")
    public ResponseEntity<Page<Contract>> getContractsByPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Contract> contracts = contractService.getContractsByPage(pageable);
        return ResponseEntity.ok(contracts);
    }

    /**
     * 根据项目编号查询
     */
    @Operation(summary = "根据项目编号查询", description = "根据项目编号查询相关合同")
    @GetMapping("/by-project/{projectNo}")
    public ResponseEntity<List<Contract>> getContractsByProjectNo(
            @Parameter(description = "项目编号") @PathVariable String projectNo) {
        List<Contract> contracts = contractService.getContractsByProjectNo(projectNo);
        return ResponseEntity.ok(contracts);
    }

    /**
     * 根据承租人名称查询
     */
    @Operation(summary = "根据承租人查询", description = "根据承租人名称模糊查询合同")
    @GetMapping("/by-lessee")
    public ResponseEntity<List<Contract>> getContractsByLesseeName(
            @Parameter(description = "承租人名称") @RequestParam String lesseeName) {
        List<Contract> contracts = contractService.getContractsByLesseeName(lesseeName);
        return ResponseEntity.ok(contracts);
    }

    /**
     * 根据合同状态查询
     */
    @Operation(summary = "根据状态查询", description = "根据合同状态查询合同")
    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<Contract>> getContractsByStatus(
            @Parameter(description = "合同状态") @PathVariable Contract.ContractStatus status) {
        List<Contract> contracts = contractService.getContractsByStatus(status);
        return ResponseEntity.ok(contracts);
    }

    /**
     * 根据签订日期范围查询
     */
    @Operation(summary = "根据签订日期查询", description = "根据签订日期范围查询合同")
    @GetMapping("/by-date-range")
    public ResponseEntity<List<Contract>> getContractsBySignDateRange(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Contract> contracts = contractService.getContractsBySignDateRange(startDate, endDate);
        return ResponseEntity.ok(contracts);
    }

    /**
     * 更新合同状态
     */
    @Operation(summary = "更新合同状态", description = "更新合同的状态")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Contract> updateContractStatus(
            @Parameter(description = "合同ID") @PathVariable Long id,
            @Parameter(description = "新状态") @RequestParam Contract.ContractStatus status) {
        Contract updatedContract = contractService.updateContractStatus(id, status);
        return ResponseEntity.ok(updatedContract);
    }

    /**
     * 检查合同编号是否存在
     */
    @Operation(summary = "检查合同编号", description = "检查合同编号是否已存在")
    @GetMapping("/check-no")
    public ResponseEntity<Map<String, Boolean>> checkContractNo(
            @Parameter(description = "合同编号") @RequestParam String contractNo) {
        boolean exists = contractService.existsByContractNo(contractNo);
        Map<String, Boolean> result = new HashMap<>();
        result.put("exists", exists);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询大额合同
     */
    @Operation(summary = "查询大额合同", description = "查询金额大于指定值的合同")
    @GetMapping("/by-amount")
    public ResponseEntity<List<Contract>> getContractsByAmountGreaterThan(
            @Parameter(description = "金额阈值") @RequestParam BigDecimal amount) {
        List<Contract> contracts = contractService.getContractsByAmountGreaterThan(amount);
        return ResponseEntity.ok(contracts);
    }
}
