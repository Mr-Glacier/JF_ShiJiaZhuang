package Down_Analysis;

import Entity.Bean_JieFang;
import Until.ReadUntil;
import Until.SaveUntil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Analysis_BOM清单JSON {
    //两个工具类  用于读写文件操作 编码格式为GB2312 解析存储 也是如此
    private SaveUntil saveUntil = new SaveUntil();
    private ReadUntil readUntil = new ReadUntil();

    //主文件夹路径
    private String MainPath = "F:\\A_解放_New\\下载2023_0729\\";
    //需要在主路径下面,建立一个 -> "VIN_文件"的文件夹,并取其中具体的VIN文本文件为VIN_Path;
    private String VIN_Path = MainPath+"VIN_文件\\解析错误VIN.txt";
    //VIN BOM清单 全部层级 的JSON文件 下载路径
    private String BOM_JSon_FolderPath = "";
    //VIN 全部  单条解析 保存 地址

    private String BOM_One_JSONPath = "";
    private String BOM_Analysis_FolderPath = "";
    //用于存储   总的CSV / 分组CSV
    private String BOM_All_Analysis = "";
    //用于记录各种异常的出现  ->链接超时/数据为空/等情况等   +VIN无数据 情况 记录
    private String Error_Path = "";
    private String Lack_Path = "";
    private String vin = "";

    /*
    整体思路:
    1.获取VIN文件的单个VIN
    2.创建相关文件夹
    3.下载BOM清单数据 ->保存到 BOM_JSon_FolderPath 路径下 命名格式为 VIN.txt
    4.解析 单个 VIN的JSON数据->导出CSV文件->存到本地 BOM_Analysis_FolderPath 路径下  命名格式为 VIN.csv
    5.解析 全部 VIN的JSON数据(并不建议太大).导入数据库麻烦,建议根据数量分组进行
     */
    public void Method_1_创建整体结构文件夹() {
        this.BOM_JSon_FolderPath = this.MainPath + "BOM清单_JSON数据";
        File file1 = new File(this.BOM_JSon_FolderPath);
        if (!file1.exists()) {
            file1.mkdirs();
        }

        this.BOM_Analysis_FolderPath = this.MainPath + "BOM清单_单个解析";
        File file2 = new File(this.BOM_Analysis_FolderPath);
        if (!file2.exists()) {
            file2.mkdirs();
        }

        this.BOM_All_Analysis = this.MainPath + "BOM清单_总CSV解析";
        File file3 = new File(this.BOM_All_Analysis);
        if (!file3.exists()) {
            file3.mkdirs();
        }

        this.Error_Path = this.MainPath + "异常_记录";
        File file4 = new File(this.Error_Path);
        if (!file4.exists()) {
            file4.mkdirs();
        }
        this.Lack_Path = this.MainPath + "数据缺失处理";
        File file5 = new File(this.Lack_Path);
        if (!file5.exists()) {
            file5.mkdirs();
        }
    }
    public ArrayList<String> Method_2_获取单个VIN() {
        ArrayList<String> VINList = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(this.VIN_Path);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String Str;
            while ((Str = bufferedReader.readLine()) != null) {
                VINList.add(Str);
            }
            bufferedReader.close();
            fileReader.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return VINList;
    }
    public void Method_3_创建单个VIN文件夹(String vin) {
        this.vin = vin;
        this.BOM_One_JSONPath = this.BOM_JSon_FolderPath + "\\" + this.vin;
        File file_One = new File(this.BOM_One_JSONPath);
        if (!file_One.exists()) {
            file_One.mkdirs();
        }
    }
    public void Method_4_解析JSON保存单个CSV文件() {
        //使用 HashMap  进行赋值 规范整体行数 以及列名
        Map<String, String> columnMap = new HashMap<>();
        columnMap.put("nLevel", "nLevel");
        columnMap.put("isUse", "isUse");
        columnMap.put("vMaterial", "vMaterial");
        columnMap.put("nMaterial", "nMaterial");
        columnMap.put("vAffectExchange", "vAffectExchange");
        columnMap.put("vMFRoutes", "vMFRoutes");
        columnMap.put("nqty", "nqty");
        columnMap.put("subvMaterial", "subvMaterial");
        columnMap.put("ysbs", "ysbs");
        columnMap.put("uppDate", "uppDate");
        columnMap.put("changeDate", "changeDate");
        columnMap.put("kgbs", "kgbs");
        columnMap.put("snqty", "snqty");
        columnMap.put("bomOrder", "bomOrder");
        columnMap.put("id", "id");
        columnMap.put("vid", "vid");
        columnMap.put("parentNodeCode", "parentNodeCode");
        columnMap.put("childID", "childID");
        columnMap.put("levelNO", "levelNO");
        columnMap.put("VACCORDFILE", "VACCORDFILE");
        columnMap.put("vASRoutes", "vASRoutes");
        columnMap.put("vremark", "vremark");
        columnMap.put("vpict", "vpict");
        columnMap.put("vpurchase", "vpurchase");
        columnMap.put("vFeatureInfo", "vFeatureInfo");

        ArrayList<Bean_JieFang> BeanList = new ArrayList<>();
        //声明一个总的 BeanList 通过读取 进行存储
        try {
            //读取单个的JSON 文件夹下面所有文件名字
            List<String> nameList = readUntil.getFileNames(this.BOM_JSon_FolderPath + "\\");
            String titleOne = "VIN,nLevel,isUse,vMaterial,nMaterial,vAffectExchange,vMFRoutes,nqty" +
                    ",subvMaterial,ysbs,uppDate,changeDate,kgbs,snqty,bomOrder,id,vid,parentNodeCode" +
                    ",childID,levelNO,VACCORDFILE,vASRoutes,vremark,vpict,vpurchase,vFeatureInfo\n";
            //读取前先设置一下 表头  列名
            saveUntil.Method_SaveFile_True(this.BOM_Analysis_FolderPath+"\\" + this.vin + ".csv", titleOne.replace("\n", "").replace("\r", "")+"\n");
            //进入循环读取单个VIN 的 单个JSON文件
            for (int i = 0; i < nameList.size(); i++) {
                //读取单个VIN  JOSN文件
                String JsonContent = readUntil.Method_ReadFile(this.BOM_JSon_FolderPath + "\\" + nameList.get(i));
                JSONObject jsonObject = JSON.parseObject(JsonContent);
                JSONArray jsonArray = jsonObject.getJSONObject("result").getJSONObject("resultObj").getJSONArray("curresult");

                //取出第一个JsonArray得到列名 存到TitleList   当作key使用
                ArrayList<String> titleList = new ArrayList<>();
                JSONArray Items = jsonArray.getJSONArray(1);
                for (int j = 0; j < Items.size(); j++) {
                    String title = Items.getString(j);
                    System.out.println(title);
                    titleList.add(title);
                }
                //构建空的BeanList
                ArrayList<Bean_JieFang> tempList = new ArrayList<>();
                for (int j = 2; j < jsonArray.size(); j++) {
                    Bean_JieFang bean_jieFang = new Bean_JieFang();
                    bean_jieFang.setVIN(this.vin);
                    tempList.add(bean_jieFang);
                }
                //进入具体数据, 将title当作key 取得的值当作 value  使用hashMap赋值
                for (int j = 2; j < jsonArray.size(); j++) {
                    JSONArray ItemsData = jsonArray.getJSONArray(j);
                    for (int k = 0; k < ItemsData.size(); k++) {
                        String thetitle = titleList.get(k);
                        String value = ItemsData.getString(k);
                        //System.out.println(thetitle + "_____" + value);
                        Class c = tempList.get(j - 2).getClass();
                        Field field = c.getDeclaredField(columnMap.get(thetitle));
                        field.setAccessible(true);
                        field.set(tempList.get(j - 2), value);
                        //System.out.println("赋值一次");
                    }
                }
                BeanList.addAll(tempList);
                tempList.clear();
            }
            //读取BeanList 内容 并存储
            for (int i = 0; i < BeanList.size(); i++) {
                Bean_JieFang bean = BeanList.get(i);
                System.out.println("内含数据个数 : "+BeanList.size());
                String tempString = this.vin + "," + bean.getnLevel() + "," + bean.getisUse() + "," + bean.getvMaterial() + "," + bean.getnMaterial() +
                        "," + bean.getvAffectExchange() + "," + bean.getvMFRoutes() + "," + bean.getnqty() + "," + bean.getsubvMaterial() + "," + bean.getysbs() +
                        "," + bean.getuppDate() + "," + bean.getchangeDate() + "," + bean.getkgbs() + "," + bean.getsnqty() + "," + bean.getbomOrder() + "," + bean.getid() +
                        "," + bean.getvid() + "," + bean.getparentNodeCode() + "," + bean.getchildID() + "." + bean.getlevelNO() + "," + bean.getVACCORDFILE() +
                        "," + bean.getvASRoutes() + "," + bean.getvremark() + "," + bean.getvpict() + "," + bean.getvpurchase() + "," + bean.getvFeatureInfo() + "\n";
                System.out.println(tempString);
                saveUntil.Method_SaveFile_True(this.BOM_Analysis_FolderPath+"\\" + this.vin + ".csv", tempString.replace('"',' ').replace("\n", "").replace("\r", "")+"\n");
            }
        } catch (Exception ex) {
            saveUntil.Method_SaveFile_True(this.Error_Path+"\\单个解析异常.txt",this.vin+"\n");
            ex.printStackTrace();
        }
    }
    public static void main(String[] args) {
        Analysis_BOM清单JSON AS_BOM = new Analysis_BOM清单JSON();
        AS_BOM.Method_1_创建整体结构文件夹();
       ArrayList<String> VINList =  AS_BOM.Method_2_获取单个VIN();
        for (int i = 0; i < VINList.size(); i++) {
            AS_BOM.Method_3_创建单个VIN文件夹(VINList.get(i));
            AS_BOM.Method_4_解析JSON保存单个CSV文件();
        }
    }
}
