public class DepartmentRouter {
    public static String detectDepartment(String description) {
        String desc = safe(description);

        if (containsAny(desc, "payment", "refund", "billing", "invoice", "money", "transaction")) {
            return "Finance";
        }
        if (containsAny(desc, "login", "password", "account", "app", "website", "server", "software")) {
            return "IT Support";
        }
        if (containsAny(desc, "delivery", "order", "package", "shipment", "courier", "late")) {
            return "Logistics";
        }
        if (containsAny(desc, "product", "quality", "damaged", "defective", "broken", "replacement")) {
            return "Product Team";
        }
        return "General Support";
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static String safe(String text) {
        return text == null ? "" : text.toLowerCase();
    }
}
