package com.springsearchengine.dao;

import java.util.ArrayList;

public class SiteTree {
    private String root;
    private ArrayList<SiteTree> children;
    public int level;

    public SiteTree(String root) {
        this.root = root;
        children = new ArrayList<>();
    }

    public String getRoot() {
        return root;
    }

    public void addChild(SiteTree tree) {
        tree.setLevel(level + 1);
        children.add(tree);
    }

    private void setLevel(int level) {
        this.level = level;
    }

    public ArrayList<SiteTree> getChildren() {

        return children;
    }

}