/*
 * Client
 */

package database;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
 
public class Client{
	
	static String userID = "";
    static String password = "";
    static String serverIP = "127.0.0.1";
    
    public static boolean IDcheck(DataOutputStream dout, DataInputStream din, String userID)
    {
    		JSONObject user = new JSONObject();
    		user.put("userID", userID);
    		try {
				dout.writeUTF(user.toString());
				String result = din.readUTF();
				if (result != "")
	    				return true;
	    			else
	    				return false;
    			} catch (IOException e) {
				// TODO Auto-generated catch block
				return false;
			}
 
    }
    
	public static String MD5(String str){

		String MD5 = ""; 

		try{

			MessageDigest md = MessageDigest.getInstance("MD5"); 

			md.update(str.getBytes()); 

			byte byteData[] = md.digest();

			StringBuffer sb = new StringBuffer(); 

			for(int i = 0 ; i < byteData.length ; i++){

				sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));

			}

			MD5 = sb.toString();

			

		}catch(NoSuchAlgorithmException e){

			e.printStackTrace(); 

			MD5 = null; 

		}

		return MD5;

	}	
    public static void main(String[] args){
        OutputStream out = null;
        FileInputStream fin;
        
        try{
        	    Socket soc = new Socket(serverIP,11111);
            System.out.println("Server Connected!");            //11111은 서버접속 포트입니다.
            out =soc.getOutputStream();                 		  //서버에 바이트단위로 데이터를 보내는 스트림을 개통합니다.
            DataOutputStream dout = new DataOutputStream(out); //OutputStream을 이용해 데이터 단위로 보내는 스트림을 개통합니다.
            InputStream in = soc.getInputStream();             //클라이언트로 부터 바이트 단위로 입력을 받는 InputStream을 얻어와 개통합니다.
            DataInputStream din = new DataInputStream(in);     //InputStream을 이용해 데이터 단위로 입력을 받는 DataInputStream을 개통합니다.
            
            
            Scanner s = new Scanner(System.in);   //파일 이름을 입력받기위해 스캐너를 생성합니다.
            while(true){
                String command = s.nextLine();	//키보드로 명령어를 입력받습니다.
                System.out.print(command);
                dout.writeUTF(command);			//서버에 명령어를 보냅니다.
                if (command.equalsIgnoreCase("Upload") | command.equalsIgnoreCase("Download"))		//명령이 업로드/다운로드 인경
                	{
	                String filename = s.next();    //스캐너를 통해 파일의 이름을 입력받고,
	                int port = din.readInt();	  //서버로부터 새로운 소켓의 포트번호를 받고 
	                new UpdownData(serverIP, port, filename, command).run(); //쓰레드를 새로 생성해서 서버와 통신합니다.
                	}
                
                else if (command.equalsIgnoreCase("Login"))	//명령어가 로그인인 경우,
                {
                	JSONObject user = new JSONObject();	//JSON 을 이용하여 필요한 정보를 저합니다.
                	user.put("userID", "userID");		//userID는 유저의 ID
                	user.put("password", "password");	//password는 유저의 password
                	dout.writeUTF(user.toString());		//JSON정보를 서버에 전달합니다.
                	String result = din.readUTF();		//로그인 결과를 서버로부터 받아옵니다.
                	if (result.equalsIgnoreCase("Success"))	//성공한 경우 전역변수에 유저 아이디와 비밀번호를 저장합니다.
                	{
                		userID = "userID";
                		password = "password";
                		System.out.println("Log in success");
                	}else								//로그인이 실패한 경우
                	{
                		System.out.println("Log in failed");
                	}
                }
                
                else if (command.equalsIgnoreCase("Signup"))	//명령어가 회원가입인 경우
                {
	                	JSONObject user = new JSONObject();	//JSON을 이용하여 유저 DB에 들어가야하는 정보를 저장합니다.
	                	user.put("userID", "userID");
	                	user.put("password", "password");
	                	user.put("name", "name");
	                	user.put("phoneNumber", "phoneNumber");
	                	user.put("address", "address");
	                	user.put("email", "email");
	                	user.put("gender", "male");
	                	user.put("totalStorage", "totalStorage");
	                	user.put("usageStorage", "usageStorage");
	                	dout.writeUTF(user.toString());
                }
                else if (command.equalsIgnoreCase("Search"))	//명령어가 검색 명령어인 경우.
                {
                		JSONObject searchKey = new JSONObject();	//JSON을 이용해서검색에 필요한 정보를 저장합니다.
                		searchKey.put("keyword", "userID");		//Keyword는 검색어.
                		searchKey.put("std", "user");			//std는 무엇을 기준으로 검색할 것인가.
                		searchKey.put("range", "0");				//range는 파일 검색의 공개 범위(0은 공개파일, 1은 개인파일, 그룹파일은 그룹 아이디로 검색)
                		dout.writeUTF(searchKey.toString());
                		JSONParser parser = new JSONParser();	//서버에서 보내온 정보를 가지고 파싱.
                		JSONObject result = (JSONObject)parser.parse(din.readUTF());
                		JSONArray files = (JSONArray)result.get("files");
                		for(int i=0; i<files.size(); i++)		//파일 1개당 해당 파일의 정보를 출력.
                		{
                			JSONObject file = (JSONObject)files.get(i);
                			System.out.println(file.get("userID"));
                			System.out.println(file.get("fileID"));
                			System.out.println(file.get("fileName"));
                			System.out.println(file.get("type"));
                			System.out.println(file.get("category"));
                			System.out.println(file.get("directory"));
                			System.out.println(file.get("size"));
                			System.out.println(file.get("date"));
                			System.out.println(file.get("shareOffset"));
                			System.out.println(file.get("download"));
                		}
                }
                else if (command.equalsIgnoreCase("Private2Public") | command.equalsIgnoreCase("Public2Private"))	//개인 파일을 공개파일로 변경하는 경우나, 공개파일을 개인파일로 변경하는 경
                {
                		JSONObject info = new JSONObject();	//JSON을 이용해서 유저 아이디와 파일 아이디를 저장합니다.
                		info.put("userID", "userID");
                		info.put("fileID", "e4c2710e4c3a1ac59081596970ce0f15");
                		dout.writeUTF(info.toString());
                }
                else if (command.equalsIgnoreCase("CreateGroup"))	//명령어가 그룹을 만드는 명령어인 경우.
                {
                		JSONObject groupinfo = new JSONObject();		//JSON을 이용해서 그룹 이름, 그룹을 만드려는 유저의 이름, 그룹 아이디를 저장합니다.
                		groupinfo.put("groupName", "Test");
                		groupinfo.put("userID", "userID");
                		groupinfo.put("groupID", MD5("userID"+(new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime())+"Test")));
                		dout.writeUTF(groupinfo.toString());
                }
                else if (command.equalsIgnoreCase("AddMember"))	//명령어가 기존의 그룹에 새로운 유저를 추가하는 명령어인 경우.
                {
                		JSONObject meminfo = new JSONObject();		//JSON을 이용해서 그룹 아이디, 초대하려는 유저 아이디를 전달합니다.
                		meminfo.put("groupID", "247e807f1f64fb2b783441bf729615e9");
                		meminfo.put("userID", "userID2");
                		dout.writeUTF(meminfo.toString());
                }
                else if (command.equalsIgnoreCase("OutMember"))	//명령어가 기존의 그룹에서 나가려는 명령어인 경우.
                {
                		JSONObject meminfo = new JSONObject();		//JSON을 이용해서 나가려는 그룹의 그룹아이디, 나가려는 유저의 유저아이디를 저장합니다.
                		meminfo.put("groupID", "247e807f1f64fb2b783441bf729615e9");
                		meminfo.put("userID", "userID2");
                		dout.writeUTF(meminfo.toString());
                }
                else if (command.equalsIgnoreCase("Private2Group") || command.equalsIgnoreCase("Group2Private") || command.equalsIgnoreCase("Public2Group") || command.equalsIgnoreCase("Group2Public"))	//명령어가 공개/비공개 파일을 그룹 공개 파일로 전환하려는 경우나, 그룹 공개 파일을 공개/비공개 파일로 전환하려는 경우.
                {
                		JSONObject fileinfo = new JSONObject();	//JSON을 이용해서 전환하려는 파일의 파일 아이디, 유저 아이디, 그룹 아이디를 저장합니다.
                		fileinfo.put("fileID", "e4c2710e4c3a1ac59081596970ce0f15");
                		fileinfo.put("userID", "userID");
                		fileinfo.put("groupID", "247e807f1f64fb2b783441bf729615e9");
                		dout.writeUTF(fileinfo.toString());
                }
                else if (command.equalsIgnoreCase("SearchUser"))
                {
                		JSONObject fileinfo = new JSONObject(); //JSON을 이용해서 검색하려는 유저 아이디 저장.
                		fileinfo.put("userID", "userID");
                		dout.writeUTF(fileinfo.toString());
                		JSONParser parser = new JSONParser();
                		JSONObject user = (JSONObject)parser.parse(din.readUTF());
                		System.out.println(user.get("userID"));	//시험삼아 유저아이디를 검색하면 유저의 모든 정보를 출력하도록 만듬.
                		System.out.println(user.get("name"));
                		System.out.println(user.get("phoneNumber"));
                		System.out.println(user.get("address"));
                		System.out.println(user.get("email"));
                		System.out.println(user.get("gender"));
                		System.out.println(user.get("totalStorage"));
                		System.out.println(user.get("usageStorage"));
                		
                }
                else if (command.equalsIgnoreCase("IDcheck")) //혹시 명령어가 아이디 중복 체크라면.
                {
                		System.out.println(IDcheck(dout, din, "userID"));		//ID중복 체크를 해주는 함수를 호출한다.
                		
                }
                else if (command.equalsIgnoreCase("Quit"))	//만약 종료하는 명령어인 경
                {
	                	out.close();
	                	soc.close();
	                	dout.close();
	                	din.close();
	                	in.close();
	                	System.exit(0);
                }
            }
        }
		                catch(Exception e){
			       }
        
    }
    
    private static class UpdownData extends Thread
    {
    	   private OutputStream out = null;
    	   private DataOutputStream dout = null;
    	   private InputStream in = null;
    	   private DataInputStream din = null;
    	   private Socket data = null;
    	   private String filename = null;
    	   private String command = null;
    	   
    	   public UpdownData(String serverIP, int port, String filename, String command)
    	   {
    		   try {
				Thread.sleep(100);
			   System.out.println(port);
			   this.data = new Socket(serverIP, port);
	    		   this.filename = filename;
	    		   this.command = command;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	   }
    	
    	   public void run()
    	   {
    		   
    		   try {
    		out =this.data.getOutputStream();                 //서버에 바이트단위로 데이터를 보내는 스트림을 개통합니다.
        dout = new DataOutputStream(out); //OutputStream을 이용해 데이터 단위로 보내는 스트림을 개통합니다.
        in = this.data.getInputStream();                //클라이언트로 부터 바이트 단위로 입력을 받는 InputStream을 얻어와 개통합니다.
        DataInputStream din = new DataInputStream(in);  //InputStream을 이용해 데이터 단위로 입력을 받는 DataInputStream을 개통합니다.
        
            if (command.equalsIgnoreCase("Upload"))		//업로드 명령어인 경우.
            	{
            		File f = new File(filename);
                FileInputStream fin = new FileInputStream(f); //FileInputStream - 파일에서 입력받는 스트림을 개통합니다.
                JSONObject fileinfo = new JSONObject();
		        byte[] buffer = new byte[1024];        //바이트단위로 임시저장하는 버퍼를 생성합니다.
		        int len;                               //전송할 데이터의 길이를 측정하는 변수입니다.
		        long data = f.length();                            //전송횟수, 용량을 측정하는 변수입니다.
		        if(data%1024 != 0)
		        {
		        		data = data/1024 + 1;
		        }else
		        {
		        		data = data/1024;
		        }
        
		        long datas = data;                      //아래 for문을 통해 data가 0이되기때문에 임시저장한다.
		        
		        fileinfo.put("userID", "userID");
		        fileinfo.put("fileID", MD5("userID"+filename+"directory"));
		        fileinfo.put("type", Files.probeContentType(Paths.get(filename)));
		        fileinfo.put("category", "Movie");
		        fileinfo.put("directory", "directory");
		        fileinfo.put("size", datas);
		        fileinfo.put("date", new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()));
		        fileinfo.put("shareOffset", "1");
		        
		        dout.writeUTF(fileinfo.toString());
		        dout.writeLong(data);                   //데이터 전송횟수를 서버에 전송하고,
		        dout.writeUTF(filename);               //파일의 이름을 서버에 전송합니다.
		        String e = din.readUTF();
		        
		        if(e.equalsIgnoreCase("EXIST"))
		        {
		        		System.out.println("File already exist");
		        }else
		        {
		         len = 0;
		        
		        for(;data>0;data--){                   //데이터를 읽어올 횟수만큼 FileInputStream에서 파일의 내용을 읽어옵니다.
		            len = fin.read(buffer);        //FileInputStream을 통해 파일에서 입력받은 데이터를 버퍼에 임시저장하고 그 길이를 측정합니다.
		            out.write(buffer,0,len);       //서버에게 파일의 정보(1kbyte만큼보내고, 그 길이를 보냅니다.
		        }
		        
		        System.out.println("약 "+datas+" kbyte");
		        }
            	}
            else if (command.equalsIgnoreCase("Download"))
            {
            		JSONObject fileinfo = new JSONObject();
            		fileinfo.put("name", filename);
            		fileinfo.put("user", "userID");
            		fileinfo.put("range", "1");
        	        dout.writeUTF(fileinfo.toString());
        	        Long data = din.readLong();           //Int형 데이터를 전송받습니다.
        	        File file = new File("/Users/Knight/test.pptx");             //입력받은 File의 이름으로 복사하여 생성합니다.
        	        out = new FileOutputStream(file);           //생성한 파일을 클라이언트로부터 전송받아 완성시키는 FileOutputStream을 개통합니다.
        	 
        	        Long datas = data;                            //전송횟수, 용량을 측정하는 변수입니다.
        	        byte[] buffer = new byte[1024];        //바이트단위로 임시저장하는 버퍼를 생성합니다.
        	        int len;                               //전송할 데이터의 길이를 측정하는 변수입니다.
        	        
        	        
        	        for(;data>0;data--){                   //전송받은 data의 횟수만큼 전송받아서 FileOutputStream을 이용하여 File을 완성시킵니다.
        	            len = in.read(buffer);
        	            out.write(buffer,0,len);
        	        }
        	        System.out.println("약: "+datas+" kbps");
        	        out.flush();
        	        System.out.println("Download Finish");
            }
    		   }
	                catch(Exception e){
		       }
    }
}
}
