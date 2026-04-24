public class PriorityCalculator {
    private static int learningFactor = 0;

    private static final String[] NEGATIVE_WORDS = {
            "angry", "worst", "bad", "terrible", "hate", "frustrated", "issue", "problem",
            "poor", "disappointed", "complaint", "unhappy"
    };

    private static final String[] CRITICAL_WORDS = {
            "crash", "failed", "error", "not working", "down", "broken", "urgent", "blocked",
            "loss", "security", "fraud", "critical"
    };

    public static int detectSentimentScore(String description) {
        int matches = countMatches(description, NEGATIVE_WORDS);
        if (matches >= 3) {
            return 5;
        }
        if (matches >= 1) {
            return 4;
        }
        return 2;
    }

    public static int detectImpact(String description) {
        int matches = countMatches(description, CRITICAL_WORDS);
        if (matches >= 2) {
            return 5;
        }
        if (matches == 1) {
            return 4;
        }
        return 2;
    }

    public static int calculate(String description) {
        int priority = detectSentimentScore(description) + detectImpact(description) + learningFactor;
        learningFactor = Math.min(2, learningFactor + 1);
        return Math.max(1, Math.min(10, priority));
    }

    public static int calculate(String description, int extraWeight) {
        return Math.max(1, Math.min(10, calculate(description) + extraWeight));
    }

    public static int preview(String description) {
        return Math.max(1, Math.min(10, detectSentimentScore(description) + detectImpact(description)));
    }

    public static String getPriorityLevel(int priority) {
        if (priority >= 9) {
            return "HIGH";
        }
        if (priority >= 6) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private static int countMatches(String description, String[] words) {
        String text = description == null ? "" : description.toLowerCase();
        int score = 0;
        for (String word : words) {
            if (text.contains(word)) {
                score++;
            }
        }
        return score;
    }
}
