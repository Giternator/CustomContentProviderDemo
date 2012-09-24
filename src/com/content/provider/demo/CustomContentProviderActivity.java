package com.content.provider.demo;

import android.app.Activity;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class CustomContentProviderActivity extends Activity {
	
	MediaPlayer mp;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
       insertData();
       readData();
       readAssetsSongFromAnotherApplication();
    }
    
    private void readData() {
    	 Uri allTitles = Uri.parse("content://com.content.provider.demo.Books/books");
	   Cursor c = managedQuery(allTitles, null, null, null, "title asc");
	   if (c.moveToFirst()) {
	      do{
	    	  
	    	  Log.d("output", c.getString(c.getColumnIndex(
	            MyContentProvider._ID)) + ", " +                     
	            c.getString(c.getColumnIndex(
	               MyContentProvider.TITLE)) + ", " +                     
	            c.getString(c.getColumnIndex(
	               MyContentProvider.ISBN)));
	    	  
	    	  
	         Toast.makeText(this, 
	            c.getString(c.getColumnIndex(
	            MyContentProvider._ID)) + ", " +                     
	            c.getString(c.getColumnIndex(
	               MyContentProvider.TITLE)) + ", " +                     
	            c.getString(c.getColumnIndex(
	               MyContentProvider.ISBN)), 
	            Toast.LENGTH_LONG).show();               
	      } while (c.moveToNext());
	   }
	}
    
    private void insertData() {
    	ContentValues values = new ContentValues();
        values.put(MyContentProvider.TITLE, "C# 2008 Programmer's Reference");
        values.put(MyContentProvider.ISBN, "0470285818");        
        getContentResolver().insert(MyContentProvider.CONTENT_URI, values);
      
        //---add another book---
        values.clear();
        values.put(MyContentProvider.TITLE, "Programming Sudoku");
        values.put(MyContentProvider.ISBN, "1590596625");        
        getContentResolver().insert(MyContentProvider.CONTENT_URI, values);
    }
    
    /*private void updateDataByID() {
    	ContentValues values = new ContentValues();
        values.put(MyContentProvider.TITLE, "salman");
        values.put(MyContentProvider.ISBN, "0470285818");
        getContentResolver().update(MyContentProvider.CONTENT_URI, values, "_id=?", new String[]{"2"});
    }
    
    private void deleteDataByID() {
    	getContentResolver().delete(MyContentProvider.CONTENT_URI, "_id=?",new String[]{"1"});
	}*/
    
    /**
     * 
     * Use this method to read data from Another Application
     * 
     **/
    /*private void readDataFromAnotherApplication() {
      	 Uri allTitles = Uri.parse(
   	      "content://com.content.provider.demo.Books/books");
   	   Cursor c = managedQuery(allTitles, null, null, null, "title asc");
   	   if (c.moveToFirst()) {
   	      do{
   	    	  
   	    	  Log.d("output", c.getString(c.getColumnIndex("_id")) + ", " +                     
   	            c.getString(c.getColumnIndex("title")) + ", " +                     
   	            c.getString(c.getColumnIndex("isbn")));
   	    	  
   	    	  
   	         Toast.makeText(this, 
   	            c.getString(c.getColumnIndex("_id")) + ", " +                     
   	            c.getString(c.getColumnIndex("title")) + ", " +                     
   	            c.getString(c.getColumnIndex("isbn")), 
   	            Toast.LENGTH_LONG).show();               
   	      } while (c.moveToNext());
   	   }
   	}*/
    
	private void readAssetsSongFromAnotherApplication() {
		Uri uri = Uri.parse("content://com.content.provider.demo.Books/books");
		try {
			AssetFileDescriptor asd = getContentResolver()
					.openAssetFileDescriptor(uri, "r");
			mp = new MediaPlayer();
			mp.setDataSource(asd.getFileDescriptor(), asd.getStartOffset(),
					asd.getLength());
			mp.prepare();
			mp.start();// play sound
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(mp.isPlaying())
		mp.stop();
		mp.release();
	}
}