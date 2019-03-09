package com.example.pam.tapp;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {
    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    //to call the tabs
    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                ChatsFragment chatsFragment =  new ChatsFragment();
                return chatsFragment;
            case 1:
                GroupsFragment groupsFragment =  new GroupsFragment();
                return groupsFragment;
            case 2:
                ContactsFragment contactsFragment =  new ContactsFragment();
                return contactsFragment;
            case 3:
                RequestFragment requestsFragment =  new RequestFragment();
                return requestsFragment;
            default:
                return null;

        }

    }

    @Override
    public int getCount() {
        return 4;
    }

    //setting the title for the tabs
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Chats";
            case 1:
                return "Groups";
            case 2:
                return "Contacts";
            case 3:
                return "Requests";

            default:
                return null;

        }
    }
}
