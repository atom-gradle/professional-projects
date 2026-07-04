package com.qian.feather.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.qian.feather.item.User;

import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    // 数据库版本
    private static final int DATABASE_VERSION = 1;
    // 数据库名称
    private static final String DATABASE_NAME = "feather_db.db";

    // 创建表的SQL语句
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " +
            "entries (id INTEGER PRIMARY KEY, title TEXT, body TEXT)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 这里可以添加更新数据库的代码，例如删除旧表并创建新表
        db.execSQL("DROP TABLE IF EXISTS entries");
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    public void insertData(String name, int age){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("age", age);
        db.insert("user", null, values);
    }
    public Cursor queryData(){
        SQLiteDatabase db = getReadableDatabase();
        String[] columns ={"id","name","age"};
        Cursor cursor = db.query("user", columns, null, null, null, null, null);
        return cursor;
    }
    public void insertData(List<User> userList) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (User user : userList) {
                ContentValues values = new ContentValues();
                values.put("name", user.getName());
                db.insert("user", null, values);
            }        db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    public void createIndex() {
        SQLiteDatabase db = getWritableDatabase();
        String sql ="create index idx_user_name on user(name)";
        db.execSQL(sql);
    }

    /*
    public void insertData(List<User> userList){
        SQLiteDatabase db = getWritableDatabase();
        String sql ="insert into user(name, age) values(?,?)";
        SQLiteStatement statement = db.compileStatement(sql);
        db.beginTransaction();
        try {
            for (User user : userList) {
                statement.bindString(1, user.getName());
                statement.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
     */
    /*
    private void queryData() {
    SQLiteDatabase db = mDbHelper.getReadableDatabase();
    String[] projection = { "id", "title", "body" }; // 要查询的列名数组
    Cursor cursor = db.query("entries", projection, null, null, null, null, null); // 查询所有行，无筛选条件等。可以根据需要添加条件。
    while (cursor.moveToNext()) { // 遍历查询结果集
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id")); // 获取列值，注意索引是从0开始的。使用getColumnIndexOrThrow可以避免运行时错误。
        String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        // 处理获取到的数据...例如打印到Log或显示在UI上。
        Log.d("QueryResult", "ID: " + id + ", Title: " + title + ", Body: " + body);
    }
    cursor.close(); // 关闭Cursor以释放资源。虽然Cursor在Activity销毁时也会自动关闭，但最好手动关闭以养成良好的编程习惯。
    db.close(); // 关闭数据库连接以释放资源。同上，虽然Activity销毁时也会自动关闭，但最好手动关闭。
}
     */
}
