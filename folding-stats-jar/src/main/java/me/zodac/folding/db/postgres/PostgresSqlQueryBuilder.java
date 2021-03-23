package me.zodac.folding.db.postgres;

import me.zodac.folding.api.rest.HardwareCategory;

class PostgresSqlQueryBuilder {

    private PostgresSqlQueryBuilder() {

    }

    public static String insertHardwareCategory(final HardwareCategory hardwareCategory) {
        return String.format("INSERT INTO hardware_categories (category_name, multiplier) VALUES ('%s', '%s') RETURNING hardware_id;", hardwareCategory.getCategoryName(), hardwareCategory.getMultiplier());
    }

    public static String getHardwareCategories() {
        return "SELECT * FROM hardware_categories;";
    }

    public static String getHardwareCategory(final String hardwareId) {
        return String.format("SELECT * FROM hardware_categories WHERE hardware_id = '%s';", hardwareId);
    }
}
