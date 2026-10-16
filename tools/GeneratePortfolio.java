import java.util.Random;

/**
 * Writes a synthetic applicant portfolio. Values are random and do not describe real people.
 * Usage: java tools/GeneratePortfolio.java 250 > src/main/resources/portfolio/synthetic-portfolio.csv
 */
public class GeneratePortfolio {
    public static void main(String[] args) {
        int n = args.length > 0 ? Integer.parseInt(args[0]) : 250;
        Random r = new Random(2026);
        System.out.println("applicant_id,credit_score,annual_income_minor,monthly_debt_minor,requested_amount_minor,employment_months,open_delinquencies,bankruptcy_last_7_years");
        for (int i = 1; i <= n; i++) {
            int score = 520 + r.nextInt(310);
            long income = (25_000 + r.nextInt(175_000)) * 100L;
            long monthlyDebt = (long) (income / 12.0 * (0.05 + r.nextDouble() * 0.55));
            long requested = (1_000 + r.nextInt(40_000)) * 100L;
            int months = r.nextInt(180);
            int delinquencies = r.nextDouble() < 0.8 ? 0 : 1 + r.nextInt(4);
            boolean bankruptcy = r.nextDouble() < 0.04;
            System.out.printf("APP-%05d,%d,%d,%d,%d,%d,%d,%b%n", i, score, income, monthlyDebt, requested, months, delinquencies, bankruptcy);
        }
    }
}
