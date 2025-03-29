package ru.ifsoft.network.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBManager {

    private DBHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public DBManager(Context c) {

        context = c;
    }

    public DBManager open() throws SQLException {

        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();

        return this;
    }

    public void delete_table(String TABLE_NAME) throws SQLException {

        database.delete(TABLE_NAME, null, null);
    }

    public void close() {

        dbHelper.close();
    }

    public void insert(long accountId, long createAt) {

        ContentValues contentValue = new ContentValues();
        contentValue.put(DBHelper.ACCOUNT_ID, accountId);
        contentValue.put(DBHelper.CREATE_AT, createAt);
        database.insert(DBHelper.TABLE_NAME_BLOCKED_USERS, null, contentValue);
    }

    public Cursor fetch() {

        String[] columns = new String[] { DBHelper.ID, DBHelper.ACCOUNT_ID, DBHelper.CREATE_AT};
        Cursor cursor = database.query(DBHelper.TABLE_NAME_BLOCKED_USERS, columns, null, null, null, null, null);

        if (cursor != null) {

            cursor.moveToFirst();
        }

        return cursor;
    }

    public int update(long id, long accountId, long createAt) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.ACCOUNT_ID, accountId);
        contentValues.put(DBHelper.CREATE_AT, createAt);

        int i = database.update(DBHelper.TABLE_NAME_BLOCKED_USERS, contentValues, DBHelper.ID + " = " + id, null);

        return i;
    }

    public void delete(long id) {

        database.delete(DBHelper.TABLE_NAME_BLOCKED_USERS, DBHelper.ID + "=" + id, null);
    }

    public long count() {

        return DatabaseUtils.queryNumEntries(database, DBHelper.TABLE_NAME_BLOCKED_USERS);
    }

    public Boolean isExist(long accountId) {

        SQLiteDatabase sqldb = dbHelper.getReadableDatabase();

        String Query = "SELECT * FROM  " + DBHelper.TABLE_NAME_BLOCKED_USERS + " WHERE " + DBHelper.ACCOUNT_ID + " = " + accountId;
        Cursor cursor = sqldb.rawQuery(Query, null);

        if (cursor.getCount() <= 0){

            cursor.close();

            return false;
        }

        cursor.close();

        return true;
    }

    public Boolean isExpired(long accountId) {

        if (this.isExist(accountId)) {

            String Query = "SELECT * FROM  " + DBHelper.TABLE_NAME_BLOCKED_USERS + " WHERE " + DBHelper.ACCOUNT_ID + " = " + accountId;
            Cursor cursor = database.rawQuery(Query, null);

            if (cursor.getCount() > 0){

                cursor.moveToFirst();

                long createAt  = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.CREATE_AT));

                cursor.close();

                long unixTime = System.currentTimeMillis() / 1000L;

                if (createAt < (unixTime - 2592000)) {

                    return true;

                } else {

                    return false;
                }
            }
        }

        return true;
    }
}