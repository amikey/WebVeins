package com.xiongbeer.webveins;

import com.xiongbeer.webveins.filter.bloom.UrlFilter;
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
import java.util.Map;

/**
 * Created by shaoxiong on 17-4-11.
 */
public class Configuration {
    private static Configuration conf;
    private Logger logger = LoggerFactory.getLogger(Configuration.class);
    private Map<String, String> map = new HashMap<String, String>();

    private static String confPath = System.getenv("WEBVEINS_HOME")+"conf/";

    /*
        manager端必须指定ZK使用的ip地址和端口号，
       就目前而言，manager也必须运行在提供zk服务的机器上

       map<ip地址, 端口号>
    */
    public static Map<String, Integer> ZOOKEEPER_MANAGER_ADDRESS
            = new HashMap<String, Integer>();;

    /* 常量的具体解释见后面的init() */
    public static String BLOOM_SAVE_PATH;
    public static String C_BLOOM_SAVE_PATH;
    public static String R_BLOOM_SAVE_PATH;
    public static String HDFS_ROOT;
    public static String WAITING_TASKS_URLS;
    public static String FINNSED_TASKS_URLS;
    public static String NEW_TASKS_URLS;
    public static String BLOOM_BACKUP_PATH;
    public static String HDFS_SYSTEM_PATH;
    public static String TEMP_DIR;
    public static int WORKER_DEAD_TIME;
    public static int CHECK_TIME;
    public static String TEMP_SUFFIX = ".bak";
    public static String LOCAL_HOST;
    public static int LOCAL_PORT;
    public static String INIT_SERVER;
    public static int BALANCE_SERVER_PORT;
    private static UrlFilter URL_FILTER;
    
    private Configuration() throws SAXException, IOException, ParserConfigurationException {
        /* 读取配置信息失败，后续的任务肯定无法进行了 */
    	logger.info("Checking...");
        if(!check(confPath+"core.xml")){
            System.exit(1);
        }
        logger.info("Loading default configuration...");
        init();
        logger.info("Loading user's configuration...");
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

        LOCAL_HOST = map.get("local_host");
        LOCAL_PORT = Integer.parseInt(map.get("local_port"));

        HDFS_SYSTEM_PATH = map.get("hdfs_system_path");

        BALANCE_SERVER_PORT = Integer.parseInt(map.get("balance_server_port"));

        INIT_SERVER = map.get("init_server");
    }

    /**
     * UrlFilter需要延迟初始化，因为
     * 只有manager需要持有它，而且它会
     * 占用大量硬盘或者内存空间
     * @return
     */
    public UrlFilter getUrlFilter(){
        long elementNums = Long.parseLong(map.get("bloom_filter_enums"));
        double falsePositiveRate = Double.parseDouble(
                map.get("bloom_filter_fpr"));
        if(URL_FILTER == null) {
            switch (map.get("bloom_filter")) {
                case "ram":
                    URL_FILTER = new UrlFilter(elementNums, falsePositiveRate,
                            UrlFilter.CreateMode.RAM);
                    break;
                case "disk":
                    URL_FILTER = new UrlFilter(elementNums, falsePositiveRate,
                            UrlFilter.CreateMode.DISK);
                    break;
                case "compressed_disk":
                    URL_FILTER = new UrlFilter(elementNums, falsePositiveRate,
                            UrlFilter.CreateMode.DISK_COMPRESSED);
                    break;
                default:
                    return null;
            }
        }
        return URL_FILTER;
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
        map.put("bloom_save_path", "data/bloom/urlbloomvalue.dat");
        map.put("c_bloom_save_path", "data/bloom/urlbloomvalue_compressed.dat");
        map.put("r_bloom_save_path", "data/bloom/urlbloomvalue_ram.dat");

        map.put("hdfs_root", "/webveins");
        String root = map.get("hdfs_root");
        map.put("waiting_tasks_urls", root + "/tasks/waitingtasks");
        map.put("finnsed_tasks_urls", root + "/tasks/finnsedtasks");
        map.put("new_tasks_urls", root + "/tasks/newurls");

        /* HDFS的连接路径 */
        map.put("hdfs_system_path", "hdfs://localhost:9000/");

        /* bloom过滤器会定时备份，此为其存放的路径 */
        map.put("bloom_backup_path", root + "/bloom");

        /* 临时文件（UrlFile）的存放的本地路径 */
        map.put("temp_dir", "data/temp");

        /* Worker与ZooKeeper断开连接后，经过DEADTIME后认为Worker死亡 */
        map.put("worker_dead_time" , "5");
        /* Manager进行检查的间隔 */
        map.put("check_time", "60");

        /* 本机ip Worker节点需要配置 */
        map.put("local_host" , "127.0.0.1");
        /* Worker服务使用的端口 Worker节点需要配置 */
        map.put("local_port", "22000");

        /* bloom过滤器的模式 */
        map.put("bloom_filter", "ram");
        /* bloom过滤器出错的概率 */
        map.put("bloom_filter_fpr", "0.0000001");
        /* bloom过滤器的预计最大容量 */
        map.put("bloom_filter_enums", "1000000");
        
        /* 提供均衡负载之前必须首先读取信息，需要一个用于初始化的BalanceServer的ip和端口号 */
        map.put("init_server", "127.0.0.1:2181");

        /* 均衡负载server端默认端口 */
        map.put("balance_server_port", "8080");
    }


    /**
     * 读取配置文件中的信息
     * 这段代码比较乱....看看以后能不能改得清楚些
     *
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public void parse() throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        File file = new File(confPath+"core.xml");
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
                                name = text;
                            } else {
                                if(!name.equals("zookeeper_manager_address")) {
                                    map.put(name, text);
                                }
                                else{
                                    String[] items = text.split(":");
                                    String ip = items[0];
                                    Integer port = new Integer(Integer.parseInt(items[1]));
                                    ZOOKEEPER_MANAGER_ADDRESS.put(ip, port);
                                }
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
        File schemaLocation = new File(confPath+"config.xsd");
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
