package net.mvla.mvhs;

import java.util.HashMap;
import java.util.Map;

public class MvpPresenterHolder {
    static volatile MvpPresenterHolder singleton = null;

    private Map<Class, MvpPresenter> presenterMap;


    private MvpPresenterHolder() {
        this.presenterMap = new HashMap<>();
    }

    public static MvpPresenterHolder getInstance() {
        if (singleton == null) {
            synchronized (MvpPresenterHolder.class) {
                if (singleton == null) {
                    singleton = new MvpPresenterHolder();
                }
            }
        }
        return singleton;
    }

    public void putPresenter(Class c, MvpPresenter p) {
        presenterMap.put(c, p);
    }

    public <T extends MvpPresenter> T getPresenter(Class<T> c) {
        return (T) presenterMap.get(c);
    }

    public void remove(Class c) {
        presenterMap.remove(c);
    }
}