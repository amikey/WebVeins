package com.xiongbeer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by shaoxiong on 17-4-11.
 */
public class Configuration {
    private static Configuration conf;
    private Logger logger = LoggerFactory.getLogger(Configuration.class);
    private HashMap<String, String> map = new HashMap<String, String>();

    public static String BLOOM_SAVE_PATH;
    public static String C_BLOOM_SAVE_PATH;
    public static String R_BLOOM_SAVE_PATH;

    public static String HDFS_ROOT;
    public static String WAITING_TASKS_URLS;
    public static String FINNSED_TASKS_URLS;
    public static String NEW_TASKS_URLS;
    public static String BLOOM_BACKUP_PATH;

    public static String TEMP_DIR;

    public static int WORKER_DEAD_TIME;
    public static int CHECK_TIME;

    public static String TEMP_SUFFIX = ".bak";

    private Configuration() throws SAXException, IOException, ParserConfigurationException {
        /* 读取配置信息失败，后续的任务肯定无法进行了 */
        if(!check("conf/core.xml")){
            System.exit(1);
        }
        init();
        parse();

        /* 初始化final变量 */
        BLOOM_SAVE_PATH = map.get("bloom_save_path");
        C_BLOOM_SAVE_PATH = map.get("c_bloom_save_path");
        R_BLOOM_SAVE_PATH = map.get("r_bloom_save_path");

        HDFS_ROOT = map.get("hdfs_root");
        WAITING_TASKS_URLS = map.get("waiting_tasks_urls");
        FINNSED_TASKS_URLS = map.get("finnsed_tasks_urls");
        NEW_TASKS_URLS = map.get("new_tasks_urls");
        BLOOM_BACKUP_PATH = map.get("bloom_backup_path");

        TEMP_DIR = map.get("temp_dir");

        WORKER_DEAD_TIME = Integer.parseInt(map.get("worker_dead_time"));
        CHECK_TIME = Integer.parseInt(map.get("check_time"));

    }

    public static synchronized Configuration getInstance() {
        if(conf == null){
            try {
                conf = new Configuration();
            } catch (SAXException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        return conf;
    }

    /**
     * 设置默认值
     */
    public void init(){
        map.put("bloom_save_path", "bloom/urlbloomvalue.dat");
        map.put("c_bloom_save_path", "bloom/urlbloomvalue_compressed.dat");
        map.put("r_bloom_save_path", "bloom/urlbloomvalue_ram.dat");

        map.put("hdfs_root", "/webveins");
        String root = map.get("hdfs_root");
        map.put("waiting_tasks_urls", root + "/tasks/waitingtasks");
        map.put("finnsed_tasks_urls", root + "/tasks/finnsedtasks");
        map.put("new_tasks_urls", root + "/tasks/newurls");
        map.put("bloom_backup_path", root + "/bloom");

        map.put("temp_dir", "temp");

        map.put("worker_dead_time" , "5");
        map.put("check_time", "60");
    }


    /**
     * 读取配置文件中的信息
     * 这段代码比较乱....看看以后能不能改得清楚些
     *
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public void parse() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        File file = new File("conf/core.xml");
        Document doc = builder.parse(file);
        Element root = (Element) doc.getDocumentElement();

        NodeList children = root.getChildNodes();
        for(int i=0; i<children.getLength(); ++i){
            Node child = children.item(i);
            if(child instanceof Element){
                Element childElement = (Element) child;
                if(childElement.getNodeName().equals("property")){
                    NodeList nodes = childElement.getChildNodes();
                    String name = null;
                    for(int j=0; j<nodes.getLength(); ++j){
                        Node node = nodes.item(j);
                        if(node instanceof  Element) {
                            Text textNode = (Text) node.getFirstChild();
                            String text = textNode.getData().trim();
                            if(node.getNodeName().equals("name")){
                                if(!map.containsKey(text)){
                                    throw new ParserConfigurationException("Invalid key: " + text);
                                }
                                name = text;
                            } else {
                                map.put(name, text);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean check(String url) throws SAXException {
        boolean result = false;
        /* 查找W3C XML Schema语言的工厂 */
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        /* 编译指定xsd文件的格式 */
        File schemaLocation = new File("conf/config.xsd");
        Schema schema = factory.newSchema(schemaLocation);

        /* 获取验证器 */
        Validator validator = schema.newValidator();

        /* 解析要查看的文档 */
        Source source = new StreamSource(url);

        /* 验证 */
        try {
            validator.validate(source);
            logger.info(url+" is valid.");
            result = true;
        }
        catch (SAXException ex) {
            logger.error(ex.getMessage());
            result = false;
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }


}
