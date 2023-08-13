package Down_Analysis;

import Entity.Bean_JieFang;
import Until.ReadUntil;
import Until.SaveUntil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;


public class Down_BOM清单JSON {
    //两个工具类  用于读写文件操作
    private SaveUntil saveUntil = new SaveUntil();
    private ReadUntil readUntil = new ReadUntil();

    //主文件夹路径
    private String MainPath = "F:\\A_解放_New\\下载2023_0801\\第二组\\";
    //需要在主路径下面,建立一个 -> "VIN_文件"的文件夹,并取其中具体的VIN文本文件为VIN_Path;
    private String VIN_Path = MainPath + "JH6-349.txt";
    //VIN BOM清单 全部层级 的JSON文件 下载路径
    private String BOM_JSon_FolderPath = "";
    //VIN 全部  单条解析 保存 地址
    private String BOM_One_JSONPath = "";
    private String BOM_Analysis_FolderPath = "";
    //用于存储   总的CSV / 分组CSV
    private String BOM_All_Analysis = "";
    //用于记录各种异常的出现  ->链接超时/数据为空/等情况等
    private String Error_Path = "";
    private String Lack_Path = "";
    private String vin = "";


    //这下面是 需要 更改的 payload 内容
    private String mainurl = "http://jfepc.fawtc.com.cn:8080/QMERP/RemoteCallManager?fromClient=QMBS&ModuleName=epc&service=SMP1320Service";

    private String seesionID = "d65a61c0-4dc8-4977-9b55-ea78acb7c337";
    private String actionID = "dc87a717-dc52-4365-8c13-875872005428";
    private String guid = "34a8dffd-b086-4631-b63b-962183847e2d";
    private String requestRandom = "";
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

    public void Method_4_下载存储BOM数据() {
        //Error_Time 时间 用于记录 异常发生的时间
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String Error_Time = formatter.format(date);
        //1.拼装payload 中的重要参数
        //1.1  进行多重循环 用于解决分页问题  第一页尺寸不同 为500 后续页为1000
        //1.2  拼装 其他 参数 random随机生成
        this.actionID = this.actionID.substring(0, this.actionID.length() - 12) + RandomStringUtils.random(12, "123456789abcdefjhigklmnopqrstuvwxyz");
        this.requestRandom = RandomStringUtils.random(16, "123456789") + "0000";

        for (int i = 1; i < 300; i++) {
            this.guid = this.guid.substring(0, this.guid.length() - 12) + RandomStringUtils.random(12, "123456789abcdefjhigklmnopqrstuvwxyz");
            int PagelenChose = 0;
            if (i == 1) {
                PagelenChose = 500;
            } else {
                PagelenChose = 1000;
            }
            String payload = "{\"serviceName\": \"SMP1320Service\",  \"methodName\": \"doSearch\",  \"client\": \"800\"," +
                    "\"userName\": \"HBQYK\", \"tran\": \"smp1000\",  \"language\": \"CN\",  " +
                    "\"sessionID\": \"" + this.seesionID + "\",  \"loginTime\": 1691128835043,  \"backendVersion\": \"\",  " +
                    "\"actionID\": \"" + actionID + "\",  " +
                    "\"guid\": \"" + guid + "\",  \"paraValues\":{" +
                    "\"keyword\": \"" + this.vin + "\",\"pageno\": " + i + ",\"pagelen\": " + PagelenChose + ",\"optionValue\": \"KEY02\",\"selectLevel\": 100," +
                    "\"requestRandom\": " + this.requestRandom + " }}";
            System.out.println(payload);
            try {
                Connection.Response res = Jsoup.connect(this.mainurl).method(Connection.Method.POST)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                        .requestBody(payload).ignoreContentType(true).execute();
                String resultJson = res.body();
                JSONObject Items1 = JSONObject.parseObject(resultJson);
                //System.out.println(Items1);
                JSONArray Itmes2 = (Items1.getJSONObject("result").getJSONObject("resultObj")).getJSONArray("curresult");
                System.out.println(" 内含数据条数  : " + Itmes2.size());
                if (Itmes2.size() > 1500) {
                    System.out.println("! 超过1010了! 出错了");
                    saveUntil.Method_SaveFile_True(this.Error_Path + "\\超过1010.txt", this.vin + "_" + Error_Time + "\n");
                    break;
                } else {
                    System.out.println("! 还好没超过1010 !");
                }
                if (Itmes2.size() == 0) {
                    Thread.sleep(500);
                    System.out.println("结束一个VIN下载");
                    break;
                }

                String tempString = this.BOM_One_JSONPath + "\\" + this.vin + "_" + i + ".text";
                File f = new File(tempString);
                if (!f.exists()) {
                    saveUntil.Method_SaveFile(this.BOM_One_JSONPath + "\\" + this.vin + "_" + i + ".text", resultJson);
                    System.out.println("进行了一次存储");
                } else {
                    System.out.println("已经存在了");
                }
            } catch (Exception ex) {
                //下载的时候 发生异常  或发送请求 发生异常
                saveUntil.Method_SaveFile_True(this.Error_Path + "\\EPC下载VIN_ERROR.txt", this.vin + "_" + Error_Time + "\t" + ex + "\n");
                System.out.println(ex.toString());
            }
        }
    }

    public void Method_5_解析单个JSON保存CSV() {
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
        //存储路径
        String savePath = this.BOM_Analysis_FolderPath + "\\" + this.vin + ".csv";
        File file = new File(savePath);
        try {
            List<String> nameList = readUntil.getFileNames(this.BOM_One_JSONPath + "\\");
            String titleOne = "VIN,nLevel,isUse,vMaterial,nMaterial,vAffectExchange,vMFRoutes,nqty" +
                    ",subvMaterial,ysbs,uppDate,changeDate,kgbs,snqty,bomOrder,id,vid,parentNodeCode" +
                    ",childID,levelNO,VACCORDFILE,vASRoutes,vremark,vpict,vpurchase,vFeatureInfo\n";
            if (!file.exists()) {
                saveUntil.Method_SaveFile_True(this.BOM_Analysis_FolderPath + "\\" + this.vin + ".csv", titleOne.replace("\n", "").replace("\t", "") + "\n");

                for (int i = 0; i < nameList.size(); i++) {
                    String JsonContent = readUntil.Method_ReadFile(this.BOM_One_JSONPath + "\\" + nameList.get(i));
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
                String AllData = "";
                for (int i = 0; i < BeanList.size(); i++) {
                    Bean_JieFang bean = BeanList.get(i);
                    System.out.println(BeanList.size());
                    String tempString = this.vin + "," + bean.getnLevel() + "," + bean.getisUse() + "," + bean.getvMaterial() + "," + bean.getnMaterial() +
                            "," + bean.getvAffectExchange() + "," + bean.getvMFRoutes() + "," + bean.getnqty() + "," + bean.getsubvMaterial() + "," + bean.getysbs() +
                            "," + bean.getuppDate() + "," + bean.getchangeDate() + "," + bean.getkgbs() + "," + bean.getsnqty() + "," + bean.getbomOrder() + "," + bean.getid() +
                            "," + bean.getvid() + "," + bean.getparentNodeCode() + "," + bean.getchildID() + "." + bean.getlevelNO() + "," + bean.getVACCORDFILE() +
                            "," + bean.getvASRoutes() + "," + bean.getvremark() + "," + bean.getvpict() + "," + bean.getvpurchase() + "," + bean.getvFeatureInfo() + "\n";
                    System.out.println(tempString);
                    AllData += tempString.replace("\"", "").replace("\n", "").replace("\r", "") + "\n";
                }
                saveUntil.Method_SaveFile_True(this.BOM_Analysis_FolderPath + "\\" + this.vin + ".csv", AllData);
            }else {
                System.out.println(" 已经存在");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Down_BOM清单JSON D = new Down_BOM清单JSON();
        D.Method_1_创建整体结构文件夹();
        ArrayList<String> VINList = D.Method_2_获取单个VIN();
        for (int i = 0; i < VINList.size(); i++) {
            D.Method_3_创建单个VIN文件夹(VINList.get(i));
            //D.Method_4_下载存储BOM数据();
            D.Method_5_解析单个JSON保存CSV();
        }
    }
}
