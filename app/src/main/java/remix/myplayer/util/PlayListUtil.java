package remix.myplayer.util;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;

import remix.myplayer.model.PlayListNewInfo;
import remix.myplayer.model.PlayListSongInfo;
import remix.myplayer.db.PlayListSongs;
import remix.myplayer.db.PlayLists;

/**
 * @ClassName
 * @Description
 * @Author Xiaoborui
 * @Date 2016/10/17 10:24
 */
public class PlayListUtil {
    private static final String TAG = "PlayListUtil";
    private static Context mContext;
//    private static DBContentProvider mProvider;
    private PlayListUtil(){}
    public static void setContext(Context context){
        mContext = context;
//        mProvider = new DBContentProvider(mContext);
    }

    /**
     * 新增播放列表
     * @param playListName
     * @return 新增的播放列表id
     */
    public static int addPlayList(String playListName) {
        if(TextUtils.isEmpty(playListName))
            return -1;
        ContentValues cv = new ContentValues();
        cv.put(PlayLists.PlayListColumns.COUNT, 0);
        cv.put(PlayLists.PlayListColumns.NAME, playListName);
        Uri uri = mContext.getContentResolver().insert(PlayLists.CONTENT_URI, cv);
        return uri != null ? (int) ContentUris.parseId(uri) : -1;
    }

    /**
     * 删除一个播放列表
     * @param id
     * @return
     */
    public static boolean deletePlayList(int id){
        return id > 0 && mContext.getContentResolver().delete(PlayLists.CONTENT_URI, PlayLists.PlayListColumns._ID + "=?",new String[]{id + ""}) > 0;
    }

    /**
     * 删除多个播放列表
     * @param IdList
     * @return
     */
    public static int deleteMultiPlayList(ArrayList<Integer> IdList){
        if(IdList == null || IdList.size() == 0)
            return 0;
        int deleteNum = 0;
        String where = "";
        String[] whereArgs = new String[IdList.size()];
        for(int i = 0 ; i < IdList.size() ;i++){
            whereArgs[i] = IdList.get(i) + "";
            where += (PlayLists.PlayListColumns._ID + "=?");
            if(i != IdList.size() - 1){
                where += " or ";
            }
        }
        return mContext.getContentResolver().delete(PlayLists.CONTENT_URI,where,whereArgs);
    }

    /**
     * 添加一首歌曲
     * @param info
     * @return 新增歌曲的id
     */
    public static int addSong(PlayListSongInfo info){
        if(getIDList(info.PlayListID).contains(info.AudioId))
            return 0;
        ContentValues cv = new ContentValues();
        cv.put(PlayListSongs.PlayListSongColumns.AUDIO_ID,info.AudioId);
//        cv.put(PlayListSongs.PlayListSongColumns.Album,info.Album);
//        cv.put(PlayListSongs.PlayListSongColumns.Album_ID,info.AlbumID);
//        cv.put(PlayListSongs.PlayListSongColumns.Artist,info.Artist);
//        cv.put(PlayListSongs.PlayListSongColumns.Artist_ID,info.ArtistID);
        cv.put(PlayListSongs.PlayListSongColumns.PLAY_LIST_ID,info.PlayListID);
        cv.put(PlayListSongs.PlayListSongColumns.PLAY_LIST_NAME,info.PlayListName);
        Uri uri = mContext.getContentResolver().insert(PlayListSongs.CONTENT_URI, cv);
        return uri != null ? (int) ContentUris.parseId(uri) : -1;
    }

    /**
     * 添加多首歌曲
     * @param infos
     * @return
     */
    public static int addMultiSongs(ArrayList<PlayListSongInfo> infos){
        if(infos == null || infos.size() == 0 )
            return 0;
        //不重复添加
        ArrayList<Integer> rawIDList = getIDList(infos.get(0).PlayListID);
        for(int i = 0 ; i < rawIDList.size() ;i++){
            for(int j = infos.size() - 1; j >= 0; j--){
                if(rawIDList.get(i) == infos.get(j).AudioId)
                    infos.remove(j);
            }
        }

        ContentValues[] values = new ContentValues[infos.size()];
        for(int i = 0 ; i < infos.size() ;i++){
            ContentValues cv = new ContentValues();
            PlayListSongInfo info = infos.get(i);
            cv.put(PlayListSongs.PlayListSongColumns.PLAY_LIST_NAME,info.PlayListName);
            cv.put(PlayListSongs.PlayListSongColumns.PLAY_LIST_ID,info.PlayListID);
            cv.put(PlayListSongs.PlayListSongColumns.AUDIO_ID,info.AudioId);
            values[i] = cv;
        }
        return mContext.getContentResolver().bulkInsert(PlayListSongs.CONTENT_URI,values);
    }

    /**
     * 添加多首歌曲
     * @param IDList
     * @return
     */
    public static int addMultiSongs(ArrayList<Integer> IDList,String playListName){
        return addMultiSongs(IDList,playListName,getPlayListID(playListName));
    }

    /**
     * 添加多首歌曲
     * @param IDList
     * @return
     */
    public static int addMultiSongs(ArrayList<Integer> IDList,String playListName,int playListId){
        if(IDList == null || IDList.size() == 0 )
            return 0;
        //不重复添加
        ArrayList<Integer> rawIDList = getIDList(playListName);
        for(int i = 0 ; i < rawIDList.size() ;i++){
            for(int j = IDList.size() - 1; j >= 0; j--){
                if(rawIDList.get(i).equals(IDList.get(j)))
                    IDList.remove(j);
            }
        }

        ContentValues[] values = new ContentValues[IDList.size()];
        for(int i = 0 ; i < IDList.size() ;i++){
            ContentValues cv = new ContentValues();
            cv.put(PlayListSongs.PlayListSongColumns.PLAY_LIST_NAME,playListName);
            cv.put(PlayListSongs.PlayListSongColumns.PLAY_LIST_ID,playListId);
            cv.put(PlayListSongs.PlayListSongColumns.AUDIO_ID,IDList.get(i));
            values[i] = cv;
        }
        return mContext.getContentResolver().bulkInsert(PlayListSongs.CONTENT_URI,values);
    }

    /**
     * 删除一首歌曲
     * @param audioId
     * @param playListId
     * @return
     */
    public static boolean deleteSong(int audioId,int playListId){
        return audioId > 0 &&
                mContext.getContentResolver().delete(PlayListSongs.CONTENT_URI,
                        PlayListSongs.PlayListSongColumns.AUDIO_ID + "=?" + " and " + PlayListSongs.PlayListSongColumns.PLAY_LIST_ID + "=?",
                        new String[]{audioId + "",playListId + ""}) > 0;
    }

    /**
     * 删除一首歌曲
     * @param audioId
     * @param playListName
     * @return
     */
    public static boolean deleteSong(int audioId,String playListName){
        return audioId > 0 && !TextUtils.isEmpty(playListName) &&
                mContext.getContentResolver().delete(PlayListSongs.CONTENT_URI,
                        PlayListSongs.PlayListSongColumns.PLAY_LIST_NAME + "=?" + " and " + PlayListSongs.PlayListSongColumns.PLAY_LIST_NAME + "=?",
                        new String[]{audioId + "",playListName}) > 0;
    }

    /**
     * 删除多首歌曲
     * @param IdList
     * @param playlistId
     * @return
     */
    public static int deleteMultiSongs(ArrayList<Integer> IdList,int playlistId){
        if(IdList == null || IdList.size() == 0)
            return 0;
        String where = "";
        String[] whereArgs = new String[IdList.size() + 1];
        for(int i = 0 ; i < IdList.size() + 1;i++) {
            if (i != IdList.size()) {
                if (i == 0) {
                    where += "(";
                }
                where += (PlayListSongs.PlayListSongColumns.AUDIO_ID + "=?");
                if (i != IdList.size() - 1) {
                    where += " or ";
                }
                whereArgs[i] = IdList.get(i) + "";
            }else {
                where += (") and " + PlayListSongs.PlayListSongColumns.PLAY_LIST_ID + "=?");
                whereArgs[i] = playlistId + "";
            }
        }
        return mContext.getContentResolver().delete(PlayListSongs.CONTENT_URI,where,whereArgs);
    }

    /**
     * 根据播放列表id获得对应的名字
     * @param playListId
     * @return
     */
    public static String getPlayListName(int playListId){
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(PlayLists.CONTENT_URI,new String[]{PlayLists.PlayListColumns.NAME},
                    PlayLists.PlayListColumns._ID + "=?",new String[]{playListId + ""},null,null);
            if(cursor != null && cursor.getCount() > 0 && cursor.moveToFirst())
                return cursor.getString(cursor.getColumnIndex(PlayLists.PlayListColumns.NAME));
        } catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 根据播放列表名获得播放列表id
     * @param playListName
     * @return
     */
    public static int getPlayListID(String playListName){
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(PlayLists.CONTENT_URI,new String[]{PlayLists.PlayListColumns._ID},
                    PlayLists.PlayListColumns.NAME + "=?",new String[]{playListName},null,null);
            if(cursor != null && cursor.getCount() > 0 && cursor.moveToFirst())
                return cursor.getInt(cursor.getColumnIndex(PlayLists.PlayListColumns._ID));
        } catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 返回系统中除播放队列所有播放列表
     * @return
     */
    public static ArrayList<PlayListNewInfo> getAllPlayListInfo(){
        ArrayList<PlayListNewInfo> playList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(PlayLists.CONTENT_URI,null,PlayLists.PlayListColumns.NAME + "!= ?",
                    new String[]{Constants.PLAY_QUEUE},null);
            if(cursor != null && cursor.getCount() > 0){
                while (cursor.moveToNext()){
                    PlayListNewInfo info = new PlayListNewInfo();
                    info._Id = cursor.getInt(cursor.getColumnIndex(PlayLists.PlayListColumns._ID));
                    info.Count = cursor.getInt(cursor.getColumnIndex(PlayLists.PlayListColumns.COUNT));
                    info.Name = cursor.getString(cursor.getColumnIndex(PlayLists.PlayListColumns.NAME));
                    playList.add(info);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if(cursor != null && !cursor.isClosed())
                cursor.close();
        }
        return playList;
    }

    /**
     * 根据播放列表名获得歌曲id列表
     * @param playlistName
     * @return
     */
    public static ArrayList<Integer> getIDList(String playlistName){
        ArrayList<Integer> IDList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(PlayLists.CONTENT_URI,new String[]{PlayListSongs.PlayListSongColumns.AUDIO_ID},
                    PlayLists.PlayListColumns.NAME + "=?",new String[]{playlistName},null,null);
            if(cursor != null && cursor.getCount() > 0){
                while (cursor.moveToNext()){
                    IDList.add(cursor.getInt(cursor.getInt(cursor.getColumnIndex(PlayListSongs.PlayListSongColumns.AUDIO_ID))));
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if(cursor != null && !cursor.isClosed()){
                cursor.close();
            }
        }
        return IDList;
    }

    /**
     * 根据播放列表id获得歌曲id列表
     * @param playlistId
     * @return
     */
    public static ArrayList<Integer> getIDList(int playlistId){
        ArrayList<Integer> IDList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(PlayListSongs.CONTENT_URI,new String[]{PlayListSongs.PlayListSongColumns.AUDIO_ID},
                    PlayListSongs.PlayListSongColumns.PLAY_LIST_ID + "=?",new String[]{playlistId + ""},null,null);
            if(cursor != null && cursor.getCount() > 0){
                while (cursor.moveToNext()){
                    IDList.add(cursor.getInt(cursor.getColumnIndex(PlayListSongs.PlayListSongColumns.AUDIO_ID)));
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if(cursor != null && !cursor.isClosed()){
                cursor.close();
            }
        }
        return IDList;
    }

    /**
     *
     * @param cursor
     * @return
     */
    public static PlayListNewInfo getPlayListInfo(Cursor cursor){
        PlayListNewInfo info = new PlayListNewInfo();
        try {
            info.Count = cursor.getInt(cursor.getColumnIndex(PlayLists.PlayListColumns.COUNT));
            info._Id = cursor.getInt(cursor.getColumnIndex(PlayLists.PlayListColumns._ID));
            info.Name = cursor.getString(cursor.getColumnIndex(PlayLists.PlayListColumns.NAME));
        } catch (Exception e){
            e.printStackTrace();
        }
        return info;
    }
}
