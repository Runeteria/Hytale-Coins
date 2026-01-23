package com.pandaismyname1.coins.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class MySQLUtil {

    public enum Compare {
        EQUAL, NOTEQUAL, GREATERTHAN, LESSTHAN
    }

    ///////////////////////////
    // SQL Opening selectors //
    ///////////////////////////

    public static String selectFrom(String table) {
        return "SELECT * FROM " + table + " ";
    }

    public static String update(String table) {
        return "UPDATE " + table + " ";
    }

    public static String insertInto(String table) {

        return "INSERT INTO " + table + " ";
    }

    public static String replaceInto(String table) {

        return "REPLACE INTO " + table + " ";
    }

    public static String deleteFrom(String table) {

        return "DELETE FROM " + table + " ";
    }


    ////////////////////////
    // SQL Fields setters //
    ////////////////////////

    public static String withFields(List<String> fields) {
        String data = String.join(",", fields);
        return "(" + data + ")" + " ";
    }

    public static String whereFields(List<String> fields) {
        String data = String.join(",", fields);
        return "WHERE (" + data + ")" + " ";
    }

    public static String withValues(List<Object> fields) {
        StringBuilder data = new StringBuilder();
        for (Object field : fields) {
            data.append(",").append("'").append(field).append("'");
        }
        data = new StringBuilder(data.substring(data.toString().indexOf(',') + 1));
        return "VALUES (" + data + ")" + " ";
    }

    @SuppressWarnings("unused")
    public static String withTokenValues(List<Object> fields) {
        StringBuilder data = new StringBuilder();
        for (Object field : fields) {
            data.append(",").append("?");
        }
        data = new StringBuilder(data.substring(data.toString().indexOf(',') + 1));
        return "VALUES (" + data + ")" + " ";
    }

    public static String withMultiValues(List<List<Object>> valuesList) {
        StringBuilder data = new StringBuilder();
        for (List<Object> values : valuesList) {
            StringBuilder data2 = new StringBuilder();
            for (Object field : values) {
                data2.append(",").append("'").append(field).append("'");
            }
            data2 = new StringBuilder("(" + data2.substring(data2.toString().indexOf(',') + 1) + ")");
            data.append(",").append(data2);
        }

        data = new StringBuilder(data.substring(data.toString().indexOf(',') + 1));
        return "VALUES " + data + " ";
    }

    @SuppressWarnings("unused")
    public static String withMultiTokenValues(List<List<Object>> valuesList) {
        StringBuilder data = new StringBuilder();
        for (List<Object> values : valuesList) {
            StringBuilder data2 = new StringBuilder();
            for (Object field : values) {
                data2.append(",").append("?");
            }
            data2 = new StringBuilder("(" + data2.substring(data2.toString().indexOf(',') + 1) + ")");
            data.append(",").append(data2);
        }

        data = new StringBuilder(data.substring(data.toString().indexOf(',') + 1));
        return "VALUES " + data + " ";
    }

    public static String inMultiValues(List<List<Object>> valuesList) {
        StringBuilder data = new StringBuilder();
        for (List<Object> values : valuesList) {
            StringBuilder data2 = new StringBuilder();
            for (Object field : values) {
                data2.append(",").append("'").append(field).append("'");
            }
            data2 = new StringBuilder("(" + data2.substring(data2.toString().indexOf(',') + 1) + ")");
            data.append(",").append(data2);
        }

        data = new StringBuilder(data.substring(data.toString().indexOf(',') + 1));
        return "IN (" + data + ") ";
    }

    public static String set(String field, Object var) {
        return "SET " + field + " = " + "'" + var + "'" + " ";
    }

    //////////////////////
    // SQL Conditionals //
    //////////////////////

    public static String where(String field, Object var) {
        return "WHERE " + field + " = " + "'" + var + "'" + " ";
    }

    public static String where(String field, Object var, Compare compare) {
        return "WHERE " + field + " " + getCompareString(compare) + " " + "'" + var + "'" + " ";
    }

    public static String whereNULL(String field) {
        return "WHERE " + field + " IS NULL" + " ";
    }

    public static String whereNOTNULL(String field) {
        return "WHERE " + field + " IS NOT NULL" + " ";
    }

    public static String and(String field, Object var) {
        return "AND " + field + " = " + "'" + var + "'" + " ";
    }

    public static String and(String field, Object var, Compare compare) {
        return "AND " + field + " " + getCompareString(compare) + " " + "'" + var + "'" + " ";
    }

    public static String andNULL(String field) {
        return "AND " + field + " IS NULL" + " ";
    }

    public static String andNOTNULL(String field) {
        return "AND " + field + " IS NOT NULL" + " ";
    }

    public static String or(String field, Object var) {
        return "OR " + field + " = " + "'" + var + "'" + " ";
    }

    public static String or(String field, Object var, Compare compare) {
        return "OR " + field + " " + getCompareString(compare) + " " + "'" + var + "'" + " ";
    }

    public static String orNULL(String field) {
        return "OR " + field + " IS NULL" + " ";
    }

    public static String orNOTNULL(String field) {
        return "OR " + field + " IS NOT NULL" + " ";
    }

    public static String in(String field, Object var) {
        if (field instanceof String) {
            return "OR " + field + " = " + "'" + var + "'" + " ";
        } else {
            return "OR " + field + " = " + var + " ";
        }
    }

    ///////////////////////////
    // SQL EXTENSION PHRASES //
    ///////////////////////////

    public static String onDuplicateKeyUpdate(String field, Object var) {
        if (field instanceof String) {
            return "ON DUPLICATE KEY UPDATE " + field + " = " + "'" + var + "'" + " ";
        } else {
            return "ON DUPLICATE KEY UPDATE " + field + " = " + var + " ";
        }

    }

    public static String onDuplicateKeysUpdate(String field) {
        if (field instanceof String) {
            return "ON DUPLICATE KEY UPDATE " + field + " = " + "VALUES" + "(" + /*"'" +*/ field /*+ "'"*/ + ") ";
        } else {
            return "ON DUPLICATE KEY UPDATE " + field + " = " + "VALUES" + "(" + field + ") ";
        }

    }


    /////////////////
    // SQL Closers //
    /////////////////

    public static String end() {
        return ";";
    }


    ///////////////////
    // SQL Tokenizer //
    ///////////////////

    public static void addTokens(PreparedStatement ps, List<Object> values) {
        try {
            int index = 1;
            for (Object value : values) {
                ps.setObject(index, value);
                index++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addMultiTokens(PreparedStatement ps, List<List<Object>> valuesList) {
        try {
            int index = 1;
            for (List<Object> values : valuesList) {
                for (Object value : values) {
                    ps.setObject(index, value);
                    index++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////
    // Private Functions //
    ///////////////////////

    private static String getCompareString(Compare compare) {
        return switch (compare) {
            case EQUAL -> "=";
            case NOTEQUAL -> "!=";
            case GREATERTHAN -> ">";
            case LESSTHAN -> "<";
        };
    }


}
