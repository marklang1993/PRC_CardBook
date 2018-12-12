package cn.prichan.Crawler;

import cn.prichan.Crawler.utility.FileUtility;
import cn.prichan.Crawler.utility.HttpUtility;

public class EntryPoint {

    public static void main(String[] args)
    {
        HttpUtility httpUtility = new HttpUtility();
        FileUtility fileUtility = new FileUtility();

        //fileUtility.WriteAllMetaData(httpUtility, true);
        fileUtility.DownloadAllImages(httpUtility, true);
    }
}
