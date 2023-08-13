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

public class Down_补充BOM清单JSON {
    private SaveUntil saveUntil = new SaveUntil();
    private ReadUntil readUntil = new ReadUntil();
    //主文件夹 路径
    private String MainPath = "F:\\A_解放_New\\下载2023_0801\\第一组\\";

    private String Lack_Path = "";
    private String Lack_One_JSONPath = "";
    private String Lack_Analysis_Path="";
    private String Lack_VINPath =MainPath+"\\数据缺失处理\\缺失的VIN.txt";
    private String Lack_JSON文件="";
    private String vin="";

    //用于下载的 payload参数
    private String mainurl = "http://jfepc.fawtc.com.cn:8080/QMERP/RemoteCallManager?fromClient=QMBS&ModuleName=epc&service=SMP1320Service";

    private String seesionID = "ba231568-84fd-4dcd-ac4a-2da7ff7eae47";
    private String actionID = "220e04b6-cbcc-421f-b02b-7d3f368171fc";
    private String guid = "b6329e5e-b2e0-4fdd-aa36-52bdc46faf6c";
    private String requestRandom = "";

    public ArrayList<String> Method_1_获取缺少的单个VIN(){
        ArrayList<String> VINList =new ArrayList<>();
        try{
            FileReader fileReader = new FileReader(this.Lack_VINPath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String Str ="";
            while ((Str=bufferedReader.readLine())!=null){
                VINList.add(Str);
            }
            bufferedReader.close();
            fileReader.close();
        }catch (Exception ex){
            System.out.println(ex.toString());
        }
        return VINList;
    }
    public void Method_2_创建整体结构文件夹(String vin) {
        this.vin = vin;
        this.Lack_Path = this.MainPath + "数据缺失处理";
        File file1 = new File(this.Lack_Path);
        if (!file1.exists()) {
            file1.mkdirs();
        }
        this.Lack_Analysis_Path = this.Lack_Path+"\\"+"JSON文件解析";
        File file3 = new File(this.Lack_Analysis_Path);
        if (!file3.exists()){
            file3.mkdirs();
        }
        this.Lack_JSON文件 = this.Lack_Path+"\\JSON文件下载";
        File file4 = new File(this.Lack_JSON文件);
        if (!file4.exists()){
            file4.mkdirs();
        }

        this.Lack_One_JSONPath =this.Lack_JSON文件+"\\"+this.vin;
        File file2 = new File(this.Lack_One_JSONPath);
        if (!file2.exists()){
            file2.mkdirs();
        }

    }

    public void Method_3_下载存储BOM数据() {
        //Error_Time 时间 用于记录 异常发生的时间
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String Error_Time = formatter.format(date);
        //1.拼装payload 中的重要参数
        //1.1  进行多重循环 用于解决分页问题  第一页尺寸不同 为500 后续页为1000
        //1.2  拼装 其他 参数 random随机生成
        this.actionID = this.actionID.substring(0,this.actionID.length()-12)+ RandomStringUtils.random(12,"123456789abcdefjhigklmnopqrstuvwxyz");
        this.requestRandom = RandomStringUtils.random(16,"123456789")+"0000";


        for (int i = 1; i < 300; i++) {
            this.guid = this.guid.substring(0,this.guid.length()-12)+RandomStringUtils.random(12,"123456789abcdefjhigklmnopqrstuvwxyz");
            int PagelenChose = 0;
            if (i == 1) {
                PagelenChose = 500;
            } else {
                PagelenChose = 1000;
            }
            String payload = "{\"serviceName\": \"SMP1320Service\",  \"methodName\": \"doSearch\",  \"client\": \"800\"," +
                    "\"userName\": \"HBQYK\", \"tran\": \"smp1000\",  \"language\": \"CN\",  " +
                    "\"sessionID\": \"" + this.seesionID + "\",  \"loginTime\": 1690713278405,  \"backendVersion\": \"\",  " +
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
                    saveUntil.Method_SaveFile_True(this.Lack_Path+"\\超过1010.txt", this.vin +"_"+Error_Time+ "\n");
                    break;
                } else {
                    System.out.println("! 还好没超过1010 !");
                }
                if (Itmes2.size() == 0) {
                    Thread.sleep(1000);
                    System.out.println("结束一个VIN下载");
                    break;
                }

                String tempString = this.Lack_One_JSONPath + "\\" + this.vin + "_" + i + ".text";
                File f = new File(tempString);
                if (!f.exists()) {
                    saveUntil.Method_SaveFile(this.Lack_One_JSONPath + "\\" + this.vin + "_" + i + ".text", resultJson);
                    System.out.println("进行了一次存储");
                } else {
                    System.out.println("已经存在了");
                }
            } catch (Exception ex) {
                //下载的时候 发生异常  或发送请求 发生异常
                saveUntil.Method_SaveFile_True(this.Lack_Path+"\\EPC下载VIN_ERROR.txt", this.vin +"_"+Error_Time+ "\t"+ex+"\n");
                System.out.println(ex.toString());
            }
        }
    }

    public void Method_4_解析单个JSON保存CSV() {
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
        try {
            List<String> nameList = readUntil.getFileNames(this.Lack_One_JSONPath + "\\");
            //先存为csv文件 存一个表头
            String titleOne = "VIN,nLevel,isUse,vMaterial,nMaterial,vAffectExchange,vMFRoutes,nqty" +
                    ",subvMaterial,ysbs,uppDate,changeDate,kgbs,snqty,bomOrder,id,vid,parentNodeCode" +
                    ",childID,levelNO,VACCORDFILE,vASRoutes,vremark,vpict,vpurchase,vFeatureInfo\n";
            saveUntil.Method_SaveFile_True(this.Lack_Analysis_Path + "\\" + this.vin + ".csv", titleOne.replace("\n", "").replace("\r", "") + "\n");

            for (int i = 0; i < nameList.size(); i++) {
                String JsonContent = readUntil.Method_ReadFile(this.Lack_One_JSONPath + "\\" + nameList.get(i));
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
                    }
                }
                BeanList.addAll(tempList);
            }
            for (int i = 0; i < BeanList.size(); i++) {
                Bean_JieFang bean = BeanList.get(i);
                System.out.println(BeanList.size());
                String tempString = this.vin + "," + bean.getnLevel() + "," + bean.getisUse() + "," + bean.getvMaterial() + "," + bean.getnMaterial() +
                        "," + bean.getvAffectExchange() + "," + bean.getvMFRoutes() + "," + bean.getnqty() + "," + bean.getsubvMaterial() + "," + bean.getysbs() +
                        "," + bean.getuppDate() + "," + bean.getchangeDate() + "," + bean.getkgbs() + "," + bean.getsnqty() + "," + bean.getbomOrder() + "," + bean.getid() +
                        "," + bean.getvid() + "," + bean.getparentNodeCode() + "," + bean.getchildID() + "." + bean.getlevelNO() + "," + bean.getVACCORDFILE() +
                        "," + bean.getvASRoutes() + "," + bean.getvremark() + "," + bean.getvpict() + "," + bean.getvpurchase() + "," + bean.getvFeatureInfo() + "\n";
                System.out.println(tempString);
                saveUntil.Method_SaveFile_True(this.Lack_Analysis_Path + "\\" + this.vin + ".csv", tempString.replace("\n", "").replace("\r", "") + "\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public static void main(String[] args) {
        Down_补充BOM清单JSON D= new Down_补充BOM清单JSON();
        ArrayList<String> VINList = D.Method_1_获取缺少的单个VIN();
        for (int i = 0; i < VINList.size(); i++) {
            D.Method_2_创建整体结构文件夹(VINList.get(i));
            //D.Method_3_下载存储BOM数据();
            D.Method_4_解析单个JSON保存CSV();

        }
    }
}
