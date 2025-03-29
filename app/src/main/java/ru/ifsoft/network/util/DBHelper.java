package ru.ifsoft.network.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    // Table Name
    public static final String TABLE_NAME_BLOCKED_USERS = "BLOCKED_USERS";

    // Table columns
    public static final String ID = "id";
    public static final String ACCOUNT_ID = "accountId";
    public static final String CREATE_AT = "createAt";

    // Database Information
    static final String DB_NAME = "MYSOCIALNETWORK.DB";

    // database version
    static final int DB_VERSION = 1;

    // Creating table query
    private static final String CREATE_TABLE_BLOCKED_USERS = "create table " + TABLE_NAME_BLOCKED_USERS + "("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ACCOUNT_ID + " INTEGER NOT NULL DEFAULT 0, "
            + CREATE_AT + " INTEGER NOT NULL DEFAULT 0);";

    public DBHelper(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE_BLOCKED_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_BLOCKED_USERS);
        onCreate(db);
    }
}