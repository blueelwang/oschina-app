package net.oschina.app.v2.activity;

import net.oschina.app.v2.ui.tab.SlidingTabLayout;

/**
 * Created by Sim on 2015/3/9.
 */
public interface IMainTab {

    public SlidingTabLayout getSlidingTabLayout();

    public  int getCurrentTab();
}
