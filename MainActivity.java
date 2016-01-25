package com.karview.android.app;

import me.notisfy.android.ui.SideMenuView;
import me.notisfy.android.ui.SideMenuView.MenuItemGroup;
import net.simonvt.menudrawer.MenuDrawer;

import com.karview.android.R;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;

public class MainActivity extends SherlockFragmentActivity {
    
	public static final int FG_SIGNIN = 0;
	public static final int FG_SIGNUP = 1;
	public static final int FG_PROFILE = 2;
	public static final int FG_NEWSFEED = 3;
	public static final int FG_CARSHOP = 4;
    
    private static final String[] FRAGMENT_TAG = new String[]{
    	"signin", "signup", "profile", "newsfeed", "charshop"};

    public static void setFragment(FragmentManager fragmentMgr, int fid){
//        FragmentManager fragmentMgr = getSupportFragmentManager();
        String tag = FRAGMENT_TAG[fid];
        int menuId = 0;
        Fragment targetFragment = fragmentMgr.findFragmentByTag(tag);
        if(targetFragment == null){
        	switch(fid) {
        	case FG_SIGNIN:
        		targetFragment = new SignInFragment();
        		menuId = 0;
        		break;
        	case FG_SIGNUP:
        		targetFragment = new SignUpFragment();
        		menuId = 0;
        		break;
        	case FG_NEWSFEED:
        		targetFragment = new NewsFeedFragment();
        		menuId = 1;
        		break;
        	case FG_CARSHOP:
        		targetFragment = new CarShopHomeFragment();
        		menuId = 2;
        		break;
        	}
        }else{
            int cnt = fragmentMgr.getBackStackEntryCount();
            if(cnt > 0){
                // target == top of back stack
                // do nothing
                String backStackName = fragmentMgr.getBackStackEntryAt(cnt-1).getName();
                if(tag.equals(backStackName)){
                    return;
                }
                
                if(backStackName.equalsIgnoreCase("signup") && tag.equalsIgnoreCase("signin")){
                	return;
                }
                
                // target == home
                // pop back stack and reset target
                if(fid == FG_NEWSFEED){
                    fragmentMgr.popBackStack(tag, 0);
                    targetFragment = null;
                }
            }
        }
            
        if(targetFragment != null){
            FragmentTransaction ft = fragmentMgr.beginTransaction();
            if(fid != FG_NEWSFEED){
//                ft.setCustomAnimations(android.R.anim.fade_in, 0, 0, me.notisfy.android.ui.R.anim.fade_out);
            }
            ft.replace(R.id.fragment_container, targetFragment, tag);
            ft.addToBackStack(tag);
            ft.commitAllowingStateLoss();
        }
        sideMenuView.setItemSelected(menuId);
    }
    
    public static void backFragment(FragmentManager fragmentMgr) {
        int backStackCnt = fragmentMgr.getBackStackEntryCount();
        if(backStackCnt != 1){
        	fragmentMgr.popBackStack();
        }
    }

    private static SideMenuView sideMenuView = null;
    private MenuDrawer menuDrawer = null;
    private FragmentManager fragmentMgr = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // action bar
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setTitle("KarView");
        
        // content and side menu
        menuDrawer = MenuDrawer.attach(this, MenuDrawer.Type.OVERLAY);
        menuDrawer.setDropShadowEnabled(false);
        menuDrawer.setContentView(R.layout.activity_main);

        sideMenuView = new SideMenuView(this);
        fragmentMgr = getSupportFragmentManager();

        MenuItemGroup sideMenuItem = sideMenuView.addGroup("카뷰");
        sideMenuItem.addItem("로그인", new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
            	setFragment(fragmentMgr, FG_SIGNIN);
                closeSideMenu();
            }
        });
        sideMenuItem.addItem("자동차 소식", new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                setFragment(fragmentMgr, FG_NEWSFEED);
                closeSideMenu();
            }
        });
//        sideMenuItem.addItem("주변 자동차 업체", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                setFragment(CAR_SHOP_LIST);
//                closeSideMenu();
//            }
//        });

        menuDrawer.setMenuView(sideMenuView);

        // set initial side menu and fragment
        setEnableSideMenu(true);
        setFragment(fragmentMgr, FG_NEWSFEED);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                menuDrawer.toggleMenu();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(closeSideMenu()){
                return true;
            }
            // back fragment
            FragmentManager fm = getSupportFragmentManager();
            int backStackCnt = fm.getBackStackEntryCount();
            if(backStackCnt == 1){
                finish();
                return true;
            }
            fm.popBackStack();
            String tag = fm.getBackStackEntryAt(backStackCnt-2).getName();
            for(int i = 0; i < FRAGMENT_TAG.length; ++i){
                if(FRAGMENT_TAG[i].equals(tag)){
                    sideMenuView.setItemSelected(i);
                    break;
                }
            }
            return true;
            
        }
        return super.onKeyUp(keyCode, event);
    }
    
    private void setEnableSideMenu(boolean enable){
        if(enable){
            menuDrawer.setSlideDrawable(R.drawable.ic_drawer);
            menuDrawer.setDrawerIndicatorEnabled(true);
        }else{
            menuDrawer.setDrawerIndicatorEnabled(false);
            menuDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_NONE);
        }
    }
    
    private boolean closeSideMenu(){
        final int drawerState = menuDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            menuDrawer.closeMenu();
            return true;
        }
        
        return false;
    }
}