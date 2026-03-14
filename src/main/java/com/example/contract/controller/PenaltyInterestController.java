package com.example.contract.controller;

import com.example.contract.entity.RepaymentSchedule;
import com.example.contract.service.PenaltyInterestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
 * 罚息管理控制器
 * 
 * 逾期规则：
 * 1. 还款日当天晚上12点没还，算逾期1天
 * 2. 逾期前3天为宽限期，罚息为0
 * 3. 逾期第4天开始计算罚息，罚息按全部逾期天数计算（包括前3天）
 */
@Tag(name = "罚息管理", description = "罚息计算相关接口（逾期前3天为宽限期罚息为0，第4天起按全部逾期天数计算罚息）")
@RestController
@RequestMapping("/api/penalty-interest")
@RequiredArgsConstructor
public class PenaltyInterestController {

    private final PenaltyInterestService penaltyInterestService;

    @Operation(summary = "计算单期罚息", description = "根据剩余租金计算当天的罚息")
    @GetMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateDailyPenalty(
            @Parameter(description = "当期剩余租金", required = true) @RequestParam BigDecimal remainingRent) {
        BigDecimal dailyPenalty = penaltyInterestService.calculateDailyPenaltyInterest(remainingRent);
        
        Map<String, Object> result = new HashMap<>();
        result.put("remainingRent", remainingRent);
        result.put("penaltyRate", PenaltyInterestService.PENALTY_RATE);
        result.put("dailyPenalty", dailyPenalty);
        
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "计算逾期罚息", description = "根据剩余租金和逾期天数计算罚息（逾期前3天为宽限期罚息为0，第4天起按全部逾期天数计算罚息）")
    @GetMapping("/calculate-overdue")
    public ResponseEntity<Map<String, Object>> calculateOverduePenalty(
            @Parameter(description = "当期剩余租金", required = true) @RequestParam BigDecimal remainingRent,
            @Parameter(description = "实际逾期天数", required = true) @RequestParam Integer overdueDays) {
        BigDecimal penaltyInterest = penaltyInterestService.calculatePenaltyInterest(remainingRent, overdueDays);
        int effectiveOverdueDays = penaltyInterestService.calculateEffectiveOverdueDays(overdueDays);
        
        Map<String, Object> result = new HashMap<>();
        result.put("remainingRent", remainingRent);
        result.put("actualOverdueDays", overdueDays);
        result.put("gracePeriodDays", PenaltyInterestService.GRACE_PERIOD_DAYS);
        result.put("effectiveOverdueDays", effectiveOverdueDays);
        result.put("penaltyRate", PenaltyInterestService.PENALTY_RATE);
        result.put("penaltyInterest", penaltyInterest);
        result.put("inGracePeriod", overdueDays <= PenaltyInterestService.GRACE_PERIOD_DAYS);
        
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "计算并更新单期罚息", description = "根据还款计划ID计算并更新罚息")
    @PostMapping("/schedule/{scheduleId}")
    public ResponseEntity<RepaymentSchedule> calculateAndUpdateSchedulePenalty(
            @Parameter(description = "还款计划ID", required = true) @PathVariable Long scheduleId,
            @Parameter(description = "计算日期") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate calculateDate) {
        RepaymentSchedule schedule = penaltyInterestService.calculateAndUpdatePenaltyInterest(scheduleId, calculateDate);
        return ResponseEntity.ok(schedule);
    }

    @Operation(summary = "批量计算合同罚息", description = "根据合同编号计算所有还款计划的罚息")
    @PostMapping("/contract/{contractNo}")
    public ResponseEntity<List<RepaymentSchedule>> calculateContractPenalty(
            @Parameter(description = "合同编号", required = true) @PathVariable String contractNo,
            @Parameter(description = "计算日期") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate calculateDate) {
        List<RepaymentSchedule> schedules = penaltyInterestService.calculateAllOverduePenaltyInterest(contractNo, calculateDate);
        return ResponseEntity.ok(schedules);
    }

    @Operation(summary = "查询合同总罚息", description = "查询合同下所有还款计划的总罚息金额")
    @GetMapping("/contract/{contractNo}/total")
    public ResponseEntity<Map<String, Object>> getContractTotalPenalty(
            @Parameter(description = "合同编号", required = true) @PathVariable String contractNo,
            @Parameter(description = "计算日期") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate calculateDate) {
        BigDecimal totalPenalty = penaltyInterestService.calculateContractTotalPenaltyInterest(contractNo, calculateDate);
        
        Map<String, Object> result = new HashMap<>();
        result.put("contractNo", contractNo);
        result.put("calculateDate", calculateDate != null ? calculateDate : LocalDate.now());
        result.put("totalPenaltyInterest", totalPenalty);
        
        return ResponseEntity.ok(result);
    }
}
