package com.xiongbeer.webveins.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiongbeer.webveins.api.jsondata.JData;
import io.bretty.console.table.Alignment;
import io.bretty.console.table.ColumnFormatter;
import io.bretty.console.table.Precision;
import io.bretty.console.table.Table;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by shaoxiong on 17-5-13.
 */
public class OutputFormatter {
    List<JData> dataSet;
    boolean fullPrint = false;
    final int LIMIT = 30;

    public OutputFormatter(List<JData> dataSet){
        this.dataSet = dataSet;
    }

    public void setFullPrint(boolean flag){
        fullPrint = flag;
    }

    public String format(){
        return getFormatTable();
    }

    private String getFormatTable(){
        int limit = fullPrint?Integer.MAX_VALUE:LIMIT;
        int boundLen = 0;
        List<Integer> colMaxLenCounter = new LinkedList<Integer>();
        List<List<String>> content = new LinkedList<List<String>>();
        List<String> keys = new LinkedList<String>();
        int counter = 0;
        boolean full = true;
        /* 读取制表数据 */
        for(JData data:dataSet){
            if(counter >= limit){
                full = false;
                break;
            }
            int colCounter = 0;
            JSONObject object = (JSONObject) JSON.toJSON(data);
            Iterator iterator = object.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry entry = (Map.Entry) iterator.next();
                if(colMaxLenCounter.size() == colCounter){
                    String key = entry.getKey().toString();
                    String value = entry.getValue().toString();
                    List<String> col = new LinkedList<String>();
                    keys.add(key);
                    col.add(value);
                    content.add(col);

                    int keyLen = key.length();
                    int valueLen = value.length();
                    colMaxLenCounter.add(keyLen>valueLen?keyLen:valueLen);
                }
                else{
                    String value = entry.getValue().toString();
                    int preMaxLen = colMaxLenCounter.get(colCounter);
                    int curLen = value.length();
                    colMaxLenCounter.set(colCounter
                            , curLen>preMaxLen?curLen:preMaxLen);
                    List<String> col = content.get(colCounter);
                    col.add(value);
                }
                colCounter++;
            }
            counter++;
        }

        /* 制表 */
        /* 先额外加一行id信息 */
        Table.Builder builder = null;
        int tableSize = keys.size();
        Integer[] id = new Integer[counter];
        for(int i=1; i<=counter; ++i){
            id[i-1] = i;
        }
        int nwidth = 4;
        int foo = 10;
        while(tableSize/foo != 0){
            nwidth++;
            foo*=10;
        }
        ColumnFormatter<Number> nformatter
                = ColumnFormatter.number(Alignment.CENTER, nwidth, Precision.ZERO);
        builder = new Table.Builder(" id "
                , id, nformatter);

        /* 加入内容 */
        boundLen += (nwidth + 1 +tableSize + 1);
        for(int i=0; i<tableSize; ++i){
            int width = colMaxLenCounter.get(i) + 2;
            boundLen += width;
            ColumnFormatter<String> sformatter
                    = ColumnFormatter.text(Alignment.CENTER, width);
            builder.addColumn(keys.get(i)
                    , content.get(i).toArray(new String[content.get(i).size()]), sformatter);
        }

        /* 加上显示信息和表的边界 */
        String separator = System.getProperty("line.separator");
        String prefix = "total " + dataSet.size()
                + " results," + " show " + counter
                + " of them on console." + separator;
        StringBuilder bound = new StringBuilder();
        for(int i=0; i<boundLen; ++i){
            if(i == 0){
                bound.append(separator + "+");
            }
            else if(i == boundLen-1){
                bound.append("+" + separator);
            }
            else{
                bound.append('-');
            }
        }

        return prefix
                + bound.toString()
                + builder.build().toString()
                + bound.toString();
    }
}
