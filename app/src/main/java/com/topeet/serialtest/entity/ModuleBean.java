package com.topeet.serialtest.entity;

/**
 * 模块bean
 */
public class ModuleBean {
    private String name;
    private int icon;
    private Class<?> DesCls;
    private Object what;

    public ModuleBean(String name, int icon,
                      Class<?> desCls, Object what) {
        this.name = name;
        this.icon = icon;
        DesCls = desCls;
        this.what = what;
    }

    public ModuleBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public Class<?> getDesCls() {
        return DesCls;
    }

    public void setDesCls(Class<?> desCls) {
        DesCls = desCls;
    }

    public Object getWhat() {
        return what;
    }

    public void setWhat(Object what) {
        this.what = what;
    }
}
