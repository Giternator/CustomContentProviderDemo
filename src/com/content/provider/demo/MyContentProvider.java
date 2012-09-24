package com.content.provider.demo;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class MyContentProvider extends ContentProvider{

	   public static final String PROVIDER_NAME = 
			      "com.content.provider.demo.Books";
			 
			   public static final Uri CONTENT_URI = 
			      Uri.parse("content://"+ PROVIDER_NAME + "/books");
			 
			   public static final String _ID = "_id";
			   public static final String TITLE = "title";
			   public static final String ISBN = "isbn";
			 
			   private static final int BOOKS = 1;
			   private static final int BOOK_ID = 2;   
			 
			   private static final UriMatcher uriMatcher;
			   
			   static{
			      uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
			      uriMatcher.addURI(PROVIDER_NAME, "books", BOOKS);
			      uriMatcher.addURI(PROVIDER_NAME, "books/#", BOOK_ID);      
			   }   
			 
			   //---for database use---
			   private SQLiteDatabase booksDB;
			   private static final String DATABASE_NAME = "Books";
			   private static final String DATABASE_TABLE = "titles";
			   private static final int DATABASE_VERSION = 1;
			   private static final String DATABASE_CREATE =
			         "create table " + DATABASE_TABLE + 
			         " (_id integer primary key autoincrement, "
			         + "title text not null, isbn text not null);";
			   
			   
			   private static class DatabaseHelper extends SQLiteOpenHelper 
			   {
			      DatabaseHelper(Context context) {
			         super(context, DATABASE_NAME, null, DATABASE_VERSION);
			      }
			 
			      @Override
			      public void onCreate(SQLiteDatabase db) 
			      {
			         db.execSQL(DATABASE_CREATE);
			      }
			 
			      @Override
			      public void onUpgrade(SQLiteDatabase db, int oldVersion, 
			      int newVersion) {
			         Log.w("Content provider database", 
			              "Upgrading database from version " + 
			              oldVersion + " to " + newVersion + 
			              ", which will destroy all old data");
			         db.execSQL("DROP TABLE IF EXISTS titles");
			         onCreate(db);
			      }
			   }   
			   
			@Override
			public int delete(Uri uri, String selection, String[] selectionArgs) {
				
				Log.d("TAG", "delete");
				
				int count=0;
			      switch (uriMatcher.match(uri)){
			         case BOOKS:
			            count = booksDB.delete(
			               DATABASE_TABLE,
			               selection, 
			               selectionArgs);
			            break;
			         case BOOK_ID:
			            String id = uri.getPathSegments().get(1);
			            count = booksDB.delete(
			               DATABASE_TABLE,                        
			               _ID + " = " + id + 
			               (!TextUtils.isEmpty(selection) ? " AND (" + 
			            		   selection + ')' : ""), 
			            		   selectionArgs);
			            break;
			         default: throw new IllegalArgumentException(
			            "Unknown URI " + uri);    
			      }       
			      getContext().getContentResolver().notifyChange(uri, null);
			      return count;
			}
			@Override
			public String getType(Uri uri) {
				
				Log.d("TAG", "getType");
				
				return null;/*
				switch (uriMatcher.match(uri)){
		         //---get all books---
		         case BOOKS:
		            return "vnd.android.cursor.dir/vnd.learn2develop.books ";
		         //---get a particular book---
		         case BOOK_ID:                
		            return "vnd.android.cursor.item/vnd.learn2develop.books ";
		         default:
		            throw new IllegalArgumentException("Unsupported URI: " + uri);        
		      }  
			*/}
			@Override
			public Uri insert(Uri uri, ContentValues values) {
				
				Log.d("TAG", "insert");
				long rowID = booksDB.insert(
				         DATABASE_TABLE, "", values);
				 
				      //---if added successfully---
				      if (rowID>0)
				      {
				         Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
				         getContext().getContentResolver().notifyChange(_uri, null);    
				         return _uri;                
				      }        
				      throw new SQLException("Failed to insert row into " + uri);
			}
			
			@Override
			public boolean onCreate() {
				
				Log.d("TAG", "onCreate");
				
				Context context = getContext();
			      DatabaseHelper dbHelper = new DatabaseHelper(context);
			      booksDB = dbHelper.getWritableDatabase();
			      return (booksDB == null)? false:true;
			}
			
			@Override
			public Cursor query(Uri uri, String[] projection, String selection,
					String[] selectionArgs, String sortOrder) {
				
				Log.d("TAG", "query");
				
				SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
			      sqlBuilder.setTables(DATABASE_TABLE);
			 
			      if (uriMatcher.match(uri) == BOOK_ID)
			         //---if getting a particular book---
			         sqlBuilder.appendWhere(
			            _ID + " = " + uri.getPathSegments().get(1));                
			 
			      if (sortOrder==null || sortOrder=="")
			         sortOrder = TITLE;
			 
			      Cursor c = sqlBuilder.query(
			         booksDB, 
			         projection, 
			         selection, 
			         selectionArgs, 
			         null, 
			         null, 
			         sortOrder);
			 
			      //---register to watch a content URI for changes---
			      c.setNotificationUri(getContext().getContentResolver(), uri);
			      return c;
			}
			@Override
			public int update(Uri uri, ContentValues values, String selection,
					String[] selectionArgs) {
				
				Log.d("TAG", "update");
				
				int count = 0;
			      switch (uriMatcher.match(uri)){
			         case BOOKS:
			            count = booksDB.update(
			               DATABASE_TABLE, 
			               values,
			               selection, 
			               selectionArgs);
			            break;
			         case BOOK_ID:                
			            count = booksDB.update(
			               DATABASE_TABLE, 
			               values,
			               _ID + " = " + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),selectionArgs);
			            break;
			         default: throw new IllegalArgumentException(
			            "Unknown URI " + uri);    
			      }       
			      getContext().getContentResolver().notifyChange(uri, null);
			      return count;
			}
			
			@Override
			public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
				AssetManager am = getContext().getAssets();
		        AssetFileDescriptor afd = null;
		        try {
		            afd = am.openFd("song.mp3");
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		        return afd;
			}
}
