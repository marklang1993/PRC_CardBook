package com.swws.marklang.prc_cardbook.utility;

public class MathUtility {

    /**
     * Calculate the value of current progress
     * @param currentActual current value of actual value
     * @param minActual minimal value of actual value
     * @param maxActual maximal value of actual value
     * @param minProgress minimal value of the value used by ProgressBar
     * @param maxProgress maximal value of the value used by ProgressBar
     * @return
     */
    public static int calculateCurrentProgressValue(
            int currentActual,
            int minActual,
            int maxActual,
            int minProgress,
            int maxProgress
    ) {
        int rangeActual = maxActual - minActual;
        int deltaActual = currentActual - minActual;
        int rangeProgress = maxProgress - minProgress;

        double percentageActual = (double)deltaActual / (double)rangeActual;

        return (int)(rangeProgress * percentageActual) + minProgress;
    }
}
