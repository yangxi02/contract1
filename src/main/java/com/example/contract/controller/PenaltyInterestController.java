package com.example.contract.controller;

import com.example.contract.dto.BatchPenaltyCalcRequest;
import com.example.contract.dto.PenaltyCalcRequest;
import com.example.contract.service.PenaltyInterestService;
import com.example.contract.vo.ContractPenaltyVO;
import com.example.contract.vo.PenaltyCalcResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 罚息计算控制器
 */
@Tag(name = "罚息计算", description = "融资租赁合同罚息计算相关接口")
@RestController
@RequestMapping("/api/penalty")
@RequiredArgsConstructor
public class PenaltyInterestController {

    private final PenaltyInterestService penaltyInterestService;

    /**
     * 计算单期罚息
     */
    @Operation(summary = "计算单期罚息", description = "根据还款计划计算单期罚息（不保存）")
    @PostMapping("/calculate")
    public ResponseEntity<PenaltyCalcResultVO> calculatePenalty(@Valid @RequestBody PenaltyCalcRequest request) {
        PenaltyCalcResultVO result = penaltyInterestService.calculatePenalty(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据还款计划ID计算罚息
     */
    @Operation(summary = "根据还款计划ID计算罚息", description = "根据还款计划ID计算罚息（不保存）")
    @GetMapping("/calculate/{scheduleId}")
    public ResponseEntity<PenaltyCalcResultVO> calculatePenaltyByScheduleId(
            @Parameter(description = "还款计划ID") @PathVariable Long scheduleId,
            @Parameter(description = "计算日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate calcDate,
            @Parameter(description = "罚息年利率，如0.24表示24%") @RequestParam(required = false) BigDecimal penaltyRate) {
        PenaltyCalcResultVO result = penaltyInterestService.calculatePenaltyByScheduleId(scheduleId, calcDate, penaltyRate);
        return ResponseEntity.ok(result);
    }

    /**
     * 批量计算合同罚息
     */
    @Operation(summary = "批量计算合同罚息", description = "计算合同下所有逾期期数的罚息（不保存）")
    @PostMapping("/calculate/contract")
    public ResponseEntity<ContractPenaltyVO> calculateContractPenalty(@Valid @RequestBody BatchPenaltyCalcRequest request) {
        ContractPenaltyVO result = penaltyInterestService.calculateContractPenalty(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 计算并更新单期罚息
     */
    @Operation(summary = "计算并更新单期罚息", description = "计算罚息并更新到还款计划表")
    @PostMapping("/calculate-and-update/{scheduleId}")
    public ResponseEntity<PenaltyCalcResultVO> calculateAndUpdatePenalty(
            @Parameter(description = "还款计划ID") @PathVariable Long scheduleId,
            @Parameter(description = "计算日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate calcDate,
            @Parameter(description = "罚息年利率，如0.24表示24%") @RequestParam(required = false) BigDecimal penaltyRate) {
        PenaltyCalcResultVO result = penaltyInterestService.calculateAndUpdatePenalty(scheduleId, calcDate, penaltyRate);
        return ResponseEntity.ok(result);
    }

    /**
     * 批量计算并更新合同罚息
     */
    @Operation(summary = "批量计算并更新合同罚息", description = "批量计算合同下所有逾期期数的罚息并更新")
    @PostMapping("/calculate-and-update/contract/{contractId}")
    public ResponseEntity<List<PenaltyCalcResultVO>> batchCalculateAndUpdatePenalty(
            @Parameter(description = "合同ID") @PathVariable Long contractId,
            @Parameter(description = "计算日期，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate calcDate,
            @Parameter(description = "罚息年利率，如0.24表示24%") @RequestParam(required = false) BigDecimal penaltyRate) {
        List<PenaltyCalcResultVO> results = penaltyInterestService.batchCalculateAndUpdatePenalty(contractId, calcDate, penaltyRate);
        return ResponseEntity.ok(results);
    }
}
