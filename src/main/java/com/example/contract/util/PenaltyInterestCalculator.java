package com.example.contract.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 罚息计算工具类
 * 用于计算融资租赁合同的逾期罚息
 *
 * 计算逻辑：当期剩余租金 × 罚息年利率 ÷ 365 = 当天罚息
 */
public class PenaltyInterestCalculator {

    /**
     * 默认罚息年利率 24%
     */
    public static final BigDecimal DEFAULT_PENALTY_RATE = new BigDecimal("0.24");

    /**
     * 一年天数（按365天计算）
     */
    public static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");

    /**
     * 金额精度
     */
    public static final int SCALE = 2;

    /**
     * 计算单日罚息
     *
     * @param remainingRent 当期剩余租金（未还金额）
     * @param penaltyRate   罚息年利率（如 0.24 表示 24%）
     * @return 当天罚息金额
     */
    public static BigDecimal calculateDailyPenalty(BigDecimal remainingRent, BigDecimal penaltyRate) {
        if (remainingRent == null || remainingRent.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (penaltyRate == null || penaltyRate.compareTo(BigDecimal.ZERO) <= 0) {
            penaltyRate = DEFAULT_PENALTY_RATE;
        }

        // 当天罚息 = 当期剩余租金 × 罚息年利率 ÷ 365
        return remainingRent
                .multiply(penaltyRate)
                .divide(DAYS_IN_YEAR, SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 计算单日罚息（使用默认罚息利率 24%）
     *
     * @param remainingRent 当期剩余租金（未还金额）
     * @return 当天罚息金额
     */
    public static BigDecimal calculateDailyPenalty(BigDecimal remainingRent) {
        return calculateDailyPenalty(remainingRent, DEFAULT_PENALTY_RATE);
    }

    /**
     * 计算累计罚息
     *
     * @param remainingRent   当期剩余租金（未还金额）
     * @param penaltyRate     罚息年利率
     * @param overdueDays     逾期天数
     * @return 累计罚息金额
     */
    public static BigDecimal calculateTotalPenalty(BigDecimal remainingRent, BigDecimal penaltyRate, int overdueDays) {
        if (overdueDays <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal dailyPenalty = calculateDailyPenalty(remainingRent, penaltyRate);
        return dailyPenalty.multiply(BigDecimal.valueOf(overdueDays));
    }

    /**
     * 计算累计罚息（使用默认罚息利率 24%）
     *
     * @param remainingRent 当期剩余租金（未还金额）
     * @param overdueDays   逾期天数
     * @return 累计罚息金额
     */
    public static BigDecimal calculateTotalPenalty(BigDecimal remainingRent, int overdueDays) {
        return calculateTotalPenalty(remainingRent, DEFAULT_PENALTY_RATE, overdueDays);
    }

    /**
     * 计算两个日期之间的天数差
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 天数差
     */
    public static int calculateDaysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 宽限期天数（逾期前3天逾期天数显示为0，但罚息正常计算）
     */
    public static final int GRACE_PERIOD_DAYS = 3;

    /**
     * 计算逾期天数（用于展示）
     * 逻辑：
     * 1. 还款计划日晚上12点没还，就算逾期1天
     * 2. 逾期前3天为宽限期，逾期天数显示为0
     * 3. 从第4天开始显示逾期天数
     * 注意：逾期天数显示为0时，罚息仍正常计算
     *
     * @param planRepayDate 计划还款日期
     * @param calcDate      计算日期
     * @return 逾期天数（宽限期内返回0，但实际罚息仍计算）
     */
    public static int calculateOverdueDays(LocalDate planRepayDate, LocalDate calcDate) {
        if (planRepayDate == null || calcDate == null) {
            return 0;
        }
        // 逾期从计划还款日期的第二天开始计算（晚上12点没还就算逾期1天）
        LocalDate overdueStartDate = planRepayDate.plusDays(1);
        if (calcDate.isBefore(overdueStartDate)) {
            return 0;
        }

        // 计算原始逾期天数
        int rawOverdueDays = (int) ChronoUnit.DAYS.between(overdueStartDate, calcDate) + 1;

        // 宽限期内（前3天）逾期天数显示为0
        if (rawOverdueDays <= GRACE_PERIOD_DAYS) {
            return 0;
        }

        // 超过宽限期后，显示实际逾期天数
        return rawOverdueDays;
    }

    /**
     * 计算实际逾期天数（用于罚息计算）
     * 从逾期第一天就开始计算，包含宽限期
     *
     * @param planRepayDate 计划还款日期
     * @param calcDate      计算日期
     * @return 实际逾期天数（用于计算罚息）
     */
    public static int calculateActualOverdueDays(LocalDate planRepayDate, LocalDate calcDate) {
        if (planRepayDate == null || calcDate == null) {
            return 0;
        }
        LocalDate overdueStartDate = planRepayDate.plusDays(1);
        if (calcDate.isBefore(overdueStartDate)) {
            return 0;
        }
        // 从逾期第一天开始计算，包含宽限期
        return (int) ChronoUnit.DAYS.between(overdueStartDate, calcDate) + 1;
    }

    /**
     * 计算原始逾期天数（包含宽限期）
     * 用于展示总逾期天数（包含宽限期）
     *
     * @param planRepayDate 计划还款日期
     * @param calcDate      计算日期
     * @return 原始逾期天数（包含宽限期）
     */
    public static int calculateRawOverdueDays(LocalDate planRepayDate, LocalDate calcDate) {
        if (planRepayDate == null || calcDate == null) {
            return 0;
        }
        LocalDate overdueStartDate = planRepayDate.plusDays(1);
        if (calcDate.isBefore(overdueStartDate)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(overdueStartDate, calcDate) + 1;
    }

    /**
     * 是否在宽限期内
     *
     * @param planRepayDate 计划还款日期
     * @param calcDate      计算日期
     * @return true-在宽限期内，false-已过宽限期
     */
    public static boolean isInGracePeriod(LocalDate planRepayDate, LocalDate calcDate) {
        return calculateOverdueDays(planRepayDate, calcDate) == 0
                && calculateRawOverdueDays(planRepayDate, calcDate) > 0;
    }

    /**
     * 计算指定日期的罚息
     * 适用于按日计算罚息的场景
     * 注意：宽限期（前3天）内逾期天数显示为0，但罚息正常计算
     *
     * @param remainingRent   当期剩余租金
     * @param penaltyRate     罚息年利率
     * @param planRepayDate   计划还款日期
     * @param calcDate        计算日期
     * @param lastCalcDate    上次计算罚息的日期（用于计算新增罚息天数）
     * @return 计算结果对象，包含新增罚息、累计罚息等信息
     */
    public static PenaltyCalcResult calculatePenalty(
            BigDecimal remainingRent,
            BigDecimal penaltyRate,
            LocalDate planRepayDate,
            LocalDate calcDate,
            LocalDate lastCalcDate) {

        PenaltyCalcResult result = new PenaltyCalcResult();

        // 计算用于展示的逾期天数（宽限期内显示为0）
        int displayOverdueDays = calculateOverdueDays(planRepayDate, calcDate);
        result.setTotalOverdueDays(displayOverdueDays);

        // 计算实际逾期天数（用于罚息计算，包含宽限期）
        int actualOverdueDays = calculateActualOverdueDays(planRepayDate, calcDate);
        result.setActualOverdueDays(actualOverdueDays);

        // 计算原始逾期天数
        int rawOverdueDays = calculateRawOverdueDays(planRepayDate, calcDate);
        result.setRawOverdueDays(rawOverdueDays);

        // 正常还款，无罚息
        if (actualOverdueDays <= 0) {
            result.setDailyPenalty(BigDecimal.ZERO);
            result.setNewPenalty(BigDecimal.ZERO);
            result.setTotalPenalty(BigDecimal.ZERO);
            result.setInGracePeriod(false);
            return result;
        }

        // 计算当天罚息
        BigDecimal dailyPenalty = calculateDailyPenalty(remainingRent, penaltyRate);
        result.setDailyPenalty(dailyPenalty);

        // 计算累计罚息（按实际逾期天数计算，包含宽限期）
        BigDecimal totalPenalty = calculateTotalPenalty(remainingRent, penaltyRate, actualOverdueDays);
        result.setTotalPenalty(totalPenalty);

        // 计算新增罚息（从上一次计算日期到当前计算日期）
        if (lastCalcDate != null && lastCalcDate.isBefore(calcDate)) {
            // 计算上次实际逾期天数
            int lastActualDays = calculateActualOverdueDays(planRepayDate, lastCalcDate);
            // 新增实际逾期天数
            int newActualDays = actualOverdueDays - lastActualDays;

            BigDecimal newPenalty = dailyPenalty.multiply(BigDecimal.valueOf(newActualDays));
            result.setNewPenalty(newPenalty);
            result.setNewOverdueDays(newActualDays);
        } else {
            result.setNewPenalty(totalPenalty);
            result.setNewOverdueDays(actualOverdueDays);
        }

        // 判断是否在宽限期内（逾期天数显示为0但实际有逾期）
        result.setInGracePeriod(displayOverdueDays == 0 && actualOverdueDays > 0);
        return result;
    }

    /**
     * 罚息计算结果
     */
    public static class PenaltyCalcResult {
        /**
         * 当天罚息金额
         */
        private BigDecimal dailyPenalty;

        /**
         * 新增罚息金额（从上一次计算到本次计算的罚息）
         */
        private BigDecimal newPenalty;

        /**
         * 累计罚息金额
         */
        private BigDecimal totalPenalty;

        /**
         * 展示用的逾期天数（宽限期内显示为0）
         */
        private int totalOverdueDays;

        /**
         * 实际逾期天数（用于计算罚息，包含宽限期）
         */
        private int actualOverdueDays;

        /**
         * 原始逾期天数（包含宽限期）
         */
        private int rawOverdueDays;

        /**
         * 新增逾期天数
         */
        private int newOverdueDays;

        /**
         * 是否在宽限期内
         */
        private boolean inGracePeriod;

        public BigDecimal getDailyPenalty() {
            return dailyPenalty;
        }

        public void setDailyPenalty(BigDecimal dailyPenalty) {
            this.dailyPenalty = dailyPenalty;
        }

        public BigDecimal getNewPenalty() {
            return newPenalty;
        }

        public void setNewPenalty(BigDecimal newPenalty) {
            this.newPenalty = newPenalty;
        }

        public BigDecimal getTotalPenalty() {
            return totalPenalty;
        }

        public void setTotalPenalty(BigDecimal totalPenalty) {
            this.totalPenalty = totalPenalty;
        }

        public int getTotalOverdueDays() {
            return totalOverdueDays;
        }

        public void setTotalOverdueDays(int totalOverdueDays) {
            this.totalOverdueDays = totalOverdueDays;
        }

        public int getActualOverdueDays() {
            return actualOverdueDays;
        }

        public void setActualOverdueDays(int actualOverdueDays) {
            this.actualOverdueDays = actualOverdueDays;
        }

        public int getRawOverdueDays() {
            return rawOverdueDays;
        }

        public void setRawOverdueDays(int rawOverdueDays) {
            this.rawOverdueDays = rawOverdueDays;
        }

        public int getNewOverdueDays() {
            return newOverdueDays;
        }

        public void setNewOverdueDays(int newOverdueDays) {
            this.newOverdueDays = newOverdueDays;
        }

        public boolean isInGracePeriod() {
            return inGracePeriod;
        }

        public void setInGracePeriod(boolean inGracePeriod) {
            this.inGracePeriod = inGracePeriod;
        }
    }
}
