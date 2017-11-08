package com.android.gallery3d.common;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.android.gallery3d.common.Entry.Column;
import com.android.gallery3d.common.Entry.Table;
import java.lang.reflect.Field;
import java.util.ArrayList;

public final class EntrySchema {
    private static final String[] SQLITE_TYPES = new String[]{"TEXT", "INTEGER", "INTEGER", "INTEGER", "INTEGER", "REAL", "REAL", "NONE"};
    private final ColumnInfo[] mColumnInfo;
    private final boolean mHasFullTextIndex;
    private final String[] mProjection;
    private final String mTableName;

    public static final class ColumnInfo {
        public final String defaultValue;
        public final Field field;
        public final boolean fullText;
        public final boolean indexed;
        public final String name;
        public final int projectionIndex;
        public final int type;
        public final boolean unique;

        public ColumnInfo(String name, int type, boolean indexed, boolean unique, boolean fullText, String defaultValue, Field field, int projectionIndex) {
            this.name = name.toLowerCase();
            this.type = type;
            this.indexed = indexed;
            this.unique = unique;
            this.fullText = fullText;
            this.defaultValue = defaultValue;
            this.field = field;
            this.projectionIndex = projectionIndex;
            field.setAccessible(true);
        }

        public boolean isId() {
            return "_id".equals(this.name);
        }
    }

    public EntrySchema(Class<? extends Entry> clazz) {
        ColumnInfo[] columns = parseColumnInfo(clazz);
        this.mTableName = parseTableName(clazz);
        this.mColumnInfo = columns;
        String[] projection = new String[0];
        boolean hasFullTextIndex = false;
        if (columns != null) {
            projection = new String[columns.length];
            for (int i = 0; i != columns.length; i++) {
                ColumnInfo column = columns[i];
                projection[i] = column.name;
                if (column.fullText) {
                    hasFullTextIndex = true;
                }
            }
        }
        this.mProjection = projection;
        this.mHasFullTextIndex = hasFullTextIndex;
    }

    public String getTableName() {
        return this.mTableName;
    }

    private void logExecSql(SQLiteDatabase db, String sql) {
        db.execSQL(sql);
    }

    public void createTables(SQLiteDatabase db) {
        String tableName = this.mTableName;
        Utils.assertTrue(tableName != null);
        StringBuilder sql = new StringBuilder("CREATE TABLE ");
        sql.append(tableName);
        sql.append(" (_id INTEGER PRIMARY KEY AUTOINCREMENT");
        StringBuilder unique = new StringBuilder();
        for (ColumnInfo column : this.mColumnInfo) {
            if (!column.isId()) {
                sql.append(',');
                sql.append(column.name);
                sql.append(' ');
                sql.append(SQLITE_TYPES[column.type]);
                if (!TextUtils.isEmpty(column.defaultValue)) {
                    sql.append(" DEFAULT ");
                    sql.append(column.defaultValue);
                }
                if (column.unique) {
                    if (unique.length() == 0) {
                        unique.append(column.name);
                    } else {
                        unique.append(',').append(column.name);
                    }
                }
            }
        }
        if (unique.length() > 0) {
            sql.append(",UNIQUE(").append(unique).append(')');
        }
        sql.append(");");
        logExecSql(db, sql.toString());
        sql.setLength(0);
        for (ColumnInfo column2 : this.mColumnInfo) {
            if (column2.indexed) {
                sql.append("CREATE INDEX ");
                sql.append(tableName);
                sql.append("_index_");
                sql.append(column2.name);
                sql.append(" ON ");
                sql.append(tableName);
                sql.append(" (");
                sql.append(column2.name);
                sql.append(");");
                logExecSql(db, sql.toString());
                sql.setLength(0);
            }
        }
        if (this.mHasFullTextIndex) {
            String ftsTableName = tableName + "_fulltext";
            sql.append("CREATE VIRTUAL TABLE ");
            sql.append(ftsTableName);
            sql.append(" USING FTS3 (_id INTEGER PRIMARY KEY");
            for (ColumnInfo column22 : this.mColumnInfo) {
                if (column22.fullText) {
                    String columnName = column22.name;
                    sql.append(',');
                    sql.append(columnName);
                    sql.append(" TEXT");
                }
            }
            sql.append(");");
            logExecSql(db, sql.toString());
            sql.setLength(0);
            StringBuilder insertSql = new StringBuilder("INSERT OR REPLACE INTO ");
            insertSql.append(ftsTableName);
            insertSql.append(" (_id");
            for (ColumnInfo column222 : this.mColumnInfo) {
                if (column222.fullText) {
                    insertSql.append(',');
                    insertSql.append(column222.name);
                }
            }
            insertSql.append(") VALUES (new._id");
            for (ColumnInfo column2222 : this.mColumnInfo) {
                if (column2222.fullText) {
                    insertSql.append(",new.");
                    insertSql.append(column2222.name);
                }
            }
            insertSql.append(");");
            String insertSqlString = insertSql.toString();
            sql.append("CREATE TRIGGER ");
            sql.append(tableName);
            sql.append("_insert_trigger AFTER INSERT ON ");
            sql.append(tableName);
            sql.append(" FOR EACH ROW BEGIN ");
            sql.append(insertSqlString);
            sql.append("END;");
            logExecSql(db, sql.toString());
            sql.setLength(0);
            sql.append("CREATE TRIGGER ");
            sql.append(tableName);
            sql.append("_update_trigger AFTER UPDATE ON ");
            sql.append(tableName);
            sql.append(" FOR EACH ROW BEGIN ");
            sql.append(insertSqlString);
            sql.append("END;");
            logExecSql(db, sql.toString());
            sql.setLength(0);
            sql.append("CREATE TRIGGER ");
            sql.append(tableName);
            sql.append("_delete_trigger AFTER DELETE ON ");
            sql.append(tableName);
            sql.append(" FOR EACH ROW BEGIN DELETE FROM ");
            sql.append(ftsTableName);
            sql.append(" WHERE _id = old._id; END;");
            logExecSql(db, sql.toString());
            sql.setLength(0);
        }
    }

    public void dropTables(SQLiteDatabase db) {
        String tableName = this.mTableName;
        StringBuilder sql = new StringBuilder("DROP TABLE IF EXISTS ");
        sql.append(tableName);
        sql.append(';');
        logExecSql(db, sql.toString());
        sql.setLength(0);
        if (this.mHasFullTextIndex) {
            sql.append("DROP TABLE IF EXISTS ");
            sql.append(tableName);
            sql.append("_fulltext");
            sql.append(';');
            logExecSql(db, sql.toString());
        }
    }

    private String parseTableName(Class<? extends Object> clazz) {
        Table table = (Table) clazz.getAnnotation(Table.class);
        if (table == null) {
            return null;
        }
        return table.value();
    }

    private ColumnInfo[] parseColumnInfo(Class<? extends Object> clazz) {
        ArrayList<ColumnInfo> columns = new ArrayList();
        while (clazz != null) {
            parseColumnInfo(clazz, columns);
            clazz = clazz.getSuperclass();
        }
        ColumnInfo[] columnList = new ColumnInfo[columns.size()];
        columns.toArray(columnList);
        return columnList;
    }

    private void parseColumnInfo(Class<? extends Object> clazz, ArrayList<ColumnInfo> columns) {
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i != fields.length; i++) {
            Field field = fields[i];
            Column info = (Column) field.getAnnotation(Column.class);
            if (info != null) {
                int type;
                Class<?> fieldType = field.getType();
                if (fieldType == String.class) {
                    type = 0;
                } else if (fieldType == Boolean.TYPE) {
                    type = 1;
                } else if (fieldType == Short.TYPE) {
                    type = 2;
                } else if (fieldType == Integer.TYPE) {
                    type = 3;
                } else if (fieldType == Long.TYPE) {
                    type = 4;
                } else if (fieldType == Float.TYPE) {
                    type = 5;
                } else if (fieldType == Double.TYPE) {
                    type = 6;
                } else if (fieldType == byte[].class) {
                    type = 7;
                } else {
                    throw new IllegalArgumentException("Unsupported field type for column: " + fieldType.getName());
                }
                columns.add(new ColumnInfo(info.value(), type, info.indexed(), info.unique(), info.fullText(), info.defaultValue(), field, columns.size()));
            }
        }
    }
}
