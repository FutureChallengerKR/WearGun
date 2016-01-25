package com.karview.android.app;


//eunmin 01.25
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.jakewharton.disklrucache.DiskLruCache;
import com.karview.android.R;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import me.notisfy.android.ui.card.ListCardView;

public abstract class CardListFragment extends Fragment {
    
    public abstract void onInit(ListCardView listCardView);
    public abstract void onHalt();

    public abstract View onItemDisplay(int position, View convertView, ViewGroup parent);
    public abstract int onItemCount();

    private final ListCardView.ItemListener listItemListener = new ListCardView.ItemListener() {
        
        @Override
        public void onItemAutoLoad() {
        }
        
        @Override
        public View onItemDisplay(int position, View convertView, ViewGroup parent) {
            return CardListFragment.this.onItemDisplay(position, convertView, parent);
        }

        @Override
        public int onItemCount() {
            return CardListFragment.this.onItemCount();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inflate UI
        FrameLayout layout = (FrameLayout)inflater.inflate(R.layout.fragment_card_list, container, false);
        
        // list card view
        ListCardView listCardView = (ListCardView)layout.findViewById(R.id.list);
        listCardView.initListView( false, null, listItemListener);
        
        onInit(listCardView);

        return layout;
    }

    @Override
    public void onDestroyView() {
        onHalt();
        super.onDestroyView();
    }
    
    protected ImageLoader getImageLoader(){
        if(volleyImageLoader == null){
            volleyImageLoader = new ImageLoader(Volley.newRequestQueue(getActivity().getApplicationContext()), volleyImageCache);
        }
        return volleyImageLoader;
    }

    private ImageLoader volleyImageLoader = null;
    private ImageCache volleyImageCache = new ImageCache() {
        private DiskLruCache diskCache;
        private CompressFormat compressFormat = CompressFormat.PNG;
        private static final int IO_BUFFER_SIZE = 8*1024;
        private int compressQuality = 70;
        private static final int APP_VERSION = 1;
        private static final int VALUE_COUNT = 1;
        
        private void prepareDiskCache(){
            if(diskCache == null){
                long cacheSize = 10 * 1024 * 1024; // 10 MiB
                File cacheDir = new File(getActivity().getCacheDir(), "image");
                try {
                    diskCache = DiskLruCache.open( cacheDir, APP_VERSION, VALUE_COUNT, cacheSize );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor )
            throws IOException, FileNotFoundException {
            OutputStream out = null;
            try {
                out = new BufferedOutputStream( editor.newOutputStream( 0 ), IO_BUFFER_SIZE );
                return bitmap.compress( compressFormat, compressQuality, out );
            } finally {
                if ( out != null ) {
                    out.close();
                }
            }
        }
        
        private String md5(String s) {
            try {
                MessageDigest m = MessageDigest.getInstance("MD5");
                m.update(s.getBytes("UTF-8"));
                byte[] digest = m.digest();
                BigInteger bigInt = new BigInteger(1, digest);
                return bigInt.toString(16);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public void putBitmap( String key, Bitmap data ) {
            
            prepareDiskCache();
            if(diskCache == null){
                return;
            }
            
            key = md5(key);
            if(key == null){
                return;
            }

            DiskLruCache.Editor editor = null;
            try {
                editor = diskCache.edit( key );
                if ( editor == null ) {
                    return;
                }

                if( writeBitmapToFile( data, editor ) ) {               
                    diskCache.flush();
                    editor.commit();
                } else {
                    editor.abort();
                }   
            } catch (IOException e) {
                try {
                    if ( editor != null ) {
                        editor.abort();
                    }
                } catch (IOException ignored) {
                }           
            }

        }

        @Override
        public Bitmap getBitmap( String key ) {
            prepareDiskCache();
            if(diskCache == null){
                return null;
            }

            key = md5(key);
            if(key == null){
                return null;
            }

            Bitmap bitmap = null;
            DiskLruCache.Snapshot snapshot = null;
            try {

                snapshot = diskCache.get( key );
                if ( snapshot == null ) {
                    return null;
                }
                final InputStream in = snapshot.getInputStream( 0 );
                if ( in != null ) {
                    final BufferedInputStream buffIn = new BufferedInputStream( in, IO_BUFFER_SIZE );
                    bitmap = BitmapFactory.decodeStream( buffIn );              
                }   
            } catch ( IOException e ) {
                e.printStackTrace();
            } finally {
                if ( snapshot != null ) {
                    snapshot.close();
                }
            }

            return bitmap;

        }
    };
}