package com.springsearchengine.dao;

import com.springsearchengine.config.FieldConfig;
import com.springsearchengine.model.IndexStorage;
import com.springsearchengine.model.entity.Site;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

public class IndexTask extends RecursiveTask<Set<IndexStorage>> {
    private SiteTree tree;
    private static String rootPath;
    private FieldConfig fieldConfig;
    private Site site;

    public IndexTask(SiteTree tree, FieldConfig fieldConfig, Site site) {
        this.fieldConfig = fieldConfig;
        this.tree = tree;
        this.site = site;
    }

    Set<String> mapList = new HashSet<>();


    @Override
    protected Set<IndexStorage> compute() {
        List<IndexTask> taskList = new LinkedList<>();
        Set<IndexStorage> indexSet = new HashSet<>();
        if (tree.level == 0) {
            rootPath = tree.getRoot();
            rootPath = rootPath.trim();
        }

        try {
            String path = tree.getRoot();
            path = path.trim();
            Connection connection = Jsoup.connect(path);
            Document document = connection
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .referrer("http://www.google.com")
                    .timeout(5000)
                    .get();
            IndexElementCreator indexElementCreator = new IndexElementCreator(document, fieldConfig, site);
            IndexStorage indexStorage = indexElementCreator.getIndexStorage();
            indexSet.add(indexStorage);
            Elements links = document.select("a");
            for (Element element : links) {
                if (SearchEngineManager.toStop) {
                    break;
                }
                String linkName = element.attr("abs:href");
                if (!mapList.contains(linkName) && linkName.matches(path + ".+")
                        && !linkName.contains("#") && !linkName.matches(".+pdf")) {
                    SiteTree child = new SiteTree(linkName);
                    IndexTask task = new IndexTask(child, fieldConfig, site);
                    task.fork();
                    taskList.add(task);
                    mapList.add(linkName);
                    tree.addChild(child);

                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            for (IndexTask task : taskList) {
                indexSet.addAll(task.join());
            }

        }
        tree.addChild(tree);
        return indexSet;
    }


}